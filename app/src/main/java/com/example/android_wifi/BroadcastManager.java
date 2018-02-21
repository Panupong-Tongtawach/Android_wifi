/*
  Created by NOT on 2/12/18.
 */

package com.example.android_wifi;

import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;



class BroadcastManager {

    // Send
    final int BROADCASTPORT = 1234;
    final int SERVERPORT = 1235;

    public void startBroadcastBeacon() {

        TimerTask doAsynchronousTask;
        final Handler handler = new Handler();
        Timer timer = new Timer();

        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        sendBroadcast("Hello from wifiTimer!");
                    }
                });
            }
        };

        timer.schedule(doAsynchronousTask, 0,getBroadCastInterval()); //put the time you want
    }

    long getBroadCastInterval(){
        return  3000;
    }

    void sendBroadcast(String messageStr) {
        // Hack Prevent crash (sending should be done using an async task)

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] sendData = messageStr.getBytes();
            InetAddress broadcastAddr = getBroadcast(getIpAddress());
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddr, BROADCASTPORT);
            socket.send(sendPacket);
            System.out.println(getClass().getName() + "Broadcast packet sent to: " + broadcastAddr.getHostAddress());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }

    private InetAddress getIpAddress() {
        InetAddress inetAddress = null;
        InetAddress myAddr = null;

        try {
            for (Enumeration<NetworkInterface> networkInterface = NetworkInterface.getNetworkInterfaces(); networkInterface.hasMoreElements();) {

                NetworkInterface singleInterface = networkInterface.nextElement();

                for (Enumeration < InetAddress > IpAddresses = singleInterface.getInetAddresses(); IpAddresses
                        .hasMoreElements();) {
                    inetAddress = IpAddresses.nextElement();

                    if (!inetAddress.isLoopbackAddress() && (singleInterface.getDisplayName()
                            .contains("wlan0") ||
                            singleInterface.getDisplayName().contains("eth0") ||
                            singleInterface.getDisplayName().contains("ap0"))) {

                        myAddr = inetAddress;
                    }
                }
            }

        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return myAddr;
    }

    private InetAddress getBroadcast(InetAddress inetAddr) {

        NetworkInterface temp;
        InetAddress iAddr = null;
        try {
            temp = NetworkInterface.getByInetAddress(inetAddr);
            List<InterfaceAddress> addresses = temp.getInterfaceAddresses();

            for (InterfaceAddress inetAddress: addresses)

                iAddr = inetAddress.getBroadcast();
            Log.d(TAG, "iAddr=" + iAddr);
            return iAddr;

        } catch (SocketException e) {

            e.printStackTrace();
            Log.d(TAG, "getBroadcast" + e.getMessage());
        }
        return null;
    }

    // Listen

    void listenBroadcast(){
        Thread mThread = new Thread(new BroadcastListenerThread());
        mThread.start();
    }

    class BroadcastListenerThread implements Runnable {

        @Override
        public void run() {

            try {
                //Keep a socket open to listen to all the UDP traffic that is destined for this port
                DatagramSocket socket = new DatagramSocket(BROADCASTPORT, InetAddress.getByName("0.0.0.0"));
                socket.setBroadcast(true);

                while (true) {
                    Log.i(TAG,"Ready to receive broadcast packets!");

                    //Receive a packet
                    byte[] recvBuf = new byte[15000];
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.receive(packet);

                    //Packet received
                    Log.i(TAG, "Packet received from: " + packet.getAddress().getHostAddress());
                    String data = new String(packet.getData()).trim();
                    Log.i(TAG, "Packet received; data: " + data);
                }

            } catch (IOException ex) {
                Log.i(TAG, "Oops" + ex.getMessage());
            }
        }
    }
}