package com.sun.s1asdev.ejb.allowedmethods.remove;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface Driver extends EJBObject {

    public boolean test() throws RemoteException;

}
