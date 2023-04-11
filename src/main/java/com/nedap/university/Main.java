package com.nedap.university;

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

    private static boolean keepAlive = true;
    private static boolean running = false;




    public static void main(String[] args) {
        // setup
        running = true;
        System.out.println("Hello, Nedap University! ilana ");
        int port = 62828;
        DatagramSocket socket;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        System.out.println("local port is : " + socket.getLocalPort());
        initShutdownHook();

        // waiting for input
        while (keepAlive) {
            try {
                byte[] buffer = new byte[512]; // this is the maximum a packet size you can receive
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);  // waiting for the request

                // todo how do you want to split
                String a = new String(buffer, MakePacket.personalizedHeaderLength, request.getLength());
                String[] splittedLine = a.split("~");

                if(splittedLine[0].equals("SEND")){
                    System.out.println("start receiving");
                    ReceiveFile(request, socket, splittedLine[1]);
                }
                else{
                    GETAnswers(request, socket, a  );
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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



    private static void GETAnswers(DatagramPacket request, DatagramSocket socket, String filename) throws IOException {
//        File file = new File(filename);
        File file = new File("/Users/ilana.lakerveld/Documents/NetworkSystems/project/nu-module-2-mod2.2023/example_files/medium.pdf");
        byte[] bytefile = Fileclass.loadFile(file);
        Sending send = new Sending();
        send.sending(bytefile,socket, request.getAddress(),request.getPort());


    }

    private static void ReceiveFile(DatagramPacket request, DatagramSocket socket,String filename) throws IOException {
//        File file = new File(filename);
//        if (file.exists()) {
            System.out.println("file already exist");
            // todo maak hier iets dat zegt met een vraag wil je dit overschijven?
//        }
//        else {
            // toDo change are magic numbers
            byte[] ack = MakePacket.makePacket(new byte[]{1},0,0,(byte) 0,0,0);

            DatagramPacket packet = new DatagramPacket(ack,0,ack.length,request.getAddress(),request.getPort()) ;
            socket.send(packet);

            Receiver receiver = new Receiver();
            byte[] receivedfile = receiver.receiver(socket, request.getAddress(), request.getPort());
            Fileclass.makeFileFromBytes(filename, receivedfile);
//        }

    }
}
