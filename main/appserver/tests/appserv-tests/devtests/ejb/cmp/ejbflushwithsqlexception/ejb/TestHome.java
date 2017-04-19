package com.sun.s1asdev.ejb.ejbflush;

import javax.ejb.*;
import java.rmi.*;

public interface TestHome extends EJBHome {
 
    Test create() throws RemoteException, CreateException;
}
