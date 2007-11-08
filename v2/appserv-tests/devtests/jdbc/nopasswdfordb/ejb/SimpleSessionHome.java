package com.sun.s1asdev.jdbc.nopasswdfordb.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleSessionHome extends EJBHome
{
    SimpleSession create()
        throws RemoteException, CreateException;

}
