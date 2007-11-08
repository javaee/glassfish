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
package com.sun.enterprise.naming;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import javax.naming.*;
import javax.naming.spi.InitialContextFactory;
import com.sun.enterprise.util.ORBManager;
import com.sun.appserv.naming.S1ASCtxFactory;

/**
 * Implements the JNDI SPI InitialContextFactory interface used to create
 * the InitialContext objects. It creates an instance of the serial context.
 */

public class SerialInitContextFactory implements InitialContextFactory {


    private Hashtable defaultEnv;

    private S1ASCtxFactory s1asCtxFactory;

    
    
    private static boolean _initializeOrbManager = true;

    static {
        // setting ORB class and ORB singleton to SE values
        //setting RMI-IIOP delegates to EE values
        com.sun.enterprise.util.ORBManager.setORBSystemProperties();
    }

    /**
     * Set to false if you do not want the OrbManager to be initialized. 
     * This is necessary in the NodeAgent since OrbManager initialization causes
     * the NSS certificate database to be opended (and loaded) in which case
     * it cannot be synchronized on Windows platforms (file in use).
     */
    public static void setInitializeOrbManager(boolean init) {
        _initializeOrbManager = init;
    }
    
    /** 
     * Default constructor. Creates an ORB if one is not already created.
     */
    public SerialInitContextFactory() {
        String LBPOLICY = 
	  System.getProperty(S1ASCtxFactory.LOAD_BALANCING_PROPERTY);
      
	String ENDPOINTS = 
	  System.getProperty(S1ASCtxFactory.IIOP_ENDPOINTS_PROPERTY);
	if ((LBPOLICY != null &&
	     !LBPOLICY.equals("")) ||
	    (ENDPOINTS != null &&
	     !ENDPOINTS.equals(""))) {
	    s1asCtxFactory = new S1ASCtxFactory();
	} else {
	    // create a default env
	    defaultEnv = new Hashtable();

	    // Client side : removing the initialization of the ORB 
	    // from the constructor since
	    // we need to propagate the host:port values that are 
	    // set in the App client to ORB.init()	    
	    /*    if (_initializeOrbManager) {
	        ORBManager.init(null, null);
		defaultEnv.put("java.naming.corba.orb", ORBManager.getORB());
		}*/
	}
    }

    // Removing this constructor since it never gets called in the 
    // in the JNDI code path
    // a call to new InitialContext(env) creates an instance of 
    // this class via the SerialInitContextFactory.class.newInstance()
    // which will call the nullary constructor

    /*  public SerialInitContextFactory(Hashtable env) {

	if ((LBPOLICY != null &&
	     !LBPOLICY.equals("")) ||
	    (ENDPOINTS != null &&
	     !ENDPOINTS.equals("")) ||
	    env.get(S1ASCtxFactory.LOAD_BALANCING_PROPERTY) != null ||
	    env.get(S1ASCtxFactory.IIOP_ENDPOINTS_PROPERTY) != null) {
	    s1asCtxFactory = new S1ASCtxFactory(env);
	} else {
	    defaultEnv = env;
	    if (_initializeOrbManager) {
	        ORBManager.init(null, (Properties)env);
		defaultEnv.put("java.naming.corba.orb", ORBManager.getORB());
		System.out.println("SHEETAL : port# " + ORBManager.getORBInitialPort());
	    }
	}
    }
*/
    /**
     * Create the InitialContext object.
     */
    public Context getInitialContext(Hashtable env) throws NamingException {
        if (env != null) {
	    if (env.get(S1ASCtxFactory.IIOP_ENDPOINTS_PROPERTY) != null) {
	        System.setProperty(S1ASCtxFactory.IIOP_ENDPOINTS_PROPERTY, 
				   (String)env.get(S1ASCtxFactory.IIOP_ENDPOINTS_PROPERTY));
	    } 
	    if (env.get(S1ASCtxFactory.LOAD_BALANCING_PROPERTY) != null) {
	        System.setProperty(S1ASCtxFactory.LOAD_BALANCING_PROPERTY,
				   (String)env.get(S1ASCtxFactory.LOAD_BALANCING_PROPERTY));
	    }

	    String LBPOLICY = 
	      System.getProperty(S1ASCtxFactory.LOAD_BALANCING_PROPERTY);
	    
	    String ENDPOINTS = 
	      System.getProperty(S1ASCtxFactory.IIOP_ENDPOINTS_PROPERTY);
    
	    if ((LBPOLICY != null &&
		 !LBPOLICY.equals("")) ||
		(ENDPOINTS != null &&
		 !ENDPOINTS.equals(""))) {
	      s1asCtxFactory = new S1ASCtxFactory();
	        return (s1asCtxFactory.getInitialContext(env));
	    }
	}
	
	if (_initializeOrbManager && 
	    (env == null || 
	     (env != null && env.get("java.naming.corba.orb") == null))) {

	    //javax.naming.InitialContext passes its own local Hashtable 
	    //variable, so can't cast it to Properties.
	  
	    Properties props = new Properties();
	    if (env != null) {
	        if (env.get("org.omg.CORBA.ORBInitialHost") != null &&
		    env.get("org.omg.CORBA.ORBInitialPort") != null) {
		    props.put("org.omg.CORBA.ORBInitialHost", 
			      (String) env.get("org.omg.CORBA.ORBInitialHost"));
		    props.put("org.omg.CORBA.ORBInitialPort", 
			      (String) env.get("org.omg.CORBA.ORBInitialPort"));
		}			
	    }
	    org.omg.CORBA.ORB orb = ORBManager.getORB(props) ;
	    /*    if (defaultEnv != null)
		  defaultEnv.put("java.naming.corba.orb", orb ) ; 
		  if (env != null)
		  env.put("java.naming.corba.orb", orb );
		  */
	}
	
	
	if (SerialContext.getSticky() != null) {
	    Context ctx = SerialContext.getStickyContext();	
	    return ctx;
	}

	if (s1asCtxFactory != null) {
	    return (s1asCtxFactory.getInitialContext(env));
	}
        
	if (env != null) {	    	    
            return new SerialContext(env);
        } else {
            return new SerialContext(defaultEnv);
        }
    }
}
