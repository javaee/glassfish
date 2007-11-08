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
 * DASPropertyReader.java
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

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;

import javax.management.remote.JMXServiceURL;
import com.sun.enterprise.admin.jmx.remote.server.rmi.JmxServiceUrlFactory;

/**
 *
 * @author  kebbs
 */
public class DASPropertyReader {  

    private Properties _properties;
    
    private File _propertyFileDir;
    
    private static final StringManager _strMgr = 
        StringManager.getManager(DASPropertyReader.class);
    
    private static final String DAS_PROPERTY_FILE_NAME = "das.properties";
    
    public DASPropertyReader (RepositoryConfig config)
    {       
        AgentConfig agentConfig = new AgentConfig(config.getRepositoryName(), 
            config.getRepositoryRoot());
        _propertyFileDir = (new EEFileLayout(agentConfig)).getConfigRoot();       
        _properties = new Properties();
        final String dasHost = (String)config.get(AgentConfig.K_DAS_HOST);
        if (dasHost != null) {
            _properties.setProperty(AgentConfig.K_DAS_HOST, dasHost);            
            _properties.setProperty(AgentConfig.K_DAS_PORT, 
                (String)config.get(AgentConfig.K_DAS_PORT));                  
            _properties.setProperty(AgentConfig.K_DAS_PROTOCOL, 
                (String)config.get(AgentConfig.K_DAS_PROTOCOL));              
            String isSecure = (String)config.get(AgentConfig.K_DAS_IS_SECURE);
            if (isSecure == null) {
                isSecure = "true";
            }
            _properties.setProperty(AgentConfig.K_DAS_IS_SECURE, isSecure);
        }
    }
   
    public String getJMXURL() {
        //return "service:jmx:" + getProtocol() + "://" + 
            //getHost() + ":" + getPort();
        // Note that this is the client side url
        try {
            final JMXServiceURL url = 
            JmxServiceUrlFactory.forRmiWithJndiInAppserver(getHost(),  Integer.parseInt(getPort()));
            return ( url.toString() );
        }
        catch (final Exception e) {
            throw new RuntimeException (e);
        }
    }
    
    public String isDASSecure() {
        return _properties.getProperty(AgentConfig.K_DAS_IS_SECURE);
    }    
    public void setIsDASSecure(String secure) {
        _properties.setProperty(AgentConfig.K_DAS_IS_SECURE, secure);
    }
    public String getProtocol() {
        return _properties.getProperty(AgentConfig.K_DAS_PROTOCOL);
    }
    public void setProtocol(String protocol) {
        _properties.setProperty(AgentConfig.K_DAS_PROTOCOL, protocol);
    }       
   
    public String getHost()  {
        return _properties.getProperty(AgentConfig.K_DAS_HOST);
    }
    public void setHost(String host) {
        _properties.setProperty(AgentConfig.K_DAS_HOST, host);
    }
    
    public String getPort() {
        return _properties.getProperty(AgentConfig.K_DAS_PORT);
    }
    public void setPort(String port) {
        _properties.setProperty(AgentConfig.K_DAS_PORT, port);
    }  

    public void clearProperties() {        
        _properties.remove(AgentConfig.K_DAS_HOST);
        _properties.remove(AgentConfig.K_DAS_PORT);
        _properties.remove(AgentConfig.K_DAS_PROTOCOL);
    }
    
    
    //There is an implicit assumption that the 
    public void write() throws IOException {        
        FileOutputStream fos = new FileOutputStream(new File(_propertyFileDir, 
            DAS_PROPERTY_FILE_NAME));
        _properties.store(fos, _strMgr.getString("dasPropertyFileComment"));
        fos.close();        
    }

    public void read() throws IOException, FileNotFoundException {
        _properties.load(new FileInputStream(new File(_propertyFileDir, DAS_PROPERTY_FILE_NAME)));
    }
}
