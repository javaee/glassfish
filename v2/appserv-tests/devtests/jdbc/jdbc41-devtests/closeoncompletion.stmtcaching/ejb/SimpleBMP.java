package com.sun.s1asdev.jdbc.stmtcaching.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
        extends EJBObject {
    public boolean testCloseOnCompletion() throws RemoteException;
}
