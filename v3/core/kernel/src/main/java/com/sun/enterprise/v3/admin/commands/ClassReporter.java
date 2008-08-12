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
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServerConnection;

/**
 */
class ClassReporter {

    private final MBeanServerConnection mbsc;
    private final StringManager sm = StringManager.getManager(ClassReporter.class);
    public ClassReporter(final MBeanServerConnection mbsc) {
        this.mbsc = mbsc;
    }
    public String getClassReport() throws RuntimeException {
        try {
            final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
            final ClassLoadingMXBean clmb = ManagementFactory.newPlatformMXBeanProxy(mbsc, 
                    ManagementFactory.CLASS_LOADING_MXBEAN_NAME, ClassLoadingMXBean.class);
            sb.append(sm.getString("classloading.info"));
            sb.append(sm.getString("classes.loaded", clmb.getLoadedClassCount()));
            sb.append(sm.getString("classes.total", clmb.getTotalLoadedClassCount()));
            sb.append(sm.getString("classes.unloaded", clmb.getUnloadedClassCount()));
            
            final CompilationMXBean cmb = ManagementFactory.newPlatformMXBeanProxy(mbsc, 
                    ManagementFactory.COMPILATION_MXBEAN_NAME, CompilationMXBean.class);
            sb.append(sm.getString("complilation.info"));
            sb.append(sm.getString("compilation.monitor.status", cmb.isCompilationTimeMonitoringSupported()));
            sb.append(sm.getString("jit.compilar.name", cmb.getName()));
            sb.append(sm.getString("compilation.time", JVMInformationCollector.millis2HoursMinutesSeconds(cmb.getTotalCompilationTime())));
            return ( sb.toString() );
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }
}