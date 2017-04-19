package com.sun.s1asdev.security.wss.permethod.servlet;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HelloIF extends Remote {
    public String sayHello(String message) throws RemoteException;
    public int sendSecret(String message) throws RemoteException;
    public String getSecret(double key) throws RemoteException;
}
