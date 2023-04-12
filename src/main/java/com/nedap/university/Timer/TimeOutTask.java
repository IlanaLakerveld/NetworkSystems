package com.nedap.university.Timer;

import com.nedap.university.MakePacket;
import com.nedap.university.Sending;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class has a function that is being called when the timer expires.
 */
public class TimeOutTask extends TimerTask {
    private Timer timer ;
    private Sending sendingClass;
    private DatagramPacket datagramPacket ;
    public TimeOutTask(Timer timer, Sending sendingClass, DatagramPacket datagramPacket) {
        this.timer = timer ;
        this.sendingClass = sendingClass ;
        this.datagramPacket = datagramPacket;
    }

    /**
     * This function will be called when the timer expires.
     */
    @Override
    public void run() {

        if(MakePacket.getSequenceNumber(datagramPacket.getData()) <= sendingClass.filePointer){
            System.out.println("current filepoint is "+ sendingClass.filePointer + "and is smaller than sequence number so already acknowledged ");
        }
        else{
            System.out.println("current filepoint is "+ sendingClass.filePointer + "and is bigger than sequence number  so resending dataram packet");
            try {
                sendingClass.socket.send(datagramPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        timer.cancel() ;
    }
}
