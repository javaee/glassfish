package org.glassfish.web.admin.cli;

import java.beans.PropertyVetoException;
import java.util.List;

import com.sun.enterprise.config.serverbeans.Config;
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

@Service(name = "create-protocol-filter")
@Scoped(PerLookup.class)
@I18n("create.protocol.filter")
public class CreateProtocolFilter implements AdminCommand {
    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CreateProtocolFilter.class);
    @Param(name = "name", primary = true)
    String name;
    @Param(name = "protocol", optional = false)
    String protocolName;
    @Param(name = "classname", optional = false)
    String classname;
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
            final Class<?> filterClass = Thread.currentThread().getContextClassLoader().loadClass(classname);
            if (!com.sun.grizzly.ProtocolFilter.class.isAssignableFrom(filterClass)) {
                report.setMessage(localStrings.getLocalString("create.portunif.fail.notfilter",
                    "{0} create failed.  Given class is not a ProtocolFilter: {1}", name, classname));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            if (protocol.getHttp() != null || protocol.getSsl() != null) {
                throw new TransactionFailure("Mixing http/ssl and port unification elements is not allowed.");
            }
            ProtocolChainInstanceHandler handler = getHandler(protocol);
            ProtocolChain chain = getChain(handler);
            ConfigSupport.apply(new SingleConfigCode<ProtocolChain>() {
                @Override
                public Object run(ProtocolChain param) throws PropertyVetoException, TransactionFailure {
                    final List<ProtocolFilter> list = param.getProtocolFilter();
                    for (ProtocolFilter filter : list) {
                        if (name.equals(filter.getName())) {
                            throw new TransactionFailure( String.format("A protocol filter named %s already exists.",
                                name));
                        }
                    }
                    final ProtocolFilter filter = param.createChild(ProtocolFilter.class);
                    filter.setName(name);
                    filter.setClassname(classname);
                    list.add(filter);
                    return null;
                }
            }, chain);
        } catch (ValidationFailureException e) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
            report.setMessage(localStrings.getLocalString("create.portunif.fail", "{0} create failed: {1}", name,
                e.getMessage() == null ? "No reason given" : e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;

        }
    }

    private ProtocolChain getChain(ProtocolChainInstanceHandler handler) throws TransactionFailure {
        ProtocolChain chain = handler.getProtocolChain();
        if (chain == null) {
            chain = (ProtocolChain) ConfigSupport.apply(new SingleConfigCode<ProtocolChainInstanceHandler>() {
                @Override
                public Object run(ProtocolChainInstanceHandler param)
                    throws PropertyVetoException, TransactionFailure {
                    final ProtocolChain protocolChain = param.createChild(ProtocolChain.class);
                    param.setProtocolChain(protocolChain);
                    return protocolChain;

                }
            }, handler);
        }
        return chain;
    }

    private ProtocolChainInstanceHandler getHandler(Protocol protocol) throws TransactionFailure {
        ProtocolChainInstanceHandler handler = protocol.getProtocolChainInstanceHandler();
        if (handler == null) {
            handler = (ProtocolChainInstanceHandler) ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                @Override
                public Object run(Protocol param)
                    throws PropertyVetoException, TransactionFailure {
                    final ProtocolChainInstanceHandler child = param.createChild(ProtocolChainInstanceHandler.class);
                    param.setProtocolChainInstanceHandler(child);
                    return child;

                }
            }, protocol);
        }
        return handler;
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
