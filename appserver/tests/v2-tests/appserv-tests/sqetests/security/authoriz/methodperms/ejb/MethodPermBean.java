/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.security.authoriz.methodperms;

import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

public class MethodPermBean implements SessionBean {
    private String str;
    private SessionContext sc;
    
    public MethodPermBean(){}
    
    public void ejbCreate(String str) throws RemoteException {
        
	System.out.println("In ejbCreate !!");
        this.str = str;
        
    }
    
    public String authorizedMethod() throws RemoteException {
        
	System.out.println("MethodPerm Bean - inside authorizedMethod() !");
	System.out.println("CALLER PRINCIPAL: " + sc.getCallerPrincipal());
	boolean mgr = sc.isCallerInRole("MGR");
	boolean admin = sc.isCallerInRole("ADMIN");
	boolean staff =  sc.isCallerInRole("STAFF");
	boolean emp = sc.isCallerInRole("EMP");
	StringBuffer sbuf = new StringBuffer();
	sbuf.append("\nIs CallerInRole: MGR(shud be true) = " + mgr); 
	sbuf.append("\nIs CallerInRole: ADMIN(shud b false) = " + admin);
	sbuf.append("\nIs CallerInRole: STAFF(shud b true) = " + staff);
	sbuf.append("\nIs CallerInRole: EMP(shud b true) = " + emp);
        System.out.println (sbuf.toString()); 
	if(mgr &&  !admin && staff && emp)
	    return sbuf.toString();
	else
	    throw new RemoteException("Caller in role failed ");
        
    }
    
    public String authorizedMethod(String s, int i) throws RemoteException {
        
	System.out.println("MethodPerm Bean - inside authorizedMethod(String s, int i)!!");
	//System.out.println("Is CallerInRole: " + sc.isCallerInRole("MGR"));
	//System.out.println("Is CallerInRole: " + sc.isCallerInRole("STAFF"));
	//System.out.println("Is CallerInRole: " + sc.isCallerInRole("EMP"));
	//System.out.println("Is CallerInRole: " + sc.isCallerInRole("ADMIN"));
        return str + " " + s + " " + i;
        
    }
    
    public String authorizedMethod(int i) throws RemoteException {
        
	System.out.println("MethodPerm Bean - inside authorizedMethod(int i)!!!");
        return str + " " + i;
        
    }

    public void unauthorizedMethod() throws RemoteException {
        
	System.out.println("MethodPerm Bean - inside unauthorized method!!!!");
      
    }
    
    public String sayGoodbye() throws RemoteException {
        
	System.out.println("MethodPerm Bean - inside sayGoodbye()!!!!!");
        return str + " sayGoodbye";
        
    }
    
    public void setSessionContext(SessionContext sc) {
        
	this.sc = sc;

    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
