package com.sun.s1peqe.selfmanagement.tmonitor.event;
                                                                                                                              
public class GaugeLow implements com.sun.s1peqe.selfmanagement.tmonitor.event.GaugeLowMBean {
                                                                                                                              
    public GaugeLow() {}
                                                                                                                              
    public double getValue() {
                                                                                                                              
        return this.value;
    }
                                                                                                                              
    private double value = 1;
}

