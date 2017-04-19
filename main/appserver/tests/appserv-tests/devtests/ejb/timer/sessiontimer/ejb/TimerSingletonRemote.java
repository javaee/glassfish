package com.sun.s1asdev.ejb.timer.sessiontimer;

import javax.ejb.*;

@Remote
public interface TimerSingletonRemote {

    public void startTest();
    
    public boolean waitForTimeout(int seconds);

}