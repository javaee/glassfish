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
package com.sun.enterprise.diagnostics;

import java.util.Map;
import java.util.HashMap;

/**
 * Responsible for creating ServiceConfig object per configuration in
 * domain.xml
 * @author mu125243
 */
public class ServiceConfigFactory {
    
    /**
     * Map between a config and it's diagnostic service configuration.
     * Multiple instances may be using same configuration in which case
     * there is only one ServiceConfig object for those instances.
     */
    private static Map diagnosticServiceConfigs;
    
    /**
     * instance name to config name map
     */
    private static Map instanceConfigMap;
    
    /**
     * Config with default values 
     */
    private static ServiceConfig defaultConfig;
    
    private static ServiceConfigFactory configFactory;
    
    /** Creates a new instance of ServiceConfigFactory */
    private ServiceConfigFactory() {
        diagnosticServiceConfigs = new HashMap(5);
        instanceConfigMap = new HashMap(5);
    }
    
    /**
     * Returns a ServiceConfgiFactory object
     * @return ServiceConfigFactory
     */ 
    public synchronized static ServiceConfigFactory getInstance() {
        if (configFactory == null)
            configFactory = new ServiceConfigFactory();
        
        return configFactory;
    }
    
    /**
     * @return ServiceConfig with default values
     */
    public static ServiceConfig getDefaultServiceConfig() {
            if( defaultConfig == null) {
                defaultConfig = new ServiceConfig(true,true, true, 
                    true,true, true, Defaults.MIN_LOG_LEVEL, 
                    Defaults.MAX_NO_OF_ENTRIES, Defaults.LOG_FILE, null, null);
                return defaultConfig;
            }
        return null;
    }
    
    /**
     * Gets service config for a instance.
     * @param instanceName instance name
     * @return diagnostic-service configuration
     */
    public ServiceConfig getServiceConfig(String instanceName) {
        if (instanceName != null) {
            String configName = (String)instanceConfigMap.get(instanceName);
            return (ServiceConfig)diagnosticServiceConfigs.get(configName);
        }
        return null;
    }
    
    /**
     * Gets diagnostic-service configuration
     * @param local flag which indicates whether command is run in local or remote
     * @param repositoryRoot local/central repository root
     * @param instanceName name of the instance
     * @return diagnostic-service configuration for a instance
     *
     */
    public ServiceConfig getServiceConfig(boolean local, String repositoryRoot,
            String instanceName) {
        if (repositoryRoot != null && instanceName != null) {
            try {
                
                ServiceConfig config = new ServiceConfig(local, 
                        repositoryRoot, instanceName);
                return config;
            }catch(Exception de) {
                // If for some reason diagnostic-service element in domain.xml
                // is not readable, return diagnostic information with 
                // default values. 
                return getDefaultServiceConfig();
            }
        }
        return null;
    }
}
