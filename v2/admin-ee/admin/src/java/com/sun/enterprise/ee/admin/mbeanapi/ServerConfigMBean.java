
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
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;

import javax.management.MBeanException;
import javax.management.ObjectName;

import java.util.Properties;


/**
 * Interface ServerConfigMBean
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface ServerConfigMBean {

    /**
     * Method getRuntimeStatus fetches the runtime status for the server. 
     *
     * @return runtime status     
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    RuntimeStatus getRuntimeStatus() throws InstanceException, MBeanException;

    /**
     * Method clearRuntimeStatus clears the runtime status of the server.
     * This results in the list of recent error messages being deleted.
     *     
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void clearRuntimeStatus() throws InstanceException, MBeanException;

    /**
     * Method start attempts to start the server if it is not running and is
     * reachable. For an instance to be reachable, its Node Agent must
     * be running. 
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    public void start() throws InstanceException, MBeanException;

    /**
     * Method stop attempts to stop the server if it is running and is
     * reachable. For an instance to be reachable, its Node Agent must
     * be running. 
     *     
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    public void stop() throws InstanceException, MBeanException;

    /**
     * Method delete attempts to delete the server intstance. A server 
     * instance must be stopped before it can be deleted.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    public void delete() throws InstanceException, MBeanException;

    /**
     * Method isStandAlone returns an indication of whether the server is a
     * standalone server. A standalone server is an unclustered server 
     * with a configuration named "<server-instance-name>-config" and is 
     * the only instance referring to this configuration. 
     *
     * Do not confuse a standalone instance with an unclustered instance. 
     * Specifically, a standalone instance is an unclustered instance 
     * with a standalone configuration.
     *
     * @return true if the server is standalone
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    boolean isStandAlone() throws InstanceException, MBeanException;

    /**
     * Method isClustered returns an indication of whether the server
     * is a clustered server (i.e. belongs to a cluster). The set of
     * operations available to a clustered server is very limited. 
     * A clustered server can be started, stopped, deleted, its runtime status 
     * inquired, and its system properties manipulated, but little else.
     *
     * Do not confuse a standalone instance with an unclustered instance. 
     * Specifically, a standalone instance is an unclustered instance 
     * with a standalone configuration.
     *     
     * @return true if the server is clustered.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    boolean isClustered() throws InstanceException, MBeanException;

    /**
     * Method isDAS returns an indication of whether the server 
     * is the DAS (Domain Administration Server). The DAS is similar
     * to a standalone (and unclustered) server instance with the 
     * following exceptions: the DAS cannot be started or stopped
     * remotely, it cannot be deleted, and its configuration (typically
     * "server-config") cannot be shared.
     *
     * @return true if the server is the DAS (Domain Administration
     * Server).
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    boolean isDAS() throws InstanceException, MBeanException;

    /**
     * Method createResourceReference creates a reference to the specified
     * resource (e.g. jdbc-resource, connector-resource, mail-resource,
     * jdbc-connection-pool). The resource specified must exist in the
     * domain. This effectively results in the resource being "deployed"
     * and made available to the unclustered server instance.
     *
     * This method is available on unclustered server instances only; otherwise
     * an exception will be thrown.
     *
     * @param enabled true if the resource is to be enabled in the server
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
     * reference removed from the server.
     * This effectively results in the resource being "undeployed"
     * and no longer available to the unclustered server instance.
     *
     * This method is available on unclustered server instances only; otherwise
     * an exception will be thrown.
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
     * names) of the resources referenced by the server.
     *
     * @return The list of resource references.
     *     
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    public String[] listResourceReferencesAsString()
        throws InstanceException, MBeanException;

    /**
     * Method createApplicationReference creates a reference to the specified
     * application (e.g. j2ee-application, web-module, ejb-jar application).
     * The application specified must have been previously deployed to the
     * domain. This effectively results in the application being "deployed"
     * and made available to the unclustered server instance.
     *
     * This method is available on unclustered server instances only; otherwise
     * an exception will be thrown.
     *
     * @param enabled true if the application should be enabled and available in
     * the server or false otherwise.
     * @param virtualServers is the list of virtual servers in the server to
     * which the application will be deployed. This parameter is optional,
     * when null, the application is made available to all virtual servers.
     * @param referenceName the name of the application to be referenced by
     * the cluster.     
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    public void createApplicationReference(
        boolean enabled, String virtualServers, String referenceName)
            throws InstanceException, MBeanException;

    /**
     * Method deleteApplicationReference deletes a reference to the specified
     * application. The application is not removed from the domain, only the
     * reference removed from the server.
     * This effectively results in the application being "undeployed"
     * and no longer available to unclustered server.
     *
     * This method is available on unclustered server instances only; otherwise
     * an exception will be thrown.
     *     
     * @param referenceName the name of the application to be unreferenced
     * and no longer available to the server.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    public void deleteApplicationReference(String referenceName)
        throws InstanceException, MBeanException;

    /**
     * Method listApplicationReferencesAsString lists the applications referenced
     * by the server. This is effectively the list of the applications available
     * to the server. The names of the applications are returned.
     *
     * @return the list of application references
     *     
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    public String[] listApplicationReferencesAsString()
        throws InstanceException, MBeanException;

    /**
     * Method listSystemProperties lists the system properties of the server.
     * Server level system properties take precedence over system properties
     * with the same name defined at the domain, server's configuration level 
     * or server's cluster (if the server is clustered).
     *
     * @param inherit when true, system properties inherited from the domain,
     * configuration, and cluster are returned as well as the server properties; 
     * otherwise only the system properties of the server are returned.
     *
     * @return the list of system properties for the server (and optionally its 
     * inherited properties).
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    public Properties listSystemProperties(boolean inherit)
        throws InstanceException, MBeanException;

    /**
     * Method createSystemProperties reates or overwrites system properties of 
     * the server. Server level system properties take precedence over system properties
     * with the same name defined at the domain, server's configuration level 
     * or server's cluster (if the server is clustered).     
     *      
     * @param props The system properties to define for the server.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    public void createSystemProperties(Properties props)
        throws InstanceException, MBeanException;

    /**
     * Method deleteSystemProperty deletes the specified system property
     * of the server. Server level system properties take precedence over system properties
     * with the same name defined at the domain, server's configuration level 
     * or server's cluster (if the server is clustered).
     *
     * @param propertyName The name of the system property to delete. This property
     * must exist, or an exception is thrown.     
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    public void deleteSystemProperty(String propertyName)
        throws InstanceException, MBeanException;

    /**
     * Method listConfiguration returns the JMX object name of the configuration
     * referenced by the server. This will never be null.
     *
     * @return The JMX ojbect name of the server's configuration.
     *     
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    ObjectName listConfiguration() throws InstanceException, MBeanException;
}


/*--- Formatted in Sun Java Convention Style on Mon, Mar 15, '04 ---*/


/*------ Formatted by Jindent 3.0 --- http://www.jindent.de ------*/
