package com.sun.s1asdev.jdbc.transactions.test2.ejb;


import javax.ejb.*;
import java.rmi.RemoteException;
import java.util.*;
import java.io.*;

public interface bmsample1home extends EJBHome
{
    public bmsample1 create() throws CreateException, java.rmi.RemoteException;
}
