package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;

public  class Reveiver {
    public  byte[] receiver(String name, DatagramSocket socket, InetAddress address , int port) throws IOException {
        String filename = name ;
        byte[] file = new byte[0];
        int lastReceivedPacket  = 0 ;
        int windowSize = 0 ;
        boolean finished = false;
        while(!finished){

            // receive packet
            byte[] receivedPacket = new byte[512]; //todo change to real packet
            DatagramPacket request = new DatagramPacket(receivedPacket, receivedPacket.length);
            socket.receive(request);
            // todo change this only for testing
            Integer testvalue = request.getLength() - MakePacket.personalizedHeaderLength;;
            int seqNum = (int) (((receivedPacket[0]&0xff) << 24) | ((receivedPacket[1]&0xff) << 16) | ((receivedPacket[2]&0xff) << 8)  | receivedPacket[3] & 0xff );
            if (seqNum == lastReceivedPacket+(testvalue)) { // werkt alleen zo als stop & wait is
                int checksum = (receivedPacket[12] << 8) | receivedPacket[13];
                // TOdo give logical input for checksum
//                if ( (checksum == MakePacket.checksum(new int[]{0, 0}))){ // todo add!
//                    System.out.println("checksum is incorrect");
//                }
//                else{
                    byte[] ack = MakePacket.makePacket(new byte[]{1},0,seqNum,(byte) 0,windowSize, (receivedPacket[14]<<8|receivedPacket[15]));

                    // toDo sending veranderen
                    // sending(ack)
                    DatagramPacket packet = new DatagramPacket(ack,0,ack.length,address,port) ;
                    socket.send(packet);
                    int oldLength = file.length ;
                    file = Arrays.copyOf(file, oldLength  + (receivedPacket.length - MakePacket.personalizedHeaderLength));
                    System.arraycopy(receivedPacket, MakePacket.personalizedHeaderLength, file, oldLength , (receivedPacket.length - MakePacket.personalizedHeaderLength));
                    lastReceivedPacket = seqNum ;
                    if((receivedPacket[9]&1) == 1){ //if fin flag is set
                        finished = true;
                        System.out.println("finished receiving");
                   }
//                }

            }

        }
        return file ;

    }
}
