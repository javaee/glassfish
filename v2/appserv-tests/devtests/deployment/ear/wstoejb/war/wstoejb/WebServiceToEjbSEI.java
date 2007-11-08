/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
 package wstoejb;
 
import java.rmi.Remote;
import java.rmi.RemoteException;
 
 /*
  * This interface  is the service endpoint interface for a simple webservices 
  * interfacing a simple EJB
  * 
  * @author Jerome Dochez
  */
 public interface WebServiceToEjbSEI extends Remote {
 	
 	/*
 	 * Invoke the ejb
 	 */ 	
 	 public String payload(String requestInfo) throws RemoteException;
 	 
 }