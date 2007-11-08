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
package com.sun.enterprise.tools.deployment.main;

import java.rmi.RemoteException;
import java.io.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.net.*;

import org.omg.CORBA.ORB;
import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;
import javax.naming.Context;

import com.sun.enterprise.util.NotificationListener;
import com.sun.enterprise.util.NotificationEvent;
import com.sun.enterprise.util.ORBManager;
import com.sun.ejb.sqlgen.DBInfo;

import com.sun.enterprise.tools.deployment.backend.JarInstaller;
import com.sun.enterprise.tools.deployment.backend.DeploymentSession;
import com.sun.enterprise.tools.deployment.backend.DeploymentSessionImpl;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.resource.ConnectorInfo;
import com.sun.enterprise.resource.PoolingException;

//import com.sun.enterprise.tools.deployment.ui.UIUtils;

/** This is the class that handles connections to the J2EE server from 
**  client tools.
**  @author Danny Coward
*/

public class ServerManager 
{
    private static final String OMG_ORB_INIT_PORT_PROPERTY = 
	"org.omg.CORBA.ORBInitialPort"; // NOI18N
    private static final String OMG_ORB_INIT_HOST_PROPERTY = 
	"org.omg.CORBA.ORBInitialHost"; // NOI18N
    private static final String DEFAULT_ORB_INIT_HOST = "localhost"; // NOI18N
    private static final String DEFAULT_ORB_INIT_PORT = "1050"; // NOI18N
    
    /* -------------------------------------------------------------------------
    ** Localization 
    */

    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(ServerManager.class);

    /* -------------------------------------------------------------------------
    ** Notification types 
    */

    /* generic type */
    public static String NOTIFICATION_TYPE = "ServerManager"; // NOI18N
        
    /** server listener added/removed */
    public static String SERVER_LISTENER_ADDED = "serverListenerAdded"; // NOI18N
    public static String SERVER_LISTENER_REMOVED = "serverListenerRemoved"; // NOI18N

    /** server added/removed/selected */
    public static String SERVER_ADDED = "addServer"; // NOI18N
    public static String SERVER_REMOVED = "removeServer"; // NOI18N
    public static String SERVER_SELECTED = "setCurrentServer"; // NOI18N

    /** application delpoyed/undeployed */
    public static String APP_DEPLOYED   = "deployedApplication"; // NOI18N
    public static String APP_UNDEPLOYED = "undeployedApplication"; // NOI18N
    public static String SA_DEPLOYED   = "deployedApplication"; // NOI18N
    public static String SA_UNDEPLOYED  = "undeployedStandAlone"; // NOI18N

    /* -----
    */

    private static String LOCAL_SERVER = "local"; // NOI18N
    /** "localhost";*/ // NOI18N

    public static String SERVER_PROPERTY = "name"; // NOI18N

    public static String LOCAL_HOST = "localhost"; // NOI18N
    private static String SERVERS_FILENAME = "servers"; // NOI18N
    
    private File preferencesDirectory;
    private Vector listeners = new Vector();
    private Hashtable serverNameToListenerMap = new Hashtable();
    private String currentServer;
    private Context initialContext;
    
    /** Construct a new server manager which restores and saves its state 
    **  to the given
    *   directory.*/
    public ServerManager(File preferencesDirectory)
    {
	this.preferencesDirectory = preferencesDirectory;
    }

    /* -------------------------------------------------------------------------
    ** Undeploy App & Deployed notification 
    */

//    /** The undeploy method.*/
//    public void uninstall(String appName, String serverName) 
//        throws ServerException 
//    {
//        System.out.println("*** 'uninstall' is obsolete, please use 'undeployApplication' ***");
//	Thread.dumpStack();
//	this.undeployApplication(appName, serverName);
//    }
//
//    public void uninstall(Object appList[], String serverName)
//        throws ServerException 
//    {
//        System.out.println("*** 'uninstall' is obsolete, please use 'undeployApplication' ***");
//	Thread.dumpStack();
//	this.undeployApplication(appList, serverName);
//    }

    /** The undeploy method.*/
    public void undeployApplication(String applicationName, String serverName) 
        throws ServerException 
    {
	JarInstaller installer = this.getJarInstaller(serverName);
	try {
	    installer.undeployApplication(applicationName);
	    this.changed(APP_UNDEPLOYED, applicationName);
	} catch (Throwable t) { 
	    throw new ServerException(localStrings.getLocalString(
      "enterprise.tools.deployment.main.erroruninstallingapplicationfromserver",
		"Error uninstalling {0} from {1}", 
                new Object[] {applicationName, serverName}));
	}
	
    }

