package com.sun.s1asdev.jdbc.simple.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
    extends EJBObject {
    public boolean test1( int numRuns) throws RemoteException;
    public boolean test2() throws RemoteException;
    public boolean test3() throws RemoteException;
    public boolean test4() throws RemoteException;
    public boolean test5() throws RemoteException;
    public boolean test6() throws RemoteException;
    public boolean test7() throws RemoteException;
    public boolean test8() throws RemoteException;
}
