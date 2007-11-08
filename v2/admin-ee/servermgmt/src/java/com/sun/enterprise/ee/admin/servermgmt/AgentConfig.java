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
 * AgentConfig.java
 *
 * Created on August 13, 2003, 8:05 PM
 */

package com.sun.enterprise.ee.admin.servermgmt;


import java.util.Properties;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.util.SystemPropertyConstants;

/**
 *
 * @author  kebbs
 */
public class AgentConfig extends RepositoryConfig {
    
    public static final String AGENT_INSTANCE_NAME = "agent";
    
    public static final String K_USER = "agent.user";
    public static final String K_PASSWORD = "agent.password";
    public static final String K_ADMIN_PORT = "agent.adminPort";
    public static final String K_AGENT_ROOT = "agent.root";

    public static final String K_ADMIN_HOST = "agent.adminHost";
    public static final String K_AGENT_PROTOCOL = "agent.protocol";
    public static final String K_AGENT_LOG_LEVEL = "agent.log.level";    
    public static final String K_AGENT_LOG_FILE = "agent.log.file";    
    public static final String K_AGENT_BIND_STATUS = "agent.bind.status";    
    public static final String K_AGENT_POLLING_INTERVAL = "agent.polling.interval";    
    
    public static final String K_DAS_HOST = "agent.das.host";    
    public static final String K_DAS_PROTOCOL = "agent.das.protocol";    
    public static final String K_DAS_PORT = "agent.das.port";    
    public static final String K_DAS_USER = "agent.das.user";    
    public static final String K_DAS_PASSWORD = "agent.das.password";
    public static final String K_MASTER_PASSWORD = "agent.masterpassword";
    public static final String K_NEW_MASTER_PASSWORD = "agent.newmasterpassword";
    public static final String K_SAVE_MASTER_PASSWORD = "agent.saveMasterPassword";
    public static final String K_CLIENT_HOST = "agent.client.host";
    public static final String K_EXTRA_PASSWORDS = "agent.extraPasswords";
    public static final String K_DO_NOT_CONFIRM_SERVER_CERT = "agent.doNotConfirmServerCert";
    public static final String K_DAS_IS_SECURE = "agent.das.isSecure";    
    
    public static final String NODEAGENT_DELETED_STATUS = "DELETED";
    public static final String NODEAGENT_BOUND_STATUS = "BOUND";
    public static final String NODEAGENT_UNBOUND_STATUS = "UNBOUND";
    public static final String NODEAGENT_JMX_DEFAULT_PROTOCOL = "rmi_jrmp";
    public static final String NODEAGENT_DEFAULT_HOST_ADDRESS = "0.0.0.0";
    public static final String NODEAGENT_DEFAULT_DAS_IS_SECURE = "true";
    public static final String NODEAGENT_ATTEMPT_RENDEZVOUS = "attemptRendezvous";
    public static final String NODEAGENT_ATTEMPT_LOCAL_RENDEZVOUS = "attemptLocalRendezvous";
    
    // property constants for AgentConfig contructor    
    public static final String AGENT_LISTEN_ADDRESS_NAME="listenaddress";
    public static final String REMOTE_CLIENT_ADDRESS_NAME="remoteclientaddress";
    public static final String AGENT_JMX_PROTOCOL_NAME="agentjmxprotocol";
    public static final String DAS_JMX_PROTOCOL_NAME="dasjmxprotocol";
    public static final String AGENT_DAS_IS_SECURE="isDASSecure";

    
    public AgentConfig()
    {
        super();
    }
    
    /** Creates a new instance of AgentConfig */
    public AgentConfig(String agentName, String agentRoot, String instanceName)
    {
        super(agentName, agentRoot, instanceName);  
        put(K_AGENT_ROOT, agentRoot);  
    }   
    
    public AgentConfig(String agentName, String agentRoot)
    {
        this(agentName, agentRoot, AGENT_INSTANCE_NAME);
    }     

    /**
     * Constructor used by the create-nodeagent cli command
     */
    public AgentConfig(String agentName, String agentRoot, String adminUser, 
        String adminPassword, Integer adminPort, String DASHost, String DASPort, 
        String DASUser, String DASPassword, String masterPassword, Boolean saveMasterPassword,
        Properties agentProperties) {

        this(agentName, agentRoot);

        // host that remote client will use to communicate with agent
        String remoteClientHost=System.getProperty(SystemPropertyConstants.HOST_NAME_PROPERTY);
        // address actual agent will listen on
        String listenAddress=NODEAGENT_DEFAULT_HOST_ADDRESS;
        // jmx protocol clients will use to connect to agent
        String agentJmxProtocol=NODEAGENT_JMX_DEFAULT_PROTOCOL;
        // jmx protocol agent will use to connect to das
        String dasJmxProtocol=NODEAGENT_JMX_DEFAULT_PROTOCOL;
        // Is connection to DAS secure?
        String dasIsSecure=NODEAGENT_DEFAULT_DAS_IS_SECURE;
        
        if (agentProperties != null) {
            // assign values from passed in properties in they exist
            remoteClientHost=agentProperties.getProperty(REMOTE_CLIENT_ADDRESS_NAME, 
                System.getProperty(SystemPropertyConstants.HOST_NAME_PROPERTY));
            listenAddress=agentProperties.getProperty(AGENT_LISTEN_ADDRESS_NAME, NODEAGENT_DEFAULT_HOST_ADDRESS);
            agentJmxProtocol=agentProperties.getProperty(AGENT_JMX_PROTOCOL_NAME, NODEAGENT_JMX_DEFAULT_PROTOCOL);
            dasJmxProtocol=agentProperties.getProperty(DAS_JMX_PROTOCOL_NAME, NODEAGENT_JMX_DEFAULT_PROTOCOL);
            dasIsSecure=agentProperties.getProperty(AGENT_DAS_IS_SECURE, NODEAGENT_DEFAULT_DAS_IS_SECURE);
        }
        

        put(K_USER, adminUser);
        put(K_PASSWORD, adminPassword);
        put(K_ADMIN_PORT, adminPort);

        put(K_DAS_USER, DASUser);
        put(K_DAS_PASSWORD, DASPassword);
        put(K_DAS_HOST, DASHost);
        put(K_DAS_PORT, DASPort);
        put(K_DAS_PROTOCOL, dasJmxProtocol);
        put(K_MASTER_PASSWORD, masterPassword);
        put(K_SAVE_MASTER_PASSWORD, saveMasterPassword);

        put(K_AGENT_PROTOCOL, agentJmxProtocol);
        put(K_ADMIN_HOST, listenAddress);
        put(K_CLIENT_HOST, remoteClientHost);
        
        put(K_DAS_IS_SECURE, dasIsSecure);
        
        // Set Defaults
        put(K_AGENT_BIND_STATUS, NODEAGENT_UNBOUND_STATUS);
                
    }
    
    
    
    public String getAgentName() {
        return super.getRepositoryName();
    }
    
    public String getAgentRoot()
    {
        return super.getRepositoryRoot();
    }    
}
