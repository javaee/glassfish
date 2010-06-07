/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 */
package com.sun.enterprise.admin.util;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Target {
    @Inject
    private Domain domain;

    /**
     * Checks if a given target is cluster or nor
     * @param targetName the name of the target
     * @return true if the target represents a cluster; false otherwise
     */
    public boolean isCluster(String targetName) {
        return (domain.getServerNamed(targetName) == null);
    }

    /**
     * Returns the Cluster element for a given cluster name
     * @param targetName the name of the target
     * @return Cluster element that represents the cluster
     */
    public Cluster getCluster(String targetName) {
        if(!isCluster(targetName))
            return null;
        List<Cluster> clList = domain.getClusters().getCluster();
        for(Cluster cl : clList) {
            if(targetName.equals(cl.getName())) {
                return cl;
            }
        }
        return null;
    }

    /**
     * Returns the config element that represents a given cluster
     * @param targetName the name of the target
     * @return Config element representing the cluster
     */
    public Config getClusterConfig(String targetName) {
        Cluster cl = getCluster(targetName);
        if(cl == null)
            return null;
        return(domain.getConfigNamed(cl.getConfigRef()));
    }

    /**
     * Given an instance that is part of a cluster, returns the Cluster element of the cluster to which the
     * given instance belons
     * @param targetName name of target
     * @return Cluster element to which this instance below
     */
    public Cluster getClusterForInstance(String targetName) {
        if(isCluster(targetName))
            return getCluster(targetName);
        String instanceCfgRef = domain.getServerNamed(targetName).getConfigRef();
        for(Cluster c : domain.getClusters().getCluster()) {
            if(c.getConfigRef().equals(instanceCfgRef))
                return c;
        }
        return null;
    }

    /**
     * Given the name of a target, returns a list of Server objects. If given target is a standalone server,
     * then the server's Server element is returned in the list. If the target is a cluster, then the list of Server
     * elements that represent all server instances of that cluster is returned.
     * @param targetName the name of the target
     * @return list of Server elements that represent the target
     */
    public List<Server> getInstances(String targetName) {
        List<Server> instances = new ArrayList<Server>();
        //TODO : Target can be a config or a domain; handle that here
        if(domain.getServerNamed(targetName) != null) {
            instances.add(domain.getServerNamed(targetName));
        } else {
            //TODO : Is this the way to get instances ? Cant we use some DuckType methods in config beans ?
            Cluster cluster = getCluster(targetName);
            if(cluster != null) {
                String clusterConfigName = cluster.getConfigRef();
                List<Server> svrList = domain.getServers().getServer();
                for(Server svr : svrList) {
                    if(clusterConfigName.equals(svr.getConfigRef())) {
                        instances.add(svr);
                    }
                }
            }
        }
        return instances;
    }

    /**
     * Given name of a target verifies if it is valid
     * @param targetName name of the target
     * @return true if the target is a valid cluster or server instance or a config
     */
    public boolean isValid(String targetName) {
        if(isCluster(targetName))
            return true;
        if(getInstances(targetName).size() != 0)
            return true;
        if(domain.getConfigNamed(targetName) != null)
            return true;
        return false;
    }
}
