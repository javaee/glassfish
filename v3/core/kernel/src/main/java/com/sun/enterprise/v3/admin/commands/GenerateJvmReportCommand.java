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

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

/** Implements the front end for generating the JVM report. Sends back a String
 * to the asadmin console based on server's locale.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish V3
 */
@Service(name="generate-jvm-report")
@Scoped(PerLookup.class)
@I18n("generate.jvm.report")
public class GenerateJvmReportCommand implements AdminCommand {
    
    @Param(name="target", optional=true) // default is DAS, TODO
    String target;
    
    @Param(name="type", optional=true, defaultValue="summary", acceptableValues = "summary, thread, class, memory, log") //default is "summary"
    String type;
    
    private MBeanServer mbs = null;  //needs to be injected, I guess
    private JVMInformation jvmi = null;

    public void execute(AdminCommandContext ctx) {
        prepare();
        String result = getResult();
        ActionReport report = ctx.getActionReport();
        report.setMessage(result);
        report.setActionExitCode(ExitCode.SUCCESS);
    }
    
    private synchronized void prepare() {
        mbs = ManagementFactory.getPlatformMBeanServer(); //TODO
        if (jvmi == null)
            jvmi = new JVMInformation(mbs);
    }
    private String getResult() {
        if ("summary".equals(type))
            return (jvmi.getSummary(target));
        else if("thread".equals(type))
            return jvmi.getThreadDump(target);
        else if ("class".equals(type))
            return jvmi.getClassInformation(target);
        else if("memory".equals(type))
            return jvmi.getMemoryInformation(target);
        else if ("log".equals(type))
            return jvmi.getLogInformation(target);
        else
            throw new IllegalArgumentException("Unsupported Option: " + type);   //this should not happen
    }
}