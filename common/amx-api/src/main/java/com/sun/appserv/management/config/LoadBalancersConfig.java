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
    Configuration for the &lt;load-balancers&gt; element; it is an internal "node" which
    groups all resources under itself.
    @since Glassfish V3
*/
public interface LoadBalancersConfig
	extends ConfigElement, Container, ConfigCreator, ConfigRemover, ConfigCollectionElement
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.LOAD_BALANCERS_CONFIG;
    
	
	/**
          Calls Container.getContaineeMap( XTypes.LOAD_BALANCER_CONFIG).
          @return Map of items, keyed by name.
          @see com.sun.appserv.management.base.Container#getContaineeMap
	*/
    public Map<String,LoadBalancerConfig> getLoadBalancerConfigMap();
    
    /**
      Create a new LoadBalancer.  The 'lbConfigName' and 'name' must be non-null.
      @param name the name of the load balancer to create
      @param lbConfigName the non-null name of the lb config to reference.
      @param autoApplyEnabled flag to indicate if the LB changes are pushed 
             immediately to the physical load balancer. Defaults to false
      @param optional optional values, properties only
        <b> The known properties are </b>
          <ul>
          <li>
            property.device-host - Host name or IP address for the device
          </li>
        <li>
            property.device-admin-port - Device administration port number
            </li>
        <li>
            property.ssl-proxy-host - proxy host used for outbound HTTP
            </li>
        <li>
            property.ssl-proxy-port - proxy port used for outbound HTTP
            </li>
        </ul>
      @return a LoadBalancer

      @see LoadBalancerConfig
	*/
	public LoadBalancerConfig createLoadBalancerConfig(String name, String lbConfigName, 
                boolean autoApplyEnabled, Map<String,String> optional);

    /**
          Remove an existing &lt;LoadBalancerConfig&gt;.
          @param name the name of the load-balancer to remove.
	*/
	public void removeLoadBalancerConfig(String name);

}







