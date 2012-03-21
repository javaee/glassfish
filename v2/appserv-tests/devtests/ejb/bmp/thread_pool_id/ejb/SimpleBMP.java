package com.sun.s1asdev.ejb.bmp.simple.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
    extends EJBObject
{
    public void foo()
        throws RemoteException;

    public boolean isServicedBy(String threadPoolID)
        throws RemoteException;

}
