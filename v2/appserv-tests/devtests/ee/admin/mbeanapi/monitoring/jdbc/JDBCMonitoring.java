/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.admin.mbeanapi.monitoring.jdbc;

import java.io.IOException;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.monitor.MonitoringRoot;
import com.sun.appserv.management.monitor.ServerRootMonitor;
import com.sun.appserv.management.monitor.JDBCConnectionPoolMonitor;
import com.sun.appserv.management.monitor.ConnectorConnectionPoolMonitor;
import com.sun.appserv.management.monitor.MonitoringStats;

import com.sun.appserv.management.util.misc.ExceptionUtil;

import com.sun.appserv.management.util.stringifier.SmartStringifier;
import com.sun.appserv.management.util.stringifier.StringifierRegistryIniterImpl;
import com.sun.appserv.management.util.stringifier.StringifierRegistryImpl;
import com.sun.enterprise.admin.mbeanapi.common.AMXConnector;


/**
 * This test print the statistics for jdbc and connector connection pools
 *
 *  JDBCMonitoring</B>
 *
 * @author <a href=mailto:satish.viswanatham@sun.com>Satish Viswanatham</a>
 *         Date: Aug 24, 2004
 * @version $Revision: 1.3 $
 */
public class JDBCMonitoring {

    private static DomainRoot	mDomainRoot;

    private static String SERVER_NAME = "server";

    public void  testCCPoolStats( ServerRootMonitor svrRootMtr)
    {

        Map cpMtrMgr =   svrRootMtr.getConnectorConnectionPoolMonitorMap();
        System.out.println("connector connection pool size  is " + 
                        cpMtrMgr.size());
        Iterator itr = cpMtrMgr.values().iterator();
        while (itr.hasNext()) {
            Object o = itr.next();
            System.out.println(" Connector Connection pool  is " + o);
            ConnectorConnectionPoolMonitor cPool  =
                    (ConnectorConnectionPoolMonitor)o; 
            listStat(cPool); 
       }

    }

    public void  testJDBCPoolStats( ServerRootMonitor svrRootMtr)
    {

        Map cpMtrMgr =   svrRootMtr.getJDBCConnectionPoolMonitorMap();
        System.out.println("jdbc connection pool size  is " + cpMtrMgr.size());
        Iterator itr = cpMtrMgr.values().iterator();
        while (itr.hasNext()) {
            Object o = itr.next();
            System.out.println(" Connection pool  is " + o);
            JDBCConnectionPoolMonitor cPool  = (JDBCConnectionPoolMonitor)o; 
            listStat(cPool); 
       }

    }

    public void listStat(MonitoringStats mtr)
    {
        if (mtr == null) {
            System.out.println("Monitoring stats is null");
        } else {
            listStats(mtr);
        }
    }

    public void listStats(MonitoringStats ms) 
    {
        Stats stats = ms.getStats();
        Statistic[] sts = stats.getStatistics();
        printStats(sts);
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

    public JDBCMonitoring(final String host, 
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
            JDBCMonitoring jdbcMtr = new JDBCMonitoring(
                System.getProperty("HOST", "localhost"),
                Integer.parseInt(System.getProperty("PORT","8686")),
                System.getProperty("ADMIN_USER", "admin"),
                System.getProperty("ADMIN_PASSWORD", "adminadmin"),
                Boolean.getBoolean(System.getProperty("USE_TLS", "false")));

            MonitoringRoot monitorRoot = mDomainRoot.getMonitoringRoot() ;
            assert(monitorRoot !=null);
            ServerRootMonitor svrRootMtr = (ServerRootMonitor) monitorRoot.
                        getServerRootMonitorMap().  get(SERVER_NAME);

            jdbcMtr.testJDBCPoolStats(svrRootMtr); 
            jdbcMtr.testCCPoolStats(svrRootMtr); 
        }
        catch( Throwable t )
        {
            ExceptionUtil.getRootCause( t ).printStackTrace();
        }
    }
}
