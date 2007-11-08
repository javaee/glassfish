/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.enterprise.management.agent;

import java.util.Hashtable;
import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;
import javax.naming.Context;
import javax.naming.InitialContext;

import javax.management.j2ee.*;
import javax.management.*;

//import com.sun.enterprise.util.ORBManager; //TBD SRI

/** 
 * ListenerRegistry provides an implementation of ListenerRegistration
 * This implementation creates instances of RemoteListenerConnectors which
 * are registered on the MEJB on behalf of the local listener.
 *
 * @author Hans Hrasna
 */
public class ListenerRegistry implements ListenerRegistration {

    private Hashtable listenerConnectors = new Hashtable();
    private String OMG_ORB_INIT_PORT_PROPERTY = "org.omg.CORBA.ORBInitialPort";
    private String serverAddress; // the hostname or ip address of the server
    private Management server;
    private boolean debug = false;


    public ListenerRegistry(String ip) {
        serverAddress = ip;
    }

    /**
     * Add a listener to a registered managed object.
     *
     * @param name The name of the managed object on which the listener should be added.
     * @param listener The listener object which will handle the notifications emitted by the registered managed object.
     * @param filter The filter object. If filter is null, no filtering will be performed before handling notifications.
     * @param handback The context to be sent to the listener when a notification is emitted.
     *
     * @exception InstanceNotFoundException The managed object name provided does not match any of the registered managed objects.
     *
     */
    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
        throws  RemoteException {
        String proxyAddress = EventListenerProxy.getEventListenerProxy().getProxyAddress();
        try {
            if(debug)System.out.println("ListenerRegistry:addNotificationListener(" + listener + ") to " + name);
            RemoteListenerConnector connector = new RemoteListenerConnector(proxyAddress);
            getMEJBUtility().addNotificationListener(name, connector, filter, connector.getId());
            EventListenerProxy.getEventListenerProxy().addListener(connector.getId(), listener, handback);
        	listenerConnectors.put(listener, connector);
        } catch (javax.management.InstanceNotFoundException inf) {
            throw new java.rmi.RemoteException(inf.getMessage(), inf);
        }
    }


    /**
     * Remove a listener from a registered managed object.
     *
     * @param name The name of the managed object on which the listener should be removed.
     * @param listener The listener object which will handle the notifications emitted by the registered managed object.
     * This method will remove all the information related to this listener.
     *
     * @exception InstanceNotFoundException The managed object name provided does not match any of the registered managed objects.
     * @exception ListenerNotFoundException The listener is not registered in the managed object.
     */
    public void removeNotificationListener(ObjectName name, NotificationListener listener)
        throws   RemoteException {
        EventListenerProxy proxy = EventListenerProxy.getEventListenerProxy();
        try {
            if(debug) {
                System.out.println("removeNotificationListener: " + listener);
            	System.out.println("listenerProxy = " + listenerConnectors.get(((RemoteListenerConnector)listener).getId()));
            }
            RemoteListenerConnector connector = ((RemoteListenerConnector)listenerConnectors.get(listener));
        	getMEJBUtility().removeNotificationListener(name, connector);
            proxy.removeListener(connector.getId());
            listenerConnectors.remove(listener);
        } catch (javax.management.InstanceNotFoundException inf) {
            throw new java.rmi.RemoteException(inf.getMessage(), inf);
        } catch (javax.management.ListenerNotFoundException lnf) {
            throw new java.rmi.RemoteException(lnf.getMessage(), lnf);
        }
    }

    MEJBUtility getMEJBUtility(){                                                               
        return MEJBUtility.getMEJBUtility();
    }
    Management getMEJB() throws RemoteException {
        if(server == null) {
            try {
                Context ic = new InitialContext();
                String ejbName = System.getProperty("mejb.name","ejb/mgmt/MEJB");
                java.lang.Object objref = ic.lookup(ejbName);
                ManagementHome home = (ManagementHome)PortableRemoteObject.narrow(objref, ManagementHome.class);
                server = (Management)home.create();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        /*if (server == null) {
            try {
            	Context ic = new InitialContext();
        		String mejbJNDIName = System.getProperty("mejb.name","ejb/mgmt/MEJB");
                String initialPort = System.getProperty(OMG_ORB_INIT_PORT_PROPERTY);
        		if (initialPort == null)
            		initialPort = String.valueOf(ORBManager.getORBInitialPort());
       			String corbaName = "corbaname:iiop:" + serverAddress + ":" + initialPort + "#" + mejbJNDIName;
        		java.lang.Object objref = ic.lookup(corbaName);
        		ManagementHome home = (ManagementHome)PortableRemoteObject.narrow(objref, ManagementHome.class);
        		server = (MEJB)home.create();
                if (debug) System.out.println("ListenerRegistry connected to: " + corbaName);
        	} catch (RemoteException re) {
            	throw re;
        	} catch (Exception e) {
            	throw new RemoteException(e.getMessage(), e);
        	}
        }*/
        return server;
    }
}
