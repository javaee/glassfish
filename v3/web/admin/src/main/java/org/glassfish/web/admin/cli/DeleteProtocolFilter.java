package org.glassfish.web.admin.cli;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.grizzly.config.dom.Protocol;
import com.sun.grizzly.config.dom.ProtocolChain;
import com.sun.grizzly.config.dom.ProtocolChainInstanceHandler;
import com.sun.grizzly.config.dom.ProtocolFilter;
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
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

@Service(name = "delete-protocol-filter")
@Scoped(PerLookup.class)
@I18n("delete.protocol.filter")
public class DeleteProtocolFilter implements AdminCommand {
    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(DeleteProtocolFilter.class);
    @Param(name = "name", primary = true)
    String name;
    @Param(name = "protocol", optional = false)
    String protocolName;
    @Param(name = "target", optional = true, defaultValue = "server")
    String target;
    @Inject(name = ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    @Inject
    Domain domain;
    private ActionReport report;

    @Override
    public void execute(AdminCommandContext context) {
        Server targetServer = domain.getServerNamed(target);
        if (targetServer!=null) {
            config = domain.getConfigNamed(targetServer.getConfigRef());
        }
        com.sun.enterprise.config.serverbeans.Cluster cluster = domain.getClusterNamed(target);
        if (cluster!=null) {
            config = domain.getConfigNamed(cluster.getConfigRef());
        }
        report = context.getActionReport();
        try {
            final Protocols protocols = config.getNetworkConfig().getProtocols();
            final Protocol protocol = protocols.findProtocol(protocolName);
            validate(protocol, "create.http.fail.protocolnotfound",
                "The specified protocol {0} is not yet configured", protocolName);
            ProtocolChainInstanceHandler handler = getHandler(protocol);
            ProtocolChain chain = getChain(handler);
            ConfigSupport.apply(new SingleConfigCode<ProtocolChain>() {
                @Override
                public Object run(ProtocolChain param) throws PropertyVetoException, TransactionFailure {
                    final List<ProtocolFilter> list = param.getProtocolFilter();
                    List<ProtocolFilter> newList = new ArrayList<ProtocolFilter>();
                    for (final ProtocolFilter filter : list) {
                        if (!name.equals(filter.getName())) {
                            newList.add(filter);
                        }
                    }
                    if (list.size() == newList.size()) {
                        throw new RuntimeException(
                            String.format("No filter named %s found for protocol %s", name, protocolName));
                    }
                    param.setProtocolFilter(newList);
                    return null;
                }
            }, chain);
            cleanChain(chain);
            cleanHandler(handler);
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

    private ProtocolChain getChain(ProtocolChainInstanceHandler handler) throws TransactionFailure {
        ProtocolChain chain = handler.getProtocolChain();
        if (chain == null) {
            report.setMessage(localStrings.getLocalString("not.found", "No {0} element found for {1}",
                "protocol-chain", handler.getParent(Protocol.class).getName()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
        return chain;
    }

    private void cleanChain(ProtocolChain chain) throws TransactionFailure {
        if (chain != null && chain.getProtocolFilter().isEmpty()) {
            ConfigSupport.apply(new SingleConfigCode<ProtocolChainInstanceHandler>() {
                @Override
                public Object run(ProtocolChainInstanceHandler param)
                    throws PropertyVetoException, TransactionFailure {
                    param.setProtocolChain(null);
                    return null;
                }
            }, chain.getParent(ProtocolChainInstanceHandler.class));
        }
    }

    private ProtocolChainInstanceHandler getHandler(Protocol protocol) throws TransactionFailure {
        ProtocolChainInstanceHandler handler = protocol.getProtocolChainInstanceHandler();
        if (handler == null) {
            report.setMessage(localStrings.getLocalString("not.found", "No {0} element found for {1}",
                "protocol-chain-instance-handler", protocol.getName()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
        return handler;
    }

    private void cleanHandler(ProtocolChainInstanceHandler handler) throws TransactionFailure {
        if (handler != null && handler.getProtocolChain() == null) {
            ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                @Override
                public Object run(Protocol param)
                    throws PropertyVetoException, TransactionFailure {
                    param.setProtocolChainInstanceHandler(null);
                    return null;

                }
            }, handler.getParent(Protocol.class));
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