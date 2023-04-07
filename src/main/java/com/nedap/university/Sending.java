package com.nedap.university;

import java.io.File;

/**
 *  This class is used to send a file.
 *  This class assumes The file exist.
 */
public final class Sending {

    public static void Sending(File file) {

        Integer[] fileContent;
        int dataSizePacket;
        int Headersizes;
//        int TotalNumberOfPackets = (fileContent.length / dataSizePacket) + 1;
        boolean finished = false;
        while(!finished){

            //sendingPart

            // getting the current number of data

            // creating the corresponding headers

            // sending
            // update lastDataSend
            // setting a timer TimeoutElapsed()

            //waiting for an ack part

            // when ack ->
                // if(ack you need)
                // if(check checksum  is correct)
                // update LastAckReceived
                // ga uit deze whileloop




        }
    }

    public void TimeoutElapsed(){

    }
}
