package com.nedap.university.Server;

import com.nedap.university.Fileclass;
import com.nedap.university.MakePacket;
import com.nedap.university.Receiver;
import com.nedap.university.Sending;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {

    public void activeServer(DatagramSocket socket) {

        while (Main.keepAlive) {
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
                } else if (flag == MakePacket.getFlagByte) {
                    System.out.println("client want to get a packet");
                    respondToGetRequest(request, socket, filename);
                } else if (flag == MakePacket.removeFlagByte) {
                    System.out.println("client want to remove a packet");
                    deleteFile(request, socket, filename);
                } else if (flag == MakePacket.listFlagByte) {
                    System.out.println("client want a list of files ");
                    getListOfFiles(request, socket);
                } else if (flag == MakePacket.replaceFlagByte) {
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
                Main.keepAlive = false ;
//                throw new RuntimeException(e);
            }
        }
    }

    private static void respondToGetRequest(DatagramPacket request, DatagramSocket socket, String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("file does not exist");
            sendErrorPacket("file does not exist", request, socket);
        } else {
            sendACK(request, socket, "getRequestsAck");

            byte[] byteFile = Fileclass.loadFile(file);
            System.out.println("start sending ");
            Sending send = new Sending(socket);
            send.sending(byteFile, request.getAddress(), request.getPort());
            System.out.println("Done Sending");
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
            System.out.println("Uploaded the packet");


        }

    }


    private static void sendACK(DatagramPacket request, DatagramSocket socket, String testline) throws IOException {
        byte[] ack = MakePacket.makePacket(testline.getBytes(), 0, MakePacket.getSequenceNumber(request.getData()) + 1, MakePacket.ackFlagByte, 0, 0);
        DatagramPacket packet = new DatagramPacket(ack, 0, ack.length, request.getAddress(), request.getPort());
        socket.send(packet);
    }


    private static void sendErrorPacket(String errormessage, DatagramPacket request, DatagramSocket socket) throws IOException {
        byte[] errorPacket = MakePacket.makePacket(errormessage.getBytes(), 0, MakePacket.getSequenceNumber(request.getData()) + 1, MakePacket.errorFlagByte, 0, MakePacket.getSessionNumber(request.getData()));
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
        byte[] listPacket = MakePacket.makePacket(listOfFiles.getBytes(), 0, MakePacket.getSequenceNumber(request.getData()) + 1, MakePacket.ackFlagByte, 0, 0);
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
