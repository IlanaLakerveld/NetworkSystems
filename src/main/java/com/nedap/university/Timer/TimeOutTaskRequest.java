package com.nedap.university.Timer;

import com.nedap.university.Client.Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;

public class TimeOutTaskRequest extends TimerTask {

    private Timer timer ;
    private Client client ;
    private DatagramPacket packet;
    private DatagramSocket socket;

    public TimeOutTaskRequest(Timer timer, Client client, DatagramSocket socket , DatagramPacket packet) {
        this.timer = timer;
        this.client=client ;
        this.socket = socket;
        this.packet = packet ;

    }

    @Override
    public void run() {

        boolean alreadyACKt = client.isGotACK() ;
        if(!alreadyACKt){
            System.out.println("resent the packet");
            try {
                socket.send(packet);
                new TimeOut(100, socket,packet,client);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        else{
//            System.out.println("already acked the request");

        }
        timer.cancel();
    }
}
