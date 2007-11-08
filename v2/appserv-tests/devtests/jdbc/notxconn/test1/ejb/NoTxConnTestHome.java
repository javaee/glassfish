package com.sun.s1asdev.jdbc.notxconn.test1.ejb;


import javax.ejb.*;
import java.rmi.RemoteException;
import java.util.*;
import java.io.*;

public interface NoTxConnTestHome extends EJBHome {
    public NoTxConnTest create() throws CreateException, java.rmi.RemoteException;
}
