package com.sun.s1peqe.transaction.txhung.ejb.test;

import javax.ejb.*;
import java.rmi.*;

public interface TestHome extends EJBHome {
 
    TestRemote create() throws RemoteException, CreateException;
}
