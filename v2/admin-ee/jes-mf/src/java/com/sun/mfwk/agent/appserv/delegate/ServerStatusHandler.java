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
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.delegate;

import java.util.HashSet;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import com.sun.mfwk.agent.appserv.mapping.MappingQueryService;
import javax.management.MBeanServerConnection;

import com.sun.mfwk.agent.appserv.connection.ConnectionRegistry;

import java.io.IOException;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import com.sun.enterprise.management.StateManageable;
import com.sun.mfwk.agent.appserv.util.Utils;
import com.sun.mfwk.agent.appserv.util.Constants;


public class ServerStatusHandler extends BaseHandler {

    public ServerStatusHandler() {
        super();
    } 
    
    public Object handleAttribute(ObjectName peer, String attribute,
            MBeanServerConnection mbs)  throws HandlerException, AttributeNotFoundException, 
             MBeanException, ReflectionException, InstanceNotFoundException,
             IOException  {
        try {    
            
            ConnectionRegistry registry = ConnectionRegistry.getInstance();
             MBeanServerConnection connection = 
                    registry.getConnection("server", (String)getMappingQueryService().getProperty(Constants.DOMAIN_NAME_PROP));
             
            Object status =
                connection.invoke(new ObjectName("com.sun.appserv:name=domain-status"),
                "getstate",
                new String[]{peer.getKeyProperty("name")},
                new String[]{"java.lang.String"} );
                int code = ((Integer)status).intValue();
                Utils.log(Level.FINEST, "status = " + code );
                Set s = Collections.synchronizedSet(new HashSet());
                switch (code) {
                    case StateManageable.RUNNING_STATE :
                        s.add(com.sun.cmm.cim.OperationalStatus.OK);
                        break;
                    case StateManageable.STARTING_STATE :
                        s.add(com.sun.cmm.cim.OperationalStatus.STARTING);
                        break;
                    case StateManageable.STOPPING_STATE :
                        s.add(com.sun.cmm.cim.OperationalStatus.STOPPED);
                    case StateManageable.STOPPED_STATE :
                        s.add(com.sun.cmm.cim.OperationalStatus.STOPPING);
                        break;
                    default:
                        s.add(com.sun.cmm.cim.OperationalStatus.UNKNOWN);
                        break;
                }
                return s;
        } catch (Exception ex) {
            throw new HandlerException(ex);
        }
        
    }
}


