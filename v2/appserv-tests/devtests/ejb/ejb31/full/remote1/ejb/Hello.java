package com.acme;

import javax.ejb.Remote;
import java.util.concurrent.Future;
import javax.ejb.Asynchronous;

@Remote
public interface Hello {

    String hello();

    @Asynchronous
    Future<String> helloAsync();

    @Asynchronous
    Future<String> asyncBlock(int seconds);

    // marked async on bean class
    void fireAndForget();
	
    @Asynchronous
    Future<String> asyncThrowException(String exceptionType);

    @Asynchronous
    Future<String> asyncCancel(int seconds) throws Exception;
    
    void throwException(String exceptionType);
}