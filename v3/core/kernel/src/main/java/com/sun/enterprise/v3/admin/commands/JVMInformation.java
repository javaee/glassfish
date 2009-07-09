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

import javax.management.MBeanServerConnection;

/**
 */
public class JVMInformation  implements JVMInformationMBean { //, MBeanRegistration TODO
    private final MBeanServerConnection mbsc;
    private final ThreadMonitor tm;
    private final SummaryReporter sr;
    private final MemoryReporter mr;
    private final ClassReporter cr;
    private final LogReporter lr;
    
    public JVMInformation(MBeanServerConnection mbsc) {
        this.mbsc = mbsc;
        tm = new ThreadMonitor(mbsc);
        sr = new SummaryReporter(mbsc);
        mr = new MemoryReporter(mbsc);
        cr = new ClassReporter(mbsc);
        lr = new LogReporter();
    }
    public String getThreadDump(String processName) { //this argument is not needed now. TODO
        return ( tm.getThreadDump() );
    }

    public String getSummary(String processName) { //this argument is not needed now. TODO
        return ( sr.getSummaryReport() );
    }

    public String getMemoryInformation(String processName) { //this argument is not needed now. TODO
        return ( mr.getMemoryReport() );
    }

    public String getClassInformation(String processName) { //this argument is not needed now. TODO
        return ( cr.getClassReport() );
    }

    public String getLogInformation(String processName) {
        return (lr.getLoggingReport());
    }
    /* //TODO
    public void postRegister(Boolean registrationDone) {
    }

    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        this.mbsc = server;
        final String sn = System.getProperty(SystemPropertyConstants.SERVER_NAME);
        final ObjectName on = JVMInformationCollector.formObjectName(sn, JVMInformation.class.getSimpleName());
        return ( on );
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }
    */
}
