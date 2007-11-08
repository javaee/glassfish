package com.sun.s1asdev.ejb.bmp.twolevel.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface Stateless
    extends EJBObject
{
    public void createBMP(Integer key)
        throws RemoteException;

    public void createBMPAndTest(Integer key)
        throws RemoteException;
}
