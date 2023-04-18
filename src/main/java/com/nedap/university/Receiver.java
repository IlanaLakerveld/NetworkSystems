package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Uses this class if you want to receive a file.
 * This class looks at the input packets and if the right packet and checksum is correct then added it to the byte file.
 * At the end it returns a byte array containing the requested file.
 * If there is something wrong and an error message is send this class stops and the error messages will be returned.
 */
public class Receiver {

    //todo change name
    static final int DATASIZE = 512;   // max. number data bytes in each packet

    public byte[] receiver(DatagramSocket socket, InetAddress address, int port) throws IOException {

        byte[] file = new byte[0];
        int lastReceivedPacket = 0;
        boolean finished = false;
        boolean gotFinflag = false;
        int windowSize;
        Map<Integer, byte[]> bufferPackets = new HashMap<>();
        int normalPayloadLength = DATASIZE - MakePacket.personalizedHeaderLength;
        int lastPacketLength = 0;


        while (!finished) {


            // receive packet
            byte[] receivedPacket = new byte[DATASIZE];
            DatagramPacket request = new DatagramPacket(receivedPacket, receivedPacket.length);
            socket.receive(request);
            // flag is set if something went wrong
            if (MakePacket.getFlag(request.getData()) == MakePacket.setFlags(false, false, false, false, false, true, false)) {
                String errorMessage = new String(receivedPacket, MakePacket.personalizedHeaderLength, request.getLength());
                return ("ERROR" + errorMessage.trim()).getBytes();
            }


            int checksum = MakePacket.getCheckSumInteger(receivedPacket);
            if ((checksum != MakePacket.checksum(MakePacket.getInputForChecksumWithoutHeader(receivedPacket)))) { //
                System.out.println("checksum is incorrect");
            } else {

                // sending an acknowledgement
                windowSize = MakePacket.getWindowsize(receivedPacket);
                int seqNum = MakePacket.getSequenceNumber(receivedPacket);
                byte[] payloadLength = lengthOfPayload(request.getLength() - MakePacket.personalizedHeaderLength);
                byte[] ack = MakePacket.makePacket(new byte[]{1}, 0, seqNum, MakePacket.setFlags(false, true, false, false, false, false, false), windowSize, MakePacket.getSessionNumber(receivedPacket));
                DatagramPacket packet = new DatagramPacket(ack, 0, ack.length, address, port);
                socket.send(packet);

                // check if this is the last packet
                if ((receivedPacket[9] & 1) == 1) { //if fin flag is set
                    System.out.println(MakePacket.getSequenceNumber(receivedPacket));
                    gotFinflag = true;
                    lastPacketLength = request.getLength() - MakePacket.personalizedHeaderLength;
                    System.out.println("fin is received");
                }


                // only if it is a new packet then you need to add it
                if (seqNum == lastReceivedPacket + normalPayloadLength) {

                    int oldLength = file.length;
                    file = Arrays.copyOf(file, oldLength + (receivedPacket.length - MakePacket.personalizedHeaderLength));
                    System.arraycopy(receivedPacket, MakePacket.personalizedHeaderLength, file, oldLength, (receivedPacket.length - MakePacket.personalizedHeaderLength));
                    lastReceivedPacket = seqNum;
                    if(MakePacket.getFlag(receivedPacket) == 1){
                        finished =true ;
                    }

                    while (!bufferPackets.isEmpty()) {
                        if (bufferPackets.containsKey(lastReceivedPacket + normalPayloadLength)) {
                            oldLength = file.length;
                            file = Arrays.copyOf(file, oldLength + (normalPayloadLength));
                            System.arraycopy(bufferPackets.get(lastReceivedPacket + normalPayloadLength), MakePacket.personalizedHeaderLength, file, oldLength, normalPayloadLength);
                            bufferPackets.remove(lastReceivedPacket + normalPayloadLength);
                            lastReceivedPacket += normalPayloadLength;
                        } else if (gotFinflag && bufferPackets.containsKey(lastReceivedPacket + lastPacketLength)) {
                            oldLength = file.length;
                            file = Arrays.copyOf(file, oldLength + lastPacketLength);
                            System.arraycopy(bufferPackets.get(lastReceivedPacket + lastPacketLength), MakePacket.personalizedHeaderLength, file, oldLength, lastPacketLength);
                            lastReceivedPacket += lastPacketLength;
                            bufferPackets.remove(lastReceivedPacket + lastPacketLength);
                            finished = true;

                        } else {
                            break;
                        }
                    }
                }
                else if(gotFinflag && seqNum == lastReceivedPacket + lastPacketLength){
                    int oldLength = file.length;
                    file = Arrays.copyOf(file, oldLength + lastPacketLength);
                    System.arraycopy(receivedPacket, MakePacket.personalizedHeaderLength, file, oldLength, lastPacketLength);
                    lastReceivedPacket += lastPacketLength;
                    finished = true;

                } else if (seqNum > lastReceivedPacket) {
                    bufferPackets.put(seqNum, receivedPacket);
                }


            }

        }
        return file;

    }

    private byte[] lengthOfPayload(int length) {
        byte[] returnByte = new byte[4];
        returnByte[0] = (byte) ((length >> 24) & 0xff);
        returnByte[1] = (byte) ((length >> 16) & 0xff);
        returnByte[2] = (byte) ((length >> 8) & 0xff);
        returnByte[3] = (byte) ((length) & 0xff);
        return returnByte;
    }
}
