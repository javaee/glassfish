package com.sun.s1asdev.jdbc.dmmcfnotxconn.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface SimpleSessionHome extends EJBHome
{
    SimpleSession create()
        throws RemoteException, CreateException;

}
