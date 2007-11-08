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
 * MBeanServerConnectionInfo.java
 *
 * Created on October 9, 2003, 12:04 PM
 */

package com.sun.enterprise.ee.admin.clientreg;

import java.io.Serializable;
import java.io.IOException;

import com.sun.enterprise.admin.util.JMXConnectorConfig;
import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;

import com.sun.enterprise.security.store.IdentityManager;

import javax.management.remote.JMXConnector;
import javax.management.MBeanServerConnection;


/**
 *
 * @author  kebbs
 */
public class MBeanServerConnectionInfo extends JMXConnectorConfig implements Serializable {
    
    public static final long NOT_YET_CONNECTED = 0;
    
    private transient JMXConnector _connector;
    
    //keeps track of the time we successfully connected
    private long _lastConnectTime = NOT_YET_CONNECTED;
        
    /** Creates a new instance of MBeanServerConnectionInfo */
    public MBeanServerConnectionInfo() {
        super();
        _lastConnectTime = NOT_YET_CONNECTED;        
    }
    
    public MBeanServerConnectionInfo(DASPropertyReader dasReader)
    {
        this(dasReader.getHost(), dasReader.getPort(), dasReader.getProtocol(), 
            IdentityManager.getUser(), IdentityManager.getPassword());
    }
    
    public MBeanServerConnectionInfo(String host, String port, String protocol,
        String user, String password)
    {                
        super(host, port, user, password, protocol);
        _connector = null;
    }      
    
    public MBeanServerConnectionInfo(JMXConnectorConfig config)
    {
        this(config.getHost(), config.getPort(), config.getProtocol(), 
            config.getUser(), config.getPassword());
    }
    
    protected JMXConnector getJMXConnector()
    {
        return _connector;
    }
    
    protected void setJMXConnector(JMXConnector connector)
    {
        _connector = connector;
    }
    
    protected MBeanServerConnection getMBeanServerConnection() 
        throws IOException
    {
        return getJMXConnector().getMBeanServerConnection();
    }
    
    protected void setLastConnectTime(long time)
    {
        _lastConnectTime = time;
    }
    
    protected long getLastConnectTime()
    {
        return _lastConnectTime;
    }
    
    protected boolean isConnected()
    {
        return getJMXConnector() == null ? false : true;
    }
        
}
