package com.nedap.university.Client;

import com.nedap.university.Exeptions.FileNotExistException;
import com.nedap.university.Exeptions.ServerGivesErrorException;
import com.nedap.university.MakePacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Some test for the client side.
 * Important you need to have someFile.pdf in your folder
 */

class ClientTest {
    Client client;

    @BeforeEach
    public void setUp() {

        int port = 23456;

        try {
            InetAddress address = InetAddress.getByName("localhost");

            client = new Client(port, address);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }


    }


    /**
     * Test if it gets errors on the write time.
     */
    @Test
    public void sendRequestTest() {
        // File does not exist so you cent an error
        assertThrows(FileNotExistException.class, this::notExistedInputTest);

        // Gets back an error message test
        TestServerGivesError testServer = new TestServerGivesError();
        new Thread(testServer).start();

        assertThrows(ServerGivesErrorException.class, this::getErrorTest);


    }

    /**
     * This function is used for sendRequestTest()
     */
    private void notExistedInputTest() throws FileNotExistException, ServerGivesErrorException, IOException {
        client.sendRequest("notExistedFile.png");
    }


    /**
     * This function is used for sendRequestTest()
     */
    private void getErrorTest() throws ServerGivesErrorException, FileNotExistException, IOException {
        client.sendRequest("someFile.pdf");
    }


    /**
     * Some dummy server die only sends back an error flagged packet so can see on the clientside how that handles that kind of packets.
     */
    public class TestServerGivesError implements Runnable {
        public void run() {
            DatagramSocket datagramSocket;
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
            byte[] errorPacket = MakePacket.makePacket("Some Error Message here".getBytes(), 0, 0, MakePacket.setFlags(false, false, false, false, false, true, false), 0, 0);
            DatagramPacket errorPacketDatagram;
            errorPacketDatagram = new DatagramPacket(errorPacket, errorPacket.length, request.getAddress(), request.getPort());

            try {
                datagramSocket.send(errorPacketDatagram);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            datagramSocket.close();
        }
    }


    /**
     * Test if it gets errors on the write time.
     */
    @Test
    public void deleteRequestTest() {
        TestServerGivesError testServer = new TestServerGivesError();
        new Thread(testServer).start();
        assertThrows(FileNotExistException.class, this::getErrorTestDelete);

    }

    /**
     * This function is used for deleteRequestTest
     */
    private void getErrorTestDelete() throws ServerGivesErrorException, FileNotExistException, IOException {
        client.deleteRequest("someFile.pdf");
    }

    /**
     * Test if it gets errors on the write time.
     */
    @Test
    public void listFilesTest() {
        TestServerGivesError testServer = new TestServerGivesError();
        new Thread(testServer).start();
        assertThrows(ServerGivesErrorException.class, this::getErrorTestListFiles);
    }

    /**
     * This function is used for listFilesTest
     */
    private void getErrorTestListFiles() throws ServerGivesErrorException, IOException {
        client.getListRequest();
    }


    /**
     * Test if it gets errors on the write time.
     */
    @Test
    public void replaceTest() {
        assertThrows(FileNotExistException.class, this::notExistedInputReplaceTest);

    }

    /**
     * Used in replaceTest
     */

    private void notExistedInputReplaceTest() throws FileNotExistException, IOException {
        client.replaceRequest("notExistedFile.png");
    }

    /**
     * You can see in output stream that it print the print string
     */
    @Test
    public void printFilenameFromDataPacketTest() {
        String printString = "name1\nname2\nname3";
        byte[] input = MakePacket.makePacket(printString.getBytes(), 0, 0, (byte) 0, 0, 0);
        DatagramPacket a = new DatagramPacket(input, input.length);
        client.printFilenameFromDataPacket(a);

    }


}