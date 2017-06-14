package com.sun.s1asdev.jdbc.txisolation.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP  extends EJBObject {
    
    public boolean test1(int i) throws RemoteException;

    public void modifyIsolation(int i) throws RemoteException;



}
