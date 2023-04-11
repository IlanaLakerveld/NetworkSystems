package com.nedap.university.Client;

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

    public Client() {
        port = 62885;
        try {
            address = InetAddress.getByName("localhost");
            //        InetAddress address = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }


    public void getRequest(String filename) {

        byte[] packet = MakePacket.makePacket(filename.getBytes(), 0, 0, (byte) 0, 0, 0);

        DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, address, port);
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(packetToSend);
            Receiver receiver = new Receiver();
            byte[] receivedFile = receiver.receiver(datagramSocket, address, port);
            Fileclass.makeFileFromBytes(filename, receivedFile);

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendRequest(String filename) {
        File file = new File("/Users/ilana.lakerveld/Documents/NetworkSystems/project/nu-module-2-mod2.2023/example_files/medium.pdf");
        byte[] bytefile = Fileclass.loadFile(file);

        String sendAndFilename = "SEND~"+filename;
        byte[] packet = MakePacket.makePacket(sendAndFilename.getBytes(),0,0,(byte)0,0,0);
        DatagramPacket packetToSend = new DatagramPacket(packet,packet.length,address,port);
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.send(packetToSend);
            byte[] buffer = new byte[512]; // this is the maximum a packet size you can receive
            DatagramPacket ackAnswer = new DatagramPacket(buffer, buffer.length); // this request is the filled with data
            socket.receive(ackAnswer);
            //toDo check request
            Sending send = new Sending();
            send.sending(bytefile,socket, ackAnswer.getAddress(),ackAnswer.getPort());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
