package org.glassfish.admin.monitor.cli;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.admin.monitor.jndi.JndiNameLookupHelper;

import javax.naming.NamingException;
import java.util.List;

@Service(name = "list-jndi-entries")
@Scoped(PerLookup.class)
@I18n("list.jndi.entries")
public class ListJndiEntries implements AdminCommand {

   final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ListJndiEntries.class);

    @Param(name="context", optional = true)
    String contextName;

    @Param(primary = true, optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    String target;

    public void execute(AdminCommandContext context) {
        List<String> names;
        final ActionReport report = context.getActionReport();

        try {
            names = getNames(contextName);
        } catch (NamingException e) {
            report.setMessage(localStrings.getLocalString("list.jndi.entries.namingexception", "Naming Exception caught.")
                    + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
                                        
        try {
        if (names.isEmpty()) {
                final ActionReport.MessagePart part =
                        report.getTopMessagePart().addChild();
                part.setMessage(localStrings.getLocalString(
                        "list.jndi.entries.empty",
                        "Nothing to list."));
            } else {
                for (String jndiName : names) {
                    final ActionReport.MessagePart part =
                            report.getTopMessagePart().addChild();
                    part.setMessage(jndiName);
                }
            }
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("" +
                    "list.jndi.entries.fail",
                    "Unable to list jndi entries.") + " " +
                    e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    private List<String> getNames(String context)
            throws NamingException {
        List<String> names;
        JndiNameLookupHelper helper = new JndiNameLookupHelper();
        names = helper.getJndiEntriesByContextPath(context);
        return names;
    }
}
