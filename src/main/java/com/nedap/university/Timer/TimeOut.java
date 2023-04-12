package com.nedap.university.Timer;

import com.nedap.university.Sending;

import java.net.DatagramPacket;
import java.util.Timer;

/**
 * Set a timer for a time and after that time it calles a funtion.
 */
public class TimeOut {
    Timer timer ;

    public TimeOut(int milliSeconds, Sending send, DatagramPacket datagramPacket){
        timer = new Timer() ;
        timer.schedule(new TimeOutTask(timer, send,datagramPacket),milliSeconds);
    }
}
