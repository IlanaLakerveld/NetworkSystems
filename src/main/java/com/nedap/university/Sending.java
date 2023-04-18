package com.nedap.university;


import com.nedap.university.Timer.TimeOut;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * This class is used to send a file.
 * This class assumes The file exist.
 */
public class Sending {


    // TOdo wil je dit static houden of wil je dit kunnen veranderen.??
    static final int DATASIZE = 512;   // max. number of user data bytes in each packet
    public int lastFrameAcknowlegded;
    public int lastFrameSend ;
    public  boolean finished ;
    public DatagramSocket socket;
    public int lengthPayloadSend ;

    byte[] file ;

    public Sending(DatagramSocket socket) {
        this.socket = socket;
    }

    public void sending(byte[] file, InetAddress address, int port) throws IOException {

        this.file = file ;
        int totalNumberOfPackets = ((file.length / (DATASIZE - MakePacket.personalizedHeaderLength)) + 1); // naar boven afronden
        lastFrameAcknowlegded = 0; // Todo change? is dit niet sequment number or acknowlegded number ?
        lastFrameSend = 0;
        finished = false;
        int windowSize = 8; // Sliding window protocol with fixed window size

        byte flagsByte = MakePacket.setFlags(false, false, false, false, false, false, false);
        int sessionNumber = (int) (Math.random() * 1000);  // Todo change kan nu alleen maar number tussen 1-1000 zijn wil je deze niet naar je server plaatsen? 
        System.out.println("total number of packets are " + totalNumberOfPackets);
        lengthPayloadSend = 0;
        Thread t = new Thread(new recevingACK());
        t.start();

        while (!finished) {

            // only send when window size is not reached

            if ((lastFrameSend - lastFrameAcknowlegded) <= sessionNumber && lastFrameSend < file.length) {
                // create and send a new packet of appropriate size
                lengthPayloadSend = Math.min(DATASIZE - MakePacket.personalizedHeaderLength, file.length - lastFrameSend);
                // check if it is the last file you are going to send
                if (lengthPayloadSend + lastFrameSend == file.length) {
                    flagsByte = MakePacket.setFlags(true, false, false, false, false, false, false);

                }
                // send the packet
                byte[] data = Arrays.copyOfRange(file, lastFrameSend, (lastFrameSend + lengthPayloadSend));
                byte[] packet = MakePacket.makePacket(data, (lastFrameSend + lengthPayloadSend), 0, flagsByte, windowSize, sessionNumber);
                DatagramPacket packetToSend = new DatagramPacket(packet, packet.length, address, port);
                socket.send(packetToSend);
                System.out.println("last frame received is : "+ lastFrameSend);
                //set time out
                lastFrameSend += lengthPayloadSend;
                new TimeOut(1000, this, packetToSend);
            }
            else{
                try {
                    sleep(10) ;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }


    public class recevingACK implements Runnable {

        @Override
        public void run() {
            try {
                extracted();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void extracted() throws IOException {
            while (!finished) {
                List<Integer> ACKBuffer = new ArrayList<>();
                byte[] ackPacket = new byte[MakePacket.personalizedHeaderLength + 1];
                DatagramPacket obtainedDatagram = new DatagramPacket(ackPacket, ackPacket.length);


                socket.receive(obtainedDatagram);
                System.out.println("packet received "+ MakePacket.getAckNumber(obtainedDatagram.getData()));


                // todo ? wil je hier niet ook checken of ack flag is set dan kan namelijk ook error flag gezet worden
                if (MakePacket.ackFlagByte == MakePacket.getFlag(obtainedDatagram.getData())) {

                    // check if the packet is correct.
                    int checksum = MakePacket.getCheckSumInteger(ackPacket);
                    if (checksum == MakePacket.checksum(MakePacket.getInputForChecksumWithoutHeader(ackPacket))) {

                        // check is this is the acknowledgement number you expect
                        int ack = MakePacket.getAckNumber(ackPacket);
                        if (ack == (lastFrameAcknowlegded + lengthPayloadSend) || ack == lastFrameAcknowlegded + (DATASIZE - MakePacket.personalizedHeaderLength)) { // werkt alleen zo als stop & wait is
                            lastFrameAcknowlegded += lengthPayloadSend;
                            while (!ACKBuffer.isEmpty()) {
                                if (ACKBuffer.contains(lastFrameAcknowlegded + (DATASIZE - MakePacket.personalizedHeaderLength))) {
                                    lastFrameAcknowlegded += (DATASIZE - MakePacket.personalizedHeaderLength);
                                    ACKBuffer.remove((DATASIZE - MakePacket.personalizedHeaderLength));
                                } else if (ACKBuffer.contains(lastFrameAcknowlegded + lengthPayloadSend)) {
                                    lastFrameAcknowlegded += (DATASIZE - MakePacket.personalizedHeaderLength);
                                    ACKBuffer.remove((DATASIZE - MakePacket.personalizedHeaderLength));
                                } else {
                                    break;
                                }
                            }
                        } else if (ack > lastFrameAcknowlegded && ack <= lastFrameSend) {
                            ACKBuffer.add(ack);
                        } else {
                            System.out.println("get an ack out of range ");
                        }


                    }

                    if (lastFrameAcknowlegded == file.length) {
                        finished = true;
                        System.out.println("finished");
                    }
                } else {
                    System.out.println("this is not an acknowledgement packet");
                    System.out.println(MakePacket.getFlag(obtainedDatagram.getData()));

                }
            }
        }


    }


}
