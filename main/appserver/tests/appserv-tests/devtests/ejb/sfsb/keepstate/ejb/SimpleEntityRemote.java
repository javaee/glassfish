package com.sun.s1asdev.ejb.sfsb.keepstate.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleEntityRemote
    extends EJBObject
{
    public String getName()
        throws RemoteException;

    public void setName(String val)
        throws RemoteException;
}
