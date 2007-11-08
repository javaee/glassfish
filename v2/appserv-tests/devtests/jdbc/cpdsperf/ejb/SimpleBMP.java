package com.sun.s1asdev.jdbc.cpdsperf.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
    extends EJBObject {
    public long test1() throws RemoteException;
    public long test2() throws RemoteException;
}
