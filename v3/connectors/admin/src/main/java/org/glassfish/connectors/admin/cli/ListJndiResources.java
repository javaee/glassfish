package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ExternalJndiResource;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

import java.util.ArrayList;
import java.util.List;

/**
 * List Jndi Resources command
 */
@Service(name = "list-jndi-resources")
@Scoped(PerLookup.class)
@I18n("list.jndi.resources")
public class ListJndiResources implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ListJndiResources.class);

    @Param(optional = true,
            defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    String target;

    @Inject
    ExternalJndiResource[] jndiResources;

    @Inject
    Domain domain;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();
        Server targetServer = domain.getServerNamed(target);


        try {
            List<String> list = new ArrayList<String>();

            for (ExternalJndiResource r : jndiResources) {
                if (targetServer.isResourceRefExists(r.getJndiName())) {
                    list.add(r.getJndiName());
                }
            }
            if (list.isEmpty()) {
                final ActionReport.MessagePart part =
                        report.getTopMessagePart().addChild();
                part.setMessage(localStrings.getLocalString(
                        "list.jndi.resources.empty",
                        "Nothing to list."));
            } else {
                for (String jndiName : list) {
                    final ActionReport.MessagePart part =
                            report.getTopMessagePart().addChild();
                    part.setMessage(jndiName);
                }
            }
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("" +
                    "list.jndi.resources.fail",
                    "Unable to list jndi resources.") + " " +
                    e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}
