package com.acme.ejb32.timer.opallowed;

import javax.ejb.Remote;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;

@Remote
public interface SingletonTimeout {
    public TimerHandle createTimer(String info);
    public void cancelFromHelper();
}
