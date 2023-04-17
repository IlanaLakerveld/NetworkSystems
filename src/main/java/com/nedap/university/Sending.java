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


    // TOdo wil je dit static houden of wil je dit kunnen veranderen.??
    static final int DATASIZE = 512;   // max. number of user data bytes in each packet
    public int filePointer;
    public DatagramSocket socket;

    public Sending(DatagramSocket socket) {
        this.socket = socket;
    }

    public void sending(byte[] file, InetAddress address, int port) throws IOException {


        int totalNumberOfPackets = ((file.length / (DATASIZE - MakePacket.personalizedHeaderLength)) + 1); // naar boven afronden
        filePointer = 0; // Todo change? is dit niet sequment number or acknowlegded number ?
        boolean finished = false;
        int windowSize = 1; // Stop en wait protocol
        byte flagsByte = MakePacket.setFlags(false, false, false, false, false, false, false);
        int sessionNumber = (int) (Math.random() * 1000);  // Todo change kan nu alleen maar number tussen 1-1000 zijn wil je deze niet naar je server plaatsen? 
        System.out.println("total number of packets are " + totalNumberOfPackets);


        while (!finished) {

            // create and send a new packet of appropriate size
            int lengthPayloadSend = Math.min(DATASIZE - MakePacket.personalizedHeaderLength, file.length - filePointer);
            // check if it is the last file you are going to send 
            if (lengthPayloadSend + filePointer == file.length) {
                flagsByte = MakePacket.setFlags(true, false, false, false, false, false, false);

            }
            // send the packet
            byte[] data = Arrays.copyOfRange(file, filePointer, (filePointer + lengthPayloadSend));
            byte[] packet = MakePacket.makePacket(data, (filePointer + lengthPayloadSend), 0, flagsByte, 1, sessionNumber);
            DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, address, port);
            socket.send(packetToSend);

            //set time out
            new TimeOut(100, this, packetToSend);

            // waiting for the acknowledgement
            boolean stopSending = true;
            while (stopSending) {
                byte[] ackPacket = new byte[MakePacket.personalizedHeaderLength + 1];
                DatagramPacket obtainedDatagram = new DatagramPacket(ackPacket, ackPacket.length);
                socket.receive(obtainedDatagram);
                // todo ? wil je hier niet ook checken of ack flag is set dan kan namelijk ook error flag gezet worden 
                if (MakePacket.ackFlagByte == MakePacket.getFlag(obtainedDatagram.getData())) {

                    // check if the packet is correct.
                    int checksum = MakePacket.getCheckSumInteger(ackPacket);
                    if (checksum == MakePacket.checksum(MakePacket.getInputForChecksumWithoutHeader(ackPacket))) {

                        // check is this is the acknowledgement number you expect
                        int ack = MakePacket.getAckNumber(ackPacket);
                        if (ack == (filePointer + lengthPayloadSend)) { // werkt alleen zo als stop & wait is
                            filePointer += lengthPayloadSend;
                            stopSending = false;
                        }
                    }

                    if (filePointer == file.length) {
                        finished = true;
                        System.out.println("finished");
                    }
                } else {
                    System.out.println("this is not an acknowledgement packet");

                }
            }

        }
    }


}
