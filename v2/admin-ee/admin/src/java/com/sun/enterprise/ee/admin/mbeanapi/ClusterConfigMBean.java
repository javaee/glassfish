
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



import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;

import javax.management.MBeanException;
import javax.management.ObjectName;

import java.util.Properties;


/**
 * Interface ClusterConfigMBean represents a particular cluster.
 *
 * @author kebbs
 * @version %I%, %G%
 */
public interface ClusterConfigMBean {

    /**
     * Method getRuntimeStatus fetches the runtime status for the cluster.
     * A cluster is considered running if any of the instances in the cluster are
     * running. Furthermore, a cluster will need to be restarted if any of
     * the instances need to be restarted. Finally, the list of error messages
     * for the server instances is not returned and each server must be
     * queried individually.
     *
     * @return runtime status
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    RuntimeStatusList getRuntimeStatus() throws InstanceException, MBeanException;

    /**
     * Method clearRuntimeStatus clears the runtime status for the cluster.
     * Specifically, the list of recently occurring error messages for each
     * instance in the cluster is cleared.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void clearRuntimeStatus() throws InstanceException, MBeanException;

    /**
     * Method start attempts to start all of the reachable instances in
     * the cluster. For an instance to be reachable, its Node Agent must
     * be running. This method will not return until an attempt has been
     * made to start all instances in the cluster; however, no indication
     * of failure will be returned should one of the instances in the
     * cluster fail to start or is not reachable.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    RuntimeStatusList start() throws InstanceException, MBeanException;        

    /**
     * Method stop attempts to stop all of the running server instances
     * in the cluster. An instance must be reachable (through its Node
     * Agent) to be stopped. This method will not return until an attempt has been
     * made to stop all instances in the cluster; however, no indication
     * of failure will not be returned should one of the instances in the
     * cluster fail to stop or is not reachable.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    RuntimeStatusList stop() throws InstanceException, MBeanException;        

    /**
     * Method delete attempts to delete the cluster. To be deleted a cluster
     * must contain no server instances. If the cluster is a standalone
     * cluster, then its corresponding configuration is deleted as well.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void delete() throws InstanceException, MBeanException;

    /**
     * Method isStandAlone indicates whether the cluster is standalone.
     * A standalone cluster has a configuration named "<cluster-name>-config"
     * and is the only entity (i.e. cluster or unclustered server instance)
     * referring to its configuration.
     *
     * @return true if the cluster is standalone, false otherwise.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    boolean isStandAlone() throws InstanceException, MBeanException;

    /**
     * Method createResourceReference creates a reference to the specified
     * resource (e.g. jdbc-resource, connector-resource, mail-resource,
     * jdbc-connection-pool). The resource specified must exist in the
     * domain. References created in a cluster are available on all
     * existing instances in the cluster or instances newly added to
     * the cluster. This effectively results in the resource being "deployed"
     * and made available to all server instances in the cluster.
     *
     * @param enabled true if the resource is to be enabled in the cluster
     * false otherwise.
     * @param referenceName the name of the resource to be referenced. The
     * name is typically the jndi name of the resource created in the domain.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void createResourceReference(boolean enabled, String referenceName)
        throws InstanceException, MBeanException;

    /**
     * Method deleteResourceReference deletes a reference to the specified
     * resource. The resource is not removed from the domain, only the
     * reference removed from the cluster (and all of its instances).
     * This effectively results in the resource being "undeployed"
     * and no longer available to server instances in the cluster.
     *
     * @param referenceName the name of the resource to be un-referenced. The
     * name is typically the jndi name of the resource.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void deleteResourceReference(String referenceName)
        throws InstanceException, MBeanException;

    /**
     * Method listResourceReferencesAsString lists the names (typically jndi
     * names) of the resources referenced by the cluster (and all of its
     * server instances).
     *
     * @return The list of resource references.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    String[] listResourceReferencesAsString()
        throws InstanceException, MBeanException;

    /**
     * Method createApplicationReference creates a reference to the specified
     * application (e.g. j2ee-application, web-module, ejb-jar application).
     * The application specified must have been previously deployed to the
     * domain. References created in a cluster are available on all
     * existing instances in the cluster or instances newly added to
     * the cluster. This effectively results in the application being "deployed"
     * and made available to all server instances in the cluster.
     *
     *
     * @param enabled true if the application should be enabled and available in
     * the cluster or false otherwise.
     * @param virtualServers is the list of virtual servers in the cluster to
     * which the application will be deployed. This parameter is optional,
     * when null, the application is made available to all virtual servers.
     * @param referenceName the name of the application to be referenced by
     * the cluster.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void createApplicationReference(
        boolean enabled, String virtualServers, String referenceName)
            throws InstanceException, MBeanException;

    /**
     * Method deleteApplicationReference deletes a reference to the specified
     * application. The application is not removed from the domain, only the
     * reference removed from the cluster (and all of its instances).
     * This effectively results in the application being "undeployed"
     * and no longer available to server instances in the cluster.
     *     
     * @param referenceName the name of the application to be unreferenced
     * and no longer available to the cluster and its instances.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void deleteApplicationReference(String referenceName)
        throws InstanceException, MBeanException;

    /**
     * Method listApplicationReferencesAsString lists the applications referenced
     * by the cluster. This is effectively the list of the applications available
     * to the cluster (and all of its instances). 
     * The names of the applications are returned.
     *
     * @return the list of application references
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    String[] listApplicationReferencesAsString()
        throws InstanceException, MBeanException;

    /**
     * Method listSystemProperties lists the system properties of the cluster.
     * Cluster level system properties take precedence over system properties
     * with the same name defined at the domain or configuration level, but can
     * be overwritten by system properties at the server instance level.
     *
     * @param inherit when true, system properties inherited from the domain
     * and and configuration are returned as well as the cluster properties; 
     * otherwise only the system properties of the cluster are returned.
     *
     * @return the list of system properties for the cluster (and optionally its 
     * inherited properties).
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    Properties listSystemProperties(boolean inherit)
        throws InstanceException, MBeanException;

    /**
     * Method createSystemProperties creates or overwrites system properties of 
     * the cluster. Cluster level system properties take precedence over system properties
     * with the same name defined at the domain or configuration level, but can
     * be overwritten by system properties at the server instance level.
     *
     * @param props The system properties to define for the cluster.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void createSystemProperties(Properties props)
        throws InstanceException, MBeanException;

    /**
     * Method deleteSystemProperty deletes the specified system property
     * of the custer. Cluster level system properties take precedence over system properties
     * with the same name defined at the domain or configuration level, but can
     * be overwritten by system properties at the server instance level.
     *
     * @param propertyName The name of the system property to delete. This property
     * must exist, or an exception is thrown.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void deleteSystemProperty(String propertyName)
        throws InstanceException, MBeanException;

    /**
     * Method listServerInstancesAsString lists the names of the server instances
     * that belong to the cluster and optionally the status (e.g. running, stopped)
     * of each instance.
     *
     * @param andStatus true if the status is to be returned along with the instance
     * name. false implies that only the server instance names will be returned.
     *
     * @return The list of servers in the cluster (optionally with their status).
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    String[] listServerInstancesAsString(boolean andStatus)
        throws InstanceException, MBeanException;

    /**
     * Method listServerInstances lists the JMX object names of the server instances
     * that belong to the cluster.
     *
     * @return The list of the object names of all the server instances in the
     * cluster. This list will be empty (0 length) if the cluster contains
     * no server instances.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    ObjectName[] listServerInstances()
        throws InstanceException, MBeanException;

    /**
     * Method listConfiguration returns the JMX object name of the configuration
     * referenced by the cluster (and all of its instances). This will never
     * be null.
     *
     * @return The JMX ojbect name of the cluster's configuration.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    ObjectName listConfiguration() throws InstanceException, MBeanException;
}


/*--- Formatted in Sun Java Convention Style on Mon, Mar 15, '04 ---*/


/*------ Formatted by Jindent 3.0 --- http://www.jindent.de ------*/
