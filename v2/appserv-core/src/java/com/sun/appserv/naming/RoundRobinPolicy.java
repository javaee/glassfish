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

import java.util.logging.Level;
import com.sun.logging.LogDomains;
import java.util.logging.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.net.InetAddress;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

import com.sun.jndi.cosnaming.IiopUrl;
import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.SocketInfo;
import com.sun.enterprise.util.ORBManager;

/**
 * The list of endpoints are randomized the very first time.
 *  This happens only once( when called from the static block
 * of S1ASCtxFactory class).
 * Simple RoundRobin is a special case of Weighted Round Robin where the
 * weight per endpoint is equal.With the dynamic reconfiguration 
 * implementation, the endpoints list willhave the following structure:
 * - server_identifier (a stringified name for the machine)
 * - weight- list of SocketInfo {type (type = CLEAR_TEXT or SSL) + 
 *         IP address + port }
 * The above structure supports multi-homed machines 
 * i.e. one machinehosting multiple IP addresses.
 * The RoundRobinPolicy class can be the class that is also implementing
 * the Listener interface for listening to events generated whenever there
 * is a change in the cluster shape. The listener/event design is still
 * under construction.This list of endpoints will have to be created during 
 * bootstrapping(i.e. when the client first starts up.) This list will comprise
 * of theendpoints specified by the user in "com.sun.appserv.iiop.endpoints"
 * property. We can assume a default weight for these endpoints (e.g 10).
 * This list will be used to make the first lookup call. During the first 
 * lookup call, the actual list of endpoints will beprovided back. 
 * Then on, whenever there is any change in the clustershape, 
 * the listener will get the updated list of endpoints from theserver.
 * The implementation for choosing the endpoint from the list of endpoints
 * is as follows:Let's assume 4 endpoints:A(wt=10), B(wt=30), C(wt=40), 
 * D(wt=20). 
 * Using the Random API, generate a random number between 1 and10+30+40+20.
 * Let's assume that the above list is randomized. Based on the weights, we
 * have intervals as follows:
 * 1-----10 (A's weight)
 * 11----40 (A's weight + B's weight)
 * 41----80 (A's weight + B's weight + C's weight)
 * 81----100(A's weight + B's weight + C's weight + C's weight)
 * Here's the psuedo code for deciding where to send the request:
 * if (random_number between 1 & 10) {send request to A;}
 * else if (random_number between 11 & 40) {send request to B;}
 * else if (random_number between 41 & 80) {send request to C;}
 * else if (random_number between 81 & 100) {send request to D;}
 * For simple Round Robin, we can assume the same weight for all endpointsand 
 * perform the above.
 * @author Sheetal Vartak
 * @date 8/2/05
 **/

public class RoundRobinPolicy {

     private List<ClusterInstanceInfo> endpointsList = 
	 new LinkedList<ClusterInstanceInfo>();

    private static Logger _logger = LogDomains.getLogger(
				    LogDomains.JNDI_LOGGER);

    private static java.util.Random rand = new java.util.Random();

    private int sumOfAllWeights = 0;

    private static final int default_weight = 10;

    //called during bootstrapping
    public RoundRobinPolicy(String[] list) {        
	setClusterInstanceInfo(list);
    }

    //will be called after dynamic reconfig
    public synchronized void setClusterInstanceInfo(List<ClusterInstanceInfo> list) {
	sumOfAllWeights = 0;
	endpointsList = list;

	String policy = System.getProperty(S1ASCtxFactory.LOAD_BALANCING_PROPERTY);
	boolean isWeighted;
	if (policy == null) {
	    //default is ic-based
	    policy = S1ASCtxFactory.IC_BASED;
	}
	if (policy.equals(S1ASCtxFactory.IC_BASED_WEIGHTED)) {
	    isWeighted = true;
	} else if (policy.equals(S1ASCtxFactory.IC_BASED)) {
	    isWeighted = false;
	} else {
	    isWeighted = false;
	    _logger.warning("loadbalancing.policy.incorrect");
	}	
	_logger.fine("isWeighted = " + isWeighted);
	//make the weights of all endpoints = default_weight
	for (ClusterInstanceInfo endpoint : endpointsList) {
	    if (! isWeighted) {
		endpoint.weight = default_weight;
	    }
	    _logger.info("endpoint.weight after checking isWeight = " + endpoint.weight);
	    sumOfAllWeights += endpoint.weight;
	} 
	_logger.info("sumOfAllWeights = " + sumOfAllWeights);
    }
    
