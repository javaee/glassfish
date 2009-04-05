package com.acme;

import javax.ejb.*;
import java.util.concurrent.*;

@Remote
public interface RemoteAsync3 extends java.rmi.Remote {


    public Future<String> helloAsync() throws java.rmi.RemoteException;
    
    public Future<String> removeAfterCalling() throws java.rmi.RemoteException;
    
    public Future<String> throwException(String exception) throws java.rmi.RemoteException, CreateException;
    
}