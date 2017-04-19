package com.sun.s1asdev.ejb.jms.jmsejb2;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface Hello extends EJBObject {
    String sendMessageNoCommitPart1(String msg) throws RemoteException;
    void sendMessageNoCommitPart2() throws RemoteException;
}
