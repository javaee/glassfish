package com.sun.s1asdev.ejb.bmp.txtests.stateless.ejb;

import javax.ejb.*;

public interface SLSB
    extends EJBObject
{

	public boolean doRollbackTest(int id)
        throws java.rmi.RemoteException;

    public boolean doReturnParamTest(int id)
        throws java.rmi.RemoteException;
}

