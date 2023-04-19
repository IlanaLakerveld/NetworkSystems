package com.nedap.university.Timer;

import com.nedap.university.Client.Client;
import com.nedap.university.Sending;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;

/**
 * Set a timer for a time and after that time it calls a function TimeOut function.
 */
public class TimeOut {
    private Timer timer ;
    public int milliSeconds ;
    private int amount ;

    public TimeOut(int milliSeconds, Sending send, DatagramPacket datagramPacket,int amount ){
        timer = new Timer() ;
        this.amount = amount ;
        this.milliSeconds = milliSeconds;
        timer.schedule(new TimeOutTask(timer, send,datagramPacket, amount),milliSeconds);
    }
    public TimeOut(int milliSeconds , DatagramSocket socket , DatagramPacket datagramPacket, Client client){
        timer = new Timer();
        this.milliSeconds = milliSeconds;
        timer.schedule(new TimeOutTaskRequest(timer,client,socket, datagramPacket),milliSeconds);
    }

}
