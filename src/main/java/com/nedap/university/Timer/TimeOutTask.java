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
    private Timer timer;
    private Sending sendingClass;
    private DatagramPacket datagramPacket;

    private int amount;

    public TimeOutTask(Timer timer, Sending sendingClass, DatagramPacket datagramPacket, int amount) {
        this.amount = amount;
        this.timer = timer;
        this.sendingClass = sendingClass;
        this.datagramPacket = datagramPacket;
    }

    /**
     * This function will be called when the timer expires.
     */
    @Override
    public void run() {
        int sequenceNumber = MakePacket.getSequenceNumber(datagramPacket.getData());
        if (amount < 20) {
            if (sequenceNumber > sendingClass.filePointer) {
                System.out.println("file pointer" + sendingClass.filePointer + "is smaller then sequence number " + sequenceNumber + "so the sequence number is not acknowledged yet, so resending");
                System.out.println("amount is :"+ amount);
                amount = amount +1 ;

                try {
                    sendingClass.socket.send(datagramPacket);
                    new TimeOut(amount*100, sendingClass, datagramPacket, amount);
                    timer.cancel();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                sendingClass.timerhasTriedToManyTimes =true ;
                timer.cancel();
            }
        } else {
            System.out.println("I tried 20 times I give up");
            timer.cancel();
        }

    }

}

