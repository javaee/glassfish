package com.sun.s1asdev.jdbc.transactions.test1.ejb;


import javax.ejb.*;
import java.rmi.RemoteException;
import java.util.*;
import java.io.*;

public interface bmsample4home extends EJBHome
{
    public bmsample4 create() throws CreateException, java.rmi.RemoteException;
}
