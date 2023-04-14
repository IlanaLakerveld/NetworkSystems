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
    public int filePointer ;
    public DatagramSocket socket ;

    public Sending(DatagramSocket socket) {
        this.socket = socket;
    }

    public void sending(byte[] file , InetAddress address, int port) throws IOException {


        int totalNumberOfPackets = ((file.length / (DATASIZE - MakePacket.personalizedHeaderLength)) + 1); // naar boven afronden
        filePointer = 0; // Todo change? is dit niet sequment number?
        boolean finished = false;
        int windowSize = 1; // Stop en wait protocol
        byte flagsByte = MakePacket.setFlags(false,false,false,false,false,false,false) ;
        int sessionNumber = (int) (Math.random() * 1000);  // Todo change kan nu alleen maar number tussen 1-1000 zijn
        System.out.println("total number of packets are "+totalNumberOfPackets);


        while (!finished) {

            // create and send a new packet of appropriate size
            int datalen = Math.min(DATASIZE-MakePacket.personalizedHeaderLength, file.length - filePointer);
            // check if it is the last file
            if(datalen+filePointer == file.length){
                flagsByte = MakePacket.setFlags(true,false,false,false,false,false,false) ;

            }
            byte[] data = Arrays.copyOfRange(file, filePointer, (filePointer + datalen));
            byte[] packet = MakePacket.makePacket(data, (filePointer+datalen), 0,  flagsByte, 1, sessionNumber);
            DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, address, port);
            socket.send(packetToSend);

            //set time out
            new TimeOut(100,this,packetToSend) ;
            // todo ? wil je hier niet ook checken of ack flag is set
            // waiting for the acknowledgement
            boolean stopSending = true;
            while (stopSending) { // toDo wil je dit alleen als je je window size straks bereikt is?
                byte[] ackPacket = new byte[MakePacket.personalizedHeaderLength + 1];
                DatagramPacket request = new DatagramPacket(ackPacket, ackPacket.length);
                socket.receive(request);

                // check if you had had aan ack
                int ack = MakePacket.getAckNumber(ackPacket);
                if (ack == (filePointer + datalen)) { // werkt alleen zo als stop & wait is
                    int checksum = MakePacket.getCheckSumInteger(ackPacket);

                    // TOdo give logical input for checksum
                    // check if the packet is correct.
                    if (checksum == MakePacket.checksum(MakePacket.getInputForChecksumWithoutHeader(ackPacket))) {
                        System.out.println("checksum correct");
                    filePointer += datalen;
                    stopSending = false;
                   }
                }


                if (filePointer == file.length) {
                    finished = true;
                    System.out.println("finished");
                }
            }

        }
    }


}
