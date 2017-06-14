/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package beans;

import connector.MyAdminObject;
import javax.rmi.PortableRemoteObject;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

import javax.naming.*;

public class WorkTestEJB implements SessionBean {

    private MyAdminObject Controls;

    public WorkTestEJB() {}

    public void ejbCreate() 
        throws CreateException {
        System.out.println("bean removed");
    }

    public void executeTest() {
	try {
            Controls.setup();
            Controls.submit();
            Controls.triggerWork();
            Controls.checkResult();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new EJBException(e);
	}
    }

    public void setSessionContext(SessionContext context) {
        try {
            Context ic = new InitialContext();
	    Controls = (MyAdminObject) ic.lookup("java:comp/env/eis/testAdmin");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void ejbRemove() {
        System.out.println("bean removed");
    }

    public void ejbActivate() {
        System.out.println("bean activated");
    }

    public void ejbPassivate() {
        System.out.println("bean passivated");
    }

    private void debug(String msg) {
        System.out.println("[MessageCheckerEJB]:: -> " + msg);
    }
}
