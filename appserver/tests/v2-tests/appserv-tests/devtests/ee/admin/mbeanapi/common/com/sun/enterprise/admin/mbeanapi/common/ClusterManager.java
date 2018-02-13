/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.admin.mbeanapi.common;

import com.sun.appserv.management.config.*;
import com.sun.appserv.management.j2ee.J2EEServer;
import com.sun.appserv.management.j2ee.J2EECluster;
import com.sun.appserv.management.util.stringifier.SmartStringifier;

import java.io.IOException;
import java.util.Map;
import java.util.Iterator;

/**
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Sep 12, 2004
 * @version $Revision: 1.2 $
 */
public class ClusterManager {
    AMXConnector mAsapiConnector;
    ClusterConfig mClusterCfg;
    DomainConfig mDomainConfig;

    public ClusterManager(
                final String host,
                final int port,
                final String adminUser,
                final String adminPassword,
                final boolean useTLS )
                    throws IOException
    {
        mAsapiConnector = new AMXConnector(host, port, adminUser, adminPassword, useTLS);
        mDomainConfig = mAsapiConnector.getDomainRoot().getDomainConfig();
    }

    public ClusterManager( final AMXConnector conn){
        mAsapiConnector = conn;
        mDomainConfig = mAsapiConnector.getDomainRoot().getDomainConfig();
    }

    /**
     * creates a cluster
     * @param clusterName
     * @param configName
     * @param optional
     *
     * @return ClusterConfig
     */
    public ClusterConfig createCluster(final String clusterName,
                                               final String configName,
                                               final java.util.Map optional ) throws Exception
    {
        mClusterCfg = mDomainConfig.createClusterConfig(clusterName, configName, optional);
        return mClusterCfg;
    }

    /**
     * removes a cluster
     * @param name
     */
    public void deleteCluster(final String name) throws Exception{
        mDomainConfig.removeClusterConfig(name);
    }

    /**
     * Starts a cluster
     * @param name
     * @throws ObjectNotFoundException
     */
    public void startCluster(final String name) throws ObjectNotFoundException {
        final J2EECluster cluster = getCluster(name);
        if(cluster != null){
            cluster.start();
            return;
        }
        throw new ObjectNotFoundException("startInstance: instance "+
                                        name+" was not found by the DAS");
    }


    /**
     * stops a named standalone instance
     * @param name
     */
    public void stopCluster(final String name) throws ObjectNotFoundException {
        final J2EECluster cluster = getCluster(name);
        if(cluster != null){
            cluster.stop();
            return;
        }
        throw new ObjectNotFoundException("stopInstance: instance "+
                                        name + " was not found by the DAS");
    }

    private J2EECluster getCluster(final String name) {
        final Map clusters = mAsapiConnector.getDomainRoot().getJ2EEDomain().getClusterMap();
        final Iterator iter;
        J2EECluster cluster=null;
        String listName;
        for(iter=clusters.keySet().iterator();iter.hasNext();){
            if((listName = (String)iter.next()).equals(name)){
                cluster = (J2EECluster)clusters.get(listName);
                break;
            }
        }
        return cluster;
    }

    public void listClusters() {
        final Map clusters = mAsapiConnector.getDomainRoot().getJ2EEDomain().getClusterMap();
        final Iterator iter;
        for(iter=clusters.keySet().iterator();iter.hasNext();){
            println(((J2EECluster)iter.next()).getName());
        }
    }

    public boolean isCreated(final String name){
        final J2EECluster cluster = getCluster(name);
        if( cluster != null )
            return true;
        return false;
    }
    public static void   println( final Object o )
    {
        System.out.println( toString( o ) );
    }

    private static String  toString(final Object o )
    {
        return( SmartStringifier.toString( o ) );
    }
}
