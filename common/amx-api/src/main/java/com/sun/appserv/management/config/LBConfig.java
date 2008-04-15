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
 
/*
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/config/LBConfig.java,v 1.2 2007/05/05 05:30:34 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2007/05/05 05:30:34 $
 */

package com.sun.appserv.management.config;

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.XTypes;

import java.util.Map;

/**
   Configuration for the lb-config element.
   @see com.sun.appserv.management.ext.lb.LoadBalancer 
 */
public interface LBConfig extends 
    AMXConfig, PropertiesAccess, NamedConfigElement,
        ServerRefConfigCR, ClusterRefConfigCR, Container {
    
    /** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
    public static final String	J2EE_TYPE = XTypes.LB_CONFIG;

    /**
      Returns the period, in seconds, within which a server must return a response 
      or otherwise it will be considered unhealthy. Must be greater than or 
      equal to 0. 
     */
    public String getResponseTimeoutInSeconds();

    /**
      Set the period, in seconds, within which a server must return a response or
      otherwise it will be considered unhealthy. Default value is 60 seconds. 
      Must be greater than or equal to 0. A value of 0 effectively turns off 
      this check functionality, meaning the server will always be considered healthy.
     */
    public void	setResponseTimeoutInSeconds(final String responseTimeoutInSeconds);

    /**
      Returns a boolean flag indicating how load-balancer will route HTTPS
      requests. If true, then an HTTPS request to the load-balancer will 
      result in an HTTPS request to the server; if false, then HTTPS requests 
      to the load-balancer result in HTTP requests to the server. 
     */
    public boolean getHttpsRouting();

    /**
      Set the boolean flag indicating how load-balancer will route HTTPS
      requests. If true, then an HTTPS request to the load-balancer will 
      result in an HTTPS request to the server; if false, then HTTPS requests 
      to the load-balancer result in HTTP requests to the server. 
      Default is to use HTTP (i.e. value of false);
     */
    public void	setHttpsRouting(final boolean value);
    
    /**
      Returns the maximum period, in seconds, that a change to the load
      balancer configuration file takes before it is detected by
      the load balancer and the file reloaded. A value of 0
      indicates that reloading is disabled. 
     */
    public String getReloadPollIntervalInSeconds();

    /**
      Set the maximum period, in seconds, that a change to the load balancer 
      configuration file takes before it is detected by the load balancer and 
      the file reloaded. A value of 0 indicates that reloading is disabled. 
      Default period is 1 minute (60 seconds)
     */
    public void	setReloadPollIntervalInSeconds(final String reloadPollIntervalInSeconds);

    /**
      Returns the boolean flag that determines whether monitoring is switched
      on or not. Default is that monitoring is switched off (false)
     */
    public boolean getMonitoringEnabled();

    /**
      Set the boolean flag that determines whether monitoring is switched
      on or not. Default is that monitoring is switched off (false)
     */
    public void	setMonitoringEnabled(final boolean value);
   
    /**
      Returns the boolean flag that determines whether a route cookie is or is
      not enabled.
     */
    public boolean getRouteCookieEnabled();

    /**
      Set the boolean flag that determines whether a route cookie is or is
      not enabled. Default is enabled (true).
     */
    public void	setRouteCookieEnabled(final boolean value);
    
    /**
      Calls Container.getContaineeMap(XTypes.CLUSTER_REF_CONFIG ).
      @return Map of ClusterRefConfig MBean proxies, keyed by name.
      @see com.sun.appserv.management.base.Container#getContaineeMap
     */
    public Map<String,ClusterRefConfig> getClusterRefConfigMap();

    /**
      Calls Container.getContaineeMap(XTypes.SERVER_REF_CONFIG ).
      @return Map of ServerRefConfig MBean proxies, keyed by name.
      @see com.sun.appserv.management.base.Container#getContaineeMap
     */
    public Map<String,ServerRefConfig> getServerRefConfigMap();    
}
