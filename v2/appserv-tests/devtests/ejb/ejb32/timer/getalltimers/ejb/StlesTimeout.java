package com.acme.ejb32.timer.getalltimers;

@javax.ejb.Remote
public interface StlesTimeout {

    public void createProgrammaticTimers();
    public void verify();
}
