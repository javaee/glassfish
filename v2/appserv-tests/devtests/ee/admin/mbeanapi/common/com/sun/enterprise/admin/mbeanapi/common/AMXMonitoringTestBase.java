/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.enterprise.admin.mbeanapi.common;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.monitor.ApplicationMonitor;
import com.sun.appserv.management.monitor.MonitoringRoot;
import com.sun.appserv.management.monitor.MonitoringStats;
import com.sun.appserv.management.monitor.ServerRootMonitor;
import java.io.IOException;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;

/**
 */
public class AMXMonitoringTestBase {
    
    DomainRoot mDomainRoot;
    public static final String SERVER_NAME = "server";
    public static final String APP_NAME = "stateless-simple";
    public static final String STATELESS_EJB_NAME = "stateless-simple.war";

    
    public AMXMonitoringTestBase(final String host, final int port, 
            final String adminUser, final String adminPassword,
            final boolean useTLS) throws IOException {
                
        final AMXConnector ct = 
            new AMXConnector( host, port, adminUser, adminPassword, useTLS );
        mDomainRoot = ct.getDomainRoot();
        
    }
    
    /**
     *
     */
    private MonitoringRoot getMonitoringRoot() {
        return mDomainRoot.getMonitoringRoot();
    }
    
    /**
     *
     */
    public ServerRootMonitor getServerRootMonitor(final String serverName) {
        
        ServerRootMonitor svrRoot =  
            (serverName != null || "".equals(serverName))
            ? ( (ServerRootMonitor) getMonitoringRoot().
                getServerRootMonitorMap().get(serverName) )
            : ( (ServerRootMonitor) getMonitoringRoot().
                getServerRootMonitorMap().get(SERVER_NAME) );
        assert(svrRoot != null) : "Cannot find server with name " + 
            serverName + "!";
        return svrRoot;
    }
    
    
    /**
     *
     */
    public ApplicationMonitor getApplicationMonitor(
            final String serverName, final String appName) {
        ApplicationMonitor app =  
            (appName != null || "".equals(appName))
            ? ( (ApplicationMonitor) getServerRootMonitor(serverName).
                getApplicationMonitorMap().get(appName) )
            : ( (ApplicationMonitor) getServerRootMonitor(serverName).
                getApplicationMonitorMap().get(APP_NAME) );
        assert(app != null) : "Cannot find application monitor with name " + 
            appName + "!";
        return app;
    }
    
    
    /**
     *
     */
    public void listStats (MonitoringStats ms) { 
        if (ms != null) {
            Stats stats = ms.getStats();
            Statistic[] sts = stats.getStatistics();
            printStats(sts);
        } else {
            System.out.println("VERIFY! Stats for " + ms.getName() 
                + " doesn't exist!");
        }
    }

    
    /**
     *
     */
    public void printStats(Statistic[] stats) {
        if (stats == null) {
            return;
        }
        
        for ( int i=0; i < stats.length; i++) {
            printStat(stats[i]);
        }
    }
    
    
    /**
     *
     *
     */
    public void printStat(Statistic stat) {
        if (stat == null) {
            return;
        } else {
            System.out.println("    |    ");
            System.out.println("    |    ");
            System.out.println("    --- Stat [" + stat.getName() + "]");
            System.out.println("         |");
            System.out.println("         |");
            System.out.println("         ---- Description: " 
                + stat.getDescription());
            System.out.println("         ---- Start Time: "
                + stat.getStartTime());
            System.out.println("         ---- Last Sample Time: " 
                + stat.getLastSampleTime());
            System.out.println("         ---- Unit: "
                + stat.getUnit());
            System.out.println("\n");
        }
    }
    
    /**
     *
     */
    public static void printArgs(final String [] args) {
        System.out.println("Printing arguments......");
        for(int i = 0; i < args.length; i++) {
            System.out.println("Args[" + i + "]: " + args[i]);
        }
    }
    
    /**
     *
     */
    public void printBlock(final String title) {
        System.out.println("\n*******************************************");
        System.out.println("*                                         *");
        System.out.println("*     " + title + "                        *");
        System.out.println("*                                         *");
        System.out.println("*******************************************\n");
    }
}
