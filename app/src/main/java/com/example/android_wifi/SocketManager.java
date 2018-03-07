package com.example.android_wifi;
/*
 * Created by NOT on 3/5/18.
 */

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;



public class SocketManager {

    public static final int SOCKETPORT = 8080;

    class startServer{

        AsyncTask<Void,Void,Void> asyncTask;
        private boolean isActive = true;

        @SuppressLint("StaticFieldLeak")
        public void startServer(){
            isActive = true;
            asyncTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    byte[] message = new byte[4096];
                    DatagramPacket packet = new DatagramPacket(message,message.length);
                    DatagramSocket socket = null;

                    try{
                        socket = new DatagramSocket(SOCKETPORT);
                        while (isActive){
                            socket.receive(packet);

                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    finally {
                        if(socket != null){
                            socket.close();
                        }
                    }

                    return null;
                }
            };

            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }
        void stopServer(){
            isActive = false;
        }

    }

    class UDPClient{

        AsyncTask<Void,Void,Void> asyncTask;
        String message;
        InetAddress address;

        @SuppressLint("StaticFieldLeak")
        public void startClient(){
            asyncTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    DatagramSocket socket = null;
                    try{
                        socket = new DatagramSocket();
                        DatagramPacket packet = new DatagramPacket(message.getBytes(),message.length(),address, SOCKETPORT);
                        socket.send(packet);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    finally {
                        if(socket != null){
                            socket.close();
                        }
                    }
                    return null;
                }
            };
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
}