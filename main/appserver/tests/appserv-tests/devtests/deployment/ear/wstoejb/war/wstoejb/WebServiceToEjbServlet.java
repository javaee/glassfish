/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package wstoejb;
 
import java.rmi.*;
import javax.rmi.*;
import javax.naming.*;

 
 /**
  *  Simple servlet implementation of the SEI
  * 
  * @author Jerome Dochez
  */
 public class WebServiceToEjbServlet implements WebServiceToEjbSEI {
 	
 	/**
 	 *  Implementation of the SEI's methods
 	 */
 	public String payload(String requestInfo) throws RemoteException {
 		try {
 			if (bean==null) {
	 			bean = createEJB();
 			}
 			return(getMsg(requestInfo) + "; and dont bother this dummy servlet also");
 		} catch(Exception e) {
 			throw new RemoteException(e.getMessage());
 		} 		
 	}

	/**
	 * Creates the ejb object from it's home interface
	 */ 	
	private StatefulSessionBean createEJB() throws Exception {
		// connect to the EJB
		Context ctxt = new InitialContext();
		java.lang.Object objref = ctxt.lookup("java:comp/env/MyEjbReference");
		StatefulSessionBeanHome homeIntf = (StatefulSessionBeanHome) PortableRemoteObject.narrow(objref, StatefulSessionBeanHome.class);
		return homeIntf.create();
	}
          
	public String getMsg(String info) throws Exception {
		if (bean == null) {
			return "could not talk to the EJB : java:comp/env/MyEjbReference";
		} else {
			return bean.payLoad(info);                
		}
	}
 	
 	private StatefulSessionBean bean;
 }
