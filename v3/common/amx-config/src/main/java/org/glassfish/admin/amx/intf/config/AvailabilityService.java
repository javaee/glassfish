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
package org.glassfish.admin.amx.intf.config;

import java.util.Map;

/**
Configuration for the &lt;availability-service&gt; element.
 */
public interface AvailabilityService
        extends ConfigElement, PropertiesAccess
{
    /**
    Get the EJBContainerAvailability MBean.
     */
    EjbContainerAvailability getEJBContainerAvailability();

    /**
    Get the WebContainerAvailability MBean.
     */
    WebContainerAvailability getWebContainerAvailability();

    
    String getAvailabilityEnabled();

    void setAvailabilityEnabled(String enabled);

    /**
     * If set to true, the lifecycle of the highly available store   
     * is matched with the lifecycle of the highly available         
     * cluster. The store is started or stopped with the cluster. It 
     * is removed when the cluster is deleted. When set to false,    
     * the store lifecycle would have to manually managed by the     
     * administrator.                   
     * @return the value of auto-manage-ha-store
     * @since AppServer 9.0
     */
    
    String getAutoManageHAStore();

    /**
     * If set to true, the lifecycle of the highly available store   
     * is matched with the lifecycle of the highly available         
     * cluster. The store is started or stopped with the cluster. It 
     * is removed when the cluster is deleted. When set to false,    
     * the store lifecycle would have to manually managed by the     
     * administrator.
     * @param enabled sets the value of auto-manage-ha-store
     * @since AppServer 9.0
     */
    void setAutoManageHAStore(String enabled);

    /**
     * This is the jndi-name for the JDBC Connection Pool used       
     * potentially by both the Web Container and the EJB Stateful    
     * Session Bean Container for use in checkpointing/passivation   
     * when persistence-type = "ha". See sfsb-ha-persistence-type    
     * and sfsb-persistence-type for more details. It will default   
     * to "jdbc/hastore". This attribute can be over-ridden in       
     * either web-container-availability (with                       
     * http-session-store-pool-name) and/or in                       
     * ejb-container-availability (with sfsb-store-pool-name). If    
     * store-pool-name is not over-ridden then both containers will  
     * share the same connection pool. If either container           
     * over-rides then it may have its own dedicated pool. In this   
     * case there must also be a new corresponding JDBC Resource and 
     * JDBC Connection Pool defined for this new pool name.
     * @return the jndi-name
     * @since AppServer 9.0
     */
    String getStorePoolName();

    /**
     * This is the jndi-name for the JDBC Connection Pool used       
     * potentially by both the Web Container and the EJB Stateful    
     * Session Bean Container for use in checkpointing/passivation   
     * when persistence-type = "ha". See sfsb-ha-persistence-type    
     * and sfsb-persistence-type for more details. It will default   
     * to "jdbc/hastore". This attribute can be over-ridden in       
     * either web-container-availability (with                       
     * http-session-store-pool-name) and/or in                       
     * ejb-container-availability (with sfsb-store-pool-name). If    
     * store-pool-name is not over-ridden then both containers will  
     * share the same connection pool. If either container           
     * over-rides then it may have its own dedicated pool. In this   
     * case there must also be a new corresponding JDBC Resource and 
     * JDBC Connection Pool defined for this new pool name.
     * @param storePoolName the jndi-name
     * @since AppServer 9.0
     */
    void setStorePoolName(String storePoolName);

    /**
     * comma-delimited list of server host names or IP addresses     
     * where high availability store management agents are running.  
     * For HADB the list must consist of an even number of hosts separated
     * by commas.  E.g. <b>host1,host2,host2,host1</b> 
     * @return the stored hostnames or IP addresses 
     * @since AppServer 9.0
     */
    String getHAAgentHosts();

    /**
     * comma-delimited list of server host names or IP addresses     
     * where high availability store management agents are running.  
     * 
     * @param value the stored hostnames or IP addresses 
     * @since AppServer 9.0
     */
    void setHAAgentHosts(String value);

    /**
     * port number where highly available store management agents    
     * can be contacted.  The default for HADB is 1862                                              
     * @return the port number
     * @since AppServer 9.0
     */
    
    String getHAAgentPort();

    /**
     * port number where highly available store management agents    
     * can be contacted.  The default for HADB is 1862                                              
     * @param value the port number 
     * @since AppServer 9.0
     */
    void setHAAgentPort(String value);

    /**
     * @since AppServer 9.0
     */
    String getHAAgentPassword();

    /**
     * @since AppServer 9.0
     */
    void setHAAgentPassword(String password);

    /**
     * The periodicity at which store health is checked.
     * @return the interval time in seconds
     * @since AppServer 9.0
     */
    
    String getHAStoreHealthcheckIntervalSeconds();

    /**
     * The periodicity at which store health is checked.
     * @param value the interval time in seconds 
     * @since AppServer 9.0
     */
    void setHAStoreHealthcheckIntervalSeconds(String value);

    /**
     * Name of the session store.  In HADB this corresponds to the name of 
     * the HADB instance.  The default is the cluster-name.
     * @return the store name
     * @since AppServer 9.0
     */
    String getHAStoreName();

    /**
     * Name of the session store.  In HADB this corresponds to the name of 
     * the HADB instance.  The default is the cluster-name.
     * @param value the new store name 
     * @since AppServer 9.0
     */
    void setHAStoreName(String value);

    /**
     * Application server stops saving session state when the store  
     * service does not function properly or is is not accessible    
     * for any reason. When this attribute is set to true, periodic  
     * checking is done to detect if the store service has become    
     * available again. If healthcheck succeeds the session state    
     * saving is resumed. Defaults to false.                      
     * @return the value of the flag 
     * @since AppServer 9.0
     */
    String getHAStoreHealthcheckEnabled();

    void setHAStoreHealthcheckEnabled(String value);

    public JmsAvailability getJmsAvailability();
}