    public void undeployApplication(Object appList[], String serverName)
        throws ServerException 
    {
	for (int i = 0; i < appList.length; i++) {
	    if (appList[i] instanceof String) {
	        this.undeployApplication((String)appList[i], serverName);
	    } else {
		// XXX this should never occur
	    }
	}
    }

    /* called by DeploymentManager when an app has been successfully deployed */
    public void deployedApplication(Application app)
    {
	this.changed(APP_DEPLOYED, app.getName());
    }

    /* -------------------------------------------------------------------------
    ** Undeploy Connectors 
    */

    public void undeployConnector(String rarName, String serverName) 
        throws ServerException 
    {
	JarInstaller installer = this.getJarInstaller(serverName);
	try {
	    installer.undeployConnector(rarName);
	    this.changed(SA_UNDEPLOYED, rarName);
	} catch (Throwable t) { 
	    throw new ServerException(localStrings.getLocalString(
      "enterprise.tools.deployment.main.erroruninstallingapplicationfromserver",
		"Error uninstalling {0} from {1}", 
                new Object[] { rarName, serverName }));
	}
    }

    public void undeployConnector(Object rarList[], String serverName)
        throws ServerException 
    {
	for (int i = 0; i < rarList.length; i++) {
	    if (rarList[i] instanceof String) {
	        this.undeployConnector((String)rarList[i], serverName);
	    } else {
		// XXX this should never occur
	    }
	}
    }

    /* called by DeploymentManager when an app has been successfully deployed */
    public void deployedStandAlone(Descriptor desc)
    {
	this.changed(SA_DEPLOYED, desc.getName());
    }

    /* -------------------------------------------------------------------------
    ** add/remove server 
    */

    /** Connect to a new server by hostnamne*/
    public void addServer(String serverName) 
	throws ServerException 
    {
	JarInstaller jarInstaller = this.getJarInstaller(serverName);
	if (serverNameToListenerMap.containsKey(serverName)) {
	    // already present
	    this.setCurrentServer(serverName);
	    this.changed(SERVER_SELECTED, serverName);
	} else {
	    ServerListener serverListener = null;
	    try {
		serverListener = this.createServerListener(serverName);
		jarInstaller.addRemoteNotificationListener(serverListener);
	    } catch (Exception e) {
		System.out.println(localStrings.getLocalString(
                  "enterprise.tools.deployment.main.errorgettingserverlistener",
		  "Error getting server listener"));
	    }
	    serverNameToListenerMap.put(serverName, serverListener);
	    this.setCurrentServer(serverName);
	    this.changed(SERVER_ADDED, serverName);
	}
    }

    /** Disconnect from new server by hostname*/
    public void removeServer(String hostName) 
    {
	if (serverNameToListenerMap.containsKey(hostName)) {
	    ServerListener serverListener = 
                (ServerListener)serverNameToListenerMap.get(hostName);
	    try {
		serverNameToListenerMap.remove(hostName);
		if ((this.getCurrentServer() != null) && 
                    this.getCurrentServer().equals(hostName)) {
		    this.currentServer = null;
		}
		this.changed(SERVER_REMOVED, hostName);
		JarInstaller jarInstaller = this.getJarInstaller(hostName);
		jarInstaller.removeRemoteNotificationListener(serverListener);
	    } catch (Exception e) {
		// leave for debug
		//System.out.println("Error removing notification listener from server");
	    } 
	}
    }
    
    /** Return the name of the current server in this manager.*/
    public String getCurrentServer() 
    {
	if ((this.currentServer != null) && 
            serverNameToListenerMap.containsKey(this.currentServer)) {
	    return currentServer;
	}
	return null;
    }
    
    /** Set the name of the current server in this manager.*/
    public void setCurrentServer(String serverName) 
    {
	this.currentServer = serverName;
	String notificationString = ""; // NOI18N
	if (serverName != null) {
	    notificationString = serverName;
	}
	this.changed(SERVER_SELECTED, notificationString);
    }
    
    
    /** Returns a list of server names. */
    public Vector getServerNames() 
    {
	Vector v = new Vector();
	for (Enumeration e = this.serverNameToListenerMap.keys(); 
            e.hasMoreElements();) {
	    v.addElement(e.nextElement());
	}
	return v;
    }

