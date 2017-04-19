/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package wstoejb;

import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;

/**
 * Simple interface to create a statefull session bean
 * 
 * @author Jerome Dochez
 */
public interface StatefulSessionBeanHome extends EJBHome {

	public StatefulSessionBean create () throws RemoteException, CreateException;

}