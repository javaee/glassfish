/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package signatureejb;

import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;
import javax.xml.rpc.handler.MessageContext;
import java.util.Date;

public class SignatureEJB implements SessionBean {
    private SessionContext sc;

    private java.util.Date date;
    private MyDateValueType myDate;
    private MyDateValueType[] myDates;

    public SignatureEJB(){}
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In SignatureEJB::ejbCreate !!");
    }

    public void SetTestDate(java.util.Date testDate) {
	System.out.println("In SignatureEJB::setTestDate = " + testDate);
        date = testDate;
    }

    public java.util.Date GetTestDate() {
	System.out.println("In SignatureEJB::getTestDate !!");
        return date;
    }

    public void setMyDateValueType(MyDateValueType mytestdate) {
	System.out.println("In SignatureEJB::setMyDateValueType: date = " 
            + mytestdate.getDate() + " ; whine = " + mytestdate.getWhine());
        myDate = mytestdate;
    }

    public MyDateValueType getMyDateValueType() {
	System.out.println("In SignatureEJB::getMyDateValueType !!");
        return myDate;
    }

    public void setMyDateValueTypes(MyDateValueType[] mytestdates) {
	System.out.println("In SignatureEJB::setMyDateValueTypes: dates.size = " 
            + mytestdates.length);
        myDates = mytestdates;
    }

    public MyDateValueType[] getMyDateValueTypes() {
	System.out.println("In SignatureEJB::getMyDateValueTypes !!");
        return myDates;
    }

    public String SayHello(String msg) {
	System.out.println("In SignatureEJB::SayHello !!");
        return "Hello! " + msg;
    }
        
    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}
}
