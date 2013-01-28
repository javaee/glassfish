package com.acme.ejb32.timer.opallowed;

import javax.ejb.Local;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;

@Local
public interface SingletonTimeoutLocal {
    public Timer createLocalTimer(String info);
}
