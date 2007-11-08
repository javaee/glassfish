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

package com.sun.enterprise.ee.admin;

import java.io.Serializable;

/**
 * Represents a port in use / conflicting port. There are two basic cases:
 * 1) The port is in use by another process.
 * 2) The port is not in use by another proces, but conflicts in domain.xml (and will 
 * conflict when instances are started).
 *
 * In addition a conflicting port may optionally be resolved to a non-conflicting port. 
 * If this is the case then 
 <b>NOT THREAD SAFE (mutable variables)</b>
 */
public final class PortInUse implements Serializable {
   
    public static final int NO_NEW_PORT = -1;
    
    private final String _propertyName;
    private final int _port;
    private final String _hostName;
    private final String _serverName; 
    private final String _conflictingPropertyName;    
    private int _newPort = NO_NEW_PORT;

    /**
     * Creates a PortInUse which represents a system-property port conflict in domain.xml.
     * @param propertyName -- the name of the system-property element containing the port
     * @param port -- the port number that conflicts
     * @param hostName -- the host name of the conflicting port
     * @param serverName -- the server instance name which owns the conflicting port.
     * @param conflictingPropertyName -- the name of tye system-property element which
     * conflicts with the given port.
     */    
    public PortInUse(String propertyName, int port, String hostName, 
        String serverName, String conflictingPropertyName) 
    {
        _propertyName = propertyName;
        _port = port;
        _hostName = hostName;
        _serverName = serverName;
        _conflictingPropertyName = conflictingPropertyName;
        _newPort = NO_NEW_PORT;
    }

    /**
     * Creates a PortInUse which represents a port conflict due to a port in use 
     * by another process.
     * @param propertyName -- the name of the system-property element containing the port
     * @param port -- the port number that conflicts
     * @param hostName -- the host name of the conflicting port
     */    
    public PortInUse(String propertyName, int port, String hostName)
    {
        this(propertyName, port, hostName, null, null);
    }
    
    /**
     * Creates a PortInUse which represents an invalid port
     * @param propertyName -- the name of the system-property element containing the port     
     */    
    public PortInUse(String propertyName)
    {
        this(propertyName, NO_NEW_PORT, null, null, null);
    }

    /**     
     * @return The non-conflicting port. The value NO_NEW_PORT will be returned if the port
     * conflict has not been resolved.
     */    
    public int getNewPort()
    {
        return _newPort;
    }
    
    public void setNewPort(int port) 
    {
        _newPort = port;
    }
    
    public int getPort()
    {
        return _port;
    }
    
    public String getHostName()
    {
        return _hostName;
    }
    
    public String getPropertyName()
    {
        return _propertyName;
    }
    
    public String getServerName()
    {
        return _serverName;
    }
    
    public String getConflictingPropertyName()
    {
        return _conflictingPropertyName;
    }
    
    @Override
    public String toString()
    {
        String result = "conflicting port " + getPropertyName() + "=" + getPort() + " host " + getHostName();
        if (getConflictingPropertyName() != null) {
            result += " conflicts with " + getServerName() + " " + getConflictingPropertyName();
        }
        if (getNewPort() >= 0) {
            result += " changed to " + getNewPort();
        }
        return result;
    }
    
    /**
     * Equality comparision is based on port number.
     * @param obj
     * @return
     */    
    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof PortInUse && _port == ((PortInUse)obj)._port) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return  _port;
                
     /* probably ought to be as follows, but equals() was originally
        written to use *only* the port.
        
        private static int
    hashIt( final Object o ) {
        return o == null ? 0 : o.hashCode();
    }    


        return  _port ^ _newPort ^
                hashIt( _propertyName ) ^
                hashIt( _hostName ) ^
                hashIt( _serverName ) ^
                hashIt( _conflictingPropertyName );
     */
    }

}
