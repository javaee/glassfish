package com.sun.s1asdev.jdbc.transactions.test1.ejb;


import javax.ejb.*;
import java.rmi.RemoteException;

import java.util.*;
import java.io.*;

public interface bmsample3 extends EJBObject
{
    public int performDBOps() throws RemoteException;

}
