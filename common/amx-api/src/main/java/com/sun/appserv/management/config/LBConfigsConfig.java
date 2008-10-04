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


package com.sun.appserv.management.config;

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.XTypes;

import java.util.Map;


/**
    Configuration for the &lt;lb-configs&gt; element; it is an internal "node" which
    groups all resources under itself.
    @since Glassfish V3
*/
public interface LBConfigsConfig
	extends ConfigElement, Container, ConfigCreator, ConfigRemover, ConfigCollectionElement
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.LB_CONFIGS_CONFIG;
    
      /**
      Calls Container.getContaineeMap( XTypes.LB_CONFIG ).
      @return Map of items, keyed by name.
      @see com.sun.appserv.management.base.Container#getContaineeMap
    */
    public Map<String,LBConfig> getLBConfigMap();

    /**
      Creates a new lb-config.  Legal options include:
      <ul>
         <li>{@link LBConfigKeys#RESPONSE_TIMEOUT_IN_SECONDS_KEY}</li>
         <li>{@link LBConfigKeys#HTTPS_ROUTING_KEY}</li>
         <li>{@link LBConfigKeys#RELOAD_POLL_INTERVAL_IN_SECONDS_KEY}</li>
         <li>{@link LBConfigKeys#MONITORING_ENABLED_KEY}</li>
         <li>{@link LBConfigKeys#ROUTE_COOKIE_ENABLED_KEY}</li>
      </ul>
      @param name The name of the load balancer configuration.
      @param params Remaining attributes for creation of a new lb-config.
      @return A proxy to the LBConfig MBean that manages the newly created lb-config.
      @see LBConfigKeys
     */
    public LBConfig createLBConfig(String name, Map<String,String> params);

    /**
      Removes a lb-config.
      @param name The name of the load balancer configuration.
     */
    public void removeLBConfig(String name);

}







