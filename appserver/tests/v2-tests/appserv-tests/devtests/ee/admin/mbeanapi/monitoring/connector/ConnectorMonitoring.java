/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.admin.mbeanapi.monitoring.connector;

import com.sun.appserv.management.monitor.ConnectorConnectionPoolMonitor;
import java.io.IOException;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;

import com.sun.appserv.management.monitor.ApplicationMonitor;
import com.sun.appserv.management.monitor.WebModuleMonitor;
import com.sun.appserv.management.monitor.ServletMonitor;
import com.sun.appserv.management.monitor.WebModuleVirtualServerMonitor;
import com.sun.appserv.management.monitor.NativeWebCoreVirtualServerRequestMonitor;
import com.sun.appserv.management.monitor.statistics.NativeWebCoreVirtualServerRequestStats;

import com.sun.appserv.management.util.misc.ExceptionUtil;

import com.sun.appserv.management.util.stringifier.SmartStringifier;
import com.sun.appserv.management.util.stringifier.StringifierRegistryIniterImpl;
import com.sun.appserv.management.util.stringifier.StringifierRegistryImpl;

import com.sun.enterprise.admin.mbeanapi.common.AMXConnector;
import com.sun.enterprise.admin.mbeanapi.common.AMXMonitoringTestBase;



/**
 * This test prints the statistics for ejb module and stateless bean
 *
 *  ConnectorMonitoring</B>
 *
 * @author <a href=mailto:satish.viswanatham@sun.com>Satish Viswanatham</a>
 *         Date: Aug 24, 2004
 * @version $Revision: 1.1 $
 */
public class ConnectorMonitoring extends AMXMonitoringTestBase {
    
    private String serverName;
    private static String SERVLETS = " - SERVLETS -";
    private static String VS = "VIRTUAL SERVER";
    
    public ConnectorMonitoring(final String host, final int port, final String serverName,
    final String adminUser, final String adminPassword,
    final boolean useTLS) throws IOException {
        super(host, port, adminUser,adminPassword,useTLS);
        this.serverName = serverName;
    }
    
    
    public void  test() {
        Map connMap =
            getServerRootMonitor(serverName).
                getConnectorConnectionPoolMonitorMap();
        assert(connMap.size() > 0) : 
            "The connection pool monitor map is " + connMap.size();
        for(Iterator itr = connMap.values().iterator(); itr.hasNext();) {
            ConnectorConnectionPoolMonitor connMonitor =
                (ConnectorConnectionPoolMonitor) itr.next();
            System.out.println("\nConnectorConnectionPoolMonitor [" +         
                connMonitor.getName() + "]");
            printStats(connMonitor.getStats().getStatistics());
        }
        /*
        HTTPServiceMonitor httpService =
        getServerRootMonitor(serverName).getHTTPServiceMonitor();
        assert (httpService != null) : "The http service monitor is null!";
        Map vsMap = httpService.getHTTPServiceVirtualServerMonitorMap();
        assert (vsMap.size() > 0) : "The http service monitor map is empty!";
        for(Iterator itr = vsMap.values().iterator(); itr.hasNext();) {
            HTTPServiceVirtualServerMonitor vsMtr =
            (HTTPServiceVirtualServerMonitor) vsMap.get(itr.next());
            assert (vsMtr != null) : "The http virtual server monitor is null!";
            NativeWebCoreVirtualServerRequestMonitor rMtr =
            vsMtr.getNativeWebCoreVirtualServerRequestMonitor();
            NativeWebCoreVirtualServerRequestStats wcStats =
            rMtr.getNativeWebCoreVirtualServerRequestStats();
            printStats(wcStats.getStatistics());
        }
         **/
    }
    
    
    /**
     *
     */
    public static void   main( final String[] args ) {
        new StringifierRegistryIniterImpl( StringifierRegistryImpl.DEFAULT );
        
        try {
            ConnectorMonitoring connMtr = new ConnectorMonitoring(
            System.getProperty("HOST", "hoyas.red.iplanet.com"),
            Integer.parseInt(System.getProperty("PORT","8686")), args[0],
            System.getProperty("ADMIN_USER", "admin"),
            System.getProperty("ADMIN_PASSWORD", "adminadmin"),
            Boolean.getBoolean(System.getProperty("USE_TLS", "false")));
            
            ConnectorMonitoring.printArgs(args);
            
            connMtr.test();
            
        } catch( Throwable t ) {
            ExceptionUtil.getRootCause( t ).printStackTrace();
        }
    }
    
}
