package com.sun.s1asdev.ejb.timer.sessiontimer;

import javax.ejb.*;
import java.util.concurrent.*;

@Singleton
@LocalBean
@Remote(TimerSingletonRemote.class)
@Lock(LockType.READ)
public class TimerSingleton
{
    
    boolean timeoutReceived = false;

    public void startTest() {
	timeoutReceived = false;
    }

    public void setTimeoutReceived() {
	System.out.println("TimerSingleton::setTimeoutReceived()");
	timeoutReceived = true;
    }

     public boolean waitForTimeout(int seconds) {
	 int i = 0;
	 while(i < seconds) {
	     i++;
	     try {
		 Thread.sleep(1000);
		 if( timeoutReceived ) {
		     System.out.println("Got timeout after " +
					i + " seconds");
		     break;
		 }
	     } catch(Exception e) {
		 e.printStackTrace();
		 throw new EJBException(e);
	     }
	 }
	return timeoutReceived;
    }

}
