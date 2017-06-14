/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package xsdanyejb;

import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;
import javax.xml.rpc.handler.MessageContext;

public class XsdAnyEJB implements SessionBean {
    private SessionContext sc;

    public XsdAnyEJB(){}
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In XsdAnyEJB::ejbCreate !!");
    }

    public int test1(int a, 
                     xsdanyejb.SingleWildcardType c, 
                     xsdanyejb.RepeatedWildcardType d) 
        throws java.rmi.RemoteException 
    {
        MessageContext msgContext = sc.getMessageContext();
        System.out.println("msgContext = " + msgContext);

        System.out.println("XsdAnyEJB.test1() called with ");
        System.out.println("a = " + a);

        System.out.println("SingleWildcardType.foo = " + c.getFoo());
        System.out.println("SingleWildcardType.bar = " + c.getBar());
        //System.out.println("SingleWildcardType._any = " + c.get_any());
        System.out.println("SingleWildcardType._any = " + c.getVoo());

        System.out.println("RepeatedWildcardType.foo = " + d.getFoo());
        System.out.println("RepeatedWildcardType.bar = " + d.getBar());
        System.out.println("RepeatedWildcardType._any = " + d.get_any());

        System.out.println("GoogleEJB returning " + a);

        return a;
    }
        
    public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}

}
