package com.sun.s1asdev.ejb.bmp;

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
