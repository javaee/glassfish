package com.sun.s1asdev.ejb.bmp.txtests.stateless.ejb;

import javax.ejb.*;

import com.sun.s1asdev.ejb.bmp.txtests.simple.ejb.SimpleBMPHome;
import com.sun.s1asdev.ejb.bmp.txtests.simple.ejb.SimpleBMP;
import com.sun.s1asdev.ejb.bmp.txtests.simple.ejb.CustomerInfo;

import javax.naming.*;
import javax.rmi.PortableRemoteObject;

public class SLSBEJB
    implements SessionBean 
{
	private SessionContext sessionCtx;

	public void ejbCreate() {}

	public void setSessionContext(SessionContext sc) {
		sessionCtx = sc;
	}

	// business method to create a timer
	public boolean doRollbackTest(int id) {
        boolean retVal = false;
        boolean doneRollback = false;
        try {
            String lookupName = "java:comp/env/ejb/SimpleBMPHome";
            Object objRef = (new InitialContext()).lookup(lookupName);
            SimpleBMPHome entityHome = (SimpleBMPHome)
                PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            sessionCtx.getUserTransaction().begin();
            entityHome.create(id);
            
            //This must be non null
            SimpleBMP entity = (SimpleBMP)
                entityHome.findByPrimaryKey(new Integer(id));

            int foundID = entity.getID();
            if (foundID != id) {
                return false;
            }

            sessionCtx.getUserTransaction().rollback();
            doneRollback = true;

            try {
                entity = (SimpleBMP)
                    entityHome.findByPrimaryKey(new Integer(id));
            } catch (FinderException finderEx) {
                //We must get this exception
                retVal = true;
            }
        } catch (Throwable th) {
            th.printStackTrace();
            if (! doneRollback) {
                try {
                    sessionCtx.getUserTransaction().rollback();
                } catch (Throwable rollTx) {
                }
            }
        }

		return retVal;
	}

    public boolean doReturnParamTest(int id) {

        boolean retVal = false;
        try {
            String lookupName = "java:comp/env/ejb/SimpleBMPHome";
            Object objRef = (new InitialContext()).lookup(lookupName);
            SimpleBMPHome entityHome = (SimpleBMPHome)
                PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            sessionCtx.getUserTransaction().begin();
            entityHome.create(id);
            
            //This must be non null
            SimpleBMP entity = (SimpleBMP)
                entityHome.findByPrimaryKey(new Integer(id));

            int foundID = entity.getID();
            if (foundID != id) {
                return false;
            }

            CustomerInfo customerInfo = entity.getCustomerInfo();
            retVal = (foundID == customerInfo.getCustomerID());
            
        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            try {
                sessionCtx.getUserTransaction().rollback();
            } catch (Throwable rollTx) {
            }
        }

		return retVal;
	}

	public void ejbRemove() {}

	public void ejbActivate() {}

	public void ejbPassivate() {}

}
