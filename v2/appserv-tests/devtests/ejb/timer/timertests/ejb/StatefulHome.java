package com.sun.s1asdev.ejb.timer.timertests;

import java.rmi.RemoteException;
import javax.ejb.*;

public interface StatefulHome extends EJBHome {
    Stateful create(TimerHandle th) throws CreateException, RemoteException;
}
