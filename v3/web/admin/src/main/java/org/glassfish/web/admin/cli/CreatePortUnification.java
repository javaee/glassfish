package org.glassfish.web.admin.cli;

import java.beans.PropertyVetoException;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.grizzly.config.dom.PortUnification;
import com.sun.grizzly.config.dom.Protocol;
import com.sun.grizzly.config.dom.Protocols;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

@Service(name = "create-port-unification")
@Scoped(PerLookup.class)
@I18n("create.port.unification")
public class CreatePortUnification implements AdminCommand {
    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CreatePortUnification.class);

    @Param(name = "name", primary = true)
    String name;

    @Inject
    Habitat habitat;

    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        if (habitat.getComponent(Protocol.class, name) != null) {
            report.setMessage(localStrings.getLocalString("create.portunif.protocol.duplicate",
                "A protocol definition named {0} already exists.", name));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        try {
            CreateProtocol.create(habitat.getComponent(Protocols.class), name, false);
            final Protocol protocol = habitat.getComponent(Protocol.class, name);
            ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                @Override
                public Object run(final Protocol param) throws PropertyVetoException, TransactionFailure {
                    final PortUnification portUnification = param.createChild(PortUnification.class);
                    param.setPortUnification(portUnification);
                    return null;
                }
            }, protocol);
        } catch (TransactionFailure e) {
            e.printStackTrace();
            report.setMessage(
                localStrings.getLocalString("create.portunif.fail", "{0} create failed: "
                    + (e.getMessage() == null ? "No reason given" : e.getMessage()), name));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;

        }
    }
}
