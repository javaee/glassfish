package com.sun.s1asdev.jdbc.statementtimeout.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
        extends EJBObject {
    public boolean statementTest() throws RemoteException;

    public boolean preparedStatementTest() throws RemoteException;

    public boolean callableStatementTest() throws RemoteException;
}
