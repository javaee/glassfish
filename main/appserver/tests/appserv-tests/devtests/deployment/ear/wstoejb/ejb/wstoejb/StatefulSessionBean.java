/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package wstoejb;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

/**
 *  Simple interface for a statefull session bean.
 * 
 * @author Jerome Dochez
 */
public interface StatefulSessionBean extends EJBObject {

	/**
	 * perform some kind of processing
	 * @param payload is the request info
	 * @return the processed info
	 * @throws RemoteException
	 */
	public String payLoad(String payload) throws RemoteException;	
}