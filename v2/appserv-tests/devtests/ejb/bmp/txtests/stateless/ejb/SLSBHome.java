package com.sun.s1asdev.ejb.bmp.txtests.stateless.ejb;

import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;

public interface SLSBHome
    extends EJBHome
{
	SLSB create()
        throws RemoteException, CreateException;
}
