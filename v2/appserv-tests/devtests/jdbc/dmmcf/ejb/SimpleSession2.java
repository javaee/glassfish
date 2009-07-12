package com.sun.s1asdev.jdbc.dmmcf.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleSession2 extends EJBObject {
    public boolean test1() throws RemoteException;
    public boolean test2() throws RemoteException;
    public boolean test3() throws RemoteException;
}
