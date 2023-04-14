package com.nedap.university.Client;

import com.nedap.university.Exeptions.FileNotExistException;
import com.nedap.university.Exeptions.ServerGivesErrorException;
import com.nedap.university.Fileclass;
import com.nedap.university.MakePacket;
import com.nedap.university.Receiver;
import com.nedap.university.Sending;

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

    public Client(int port, InetAddress address) {
        this.port = port;
        this.address = address;

    }


    public void getRequest(String filename) throws IOException, ServerGivesErrorException {

        DatagramSocket datagramSocket = new DatagramSocket();
        MakeAndSendInitialPacket(filename, datagramSocket, MakePacket.setFlags(false, false, false, true, false, false, false));
        // call the function that handles receiving a packet
        Receiver receiver = new Receiver();
        byte[] receivedFile = receiver.receiver(datagramSocket, address, port);

        if (new String(receivedFile).contains("ERROR")) {
            throw new ServerGivesErrorException(new String(receivedFile));
        } else {
            Fileclass.makeFileFromBytes(filename, receivedFile);
        }

    }

    public void sendRequest(String filename) throws FileNotExistException, ServerGivesErrorException, IOException {
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotExistException("Can not send it because file does not exist");
        } else {
            DatagramSocket socket = new DatagramSocket();
            MakeAndSendInitialPacket(filename, socket, MakePacket.setFlags(false, false, true, false, false, false, false));

            DatagramPacket ackAnswer = getAcknowledgementPacket(socket);

            if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.setFlags(false, false, false, false, false, true, false)) {
                String errorMessage = new String(ackAnswer.getData(), MakePacket.personalizedHeaderLength, ackAnswer.getLength());
                throw new ServerGivesErrorException("ERROR " + errorMessage.trim());
            } else if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.setFlags(false, true, false, false, false, false, false)) {
                byte[] bytefile = Fileclass.loadFile(file);
                Sending send = new Sending(socket);
                send.sending(bytefile, ackAnswer.getAddress(), ackAnswer.getPort());
                System.out.println("Send");
            } else {
                //TODO
                System.out.println("it's not the ack packet that is received or an error");

            }

        }

    }


    public void deleteRequest(String filename) throws FileNotExistException, IOException {
        DatagramSocket socket = new DatagramSocket();
        MakeAndSendInitialPacket(filename, socket,MakePacket.setFlags(false, false, false, false, true, false, false));
        DatagramPacket ackAnswer = getAcknowledgementPacket(socket);
        if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.setFlags(false, false, false, false, false, true, false)) {
            String errorMessage = new String(ackAnswer.getData(), MakePacket.personalizedHeaderLength, ackAnswer.getLength());
            throw new FileNotExistException("ERROR " + errorMessage.trim());
        } else if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.setFlags(false, true, false, false, false, false, false)) {
            System.out.println("File is deleted");
        }
        else{
            System.out.println("it's not the ack packet that is received or an error");
        }
    }

    public void getListRequest() throws IOException, ServerGivesErrorException {
        DatagramSocket socket = new DatagramSocket();
        MakeAndSendInitialPacket("", socket,MakePacket.setFlags(false, false, false, false, false, false, true));
        DatagramPacket ackAnswer = getAcknowledgementPacket(socket);

        if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.setFlags(false, false, false, false, false, true, false)) {
            String errorMessage = new String(ackAnswer.getData(), MakePacket.personalizedHeaderLength, ackAnswer.getLength());
            throw new ServerGivesErrorException("ERROR " + errorMessage.trim());
        } else if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.setFlags(false, true, false, false, false, false, false)) {
            System.out.println("The names of the files are :");
            String list = new String(ackAnswer.getData(), MakePacket.personalizedHeaderLength, ackAnswer.getLength());
            list = list.trim();
            System.out.println(list);
        }
        else{
            System.out.println("unknown packet");
        }
    }


    private static DatagramPacket getAcknowledgementPacket(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[512]; // this is the maximum a packet size you can receive
        DatagramPacket ackAnswer = new DatagramPacket(buffer, buffer.length); // this request is the filled with data
        socket.receive(ackAnswer);
        return ackAnswer;
    }

    private void MakeAndSendInitialPacket(String filename, DatagramSocket socket, byte flag) throws IOException {
        int sequenceNumber = (int) (Math.random() * 10000);
        // acknowledgement window size and session number are set zero.
        byte[] packet = MakePacket.makePacket(filename.getBytes(), sequenceNumber, 0, flag, 0, 0);
        DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, address, port);
        socket.send(packetToSend);
    }

}
