package com.sun.s1asdev.ejb.slsb;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SimpleSLSBHome
    extends EJBHome
{
    SimpleSLSB create()
        throws RemoteException, CreateException;
}
