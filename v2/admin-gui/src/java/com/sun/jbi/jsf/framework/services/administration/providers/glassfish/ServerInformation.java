/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 */

/*
 * ServerInformation 
 */
 
package com.sun.jbi.jsf.framework.services.administration.providers.glassfish;

import java.io.IOException;
import java.util.Set;
import javax.management.MBeanServerConnection;
import com.sun.enterprise.config.ConfigException;

/**
 * This is a helper interface used to get Application Server domain configuration 
 * and runtime context information.
 * 
 * @author Sun Microsystems
 *
 */
public interface ServerInformation {
    
    public static final String OBJECT_NAME = "com.sun.ebi:ServiceName=GlassFishServerInformation";

    /**
     * @param forceNew if set to true a new connection is created
     * @return the MBeanServerConnection for a Server instance
     */
    public MBeanServerConnection getMBeanServerConnection(String instanceName,
            boolean forceNew) throws IOException;

    /**
     * @return true if this instance is the DAS
     */
    public boolean isDAS();

    /**
     * Get the name of this instance. This would be called by both the DAS server
     * instances and a stand alone / clustered instance.
     *
     * @return the name of this server instance
     */
    public String getInstanceName();

    /**
     * @return the instance root property
     */
    public String getInstanceRootKey();

    /**
     * @return the instance root property
     */
    public String getInstallRootKey();

    /**
     * @return true if the instance is up and running, false otherwise
     */
    public boolean isInstanceUp(String instanceName) throws Exception;

    /** 
     * Returns true is multiple servers are permitted within the app 
     * server installation.
     */
    public boolean supportsMultipleServers();

    /**
     * Get the Target Name. If the instance is a clustered instance then the 
     * target is the instance name. If it is a part of a cluster then it is the
     * cluster name.
     *
     * @return the target name. 
     */
    public String getTargetName() throws Exception;

    /**
     * Get the Target Name for a specified instance. If the instance is not clustered 
     * the instance name is returned. This operation is invoked by the JBI instance
     * MBeans only.
     *
     * @return the target name. 
     */
    public String getTargetName(String instanceName) throws Exception;

    /**
     * @return a set of names of all stand alone servers in the domain.
     */
    public Set<String> getStandaloneServerNames() throws ConfigException;

    /**
     * @return a set of names of all clustered servers in the domain.
     */
    public Set<String> getClusteredServerNames() throws ConfigException;

    /**
     * @return a set of all clusters in the domain
     */
    public Set<String> getClusterNames() throws ConfigException;

    /**
     * @return a set of servers in a cluster
     */
    public Set<String> getServersInCluster(String clusterName)
            throws ConfigException;

    /**
     * @return true if the targetName is a valid standalone server name or a cluster name
     */
    public boolean isValidTarget(String targetName) throws ConfigException;

    /**
     * @return true if the target is a cluster
     */
    public boolean isCluster(String targetName) throws ConfigException;

    /**
     * @return true if the target is a standalone server
     */
    public boolean isStandaloneServer(String targetName) throws ConfigException;

    /**
     * @return true if the target is a standalone server
     */
    public boolean isClusteredServer(String targetName) throws ConfigException;

}
