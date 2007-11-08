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

package com.sun.enterprise.management.model;

import javax.management.*;
import java.util.*;

import com.sun.enterprise.instance.ServerManager;
import com.sun.enterprise.instance.InstanceDefinition;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.appserv.server.util.Version;
import com.sun.enterprise.admin.event.AdminEventCache;
import com.sun.enterprise.admin.server.core.channel.AdminChannel;
import com.sun.enterprise.admin.server.core.channel.RMIClient;
import com.sun.enterprise.server.PEMain;

//Config imports
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.JmsService;
import com.sun.enterprise.config.serverbeans.JmsHost;

// JMS util imports
import com.sun.enterprise.jms.IASJmsUtil;

//JMS SPI imports
import com.sun.messaging.jmq.jmsspi.JMSAdmin;
import com.sun.messaging.jmq.jmsspi.JMSAdminFactory;

//import com.sun.enterprise.management.util.J2EEManagementObjectUtility;
//import com.sun.enterprise.tools.admin.RealmTool;

public class J2EEServerMdl extends J2EEManagedObjectMdl {
    
    /* The vendor information for this server. */
    private String serverVendor = "Sun Microsystems, Inc.";
    
    /* The server version number. */
    private String serverVersion = "9.1";
    
    /* The managed object type. */
    private static String MANAGED_OBJECT_TYPE = "J2EEServer";
    
    /* start time */
    private long startTime = now();

    /* The debug string system property. */
    private static String DEBUG_SYS_PROPERTY = "com.sun.aas.jdwpOptions";
    
    public J2EEServerMdl() {
        /*
        super(System.getProperty(com.sun.enterprise.server.J2EEServer.J2EE_APPNAME, "j2ee") +
            System.getProperty(com.sun.enterprise.server.J2EEServer.J2EE_SERVER_ID_PROP, "100"), false, false, false);
        //this.serverName = System.getProperty(com.sun.enterprise.server.J2EEServer.J2EE_APPNAME) + System.getProperty(com.sun.enterprise.server.J2EEServer.J2EE_SERVER_ID_PROP);
         */
        super("j2ee100", true, false, false);
    }
    
    public J2EEServerMdl(String serverName, String version) {
        super(serverName, serverName, true, false, false);
        this.serverVersion = version;
    }

    //constructor for generic instantiation from the MBeanRegistry
    public J2EEServerMdl(String[] location) {
        this(location[1], "8.0" /*FIXME: use constant*/); 
    }

/** 
* A list of all applications deployed on this J2EEServer. 
* @supplierCardinality 0..* 
*/ 
    public String[] getdeployedObjects(){
        Set apps = findNames("j2eeType=J2EEApplication,J2EEServer=" + getJ2EEServer());
        apps.addAll(findNames("j2eeType=EJBModule,J2EEServer=" + getJ2EEServer()));
        apps.addAll(findNames("j2eeType=WebModule,J2EEServer=" + getJ2EEServer()));
        apps.addAll(findNames("j2eeType=ResourceAdapterModule,J2EEServer=" + getJ2EEServer()));
        apps.addAll(findNames("j2eeType=AppClientModule,J2EEServer=" + getJ2EEServer()));

        Iterator it = apps.iterator();
        String [] deployed = new String[apps.size()];
        int i =0;
        while(it.hasNext()) {
            deployed[i++] = ((ObjectName)it.next()).toString();
        }
        return deployed;
    }

/** 
* A list of resources available to this server. 
* @supplierCardinality 0..* 
*/ 
    public String[] getresources() {
        Set res = findNames("j2eeType=JCAResource,J2EEServer=" + getJ2EEServer());
        res.addAll(findNames("j2eeType=JavaMailResource,J2EEServer=" + getJ2EEServer()));
        res.addAll(findNames("j2eeType=JDBCResource,J2EEServer=" + getJ2EEServer()));
        res.addAll(findNames("j2eeType=JMSResource,J2EEServer=" + getJ2EEServer()));
        res.addAll(findNames("j2eeType=JNDIResource,J2EEServer=" + getJ2EEServer()));
        res.addAll(findNames("j2eeType=JTAResource,J2EEServer=" + getJ2EEServer()));
        res.addAll(findNames("j2eeType=RMI_IIOPResource,J2EEServer=" + getJ2EEServer()));
        res.addAll(findNames("j2eeType=URLResource,J2EEServer=" + getJ2EEServer()));
        res.addAll(findNames("j2eeType=AdminObjectResource,J2EEServer=" + getJ2EEServer()));
        Iterator it = res.iterator();
        String [] resources = new String[res.size()];
        int i =0;
        while(it.hasNext()) {
            resources[i++] = ((ObjectName)it.next()).toString();
        }
        return resources;
    }

/** 
* A list of nodes that this J2EEServer spans. 
* @supplierCardinality 1..* 
*/ 
    public String[] getnodes(){
        try {
        	return new String [] { (java.net.InetAddress.getLocalHost()).toString() };
        } catch(Exception e) {
            return new String[0];
        }
    }

/** 
* A list of all Java virtual machines on which this J2EEServer has running threads. 
* @supplierCardinality 0..* 
*/ 
    public String[] getjavaVMs() {
        Set vms = findNames("j2eeType=JVM");

        Iterator it = vms.iterator();
        String [] jvms = new String[vms.size()];
        int i =0;
        while(it.hasNext()) {
            jvms[i++] = ((ObjectName)it.next()).toString();
        }
        return jvms;
    }

