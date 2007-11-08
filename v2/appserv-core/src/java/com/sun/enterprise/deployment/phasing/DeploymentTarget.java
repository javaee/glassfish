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
 * DeploymentTarget.java
 *
 * Created on May 23, 2003, 3:47 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/DeploymentTarget.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import javax.enterprise.deploy.spi.Target;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.deployment.backend.DeployableObjectType;

/**
 * Abstract class extended by ServerDeploymentTarget, GroupDeploymentTarget
 * @author Sandhya E
 */
public interface DeploymentTarget extends Target{

    /**
     * Returns the list of modules of specified type and specified status. 
     * If enabled = true, all enabled modules of specified type are returned
     * If enabled = false, all disabled modules of type are returned
     * If enabled = null, all modules are returned
     * @param type module type DeployableObjectType
     * @param enabled status of module that have to be returned
     */
    public abstract String[] getModules(DeployableObjectType type, Boolean enabled) 
        throws DeploymentTargetException;
    
    /**
     * Adds a reference to this application in server
     * @param appName name of the application
     * @param enabled if true application is marked as enabled
     */
    public abstract void addAppReference(String appName, boolean enabled, String virtualServers) throws DeploymentTargetException;
    
    /**
     * Removes a reference to this app from the target
     * @param appName name of the application
     */
    public abstract void removeAppReference(String appName) throws DeploymentTargetException;
    
    /**
     * sends applicaiton/module start stop events to the target server.
     * @param eventType 
     * @param appName name of the application/module
     * @param moduleType ["ejb"/"web"/"connector"]
     */
    public abstract boolean sendStartEvent(int eventType, String appName, String moduleType) throws DeploymentTargetException;

    /**
     * sends applicaiton/module start stop events to the target server.
     * @param eventType
     * @param appName name of the application/module
     * @param moduleType ["ejb"/"web"/"connector"]
     * @param isForced indicates if the deployment is forced
     */
    public abstract boolean sendStartEvent(int eventType, String appName, 
           String moduleType, boolean isForced) throws DeploymentTargetException;

    public abstract boolean sendStartEvent(int eventType, String appName,
           String moduleType, boolean isForced, int loadUnloadAction) throws DeploymentTargetException;

    public abstract boolean sendStopEvent(int eventType, String appName, String moduleType, boolean cascade) throws DeploymentTargetException;

    public abstract boolean sendStopEvent(int eventType, String appName, String moduleType, boolean cascade, boolean force) throws DeploymentTargetException;

    public abstract boolean sendStopEvent(int eventType, String appName, String moduleType, boolean cascade, boolean force, int loadUnloadAction) throws DeploymentTargetException;

    public abstract com.sun.enterprise.admin.target.Target getTarget();
        
}
