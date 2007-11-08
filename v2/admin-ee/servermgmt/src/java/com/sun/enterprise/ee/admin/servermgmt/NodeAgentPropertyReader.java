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
 * NodeAgentPropertyReader.java
 *
 * Created on August 24, 2003, 8:15 PM
 */

package com.sun.enterprise.ee.admin.servermgmt;

import java.util.Properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;

import com.sun.enterprise.ee.admin.servermgmt.EEFileLayout;
/**
 *
 * @author  kebbs
 */
public class NodeAgentPropertyReader {  

    private Properties _properties;
    
    private File _propertyFileDir;
    
    private static final StringManager _strMgr = StringManager.getManager(NodeAgentPropertyReader.class);
    
    private static final String NODEAGENT_PROPERTY_FILE_NAME = "nodeagent.properties";
    
    public NodeAgentPropertyReader (RepositoryConfig config)
    {       
        AgentConfig nodeAgentConfig = new AgentConfig(config.getRepositoryName(), config.getRepositoryRoot());
        _propertyFileDir = (new EEFileLayout(nodeAgentConfig)).getConfigRoot();       
        _properties = new Properties();
        
        final String host = (String)config.get(AgentConfig.K_ADMIN_HOST);
    
        if (host != null) {              
            //_properties.setProperty(AgentConfig.AGENT_INSTANCE_NAME, config.getRepositoryName());
            _properties.setProperty(AgentConfig.K_AGENT_ROOT, config.getRepositoryRoot());            
            _properties.setProperty(AgentConfig.K_ADMIN_PORT, ((Integer)config.get(AgentConfig.K_ADMIN_PORT)).toString());
            _properties.setProperty(AgentConfig.K_AGENT_PROTOCOL, (String)config.get(AgentConfig.K_AGENT_PROTOCOL));                   
            _properties.setProperty(AgentConfig.K_ADMIN_HOST, (String)config.get(AgentConfig.K_ADMIN_HOST));
            _properties.setProperty(AgentConfig.K_CLIENT_HOST, (String)config.get(AgentConfig.K_CLIENT_HOST));                   
            _properties.setProperty(AgentConfig.K_AGENT_BIND_STATUS, (String)config.get(AgentConfig.K_AGENT_BIND_STATUS));                   
            String doNotConfirm = (String)config.get(AgentConfig.K_DO_NOT_CONFIRM_SERVER_CERT);
            if (doNotConfirm == null) {
                doNotConfirm = "true";
            }
            _properties.setProperty(AgentConfig.K_DO_NOT_CONFIRM_SERVER_CERT, doNotConfirm);
        }
    }
   
    public String getJMXURL() {
        return "service:jmx:" + getProtocol() + "://" + getHost() + ":"  + getPort();
    }       

    public String getHost() {
        return _properties.getProperty(AgentConfig.K_ADMIN_HOST);
    }
    public void setHost(String host) {
        _properties.setProperty(AgentConfig.K_ADMIN_HOST, host);
    }

    public String getPort() {
        return _properties.getProperty(AgentConfig.K_ADMIN_PORT);
    }
    public void setPort(String port) {
        _properties.setProperty(AgentConfig.K_ADMIN_PORT, port);
    }
    
    public String getProtocol()  {
        return _properties.getProperty(AgentConfig.K_AGENT_PROTOCOL);
    }
    public void setProtocol(String protocol)  {
        _properties.setProperty(AgentConfig.K_AGENT_PROTOCOL, protocol);
    }

    public String getBindStatus()  {
        return _properties.getProperty(AgentConfig.K_AGENT_BIND_STATUS);
    }
    public void setBindStatus(String bStatus)  {
        _properties.setProperty(AgentConfig.K_AGENT_BIND_STATUS, bStatus);
    }

    public String getClientHost()  {
        return _properties.getProperty(AgentConfig.K_CLIENT_HOST);
    }
    public void setClientHost(String host) {
        _properties.setProperty(AgentConfig.K_CLIENT_HOST, host);
    }
        
    public boolean isBound() {
       return getBindStatus().equals(AgentConfig.NODEAGENT_BOUND_STATUS);
    }
    public boolean isDeleted() {
       return getBindStatus().equals(AgentConfig.NODEAGENT_DELETED_STATUS);
    }
   
    public String doNotConfirmServerCert()
    {
        return _properties.getProperty(AgentConfig.K_DO_NOT_CONFIRM_SERVER_CERT);
    }    
    public void setDoNotConfirmServerCert(String confirm)
    {
        _properties.setProperty(AgentConfig.K_DO_NOT_CONFIRM_SERVER_CERT, confirm);
    }
    
    //There is an implicit assumption that the 
    public void write() throws IOException {        
        FileOutputStream fos = new FileOutputStream(new File(_propertyFileDir, 
            NODEAGENT_PROPERTY_FILE_NAME));
        _properties.store(fos, 
            _strMgr.getString("nodeAgentPropertyFileComment"));
        fos.close();
    }

    public void read() throws IOException, FileNotFoundException {
        _properties.load(new FileInputStream(new File(_propertyFileDir, NODEAGENT_PROPERTY_FILE_NAME)));
    }
}

