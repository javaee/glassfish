package com.sun.s1asdev.jdbc.statementwrapper.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP extends EJBObject {
    public boolean preparedStatementTest1(String tableName, String value) throws RemoteException;
}
