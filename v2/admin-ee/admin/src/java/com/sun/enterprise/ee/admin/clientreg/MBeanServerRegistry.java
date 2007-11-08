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
 * NodeAgentRegistry.java
 *
 * Created on September 12, 2003, 3:46 PM
 */
package com.sun.enterprise.ee.admin.clientreg;

import javax.management.MBeanServerConnection;

import com.sun.enterprise.ee.admin.servermgmt.AgentException;

import java.io.IOException;
/**
 * @author  kebbs
 *
 * Class NodeAgentRegistry maintains a list of MBeanServer connections to one
 * or more Node Agents.
 */
public class MBeanServerRegistry extends JMXConnectorRegistry 
{
    private static final String DEFAULT_NAME = "default";      
    
    private MBeanServerConnectionInfo _connectionInfo;
    
    public MBeanServerRegistry(MBeanServerConnectionInfo connectionInfo) 
    {
        super();
        _connectionInfo = connectionInfo;
    }
    
    /**
     * Implementaiton of abstract method to return the system JmxConnector element
     * associated with the given Node Agent.
     */
    protected MBeanServerConnectionInfo findConnectionInfo(String name) 
    {
        return _connectionInfo;
    }
    
    /**
     * Return an MBeanServerConnection to the specifiec node agent.
     */
    public synchronized MBeanServerConnection getMBeanServerConnection()
        throws AgentException 
    {
        return getMBeanServerConnection(DEFAULT_NAME);
    }
    
    public synchronized MBeanServerConnection getMBeanServerConnection(String name)
        throws AgentException 
    {
        return getConnection(name);
    }
    
    public synchronized void removeMBeanServerConnection() 
    {
        removeMBeanServerConnection(DEFAULT_NAME);
    }
    
    /**
     * remove an MBeanServerConnection to the specifiec node agent from the cache
     */
    public synchronized void removeMBeanServerConnection(String name) {
        try {
            removeConnectorFromCache(name);
        } catch (IOException e) {
            // don't care about the exception
        }
    }
    
}
