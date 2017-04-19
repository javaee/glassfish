package com.sun.s1asdev.ejb.timer.timertests;

import java.rmi.RemoteException;
import javax.ejb.*;

public interface FooHome extends EJBHome {

    Foo create() throws CreateException, RemoteException;
}
