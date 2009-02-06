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
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;
import javax.management.MBeanServerConnection;


class SummaryReporter {
    
    private final MBeanServerConnection mbsc;
    private final StringManager sm = StringManager.getManager(SummaryReporter.class);
    private final static String secretProperty = "module.core.status";
    
    public SummaryReporter(final MBeanServerConnection mbsc) {
        this.mbsc = mbsc;
    }
    public String getSummaryReport() throws RuntimeException {
        try {
            final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
            final OperatingSystemMXBean os = ManagementFactory.newPlatformMXBeanProxy(mbsc,
                    ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
            sb.append(getOSInfo(os));
            final RuntimeMXBean rt = ManagementFactory.newPlatformMXBeanProxy(mbsc,
                    ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
            sb.append(getVMInfo(rt));
            return ( sb.toString(secretProperty) );
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private String getOSInfo(final OperatingSystemMXBean os) {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        sb.append(sm.getString("os.info"));
        sb.append(sm.getString("os.name", os.getName()));
        sb.append(sm.getString("os.arch", os.getArch(), os.getVersion()));
        sb.append(sm.getString("os.nproc", os.getAvailableProcessors()));
        sb.append(sm.getString("os.load", getSystemLoad(os)));
        return ( sb.toString() );
    }
    private String getVMInfo(final RuntimeMXBean rt) {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        sb.append(sm.getString("rt.info", rt.getName()));
        sb.append(sm.getString("rt.bcp", rt.getBootClassPath()));
        sb.append(sm.getString("rt.cp", rt.getClassPath()));
        sb.append(sm.getString("rt.libpath", rt.getLibraryPath()));
        sb.append(sm.getString("rt.nvv", rt.getVmName(), rt.getVmVendor(), rt.getVmVersion()));
        sb.append(getProperties(rt));
        return ( sb.toString() );
    }
    private String getProperties(final RuntimeMXBean rt) {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        final Map<String, String> unsorted = rt.getSystemProperties();
        // I decided to sort this for better readability -- 27 Feb 2006
        final TreeMap<String, String> props = new TreeMap<String, String>(unsorted);
        sb.append(sm.getString("rt.sysprops"));
        for (final String n : props.keySet()) {
            sb.append(n + " = " + props.get(n));
        }
        return ( sb.toString() );
    }
    
    private String getSystemLoad(OperatingSystemMXBean os) {
        //available only on 1.6
        String info = ThreadMonitor.NA;
        try {
            String METHOD = "getSystemLoadAverage";
            Method m = os.getClass().getMethod(METHOD, (Class[]) null);
            if (m != null) {
                Object ret = m.invoke(os, (Object[])null);
                return ( ret.toString() );
            }
        } catch(Exception e) {
            
        }
        return ( info );
    }
}
