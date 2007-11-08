/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

/*
 * Class.java
 *
 * Created on November 11, 2003, 1:45 PM
 */

package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.util.RelativePathResolver;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;   

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;

/**
 *
 * @author  kebbs
 */
public class PropertyResolver extends RelativePathResolver {
    
    private Domain _domain = null;
    private Cluster _cluster = null;
    private Server _server = null;
    private Config _config = null;
    
    /** Creates a new instance of Class */       
    public PropertyResolver(ConfigContext configContext, String instanceName) 
        throws ConfigException
    {
        _domain = ServerHelper.getDomainConfigBean(configContext);
        _config = ServerHelper.getConfigForServer(configContext, instanceName);        
        _server = ServerHelper.getServerByName(configContext, instanceName);
        if (ServerHelper.isServerClustered(configContext, _server)) {
            _cluster = ClusterHelper.getClusterForInstance(configContext, instanceName);
        }
    }
    
    /**
     * Given a propery name, return its corresponding value in the specified 
     * SystemProperty array. Return null if the property is not found.
     */
    private String getPropertyValue(String propName, SystemProperty[] props) 
    {
        String propVal = null;
        for (int i = 0; i < props.length; i++) {
            if (props[i].getName().equals(propName)) {
                return props[i].getValue();
            }
        }
        return propVal;
    }
    
    /**
     * Given a propery name, return its corresponding value as defined in 
     * the domain, configuration, cluster, or server element. Return null if the property
     * is not found. Property values at the server override those at the configuration
     * which override those at the domain level.
     */
    public String getPropertyValue(String propName, boolean bIncludingEnvironmentVariables)
    {
        String propVal = null;
        //First look for a server instance property matching the propName
        if (_server != null) {
            propVal = getPropertyValue(propName, _server.getSystemProperty());
        }
        if (propVal == null) {
            if (_cluster != null) {
                //If not found in the server instance, look for the propName in the 
                //cluster
                propVal = getPropertyValue(propName, _cluster.getSystemProperty());
            }            
            if (propVal == null) {
                if (_config != null) {             
                    //If not found in the server instance or cluster, look for the 
                    //propName in the config
                    propVal = getPropertyValue(propName, _config.getSystemProperty());
                    if (propVal == null) {
                        if (_domain != null) {
                            //Finally if the property is not found in the server, cluster,
                            //or configuration, look for the propName in the domain
                            propVal = getPropertyValue(propName, _domain.getSystemProperty());
                        }
                    }
                }
            }
        }
        if (propVal == null) {
            propVal = super.getPropertyValue(propName, bIncludingEnvironmentVariables);
        }
        return propVal;
    }
    
    public String getPropertyValue(String propName)
    {
        return getPropertyValue(propName, true);
    }
}
