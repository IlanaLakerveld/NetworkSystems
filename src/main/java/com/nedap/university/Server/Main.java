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
        System.out.println("Hello, Ilana ");
        int port = 62830;
        try (DatagramSocket socket = new DatagramSocket(port)) {

            System.out.println("local port is : " + socket.getLocalPort());
            initShutdownHook();


            while (keepAlive) {
                try {
                    byte[] buffer = new byte[512]; // this is the maximum a packet size you can receive
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    socket.receive(request);  // waiting for the request

                    String filename = new String(buffer, MakePacket.personalizedHeaderLength, Math.min(request.getLength(), buffer.length - MakePacket.personalizedHeaderLength));
                    filename = filename.trim();
                    byte flag = MakePacket.getFlag(request.getData());

                    if (flag == MakePacket.sendFlagByte) {
                        System.out.println("start receiving");
                        receiveFile(request, socket, filename);
                    } else if (flag == MakePacket.setFlags(false, false, false, true, false, false, false)) {
                        System.out.println("client want to get a packet");
                        respondToGetRequest(request, socket, filename);
                    } else if (flag == MakePacket.setFlags(false, false, false, false, true, false, false)) {
                        System.out.println("client want to remove a packet");
                        deleteFile(request, socket, filename);
                    } else if (flag == MakePacket.setFlags(false, false, false, false, false, false, true)) {
                        System.out.println("client want a list of files ");
                        getListOfFiles(request, socket);
                    } else if (flag == MakePacket.setFlags(false, false, true, false, true, false, false)) {
                        System.out.println("client want to replace a file");
                        replaceFile(request, socket, filename);
                    } else {
                        System.out.println("do not understand the input");
                        String errormessage = "do not understand the input";
                        sendErrorPacket(errormessage, request, socket);
                    }

                } catch (IOException e) {
                    // TODO
                    // close system because error
                    throw new RuntimeException(e);
                }
            }
        } catch (SocketException e) {
            System.out.println("Something wrong with making the socket");
            throw new RuntimeException(e);
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
        if (!file.exists()) {
            System.out.println("file does not exist");
            sendErrorPacket("file does not exist", request, socket);
        } else {

            sendACK(request, socket, "getRequestsAck");
            long starttime = System.nanoTime();
            byte[] byteFile = Fileclass.loadFile(file);
            long endtime =System.nanoTime() ;
            System.out.println("Time to upload in ms " + ((endtime-starttime)/1000000) );
            starttime = System.nanoTime();
            Sending send = new Sending(socket);
            send.sending(byteFile, request.getAddress(), request.getPort());
            endtime =System.nanoTime() ;
            System.out.println("Time to send in ms = " + ((endtime-starttime)/1000000));
        }


    }

    private static void receiveFile(DatagramPacket request, DatagramSocket socket, String filename) throws IOException {
        File file = new File(filename);
        if (file.exists()) {
            System.out.println("file already exist");
            sendErrorPacket("file already exist, if you want to replace this file try replace", request, socket);
        } else {
            sendACK(request, socket, "receivefile");

            Receiver receiver = new Receiver();
            byte[] receivedFile = receiver.receiver(socket, request.getAddress(), request.getPort());
            if (!(new String(receivedFile).equals("error"))) {
                Fileclass.makeFileFromBytes(filename, receivedFile);
            }


        }

    }


    private static void sendACK(DatagramPacket request, DatagramSocket socket, String testline) throws IOException {
        byte[] ack = MakePacket.makePacket(testline.getBytes(), 0, MakePacket.getSequenceNumber(request.getData()) + 1, MakePacket.setFlags(false, true, false, false, false, false, false), 0, 0);
        DatagramPacket packet = new DatagramPacket(ack, 0, ack.length, request.getAddress(), request.getPort());
        socket.send(packet);
    }


    private static void sendErrorPacket(String errormessage, DatagramPacket request, DatagramSocket socket) throws IOException {
        byte[] errorPacket = MakePacket.makePacket(errormessage.getBytes(), 0, MakePacket.getSequenceNumber(request.getData()) + 1, MakePacket.setFlags(false, false, false, false, false, true, false), 0, MakePacket.getSessionNumber(request.getData()));
        DatagramPacket errorPacketDatagram = new DatagramPacket(errorPacket, errorPacket.length, request.getAddress(), request.getPort());
        socket.send(errorPacketDatagram);

    }

    private static void replaceFile(DatagramPacket request, DatagramSocket socket, String filename) throws IOException {
        if (deleteFile(request, socket, filename)) {
            receiveFile(request, socket, filename);
        }
    }

    private static boolean deleteFile(DatagramPacket request, DatagramSocket socket, String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("file does not exist");
            sendErrorPacket("file " + filename + " does not exist so can not be deleted", request, socket);
            return false;
        } else {
            if (file.delete()) {
                System.out.println("file deleted");
                sendACK(request, socket, "deletefile" + filename);
                return true;
            } else {
                System.out.println("Something went wrong deleting the file send error message");
                sendErrorPacket("Something went wrong deleting the message", request, socket);
                return false;
            }

        }

    }

    private static void getListOfFiles(DatagramPacket request, DatagramSocket socket) throws IOException {
        System.out.println("want list of files");
        File currentDirectory = new File(".");
        String listOfFiles = "";
        listOfFiles = getStringOfNamesOfAllTheFilesInTheDirectory(currentDirectory, listOfFiles);
        byte[] listPacket = MakePacket.makePacket(listOfFiles.getBytes(), 0, MakePacket.getSequenceNumber(request.getData()) + 1, MakePacket.setFlags(false, true, false, false, false, false, false), 0, 0);
        DatagramPacket packet = new DatagramPacket(listPacket, listPacket.length, request.getAddress(), request.getPort());
        System.out.println("Send list of files");
        socket.send(packet);


    }

    private static String getStringOfNamesOfAllTheFilesInTheDirectory(File currentDirectory, String stringList) {
        File[] list = currentDirectory.listFiles();
        if (list != null) {
            StringBuilder stringListBuilder = new StringBuilder(stringList);
            for (File file : list) {
                if (file.isFile()) {
                    stringListBuilder.append(file.getName());
                    stringListBuilder.append("\n");
                }
            }
            stringList = stringListBuilder.toString();
        }
        return stringList;
    }
}
