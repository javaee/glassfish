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
 * $Id: NodeAgentTarget.java,v 1.3 2005/12/25 04:14:39 tcfujii Exp $
 */

package com.sun.enterprise.admin.target;

//jdk imports
import java.io.Serializable;

//config imports
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.NodeAgentHelper;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ResourceRef;

public class NodeAgentTarget extends Target
{
    /**
     * i18n strings manager object
     */
    private static final StringManager strMgr = 
        StringManager.getManager(NodeAgentTarget.class);

    protected NodeAgentTarget(String name, ConfigContext cc)
    {
        super(name, cc);
    }

    public TargetType getType()
    {
        return TargetType.NODE_AGENT;
    }

    public String getTargetObjectName(String[] tokens)
    {
        checkTokens(tokens, 1);
        return (tokens[0] + ":type=node-agent,category=config,name=" + getName());
    }

    public ConfigTarget getConfigTarget() throws Exception
    {
        throw new ConfigException(strMgr.getString(
            "target.no_config_for_node_agent"));
    }

    public String getConfigRef() throws ConfigException
    {
        return null; 
    }
    
    /**
     * Return all the servers managed by the node agent
     */
    public Server[] getServers() throws ConfigException
    {
        return ServerHelper.getServersOfANodeAgent(getConfigContext(), getName());
    }
    
    /**
     * Return all the clusters who contain instances managed by this node agent
     */
    public Cluster[] getClusters() throws ConfigException
    {
        return ClusterHelper.getClustersForNodeAgent(getConfigContext(), getName());
    }
    
    /**
     * Return the configuration associated with this Node Agent.
     */
    public Config[] getConfigs() throws ConfigException
    {
        throw new ConfigException(strMgr.getString("target.not_supported",
            "getConfigs", getType().getName()));
    }
    
    public ApplicationRef[] getApplicationRefs() throws ConfigException
    {
        throw new ConfigException(strMgr.getString("target.not_supported",
            "getApplicationRefs", getType().getName()));
    }
    
    public ResourceRef[] getResourceRefs() throws ConfigException
    {
        throw new ConfigException(strMgr.getString("target.not_supported",
            "getResourceRefs", getType().getName()));
    }    

    /**
     * Return the node agent
     */
    public NodeAgent[] getNodeAgents() throws ConfigException
    {
        NodeAgent[] agents = new NodeAgent[1];
        agents[0] = NodeAgentHelper.getNodeAgentByName(getConfigContext(), getName());
        return agents;
    }
}
