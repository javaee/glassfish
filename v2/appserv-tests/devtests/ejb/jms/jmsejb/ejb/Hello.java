package com.sun.s1asdev.ejb.jms.jmsejb;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface Hello extends EJBObject {
    String sendMessage1(String msg) throws RemoteException;
    String sendMessage2(String msg) throws RemoteException;
    String sendMessage3(String msg) throws RemoteException;
    void receiveMessage1() throws RemoteException;
    void receiveMessage2() throws RemoteException;
    void receiveMessage3() throws RemoteException;
    String sendMessage4Part1(String msg) throws RemoteException;
    String sendMessage4Part2(String msg) throws RemoteException;
    void receiveMessage4Part1() throws RemoteException;
    void receiveMessage4Part2() throws RemoteException;
    void sendAndReceiveMessage() throws RemoteException;
    void sendAndReceiveRollback() throws RemoteException;
    String sendMessageRollback(String msg) throws RemoteException;
    void receiveMessageRollback() throws RemoteException;
}
