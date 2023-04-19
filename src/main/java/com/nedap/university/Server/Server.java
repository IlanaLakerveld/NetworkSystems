package com.nedap.university.Server;

import com.nedap.university.Fileclass;
import com.nedap.university.MakePacket;
import com.nedap.university.Receiver;
import com.nedap.university.Sending;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 *  This is the server side. The request from the clients are come in here.
 *  Type of request the server can handel : GET,SEND,REMOVE,REPLACE
 */
public class Server {

    public void activeServer(DatagramSocket socket) {
        // This is the function that receives input from the client.
        while (Main.keepAlive) {

            try {
                byte[] buffer = new byte[512]; // This is the maximum a packet size you can receive
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);  // Waiting for the request

                String filename = new String(buffer, MakePacket.personalizedHeaderLength, Math.min(request.getLength(), buffer.length - MakePacket.personalizedHeaderLength));
                filename = filename.trim();
                // The flag tells what kind of input it is
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
                System.out.println("Close the server because of socket exception");
                // close system because error
                Main.keepAlive = false ;
            }
        }
    }

    /**
     * Handles the Get request. Looks of the file exist and if it exists it sends an acknowledgement ,and  if it does not exist it sends an error.
     * Then it will make a byte array from the file.
     * After that it starts sending the file.
     * @param request                   The request from the client
     * @param socket                    Datagram Socket
     * @param filename                  Name of the file that is requested
     * @throws IOException              Socket does not work
     */
    private static void respondToGetRequest(DatagramPacket request, DatagramSocket socket, String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File does not exist");
            sendErrorPacket("File does not exist", request, socket);
        } else {
            sendACK(request, socket, "getRequestsAck");

            byte[] byteFile = Fileclass.loadFile(file);
            System.out.println("Start sending ");
            Sending send = new Sending(socket);
            send.sending(byteFile, request.getAddress(), request.getPort());
            System.out.println("Done Sending");
        }


    }

    /**
     * Handles the send request. First it checks if the socket does not already exist. If not it sends an acknowledgement.
     * If it does exist it send an error back to the client. Then it start receiving the file afterwards it makes a file from the byte array.
     * @param request           The request from the client
     * @param socket            Datagram Socket
     * @param filename          Name of the file that client wants to send
     * @throws IOException      Socket does not work
     */
    private static void receiveFile(DatagramPacket request, DatagramSocket socket, String filename) throws IOException {
        File file = new File(filename);
        if (file.exists()) {
            System.out.println("File already exist");
            sendErrorPacket("File already exist, if you want to replace this file try replace", request, socket);
        } else {

            sendACK(request, socket, "Receive file");

            Receiver receiver = new Receiver();
            byte[] receivedFile = receiver.receiver(socket, request.getAddress(), request.getPort());
            if (!(new String(receivedFile).equals("error"))) {
                Fileclass.makeFileFromBytes(filename, receivedFile);
            }
            System.out.println("Uploaded the packet");


        }

    }

    /**
     * This function makes an acknowledgement datagram-pack and sends it to the client.
     * @param request               The request from the client
     * @param socket                Datagram Socket
     * @param packetLineInput       Line that says something about which kind of acknowledgement it is useful if you want to get more inside on the client side
     * @throws IOException          Socket does not work
     */
    private static void sendACK(DatagramPacket request, DatagramSocket socket, String packetLineInput) throws IOException {
        byte[] ack = MakePacket.makePacket(packetLineInput.getBytes(), 0, MakePacket.getSequenceNumber(request.getData()) + 1, MakePacket.ackFlagByte, 0, 0);
        DatagramPacket packet = new DatagramPacket(ack, 0, ack.length, request.getAddress(), request.getPort());
        socket.send(packet);
    }

    /**
     * This function makes an error packet and send it to the client
     * @param errormessage          A message that states what goes wrong. The reason this errormessage is sent
     * @param request               The request from the client
     * @param socket                Datagram Socket
     * @throws IOException          Socket does not work
     */
    private static void sendErrorPacket(String errormessage, DatagramPacket request, DatagramSocket socket) throws IOException {
        byte[] errorPacket = MakePacket.makePacket(errormessage.getBytes(), 0, MakePacket.getSequenceNumber(request.getData()) + 1, MakePacket.errorFlagByte, 0, MakePacket.getSessionNumber(request.getData()));
        DatagramPacket errorPacketDatagram = new DatagramPacket(errorPacket, errorPacket.length, request.getAddress(), request.getPort());
        socket.send(errorPacketDatagram);

    }

    /**
     * This function is called when the client wants to replace a file.
     * @param request           The request from the client
     * @param socket            Datagram Socket
     * @param filename          Name of the file that the client wants to replace
     * @throws IOException      Socket does not work
     */
    private static void replaceFile(DatagramPacket request, DatagramSocket socket, String filename) throws IOException {
        if (deleteFile(request, socket, filename)) {
            receiveFile(request, socket, filename);
        }
    }


    /**
     * This function deals with the delete request. It first checks if the file exist.
     * If it does not exist it sends an error to the server otherwise it deletes the file.
     * It there went something wrong with the removal of the file it sends an error to the client.
     * @param request           The request from the client
     * @param socket            Datagram Socket
     * @param filename          Name of the file that the client wants to remove
     * @return                  Returns true if de removal of the file is done correctly.
     * @throws IOException      Socket does not work
     */
    private static boolean deleteFile(DatagramPacket request, DatagramSocket socket, String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File does not exist");
            sendErrorPacket("File " + filename + " does not exist so can not be deleted", request, socket);
            return false;
        } else {
            if (file.delete()) {
                System.out.println("File deleted");
                sendACK(request, socket, "delete file" + filename);
                return true;
            } else {
                System.out.println("Something went wrong deleting the file send error message");
                sendErrorPacket("Something went wrong deleting the message", request, socket);
                return false;
            }

        }

    }

    /**
     * This deals with the get list of files request. It makes a list of the files and send it back to the client.
     * @param request               The request from the client
     * @param socket                 Datagram Socket
     * @throws IOException          Socket does not work
     */

    private static void getListOfFiles(DatagramPacket request, DatagramSocket socket) throws IOException {
        System.out.println("Client Wants a list of files");
        File currentDirectory = new File(".");
        String listOfFiles = "";
        listOfFiles = getStringOfNamesOfAllTheFilesInTheDirectory(currentDirectory, listOfFiles);
        byte[] listPacket = MakePacket.makePacket(listOfFiles.getBytes(), 0, MakePacket.getSequenceNumber(request.getData()) + 1, MakePacket.ackFlagByte, 0, 0);
        DatagramPacket packet = new DatagramPacket(listPacket, listPacket.length, request.getAddress(), request.getPort());
        System.out.println("Send list of files");
        socket.send(packet);


    }

    /**
     * This function makes a string with all the filenames in it
     * @param currentDirectory          The directory you this project is in
     * @param stringList                Empty list
     * @return A string with filename that are in this directory.
     */
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
