package com.sun.s1asdev.jdbc.notxconn.test1.ejb;



import javax.ejb.*;
import java.rmi.RemoteException;

import java.util.*;
import java.io.*;

public interface NoTxConnTest extends EJBObject {
    public boolean test1() throws RemoteException;
    public boolean test2() throws RemoteException;
}
