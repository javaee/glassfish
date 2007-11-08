package com.sun.s1asdev.jdbc.connsharing.nonxa.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleSession2Home extends EJBHome
{
    SimpleSession2 create()
        throws RemoteException, CreateException;

}
