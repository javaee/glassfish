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
 * ThreadMonitor.java
 *
 * Created on July 21, 2005, 11:50 AM
 */

package com.sun.enterprise.admin.mbeans.jvm;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;

/**
 */
class ThreadMonitor {
    
    private final MBeanServerConnection mbsc;
    private static final Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
    private final StringManager sm = StringManager.getManager(ThreadMonitor.class);
    public ThreadMonitor(final MBeanServerConnection mbsc) {
        this.mbsc = mbsc;
    }
    public final String getThreadDump() {
        final long start = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        final StringBuilderNewLineAppender td = new StringBuilderNewLineAppender(sb);
        try {
            final ThreadMXBean tmx = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
            final String title = getTitle();
            td.append(title); 
            final long[] tids = tmx.getAllThreadIds();
            td.append(sm.getString("thread.no", tmx.getThreadCount()));
            td.append(sm.getString("daemon.threads.no", tmx.getDaemonThreadCount()));
            final ThreadInfo[] tinfos = tmx.getThreadInfo(tids, Integer.MAX_VALUE);
            /*
            Arrays.sort(tinfos, new Comparator<ThreadInfo> () {
                public int compare(ThreadInfo a, ThreadInfo b) {
                    return ( a.getThreadName().compareTo(b.getThreadName()) );
                }
            });
             */
            for (final ThreadInfo ti : tinfos) {
                td.append(dumpThread(ti));
            }
            sb.append(getDeadlockInfo(tmx));
            return ( td.toString() );
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            final long end = System.currentTimeMillis();
            final double time = (end/1000.0) - (start/1000.0);
            logger.info("Time in seconds to get the jvm thread dump: " + time);
        }
    }
    private String dumpThread(final ThreadInfo ti) {
        final long ids = ti.getThreadId();
        final String ss  = ti.getThreadState().toString();
        //following should work because of autoboxing :)
        String msg = sm.getString("thread.title", quote(ti.getThreadName()), ids, ss);
        final StringBuilder sb = new StringBuilder(msg);
        if (ti.getLockName() != null) {
            msg = sm.getString("thread.waiting.on", ti.getLockName());
            sb.append(msg);
        }
        if (ti.isSuspended()) {
            msg = sm.getString("thread.suspended");
            sb.append(msg);
        }
        if (ti.isInNative()) {
            msg = sm.getString("thread.in.native");
            sb.append(msg);
        }
        sb.append(System.getProperty("line.separator"));
        if (ti.getLockOwnerName() != null) {
            msg = sm.getString("thread.owner", ti.getLockOwnerName(), ti.getLockOwnerId());
            sb.append(msg);
        }
        for (final StackTraceElement ste : ti.getStackTrace()) {
           msg = sm.getString("thread.stack.element", ste.toString());
           sb.append(msg);
           sb.append(System.getProperty("line.separator"));
        }
        sb.append(System.getProperty("line.separator"));
        return ( sb.toString() );
    }
    
    private String getTitle() throws Exception {
        final RuntimeMXBean rt  = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
        final String vmname     = rt.getVmName();
        final String vmversion  = rt.getVmVersion();
        final String vmvendor   = rt.getVmVendor();
        final String title      = sm.getString("td.title", vmname, vmversion, vmvendor);
        
        return ( title );
    }
    
    private String quote(final String uq) {
        final StringBuilder sb = new StringBuilder("\"");
        sb.append(uq).append("\"");
        return ( sb.toString() );
    }
    
    private String getDeadlockInfo(final ThreadMXBean tmx) {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        final long[] dts = tmx.findMonitorDeadlockedThreads();
        if (dts == null) {
            sb.append(sm.getString("no.deadlock"));
        }
        else {
            sb.append(sm.getString("deadlocks.found"));
            for (final long dt : dts) {
                final ThreadInfo ti = tmx.getThreadInfo(dt);
                sb.append(this.dumpThread(ti));
            }
        }
        return ( sb.toString() );
    }
}
