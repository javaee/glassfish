package com.sun.s1asdev.jdbc.connsharing.xa.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleSessionHome extends EJBHome
{
    SimpleSession create()
        throws RemoteException, CreateException;

}
