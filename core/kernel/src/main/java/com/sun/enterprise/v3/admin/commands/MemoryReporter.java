/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.util.i18n.StringManager;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 */
class MemoryReporter {
    private final MBeanServerConnection mbsc;
    private RuntimeMXBean rmbean;
    private MemoryMXBean mmbean;
    private List<MemoryPoolMXBean> pools;
    private List<GarbageCollectorMXBean> gcmbeans;
    private final static StringManager sm = StringManager.getManager(MemoryReporter.class);
    
    public MemoryReporter(final MBeanServerConnection mbsc) {
        this.mbsc = mbsc;
    }
    public String getMemoryReport() {
        init();
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        sb.append(getMemoryPoolReport());
        sb.append(getGarbageCollectionReport());
        sb.append(getMemoryMXBeanReport());
        return ( sb.toString() );
    }
    
    private void init() throws RuntimeException {
        try {
            this.rmbean = ManagementFactory.newPlatformMXBeanProxy(mbsc,
                                                 ManagementFactory.RUNTIME_MXBEAN_NAME,
                                                 RuntimeMXBean.class);
            this.mmbean = ManagementFactory.newPlatformMXBeanProxy(mbsc,
                                                 ManagementFactory.MEMORY_MXBEAN_NAME,
                                                 MemoryMXBean.class);
            ObjectName poolName = new ObjectName(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",*");;
            ObjectName gcName = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE+",*");
            Set mbeans = mbsc.queryNames(poolName, null);
            if (mbeans != null) {
                pools = new ArrayList<MemoryPoolMXBean>();
                Iterator iterator = mbeans.iterator();
                MemoryPoolMXBean p = null;
                    while (iterator.hasNext()) {
                        ObjectName objName = (ObjectName) iterator.next();
                        p = ManagementFactory.newPlatformMXBeanProxy(mbsc,
                                                   objName.getCanonicalName(),
                                                   MemoryPoolMXBean.class);
                        pools.add(p);
                }
            }
            mbeans = mbsc.queryNames(gcName, null);
            if (mbeans != null) {
                gcmbeans = new ArrayList<GarbageCollectorMXBean>();
                Iterator iterator = mbeans.iterator();
                GarbageCollectorMXBean gc = null;
                while (iterator.hasNext()) {
                    ObjectName objName = (ObjectName) iterator.next();
                    gc = ManagementFactory.newPlatformMXBeanProxy(mbsc,
                                               objName.getCanonicalName(),
                                               GarbageCollectorMXBean.class);
                    gcmbeans.add(gc);
                }
            }        
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private String getMemoryPoolReport() {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        final long millis = rmbean.getUptime();
        final String uptime = sm.getString("uptime", JVMInformationCollector.millis2HoursMinutesSeconds(millis));
        sb.append(uptime);
        for (final MemoryPoolMXBean m : pools) {
            final String n = m.getName();
            sb.append(sm.getString("memory.pool.name", n));
            MemoryUsage mu = m.getUsage();
            sb.append(mu2String(mu));
        }
        return ( sb.toString() );
    }
    private String mu2String(final MemoryUsage mu) {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        final String init = JVMInformationCollector.formatLong(mu.getInit());
        sb.append(sm.getString("memory.usage.init", init));
        final String comm = JVMInformationCollector.formatLong(mu.getCommitted());
        sb.append(sm.getString("memory.usage.comm", comm));
        final String max  = JVMInformationCollector.formatLong(mu.getMax());
        sb.append(sm.getString("memory.usage.max", max));
        final String used = JVMInformationCollector.formatLong(mu.getUsed());
        sb.append(sm.getString("memory.usage.used", used));        
        return ( sb.toString() );
    }
    private String getGarbageCollectionReport() {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        for (final GarbageCollectorMXBean m : gcmbeans) {
            final String name = sm.getString("gc.name", m.getName());
            sb.append(name);
            final String cc = sm.getString("gc.numcol", JVMInformationCollector.formatLong(m.getCollectionCount()));
            sb.append(cc);
            final String gct = sm.getString("gc.coltime", JVMInformationCollector.millis2SecondsMillis(m.getCollectionTime()));
            sb.append(gct);
        }
        return ( sb.toString() );
    }
    private String getMemoryMXBeanReport() {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        sb.append(sm.getString("heap.mem.usage"));
        sb.append(mu2String(mmbean.getHeapMemoryUsage()));
        sb.append(sm.getString("nonheap.mem.usage"));
        sb.append(mu2String(mmbean.getNonHeapMemoryUsage()));
        sb.append(sm.getString("obj.fin.pending", mmbean.getObjectPendingFinalizationCount()));
        return ( sb.toString() );
    }
}