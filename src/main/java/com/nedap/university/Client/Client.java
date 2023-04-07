package com.nedap.university.Client;

import com.nedap.university.MakePacket;

import java.io.IOException;
import java.net.*;

public class Client {
    public Client() {

    }


    public void getRequest(String filename) throws UnknownHostException {

        byte[] packet = MakePacket.makePacket(filename.getBytes(), 0, 0, (byte) 0, 0, 0);
        byte[] buffer = packet ; 
//        InetAddress address = InetAddress.getByName("127.0.0.1");
        InetAddress address = InetAddress.getByName("localhost");
        int port = 64888 ; //toDo change
        DatagramPacket packetToSend = new DatagramPacket(buffer,buffer.length,address, port);
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(packetToSend);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
