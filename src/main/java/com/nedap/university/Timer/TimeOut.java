package com.nedap.university.Timer;

import com.nedap.university.Sending;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;

/**
 * Set a timer for a time and after that time it calls a function TimeOut function.
 */
public class TimeOut {
    Timer timer ;

    public TimeOut(int milliSeconds, Sending send, DatagramPacket datagramPacket){
        timer = new Timer() ;
        timer.schedule(new TimeOutTask(timer, send,datagramPacket),milliSeconds);
    }
    public TimeOut(int miliSeconds, DatagramSocket socket , DatagramPacket datagramPacket){
        timer = new Timer();
        timer.schedule(new TimeOutTaskRequest(timer),miliSeconds);
    }

}
