package com.nedap.university.Server;

import com.nedap.university.Fileclass;
import com.nedap.university.MakePacket;
import com.nedap.university.Receiver;
import com.nedap.university.Sending;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * This is the server side. The request from the clients are come in here.
 * Type of request the server can handel : GET,SEND,REMOVE,(REPLACE)
 */

public class Main {

    public static boolean keepAlive = true;
    private static boolean running = false;


    public static void main(String[] args) {

        // setup
        running = true;
        System.out.println("Hello, Ilana ");
        int port = 62830;
        try (DatagramSocket socket = new DatagramSocket(port)) {

            System.out.println("local port is : " + socket.getLocalPort());
            initShutdownHook();
            Server server = new Server() ;
            server.activeServer(socket);

        } catch (SocketException e) {
            System.out.println("Server is closed, Something wrong with making the socket");

        }

        System.out.println("Stopped");
        running = false;
    }

    private static void initShutdownHook() {
        final Thread shutdownThread = new Thread() {
            @Override
            public void run() {
                keepAlive = false;
                while (running) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }


}
