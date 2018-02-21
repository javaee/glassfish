/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.s1asdev.admin.mbeanapi.monitoring.web;

import java.io.IOException;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;

import com.sun.appserv.management.monitor.ApplicationMonitor;
import com.sun.appserv.management.monitor.ServletMonitor;
import com.sun.appserv.management.monitor.WebModuleVirtualServerMonitor;

import com.sun.appserv.management.util.misc.ExceptionUtil;

import com.sun.appserv.management.util.stringifier.SmartStringifier;
import com.sun.appserv.management.util.stringifier.StringifierRegistryIniterImpl;
import com.sun.appserv.management.util.stringifier.StringifierRegistryImpl;

import com.sun.enterprise.admin.mbeanapi.common.AMXConnector;
import com.sun.enterprise.admin.mbeanapi.common.AMXMonitoringTestBase;



/**
 * This test prints the statistics for ejb module and stateless bean
 *
 *  WebMonitoring</B>
 *
 * @author <a href=mailto:satish.viswanatham@sun.com>Satish Viswanatham</a>
 *         Date: Aug 24, 2004
 * @version $Revision: 1.7 $
 */
public class WebMonitoring extends AMXMonitoringTestBase {

    private String serverName;
    private static String SERVLETS = " - SERVLETS -";
    private static String VS = "VIRTUAL SERVER";

    public WebMonitoring(final String host, final int port, final String serverName,
            final String adminUser, final String adminPassword, 
            final boolean useTLS) throws IOException {
        super(host, port, adminUser,adminPassword,useTLS);
        this.serverName = serverName;
    }
    
    
    public void  test(final String applicationName, final String webModule) {
        if(isEmbeddedWebModule(applicationName, webModule)) {
            testEmbeddedWebModules(getWebModules(applicationName));
        } else {
            testStandAloneWebModule(webModule);
        }
    }
    

    /**
     *
     */
    public void testStandAloneWebModule(final String webModuleName) {
            Map webMap = getServerRootMonitor(serverName).
                getWebModuleVirtualServerMonitorMap();
            assert(webMap != null && webMap.size() > 0) : "FAILURE!";
            testEmbeddedWebModules(webMap);
    }
    
    
    /**
     *
     */
    public void testEmbeddedWebModules(final Map webModules) {
        assert(webModules != null && webModules.size() > 0) :
            "No web module monitors found! Make sure that the monitoring level " +
            "is set to HIGH or LOW!!";
        Iterator itr = webModules.values().iterator();
        while(itr.hasNext()) {
            WebModuleVirtualServerMonitor webMonitor = (WebModuleVirtualServerMonitor)itr.next();
            if (!webMonitor.getName().equals("//__asadmin/adminapp") 
                    && !webMonitor.getName().equals("//__asadmin/admingui")
                    && !webMonitor.getName().equals("//__asadmin/com_sun_web_ui")) {
                System.out.println("\nStats for WebModule [" + 
                webMonitor.getName() + "]");
                testWebModuleServlets(webMonitor.getServletMonitorMap());
            }
        }
    }


    /**
     *
     */
    public void testWebModuleServlets(final Map servlets) {
        printBlock(SERVLETS);
        assert(servlets != null && servlets.size() > 0) :
            "No servlet monitors found! Make sure that the monitoring level " +
            "is set to HIGH or LOW!!";
        Iterator itr = servlets.values().iterator();
        while(itr.hasNext()) {
            ServletMonitor sltMonitor = (ServletMonitor)itr.next();
            System.out.println("  |");
            System.out.println("  |");
            System.out.println("  ---- SERVLET [" + sltMonitor.getName() + "]");
            listStats(sltMonitor); 
        }
    }
    
    
    /**
     *
     */
    private Map getWebModules(final String applicationName) {
        return getApplicationMonitor(serverName, applicationName).
            getWebModuleVirtualServerMonitorMap();
    }
    
    
    /**
     *
     */
    private boolean isEmbeddedWebModule(final String applicationName,
            final String webModuleName) {
        final ApplicationMonitor appMtr = 
            getApplicationMonitor(serverName, applicationName);
        final Map webMgr = appMtr.getWebModuleVirtualServerMonitorMap();
        return (webMgr.get(webModuleName) != null 
            || "".equals(webMgr.get(webModuleName)));
    }
     
    

    /**
     *
     */
    public static void   main( final String[] args ) {
        new StringifierRegistryIniterImpl( StringifierRegistryImpl.DEFAULT );

        try {
            WebMonitoring webMtr = new WebMonitoring(
                System.getProperty("HOST", "localhost"),
                Integer.parseInt(System.getProperty("PORT","8686")), args[0],
                System.getProperty("ADMIN_USER", "admin"),
                System.getProperty("ADMIN_PASSWORD", "adminadmin"),
                Boolean.getBoolean(System.getProperty("USE_TLS", "false")));
         
            WebMonitoring.printArgs(args);
            
            if(args.length < 3) {
                webMtr.test(null,null);
            } else {
                webMtr.test(args[1], args[2]); 
            }
            
        } catch( Throwable t ) {
            ExceptionUtil.getRootCause( t ).printStackTrace();
        }
    }
    
}
