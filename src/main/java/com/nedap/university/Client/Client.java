package com.nedap.university.Client;

import com.nedap.university.Exeptions.FileNotExistException;
import com.nedap.university.Exeptions.ServerGivesErrorException;
import com.nedap.university.Fileclass;
import com.nedap.university.MakePacket;
import com.nedap.university.Receiver;
import com.nedap.university.Sending;
import com.nedap.university.Timer.TimeOut;

import java.io.File;
import java.io.IOException;
import java.net.*;

/**
 * This is the class client. This uses the classes Receiver,Sending, Makepacket and Fileclass.
 * It assumes that it receive packet according to the packet header of Make packet.
 */

public class Client {

    public int port;
    public InetAddress address;

    private boolean gotACK;


    public Client(int port, InetAddress address) {
        this.port = port;
        this.address = address;
        gotACK = false;

    }


    /**
     * This deals with the get request from begin to end and deals with any throws error is anything is not correct.
     * First it sends a request to the server. If the response is an error it gives an exceptions otherwise it start receiving the file.
     * The last step is to make a file from the received byte array
     *
     * @param filename                   Filename of the file you want to have
     * @throws IOException               Some socket error
     * @throws ServerGivesErrorException Some error occurred at the server side and the request could not be handled
     */
    public void getRequest(String filename) throws IOException, ServerGivesErrorException {
        setGotACK(false);
        DatagramSocket datagramSocket = new DatagramSocket();
        MakeAndSendInitialPacket(filename, datagramSocket, MakePacket.getFlagByte);
        // call the function that handles receiving a packet
        DatagramPacket ackAnswer = getAcknowledgementPacket(datagramSocket);
        if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.ackFlagByte) {
            Receiver receiver = new Receiver();
            byte[] receivedFile = receiver.receiver(datagramSocket, address, port);
            // Check if there is not an error occurred while receiving the file.
            if (new String(receivedFile).contains("ERROR")) {
                throw new ServerGivesErrorException(new String(receivedFile));
            } else {
                // makes the files
                Fileclass.makeFileFromBytes(filename, receivedFile);
            }
        }

    }

    /**
     * This handles the send request.
     * First it checks if the file exists. If the file exist it sends a request to the server.
     * If the response is positive(an ack) then starts sending the file. If the response is an error flag it will not send the file
     * @param filename                      Filename
     * @throws FileNotExistException        File does not exist
     * @throws ServerGivesErrorException    Some error occurred at the server side and the request could not be handled
     * @throws IOException                  Some socket error
     */
    public void sendRequest(String filename) throws FileNotExistException, ServerGivesErrorException, IOException {
        setGotACK(false);
        File file = checkIfFileExist(filename);// gives error is file does not exist
        DatagramSocket socket = new DatagramSocket();
        MakeAndSendInitialPacket(filename, socket, MakePacket.sendFlagByte);
        DatagramPacket ackAnswer = getAcknowledgementPacket(socket);
        // check if the input is an acknowledgement or an error.
        if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.errorFlagByte) {
            String errorMessage = new String(ackAnswer.getData(), MakePacket.personalizedHeaderLength, ackAnswer.getLength());
            throw new ServerGivesErrorException("ERROR " + errorMessage.trim());
        } else if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.ackFlagByte) {
            byte[] bytefile = Fileclass.loadFile(file);
            Sending send = new Sending(socket);
            send.sending(bytefile, ackAnswer.getAddress(), ackAnswer.getPort());
            System.out.println("Send");
        } else {
            System.out.println("it's not the ack packet that is received or an error");

        }


    }


    /**
     * Handles the replace request.
     * First checks  if the file exist. If the file exist a request is sent to the server.
     * If there is a positive response  (ack) it starts sending the file.
     * If response is negative (error) it will not send the file
     * @param filename                      Filename
     * @throws IOException                  Some socket error
     * @throws FileNotExistException        File does not exist
     */
    public void replaceRequest(String filename) throws IOException, FileNotExistException {
        setGotACK(false);
        File file =  checkIfFileExist(filename); // gives error is file does not exist
        DatagramSocket socket = new DatagramSocket();
        MakeAndSendInitialPacket(filename, socket, MakePacket.replaceFlagByte);
        DatagramPacket ackAnswer = getAcknowledgementPacket(socket);
        // check if the input is an acknowledgement or an error.
        if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.errorFlagByte) {
            String errorMessage = new String(ackAnswer.getData(), MakePacket.personalizedHeaderLength, ackAnswer.getLength());
            throw new FileNotExistException("ERROR " + errorMessage.trim());
        } else if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.ackFlagByte) {
            System.out.println("File is deleted");
            byte[] byteFile = Fileclass.loadFile(file);
            Sending send = new Sending(socket);
            send.sending(byteFile, ackAnswer.getAddress(), ackAnswer.getPort());
            System.out.println("Done sending the file");
        } else {
            System.out.println("it's not the ack packet that is received or an error");

        }

    }

    /**
     * This handles the delete request. First checks if the file exist.
     * If the file exist it sent a request.
     * If the response is positive (ack) the file is deleted
     * If the response is an error flag the file is not deleted.
     * @param filename                      Filename
     * @throws FileNotExistException        File does not exist
     * @throws IOException                  Some socket error
     */
    public void deleteRequest(String filename) throws FileNotExistException, IOException {
        setGotACK(false);
        DatagramSocket socket = new DatagramSocket();
        MakeAndSendInitialPacket(filename, socket, MakePacket.removeFlagByte);
        DatagramPacket ackAnswer = getAcknowledgementPacket(socket);
        System.out.println(new String(ackAnswer.getData()));
        if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.errorFlagByte) {
            String errorMessage = new String(ackAnswer.getData(), MakePacket.personalizedHeaderLength, ackAnswer.getLength());
            throw new FileNotExistException("ERROR " + errorMessage.trim());
        } else if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.ackFlagByte) {
            System.out.println("File is deleted");
        } else {
            System.out.println("it's not the ack packet that is received or an error");
        }
    }

    /**
     * Handles the request for a list with file names.
     * It first sends a request to the server.
     * If the response has an error flag it could get a list
     * If the response is positive (ack) the names of the files are printed
     * @throws IOException                      Some socket error
     * @throws ServerGivesErrorException        Some error occurred at the server side and the request could not be handled
     */
    public void getListRequest() throws IOException, ServerGivesErrorException {
        setGotACK(false);
        DatagramSocket socket = new DatagramSocket();
        MakeAndSendInitialPacket("", socket, MakePacket.listFlagByte);
        DatagramPacket ackAnswer = getAcknowledgementPacket(socket);

        if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.errorFlagByte) {
            String errorMessage = new String(ackAnswer.getData(), MakePacket.personalizedHeaderLength, ackAnswer.getLength());
            throw new ServerGivesErrorException("ERROR " + errorMessage.trim());
        } else if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.ackFlagByte) {
            printFilenameFromDataPacket(ackAnswer);
        } else {
            System.out.println("Unknown input ");
        }
    }

    /**
     * Prints the different filenames.
     * @param ackAnswer Data packet with filenames in it.
     */
    private static void printFilenameFromDataPacket(DatagramPacket ackAnswer) {
        System.out.println("The names of the files are :");
        String list = new String(ackAnswer.getData(), MakePacket.personalizedHeaderLength, ackAnswer.getLength());
        list = list.trim();
        System.out.println(list);
    }

    /**
     * Socket waits for an input from the server and set the boolean got ack op true when they got the in packet
     * @param socket socket
     * @return the acknowledgement packet
     * @throws IOException
     */

    private DatagramPacket getAcknowledgementPacket(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[512]; // this is the maximum a packet size you can receive
        DatagramPacket ackAnswer = new DatagramPacket(buffer, buffer.length); // this request is the filled with data
        socket.receive(ackAnswer);
        setGotACK(true);
        return ackAnswer;
    }


    /**
     * Makes a request packet and send it and makes a timer for a time out
     * @param filename filename
     * @param socket socket
     * @param flag set flags of things to send
     * @throws IOException
     */
    private void MakeAndSendInitialPacket(String filename, DatagramSocket socket, byte flag) throws IOException {
        int sequenceNumber = (int) (Math.random() * 10000);
        // acknowledgement window size and session number are set zero.
        byte[] packet = MakePacket.makePacket(filename.getBytes(), sequenceNumber, 0, flag, 0, 0);
        DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, address, port);
        socket.send(packetToSend);
        new TimeOut(150, socket, packetToSend, this);
    }


    /**
     *
     * @param filename name of the file
     * @return returns the file is the file exits otherwise it gives an error.
     * @throws FileNotExistException
     */
    private File checkIfFileExist(String filename) throws FileNotExistException {
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotExistException("Can not send it because file does not exist");
        }
        return  file ;
    }


    public boolean isGotACK() {
        return gotACK;
    }

    public void setGotACK(boolean gotACK) {
        this.gotACK = gotACK;
    }
}
