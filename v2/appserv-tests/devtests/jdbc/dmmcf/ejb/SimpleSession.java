package com.sun.s1asdev.jdbc.dmmcf.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleSession extends EJBObject {
    public boolean test1() throws RemoteException;

    public boolean test2() throws RemoteException;

    public boolean test3() throws RemoteException;

    public boolean test4() throws RemoteException;

    public boolean test5() throws RemoteException;

    public boolean query() throws RemoteException;

    public boolean query2() throws RemoteException;

    public boolean test6() throws RemoteException;
}
