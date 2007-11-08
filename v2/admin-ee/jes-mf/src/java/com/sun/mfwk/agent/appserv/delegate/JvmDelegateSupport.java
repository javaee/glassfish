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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;

import javax.management.ObjectName;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


/**
 * Provides the mapping attributes
 */
public class JvmDelegateSupport
       extends AbstractDelegate {

    /** Creates a new instance of JvmDelegateSupport */
    public JvmDelegateSupport() {

        super();
    }

    /**
     * Initialize the JvmDelegateSupport
     * @param params the MbeanServerConnection
     * @throws java.lang.Exception if the initialization could not be done
     */
    public void initialize(Object[] params) throws Exception {
        super.initialize(params);

        try {
            // bind to JVM
            Set set = queryNames(new ObjectName("amx:j2eeType=JVM,J2EEServer=server,*"));
            if ( set.size() != 1 ) {
                System.out.println("Unable to retrieve JVM peer");
            }
            ObjectName JVM_Peer = (ObjectName)set.iterator().next();

            // bind to JVM
            set = queryNames(new ObjectName("amx:j2eeType=X-JVMMonitor,*"));
            if ( set.size() != 1 ) {
                System.out.println("Unable to retrieve X-JVMMonitor peer");
            }
            ObjectName JVMMonitor_Peer = (ObjectName)set.iterator().next();

            /*
             * Build table with attributes that can be mapped
             */
            addMappingEntry("JavaVendor", "javaVendor", JVM_Peer); // java.lang.String
            addMappingEntry("JavaVersion", "javaVersion", JVM_Peer); // java.lang.String
            addMappingEntry("Name", "Name", JVM_Peer); // java.lang.String
            addMappingEntry("Node", "node", JVM_Peer); // java.lang.String
            addMappingEntry("StartTime", "UpTime_StartTime", JVMMonitor_Peer); // long

            /*
             * Build table with default values for attributes that no needs be mapped
             */
            addDefaultMappingEntry("Caption", "AS JVM"); // java.lang.String
            addDefaultMappingEntry("Description", "Java ES Application Server JVM"); // java.lang.String
            addDefaultMappingEntry("ElementName", "AS_JVM"); // java.lang.String
            addDefaultMappingEntry("OtherEnabledState", null); // java.lang.String
            addDefaultMappingEntry("StatesEnabled", Boolean.TRUE); // boolean

            /*
             * Remaining attributes that should fall into one of the two mappings above
             * in order to be compliant with the CMM specification
             */
//            addMappingEntry("EnabledDefault", null); // com.sun.cmm.cim.EnabledDefault
//            addMappingEntry("EnabledState", null); // com.sun.cmm.cim.EnabledState
//            addMappingEntry("EventsEnabled", null); // boolean
//            addMappingEntry("InstallDate", null); // long
//            addMappingEntry("JavaVendorURL", null); // java.lang.String
//            addMappingEntry("LastUpdateTime", null); // long
//            addMappingEntry("LogsEnabled", null); // boolean
//            addMappingEntry("MonitoringEnabled", null); // boolean
//            addMappingEntry("OSArch", null); // java.lang.String
//            addMappingEntry("OSCreationClassName", null); // java.lang.String
//            addMappingEntry("OSName", null); // java.lang.String
//            addMappingEntry("OSVersion", null); // java.lang.String
//            addMappingEntry("OperationalStatus", null); // java.util.Set
//            addMappingEntry("OperationalStatusLastChange", null); // long
//            addMappingEntry("RequestedState", null); // com.sun.cmm.cim.RequestedState
//            addMappingEntry("ServiceTimeEnabled", null); // boolean
//            addMappingEntry("SettingsEnabled", null); // boolean
//            addMappingEntry("SpecName", null); // java.lang.String
//            addMappingEntry("SpecVendor", null); // java.lang.String
//            addMappingEntry("SpecVersion", null); // java.lang.String
//            addMappingEntry("StatisticsEnabled", null); // boolean
//            addMappingEntry("StatusDescriptions", null); // java.util.Set
//            addMappingEntry("TimeOfLastStateChange", null); // long
//            addMappingEntry("VmName", null); // java.lang.String
//            addMappingEntry("VmSpecName", null); // java.lang.String
//            addMappingEntry("VmSpecVendor", null); // java.lang.String
//            addMappingEntry("VmSpecVersion", null); // java.lang.String
//            addMappingEntry("VmVendor", null); // java.lang.String
//            addMappingEntry("VmVersion", null); // java.lang.String
        }
        catch(Exception e) {
            System.err.println("JvmDelegateSupport - constructor : " + e);
        }
    }
}
