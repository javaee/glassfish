package com.sun.s1asdev.jdbc.txpassthrough.ejb;



import javax.ejb.*;
import java.rmi.RemoteException;

import java.util.*;
import java.io.*;

public interface Third extends EJBObject {
    public boolean test1() throws RemoteException;
}
