package com.nedap.university;


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

    public void sending(byte[] file , DatagramSocket socket, InetAddress address, int port) throws IOException {


        int totalNumberOfPackets = ((file.length / (DATASIZE - MakePacket.personalizedHeaderLength)) + 1); // naar boven afronden
        int filePointer = 0; // Todo change? is dit niet sequment number?
        boolean finished = false;
        int windowSize = 1; // Stop en wait protocol
        int finFlag = 0 ;


        int i = 0 ;
        int sessionNumber = (int) (Math.random() * 1000);  // Todo change kan nu alleen maar nummber tussen 1-1000 zijn
        System.out.println("total number of packets are "+totalNumberOfPackets);
        while (!finished) {
            i++ ;
            System.out.println("packet number "+ i);
            if(i == 66){
                System.out.println("test");
            }
            //sendingPart
            // create a new packet of appropriate size
            int datalen = Math.min(DATASIZE-MakePacket.personalizedHeaderLength, file.length - filePointer);

            if(datalen+filePointer == file.length){
                finFlag=1;
                System.out.println("finflags");
            }
            byte[] data = Arrays.copyOfRange(file, filePointer, (filePointer + datalen));
            byte[] packet = MakePacket.makePacket(data, (filePointer+datalen), 0, (byte) finFlag, 1, sessionNumber);
            DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, address, port);
            socket.send(packetToSend); // Todo send packet

            // Todo set time out
            TimeoutElapsed(filePointer);


            boolean stopSending = true;
            while (stopSending) { // toDo wil je dit alleen als je je window size straks bereikt is?
                byte[] ackPacket = new byte[MakePacket.personalizedHeaderLength + 1]; //todo change to real packet
                DatagramPacket request = new DatagramPacket(ackPacket, ackPacket.length);
                socket.receive(request);

                // check if you had had aan ack
                int ack = (int) ((ackPacket[4] << 24) | ((ackPacket[5]&0xff) << 16) | ((ackPacket[6] &0xff) << 8) | (ackPacket[7] & 0xff));
                Integer testvalue = filePointer + (datalen);
                if (ack == testvalue) { // werkt alleen zo als stop & wait is
                    int checksum = (ackPacket[12] << 8) | ackPacket[13];
                    // TOdo give logical input for checksum
//                    if (checksum == MakePacket.checksum(new int[]{0, 0})) {
                    filePointer += datalen;
                    stopSending = false;
//                    }
                }


                if (filePointer == file.length) {
                    finished = true;
                    System.out.println("finished");
                }
            }

        }
    }

    public void TimeoutElapsed(int filePointer) {
        //TODo make timeout
    }
}
