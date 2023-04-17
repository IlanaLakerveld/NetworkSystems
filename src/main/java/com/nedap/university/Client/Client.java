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

    private boolean gotACK ;



    public Client(int port, InetAddress address) {
        this.port = port;
        this.address = address;
        gotACK = false;

    }


    public void getRequest(String filename) throws IOException, ServerGivesErrorException {
        setGotACK(false);
        DatagramSocket datagramSocket = new DatagramSocket();
        MakeAndSendInitialPacket(filename, datagramSocket, MakePacket.setFlags(false, false, false, true, false, false, false));
        // call the function that handles receiving a packet
        DatagramPacket ackAnswer = getAcknowledgementPacket(datagramSocket);
        if(MakePacket.getFlag(ackAnswer.getData()) == MakePacket.setFlags(false, true, false, false, false, false, false)) {
            Receiver receiver = new Receiver();
            byte[] receivedFile = receiver.receiver(datagramSocket, address, port);
            // Check if there is not an error occurred while receiving the file.
            System.out.println("received something");
            if (new String(receivedFile).contains("ERROR")) {
                throw new ServerGivesErrorException(new String(receivedFile));
            } else {
                Fileclass.makeFileFromBytes(filename, receivedFile);
            }
        }

    }

    public void sendRequest(String filename) throws FileNotExistException, ServerGivesErrorException, IOException {
        setGotACK(false);
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotExistException("Can not send it because file does not exist");
        } else {
            DatagramSocket socket = new DatagramSocket();
            MakeAndSendInitialPacket(filename, socket, MakePacket.setFlags(false, false, true, false, false, false, false));
            DatagramPacket ackAnswer = getAcknowledgementPacket(socket);
            // check if the input is an acknowledgement or an error.
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

    public void replaceRequest(String filename) throws IOException, FileNotExistException {
        setGotACK(false);
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotExistException("Can not send it because file does not exist");
        }
        else {
            DatagramSocket socket = new DatagramSocket();
            MakeAndSendInitialPacket(filename, socket, MakePacket.setFlags(false, false, true, false, true, false, false));
            DatagramPacket ackAnswer = getAcknowledgementPacket(socket);
            // check if the input is an acknowledgement or an error.
            if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.setFlags(false, false, false, false, false, true, false)) {
                String errorMessage = new String(ackAnswer.getData(), MakePacket.personalizedHeaderLength, ackAnswer.getLength());
                throw new FileNotExistException("ERROR " + errorMessage.trim());
            } else if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.setFlags(false, true, false, false, false, false, false)) {
                System.out.println("File is deleted");
                byte[] bytefile = Fileclass.loadFile(file);
                Sending send = new Sending(socket);
                send.sending(bytefile, ackAnswer.getAddress(), ackAnswer.getPort());
                System.out.println("Send");
            } else {
                System.out.println("it's not the ack packet that is received or an error");
            }
        }




    }


    public void deleteRequest(String filename) throws FileNotExistException, IOException {
        setGotACK(false);
        DatagramSocket socket = new DatagramSocket();
        MakeAndSendInitialPacket(filename, socket,MakePacket.setFlags(false, false, false, false, true, false, false));
        DatagramPacket ackAnswer = getAcknowledgementPacket(socket);
        System.out.println(new String(ackAnswer.getData()));
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
        setGotACK(false);
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
            System.out.println("Unknown input ");
        }
    }

    private DatagramPacket getAcknowledgementPacket(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[512]; // this is the maximum a packet size you can receive
        DatagramPacket ackAnswer = new DatagramPacket(buffer, buffer.length); // this request is the filled with data
        socket.receive(ackAnswer);
        setGotACK(true);
        return ackAnswer;
    }

    private void MakeAndSendInitialPacket(String filename, DatagramSocket socket, byte flag) throws IOException {
        // TODO ga je hier nog wat mee doen???
        int sequenceNumber = (int) (Math.random() * 10000);
        // acknowledgement window size and session number are set zero.
        byte[] packet = MakePacket.makePacket(filename.getBytes(), sequenceNumber, 0, flag, 0, 0);
        DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, address, port);
        socket.send(packetToSend);
        new TimeOut(1000, socket ,packetToSend, this) ;
    }


    public boolean isGotACK() {
        return gotACK;
    }

    public void setGotACK(boolean gotACK) {
        this.gotACK = gotACK;
    }
}
