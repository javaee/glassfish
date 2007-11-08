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
 * ServerConnector.java
 */

package com.sun.jbi.jsf.framework.connectors;

import java.io.Serializable;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;

/**
 * @author Sun Microsystems
 *
 */
public abstract class ServerConnector implements Serializable {
    
    protected String password;
    protected String type;
    protected String userName;
    protected String hostName;
    protected String port;
    protected String domainName;
    protected transient MBeanServerConnection connection;
    
    private Logger logger = Logger.getLogger(ServerConnector.class.getName());

    
    public ServerConnector(String hostName, String port, String userName, String password) {
        this.hostName = hostName;
        this.port = port;
        this.userName = userName;
        this.password = password;
        
        // setup server connection
        setupConnection();
    }
    
    public ServerConnector() {
        this(null,null,null,null);
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return this.password;
    }
    
    /**
     * @return Returns the userName.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return Returns the hostName.
     */
    public String getHostName() {
        return hostName;
    }
    
    /**
     * @param hostName The hostName to set.
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    
    /**
     * @return Returns the port.
     */
    public String getPort() {
        return port;
    }
    
    /**
     * @param port The port to set.
     */
    public void setPort(String port) {
        this.port = port;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDomainName() {
        return domainName;
    }

    /**
     * @param domainName    domain name of server
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
    
    /**
     * @return Returns the connection.
     */
    public MBeanServerConnection getConnection() {
        return this.connection;
    }
    
    protected abstract void setupConnection();

}
