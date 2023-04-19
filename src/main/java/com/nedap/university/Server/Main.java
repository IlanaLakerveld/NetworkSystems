package com.nedap.university.Server;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * This starts the server.
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

            System.out.println("Local port is : " + socket.getLocalPort());
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
