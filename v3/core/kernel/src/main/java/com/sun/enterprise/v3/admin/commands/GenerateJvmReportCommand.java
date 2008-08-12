/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.util.i18n.StringManager;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

/** Implements the front end for generating the JVM report. Sends back a String
 * to the server based on its locale.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish V3
 */
@Service(name="generate-jvm-report")
@Scoped(Singleton.class) //I want exactly one instance of this class all the time
@I18n("generate.jvm.report")
public class GenerateJvmReportCommand implements AdminCommand {
    
    @Param(name="target", optional=true) // default is DAS, TODO
    String target;
    
    @Param(name="type", optional=true, defaultValue="summary") //default is "summary"
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
        if (type == null || "summary".equals(type))
            return (jvmi.getSummary(target));
        else if("thread".equals(type))
            return jvmi.getThreadDump(target);
        else if ("class".equals(type))
            return jvmi.getClassInformation(target);
        else if("memory".equals(type))
            return jvmi.getMemoryInformation(target);
        else
            throw new IllegalArgumentException("Unsupported Option: " + type);
    }
}