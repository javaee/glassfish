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
import com.sun.ejb.containers.EntityContainer;

import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.external.probe.provider.annotations.*;
import org.glassfish.external.statistics.*;
import org.glassfish.external.statistics.impl.*;
import org.glassfish.gmbal.*;

/**
 * Probe listener for the Ejb Timed Object monitoring events. 
 *
 * @author Marina Vatkina
 */
// TODO: find the right names
// v2: com.sun.appserv:application=__ejb_container_timer_app,name=TimerBean,type=entity-bean,category=monitor,ejb-module=ejb_jar,server=server
// v3: amx:pp=/mon/server-mon[server],type=entity-bean-mon,name=??????????
@AMXMetadata(type="entity-bean-mon", group="monitoring", isSingleton=false)
@ProbeProvider(moduleProviderName="glassfish", moduleName="ejb", probeProviderName="ejb-timers-monitoring")
@ManagedObject
@Description("Entity Bean Statistics")
public class EntityBeanStatsProvider {

    private BoundedRangeStatisticImpl pooledCount = null;
    private BoundedRangeStatisticImpl readyCount = null;

    private static final Logger _logger =
            EjbContainerUtilImpl.getInstance().getLogger();

    private String appName = null;
    private String moduleName = null;
    private String beanName = null;
    private boolean registered = false;

    private EntityContainer delegate;

    public EntityBeanStatsProvider(EntityContainer delegate, String appName, 
            String moduleName, String beanName) {

        this.delegate = delegate;
        this.appName = appName;
        this.moduleName = moduleName;
        this.beanName = beanName;

        long now = System.currentTimeMillis();

        pooledCount = new BoundedRangeStatisticImpl(
            0, 0, 0, delegate.getMaxPoolSize(), delegate.getSteadyPoolSize(),
            "PooledCount", "count", "Number of entity beans in pooled state",
            now, now);
        readyCount = new BoundedRangeStatisticImpl(
            0, 0, 0, delegate.getMaxCacheSize(), 0,
            "ReadyCount", "count", "Number of entity beans in ready state",
            now, now);
    }

    public void register() {
        String node = EjbMonitoringUtils.registerSubComponent(
                appName, moduleName, beanName, /** TODO **/ "entity-beans", this);
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

    @ManagedAttribute(id="pooledcount")
    @Description( "Number of entity beans in pooled state")
    public RangeStatistic getPooledCount() {
        return EjbMonitoringUtils.getRangeStatisticForCurrentValue(
                pooledCount, delegate.getPooledCount());
    }

    @ManagedAttribute(id="readycount")
    @Description( "Number of entity beans in ready state")
    public RangeStatistic getReadyCount() {
        return EjbMonitoringUtils.getRangeStatisticForCurrentValue(
                readyCount, delegate.getReadyCount());
    }
}
