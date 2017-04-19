package com.sun.s1asdev.ejb.bmp.twolevel.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import javax.rmi.*;
import java.util.*;
import java.sql.*;

public class StatelessBean
    implements SessionBean
{

    private SessionContext  sessionContext;
    private SimpleBMPHome   bmpHome;

    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;

	    Context context = null;
	    try {
	        context    = new InitialContext();
	        Object objRef = context.lookup("java:comp/env/ejb/SimpleBMPHome");
	        bmpHome = (SimpleBMPHome) PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);
	    } catch (NamingException e) {
	        throw new EJBException("cant find SimpleBMPHome");
	    }
    }

    public void createBMP(Integer key)
        throws RemoteException
    {
        try {
            SimpleBMP bmp = bmpHome.create(key.intValue());
        } catch (Exception ex) {
            throw new RemoteException("Error while creating SimpleBMP: " + key);
        }
    }

    public void createBMPAndTest(Integer key)
        throws RemoteException
    {
        try {
            SimpleBMP bmp = bmpHome.create(key.intValue());
            bmp.foo();

            SimpleBMP bmp1 = bmpHome.findByPrimaryKey(key);
            bmp1.foo();
        } catch (Exception ex) {
            throw new RemoteException("Error while creating SimpleBMP: " + key);
        }
    }

    public void ejbCreate() {}

    public void ejbRemove() {}

    public void ejbActivate() {}

    public void ejbPassivate() {}

    public void unsetEntityContext() {}

}
