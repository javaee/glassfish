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
package com.sun.enterprise.ee.deployment;

import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.common.exception.DeploymentException;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.deployment.phasing.DeploymentService;

import com.sun.enterprise.admin.mbeans.ApplicationsConfigMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Deployment MBean for SE/EE.
 * 
 * @author Nazrul Islam
 */
public class EEApplicationsConfigMBean extends ApplicationsConfigMBean   
{    
    public EEApplicationsConfigMBean(String instanceName, 
            ConfigContext ctx) throws MBeanConfigException 
    {

        super(instanceName, ctx);
        mInstanceName = instanceName;
        m_configContext = ctx;
        deployService = DeploymentService.getDeploymentService(ctx);
    }
    
    public EEApplicationsConfigMBean() 
            throws MBeanConfigException  
    {
        super();
    }        
                    
    /**
     * Returns all deployable targets in this domain. All groups
     * and all servers(servers that are not part of any groups)
     * @return array of targets to which deployment can be performed
     *               [ an array of 0 if there are no targets]
     * @throws MBeanConfigException
     */
    public String[] getTargets() throws MBeanConfigException{
        try {
            java.util.ArrayList targetList = new java.util.ArrayList();
            final MBeanServer mbs = MBeanServerFactory.getMBeanServer();
            final AdminContext ac = MBeanRegistryFactory.getAdminContext();
            final MBeanRegistry mr = 
                    MBeanRegistryFactory.getAdminMBeanRegistry();
            final String dn = ac.getDomainName();
            final String servers = ServerTags.SERVERS;
            final String server = ServerTags.SERVER;
            final String cluster = ServerTags.CLUSTERS;
            final String[] locations = new String[]{dn};
            ObjectName serversON = mr.getMbeanObjectName(servers, locations);
            ObjectName groupsON = mr.getMbeanObjectName(cluster, locations);
            
            try{
                ObjectName[] groupONArr = (ObjectName[])mbs.invoke(groupsON, 
                    "getCluster", emptyParams, emptySignature);
                for(int i = 0; i < groupONArr.length; i++){
                    targetList.add(mbs.getAttribute(groupONArr[i], "name"));              
                }
            }catch(Exception e){
            }

            try {
                ObjectName[] serverONArr = (ObjectName[])mbs.invoke(serversON, 
                    "getServer", emptyParams, emptySignature);
                for(int i = 0; i < serverONArr.length; i++){
                    String serverName = 
                        (String)mbs.getAttribute(serverONArr[i], "name");
                    String[] locs = new String[]{dn,serverName};
                    ObjectName mbeanName = mr.getMbeanObjectName(server, locs);
                    Boolean clustered = (Boolean)mbs.invoke(mbeanName,
                            "isClustered", emptyParams, emptySignature);
                    if(!clustered.booleanValue()) {
                        targetList.add(serverName);              
                    }
                }   
            } catch(Exception e) {
                e.printStackTrace();
                targetList.add("server");
            }
            return (String[])targetList.toArray(new String[]{});
        }catch(Throwable t) {
            throw new MBeanConfigException(t.getMessage());
        }
    }
}
