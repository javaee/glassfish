package com.acme;

import javax.ejb.*;
import java.rmi.RemoteException;


public interface HelloHome extends EJBHome {

    HelloRemote create() throws CreateException, RemoteException;

}