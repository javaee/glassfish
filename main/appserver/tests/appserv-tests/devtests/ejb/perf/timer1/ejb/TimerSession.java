package com.sun.s1asdev.ejb.perf.timer1;

import javax.ejb.*;

@Remote
public interface TimerSession {

    public void createTimer(int intervalInMillis, int numTimeouts);
    
}

