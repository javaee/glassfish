package com.sun.s1asdev.jdbc.txpassthrough.ejb;


import javax.ejb.*;
import java.rmi.RemoteException;
import java.util.*;
import java.io.*;

public interface SecondHome extends EJBHome {
    public Second create() throws CreateException, java.rmi.RemoteException;
}
