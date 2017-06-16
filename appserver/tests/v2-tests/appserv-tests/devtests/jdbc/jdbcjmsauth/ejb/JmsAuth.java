package com.sun.s1asdev.jdbc.jdbcjmsauth.ejb;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface JmsAuth extends EJBObject {
    public boolean test1() throws RemoteException;
    public boolean test2() throws RemoteException;
    public boolean test3() throws RemoteException;
    public boolean test4() throws RemoteException;
    public boolean test5() throws RemoteException;
    public boolean test6() throws RemoteException;
    public boolean test7() throws RemoteException;
    
}
