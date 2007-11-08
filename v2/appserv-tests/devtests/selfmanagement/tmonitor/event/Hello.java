package com.sun.s1peqe.selfmanagement.tmonitor.event;

public class Hello implements com.sun.s1peqe.selfmanagement.tmonitor.event.HelloMBean {

    public Hello() {}

    public long getValue() {
                                        
        return this.value;                                                                                                
    }
    
    private long value = 10;
}