    /**
     * add a string array of endpoints to list
     */
    public synchronized void setClusterInstanceInfo(String[] list) {
        String[] newList = null;
	
	//if no endpoints are specified as a system property,
        //then look for JNDI provider url
        //we are not looking for the host:port sys property
        //because ORBManager assumes default values of localhost 
	//and 3700
        //localhost is an issue since it gives back both IPv4 and
	// IPv6 addresses
        //3700 is a problem since it is specifically used as a DAS port. 
        //If resources are deployed to a cluster, they are not available
	// to DAS. 
        //So if the DAS host:port is used, 
        //then it will definitely result in a NameNotFoundException
	if (list != null && list.length > 0) {
	    newList = getAddressPortList(list);
	} else {
	    newList = getEndpointForProviderURL(
	  System.getProperty(ORBManager.JNDI_PROVIDER_URL_PROPERTY));
	}
	//randomize the list before adding it to linked list
	if (newList != null && newList.length > 0) {
	    String[] new_list = randomize(newList);
	    List<ClusterInstanceInfo> targetServerList = 
		new LinkedList <ClusterInstanceInfo> ();
	    for (int i = 0; i < new_list.length; i++) {
		if (notDuplicate(new_list[i])) {
		    targetServerList.add(
					 makeClusterInstanceInfo(
								 new_list[i], 
								 default_weight));
		}
	    }
	    if (!targetServerList.isEmpty()) {
		targetServerList.addAll(endpointsList);
		setClusterInstanceInfo(targetServerList); 
	    }
	} else {
	    _logger.log(Level.WARNING,"no.endpoints");	 
	}
    }   

    /**
     * during bootstrapping, weight is assumed "10" for all endpoints
     * then on, whenever server sends updates list,
     * create the list again here with right weights
     */
    private ClusterInstanceInfo makeClusterInstanceInfo(String str, int weight) {
	String[] host_port = str.split(":");
	String server_identifier = ""; //for bootstrapping, can be ""
	String type = "CLEAR_TEXT"; //will be clear_text for bootstrapping
	SocketInfo socketInfo = new SocketInfo(type,
					       host_port[0],
					       new Integer(host_port[1]).intValue());
	ClusterInstanceInfo instanceInfo = 
	    new ClusterInstanceInfo(server_identifier, 
				    weight,
				    new SocketInfo[]{socketInfo});
	return instanceInfo;
	
    }

    /**
    * find whether the string argument is already 
    * present in the endpointsList vector
    */
    private synchronized boolean notDuplicate(String str) {
	String[] host_port = str.split(":");
	for (ClusterInstanceInfo endpoint : endpointsList) {
	    ClusterInstanceInfo instanceInfo = endpoint;
	    for(int j = 0; j < instanceInfo.endpoints.length; j++) {
		if (instanceInfo.endpoints[j].host.equals(host_port[0]) && 
		    instanceInfo.endpoints[j].port == 
		    new Integer(host_port[1]).intValue()) {
		    return false;
		}
	    }
	}
	return true;
    }
    
    /**
     * This method checks for other ways of specifying endpoints
     * namely JNDI provider url 
     * orb host:port is used only if even env passed into 
     * getInitialContext is empty. This check is performed in 
     * S1ASCtxFactory.getInitialContext()
     */
    public String[] getEndpointForProviderURL(String providerURLString) {
     	String[] newList = null;
	if (providerURLString != null) {
	    try {
		IiopUrl providerURL = new IiopUrl(providerURLString);
		newList = getAddressPortList(providerURL);	
		_logger.log(Level.WARNING, "no.endpoints.selected.provider", 
			    new Object[] {providerURLString});
	    } catch (MalformedURLException me) {
		_logger.log(Level.WARNING, "provider.exception", 
			    new Object[] {me.getMessage(), 
					  providerURLString});
	    }	    
	} 
	return newList;
    }
    
    /**
     * randomize the list
     */
    public String[] randomize(String[] list) {
        //randomise the list to enable loadbalancing
	String[] randomizedList = new String[list.length];
        for (int i = 0; i < list.length; i++) {
	    int random;
	    do {		
	        random = rand.nextInt(list.length);
		_logger.fine("random ==> " + random);
	    } while (list[random] == null);
	    randomizedList[i] = list[random];
	    _logger.fine("randomisedList[" + i + "] ==> " + 
			 randomizedList[i]);
	    list[random] = null;
	}
	return randomizedList;
    }

   /**
    * return true of endpoints list is empty
    */
      public synchronized boolean isEmpty() {
	if (endpointsList.size() == 0)
	    return true;
	else return false;
      }


