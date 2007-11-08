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
package com.sun.enterprise.security;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import javax.naming.Context;
import com.sun.enterprise.util.Utility;
import com.sun.enterprise.util.ORBManager;
import com.sun.enterprise.Switch;
import com.sun.enterprise.security.auth.RemoteObject;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.NamingManager;
import com.sun.enterprise.naming.NamingManagerImpl;
import com.sun.enterprise.security.RealmConfig;
import com.sun.logging.*;


/**
 * This class has methods to startup and access the Realm and register
 * it within the current Name Service.
 * @see IRealmManager the remote interface to access the realm
 * @author Harpreet Singh
 */

public class RealmManager extends RemoteObject implements IRealmManager{

    private static final Logger _logger = 
        LogDomains.getLogger(LogDomains.SECURITY_LOGGER);

    // Name of the Realm Manager Service in the Name Service.
    public static final String REALM_MANAGER_NAME = "Realm_Manager"; 

    
    /**
     * Creates a new instance of the Realm Manager.
     * This call expects that an initialized ORB has been set in the
     * ORBManager
     * @see com.sun.enterprise.util.ORBManager
     */
    public RealmManager() {
	super();
    }

    
    /**
     * Initializes the Realm Manager Server & binds the name in the 
     * Name Service.
     * @exception A javax.naming.NamingException is thrown if unable 
     * to register the Authentication Server. 
     */
    public void init()
	throws javax.naming.NamingException, java.rmi.RemoteException {

            RealmConfig.createRealms();
    }

    
    /**
     * Starts the Authentication Server & waits forever for invocations.
     */
    public void start() throws java.lang.InterruptedException {
	// wait for object invocation
	Object sync = new Object();
	synchronized (sync) { 
	    sync.wait(); 
	}
    }

    
    public void refreshRealms(String realmName) 
	throws java.rmi.RemoteException{
	    try{
		Realm r = Realm.getInstance(realmName);
		r.refresh();
	    }catch (Exception e){
		throw new java.rmi.RemoteException(e.toString());
	    }
    }

    
    /**
     *
     * Starts up a standalone  Realm Manager. It uses the command
     * line arguments to initialize the ORB.
     *
     */
    public static void main(String[] args) {
  	try {
	    Utility.checkJVMVersion();
	    // Initialize the ORB if not already started.
	    ORBManager.getORB() ;

	    Switch theSwitch = Switch.getSwitch();
	    NamingManager nm = new NamingManagerImpl();
	    theSwitch.setNamingManager(nm);
	    
	    RealmManager mgr = new RealmManager();
	    mgr.init(); // Initialize the Authentication Service ...
	    mgr.start(); // Start the Server.
        } catch (Exception ex) {
 	    _logger.log(Level.SEVERE,
                        "java_security.realm_manager_exception",ex);
	}
    }

    
}



