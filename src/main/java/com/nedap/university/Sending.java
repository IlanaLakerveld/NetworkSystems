package com.nedap.university;


import com.nedap.university.Timer.TimeOut;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * This class is used to send a file.
 * This class assumes The file exist.
 */
public class Sending {

    static final int DATASIZE = 512;   // max. number of user data bytes in each packet should be the same on sending side
    public int filePointer;
    public DatagramSocket socket;
    public boolean timerHasTriedToManyTimes;

    public Sending(DatagramSocket socket) {
        this.socket = socket;
    }

    public void sending(byte[] file, InetAddress address, int port) throws IOException {

        timerHasTriedToManyTimes = false;
        int totalNumberOfPackets = ((file.length / (DATASIZE - MakePacket.personalizedHeaderLength)) + 1);
        filePointer = 0;
        boolean finished = false;
        int windowSize = 1; // Stop and wait protocol
        byte flagsByte = MakePacket.setFlags(false, true, false, false, false, false, false);
        int sessionNumber = (int) (Math.random() * 1000);
        System.out.println("total number of packets are " + totalNumberOfPackets);

        // While not finished sending the file
        while (!finished) {

            // Create and send a new packet of appropriate size
            int lengthPayloadSend = Math.min(DATASIZE - MakePacket.personalizedHeaderLength, file.length - filePointer);
            // Check if it is the last file you are going to send
            if (lengthPayloadSend + filePointer == file.length) {
                flagsByte = MakePacket.finFlagByte;
            }
            // Send the packet
            byte[] data = Arrays.copyOfRange(file, filePointer, (filePointer + lengthPayloadSend));
            byte[] packet = MakePacket.makePacket(data, (filePointer + lengthPayloadSend), 0, flagsByte, 1, sessionNumber);
            DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, address, port);
            socket.send(packetToSend);

            //Set time out
            new TimeOut(100, this, packetToSend,0);

            // Waiting for the acknowledgement
            boolean stopSending = true;
            while (stopSending) {
                byte[] ackPacket = new byte[MakePacket.personalizedHeaderLength + 1];
                DatagramPacket obtainedDatagram = new DatagramPacket(ackPacket, ackPacket.length);
                socket.receive(obtainedDatagram);
                if (MakePacket.ackFlagByte == MakePacket.getFlag(obtainedDatagram.getData())) {

                    // Check if the packet is correct.
                    int checksum = MakePacket.getCheckSumInteger(ackPacket);
                    if (checksum == MakePacket.checksum(MakePacket.getInputForChecksumWithoutHeader(ackPacket))) {

                        // Check is this is the acknowledgement number you expect
                        int ack = MakePacket.getAckNumber(ackPacket);
                        if (ack == (filePointer + lengthPayloadSend)) {
                            filePointer += lengthPayloadSend;
                            stopSending = false;
                        }
                    }
                    // if the last packet is acknowledged
                    if (filePointer == file.length) {
                        finished = true;
                        System.out.println("Finished");
                    }
                } else {
                    // if the last packet is sent and resent, after a while it tried to many times and stop resending
                    if(lengthPayloadSend + filePointer == file.length){
                        if(timerHasTriedToManyTimes){
                            finished= true ;
                            stopSending = false ;
                        }

                    }
                }
            }

        }
    }


}
