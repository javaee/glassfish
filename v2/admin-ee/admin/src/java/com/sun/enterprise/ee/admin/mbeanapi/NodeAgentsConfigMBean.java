
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



import com.sun.enterprise.ee.admin.servermgmt.AgentException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;

import javax.management.MBeanException;
import javax.management.ObjectName;


/**
 * Interface NodeAgentsConfigMBean
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface NodeAgentsConfigMBean {

    /**
     * Method listNodeAgentsAsString lists the node agents and optionally their status 
     * (e.g. running, stopped) corresponding to the specified target.
     *
     * @param targetName The target can be one of the following: "domain" -- lists all 
     * the nodeagents in the domain, node-agent-name -- lists the specified node agent
     * only, server-name -- lists the node agent which manages that specified server 
     * instance, cluster-name -- lists the node agents who manage instances in the 
     * specified custer.
     * @param andStatus true indicates that the server instances status are to be returned 
     * along with their names.
     *
     * @return the list of server instances managed by the node agent optionally with
     * their status.         
     *
     * @throws AgentException
     * @throws MBeanException
     *
     */
    String[] listNodeAgentsAsString(String targetName, boolean andStatus)
        throws AgentException, MBeanException;

    /**
     * Method listNodeAgents returns the JMX object names of the node agents 
     * corresponding to the specified target.
     *
     *
     * @param targetName The target can be one of the following: "domain" -- returns all 
     * the nodeagents in the domain, node-agent-name -- returns the specified node agent
     * only, server-name -- returns the node agent which manages that specified server 
     * instance, cluster-name -- returns the node agents who manage instances in the 
     * specified custer.
     *
     * @return The JMX object names of the node agents corresponding to the given target.
     *
     * @throws AgentException
     * @throws MBeanException
     *
     */
    ObjectName[] listNodeAgents(String targetName)
        throws AgentException, MBeanException;

    /**
     * Method getRuntimeStatus fetches the runtime status of the specified node agent.
     *
     *
     * @param agentName The node agent whose status is to be fetched
     *
     * @return runtime status
     *
     * @throws AgentException
     * @throws MBeanException
     *
     */
    RuntimeStatus getRuntimeStatus(String agentName)
        throws AgentException, MBeanException;
    
    /**
     * Method getNodeAgentRuntimeStatus fetches the runtime status object for each
     * node agent specified by the target.
     *
     * @param target The target can be one of the following: "domain" -- returns all 
     * the nodeagents in the domain, node-agent-name -- returns the specified node agent
     * only, server-name -- returns the node agent which manages that specified server 
     * instance, cluster-name -- returns the node agents who manage instances in the 
     * specified custer.
     *
     * @throws AgentException
     * @throws MBeanException
     *
     * @return a status for each node agent specified by the given target
     */    
    RuntimeStatusList getNodeAgentRuntimeStatus(String target)
        throws AgentException, MBeanException;

    /**
     * Method clearRuntimeStatus clears the runtime status of the named node agent.
     * This results in the list of recent error messages being deleted.
     *
     *
     * @param agentName The name of the node agent whose status is to be cleared.
     *
     * @throws AgentException
     * @throws MBeanException
     *
     */
    void clearRuntimeStatus(String agentName)
        throws AgentException, MBeanException;

    /**
     * Method deleteNodeAgentConfig deletes the node agent configuration of the named 
     * node agent. The node agent need not be running, but all of its server instances 
     * must be deleted.
     *     
     * @param nodeAgentName The name of the node agent to delete.
     *
     * @throws AgentException
     * @throws MBeanException
     *
     */
    void deleteNodeAgentConfig(String nodeAgentName)
        throws AgentException, MBeanException;

    /**
     * Method createNodeAgentConfig creates a node agent configuration. This is 
     * useful to create a place holder for a node agent which has yet to be 
     * installed or which has been installed but has not yet contacted the 
     * Domain Administration Server (DAS).     
     *
     * @param nodeAgentName The name of the node agent to create
     *
     * @return
     *
     * @throws AgentException
     * @throws MBeanException
     *
     */
    ObjectName createNodeAgentConfig(String nodeAgentName)
        throws AgentException, MBeanException;

    /**
     * Method rendezvousWithDAS adds the specified Node Agent to the domain. 
     * This operation is invoked from the Node Agent when it is initiating the rendezvous. 
     * 
     * This is an internal method intended to be called by the Node Agent only.
     *     
     * @param host Node Agent host name on which the node agent will listen
     * @param port Node Agent JSR160 jmx connector port
     * @param nodeAgentName Node agent name
     * @param protocol JSR160 jmx connector protocol
     * @param clientHostName Host name on which clients can connect.
     * 
     * @return The portNumber that the DAS system connector is listening on
     * @throws AgentException
     * @throws MBeanException
     *
     */
    String rendezvousWithDAS(
        String host, String port, 
            String nodeAgentName, String protocol, String clientHostName)
                throws AgentException, MBeanException;
}


/*--- Formatted in Sun Java Convention Style on Tue, Mar 16, '04 ---*/


/*------ Formatted by Jindent 3.0 --- http://www.jindent.de ------*/
