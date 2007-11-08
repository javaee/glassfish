package com.sun.s1asdev.jdbc.reconfig.userpass.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
    extends EJBObject {
    public boolean test1(String user, String password,
            String tableName) throws RemoteException;
}
