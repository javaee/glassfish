package com.sun.s1asdev.ejb.bmp.twolevel.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMPHome
    extends EJBHome
{
    SimpleBMP create(int i)
        throws RemoteException, CreateException;

    SimpleBMP findByPrimaryKey(Integer key)
        throws RemoteException, FinderException;
}
