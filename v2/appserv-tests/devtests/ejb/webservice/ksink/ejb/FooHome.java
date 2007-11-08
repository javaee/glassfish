package com.sun.s1asdev.ejb.webservice.ksink.googleserver;

import javax.ejb.EJBHome;
import java.rmi.RemoteException;

public interface FooHome extends EJBHome {

    FooRemote create() throws RemoteException;

}
