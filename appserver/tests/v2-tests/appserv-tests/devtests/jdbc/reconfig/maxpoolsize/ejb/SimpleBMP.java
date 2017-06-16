package com.sun.s1asdev.jdbc.reconfig.maxpoolsize.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP extends EJBObject {
    public boolean test1(int maxPoolSize, boolean expectToPass, boolean useXA ) 
        throws RemoteException;
}
