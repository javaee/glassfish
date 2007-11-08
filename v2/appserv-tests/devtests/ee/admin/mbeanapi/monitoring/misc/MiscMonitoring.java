/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.admin.mbeanapi.monitoring.misc;

import java.io.IOException;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.monitor.MonitoringRoot;
import com.sun.appserv.management.monitor.ServerRootMonitor;
import com.sun.appserv.management.monitor.JVMMonitor;
import com.sun.appserv.management.monitor.TransactionServiceMonitor;
import com.sun.appserv.management.monitor.HTTPServiceMonitor;
import com.sun.appserv.management.monitor.ConnectionManagerMonitor;
import com.sun.appserv.management.monitor.ThreadPoolMonitor;
import com.sun.appserv.management.monitor.MonitoringStats;

import com.sun.appserv.management.util.misc.ExceptionUtil;

import com.sun.appserv.management.util.stringifier.SmartStringifier;
import com.sun.appserv.management.util.stringifier.StringifierRegistryIniterImpl;
import com.sun.appserv.management.util.stringifier.StringifierRegistryImpl;
import com.sun.enterprise.admin.mbeanapi.common.AMXConnector;


/**
 * This test print the statistics for ejb module and stateless bean
 *
 *  JDBCMonitoring</B>
 *
 * @author <a href=mailto:satish.viswanatham@sun.com>Satish Viswanatham</a>
 *         Date: Aug 24, 2004
 * @version $Revision: 1.3 $
 */
public class MiscMonitoring {

    private static DomainRoot	mDomainRoot;

    private static final String SERVER_NAME = "server";

    public void  testThreadPoolStats(ServerRootMonitor svrRootMtr)
    {
        Map tpMap= svrRootMtr.getThreadPoolMonitorMap();
        System.out.println("\n" + "Thread Pool Monitor Stats: " 
        + "\n");

        System.out.println("Thread PoolORB monitor map " + tpMap.size());

        Iterator itr = tpMap.values().iterator();

        while ( itr.hasNext()) {
            Object o = itr.next();
            System.out.println(" Looking at monitor " + o);

            ThreadPoolMonitor tpm = (ThreadPoolMonitor) o;
            listStats(tpm);
        }
    }

    public void  testORBStats(ServerRootMonitor svrRootMtr)
    {
        Map orbMap= svrRootMtr.getORBConnectionManagerMonitorMap();
        System.out.println("\n" + "ORB Connection Manager Monitor Stats: " 
        + "\n");

        System.out.println("ORB monitor map " + orbMap.size());

        Iterator itr = orbMap.values().iterator();

        while ( itr.hasNext()) {
            Object o = itr.next();
            System.out.println(" Looking at monitor " + o);

            ConnectionManagerMonitor cm = (ConnectionManagerMonitor) o;
            listStats(cm);
        }
    }

    public void  testJVMStats(ServerRootMonitor svrRootMtr)
    {
        JVMMonitor jvmMtr = svrRootMtr.getJVMMonitor();

        System.out.println("\n" + "JVM Monitor Stats: " + "\n");
        listStats(jvmMtr);
    }

    public void  testTransactionServiceStats(ServerRootMonitor svrRootMtr)
    {
        TransactionServiceMonitor tsMtr = 
            svrRootMtr.getTransactionServiceMonitor();

        System.out.println("\n" + "Transaction Service Monitor Stats: " + "\n");
        listStats(tsMtr);
    }

    public void  testHTTPServiceStats(ServerRootMonitor svrRootMtr)
    {
        HTTPServiceMonitor tsMtr =     svrRootMtr.getHTTPServiceMonitor();

        System.out.println("\n" + "File Cache Monitor Stats: " + "\n");
        listStats(tsMtr.getFileCacheMonitor());
        System.out.println("\n" + "DNS Monitor Stats: " + "\n");
        listStats(tsMtr.getDNSMonitor());
        System.out.println("\n" + "PWC Thread pool Monitor Stats: " + "\n");
        listStats(tsMtr.getPWCThreadPoolMonitor());
        System.out.println("\n" + "Connection queue  Monitor Stats: " + "\n");
        listStats(tsMtr.getConnectionQueueMonitor());
        System.out.println("\n" + "Keep Alive Monitor Stats: " + "\n");
        listStats(tsMtr.getKeepAliveMonitor());
    }

    public void listStats(MonitoringStats mtr)
    {
        if (mtr == null) {
            System.out.println("Monitoring stats is null");
        } else {
            Stats stats = mtr.getStats();
            Statistic[] sts = stats.getStatistics();
            printStats(sts);
        }
    }

    public void printStats(Statistic[] stats) 
    {
        if (stats == null)
            return;

        for ( int i=0; i < stats.length; i++) 
        {
            printStat(stats[i]);
        }
        
    }
    
    public void printStat(Statistic stat) 
    {
        if (stat == null)
            return;
        else
            System.out.println(" Stat name is " + stat.getName() + 
                " description: " + stat.getDescription() + " start time " 
                + stat.getStartTime() + " last sample time " 
                + stat.getLastSampleTime() + " unit " + stat.getUnit());
    }

    public MiscMonitoring(final String host, 
                                   final int port, 
                                   final String adminUser, 
                                   final String adminPassword,
                                   final boolean useTLS)
                                    throws IOException
    {
        final AMXConnector ct	= 
            new AMXConnector( host, port, adminUser, adminPassword, useTLS );

        mDomainRoot	= ct.getDomainRoot();

    }


    public static void   main( final String[] args )
    {
        new StringifierRegistryIniterImpl( StringifierRegistryImpl.DEFAULT );

        try
        {
            MiscMonitoring miscMtr = new MiscMonitoring(
                System.getProperty("HOST", "localhost"),
                Integer.parseInt(System.getProperty("PORT","8686")),
                System.getProperty("ADMIN_USER", "admin"),
                System.getProperty("ADMIN_PASSWORD", "adminadmin"),
                Boolean.getBoolean(System.getProperty("USE_TLS", "false")));

            MonitoringRoot monitorRoot = mDomainRoot.getMonitoringRoot() ;
            assert(monitorRoot !=null);
            ServerRootMonitor svrRootMtr = (ServerRootMonitor) monitorRoot.
                        getServerRootMonitorMap().  get(SERVER_NAME);

            miscMtr.testJVMStats(svrRootMtr); 
            miscMtr.testTransactionServiceStats(svrRootMtr); 
            miscMtr.testHTTPServiceStats(svrRootMtr);
            miscMtr.testThreadPoolStats(svrRootMtr);
            miscMtr.testORBStats(svrRootMtr);
        }
        catch( Throwable t )
        {
            ExceptionUtil.getRootCause( t ).printStackTrace();
        }
    }
}