    /* -------------------------------------------------------------------------
    ** add/remove notification listeners 
    */
    
    /** add a notificationlistsner for server changes. */
    public void addNotificationListener(NotificationListener nl) 
    {
	listeners.addElement(nl);
	this.changed(SERVER_LISTENER_ADDED, ""); // NOI18N
    }
    
    /** removes a notificationlistsner for server changes. */
    public void removeNotificationListener(NotificationListener nl) 
    {
	this.listeners.removeElement(nl);
	this.changed(SERVER_LISTENER_REMOVED, ""); // NOI18N
    }
    
    /** Force an update of listeners.*/
    protected void changed() 
    {
	Vector listenersClone = null;
	synchronized (listeners) {
	    listenersClone = (Vector)listeners.clone();
	}
	for (Enumeration e = listenersClone.elements(); e.hasMoreElements();) {
	    NotificationListener nl = (NotificationListener) e.nextElement();
	    nl.notification(new NotificationEvent(this, NOTIFICATION_TYPE));
	}
    }
    
    protected void changed(String type, String name) 
    {
	Vector listenersClone = null;
	synchronized (listeners) {
	    listenersClone = (Vector)listeners.clone();
	}
	NotificationEvent event = new NotificationEvent(this, type, 
            SERVER_PROPERTY, name);
	for (Enumeration e = listenersClone.elements(); e.hasMoreElements();) {
	    NotificationListener nl = (NotificationListener) e.nextElement();
	    nl.notification(event);
	}
	
    }

    /* -------------------------------------------------------------------------
    ** support fo application deployment 
    */

   /** 
    * Creates a Session object for listening to and managing deployment 
    * progress reports. 
    */
    public DeploymentSession createDeploymentSession(String serverName) 
        throws Exception 
    {
	try {
	    DeploymentSession ds = new DeploymentSessionImpl();
	    PortableRemoteObject.exportObject(ds);
	    Tie servantsTie = javax.rmi.CORBA.Util.getTie(ds);
	    servantsTie.orb(ORBManager.getORB());
	    return ds;
	} catch (Throwable t) {
	    throw new ServerException(localStrings.getLocalString(
		"enterprise.tools.deployment.main.couldnotgetorbforserver",
		"Couldn't get orb for ({0}) {1}", 
                new Object[] {"createDeploymentSession",serverName})); // NOI18N
	}
    }
    
    private ServerListener createServerListener(String serverName) 
        throws Exception 
    {
	try {
	    ServerListener listener = new ServerListener(this);
	    PortableRemoteObject.exportObject(listener);
	    Tie servantsTie = javax.rmi.CORBA.Util.getTie(listener);
	    servantsTie.orb(ORBManager.getORB());
	    return listener;
	} catch (Throwable t) {
	    throw new ServerException(localStrings.getLocalString(
		"enterprise.tools.deployment.main.couldnotgetorbforserver",
		"Couldn't get orb for ({0}) {1}", 
		new Object[] {"createCallBack", serverName})); // NOI18N
	}
    
    
    }
    
    public boolean isInstalled(String applicationName, String serverName) 
	throws ServerException 
    {
	JarInstaller installer = this.getJarInstaller(serverName);
	if (installer != null) {
	    try {
		Vector applicationNames = installer.getApplicationNames();
		for (int i = 0; i < applicationNames.size(); i++) {
		    if (applicationName.equals(applicationNames.elementAt(i))) {
			return true;
		    }
		}
	    } catch (Throwable t) {
		throw new ServerException(localStrings.getLocalString(
	   "enterprise.tools.deployment.main.couldnotapplicationlistfromserver",
		    "Couldn't get application list from {0}", 
		    new Object[] {serverName}));
	    }
	}
	return false;
    }
    
    /**
     * Return a vector of application names.
     */

     public Vector getApplicationNamesForServer(String serverName) 
         throws ServerException 
     {
	Vector v = null;
	if (serverName == null) {
	    return v;
	}
	JarInstaller installer = this.getJarInstaller(serverName);
	if (installer != null) {
	    try {
		v = installer.getApplicationNames();
	    } catch (RemoteException re) {
		throw new ServerException(localStrings.getLocalString(   
     "enterprise.tools.deployment.main.errorgettingappnamefromserverwithreason",
		    "Error obtaining application names from {0} \n reason {1}", 
                    new Object[] {serverName,re.getMessage()}));
	    }
	}
	return v;
     }

