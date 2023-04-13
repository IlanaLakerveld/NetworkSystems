package com.nedap.university.Client;

import com.nedap.university.Exeptions.FileNotExistException;
import com.nedap.university.Exeptions.ServerGivesErrorException;
import com.nedap.university.MakePacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {
    Client client ;
    @BeforeEach
    public void setUp(){

        int port = 23456 ;

        try {
            InetAddress address = InetAddress.getByName("localhost");

            client = new Client(port,address );
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }



    }

    @Test
    public void getRequestTest() {
       //TOdo
    }

    @Test
    public void sendRequestTest() throws InterruptedException {
        // File bestaat niet dus krijg je die system out terug
        assertThrows(FileNotExistException.class, this::notExistedInputTest);

        // Gets back an error message test
        TestServerGivesError testServer = new TestServerGivesError();
        new Thread(testServer).start();

        assertThrows(ServerGivesErrorException.class, this::getErrorTest);

        // als alles wel werkt??

    }

    private void notExistedInputTest() throws FileNotExistException, ServerGivesErrorException {
        client.sendRequest("notExistedFile.png");
    }

    private void getErrorTest() throws ServerGivesErrorException, FileNotExistException {
        client.sendRequest("somefile.pdf");
    }


    /**
     * Some dummy server die only sends back an error flagged packet so can see on the clientside how that handles that kind of packets.
     */
    public class TestServerGivesError implements Runnable {
        public void run(){
            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket(23456);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
            byte[] buffer = new byte[512]; // this is the maximum a packet size you can receive
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            try {
                datagramSocket.receive(request);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // only the flags is important
            byte[] errorPacket = MakePacket.makePacket("Some Error Message here".getBytes(), 0, 0, MakePacket.setFlags(false, false, false, false, false, true), 0, 0);
            DatagramPacket errorPacketDatagram = null;
            errorPacketDatagram = new DatagramPacket(errorPacket, errorPacket.length, request.getAddress(), request.getPort());

            try {
                datagramSocket.send(errorPacketDatagram);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            datagramSocket.close();
        }
}

    private void getErrorTestDelete() throws ServerGivesErrorException, FileNotExistException {
        client.deleteRequest("somefile.pdf");
    }


    @Test
    public void deleteRequestTest() {
        TestServerGivesError testServer = new TestServerGivesError();
        new Thread(testServer).start();
        assertThrows(FileNotExistException.class, this::getErrorTestDelete);

    }
}