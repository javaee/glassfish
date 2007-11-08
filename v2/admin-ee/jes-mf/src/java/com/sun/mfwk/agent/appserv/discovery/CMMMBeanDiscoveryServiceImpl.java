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

package com.sun.mfwk.agent.appserv.discovery;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Collection;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.sun.mfwk.agent.appserv.logging.LogDomains;
import com.sun.mfwk.agent.appserv.util.Constants; 
import java.util.logging.Logger;
import java.util.logging.Level;
        
class CMMMBeanDiscoveryServiceImpl implements CMMMBeanDiscoveryService {
    
    
    public CMMMBeanDiscoveryServiceImpl(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    
    public Set discoverCMMMBeans() throws Exception   {
        HashSet mbeans = new HashSet();
        //FIXME - not yet implemented 
        return mbeans;
    }

    public Set discoverCMMMBeans(String serverName, String domainName) 
            throws Exception   {

        HashSet mbeans = new HashSet();

        // all mbeans with server=<serverName>
        String serverPattern = Constants.DEF_MODULE_NAME 
                             + ":" + "*" + "," + SERVER_KEY + "=" + serverName
                             + "," + DOMAIN_KEY + "=" + domainName;

        ObjectName sp = new ObjectName(serverPattern);
        mbeans.addAll(mbeanServer.queryNames(sp, null));


        // mbean with name=<serverName>
        String runtimePattern = Constants.DEF_MODULE_NAME 
                             + ":" + "*" + "," + NAME_KEY + "=" + serverName
                             + "," + DOMAIN_KEY + "=" + domainName;

        ObjectName rp = new ObjectName(runtimePattern);
        Set runtimeObjects = mbeanServer.queryNames(rp, null); 

        //filter out the objects that contain SERVER_KEY={...} 
        HashSet filteredRuntimeObjects = new HashSet();
        Iterator iterator = runtimeObjects.iterator();
        ObjectName runtimeObjectName = null;
        while(iterator.hasNext()) {
            runtimeObjectName = (ObjectName) iterator.next();
            try {
                if(null == runtimeObjectName.getKeyProperty(SERVER_KEY)) {   
                    filteredRuntimeObjects.add(runtimeObjectName);
                }
            } catch (NullPointerException npe) {
                //Key is present in the objectname but its value is null; continue.
            } 
        } 

        mbeans.addAll(filteredRuntimeObjects);
        

        // mbean with name=<domainName>
        String domainPattern = Constants.DEF_MODULE_NAME 
                             + ":" + "*" + "," + NAME_KEY + "=" + domainName;

        ObjectName dp = new ObjectName(domainPattern);
        mbeans.addAll(mbeanServer.queryNames(dp, null));
        
        return mbeans;
    }
    
    
    public Set discoverClusterCMMMBeans() throws Exception   {
        String clusterPattern = Constants.DEF_MODULE_NAME 
                             + ":" + "*" + "," + TYPE + "=" + CLUSTER;
        return mbeanServer.queryNames(new ObjectName(clusterPattern), null);        
    }


    public Set discoverInstalledProductCMMMBeans() throws Exception   {
        String installedProductPattern = Constants.DEF_MODULE_NAME 
                             + ":" + "*" + "," + TYPE + "=" + INSTALLED_PRODUCT;
        return mbeanServer.queryNames(new ObjectName(installedProductPattern), null);        
    }

    public Set discoverCMMMBeans(ObjectName objectName) throws Exception {

        HashSet mbeans = new HashSet();
        mbeans.add(objectName);
        String type = objectName.getKeyProperty(TYPE);
        String key = getKey(type);

        if (key != null) {
            String name = objectName.getKeyProperty(NAME);

            // server and domain keys are mandatory part of cmm mbean name
            String serverName = objectName.getKeyProperty(SERVER);
            String domainName = objectName.getKeyProperty(DOMAIN);

            String patternString = Constants.DEF_MODULE_NAME +
                ":" + "*" + "," + key + "=" + name
                    + "," + SERVER_KEY + "=" + serverName
                    + "," + DOMAIN_KEY + "=" + domainName;

            ObjectName pattern = new ObjectName(patternString);

            mbeans.addAll(mbeanServer.queryNames(pattern, null));
        }
        return mbeans;
    }

    public ObjectName discoverCMMMBean(String name) throws Exception {
        ObjectName objectName = new ObjectName(name);
        if(mbeanServer.isRegistered(objectName)) {
            return objectName;
        } else {
            return null;
        }
    }

    private String getKey(String cmmType){
        if(APPLICATION.equals(cmmType)) {
            return APPLICATION_KEY;
        }
        if(STANDALONE_WEB_MODULE.equals(cmmType)) {
            return STANDALONE_WEB_MODULE_KEY;
        }
        if(STANDALONE_EJB_MODULE.equals(cmmType)) {
            return STANDALONE_EJB_MODULE_KEY;
        }
        return null;
    }


    MBeanServer mbeanServer;
    public static final String TYPE = "type";    
    public static final String APPLICATION = "CMM_J2eeApplication";
    public static final String APPLICATION_KEY = "application";
    public static final String STANDALONE_WEB_MODULE = "CMM_J2eeWebModule";
    public static final String STANDALONE_WEB_MODULE_KEY = "standalone-web-module";
    public static final String STANDALONE_EJB_MODULE = "CMM_J2eeEJBModule";
    public static final String STANDALONE_EJB_MODULE_KEY = "standalone-ejb-module";
    public static final String DOMAIN_KEY = "domain";
    public static final String SERVER_KEY = "server";
    public static final String NAME_KEY = "name";
    public static final String NAME = "name";
    public static final String SERVER = "server";
    public static final String DOMAIN = "domain";
    public static final String CLUSTER = "CMM_J2eeCluster";
    public static final String INSTALLED_PRODUCT= "CMM_InstalledProduct";

}
