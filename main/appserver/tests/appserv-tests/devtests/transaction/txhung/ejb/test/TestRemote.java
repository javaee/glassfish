package com.sun.s1peqe.transaction.txhung.ejb.test;

import javax.ejb.*;
import java.rmi.*;

public interface TestRemote extends EJBObject {
 
    public boolean testA1(boolean xa) throws CreateException, RemoteException;
        
}
