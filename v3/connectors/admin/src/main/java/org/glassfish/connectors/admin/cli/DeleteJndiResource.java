package org.glassfish.connectors.admin.cli;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.config.serverbeans.*;

import java.beans.PropertyVetoException;

/**
 * Delete Jndi Resource object
 *
 */
@Service(name="delete-jndi-resource")
@Scoped(PerLookup.class)
@I18n("delete.jndi.resource")
public class DeleteJndiResource implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DeleteJndiResource.class);

    @Param(optional=true,
    defaultValue= SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    String target;

    @Param(name="jndi_name", primary=true)
    String jndiName;

    @Inject
    Resources resources;

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

        // ensure we already have this resource
        if (!isResourceExists(resources, jndiName)) {
            report.setMessage(localStrings.getLocalString(
                    "delete.jndi.resource.notfound",
                    "A jndi resource named {0} does not exist.", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            // delete admin-object-resource
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException,
                        TransactionFailure {
                    for (ExternalJndiResource resource : jndiResources) {
                        if (resource.getJndiName().equals(jndiName)) {
                            param.getResources().remove(resource);
                            break;
                        }
                    }
                    return jndiResources;
                }
            }, resources);

            // delete resource-ref
            targetServer.deleteResourceRef(jndiName);

            report.setMessage(localStrings.getLocalString("" +
                    "delete.jndi.resource.success",
                    "Jndi resource {0} deleted.", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("" +
                    "delete.jndi.resource.fail",
                    "Unable to delete jndi resource {0}.", jndiName) + " "
                    + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
            return;
        }

    }

    private boolean isResourceExists(Resources resources, String jndiName) {
        for (Resource resource : resources.getResources()) {
            if (resource instanceof ExternalJndiResource) {
                if (((ExternalJndiResource) resource).getJndiName().equals(jndiName))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
