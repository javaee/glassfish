package com.sun.s1asdev.ejb.stress.passivateactivate.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface SFSB
    extends EJBObject
{

	public String getName()
        throws RemoteException;


}
