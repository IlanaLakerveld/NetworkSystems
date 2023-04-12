package com.nedap.university;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static org.junit.jupiter.api.Assertions.*;

class SendingTest {

    @Test
    public void ResentTest(){
        int port = 62826;
        DatagramSocket socketServer;
        try {
            socketServer = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        File file = new File("/Users/ilana.lakerveld/Documents/NetworkSystems/project/nu-module-2-mod2.2023/example_files/medium.pdf");
        byte[] bytefile = Fileclass.loadFile(file);
        Sending send = new Sending(socketServer);
        try {
            send.sending(bytefile, InetAddress.getLocalHost(),port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }

}