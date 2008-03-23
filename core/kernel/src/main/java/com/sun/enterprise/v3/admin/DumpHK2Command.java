package com.sun.enterprise.v3.admin;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.impl.ModulesRegistryImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Dumps the currently configured HK2 modules and their contents.
 *
 * <p>
 * Useful for debugging classloader related issues.
 *
 * @author Kohsuke Kawaguchi
 */
@Service(name="dump-hk2")
public class DumpHK2Command implements AdminCommand {

    @Inject
    ModulesRegistry modulesRegistry;
    
    public void execute(AdminCommandContext context) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        modulesRegistry.dumpState(new PrintStream(baos));

        ActionReport report = context.getActionReport();
        report.setActionExitCode(ExitCode.SUCCESS);
        report.setMessage(baos.toString());
    }
}