     public Vector getApplicationNames() 
         throws ServerException 
     {
        return this.getApplicationNamesForServer(getCurrentServer());
     }
    
    /**
     * Return a vector of connector names.
     */

    public Vector getConnectorNamesForServer(String serverName) 
	throws ServerException 
    {
	Vector v = null;
	if (serverName != null) {
	    JarInstaller installer = this.getJarInstaller(serverName);
	    if (installer != null) {
	        try {
		    v = new Vector();
	    	    ConnectorInfo ci = installer.listConnectors();
	    	    for (int i = 0; i < ci.connectors.length; i++) {
		    	v.add(ci.connectors[i].toString());
                    }
	    	} catch (Exception re) {
		    throw new ServerException(
			localStrings.getLocalString(   
     "enterprise.tools.deployment.main.errorgettingappnamefromserverwithreason",
		    "Error obtaining application names from {0} \n reason {1}", 
                    new Object[] { serverName, re.toString() }));
	    	}
	    }
	}
	return v;
    }

    public Vector getConnectorNames() 
	throws ServerException 
    {
        return this.getConnectorNamesForServer(getCurrentServer());
    }
    
    /**
     * Return a vector of connection-factory names.
     */

    public Vector getConnectionFactoriesForServer(String serverName) 
	throws ServerException 
    {
	Vector v = null;
	if (serverName != null) {
	    JarInstaller installer = this.getJarInstaller(serverName);
	    if (installer != null) {
	        try {
		    v = new Vector();
	    	    ConnectorInfo ci = installer.listConnectors();
	    	    for (int i = 0; i < ci.connectionFactories.length; i++) {
		    	v.add(ci.connectionFactories[i].toString());
                    }
	    	} catch (Exception re) {
		    throw new ServerException(localStrings.getLocalString(   
     "enterprise.tools.deployment.main.errorgettingappnamefromserverwithreason",
		    "Error obtaining application names from {0} \n reason {1}", 
                    new Object[] { serverName, re.toString() }));
	    	}
	    }
	}
	return v;
    }

    public Vector getConnectionFactories() 
         throws ServerException 
    {
        return this.getConnectionFactoriesForServer(getCurrentServer());
    }

    /**
     * Return a set of EnvironmentProperties that represents
     * the configuration properties of a connection factory
     * @param AppName Name of application (can be null if the adapter
     * is deployed standalone)
     * @param connectorName Name of resource adapter
     */
    public Set getConnectionFactoryPropertyTemplate(String appName,
                                                    String connectorName)
        throws RemoteException, PoolingException, ServerException {

        String serverName = getCurrentServer();
        JarInstaller installer = this.getJarInstaller(serverName);
        return installer.getConnectionFactoryPropertyTemplate
            (appName, connectorName);
    }



    public void addConnectionFactory(String appName,
                                     String connectorName,
                                     String jndiName,
                                     String xaRecoveryUser,
                                     String xaRecoveryPassword,
                                     Properties props) 
        throws RemoteException, PoolingException, ServerException {

        String serverName = getCurrentServer();
        JarInstaller installer = this.getJarInstaller(serverName);
        installer.addConnectionFactory(appName, connectorName,
                                       jndiName, xaRecoveryUser,
                                       xaRecoveryPassword, props);
    }

    public void removeConnectionFactory(String jndiName)
        throws RemoteException, PoolingException, ServerException {

        String serverName = getCurrentServer();
        JarInstaller installer = this.getJarInstaller(serverName);
        installer.removeConnectionFactory(jndiName);
    }

    public Set listConnectorResources() 
        throws RemoteException, ServerException {
        String serverName = getCurrentServer();
        JarInstaller installer = this.getJarInstaller(serverName);
        Set res = installer.listConnectorResources();
        return res;
    }

    /** 
     * Return a server by name. return null if there is no server of that 
     * name. 
     */
    public JarInstaller getServerForName(String serverName) 
	throws ServerException 
    {
	return this.getJarInstaller(serverName);
    }

