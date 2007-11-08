
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
package com.sun.enterprise.ee.admin.mbeanapi;



import java.util.Map;

import com.sun.enterprise.config.ConfigException;

import javax.management.MBeanException;

import com.sun.enterprise.deployment.backend.DeploymentStatus;


/**
 * Interface EEApplicationsConfigMBean
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface EEApplicationsConfigMBean {

    /**
     * Method createApplicationReference reates a reference to the specified
     * application (e.g. j2ee-application, web-module, ejb-jar application).
     * The application specified must have been previously deployed to the
     * domain. This effectively results in the application being "deployed"
     * and made available to instances specified in the target.
     *
     * @param targetName The target can be one of the following: cluster-name -- 
     * creates an application reference from the cluster (and all its instances)
     * to the application, unclustered-server-name -- creates an application 
     * reference from the unclustered server instance to the application.
     * @param enabled true if the application should be enabled and available in
     * the target or false otherwise.
     * @param virtualServers is the list of virtual servers in the cluster to
     * which the application will be deployed. This parameter is optional,
     * when null, the application is made available to all virtual servers.
     * @param referenceName the name of the application to be referenced by
     * the target.
     *
     * @throws ConfigException
     * @throws MBeanException
     *
     */
    DeploymentStatus createApplicationReference(
        String targetName, boolean enabled, String virtualServers,
            String referenceName) throws ConfigException, MBeanException;

    DeploymentStatus createApplicationReference(
        String targetName, String referenceName, Map options) 
        throws ConfigException, MBeanException;

    /**
     * Method deleteApplicationReference deletes a reference to the specified
     * application. The application is not removed from the domain, only the
     * reference removed from the specified target.
     * This effectively results in the application being "undeployed"
     * and no longer available to server instances of the target.
     *
     * @param targetName The target can be one of the following: cluster-name -- 
     * deletes an application reference from the cluster (and all its instances),
     * unclustered-server-name -- deletes an application reference
     * from the unclustered server instance.
     * @param referenceName the name of the application to be unreferenced
     * and no longer available to the target.
     *
     * @throws ConfigException
     * @throws MBeanException
     *
     */
    DeploymentStatus deleteApplicationReference(String targetName, String referenceName)
        throws ConfigException, MBeanException;

    DeploymentStatus deleteApplicationReference(String targetName, 
        String referenceName, Map options) throws ConfigException, MBeanException;

    /**
     * Method listApplicationReferencesAsString lists the applications referenced
     * by the target. This is effectively the list of the applications available
     * to the target. The names of the applications are returned.
     *
     * @param targetName The target can be one of the following: cluster-name -- 
     * lists application references of the cluster (and all its instances),
     * unclustered-server-name -- lists application references
     * of the unclustered server instance.
     *
     * @return the list of application references
     *
     * @throws ConfigException
     * @throws MBeanException
     *
     */
    String[] listApplicationReferencesAsString(String targetName)
        throws ConfigException, MBeanException;
}


/*--- Formatted in Sun Java Convention Style on Mon, Mar 15, '04 ---*/


/*------ Formatted by Jindent 3.0 --- http://www.jindent.de ------*/
