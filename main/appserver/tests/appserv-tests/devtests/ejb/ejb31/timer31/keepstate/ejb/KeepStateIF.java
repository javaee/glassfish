package com.sun.s1asdev.ejb31.timer.keepstate;

@javax.ejb.Remote
public interface KeepStateIF {
    public static final String OLD_INFO = "xxx";
    public static final String NEW_INFO = "yyy";
    public static final String INFO = OLD_INFO;

    public String verifyTimers(boolean keepState) throws Exception;
}
