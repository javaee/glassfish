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

package com.sun.appserv.naming;

import javax.naming.spi.InitialContextFactory;
import javax.naming.*;

import java.util.Hashtable;
import java.util.Properties;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.impl.orbutil.ORBConstants;
import com.sun.corba.ee.spi.folb.GroupInfoService;

import com.sun.jndi.cosnaming.CNCtxFactory;

import com.sun.enterprise.naming.SerialContext;
import com.sun.enterprise.util.ORBManager;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 * Implements the JNDI SPI InitialContextFactory interface used to
 * create the InitialContext objects.
 *
 * The static block creates the list of endpoints from the system property
 * com.sun.appserv.iiop.endpoints.
 * The list is randomised the very first time S1ASCtxFactory is initialized.
 * When a call for a new InitialContext comes, the pointer in the list is moved 
 * one element ahead. Thus for loadbalancing purposes, there is always a different 
 * list created.
 * failover is taken care of by the ORB infrastructure.
 *
 * @author Dhiru Pandey & Vijay Raghavan
 * @author Sheetal Vartak
 */

public class S1ASCtxFactory extends CNCtxFactory {    

    protected static final Logger _logger = LogDomains.getLogger(
					   LogDomains.JNDI_LOGGER);    

    private static final RoundRobinPolicy rrPolicy;

    private static final String IIOP_URL = "iiop:1.2@";

    private static final String CORBALOC = "corbaloc:";

    public static final String LOAD_BALANCING_PROPERTY = 
            "com.sun.appserv.iiop.loadbalancingpolicy";

    public static final String IIOP_ENDPOINTS_PROPERTY = 
            "com.sun.appserv.iiop.endpoints";

    private final Hashtable defaultEnv;

    private static GroupInfoService gis;

    public static final String IC_BASED_WEIGHTED = "ic-based-weighted";

    public static final String IC_BASED = "ic-based";

    private static boolean firstTime = true;

    private static GroupInfoServiceObserverImpl giso;

    static {    
        String [] list = null;
        String [] commaDelimitedValues = null;
        String policy = null;
          
        // Get the load balancing policy
        String propertyValue = System.getProperty(
                              LOAD_BALANCING_PROPERTY);

        if (propertyValue != null) {
            commaDelimitedValues = propertyValue.split(",");
            
            
            if (commaDelimitedValues != null) {
            if (commaDelimitedValues[0].trim().equals(IC_BASED) ||
                commaDelimitedValues[0].trim().equals(IC_BASED_WEIGHTED)) {
                policy = commaDelimitedValues[0];
            }
            }
            if(policy != null) {
            System.setProperty(LOAD_BALANCING_PROPERTY, policy);
            }
        }
        
        propertyValue = System.getProperty(
                           IIOP_ENDPOINTS_PROPERTY);
        
        if(propertyValue==null||propertyValue.length()==0){
          //do not use the defaults here as then we are not giving a 
          //chance to the <policy>,host:port(,host:port)* type of policy
          //specification
            list = null;
        } else {
            list = propertyValue.split(",");	    
        }
                
        //if the endpoints property was not specified, give a
        //chance to the <policy>,host:port(,host:port)* type of policy
        //specification

        String host;
        String port;

        if(list == null ) {
            if (commaDelimitedValues != null && 
            commaDelimitedValues.length > 1) {
                
                list = new String[commaDelimitedValues.length-1];
            for (int i=0; i<list.length; i++) {
                list[i] = commaDelimitedValues[i+1];
            }		
            } else if((host = System.getProperty(ORBManager.OMG_ORB_INIT_HOST_PROPERTY)) != null &&
                  (port = System.getProperty(ORBManager.OMG_ORB_INIT_PORT_PROPERTY)) != null) {
                list = new String[1];
            list[0] = host + ":" + port;
              
            }
        }
        
        rrPolicy = new RoundRobinPolicy(list);

	if (list != null && list.length > 0) {
	    getGIS();
	}

    }

    public S1ASCtxFactory() {
        defaultEnv = new Hashtable();
    }

    public S1ASCtxFactory(Hashtable env) {
        defaultEnv = env;
    }

    public static RoundRobinPolicy getRRPolicy() {
	return rrPolicy;
    }

