/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package justbean;

import java.rmi.RemoteException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

public class JustSessionBean implements SessionBean {
 
    private SessionContext ctx;
    
    public void 
    ejbCreate() 
        throws RemoteException 
    {
        log("JustSessionBean.ejbCreate()...");
    }

    public void 
    ejbRemove() 
        throws RemoteException 
    {
        log("JustSessionBean.ejbRemove()...");
    }
    
    public void
    log(String message) 
    {
        Log.log(message);
    }

    public String[]
    findAllMarbles()
    {
        System.out.println("JustSessionBean.findAllMarbles()...");
        String[] strArray = new String[2];
        strArray[0] = "This is a test.";
        strArray[1] = "You have lost all your marbles.";
        return strArray;
    }
    
    
    /** 
     * ejbDestroy - called by the Container before this bean is destroyed.
     */ 
    public void 
    ejbDestroy() 
    {
        log("JustSessionBean.ejbDestroy()...");
    }
    
    /** 
     * ejbActivate - called by the Container after this bean instance 
     * is activated from its passive state.
     */
    public void 
    ejbActivate() 
    {
        log("JustSessionBean.ejbActivate()...");
    }
    
    /**
     * ejbPassivate - called by the Container before this bean instance 
     * is put in passive state. 
     */ 
    public void 
    ejbPassivate() 
    {
        log("JustSessionBean.ejbPassivate()...");
    }
    
    /**
     * setSessionContext - called by the Container after creation of this
     * bean instance.
     */
    public void 
    setSessionContext(SessionContext context) 
    {
        log("JustSessionBean.setSessionContext(ctx)... ctx = " + ctx);
        ctx = context;
    }
}

