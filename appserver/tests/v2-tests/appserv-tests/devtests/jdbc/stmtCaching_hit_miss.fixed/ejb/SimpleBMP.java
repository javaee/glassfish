package com.sun.s1asdev.jdbc.stmtcaching.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
        extends EJBObject {
    public boolean testHit() throws RemoteException;
    public boolean testMiss() throws RemoteException;
    public boolean testHitColumnIndexes() throws RemoteException;
    public boolean testHitColumnNames() throws RemoteException;
}