    public synchronized Context getInitialContext(Hashtable env) throws NamingException {

        Object [] list;

	if (SerialContext.getSticky() != null) {
	    Context ctx = SerialContext.getStickyContext();	
	    return ctx;
	}
	
	// firstTime is a boolean introduced to check if this is the first call to 
	// new InitialContext(). This is important especially on the server side
	if (firstTime == true) {

	    String policy = null;
	    
	    if (env == null) {
	      env = defaultEnv;
	    }
	    
	    //user can specify the load balancing policy and endpoints
	    // via env. Hence the logic below. 
	    
	    String propertyValue = (String) env.get(LOAD_BALANCING_PROPERTY);
	    String[] commaDelimitedValues = null;
	    String host = null;
	    String port = null;
	    
	    if (propertyValue != null) {
	      commaDelimitedValues = propertyValue.split(",");	
	      
	      if (commaDelimitedValues != null) {
		if (commaDelimitedValues[0].trim().equals(IC_BASED) ||
		    commaDelimitedValues[0].trim().equals(IC_BASED_WEIGHTED)) {
		  policy = commaDelimitedValues[0];
		}
	      }
	      if (policy != null) {
		System.setProperty(LOAD_BALANCING_PROPERTY, policy);
	      }
	    }
	    
	    propertyValue = (String) env.get(IIOP_ENDPOINTS_PROPERTY);
	    
	    String [] temp_list = (propertyValue == null || 
				   propertyValue.length() == 0)
	      ? null 
	      : propertyValue.split(",");
	    if(temp_list == null || temp_list.length == 0) {
	      if (commaDelimitedValues != null) {
	        temp_list = new String[commaDelimitedValues.length - 1];
		
		for (int i=0; i<temp_list.length; i++) {
		  temp_list[i] = commaDelimitedValues[i+1];
		}
	      }	    	
	    }
	    //if endpoints property is not set by commandline or via env
	    // check for JNDI provider url
	    // else use ORB host:port value
	    if ((System.getProperty(IIOP_ENDPOINTS_PROPERTY) == null) &&
		(temp_list == null || 
		 temp_list.length == 0)) {
	      if (env.get(ORBManager.JNDI_PROVIDER_URL_PROPERTY) != null) {
		temp_list = rrPolicy.getEndpointForProviderURL(
							       (String)env.get(ORBManager.JNDI_PROVIDER_URL_PROPERTY));
	      }
	      if (temp_list == null || temp_list.length == 0) {
		if (env.get(ORBManager.OMG_ORB_INIT_HOST_PROPERTY) != null &&
		    env.get(ORBManager.OMG_ORB_INIT_PORT_PROPERTY) != null) {
		  host = (String)env.get(
					 ORBManager.OMG_ORB_INIT_HOST_PROPERTY);
		  port = (String)env.get(
					 ORBManager.OMG_ORB_INIT_PORT_PROPERTY);
		} else {
		  host = System.getProperty(
					    ORBManager.OMG_ORB_INIT_HOST_PROPERTY);
		  port = System.getProperty(
					    ORBManager.OMG_ORB_INIT_PORT_PROPERTY);
		}
		if (host != null &&
		    port != null) {
		  temp_list = rrPolicy.getAddressPortList(host, port);	 
		  _logger.log(Level.WARNING, "no.endpoints.selected", 
			      new Object[] {host, port});
		} else {	  
		  _logger.log(Level.SEVERE, "no.endpoints");
		  throw new RuntimeException("Cannot Proceed. No Endpoints specified.");
		}
	      }	    
	    }

	    if (giso == null) {
  	        if (temp_list != null && temp_list.length > 0) {		   
		    getGIS();
		} else {
		   _logger.warning("Cannot obtain GroupInfoServiceObserverImpl");
		}
	    }
	    
	    //add the list after randomising it to the circular list in rrPolicy
	    if (temp_list != null && temp_list.length > 0) {
	      rrPolicy.setClusterInstanceInfo(temp_list);
	    } 
	    
	    firstTime = false;
	    if (giso != null) {
	        //need to get the list of cluster instances the very first time a context is created
	        giso.membershipChange();
	    } else {
	        _logger.warning("Cannot obtain GroupInfoServiceObserverImpl");
	    }

	} 
	

	//get next version of the randomized list using round robin algo
	list = rrPolicy.getNextRotation();
	
	if (_logger.isLoggable(Level.FINE)) {
	    rrPolicy.print();
	}
	
	String corbalocURL = getCorbalocURL(list);

	env.put("com.sun.appserv.ee.iiop.endpointslist", 
		CORBALOC + corbalocURL);	
	env.put(ORBManager.JNDI_CORBA_ORB_PROPERTY, ORBManager.getORB());
	
	return new SerialContext(env);
    }  

 
    public static String getCorbalocURL(Object[] list) {

	String corbalocURL = "";
	//convert list into corbaloc url
	for (int i = 0; i < list.length;i++) {
	    _logger.info("list[i] ==> " + list[i]);
	    if (corbalocURL.equals("")) {
	        corbalocURL = IIOP_URL + ((String)list[i]).trim();
	    } else {
	        corbalocURL = corbalocURL + "," +
		    IIOP_URL + ((String)list[i]).trim();
	    }
	}	
	_logger.info("corbaloc url ==> " + corbalocURL);
	return corbalocURL;
    }

    private static void getGIS() {
        try {
	    //fix for bug 6527987
	    // passing the first endpoint for ORBManager.getORB() to connect to
	  
	    //need to make NameService HA...what if first endpoint is down.
	    //need to address this issue.
	  
	  /*  Properties props = new Properties();
	      String hostPort = endpoint;
	      // for IPv6 support, using lastIndex of ":"
	      int lastIndex = hostPort.lastIndexOf(':');
	      
	      _logger.fine("hostPort = " + hostPort + " lastIndex = " + lastIndex);
	      _logger.fine("hostPort.substring(0, lastIndex) = " + hostPort.substring(0, lastIndex));
	      _logger.fine("hostPort.substring(lastIndex + 1) = " + hostPort.substring(lastIndex + 1));
	      
	      props.put("org.omg.CORBA.ORBInitialHost", hostPort.substring(0, lastIndex));
	      props.put("org.omg.CORBA.ORBInitialPort", hostPort.substring(lastIndex + 1));
	      */	

	    gis = (GroupInfoService)
	      ((ORBManager.getORB()).resolve_initial_references(
				     ORBConstants.FOLB_CLIENT_GROUP_INFO_SERVICE));
	    giso = new GroupInfoServiceObserverImpl(gis);
	    gis.addObserver(giso);
	    
	    rrPolicy.print();
	} catch (org.omg.CORBA.ORBPackage.InvalidName in) {
	    _logger.fine("GroupInfoService not available. This is PE");
	} 
    } 
}
