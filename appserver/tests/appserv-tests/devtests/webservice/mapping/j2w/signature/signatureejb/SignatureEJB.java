/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
