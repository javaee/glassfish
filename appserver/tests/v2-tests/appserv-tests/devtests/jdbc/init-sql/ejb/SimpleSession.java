package com.sun.s1asdev.jdbc.initsql.ejb;

import javax.ejb.*;
import java.rmi.*;
import javax.sql.DataSource;

public interface SimpleSession extends EJBObject {
    public boolean test1(boolean caseSensitive) throws RemoteException;
}
