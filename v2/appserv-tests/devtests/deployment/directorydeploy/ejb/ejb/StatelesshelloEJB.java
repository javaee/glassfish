/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
