package com.sun.s1asdev.ejb.bmp.txtests.simple.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
    extends EJBObject
{
    public int getID()
        throws RemoteException;

    public CustomerInfo getCustomerInfo()
        throws RemoteException;
}
