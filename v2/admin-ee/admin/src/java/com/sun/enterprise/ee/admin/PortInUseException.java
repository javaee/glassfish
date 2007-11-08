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

import java.util.ArrayList;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * The PortInUseException represents a collection of port that are in use.
 */
public class PortInUseException extends Exception
{        
    //Holds the list of ports that are in use. Element are of type PortInUse
    protected ArrayList _inUse = new ArrayList();          
        
    protected static final StringManager _strMgr = 
        StringManager.getManager(PortInUseException.class);
    
    public PortInUseException()
    {
        super();
    }

    /**
     * Creates a PortInUseException which represents a system-property port conflict in domain.xml.
     * @param conflictingPropertyName The system property name with which the port conflicts. If this is
     * null, then this is an indication that the port is actually in use. When not null, this
     * indicates a conflict with the specified property in domain.xml.
     * @param serverName The server instance name with the conflicting port
     * @param hostName The host name where the port resides
     * @param port The port that is in use or conlicts with another port in domain.xml.
     * @param propertyName The system propert name containing the conflicting port.
     */    
    public PortInUseException(String serverName, String hostName, int port, 
        String propertyName, String conflictingPropertyName)
    {
        this();               
        addConflictingPort(new PortInUse(propertyName, port, hostName, serverName, conflictingPropertyName));
    }    
    
    /**
     * Creates a PortInUse which represents a port conflict due to a port in use 
     * by another process.
     * @param hostName
     * @param port
     * @param propertyName
     */    
    public PortInUseException(String hostName, int port, String propertyName)
    {
        this();                   
        addConflictingPort(new PortInUse(propertyName, port, hostName));
    }    
    
    /**
     * Adds a conflicting port to the list of conflicting ports
     */    
    private void addConflictingPort(PortInUse port)
    {       
        if (!portAlreadyConflicts(port.getPort())) {            
            _inUse.add(port);
        }
    }
    
    /**
     * Adds the conflicting ports from the given exception. This allows the exception 
     * to be augmented so that it contains multiple conflicting ports.
     * @param ex The exception whose conflicting ports are to be added to this exception.
     */    
    public void augmentException(PortInUseException ex)
    {
        final ArrayList conflictingPorts = ex.getConflictingPorts();        
        final int numPorts = conflictingPorts.size();        
        for (int i = 0; i < numPorts; i++) {
            addConflictingPort((PortInUse)conflictingPorts.get(i));
        }
    }
    
    /**
     * 
     * @param port port number
     * @return true if the port already conflicts (i.e. if it is in our list of 
     * conflicting ports.
     */    
    public boolean portAlreadyConflicts(int port)
    {
        final PortInUse portInUse = new PortInUse(null, port, null);
        return _inUse.contains(portInUse);
    }
       
    /**
     *
     * @return This list of conflicting ports (i.e. of type PortInUse).
     */    
    public ArrayList getConflictingPorts() 
    {
        return _inUse;
    }       
    
    public String toString()
    {
        return getMessage();
    }
    
    public String getLocalizedMessage()
    {
        return getMessage();
    }
    
    /**
     * Formats a string of all the conflicting ports. A port will be "conflicting" for
     * one of two reasons: 1)The port is actually in use (i.e. someone's listening) OR
     * 2)The port is being used by another server instance with the same node agent 
     * in domain.xml
     * @return
     */    
    public String getMessage() {
        PortInUse portInUse;
        String result = "";
        final int numPorts = _inUse.size();
        for (int i = 0; i < numPorts; i++) {            
            portInUse = (PortInUse)_inUse.get(i);                  
            if (portInUse.getConflictingPropertyName() == null) {
                result += _strMgr.getString("portInUse",
                    new Integer(portInUse.getPort()), portInUse.getHostName(), portInUse.getPropertyName());
            } else {                               
                result += _strMgr.getString("portNotUnique",
                    new Object[] {  new Integer(portInUse.getPort()), portInUse.getHostName(), 
						portInUse.getPropertyName(), portInUse.getServerName(), 
                        portInUse.getConflictingPropertyName()} );                
            }
            if (i < numPorts - 1) {
                result += "\n";
            }
        }
        return result;
    }
}
