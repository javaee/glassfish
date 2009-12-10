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
package com.sun.enterprise.v3.services.impl.monitor.stats;

import com.sun.grizzly.http.KeepAliveStats;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.annotations.Reset;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 * Keep-alive statistics
 *
 * @author Alexey Stashok
 */
@AMXMetadata(type = "keep-alive-mon", group = "monitoring")
@ManagedObject
@Description("Keep-Alive Statistics")
public class KeepAliveStatsProvider implements StatsProvider {

    private final String name;
    private final CountStatisticImpl maxRequestsCount = new CountStatisticImpl("MaxRequests", "count", "Maximum number of requests allowed on a single keep-alive connection");
    private final CountStatisticImpl timeoutInSeconds = new CountStatisticImpl("SecondsTimeouts", "seconds", "Keep-alive timeout value in seconds");
    private final CountStatisticImpl keepAliveConnectionsCount = new CountStatisticImpl("CountConnections", "count", "Number of connections in keep-alive mode");
    private final CountStatisticImpl flushesCount = new CountStatisticImpl("CountFlushes", "count", "Number of keep-alive connections that were closed");
    private final CountStatisticImpl hitsCount = new CountStatisticImpl("CountHits", "count", "Number of requests received by connections in keep-alive mode");
    private final CountStatisticImpl refusalsCount = new CountStatisticImpl("CountRefusals", "count", "Number of keep-alive connections that were rejected");
    private final CountStatisticImpl timeoutsCount = new CountStatisticImpl("CountTimeouts", "count", "Number of keep-alive connections that timed out");
    private volatile KeepAliveStats keepAliveStats;

    public KeepAliveStatsProvider(String name) {
        this.name = name;
    }

    @Override
    public Object getStatsObject() {
        return keepAliveStats;
    }

    @Override
    public void setStatsObject(Object object) {
        if (object instanceof KeepAliveStats) {
            keepAliveStats = (KeepAliveStats) object;
        } else {
            keepAliveStats = null;
        }
    }

    @ManagedAttribute(id = "maxrequests")
    @Description("Maximum number of requests allowed on a single keep-alive connection")
    public CountStatistic getMaxKeepAliveRequestsCount() {
        return maxRequestsCount;
    }

    @ManagedAttribute(id = "secondstimeouts")
    @Description("Keep-alive timeout value in seconds")
    public CountStatistic getKeepAliveTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    @ManagedAttribute(id = "countconnections")
    @Description("Number of connections in keep-alive mode")
    public CountStatistic getKeepAliveConnectionsCount() {
        return keepAliveConnectionsCount;
    }

    @ManagedAttribute(id = "countflushes")
    @Description("Number of keep-alive connections that were closed")
    public CountStatistic getFlushesCount() {
        return flushesCount;
    }

    @ManagedAttribute(id = "counthits")
    @Description("Number of requests received by connections in keep-alive mode")
    public CountStatistic getHitsCount() {
        return hitsCount;
    }

    @ManagedAttribute(id = "countrefusals")
    @Description("Number of keep-alive connections that were rejected")
    public CountStatistic getRefusalsCount() {
        return refusalsCount;
    }

    @ManagedAttribute(id = "counttimeouts")
    @Description("Number of keep-alive connections that timed out")
    public CountStatistic getTimeoutsCount() {
        return timeoutsCount;
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:setMaxCountRequestsEvent")
    public void setMaxCountRequestsEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("maxRequests") int max) {
        if (name.equals(listenerName)) {
            maxRequestsCount.setCount(max);
        }
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:setTimeoutInSecondsEvent")
    public void setTimeoutInSecondsEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("timeoutInSeconds") int timeoutInSeconds) {
        if (name.equals(listenerName)) {
            this.timeoutInSeconds.setCount(timeoutInSeconds);
        }
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountConnectionsEvent")
    public void incrementCountConnectionsEvent(@ProbeParam("listenerName") String listenerName) {
        if (name.equals(listenerName)) {
            keepAliveConnectionsCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:decrementCountConnectionsEvent")
    public void decrementCountConnectionsEvent(@ProbeParam("listenerName") String listenerName) {
        if (name.equals(listenerName)) {
            keepAliveConnectionsCount.decrement();
        }
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountFlushesEvent")
    public void incrementCountFlushesEvent(@ProbeParam("listenerName") String listenerName) {
        if (name.equals(listenerName)) {
            flushesCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountHitsEvent")
    public void incrementCountHitsEvent(@ProbeParam("listenerName") String listenerName) {
        if (name.equals(listenerName)) {
            hitsCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountRefusalsEvent")
    public void incrementCountRefusalsEvent(@ProbeParam("listenerName") String listenerName) {
        if (name.equals(listenerName)) {
            refusalsCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountTimeoutsEvent")
    public void incrementCountTimeoutsEvent(@ProbeParam("listenerName") String listenerName) {
        if (name.equals(listenerName)) {
            timeoutsCount.increment();
        }
    }

    @Reset
    public void reset() {
        final KeepAliveStats stats = keepAliveStats;
        if (stats != null) {
            maxRequestsCount.setCount(stats.getMaxKeepAliveRequests());
            timeoutInSeconds.setCount(stats.getKeepAliveTimeoutInSeconds());
        }

        keepAliveConnectionsCount.setCount(0);
        flushesCount.setCount(0);
        hitsCount.setCount(0);
        refusalsCount.setCount(0);
        timeoutsCount.setCount(0);
    }
}
