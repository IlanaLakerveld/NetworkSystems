package com.nedap.university.Client;

import com.nedap.university.LoadFile;
import com.nedap.university.MakePacket;
import com.nedap.university.Reveiver;

import java.io.IOException;
import java.net.*;

public class Client {
    public Client() {

    }


    public void getRequest(String filename) throws UnknownHostException {

        byte[] packet = MakePacket.makePacket(filename.getBytes(), 0, 0, (byte) 0, 0, 0);
        //        InetAddress address = InetAddress.getByName("127.0.0.1");
        InetAddress address = InetAddress.getByName("localhost");
        int port = 54239; //toDo change
        DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, address, port);
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(packetToSend);
            Reveiver reveiver = new Reveiver();
            byte[] receivedfile = reveiver.receiver(filename, datagramSocket, address, port);
            LoadFile.makeFileFromBytes(filename,receivedfile);

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }
}
