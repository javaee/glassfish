package com.sun.s1asdev.connector.txlevelswitch.test1.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleSession extends EJBObject {
    public boolean test1() throws RemoteException;
    public boolean test2() throws RemoteException;
    public boolean jmsJdbcTest1() throws RemoteException;
    public boolean jmsJdbcTest2() throws RemoteException;
    public boolean jmsJdbcTest3() throws RemoteException;
}
