package com.nedap.university;

public final class Reveiver {
    public static Integer[] receiver(String name){
        String filename = name ;
        Integer[] fileContests = new Integer[0];
        int lastReceivedPacket ;
        boolean stopReceiver = false;
        while(!stopReceiver){


            // receive packet
            // check if is the data you expect
            // check if the checksums? is correct

            // send ack

            // append the packet's data part (excluding the header) to the fileContents array, first making it larger
            // extends the packet


            //if(packet is last packet) that is as flag Ipv6 header is 0 ;

            stopReceiver = true;

        }
        return fileContests ;
    }
}
