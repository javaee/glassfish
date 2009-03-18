package com.sun.s1asdev.connector.serializabletest.ejb;

import javax.ejb.EJBLocalObject;
import java.rmi.RemoteException;

public interface SimpleSession extends EJBLocalObject {
    public boolean test1() ;
}
