package org.glassfish.web.admin.cli;

import java.util.ArrayList;
import java.util.List;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.grizzly.config.dom.PortUnification;
import com.sun.grizzly.config.dom.Protocol;
import com.sun.grizzly.config.dom.ProtocolFinder;
import com.sun.grizzly.config.dom.Protocols;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigCode;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

@Service(name = "delete-protocol-finder")
@Scoped(PerLookup.class)
@I18n("delete.protocol.finder")
public class DeleteProtocolFinder implements AdminCommand {
    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(DeleteProtocolFinder.class);
    @Param(name = "name", primary = true)
    String name;
    @Param(name = "protocol", optional = false)
    String protocolName;
    @Inject(name = ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    private ActionReport report;

    @Override
    public void execute(AdminCommandContext context) {
        report = context.getActionReport();
        try {
            final Protocols protocols = config.getNetworkConfig().getProtocols();
            final Protocol protocol = protocols.findProtocol(protocolName);
            validate(protocol, "create.http.fail.protocolnotfound",
                "The specified protocol {0} is not yet configured", protocolName);
            PortUnification pu = getPortUnification(protocol);
            ConfigSupport.apply(new ConfigCode() {
                @Override
                public Object run(ConfigBeanProxy... params) {
                    final Protocol prot = (Protocol) params[0];
                    final PortUnification portUnification = (PortUnification) params[1];

                    final List<ProtocolFinder> oldList = portUnification.getProtocolFinder();
                    List<ProtocolFinder> newList = new ArrayList<ProtocolFinder>();
                    for (final ProtocolFinder finder : oldList) {
                        if (!name.equals(finder.getName())) {
                            newList.add(finder);
                        }
                    }
                    if (oldList.size() == newList.size()) {
                        throw new RuntimeException(
                            String.format("No finder named %s found for protocol %s", name, protocolName));
                    }
                    if(newList.isEmpty()) {
                        prot.setPortUnification(null);
                    } else {
                        portUnification.setProtocolFinder(newList);
                    }
                    return null;
                }
            }, protocol, pu);
            cleanPortUnification(pu);
        } catch (ValidationFailureException e) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
            report.setMessage(localStrings.getLocalString("delete.fail", "{0} delete failed: {1}", name,
                e.getMessage() == null ? "No reason given" : e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;

        }
    }

    private PortUnification getPortUnification(Protocol protocol) {
        PortUnification pu = protocol.getPortUnification();
        if (pu == null) {
            report.setMessage(localStrings.getLocalString("not.found", "No {0} element found for {1}",
                "port-unification", protocol.getName()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
        return pu;
    }

    private void cleanPortUnification(PortUnification pu) throws TransactionFailure {
        if (pu != null && pu.getProtocolFinder().isEmpty()) {
            ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                @Override
                public Object run(Protocol param) {
                    param.setPortUnification(null);
                    return null;

                }
            }, pu.getParent(Protocol.class));
        }
    }

    private void validate(ConfigBeanProxy check, String key, String defaultFormat, String... arguments)
        throws ValidationFailureException {
        if (check == null) {
            report.setMessage(localStrings.getLocalString(key, defaultFormat, arguments));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            throw new ValidationFailureException();
        }
    }

    private class ValidationFailureException extends Exception {
    }
}