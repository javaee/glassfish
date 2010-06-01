package org.glassfish.web.admin.cli;

import java.beans.PropertyVetoException;
import java.util.List;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.grizzly.config.dom.PortUnification;
import com.sun.grizzly.config.dom.Protocol;
import com.sun.grizzly.config.dom.ProtocolFilter;
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
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

@Service(name = "create-protocol-finder")
@Scoped(PerLookup.class)
@I18n("create.protocol.finder")
public class CreateProtocolFinder implements AdminCommand {
    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CreateProtocolFinder.class);
    @Param(name = "name", primary = true)
    String name;
    @Param(name = "protocol", optional = false)
    String protocolName;
    @Param(name = "target-protocol", optional = false)
    String targetName;
    @Param(name = "classname", optional = false)
    String classname;
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
        final Protocols protocols = config.getNetworkConfig().getProtocols();
        final Protocol protocol = protocols.findProtocol(protocolName);
        final Protocol target = protocols.findProtocol(targetName);
        try {
            validate(protocol, "create.http.fail.protocolnotfound",
                "The specified protocol {0} is not yet configured", protocolName);
            validate(target, "create.http.fail.protocolnotfound",
                "The specified protocol {0} is not yet configured", targetName);
            final Class<?> finderClass = Thread.currentThread().getContextClassLoader().loadClass(classname);
            if(!com.sun.grizzly.portunif.ProtocolFinder.class.isAssignableFrom(finderClass)) {
                report.setMessage(localStrings.getLocalString("create.portunif.fail.notfinder",
                    "{0} create failed.  Given class is not a ProtocolFinder: {1}", name, classname));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            PortUnification unif = (PortUnification)ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                @Override
                public Object run(Protocol param) throws PropertyVetoException, TransactionFailure {
					     PortUnification pu = param.getPortUnification();
						  if(pu == null) {
						      pu = param.createChild(PortUnification .class);
								param.setPortUnification(pu);
						  }
						  return pu;
                }
            }, protocol);
            ConfigSupport.apply(new SingleConfigCode<PortUnification>() {
                @Override
                public Object run(PortUnification param) throws PropertyVetoException, TransactionFailure {
                    final List<ProtocolFinder> list = param.getProtocolFinder();
                    for (ProtocolFinder finder : list) {
                        if (name.equals(finder.getName())) {
                            throw new TransactionFailure(
                                String.format("A protocol finder named %s already exists.", name));
                        }
                    }
                    final ProtocolFinder finder = param.createChild(ProtocolFinder.class);
                    finder.setName(name);
                    finder.setProtocol(targetName);
                    finder.setClassname(classname);
                    list.add(finder);
                    return null;
                }
            }, unif);
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
