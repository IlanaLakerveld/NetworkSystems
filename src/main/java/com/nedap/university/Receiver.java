package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public  class Receiver {

    //todo change name
    static final int DATASIZE = 512;   // max. number data bytes in each packet

    public  byte[] receiver(DatagramSocket socket, InetAddress address , int port) throws IOException {

        byte[] file = new byte[0];
        int lastReceivedPacket  = 0 ;
        int windowSize = 0 ;
        boolean finished = false;
        while(!finished){

            // receive packet
            byte[] receivedPacket = new byte[DATASIZE];
            DatagramPacket request = new DatagramPacket(receivedPacket, receivedPacket.length);
            socket.receive(request);

            int seqNum = MakePacket.getSequenceNumber(receivedPacket);
            if (seqNum == lastReceivedPacket+(request.getLength() - MakePacket.personalizedHeaderLength)) { // need to change if it's not  stop & wait
                int checksum = MakePacket.getCheckSumInteger(receivedPacket) ;
                // TOdo give logical input for checksum and add the checksum

                //TODO CHANGe
                if ((checksum != MakePacket.checksum(MakePacket.getInputForChecksumWithoutHeader(receivedPacket)))){ //
                    System.out.println("checksum is incorrect");
                }

                else{
                    System.out.println("checksum if correct");
                // toDo change sending???
                    // sending an acknowledgement
                    byte[] ack = MakePacket.makePacket(new byte[]{1},0,seqNum,(byte) 0,windowSize, MakePacket.getSessionNumber(receivedPacket));
                    DatagramPacket packet = new DatagramPacket(ack,0,ack.length,address,port) ;
                    socket.send(packet);

                    // Update the file
                    int oldLength = file.length ;
                    file = Arrays.copyOf(file, oldLength  + (receivedPacket.length - MakePacket.personalizedHeaderLength));
                    System.arraycopy(receivedPacket, MakePacket.personalizedHeaderLength, file, oldLength , (receivedPacket.length - MakePacket.personalizedHeaderLength));
                    lastReceivedPacket = seqNum ;

                    // check if this is the last packet
                    if((receivedPacket[9]&1) == 1){ //if fin flag is set
                        finished = true;
                        System.out.println("finished receiving");
                   }
                }

            }

        }
        return file ;

    }
}
