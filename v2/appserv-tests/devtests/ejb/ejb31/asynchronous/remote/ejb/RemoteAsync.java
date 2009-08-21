package com.acme;

import javax.ejb.*;
import java.util.concurrent.*;

@Remote
public interface RemoteAsync {

    void startTest();

    void fireAndForget();
    
    public int getFireAndForgetCount();

    Future<String> helloAsync();

    Future<Integer> processAsync(int sleepInterval, int numIntervals) 
	throws Exception;

	

}