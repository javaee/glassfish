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
package com.sun.enterprise.management.monitor;

import javax.management.ObjectName;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import java.io.IOException;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.BoundaryStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.TimeStatistic;

import com.sun.appserv.management.util.j2ee.J2EEUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.stringifier.ArrayStringifier;
import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.appserv.management.j2ee.statistics.*;
import com.sun.appserv.management.base.Util;

import com.sun.enterprise.management.support.ComSunAppservTest;
import com.sun.enterprise.management.support.OldMonitorTypes;

/**
    Unit test for the com.sun.appserv monitoring MBeans relied
    upon by AMX.
 */
public final class ComSunAppservMonitorTest extends ComSunAppservTest
{
    	public
    ComSunAppservMonitorTest()
    {
    }
    
     
    private interface MonitorIntf
    {
        public String[]     getStatisticNames() throws Exception;
        public Statistic[]  getStatistics() throws Exception;
    };
    
    private final class MonitorImpl implements MonitorIntf
    {
        final ObjectName            mObjectName;
        
            public
        MonitorImpl(
            final ObjectName    objectName )
        {
            mObjectName = objectName;
        }
        
            public String[]
        getStatisticNames()
            throws Exception
        {
        	final String[] statisticNames =
        	    (String[])getMBeanServerConnection().invoke( mObjectName, "getStatisticNames", null,  null );
        	
        	return statisticNames;
        }
        
            public Statistic[]
        getStatistics()
            throws Exception
        {
        	final Statistic[] statistics =
        	    (Statistic[])getMBeanServerConnection().invoke( mObjectName, "getStatistics", null,  null );
        	
        	return statistics;
        }
    };
	
        private MonitorIntf
    getMonitorIntf( final ObjectName objectName )
    {
        final MonitorIntf   intf    = new MonitorImpl( objectName );
        boolean basicsOK = true;
        
        if ( ! hasStatisticSupport( objectName ) )
        {
            warning( "MBean " + StringUtil.quote( objectName ) +
                " doesn't have getStatisticNames() and getStatistics() methods." );
            return null;
        }
        
        try
        {
            final String[] statisticNames  = intf.getStatisticNames();
            if ( statisticNames == null )
            {
                warning( "MBean " + StringUtil.quote( objectName ) +
                    " returned null from getStatisticNames()" );
                basicsOK    = false;
            }
            if ( statisticNames.length == 0 )
            {
                warning( "MBean " + StringUtil.quote( objectName ) +
                    " returned an empty String[] from getStatisticNames()" );
                basicsOK    = false;
            }
        }
        catch( Exception e )
        {
			final Throwable rootCause	= ExceptionUtil.getRootCause( e );
			
            warning( "MBean " + StringUtil.quote( objectName ) +
                " threw an exception from getStatisticNames(): " + rootCause );
            basicsOK    = false;
        }
        
        if ( basicsOK )
        {
            try
            {
                final Statistic[] statistics  = intf.getStatistics();
                if ( statistics == null )
                {
                    warning( "MBean " + StringUtil.quote( objectName ) +
                        " returned null from getStatistics()" );
                    basicsOK    = false;
                }
                if ( statistics != null && statistics.length == 0 )
                {
                    warning( "MBean " + StringUtil.quote( objectName ) +
                        " returned an empty Statistic[] from getStatistics()" );
                    basicsOK    = false;
                }
            }
            catch( Exception e )
            {
			    final Throwable rootCause	= ExceptionUtil.getRootCause( e );
			
                warning( "MBean " + StringUtil.quote( objectName ) +
                    " threw an exception from getStatistics(): " + rootCause );
                basicsOK    = false;
            }
        }
        
        return basicsOK ? intf : null;
    }


        private boolean
    checkMonitor(final ObjectName   objectName )
        throws Exception
    {
        boolean worksOK = true;
        
        final MonitorIntf   intf  = getMonitorIntf( objectName );
        if ( intf != null )
        {
            final String[]      statisticNames   = intf.getStatisticNames();
            final Set<String>   statisticNamesSet   = GSetUtil.newStringSet( statisticNames );
            
            // get all names as defined by Statistics
            final Statistic[]   statistics       = intf.getStatistics();
            final Set<String>   statisticNamesFromStatisticsSet   = new HashSet<String>();
            for( final Statistic s : statistics )
            {
                statisticNamesFromStatisticsSet.add( s.getName() );
            }
            
            if ( ! statisticNamesSet.equals( statisticNamesFromStatisticsSet ) )
            {
                final String[]  statisticNamesFromStatistics    =
                    GSetUtil.toStringArray( statisticNamesFromStatisticsSet );
                
                Arrays.sort( statisticNames );
                Arrays.sort( statisticNamesFromStatistics );
                
                printVerbose( "WARNING: MBean " + StringUtil.quote( objectName ) +
                    " returns Statistic names from getStatisticNames() " +
                    "that disagree with the names actually " +
                    "found in Statistics from getStatistics(): " +
                    "getStatisticNames() = {" + ArrayStringifier.stringify( statisticNames, "," ) +
                    "}, getStatistics() = {" +
                        ArrayStringifier.stringify( statisticNamesFromStatistics, "," ) + "}" );
                worksOK = false;
            }
        }
        
        return( worksOK );
    }
    
    
    /**
        private boolean
   shouldBeTested( final ObjectName objectName )
        throws Exception
   {
        final MBeanInfo mbeanInfo   = getMBeanServerConnection().getMBeanInfo( objectName );
        boolean shouldTest  = false;
        
        final MBeanOperationInfo[]  candidates  = mbeanInfo.getOperations();
        if ( JMXUtil.findOperations( candidates, "getStatisticNames" ).length != 0 &&
             JMXUtil.findOperations( candidates, "getStatistics" ).length != 0 )
        {
            shouldTest  = true;
        }
        
        return shouldTest;
   }
   */
   
