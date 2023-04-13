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

    private static boolean keepAlive = true;
    private static boolean running = false;




    public static void main(String[] args) {
        // setup
        running = true;
        System.out.println("Hello, Nedap University! ilana ");
        int port = 62830;
        DatagramSocket socket;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        System.out.println("local port is : " + socket.getLocalPort());
        initShutdownHook();


        while (keepAlive) {
            try {
                byte[] buffer = new byte[512]; // this is the maximum a packet size you can receive
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);  // waiting for the request


                String filename = new String(buffer, MakePacket.personalizedHeaderLength, Math.min(request.getLength(), buffer.length-MakePacket.personalizedHeaderLength));
                filename = filename.trim();
                byte flag = MakePacket.getFlag(request.getData());

                if(flag == MakePacket.setFlags(false,false,true,false,false,false)){
                    System.out.println("start receiving");
                    ReceiveFile(request, socket, filename)  ;
                }
                else if(flag == MakePacket.setFlags(false,false,false,true,false,false)){
                    System.out.println("client want to get a packet");
                    respondToGetRequest(request, socket, filename );
                }
                else if(flag == MakePacket.setFlags(false,false,false,false,true,false)){
                    System.out.println("client want to remove a packet");
                    deleteFile(request,socket,filename);
                }
                else{
                    System.out.println("do not understand the input") ;
                    String errormessage = "do not understand the input" ;
                    sendErrorPacket(errormessage,request,socket) ;
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




    private static void respondToGetRequest(DatagramPacket request, DatagramSocket socket, String filename) throws IOException {
        File file = new File(filename);
        if(!file.exists()){
            System.out.println("file does not exist");
            sendErrorPacket("file does not exist",request,socket);
        }
        else {
            byte[] byteFile = Fileclass.loadFile(file);
            Sending send = new Sending(socket);
            send.sending(byteFile, request.getAddress(), request.getPort());
        }



    }

    private static void ReceiveFile(DatagramPacket request, DatagramSocket socket,String filename) throws IOException {
        File file = new File(filename);
        if (file.exists()) {
            System.out.println("file already exist");
            sendErrorPacket("file already exist, if you want to replace this file try replace",request,socket);
        }
        else {

            sendACK(request, socket);

            Receiver receiver = new Receiver();
            byte[] receivedfile = receiver.receiver(socket, request.getAddress(), request.getPort());
            if(!(new String(receivedfile).equals("error"))){
                Fileclass.makeFileFromBytes(filename, receivedfile);
            }

        }

    }


    //TODO change magic numbers
    private static void sendACK(DatagramPacket request, DatagramSocket socket) throws IOException {
        byte[] ack = MakePacket.makePacket(new byte[]{1},0,0,MakePacket.setFlags(false,true,false,false,false,false),0,0);

        DatagramPacket packet = new DatagramPacket(ack,0,ack.length, request.getAddress(), request.getPort()) ;
        socket.send(packet);
    }


    private static void sendErrorPacket(String errormessage, DatagramPacket request , DatagramSocket socket) throws IOException {
        byte[] errorPacket = MakePacket.makePacket(errormessage.getBytes(), 0, MakePacket.getSequenceNumber(request.getData()) + 1, MakePacket.setFlags(false, false, false, false, false, true), 0, MakePacket.getSessionNumber(request.getData()));
        DatagramPacket errorPacketDatagram = new DatagramPacket(errorPacket, errorPacket.length,request.getAddress(),request.getPort()) ;
        socket.send(errorPacketDatagram);

    }

    private static void deleteFile(DatagramPacket request, DatagramSocket socket,String filename) throws IOException {
        File file = new File(filename);
        if(!file.exists()){
            System.out.println("file does not exist");
            sendErrorPacket("file " + filename +" does not exist so can not be deleted",request,socket);
        }
        else{
            file.delete();
            System.out.println("file deleted");
            sendACK(request,socket);

        }

    }
}
