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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 * Network Connections Statistics
 * 
 * @author Alexey Stashok
 */
@ManagedObject
@Description("Network Connections Statistics")
public class ConnectionsStatsProvider {

    private final String name;
    private final CountStatisticImpl totalConnectionsCount = new CountStatisticImpl("TotalConnectionsCount", "count", "The total number of connections accepted");
    private final Map<Integer, Long> openConnectionsCount = new ConcurrentHashMap<Integer, Long>();

    public ConnectionsStatsProvider(String name) {
        this.name = name;
    }

    @ManagedAttribute(id = "total-connections-count")
    @Description("The total number of connections accepted")
    public CountStatistic getTotalConnectionsCount() {
        return totalConnectionsCount;
    }

    @ManagedAttribute(id = "open-connections-count")
    @Description("The number of open connections")
    public CountStatistic getOpenConnectionsCount() {
        CountStatisticImpl stats = new CountStatisticImpl("OpenConnectionsCount", "count", "The number of open connections");
        stats.setCount(openConnectionsCount.size());
        return stats;
    }

    @ProbeListener("glassfish:kernel:connections:connectionAcceptedEvent")
    public void connectionAcceptedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("connection") int connectionId) {

        if (name.equals(listenerName)) {
            totalConnectionsCount.increment();
            openConnectionsCount.put(connectionId, System.currentTimeMillis());
        }
    }

// We're not interested in client connections, created via Grizzly
//    @ProbeListener("glassfish:kernel:connections:connectionConnectedEvent")
//    public void connectionConnectedEvent(
//            @ProbeParam("listenerName") String listenerName,
//            @ProbeParam("connection") int connectionId) {
//    }
    @ProbeListener("glassfish:kernel:connections:connectionClosedEvent")
    public void connectionClosedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("connection") int connectionId) {
        if (name.equals(listenerName)) {
            openConnectionsCount.remove(connectionId);
        }
    }
}
