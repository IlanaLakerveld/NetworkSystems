package com.nedap.university.Timer;

import com.nedap.university.Client.Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Will be called when a time out of a request packet goes off
 */
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

    /**
     * will check if the packet is already acknowledges and if not it will resend it and set a new timer.
     */
    @Override
    public void run() {

        boolean alreadyACKt = client.isGotACK() ;
        if(!alreadyACKt){
            System.out.println("resent the packet");
            try {
                socket.send(packet);
                new TimeOut(2000, socket,packet,client);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        timer.cancel();
    }
}
