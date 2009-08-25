/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.servermgmt;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;

@Service(name="test-upgrade", metadata="mode=debug")
@Scoped(PerLookup.class)
public class V2ToV3ConfigUpgradeTester  implements AdminCommand {
    @Inject
    Habitat habitat;

    @Inject
    V2ToV3ConfigUpgrade up;

    public void execute(AdminCommandContext context) {
        up.postConstruct();
        String msg = "Testing upgrade!";
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ExitCode.SUCCESS);
        report.setMessage(msg);
    }
}
