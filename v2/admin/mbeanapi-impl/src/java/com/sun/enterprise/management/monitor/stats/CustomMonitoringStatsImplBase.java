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

package com.sun.enterprise.management.monitor.stats;

import com.sun.enterprise.management.monitor.*;
import javax.management.InstanceNotFoundException;
import javax.management.j2ee.statistics.Statistic;
import com.sun.appserv.management.j2ee.statistics.MapStatistic;
import com.sun.appserv.management.j2ee.statistics.MapStatisticImpl;
import com.sun.appserv.management.j2ee.statistics.StatisticImpl;
import com.sun.appserv.management.j2ee.statistics.StatisticFactory;
import com.sun.appserv.management.util.misc.ExceptionUtil;

/**
	Base implementation class for LB Monitoring MBeans that provide Stats.
*/
public abstract class CustomMonitoringStatsImplBase extends MonitoringStatsImplBase {

    public CustomMonitoringStatsImplBase(String j2eeType) {
        super(j2eeType);
    }
    
    /**
        Get all Statistics from the delegate (our only available call API).
        Statistic names are translated appropriately.
    */
    protected Statistic[] getStatisticsFromImpl(CustomStatsImpl customStatsImpl) {
        try {
            final Statistic[] statistics = getStatisticsFromImplRaw(customStatsImpl);
            // translate the names to be the ones we expose in MBeanInfo
            for(int i = 0; i < statistics.length; ++i) {
                final Statistic	origStatistic	= statistics[i];

                final MapStatistic	m	= new MapStatisticImpl(origStatistic);

                final String	convertedName	= originalToDerivedStatisticName(origStatistic.getName());
                if (! convertedName.equals(origStatistic.getName())) 
                        m.setName(convertedName);

                final Class<? extends Statistic> theClass =
                    StatisticFactory.getInterface(origStatistic);
                assert(theClass != null);

                // this will create one which implements the requisite interfaces
                statistics[ i ]	= StatisticFactory.create(theClass, m.asMap());

                assert(theClass.isAssignableFrom(statistics[ i ].getClass()));
            }
            return(statistics);
        } catch (Exception e) {
            final Throwable rootCause = ExceptionUtil.getRootCause(e);

            if (!(rootCause instanceof InstanceNotFoundException)) 
                    // don't rethrow--will make MBeanServer unuseable as it has a bug if we throw
                    // an exception of of getMBeanInfo() which halts any further processing of the query
                    //NOTE: WARNING_CHANGED_TO_FINE	
                    logWarning("Can't get Statistics from impl of " + getObjectName() +
                            "\n" + rootCause.getMessage() + "\n" + ExceptionUtil.getStackTrace(rootCause));
            throw new RuntimeException(e);
        }
    }    
    
    protected Statistic[] getStatisticsFromImplRaw(CustomStatsImpl impl) {
	try {
            final Statistic[] statistics = impl.getStatistics();
            return statistics;
	} catch (Exception e) {
            final Throwable rootCause   = ExceptionUtil.getRootCause( e );
            logWarning( "CustomMonitoringStatsImplBase: " +
                "the stats impl for the stats of AMX MBean " +
                getObjectName() + " threw an exception: " + rootCause +
                ", stack = \n" + ExceptionUtil.getStackTrace( rootCause ) );
        }
        return new Statistic[0];
    }
}
























