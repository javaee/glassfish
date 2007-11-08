package com.sun.s1asdev.ejb.timer.timertests;

import java.rmi.RemoteException;
import javax.ejb.*;

public interface BarHome extends EJBHome {

    Bar create(Long value1, String value2) 
        throws CreateException, RemoteException;
    Bar createWithTimer(Long value1, String value2) 
        throws CreateException, RemoteException;
    Bar findByPrimaryKey(BarPrimaryKey bpk) throws FinderException, RemoteException;
    
    void newTimerAndRemoveBean(Long value1, String value2) throws RemoteException;
    void newTimerAndRemoveBeanAndRollback(Long value1, String value2) throws RemoteException;
    void nixBeanAndRollback(Bar b) throws RemoteException;
}
