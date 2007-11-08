/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package statelesshello;


import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;


public interface StatelesshelloHome extends EJBHome {

    public Statelesshello create () throws RemoteException, CreateException;

}
