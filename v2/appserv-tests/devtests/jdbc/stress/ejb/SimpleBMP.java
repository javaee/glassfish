package com.sun.s1asdev.jdbc.stress.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
    extends EJBObject {
    public boolean test1(int id_) throws RemoteException;
}
