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

package com.sun.mfwk.agent.appserv.modeler;


import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.remote.JMXConnector;

import javax.management.j2ee.statistics.*;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.TrustAnyTrustManager;
import com.sun.appserv.management.client.TLSParams;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class MonitorTreeCrawler {

    public static final String USER_PROPERTY = "jmx.user";
    public static final String PASSWORD_PROPERTY = "jmx.password";
    public static final String HOST_PROPERTY = "jmx.host";
    public static final String PORT_PROPERTY = "jmx.port";
    public static final String SECURE_PROPERTY = "jmx.usetls";

    public static void main(String[] args) {

        String user = System.getProperty(USER_PROPERTY);
        if (args.length > 0) {
            user = args[0];
        }
        String password = System.getProperty(PASSWORD_PROPERTY);
        if (args.length > 1) {
            password = args[1];
        }
        String host = System.getProperty(HOST_PROPERTY);
        if (args.length > 2) {
            host = args[2];
        }
        String port = System.getProperty(PORT_PROPERTY);
        if (args.length > 3) {
            port = args[3];
        }
        boolean useTls = Boolean.getBoolean(SECURE_PROPERTY);
        if (args.length > 4) {
            useTls = "true".equalsIgnoreCase(args[4]);
        }
        TLSParams tlsParams = null;
        if (useTls) {
            tlsParams = new TLSParams(TrustAnyTrustManager.getInstanceArray(),
                    null);
        }
        String monitoringRoot = null;
        if (args.length > 5) {
            monitoringRoot = args[5];
        }
        
        AppserverConnectionSource src = new AppserverConnectionSource(
                    AppserverConnectionSource.PROTOCOL_RMI,
                    host, Integer.parseInt(port), user, password, tlsParams,
                    (Map)null);
        try {
            MBeanServerConnection connection = src.getMBeanServerConnection(true);
            if (monitoringRoot == null)
                crawlMonitoringTree(connection);
            else
                crawlMonitoringTree(connection, new ObjectName(monitoringRoot));
        } catch (Exception ex) {
           System.out.println("Exception: " + ex.getMessage());
           ex.printStackTrace();
       }
        
    }
    

    public static void crawlMonitoringTree(MBeanServerConnection connection) throws Exception {
        System.out.println("Getting all data");
        crawlMonitoringTree(connection, new ObjectName("com.sun.appserv:type=http-service,category=monitor,server=server"));
        crawlMonitoringTree(connection, new ObjectName("com.sun.appserv:type=jvm,category=monitor,server=server"));
        crawlMonitoringTree(connection, new ObjectName("com.sun.appserv:type=orb,category=monitor,server=server"));
        crawlMonitoringTree(connection, new ObjectName("com.sun.appserv:type=thread-pools,category=monitor,server=server"));
        crawlMonitoringTree(connection, new ObjectName("com.sun.appserv:type=transaction-service,category=monitor,server=server"));
        crawlMonitoringTree(connection, new ObjectName("com.sun.appserv:type=resources,category=monitor,server=server"));
        crawlMonitoringTree(connection, new ObjectName("com.sun.appserv:type=applications,category=monitor,server=server"));
    }

    
    // crawls the monitoring tree from objectName
    public static void crawlMonitoringTree(MBeanServerConnection connection, ObjectName objectName) throws Exception {
        
        System.out.println("Getting Stats for" + objectName);
        getStats(connection, objectName);
        System.out.println("== End of Stats for == " + objectName);
        
        MBeanInfo mbeanInfo = connection.getMBeanInfo(objectName);
        MBeanOperationInfo[] operations = mbeanInfo.getOperations();
        for (int j = 0; j < operations.length; j++) {
            if (operations[j].getName().equals("getChildren")) {
                System.out.println("Getting Children of : " + objectName);
                ObjectName[] children = (ObjectName[])connection.invoke(objectName, "getChildren", null, null);
                System.out.println("Number of Children of = " + children.length);
                for (int i = 0; i < children.length; i++) {
                    System.out.println("Child : " + children[i]);
                    //getMonitoringData(connection, children[i]);
                }
                
                for (int c = 0; c < children.length; c++) {
                    crawlMonitoringTree(connection, children[c]);
                }
                System.out.println("==============");
                break;
            }
        }
        
        

                
/*            ObjectName[] children = 
                        (ObjectName[]) connection.invoke(name, "getChildren", null, null);
            for (int k = 0; k < children.length; k++) {
                System.out.println("\t" + children[k]);
                getStats(connection, children[k]);
            }
 */
    }
/*
    public static void monitorType1(MBeanServerConnection connection, String objectName) throws Exception {
        ObjectName[] names = (ObjectName[])connection.invoke(
                new ObjectName(objectName), "getChildren", null, null);
        for (int i = 0; i < names.length; i++) {
            System.out.println(names[i]);
            ObjectName name = names[i];
            getStats(connection, names[i]);
            ObjectName[] children = 
                        (ObjectName[]) connection.invoke(name, "getChildren", null, null);
            for (int j = 0; j < children.length; j++) {
                System.out.println("\t" + children[j]);
                getStats(connection, children[j]);
            }
        }
    }
  */  
    public static void getStats(MBeanServerConnection connection, ObjectName name) throws Exception {
            Statistic[] stats = (Statistic[])connection.invoke(name, "getStatistics", null, null);
            if (stats != null) {
                System.out.println("Number of stats = " + stats.length);
                for (int k = 0; k < stats.length; k++) {
                    Statistic stat = stats[k];
                    System.out.println(stat.getName());
                 }
            }
    }
}
