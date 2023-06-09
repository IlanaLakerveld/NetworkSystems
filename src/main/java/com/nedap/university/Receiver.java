package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Uses this class if you want to receive a file.
 * This class looks at the input packets and if the right packet and checksum is correct then added it to the byte file.
 * At the end it returns a byte array containing the requested file.
 * If there is something wrong and an error message is send this class stops and the error messages will be returned.
 */
public class Receiver {

    static final int DATASIZE = 512;   // max. number data bytes in each packet should be the same size as on the SENDING SIDE

    public byte[] receiver(DatagramSocket socket, InetAddress address, int port) throws IOException {

        byte[] file = new byte[0];
        int lastReceivedPacket = 0;
        int windowSize = 0;
        boolean finished = false;
        while (!finished) {

            // receive packet
            byte[] receivedPacket = new byte[DATASIZE];
            DatagramPacket request = new DatagramPacket(receivedPacket, receivedPacket.length);
            socket.receive(request); // is a blocking method

            // Checksum check
            int checksum = MakePacket.getCheckSumInteger(receivedPacket);
            if ((checksum != MakePacket.checksum(MakePacket.getInputForChecksumWithoutHeader(receivedPacket)))) { //
                System.out.println("checksum is incorrect");
            } else {

                // Received a packet correctly so sending an acknowledgement
                int seqNum = MakePacket.getSequenceNumber(receivedPacket);
                byte[] ack = MakePacket.makePacket(new byte[]{1}, 0, seqNum, MakePacket.ackFlagByte, windowSize, MakePacket.getSessionNumber(receivedPacket));
                DatagramPacket packet = new DatagramPacket(ack, 0, ack.length, address, port);
                socket.send(packet);
                // Only if it is a new packet then you need to add it
                if (seqNum == lastReceivedPacket + (request.getLength() - MakePacket.personalizedHeaderLength)) { // need to change if it's not  stop & wait
                    // Update the file
                    int oldLength = file.length;
                    file = Arrays.copyOf(file, oldLength + (receivedPacket.length - MakePacket.personalizedHeaderLength));
                    System.arraycopy(receivedPacket, MakePacket.personalizedHeaderLength, file, oldLength, (receivedPacket.length - MakePacket.personalizedHeaderLength));
                    lastReceivedPacket = seqNum;

                    // Check if this is the last packet
                    if ((MakePacket.getFlag(receivedPacket) & 1) == 1) { //if fin flag is set
                        finished = true;
                        System.out.println("finished receiving");
                    }
                }

            }

        }
        return file;

    }
}
