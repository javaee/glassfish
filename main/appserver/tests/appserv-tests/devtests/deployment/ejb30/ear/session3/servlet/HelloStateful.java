package com.sun.s1asdev.ejb.ejb30.hello.session3;


public interface HelloStateful {

    public void hello();
    
    public void sleepFor(int sec);

    public void ping();
}
