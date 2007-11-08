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
 * ResourceAdapterStopPhase.java
 *
 * Created on May 20, 2003, 3:19 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/ResourceAdapterStopPhase.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanException;

import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentEvent;
import com.sun.enterprise.deployment.backend.DeploymentEventType;
import com.sun.enterprise.deployment.backend.DeploymentEventInfo;
import com.sun.enterprise.deployment.backend.DeploymentLogger;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.server.core.AdminNotificationHelper;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.server.Constants;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Phase that is responsible to send stop events when an application is undeployed or
 * disassociated
 * @author  Sandhya E
 */
public class ResourceAdapterStopPhase extends DeploymentPhase {
    
    /** Deployment Logger object for this class */
    public static final Logger sLogger = DeploymentLogger.get();
    
    /** string manager */
    private static StringManager localStrings =
        StringManager.getManager( ResourceAdapterStopPhase.class );
    
    /**
     * Creates a new instance of Class 
     * @param deploymentCtx DeploymentContext object
     */
    public ResourceAdapterStopPhase(DeploymentContext deploymentCtx)
    {
        this.deploymentCtx = deploymentCtx;
        this.name = RA_STOP;
    }
    
    /** 
     * Sends stop events to the required target 
     * @param req DeploymentRequest object
     * @param phaseCtx the DeploymentPhaseContext object     
     */
    public void runPhase(DeploymentPhaseContext phaseCtx)
    {
        String type = null;
        
        DeploymentRequest req = phaseCtx.getDeploymentRequest();

        DeploymentTarget target = (DeploymentTarget)req.getTarget(); 
        DeploymentStatus status = phaseCtx.getDeploymentStatus();

        if (!DeploymentServiceUtils.containsResourceAdapter(req)) {
            status.setStageStatus(DeploymentStatus.SUCCESS);
            return;
        }

        if(!req.isApplication())
        {         
            type = DeploymentServiceUtils.getModuleTypeString(req.getType());
        }
        
        try {
            if (req.getCascade() && !req.isForced()){
                deleteConnectorDependentResources(req.getName(),
                    target.getName());
                deploymentCtx.getConfigContext().flush();
                AdminContext adminContext = 
                        AdminService.getAdminService().getAdminContext();
                new AdminNotificationHelper(adminContext).sendNotification();
            }
        } catch(Throwable t){
            status.setStageStatus(DeploymentStatus.WARNING);
            status.setStageException(t);
            status.setStageStatusMessage(t.getMessage());
            return;
        }

        prePhaseNotify(getPrePhaseEvent(req));
        
        boolean success;
        try {
            success = target.sendStopEvent(req.getActionCode(), req.getName(), type, req.getCascade(), req.isForced(), Constants.UNLOAD_RAR);
        } catch(DeploymentTargetException dte) {
            status.setStageStatus(DeploymentStatus.WARNING);
            if (dte.getCause()!=null) {
                status.setStageException(dte.getCause());
                status.setStageStatusMessage(dte.getMessage());
            }
            return;
        }
        if (success) {
            status.setStageStatus(DeploymentStatus.SUCCESS);
        } else {
            status.setStageStatus(DeploymentStatus.WARNING);
            status.setStageStatusMessage("Application failed to stop");
        }            
        
        postPhaseNotify(getPostPhaseEvent(req));
        
        // if any exception is thrown. we let the stack unroll, it 
        // will be processed in the DeploymentService.
    }
    
    /**
     * Event that will be broadcasted at the start of the phase
     * @param req Deployment request object
     * @return DeploymentEvent
     */
    protected DeploymentEvent getPrePhaseEvent(DeploymentRequest req) 
    {
        return new DeploymentEvent(DeploymentEventType.PRE_RA_STOP, new DeploymentEventInfo(req));
    }
    
    /**
     * Event that will be broadcasted at the end of the phase
     * @param req Deployment request object
     * @return DeploymentEvent
     */
    protected DeploymentEvent getPostPhaseEvent(DeploymentRequest req) 
    {
        return new DeploymentEvent(DeploymentEventType.POST_RA_STOP,new DeploymentEventInfo(req) );
    }

