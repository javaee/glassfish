package com.sun.s1asdev.ejb.jms.jmsejb2;


import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;


public interface HelloHome extends EJBHome {
    Hello create (String str) throws RemoteException, CreateException;
}