   /**
     types to be tested as in "category=monitor,type=xxx".  We can't just test all
     category=monitor, or even check that getStatistics() and getStatisticNames()
     are present, as many such MBeans don't work at all.
     
     Types taken from com.sun.enterprise.management.support.initMap().
     */
    private static final Set<String> COM_SUN_APPSERV_MONITOR_TYPES  =
        GSetUtil.newUnmodifiableStringSet(
            "jvm",
            "ejb",
            "standalone-ejb-module",
            "bean-pool",
            "bean-cache",
            "bean-method",
            "servlet",
            "virtual-server",
            "webmodule-virtual-server",
            "http-listener",
            "transaction-service",
            "thread-pool",
            "connection-manager",
            "jdbc-connection-pool",
            "connector-connection-pool",
            "file-cache",
            "keep-alive",
            "dns",
            "connection-queue",
            "webservice-endpoint" );
        
        
        private boolean
   shouldBeTested( final ObjectName objectName )
        throws Exception
   {
        final String    type    = objectName.getKeyProperty( "type" );
        
        return COM_SUN_APPSERV_MONITOR_TYPES.contains( type );
   }
   
          private boolean
    hasStatisticSupport( final ObjectName objectName )
    {
        boolean hasSupport  = false;
        
        try
        {
            final MBeanInfo mbeanInfo   = getMBeanServerConnection().getMBeanInfo( objectName );
            boolean shouldTest  = false;
            
            final MBeanOperationInfo[]  candidates  = mbeanInfo.getOperations();
            if ( JMXUtil.findOperations( candidates, "getStatisticNames" ).length != 0 &&
                 JMXUtil.findOperations( candidates, "getStatistics" ).length != 0 )
            {
                hasSupport  = true;
            }
        }
        catch( Exception e )
        {
			final Throwable rootCause	= ExceptionUtil.getRootCause( e );
			
            warning( "hasStatisticSupport: got exception: " + rootCause );
        }
        
        return hasSupport;
    }
    
        public void
    testAllMonitor()
        throws Exception
    {
        try
        {
            Class.forName( "com.sun.enterprise.admin.monitor.stats.CountStatisticImpl" );
        }
        catch( ClassNotFoundException e )
        {
            failure( "ComSunAppservMonitorTest.testAllMonitor: " +
            "CLASSPATH is missing Statistic classes, skipping tests. " +
            "Use 'maven run-tests' instead of 'ant run-tests'" );
            return;
        }
        
        final Map<String,ObjectName>    m   = getAllComSunAppservMonitor();
        
        final Collection<ObjectName>   objectNames = m.values();
        final Set<ObjectName>   defective   = new HashSet<ObjectName>();
        int testedCount = 0;
        for( final ObjectName objectName : objectNames )
        {
            if ( ! shouldBeTested( objectName ) )
            {
                continue;
            }
            
            ++testedCount;
            
            if ( ! checkMonitor( objectName ) )
            {
                defective.add( objectName );
            }
        }
        
        printVerbose( "ComSunAppservMonitorTest.testAllMonitor: checked " +
            testedCount + " com.sun.appserv:category=monitor MBeans for basic functionality, " +
            defective.size() + " failures." );
            
        if ( defective.size() != 0 )
        {
            // slim down the ObjectName for better readability
            final String[]  names   = new String[ defective.size() ];
            int i = 0;
            for( final ObjectName objectName : defective )
            {
                names[ i ]  = objectName.getCanonicalKeyPropertyListString();
                ++i;
            }
            
            Arrays.sort( names );
            
            final boolean verbose   = getVerbose();
            
            warning( "The following " + defective.size() +
                " com.sun.appserv MBeans don't work correctly, so " +
                "subsequent tests (eg J2EETest) may fail:\n" +
                ArrayStringifier.stringify( names, "\n") +
                (verbose ? "" : "\n(set amxtest.verbose=true for details)") );
        }
    }
	
}






