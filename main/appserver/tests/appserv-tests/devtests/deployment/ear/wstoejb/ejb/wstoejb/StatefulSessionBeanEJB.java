/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package wstoejb;

import javax.ejb.*;
import java.rmi.RemoteException;

/**
 *  Simple interface for a statefull session bean.
 * 
 * @author Jerome Dochez
 */
public class StatefulSessionBeanEJB implements  SessionBean {

	private SessionContext sc;
    
	public StatefulSessionBeanEJB(){}
    
	public void ejbCreate() throws RemoteException {
		System.out.println("In ejbCreate !!");
	}

	public void setSessionContext(SessionContext sc) {	
		this.sc = sc;
	}
    
	public void ejbRemove() throws RemoteException {}
    
	public void ejbActivate() {}
    
	public void ejbPassivate() {}	
	
	/**
	 * perform some kind of processing
	 * @param payload is the request info
	 * @return the processed info
	 * @throws RemoteException
	 */
	public String payLoad(String payload) throws RemoteException {
		return "Hey " + payload + ", I am such a dummy ejb, don't bother !";
	}			
}