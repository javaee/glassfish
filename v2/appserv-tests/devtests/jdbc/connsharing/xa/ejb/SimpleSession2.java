package com.sun.s1asdev.jdbc.connsharing.xa.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleSession2 extends EJBObject {
    public boolean test1(String newVal, int key) throws RemoteException;
}
