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

/**
 *  AppServerContext.java
 *
 *  Created on February 5, 2007, 10:15 AM
 */
package com.sun.enterprise.jbi;

import javax.management.MBeanServerConnection;

/**
 * The JBI Runtime needs information on the servers/clusters in a domain, state of an
 * instance etc. This is the interface to get this application server context information.
 *
 * @author Sun Microsystems, Inc.
 */
public interface AppServerContext
{
    /*----------------------------------------------------------------------------------*\
     *         Common operations invoked from the DAS and non-DAS instances.            *
    \*----------------------------------------------------------------------------------*/
    
    /**
     * Determine if this instance is the central administration server.
     *
     * @return true if this instance is the central administration server.
     */
    boolean isDAS();
    
    /**
     * Get the "target" for this instance. If this instance :
     * <ul>
     *   <li> is the central admininistration server instance then target = "server" </li>
     *   <li> is a clustered instance then target = [cluster-name] </li>
     *   <li> is a standalone instance then target = [server-name] </li>
     * </ul>
     * <br/>
     * This is equivalent to calling getTargetName(getInstanceName()).
     *
     * @return the "target" for this instance.
     */
    String getTargetName()
        throws Exception;
    
    /**
     * Get the MBeanServerConnection for a specific instance.
     *
     * @param instanceName - instance name, the MBeanServerConnection for which is to 
     *                       be obtained.
     * @return the MBeanServerConnection to a given instance.
     * @throws Exception on errors
     */
    MBeanServerConnection   getMBeanServerConnection(String instanceName)
        throws Exception;
    
    /**
     * Get the name of the server instance. In case this is the central administartion 
     * server then instance name = "server".
     *
     * @return the name of the glassfish instance.
     */
    String getInstanceName();
    
    /*----------------------------------------------------------------------------------*\
     *         Operations invoked from the DAS only.                                    *
    \*----------------------------------------------------------------------------------*/
    
    /**
     * Determine the runtime state of an instance.
     *
     * @param instanceName - name of the instance whose state is to be determined.
     * @return true if the specified instance is running false is it is shutdown
     */
    boolean isInstanceUp(String instanceName);
    
    /**
     * Determine if the central administraion server supports multiple servers.
     *
     * @return true if this administration server supports multiple servers.
     */
    boolean multipleServersSupported();
    
    /**
     * Get the names of all the non-clustered standalone servers in the domain.
     *
     * @return an array of names of all the standalone servers in the domain.
     */
    String[] getStandaloneServerNames();
    
    /**
     * Get the names of all the clsuters in the domain.
     *
     * @return an array of names of all the clusters in the domain. If the domain has zero
     * clusters then an empty array is returned.
     */
    String[] getClusterNames();
    
    /**
     * Get the names of all the member servers in the specified cluster.
     *
     * @return an array of names of all the servers in the cluster. If the clusterName is
     * a non-existent cluster or does not have any member instances then an empty 
     * array is returned
     */ 
    String[] getServersInCluster(String clusterName);
    
    /**
     * Get the "target" for the specified instance.
     *
     * <ul>
     *   <li> is the central admininistration server instance then target = "server" </li>
     *   <li> is a clustered instance then target = [cluster-name] </li>
     *   <li> is a standalone instance then target = [server-name] </li>
     * </ul>
     *
     * @param instanceName - name of the instance, whose target is to be determined
     * @return the "target" for the specific instance
     */
    String getTargetName(String instanceName)
        throws Exception;
    /**
     *
     * @return true if the instance is clustered
     * @param instanceName - instance name
     */

    boolean isInstanceClustered(String instanceName)
        throws Exception;
}
