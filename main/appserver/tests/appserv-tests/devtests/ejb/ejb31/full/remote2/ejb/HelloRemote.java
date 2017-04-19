package com.acme;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface HelloRemote extends EJBObject {

    String hello() throws RemoteException;

}