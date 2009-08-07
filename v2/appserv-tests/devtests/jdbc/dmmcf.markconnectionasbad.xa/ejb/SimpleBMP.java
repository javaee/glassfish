package com.sun.s1asdev.jdbc.markconnectionasbad.xa.ejb;

import javax.ejb.*;
import java.rmi.*;
import java.util.Set;

public interface SimpleBMP extends EJBObject {

    public Set<Integer> getFromLocalDS(int count) throws RemoteException;

    public boolean test1() throws RemoteException;

    public boolean test2() throws RemoteException;

    public boolean test3() throws RemoteException;

    public boolean test4() throws RemoteException;

    public boolean test5() throws RemoteException;

    public boolean test6() throws RemoteException;

    public boolean test7() throws RemoteException;

    public boolean test8() throws RemoteException;

    public boolean test9() throws RemoteException;

    public boolean test10() throws RemoteException;

    public boolean test11() throws RemoteException;

    public boolean test12() throws RemoteException;
}