    private void deleteConnectorDependentResources(
        String id, String targetName) throws Exception {
        try{
            MBeanServer mbs = MBeanServerFactory.getMBeanServer();
            ObjectName mbeanName =
                new ObjectName("com.sun.appserv:type=resources,category=config");
        
            Object[] params = new Object[] {};
            String[] signature = new String[] {}; 
            
            //delete admin objects
            ObjectName[] adminObjs =
                (ObjectName[]) mbs.invoke(mbeanName, LIST_ADMIN_OBJECTS,
                params, signature);
            String[] adminObjSignature = new String[]{
                "java.lang.String", "java.lang.String"};
            for(int i = 0 ; i< adminObjs.length;i++) {
                String raName =
                    (String)mbs.getAttribute(adminObjs[i],"res_adapter");
                if(id.equals(raName)) {
                    String adminObjName =
                        (String)mbs.getAttribute(adminObjs[i],"jndi_name");
                    Object[] deleteAdminParams =
                        new Object[]{adminObjName, (String)targetName};
                    mbs.invoke(mbeanName,
                        DELETE_ADMIN_OBJECT, deleteAdminParams,
                        adminObjSignature);
                }
            }
            //end delete admin objects

            //delete pools and resources
            ObjectName[] poolNames = (ObjectName[])mbs.invoke(mbeanName,
                LIST_CONNECTOR_CONNECTION_POOLS, params, signature);
            String[] deletePoolSignature = new String[] {"java.lang.String",
                "java.lang.Boolean", "java.lang.String"};
            for(int i = 0 ; i < poolNames.length ; i++) {
                String raName = (String)mbs.getAttribute(poolNames[i],
                    "resource_adapter_name");
                if(id.equals(raName)) {
                    String poolName = (String)mbs.getAttribute(poolNames[i],
                        "name");
                    Object[] deletePoolParams = new Object[] {poolName,
                        Boolean.TRUE, (String)targetName};
                    mbs.invoke(mbeanName, DELETE_CONNECTOR_CONNECTION_POOL,
                        deletePoolParams, deletePoolSignature);
                }
            }
            //end delete pools and resources

            //delete resource adapter config
            ObjectName[] resAdapterConfigs = (ObjectName[])mbs.invoke(
                mbeanName, LIST_RESOURCE_ADAPTER_CONFIGS, params, signature);
            String[] adapterConfigSignature = new String[]{
                "java.lang.String", "java.lang.String"};
            Object[] adapterConfigParams = new Object[]{id, (String)targetName};
            for(int i = 0 ; i < resAdapterConfigs.length ; i++) {
                String raName = (String)mbs.getAttribute(resAdapterConfigs[i],
                    "resource_adapter_name");
                if(id.equals(raName)) {
                    mbs.invoke(mbeanName, DELETE_RESOURCE_ADAPTER_CONFIG,
                        adapterConfigParams, adapterConfigSignature);
                }
            }
            //end delete resource adapter config

        }catch(Exception e){
            //FIXME i18n 
            throw new DeploymentPhaseException(getName(), "Exception occured while deleting dependent connector resources", e);
        }       
    }       
                
    private static final String LIST_ADMIN_OBJECTS =
        "getAdminObjectResource";
    private static final String DELETE_ADMIN_OBJECT = 
        "deleteAdminObjectResource";
    private static final String LIST_CONNECTOR_CONNECTION_POOLS = 
        "getConnectorConnectionPool"; 
    private static final String DELETE_CONNECTOR_CONNECTION_POOL = 
        "deleteConnectorConnectionPool";
    private static final String LIST_RESOURCE_ADAPTER_CONFIGS =
        "getResourceAdapterConfig";
    private static final String DELETE_RESOURCE_ADAPTER_CONFIG =
        "deleteResourceAdapterConfig";

}
