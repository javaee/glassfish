
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



import com.sun.enterprise.config.ConfigException;

import javax.management.MBeanException;


/**
 * Interface ResourcesConfigMBean
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface ResourcesConfigMBean {

    /**
     * Method createResourceReference creates a reference to the specified
     * resource (e.g. jdbc-resource, connector-resource, mail-resource,
     * jdbc-connection-pool). The resource specified must exist in the
     * domain. This effectively results in the resource being "deployed"
     * and made available to all servers in the specified target
     *
     * @param targetName The target can be one of the following: cluster-name -- 
     * creates a resources reference from the cluster (and all its instances)
     * to the resource, unclustered-server-name -- creates a resource 
     * reference from the unclustered server instance to the resource.
     * @param enabled true if the resource is to be enabled in the target
     * false otherwise.
     * @param referenceName the name of the resource to be referenced. The
     * name is typically the jndi name of the resource created in the domain.
     *
     * @throws ConfigException
     * @throws MBeanException
     *
     */
    void createResourceReference(
        String targetName, boolean enabled, String referenceName)
            throws ConfigException, MBeanException;

    /**
     * Method deleteResourceReference deletes a reference to the specified
     * resource. The resource is not removed from the domain, only the
     * reference removed from the target (and all of its instances).
     * This effectively results in the resource being "undeployed"
     * and no longer available to server instances of the target.
     *
     * @param targetName The target can be one of the following: cluster-name -- 
     * deletes a resources reference from the cluster (and all its instances)
     * to the resource, unclustered-server-name -- deletes a resource 
     * reference from the unclustered server instance to the resource.
     * @param referenceName the name of the resource to be un-referenced. The
     * name is typically the jndi name of the resource.
     *
     *
     * @throws ConfigException
     * @throws MBeanException
     *
     */
    void deleteResourceReference(String targetName, String referenceName)
        throws ConfigException, MBeanException;

    /**
     * Method listResourceReferencesAsString lists the names (typically jndi
     * names) of the resources referenced by the target.
     *
     * @param targetName The target can be one of the following: cluster-name -- 
     * lists resources references of the cluster (and all its instances),
     * unclustered-server-name -- lists resource references of the 
     * unclustered server instance.
     *
     * @return The list of resource references.
     *
     * @throws ConfigException
     * @throws MBeanException
     *
     */
    String[] listResourceReferencesAsString(String targetName)
        throws ConfigException, MBeanException;
}


/*--- Formatted in Sun Java Convention Style on Mon, Mar 15, '04 ---*/


/*------ Formatted by Jindent 3.0 --- http://www.jindent.de ------*/
