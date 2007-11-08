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

package com.sun.enterprise.deployment.phasing;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.util.DeploymentProperties;

import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.backend.DeploymentLogger;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;

import com.sun.enterprise.resource.Resource;
import com.sun.enterprise.admin.common.MBeanServerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This class is the base class for resource phases
 */
public abstract class ResourcePhase extends DeploymentPhase {

    protected static final String resourcesMBeanName = 
        "com.sun.appserv:type=resources,category=config";
    protected static final String CREATE_RESOURCE = 
        "createResource";
    protected static final String CREATE_RESOURCE_REF = 
        "createResourceReference";
    protected static final String CREATE_RESOURCE_AND_REF = 
        "createResourceAndResourceReference";
    protected static final String DELETE_RESOURCE = 
        "deleteResource";
    protected static final String DELETE_RESOURCE_REF = 
        "deleteResourceReference";
    protected static final String DELETE_RESOURCE_AND_REF = 
        "deleteResourceAndResourceReference";

    protected static final String DOMAIN_TARGET = 
        "domain";

    protected MBeanServer mbs = MBeanServerFactory.getMBeanServer();
                                                                                
    /** Deployment Logger object for this class */
    private static final Logger sLogger = DeploymentLogger.get();

    protected void doResourceOperation(DeploymentRequest req) throws Exception {
        String targetListString = req.getResourceTargetList();
        List<String> targetList = DeploymentServiceUtils.getTargetNamesFromTargetString(targetListString);

        String resourceAction = req.getResourceAction();
        if (resourceAction == null || 
            getActualAction(resourceAction).equals(
                DeploymentProperties.RES_NO_OP)) {
            return;
        }

        if (targetList == null || targetList.isEmpty()) {
            return;
        }

        List<Resource> resourceList = DeploymentServiceUtils.getResourceList(
            req, getForceParsing(resourceAction), deploymentCtx);

        // empty resource list, no resource to process
        if (resourceList.size() == 0) {
            return;
        }

        handleResources(resourceAction, targetList, 
            getRelevantResources(resourceList)); 
    }


    protected void handleResources(String resourceAction, 
        List<String> targetList, List<Resource> resourceList) 
        throws Exception {

        // empty sub resource list, no resource to process
        if (resourceList.size() == 0) {
            return;
        }

        if (resourceAction.equals(DeploymentProperties.RES_DEPLOYMENT)) {
            handleDeployment(targetList, resourceList);
        } else if (resourceAction.equals(DeploymentProperties.RES_CREATE_REF)){
            handleCreateApplicationRef(targetList, resourceList);
        } else if (resourceAction.equals(DeploymentProperties.RES_DELETE_REF)){
            handleDeleteApplicationRef(targetList, resourceList);
        } else if (resourceAction.equals(
            DeploymentProperties.RES_UNDEPLOYMENT)){
            handleUndeployment(targetList, resourceList);
        } else if (resourceAction.equals(
            DeploymentProperties.RES_REDEPLOYMENT)){
            handleRedeployment(targetList, resourceList);
        }

        // flush the config and send the events here
        DeploymentServiceUtils.flushConfigAndSendEvents();
    }

    // invoke with both resource and resource-ref elements created
    // special case: when target is domain, only create resource element
    protected void handleDeployment(List<String> targetList,
        List<Resource> resourceList) throws Exception {
        ObjectName mbeanName = new ObjectName(resourcesMBeanName);

        // if target is domain, only create resource
        if (targetList.size() == 1 &&
            targetList.get(0).equals(DOMAIN_TARGET)) {
            String[] signature = new String[]{
                "java.util.List", "java.lang.Boolean"};
            Object[] params = new Object[]{resourceList, Boolean.TRUE};
            mbs.invoke(mbeanName, CREATE_RESOURCE, params, signature);
        } else {
            String[] signature = new String[]{
                "java.util.List", "java.util.List", "java.lang.Boolean"};
            Object[] params = new Object[]{resourceList, targetList, 
                Boolean.TRUE};
            mbs.invoke(mbeanName, CREATE_RESOURCE_AND_REF, params, signature);
        }
    }

    // invoke with only resource-ref element created
    protected void handleCreateApplicationRef(List<String> targetList,
        List<Resource> resourceList) throws Exception {
        ObjectName mbeanName = new ObjectName(resourcesMBeanName);
        String[] signature = new String[]{
            "java.util.List", "java.util.List", "java.lang.Boolean"};
        Object[] params = new Object[]{resourceList, targetList, 
            Boolean.TRUE};
        mbs.invoke(mbeanName, CREATE_RESOURCE_REF, params, signature);
    }

    // invoke with both resource and resource-ref elements deleted
    // special case: when target is domain, only delete resource element
    protected void handleUndeployment(List<String> targetList,
        List<Resource> resourceList) {
        try {
            ObjectName mbeanName = new ObjectName(resourcesMBeanName);

            // if target is domain, only delete resource
            if (targetList.size() == 1 &&
                targetList.get(0).equals(DOMAIN_TARGET)) {
                String[] signature = new String[]{"java.util.List"};
                Object[] params = new Object[]{resourceList};
                mbs.invoke(mbeanName,DELETE_RESOURCE, params, signature);
            } else {
                String[] signature = new String[]{
                    "java.util.List", "java.util.List"};
                Object[] params = new Object[]{resourceList, targetList};
                mbs.invoke(mbeanName, DELETE_RESOURCE_AND_REF, params, signature);
            }
        } catch (Exception e) {
            // we will just log the exception as warning message and will not 
            // fail undeployment
            sLogger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    abstract protected void handleRedeployment(List<String> targetList,
        List<Resource> resourceList) throws Exception;

    // invoke with only resource-ref element deleted
    protected void handleDeleteApplicationRef(List<String> targetList,
        List<Resource> resourceList) throws Exception {
        ObjectName mbeanName = new ObjectName(resourcesMBeanName);
        String[] signature = new String[]{
            "java.util.List", "java.util.List"};
        Object[] params = new Object[]{resourceList, targetList};
        mbs.invoke(mbeanName, DELETE_RESOURCE_REF, params, signature);
    }

    protected boolean getForceParsing(String resAction) {
        return false;
    }

    protected String getActualAction(String resAction) {
        return resAction;
    }

    abstract protected List<Resource> getRelevantResources(
        List<Resource> allResources);
}
