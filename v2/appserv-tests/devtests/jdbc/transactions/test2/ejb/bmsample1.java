//package com.iplanet.tests.jts.txnstatus.test21;
package com.sun.s1asdev.jdbc.transactions.test2.ejb;



import javax.ejb.*;
import java.rmi.RemoteException;

import java.util.*;
import java.io.*;

public interface bmsample1 extends EJBObject
{
    public int performDBOps() throws RemoteException;
	public int performDBOps2() throws RemoteException;

}
