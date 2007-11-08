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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.mfwk.agent.appserv.discovery;

import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

import com.sun.mfwk.agent.appserv.util.Constants;
import com.sun.mfwk.agent.appserv.logging.LogDomains;

class ASMBeanDiscoveryServiceImpl implements ASMBeanDiscoveryService {
    
    
    public ASMBeanDiscoveryServiceImpl(MBeanServerConnection connection) {
        this.connection = connection;
    }
    
    public Set discoverASMBeans()  throws Exception {
        ObjectName monitorPattern = new ObjectName(Constants.MONITOR_PATTERN);
        Set set = connection.queryNames(monitorPattern, null);
        
        ObjectName runtimePattern = new ObjectName(Constants.J2EE_RUNTIME_PATTERN);
        set.addAll(connection.queryNames(runtimePattern, null));

        ObjectName jvmRuntimePattern = new ObjectName(Constants.JVM_RUNTIME_PATTERN);
        set.addAll(connection.queryNames(jvmRuntimePattern, null));
        
        ObjectName clusterPattern = new ObjectName(Constants.CLUSTER_PATTERN);
        set.addAll(connection.queryNames(clusterPattern, null));
        
        ObjectName instanceRefPattern = new ObjectName(Constants.INSTANCE_REF_PATTERN);
        set.addAll(connection.queryNames(instanceRefPattern, null));
        
        return set;
    }

    public Set discoverASMBeans(String server)  throws Exception {
        ObjectName monitorPattern = 
            new ObjectName(Constants.MONITOR_PATTERN + "," + 
                Constants.SERVER_NAME_KEY + "=" + server);
        Set set = connection.queryNames(monitorPattern, null);
        
        ObjectName runtimePattern = 
            new ObjectName(Constants.J2EE_RUNTIME_PATTERN + "," +
                Constants.NAME_KEY + "=" + server);
        set.addAll(connection.queryNames(runtimePattern, null));

        ObjectName jvmRuntimePattern = 
            new ObjectName(Constants.JVM_RUNTIME_PATTERN + "," +
                Constants.J2EE_SERVER_KEY+ "=" + server);
        set.addAll(connection.queryNames(jvmRuntimePattern, null));
        
        ObjectName clusterPattern = 
            new ObjectName(Constants.CLUSTER_PATTERN);
        set.addAll(connection.queryNames(clusterPattern, null));
        
        return set;
    }

    public Set discoverASMBeans(ObjectName root) throws Exception {
        HashSet mbeans = new HashSet();
        return discover(root, mbeans);
    }

    public Set discoverClusterMBeans()  throws Exception {
        ObjectName clusterPattern = new ObjectName(Constants.CLUSTER_PATTERN);
        return connection.queryNames(clusterPattern, null);
    }

    private Set discover(ObjectName objectName, HashSet mbeans) throws Exception  {
        mbeans.add(objectName);
        MBeanInfo mbeanInfo = connection.getMBeanInfo(objectName);
        MBeanOperationInfo[] operations = mbeanInfo.getOperations();
        for (int j = 0; j < operations.length; j++) {
            if (operations[j].getName().equals("getChildren")) {
                ObjectName[] children = (ObjectName[])connection.invoke(objectName, "getChildren", null, null);
                for (int i = 0; i < children.length; i++) {
                    discover(children[i], mbeans);
                }
                break;
            }
        }
        return mbeans;
    }
    
    MBeanServerConnection connection;
    
}
