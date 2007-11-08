package com.sun.s1asdev.jdbc.jdbcjmsauth.ejb;

import javax.ejb.EJBHome;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public interface JmsAuthHome extends EJBHome {
    public JmsAuth create() throws CreateException, RemoteException;
}
