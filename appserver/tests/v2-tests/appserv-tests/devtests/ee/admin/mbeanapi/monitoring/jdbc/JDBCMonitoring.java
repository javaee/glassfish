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