    private JarInstaller getJarInstaller(String serverName) 
	throws ServerException 
    {
	try {
	    if ( serverName.equalsIgnoreCase("local") ) // NOI18N
		serverName = "localhost"; // NOI18N

            String initialPort = System.getProperty(OMG_ORB_INIT_PORT_PROPERTY);
            if (initialPort == null)
                initialPort = String.valueOf(ORBManager.getORBInitialPort());

            String corbaName = "corbaname:iiop:" + serverName + ":" + // NOI18N
                initialPort + "#" + JarInstaller.JNDI_NAME; // NOI18N

            /* IASRI 4691307 commented out by Anissa.
             * The following code throws exception from the ORBManager and since we don't need any server
             * connection for AT, we will not perform this step.
             *
            Object objref  = getIC().lookup(corbaName);
            Object o = PortableRemoteObject.narrow(objref, JarInstaller.class);
	    JarInstaller installer = (JarInstaller) o;
	    return installer;
            * end of IASRI 4691307
             */
            
	    throw new ServerException(""); //NOI18N 
            
	} catch (Throwable t) {
	    String msg = localStrings.getLocalString(
		"enterprise.tools.deployment.main.couldnotconnecttoserver",
                "Couldn''t connect to {0}", 
		new Object[] { serverName });
	    //System.err.println(msg);  // IASRI 4691307
	    //UIUtils.printException(msg, t);
	    throw new ServerException(msg);  
	}
    }
    
    private Context getIC()
    {
	if ( initialContext == null ) {
	    Hashtable env = new Hashtable();
	    env.put("java.naming.corba.orb", ORBManager.getORB()); // NOI18N
	    try {
		initialContext = new InitialContext(env);
	    } catch ( Exception ex ) {
		ex.printStackTrace();
	    }
	}
	return initialContext;
    }

    /** Return information about the database under the given server.*/
    public DBInfo getDBInfo(String serverName) 
	throws ServerException 
    {
	try {
	    if ( serverName.equalsIgnoreCase("local") ) // NOI18N
		serverName = "localhost"; // NOI18N

            String initialPort = System.getProperty(OMG_ORB_INIT_PORT_PROPERTY);
            if (initialPort == null)
                initialPort = String.valueOf(ORBManager.getORBInitialPort());
            
	    String corbaName = "corbaname:iiop:" + serverName + ":" + // NOI18N
                initialPort + "#" + DBInfo.JNDI_NAME; // NOI18N

            Object objref  = getIC().lookup(corbaName);
            Object o = PortableRemoteObject.narrow(objref, DBInfo.class);
	    DBInfo info = (DBInfo) o;
	    return info;
	} catch (Throwable t) {
	    throw new ServerException(localStrings.getLocalString(
		"enterprise.tools.deployment.main.couldnotgetdbinfofromserver",
		"Could not get db info from the J2EE server {0}", 
		new Object[] {serverName}));  
	}
    
    }
    
    /** Reconnect to all the servers I knew about in the last session.*/
    public Hashtable restoreFromUserHome() 
	throws IOException 
    {
	Hashtable badServerNamesToExceptions = new Hashtable();
	File serversFile = new File(preferencesDirectory, SERVERS_FILENAME);
	if (serversFile.exists()) {
	    FileInputStream fis = new FileInputStream(serversFile);
	    Properties servers = new Properties();
	    servers.load(fis);  
	    fis.close();
	    for (Enumeration e = servers.propertyNames(); 
		e.hasMoreElements();) {
		String serverName = (String) e.nextElement();
		try {
		    this.addServer(serverName);  
		} catch (Throwable ex) {
		    badServerNamesToExceptions.put(badServerNamesToExceptions, 
			ex);
		}
	    }
	}
	return badServerNamesToExceptions;
    }

    /** Save my current state to my directory.*/
    public void saveToUserHome() 
	throws IOException 
    {
	File serversFile = new File(preferencesDirectory, SERVERS_FILENAME);	
	FileOutputStream fos = new FileOutputStream(serversFile);
	Properties serversP = new Properties();
	for (Enumeration e = this.getServerNames().elements(); 
	    e.hasMoreElements();) {
	    String nextServer = (String) e.nextElement();
	    serversP.put(nextServer, nextServer);
	}
	serversP.store(fos, "J2EE Servers"); // NOI18N
        if( fos != null ) {
            fos.close();
        }
    }
    
    /** My pretty format. */
    private String printList() 
    {
	String s = "Server Manager "; // NOI18N
	for (Enumeration e = this.getServerNames().elements(); 
	    e.hasMoreElements();) {
	    s = s + "\n\t" + e.nextElement(); // NOI18N
	}
	return s;
    }

    /** My pretty format as a STring.*/
    public String toString() 
    {
	return "ServerManager"; // NOI18N
    }
    
}

