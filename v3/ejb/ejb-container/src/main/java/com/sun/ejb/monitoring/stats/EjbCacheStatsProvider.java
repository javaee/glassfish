/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.ejb.monitoring.stats;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.ejb.spi.stats.EJBCacheStatsProvider;

import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.external.probe.provider.annotations.*;
import org.glassfish.external.statistics.*;
import org.glassfish.external.statistics.impl.*;
import org.glassfish.gmbal.*;

/**
 * Probe listener for the Ejb Cache monitoring events. 
 *
 * @author Marina Vatkina
 */
// TODO: find the right names
//@AMXMetadata(type="ejb-cache-mon", group="monitoring", isSingleton=false)
@ManagedObject
@Description("Bean Cache Statistics")
public class EjbCacheStatsProvider {

    private CountStatisticImpl expiredStat = new CountStatisticImpl(
            "NumExpiredSessionsRemoved", "count", 
            "Provides a count value reflecting the number of expired sessions "
                + "that were removed from the bean cache.");

    private CountStatisticImpl passivations = new CountStatisticImpl(
            "NumPassivations", "count", 
            "Provides a count value reflecting the number of passivations for a "
                + "StatefulSessionBean from the bean cache.");

    private CountStatisticImpl passivationErrors = new CountStatisticImpl(
            "NumPassivationErrors", "count", 
            "Provides a count value reflecting the number of errors that occured "
                + "while passivating a StatefulSessionBean from the bean cache.");

    private CountStatisticImpl passivationSuccess = new CountStatisticImpl(
            "NumPassivationSuccess", "count", 
            "Provides a count value reflecting the number of passivations for a "
                + "StatefulSessionBean from the bean cache that succeeded");

    private BoundedRangeStatisticImpl cacheHits;
    private BoundedRangeStatisticImpl cacheMisses;
    private BoundedRangeStatisticImpl numBeans;

    private static final Logger _logger =
            EjbContainerUtilImpl.getInstance().getLogger();

    private String appName = null;
    private String moduleName = null;
    private String beanName = null;
    private boolean registered = false;
    private EJBCacheStatsProvider delegate;

    public EjbCacheStatsProvider(EJBCacheStatsProvider delegate, String appName, 
            String moduleName, String beanName) {

        this.delegate = delegate;
        this.appName = appName;
        this.moduleName = moduleName;
        this.beanName = beanName;

        long now = System.currentTimeMillis();

        numBeans = new BoundedRangeStatisticImpl(
            0, 0, 0, delegate.getMaxCacheSize(), 0, "NumBeansInCache", "count", 
            "Provides total number of EJBs in the associated EJB Cache.",
            now, now);
        cacheHits = new BoundedRangeStatisticImpl(
            0, 0, 0, Long.MAX_VALUE, 0, "CacheHits", "count", 
            "Provides the number of times a user request hits an EJB in associated EJB cache instance",
            now, now);
        cacheMisses = new BoundedRangeStatisticImpl(
            0, 0, 0, Long.MAX_VALUE, 0, "CacheMisses", "count", 
            "Provides the number of times a user request fails to find an EJB in associated EJB cache instance",
            now, now);
    }

    public void register() {
        String node = EjbMonitoringUtils.registerSubComponent(
                appName, moduleName, beanName, "bean-cache", this);
        if (node != null) {
            registered = true;
        }
    }

    public void unregister() {
        if (registered) {
            registered = false;
            StatsProviderManager.unregister(this);
        }
    }

    @ManagedAttribute(id="cachemisses")
    @Description( "Number of times a user request fails to find an EJB in associated EJB cache instance")
    public RangeStatistic getCacheMisses() {
        return EjbMonitoringUtils.getRangeStatisticForCurrentValue(
                cacheMisses, delegate.getCacheMisses());
    }

    @ManagedAttribute(id="cachehits")
    @Description( "Number of times a user request hits an EJB in associated EJB cache instance")
    public RangeStatistic getCacheHits() {
        return EjbMonitoringUtils.getRangeStatisticForCurrentValue(
                cacheHits, delegate.getCacheHits());
    }

    @ManagedAttribute(id="numbeansincache")
    @Description( "Number of EJBs in the associated EJB Cache")
    public RangeStatistic getNumBeansInCache() {
        return EjbMonitoringUtils.getRangeStatisticForCurrentValue(
                numBeans, delegate.getNumBeansInCache());
    }

    @ManagedAttribute(id="numexpiredsessionsremoved")
    @Description( "Number of expired sessions removed by the cleanup thread.")
    public CountStatistic getNumExpiredSessionsRemoved() {
        expiredStat.setCount(delegate.getNumExpiredSessionsRemoved());
        return expiredStat;
    }

    @ManagedAttribute(id="numpassivations")
    @Description( "Number of passivated beans")
    public CountStatistic getNumPassivations() {
        passivations.setCount(delegate.getNumPassivations());
        return passivations;
    }

    @ManagedAttribute(id="numpassivationerrors")
    @Description( "Number of errors during passivation.")
    public CountStatistic getNumPassivationErrors() {
        passivationErrors.setCount(delegate.getNumPassivationErrors());
        return passivationErrors;
    }

    @ManagedAttribute(id="numpassivationsuccess")
    @Description( "Number of times passivation completed successfully.")
    public CountStatistic getNumPassivationSuccess() {
        passivationSuccess.setCount(delegate.getNumPassivationSuccess());
        return passivationSuccess;
    }
}
