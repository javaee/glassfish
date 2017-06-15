package com.sun.s1asdev.jdbc.notxconn.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface SimpleSessionHome extends EJBHome
{
    SimpleSession create()
        throws RemoteException, CreateException;

}