    /**
     * get a new shape of the endpoints
     * For e.g. if list contains A,B,C
     * if the logic below chooses B as the endpoint to send the req to
     * then return B,C,A.
     * logic used is as described in Class description comments
     */
    public synchronized Object[] getNextRotation() {
	int lowerLimit = 0; //lowerLimit
	int random = 0;
	//make sure that the random # is not 0
	//Random API gives a number between 0 and sumOfAllWeights
	//But our range intervals are from 1-upperLimit, 
	//11-upperLimit and so
	//on. Hence we dont want random # to be 0.
	_logger.fine("RoundRobinPolicy.getNextRotation -> sumOfAllWeights = " + sumOfAllWeights);
	while( random == 0) {
	    random = rand.nextInt(sumOfAllWeights);
	    if ( random != 0) {
		break;
	    }
	}
	_logger.fine("getNextRotation : random # = " + random + 
		     " sum of all weights = " + sumOfAllWeights);
	int i = 0;
	for (ClusterInstanceInfo endpoint : endpointsList) {
	    int upperLimit = lowerLimit + endpoint.weight;	    
	    _logger.fine("upperLimit = " + upperLimit);
	    if (random > lowerLimit && random <= upperLimit) {
		List<ClusterInstanceInfo> instanceInfo = 
		    new LinkedList<ClusterInstanceInfo>();
		
		//add the sublist at index 0 
		instanceInfo.addAll(0, 
				    endpointsList.subList(i, 
							  endpointsList.size()));
		//add the remaining list
		instanceInfo.addAll(endpointsList.subList(0, i));
		
		//print the contents...
		_logger.fine("returning the following list..." + 
			     instanceInfo.toString());
		
		return convertIntoCorbaloc(instanceInfo);
	    }
	    lowerLimit = upperLimit;
	    _logger.fine("lowerLimit = " + lowerLimit);	
	    i++;    
	}
	_logger.warning("Could not find an endpoint to send request to!");
	return null;
    }
    
    private Object[] convertIntoCorbaloc(List<ClusterInstanceInfo> list) {
	List host_port = new LinkedList();
	for (ClusterInstanceInfo endpoint : list) {
	    SocketInfo[] socketInfo = endpoint.endpoints;
	    for (int j = 0; j < socketInfo.length; j++) {
		if (!host_port.contains(socketInfo[j].host.trim() +
					":" + socketInfo[j].port)) {
		    host_port.add(socketInfo[j].host.trim() + 
				  ":" + socketInfo[j].port);
		}
	    }
	}
	return host_port.toArray();
    }


 

  /**
   * following methods (over-loaded) for getting all IP addresses 
   * corresponding to a particular host.
   * (multi-homed hosts). 
   */
    private String [] getAddressPortList(String [] hostPortList) {
        // The list is assumed to contain <HOST NAME>:<PORT> values
        Vector addressPortVector = new Vector();
        for (int i=0; i<hostPortList.length; i++) {
            try {
                IiopUrl url = new IiopUrl("iiop://"+hostPortList[i]);
                String [] apList = getAddressPortList(url);
                for (int j=0; j<apList.length; j++) {
                    addressPortVector.addElement(apList[j]);
                }
            } catch (MalformedURLException me) {
                _logger.log(Level.WARNING, "bad.host.port", 
			    new Object[] {hostPortList[i],
					  me.getMessage()});
            }
        }
        String [] ret = new String[addressPortVector.size()];
        for (int i=0; i<ret.length; i++) {
            ret[i] = (String)addressPortVector.elementAt(i);
        }
        // We return a list of <IP ADDRESS>:<PORT> values
        return ret;
    }
    
    private String [] getAddressPortList(IiopUrl iiopUrl) {
        // Pull out the host name and port
        IiopUrl.Address iiopUrlAddress = 
                (IiopUrl.Address)(iiopUrl.getAddresses().elementAt(0));
        String host = iiopUrlAddress.host;
        int portNumber = iiopUrlAddress.port;
        String port = Integer.toString(portNumber);
        // We return a list of <IP ADDRESS>:<PORT> values
        return getAddressPortList(host, port);        
    }
    
    public String [] getAddressPortList(String host, String port) {
        // Get the ip addresses corresponding to the host
        try {
            InetAddress [] addresses = InetAddress.getAllByName(host);
            String[] ret = new String[addresses.length];
            for (int i = 0; i < addresses.length; i++) {
                ret[i] = addresses[i].getHostAddress() + ":" + port;
            }
            // We return a list of <IP ADDRESS>:<PORT> values
            return ret;
        } catch (UnknownHostException ukhe) {
            _logger.log(Level.WARNING, "unknown.host",
			new Object[] {host, ukhe.getMessage()});
            return null;
        }
    }

  /**
   * log list contents
   */
    public synchronized void print() {
        _logger.fine("List contents ==> ");
	int i = 0;
	for (ClusterInstanceInfo endpoint : endpointsList) {
	    ClusterInstanceInfo instanceInfo = endpoint;
	    
	    _logger.fine("endpoint[" + i + "] ==> name =" + 
			 instanceInfo.name + " weight = " + 
			 instanceInfo.weight);
	    for (int j = 0; j < instanceInfo.endpoints.length; j++ ) {
		_logger.fine("IP addresses = " + 
			     instanceInfo.endpoints[j].host + ":" + 
			     instanceInfo.endpoints[j].port  + 
			     " type = " + instanceInfo.endpoints[j].type);
	    }
	    i++;
	}
    }
}
