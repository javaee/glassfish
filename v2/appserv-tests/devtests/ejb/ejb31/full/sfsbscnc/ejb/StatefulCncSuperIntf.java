package com.acme;

import javax.ejb.*;
import java.util.concurrent.*;

public interface StatefulCncSuperIntf {

    public String hello();

    public String helloWait();
  
    public Future<String> helloAsync();

    public void fireAndForget();

    public void sleep(int seconds);

    public void sleepAndRemove(int seconds);

    public void attemptLoopback();

    public void incrementCount(int secondsToSleep);

    public int getCount();

}