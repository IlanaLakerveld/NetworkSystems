package com.nedap.university.Timer;

import com.nedap.university.MakePacket;
import com.nedap.university.Sending;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class has a function that is being called when the timer expires in on the sending side.
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
        int sequenceNumber = MakePacket.getSequenceNumber(datagramPacket.getData());
        if(sequenceNumber > sendingClass.filePointer){
            System.out.println("file pointer" + sendingClass.filePointer +"is smaller then sequence number "+ sequenceNumber + "so the sequence number is not acknowledged yet, so resending");
            try {
                sendingClass.socket.send(datagramPacket);
                new TimeOut(100,sendingClass,datagramPacket) ;
                timer.cancel() ;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        timer.cancel() ;
    }
}
