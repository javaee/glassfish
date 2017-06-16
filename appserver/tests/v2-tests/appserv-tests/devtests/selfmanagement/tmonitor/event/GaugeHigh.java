package com.sun.s1peqe.selfmanagement.tmonitor.event;
                                                                                                                              
public class GaugeHigh implements com.sun.s1peqe.selfmanagement.tmonitor.event.GaugeHighMBean {
                                                                                                                              
    public GaugeHigh() {}
                                                                                                                              
    public double getValue() {
                                                                                                                              
        return this.value;
    }
                                                                                                                              
    private double value = 10;
}

