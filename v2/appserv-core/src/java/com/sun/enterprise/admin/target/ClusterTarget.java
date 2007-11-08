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
 * $Id: ClusterTarget.java,v 1.3 2005/12/25 04:14:38 tcfujii Exp $
 */

package com.sun.enterprise.admin.target;

//jdk imports
import java.io.Serializable;

//config imports
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.NodeAgentHelper;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.NodeAgent;

public class ClusterTarget extends Target
{
    protected ClusterTarget(String name, ConfigContext cc)
    {
        super(name, cc);
    }

    public TargetType getType()
    {
        return TargetType.CLUSTER;
    }

    public String getTargetObjectName(String[] tokens)
    {
        checkTokens(tokens, 1);
        return (tokens[0] + ":type=cluster,category=config,name=" + getName());
    }

    public ConfigTarget getConfigTarget() throws Exception
    {
        return new ConfigTarget(getConfigRef(), getConfigContext());
    }

    public String getConfigRef() throws ConfigException
    {
        final Config config = ClusterHelper.getConfigForCluster(getConfigContext(), getName());
        return config.getName();
    }
    
    /**
     * Return all the servers in the cluster
     */
    public Server[] getServers() throws ConfigException
    {
        return ServerHelper.getServersInCluster(getConfigContext(), getName());
    }
    
    /**
     * Return the cluster
     */
    public Cluster[] getClusters() throws ConfigException
    {
        final Cluster[] clusters = new Cluster[1];
        clusters[0] = ClusterHelper.getClusterByName(getConfigContext(), getName());
        return clusters;
    }
    
    /**
     * Return the configuration referenced by the cluster
     */
    public Config[] getConfigs() throws ConfigException
    {
        final Config[] configs = new Config[1];        
        configs[0] = ClusterHelper.getConfigForCluster(getConfigContext(), getName());
        return configs;
    }
    
    /**
     * Return all the node agents that have instances that are part of the cluster
     */
    public NodeAgent[] getNodeAgents() throws ConfigException
    {
        return NodeAgentHelper.getNodeAgentsForCluster(getConfigContext(), getName());
    }        
    
    /**
     * Return all the application refs of the cluster
     */
    public ApplicationRef[] getApplicationRefs() throws ConfigException
    {
        return ClusterHelper.getApplicationReferences(getConfigContext(), getName());        
    }
    
    /**
     * Return all the resource refs of the cluster
     */    
    public ResourceRef[] getResourceRefs() throws ConfigException
    {
        return ClusterHelper.getResourceReferences(getConfigContext(), getName());        
    }    
}
