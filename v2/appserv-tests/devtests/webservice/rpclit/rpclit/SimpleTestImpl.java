/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * @(#)SimpleTestImpl.java	1.3 02/04/05
 */

package rpclit;

import java.rmi.RemoteException;
import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;


// Service Implementation Class - as outlined in JAX-RPC Specification

public class SimpleTestImpl implements SessionBean {

    private SessionContext sc;

    public SimpleTestImpl(){}

    public EchoStringType echoString(StringResponseNameType v) 
        throws RemoteException
    {
        return null;  //for now
    }

    public void ejbCreate() throws RemoteException {
        System.out.println("In SimpleTestImpl::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
