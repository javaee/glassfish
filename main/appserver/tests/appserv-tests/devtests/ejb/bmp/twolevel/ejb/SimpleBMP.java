package com.sun.s1asdev.ejb.bmp.twolevel.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
    extends EJBObject
{
    public void foo()
        throws RemoteException;
}
