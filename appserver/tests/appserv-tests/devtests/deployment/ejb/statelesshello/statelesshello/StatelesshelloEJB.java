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

package statelesshello;

import java.io.Serializable;
import java.io.InputStream;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;

public class StatelesshelloEJB implements SessionBean {
    private SessionContext sc;
    
    public StatelesshelloEJB(){}
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In ejbCreate !!");
    }

    public boolean isStatelesshello() throws EJBException {
        return true;
    }

    public String sayStatelesshello() throws EJBException {


        try {
            System.err.println("URL: "+getClass().getResource("/statelesshello/"));
            System.err.println("URL: "+getClass().getResource("/statelesshello/StatelesshelloEJB.class"));
            InputStream is = getClass().getResourceAsStream("/statelesshello/StatelesshelloEJB.class");
            System.err.println("Stream: " + is);
            is.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }



	Double d = null;
	String name = null;
	StringBuffer buffer = new StringBuffer("Statelesshello EJB - checked environment properties for user ");
	System.out.println("StatelesshelloEJB is saying hello to user\n");
	System.out.println("Now going forward with reading the environment properties\n");

	try {
	    InitialContext ic = new InitialContext();
	    name = (String) ic.lookup("java:comp/env/user");
	    buffer.append(name);
	    buffer.append("\nand for number which is = ");
	    d = (Double) ic.lookup("java:comp/env/number");
	    buffer.append(d);
	    buffer.append("\n");
	} catch(NamingException e) {
            e.printStackTrace();
            throw new EJBException(e.getMessage());
	}
	if(d == null) {
	    throw new EJBException("Wrong value for environment property");
	}
	System.out.println(buffer);
        return new String(buffer);
    }
    
    public String getUserDefinedException() throws RemoteException, StatelesshelloException {
        
        System.out.println("Throwing a User Defined Exception");
        throw new StatelesshelloException("test exception");
        
    }
    
    
    public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}
}
