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

package com.sun.enterprise.ee.diagnostics;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.NodeAgentHelper;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.diagnostics.DiagnosticException;
import com.sun.enterprise.diagnostics.InvalidTargetException;
import com.sun.enterprise.diagnostics.TargetType;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.diagnostics.Constants;
import com.sun.enterprise.diagnostics.PERemoteTargetResolver;
import com.sun.enterprise.diagnostics.ServiceConfig;
import com.sun.enterprise.diagnostics.ServiceConfigFactory;
import com.sun.enterprise.server.ApplicationServer;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author mu125243
 */
public class EERemoteTargetResolver extends PERemoteTargetResolver{
    
    private static final String AGENT_CONFIG_CLASS_NAME =
            "com.sun.enterprise.ee.admin.servermgmt.AgentConfig";
    private static final String GET_AGENT_ROOT_METHOD ="getAgentRoot";
    private static final String GET_AGENT_NAME = "getAgentName";
    
    /** Creates a new instance of EERemoteTargetResolver */
    public EERemoteTargetResolver(String target, String repositoryDir,
            List<String> instances, TargetType type) {
        super(target,repositoryDir, false);
        this.type = type;
        this.instances = instances;
        setExecutionContext();
        
    }

    public boolean validateTarget() throws DiagnosticException {
        determineTargetType(target, repositoryDir);
        if(type != null)    
            return true;
        return false;
    }

    protected void setExecutionContext() {
        if(context == null) {
            if(instances== null && type == null) {
                instances = new ArrayList(1);
                context = EEExecutionContext.DAS_EC;
                logger.log(Level.FINEST, "diagnostic_service.execution_context_das");
            } else {
                context = EEExecutionContext.NODEAGENT_EC;
                logger.log(Level.FINEST, "diagnostic-service.execution_context_nondas");
            }
        }
    }
    
    protected void determineRepositoryDetails() {
        if(context.equals(EEExecutionContext.DAS_EC)) {
            super.determineRepositoryDetails();
            
        } else {
            
            try {
                Class classObj = Class.forName(AGENT_CONFIG_CLASS_NAME);
                Constructor constructorObj = 
                        classObj.getDeclaredConstructor(new Class[]{});
                Object obj = constructorObj.newInstance(new Object[]{});
                Method method = classObj.getMethod(GET_AGENT_ROOT_METHOD, null);
                repositoryDir = (String)method.invoke(obj, null);
                
                method = classObj.getMethod(GET_AGENT_NAME, null);
                repositoryName = (String)method.invoke(obj, null);
                
                logger.log(Level.FINEST, 
                        "diagnostic-service.repository_dir", repositoryDir);
                if(type.equals(EETargetType.INSTANCE)) {
                    repositoryDir = repositoryDir + File.separator + repositoryName ;
                    repositoryName = target;
                }

            } catch (Exception e) {
                logger.log(Level.WARNING,
                        "nodeAgent.nodeagent_properties_not_found",e);
            }
        }
        logger.log(Level.FINEST, "diagnostic-service.repository_dir", repositoryDir);
        logger.log(Level.FINEST, "diagnostic-service.repository", repositoryName);
    }
    
    protected void determineTargetType(String targetName, String taretDir) 
    throws DiagnosticException {
        logger.log(Level.FINEST, "diagnostic-service.target_type", type);
        if(type == null) {
                /** Remote mode **/
            ConfigContext configContext = com.sun.enterprise.admin.server.core.
                    AdminService.getAdminService().getAdminContext().
                    getAdminConfigContext();

            try {
                //Determine if it is a instance, a cluster, a node agent or a domain
                if(targetName.equals(EETargetType.DAS.getType()))
                    setTargetType(EETargetType.DAS);
                else if(ServerHelper.isAServer(configContext, targetName))
                    setTargetType(EETargetType.INSTANCE);
                else if(NodeAgentHelper.isANodeAgent(configContext, targetName))
                    setTargetType(EETargetType.NODEAGENT);
                else if(ClusterHelper.isACluster(configContext, targetName))
                    setTargetType(EETargetType.CLUSTER);
                else if(super.validateTarget()) 
                    setTargetType(EETargetType.DOMAIN);
            } catch (ConfigException ce) {
                throw new DiagnosticException(ce.getMessage());
            }
            //Ideally, will never reach here
            if(type == null)
                throw new InvalidTargetException("Invalid Target :" + target);
        }
    }
    
    protected void determineServiceConfigs() {
        EEExecutionContext eeContext = (EEExecutionContext)context;
        logger.log(Level.FINEST, "diagnostic-service.execution_context",
                eeContext.getContext());
        if(eeContext.equals(EEExecutionContext.DAS_EC)) {
            if((type.equals(EETargetType.DOMAIN)) || 
                (type.equals(EETargetType.DAS))) {
                addServiceConfig(Constants.SERVER);
            }
        }
        
        //Don't do anything. The service Configs for Nodeagent are added
        // in analyzeInput() of EEBackendObjectFactory
       
    } 
    
    protected void determineInstances() { 
        //Only in case of DAS context, need to determine instances
        // in NodeAgent context, it's already determined.
        if(context.equals(EEExecutionContext.DAS_EC)) {
            Server[] servers = null;
            ConfigContext configContext = com.sun.enterprise.admin.server.core.
                    AdminService.getAdminService().getAdminContext().
                    getAdminConfigContext();
            if(type.equals(EETargetType.CLUSTER)) {
                try {
                    servers = ServerHelper.getServersInCluster(configContext, target);
                    instances = getInstanceNames(servers);
                }catch(ConfigException ce) {
                    logger.log(Level.WARNING, "Error occured while retrieving servers for a cluseter" );
                }
            } else if(type.equals(EETargetType.NODEAGENT)) {
                try {
                    servers = ServerHelper.getServersOfANodeAgent(
                        configContext, target);
                    instances = getInstanceNames(servers);

                }catch (ConfigException ce){
                    logger.log(Level.WARNING, "Error occured while retrieving servers for a node agent" );
                }
            } else if(type.equals(EETargetType.DOMAIN)) {
                try {
                    servers = ServerHelper.getServersInDomain(configContext);
                    instances = getInstanceNames(servers);
                } catch(ConfigException ce) {
                    logger.log(Level.WARNING, "Error occured while retrieving servers for a domain" );
                }
            } else if(type.equals(EETargetType.INSTANCE) ||
                    type.equals(EETargetType.DAS)) {
                if(instances == null) {
                    instances = new ArrayList(1);
                }
                instances.add(target);
            }
        } 
        
    }
    
    private List<String> getInstanceNames(Server[] servers) {
        if(servers != null && servers.length > 0) {
            ArrayList instances = new ArrayList(5);
            for(int index = 0 ; index < servers.length; index++) {
                instances.add(servers[index].getName());
            }
            return instances;
        }
        return null;
    }
    
}
