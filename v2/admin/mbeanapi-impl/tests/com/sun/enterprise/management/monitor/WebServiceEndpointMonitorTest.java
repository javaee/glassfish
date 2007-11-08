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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/com/sun/enterprise/management/monitor/WebServiceEndpointMonitorTest.java,v 1.5 2005/12/25 03:41:55 tcfujii Exp $
 * $Revision: 1.5 $
 * $Date: 2005/12/25 03:41:55 $
 */
package com.sun.enterprise.management.monitor;

import javax.management.ObjectName;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.BoundaryStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.TimeStatistic;
import com.sun.appserv.management.j2ee.statistics.NumberStatistic;

			
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.monitor.WebServiceEndpointMonitor;
import com.sun.appserv.management.monitor.statistics.WebServiceEndpointAggregateStats;

import com.sun.appserv.management.j2ee.statistics.*;


import com.sun.enterprise.management.AMXTestBase;
import com.sun.enterprise.management.Capabilities;


public final class WebServiceEndpointMonitorTest
	extends AMXMonitorTestBase
{
    	public
    WebServiceEndpointMonitorTest()
    {
    }
    
  
  		public void
  	testStats()
  	{
  		final QueryMgr	q	= getQueryMgr();
  		
  		final Set	wsMonitors	= q.queryJ2EETypeSet(
        XTypes.WEBSERVICE_ENDPOINT_MONITOR );
  		
  		if (  wsMonitors.size() == 0 )
  		{
  			warning( "WebServiceEndpointMonitorTest: no MBeans found to test.");
  		}
  		else
  		{
            Iterator itr = wsMonitors.iterator();
  			while ( itr.hasNext() )
  			{
                WebServiceEndpointMonitor m = (WebServiceEndpointMonitor)
                itr.next();

  				final WebServiceEndpointAggregateStats		s	= 
                    m.getWebServiceEndpointAggregateStats();
  				
  				// verify that we can get each Statistic; 
                // there was a problem at one time

                final CountStatistic r1	= s.getTotalFaults();
                assert( r1 != null );
	    
                final CountStatistic r2	= s.getTotalNumSuccess() ;
                assert( r2 != null );
	    
                //final AverageRangeStatistic r3 = s.getResponseTime();
                //assert( r3 != null );

                final NumberStatistic c1 = s.getThroughput() ;
                assert( c1 != null );
	    
                final CountStatistic c2 = s.getTotalAuthFailures();
                assert( c2 != null );
	    
                final CountStatistic c3 = s.getTotalAuthSuccesses();
                assert( c3 != null );
	    
  			}
  		}
  	}
 
}






