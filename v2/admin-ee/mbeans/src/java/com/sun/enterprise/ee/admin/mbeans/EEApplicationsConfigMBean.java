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
package com.sun.enterprise.ee.admin.mbeans;

import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.admin.mbeans.ApplicationsConfigMBean;
import com.sun.enterprise.deployment.phasing.DeploymentService;
import com.sun.enterprise.deployment.phasing.ApplicationReferenceHelper;

import com.sun.enterprise.admin.common.exception.MBeanConfigException;

import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.target.Target;

import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ServerTags;

import com.sun.enterprise.ee.admin.ExceptionHandler;

import com.sun.logging.ee.EELogDomains;
import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanException;

/**
 * Deployment MBean for SE/EE.
 * 
 * @author Nazrul Islam
 */
public class EEApplicationsConfigMBean extends com.sun.enterprise.ee.deployment.EEApplicationsConfigMBean
    implements com.sun.enterprise.ee.admin.mbeanapi.EEApplicationsConfigMBean
{

    private static final TargetType[] VALID_CREATE_DELETE_TYPES = new TargetType[] {
        TargetType.CLUSTER, TargetType.UNCLUSTERED_SERVER, TargetType.DAS};          
        
    private static final TargetType[] VALID_LIST_TYPES = new TargetType[] {
        TargetType.CLUSTER, TargetType.SERVER, TargetType.DAS};
        
    private static Logger _logger = null;        
    
    private static Logger getLogger() 
    {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }        
        
    private static ExceptionHandler _handler = null;
    
    //The exception handler is used to parse and log exceptions
    protected static ExceptionHandler getExceptionHandler() 
    {
        if (_handler == null) {
            _handler = new ExceptionHandler(getLogger());
        }
        return _handler;
    }      
    
    public EEApplicationsConfigMBean() throws MBeanConfigException  
    {
        super();
        initializeCustomMBeanHandlers();
    }
    
    private void initializeCustomMBeanHandlers() {
        this.cmo    = new EnterpriseCustomMBeanOperations();
        this.cmcq   = new EnterpriseCustomMBeanConfigQueries();
    }
    public String[] listApplicationReferencesAsString(String targetName)
         throws ConfigException
    {
        try {            
            final ConfigContext configContext = getConfigContext();        
            final Target target = TargetBuilder.INSTANCE.createTarget(VALID_LIST_TYPES,
                targetName, configContext);    
            ApplicationRef[] refs = target.getApplicationRefs();                       
            String[] result = new String[refs.length];
            for (int i = 0; i < refs.length; i++) {
                result[i] = refs[i].getRef();
            }
            return result;          
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "eeadmin.listApplicationReferences.Exception", 
                targetName);            
        }
    }    
    
    /**
     *Returns the ObjectNames of all those components that are 
     *referencing the module ref passed.  
     *
     *@param applicationRef The deployed application or module from
     *       which we want a list of components that reference it. 
     *@return An ObjectName array of referencees.
     */
    public ObjectName[] listReferencees(String applicationRef) 
        throws ConfigException
    {
        ObjectName[]        referencees = new ObjectName[0];
        try {
            final ConfigContext ctx         = getConfigContext();            
            final ArrayList objectNames     = new ArrayList();

            final Server[] servers = 
                ServerHelper.getServersReferencingApplication(ctx, 
                        applicationRef);
            if (servers != null) {
                for (int i = 0; i < servers.length; i++) {
                    final String name = servers[i].getName();

                    if (!ServerHelper.isServerClustered(ctx, name))
                    {
                        objectNames.add(getServerObjectName(name));
                    }
                }
            }
            final Cluster[] clusters = 
                ClusterHelper.getClustersReferencingApplication(ctx, 
                        applicationRef);
            if (clusters != null) {
                for (int i = 0; i < clusters.length; i++) {
                    final String name = clusters[i].getName();
                    objectNames.add(getClusterObjectName(name));
                }
            }
            referencees = (ObjectName[])objectNames.toArray(new ObjectName[0]);
        } catch (Exception ex) {
           throw getExceptionHandler().handleConfigException(
             ex, "eeadmin.listApplicationReferencees.Exception", 
             applicationRef); 
        }

        return referencees;
    }               
}
