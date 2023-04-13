package com.nedap.university.Timer;

import java.util.Timer;
import java.util.TimerTask;

public class TimeOutTaskRequest extends TimerTask {

    private Timer timer ;

    public TimeOutTaskRequest(Timer timer) {
        this.timer = timer;
    }

    @Override
    public void run() {
        // TODO
        System.out.println("not implement yet");
        timer.cancel();
    }
}
