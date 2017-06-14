/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package soapfaultsejb;

import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;
import javax.xml.rpc.handler.MessageContext;

public class SoapFaultsEJB implements SessionBean {
    private SessionContext sc;

    public SoapFaultsEJB(){}
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In SoapFaultsEJB::ejbCreate !!");
    }

    public Test1ResponseType test1(String a, String b,
                     Test2RequestType c)
        throws FaultOne, FaultThree, FaultTwo, java.rmi.RemoteException 
    {
        MessageContext msgContext = sc.getMessageContext();
        System.out.println("msgContext = " + msgContext);

        System.out.println("SoapFaultsEJB.test1() called with ");
        System.out.println("a = " + a);
        System.out.println("b = " + b);

        System.out.println("Test2RequestType.a = " + c.getTest2RequestParamA());
        System.out.println("Test2RequestType.b = " + c.getTest2RequestParamB());

        if ("1".equals(a)) {
            System.out.println("SoapFaultsEJB... throwing FaultOne Exception");
            throw new FaultOne("1", "I need a life.");
        }

        if ("2".equals(a)) {
            System.out.println("SoapFaultsEJB... throwing FaultTwo Exception");
            throw new FaultTwo("2", "I am so tired");
        }

        if ("3".equals(a)) {
            System.out.println("SoapFaultsEJB... throwing FaultThree Exception");
            throw new FaultThree("3", "I love fortune cookies");
        }

        Test1ResponseType t = new Test1ResponseType(1,2);
        return t;
    }
        
    public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}

}