	/**
	* Identifies the J2EE platform vendor of this J2EEServer. The value of serverVendor is specified by the vendor.
	*/
    public String getserverVendor(){
        return serverVendor;
    }

    /**
	* Identifies the J2EE platform version of this J2EEServer. The value of serverVersion is specified by the vendor.
	*/
    public String getserverVersion() {
        return Version.getVersion();
    }
    /**
     * The type of the J2EEManagedObject as specified by JSR77. The class that implements a specific type must override this method and return the appropriate type string.
     */
    public String getj2eeType() {
        return MANAGED_OBJECT_TYPE;
    }
    
    /**
     * The name of the J2EEManagedObject. All managed objects must have a unique name within the context of the management
     * domain. The name must not be null.
     */
    public String getobjectName() {
        Set s = findNames("j2eeType="+getj2eeType()+",name="+getJ2EEServer());
        Object [] objs = s.toArray();
        if (objs.length > 0) {
        	String name = ((ObjectName)objs[0]).toString();
        	return name;
        } else {
            return null;
        }
    }
    
    /**
     * start time for the server instance
     */
    public long getstartTime(){
        return this.startTime;
    }
    
    /**
     * Starts the server instance.
     */
    public void start() {
        
    }
    
    /**
     * Starts the server instance.
     */
    public void startRecursive() {
        start();
    }
    
    /**
     * Stops the server instance.
     */
    public void stop() {
        PEMain.shutdown();
    }
    
    /**
     * Returns the debug port for this instance.
     */
    public String getdebugPort() {
        String debug = java.lang.System.getProperty(DEBUG_SYS_PROPERTY);
        int nameIndex;
        if ( debug!=null && (nameIndex = debug.indexOf("address")) != -1 ) {
            String value = debug.substring(nameIndex + "address".length() + 1);
            int commaIndex;
            if ( (commaIndex = value.indexOf(",")) != -1 ) {
                value = value.substring(0, commaIndex);
            }
            return value;
        }
        return null;
    }
    
    /**
     * Is instance restart required. Restart is required if dynamic
     * reconfiguration on the instance could not be dones and the user has
     * not restarted the instance since then.
     * 
     * @deprecated Use runtime status object to runtime status
     */
    public boolean isrestartRequired() {
        String instanceId = AdminService.getAdminService().getInstanceName();
        RMIClient rc = AdminChannel.getRMIClient(instanceId);

        return rc.isRestartNeeded();
    }


}
