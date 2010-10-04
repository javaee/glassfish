package com.sun.s1asdev.ejb.sfsb.keepstate.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleEntityHome
    extends EJBHome
{
    SimpleEntityRemote create(String key, String name)
        throws RemoteException, CreateException;

    SimpleEntityRemote findByPrimaryKey(String key)
        throws RemoteException, FinderException;
}
