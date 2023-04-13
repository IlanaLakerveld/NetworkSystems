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


public class Client {
    // todo change
    public int port;
    public InetAddress address;

    public Client(int port, InetAddress address) {
        this.port = port;
        this.address = address;


    }


    public void getRequest(String filename) {

        byte[] packet = MakePacket.makePacket(filename.getBytes(), 0, 0, (byte) MakePacket.setFlags(false, false, false, true, false, false, false), 0, 0);

        DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, address, port);
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(packetToSend);
            Receiver receiver = new Receiver();
            byte[] receivedFile = receiver.receiver(datagramSocket, address, port);
            if (new String(receivedFile).equals("error")) {
                System.out.println("request failed");
            } else {
                Fileclass.makeFileFromBytes(filename, receivedFile);
            }


        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendRequest(String filename) throws FileNotExistException, ServerGivesErrorException {
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotExistException("Can not send it because file does not exist");
        } else {
            byte[] packet = MakePacket.makePacket(filename.getBytes(), 0, 0, MakePacket.setFlags(false, false, true, false, false, false, false), 0, 0);
            DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, address, port);
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.send(packetToSend);
                byte[] buffer = new byte[512]; // this is the maximum a packet size you can receive
                DatagramPacket ackAnswer = new DatagramPacket(buffer, buffer.length); // this request is the filled with data
                socket.receive(ackAnswer);
                if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.setFlags(false, false, false, false, false, true, false)) {
                    String errorMessage = new String(buffer, MakePacket.personalizedHeaderLength, ackAnswer.getLength());
                    throw new ServerGivesErrorException("ERROR " + errorMessage.trim());
                } else {
                    byte[] bytefile = Fileclass.loadFile(file);
                    Sending send = new Sending(socket);
                    send.sending(bytefile, ackAnswer.getAddress(), ackAnswer.getPort());
                    System.out.println("Send");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

    public void deleteRequest(String filename) throws FileNotExistException {
        byte[] packet = MakePacket.makePacket(filename.getBytes(), 0, 0, MakePacket.setFlags(false, false, false, false, true, false, false), 0, 0);

        DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, address, port);
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.send(packetToSend);
            byte[] buffer = new byte[512]; // this is the maximum a packet size you can receive
            DatagramPacket ackAnswer = new DatagramPacket(buffer, buffer.length); // this request is the filled with data
            socket.receive(ackAnswer);
            if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.setFlags(false, false, false, false, false, true, false)) {
                String errorMessage = new String(buffer, MakePacket.personalizedHeaderLength, ackAnswer.getLength());
                throw new FileNotExistException("ERROR " + errorMessage.trim());
            } else if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.setFlags(false, true, false, false, false, false, false)) {
                System.out.println("File is deleted");
            }
        } catch (IOException e) {
            System.out.println("something went wrong with Datagram socket");
            throw new RuntimeException();
        }
    }

    public void getListRequest() throws IOException, ServerGivesErrorException {
        byte[] packet = MakePacket.makePacket(new byte[]{0}, 0, 0, MakePacket.setFlags(false, false, false, false, false, false, true), 0, 0);
        DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, address, port);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packetToSend);
        byte[] buffer = new byte[512]; // this is the maximum a packet size you can receive
        DatagramPacket ackAnswer = new DatagramPacket(buffer, buffer.length); // this request is the filled with data
        socket.receive(ackAnswer);
        if (MakePacket.getFlag(ackAnswer.getData()) == MakePacket.setFlags(false, false, false, false, false, true, false)) {
            String errorMessage = new String(buffer, MakePacket.personalizedHeaderLength, ackAnswer.getLength());
            throw new ServerGivesErrorException("ERROR " + errorMessage.trim());
        }
        else if(MakePacket.getFlag(ackAnswer.getData()) == MakePacket.setFlags(false, true, false, false, false, false, false)){
            System.out.println("The names of the files are :");
            String list = new String(buffer, MakePacket.personalizedHeaderLength, ackAnswer.getLength());
            list = list.trim();
            System.out.println(list);
        }
    }
}
