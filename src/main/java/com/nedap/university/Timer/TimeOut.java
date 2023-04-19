package com.nedap.university.Timer;

import com.nedap.university.Client.Client;
import com.nedap.university.Sending;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;

/**
 * Set a timer for a time and after that time it calls a TimeOut function.
 * The different constructors are for different time out cases The first is when timer is set during sending (part of) a file, the second for sending a request.
 */
public class TimeOut {
    private Timer timer ;
    public int milliSeconds ;
    private int amount ;

    /**
     * This constructor is used when setting a timer wil sending a (part of) file
     * When timer expires the TimeOutTask function will be called
     * @param milliSeconds          Time in milliseconds before timer goes of
     * @param send                  Class send (used in the TimeOutTask function)
     * @param datagramPacket        Datagram packet that is sent
     * @param amountOfResending     Amount of resends.
     */
    public TimeOut(int milliSeconds, Sending send, DatagramPacket datagramPacket,int amountOfResending ){
        timer = new Timer() ;
        this.amount = amountOfResending ;
        this.milliSeconds = milliSeconds;
        timer.schedule(new TimeOutTask(timer, send,datagramPacket, amountOfResending),milliSeconds);
    }

    /**
     * Timer for sending a request. If the timer goes of, the TimeOutTaskRequest server is called.
     * @param milliSeconds              Time in milliseconds before timer goes of
     * @param socket                    Datagram socket
     * @param datagramPacket            Datagram packet that is sent
     * @param client                    Client that is sending the packet
     */
    public TimeOut(int milliSeconds , DatagramSocket socket , DatagramPacket datagramPacket, Client client){
        timer = new Timer();
        this.milliSeconds = milliSeconds;
        timer.schedule(new TimeOutTaskRequest(timer,client,socket, datagramPacket),milliSeconds);
    }

}
