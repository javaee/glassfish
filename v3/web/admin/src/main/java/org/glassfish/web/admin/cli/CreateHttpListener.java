/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.web.admin.cli;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.NetworkListeners;
import com.sun.grizzly.config.dom.ThreadPool;
import com.sun.grizzly.config.dom.Transport;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.Transactions;

/**
 * Create Http Listener Command
 */
@Service(name = "create-http-listener")
@Scoped(PerLookup.class)
@I18n("create.http.listener")
public class CreateHttpListener implements AdminCommand {
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateHttpListener.class);
    @Param(name = "listeneraddress")
    String listenerAddress;
    @Param(name = "listenerport")
    String listenerPort;
    @Param(name = "defaultvs", optional = true)
    String defaultVS;
    @Param(name = "default-virtual-server", optional = true)
    String defaultVirtualServer;
    @Param(name = "servername", optional = true)
    String serverName;
    @Param(name = "xpowered", optional = true, defaultValue = "true")
    Boolean xPoweredBy;
    @Param(name = "acceptorthreads", optional = true)
    String acceptorThreads;
    @Param(name = "redirectport", optional = true)
    String redirectPort;
    @Param(name = "securityenabled", optional = true, defaultValue = "false")
    Boolean securityEnabled;
    @Param(optional = true, defaultValue = "true")
    Boolean enabled;
    @Param(optional = true, defaultValue = "false")
    Boolean secure; //FIXME
    @Param(name = "listener_id", primary = true)
    String listenerId;
    @Inject
    Configs configs;
    @Inject
    Habitat habitat;
    @Inject
    CommandRunner runner;
    @Inject
    Logger logger;
    private static final String DEFAULT_TRANSPORT = "tcp";

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and
     * the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        List<Config> configList = configs.getConfig();
        Config config = configList.get(0);
        NetworkConfig networkConfig = config.getNetworkConfig();
        HttpService httpService = config.getHttpService();
        if (!(verifyUniqueName(report, networkConfig) && verifyUniquePort(report, networkConfig)
            && verifyDefaultVirtualServer(report))) {
            return;
        }
        VirtualServer vs = httpService.getVirtualServerByName(defaultVirtualServer);
        boolean listener = false;
        boolean protocol = false;
        boolean transport = false;
        try {
            transport = createOrGetTransport(null);
            protocol = createProtocol(context);
            createHttp(context);
            final ThreadPool threadPool = getThreadPool(networkConfig);
            listener = createNetworkListener(networkConfig, transport, threadPool);
            updateVirtualServer(vs);
        } catch (TransactionFailure e) {
            try {
                if (listener) {
                    deleteListener(context);
                }
                if (protocol) {
                    deleteProtocol(context);
                }
                if (transport) {
                    deleteTransport(context);
                }
            } catch (Exception e1) {
                logger.log(Level.INFO, e.getMessage(), e);
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
            logger.log(Level.INFO, e.getMessage(), e);
            report.setMessage(localStrings.getLocalString("create.http.listener.fail",
                "Creation of: " + listenerId + "failed because of: " + e.getMessage(), listenerId, e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private void updateVirtualServer(VirtualServer vs) throws TransactionFailure {
        //now change the associated virtual server
        ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {
            public Object run(VirtualServer avs) throws PropertyVetoException {
                String DELIM = ",";
                String lss = avs.getNetworkListeners();
                boolean listenerShouldBeAdded = true;
                if (lss == null || lss.length() == 0) {
                    lss = listenerId; //the only listener in the list
                } else if (!lss.contains(listenerId)) { //listener does not already exist
                    if (!lss.endsWith(DELIM)) {
                        lss += DELIM;
                    }
                    lss += listenerId;
                } else { //listener already exists in the list, do nothing
                    listenerShouldBeAdded = false;
                }
                if (listenerShouldBeAdded) {
                    avs.setNetworkListeners(lss);
                }
                return avs;
            }
        }, vs);
    }

    private boolean createNetworkListener(NetworkConfig networkConfig, final boolean newTransport,
        final ThreadPool threadPool) throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<NetworkListeners>() {
            public Object run(NetworkListeners listenersParam)
                throws TransactionFailure {
                final NetworkListener newListener = listenersParam.createChild(NetworkListener.class);
                newListener.setName(listenerId);
                newListener.setAddress(listenerAddress);
                newListener.setPort(listenerPort);
                newListener.setTransport(newTransport ? listenerId : DEFAULT_TRANSPORT);
                newListener.setProtocol(listenerId);
                newListener.setThreadPool(threadPool.getName());
                newListener.setEnabled(enabled.toString());
                listenersParam.getNetworkListener().add(newListener);
                return newListener;
            }
        }, networkConfig.getNetworkListeners());
        habitat.getComponent(Transactions.class).waitForDrain();
        return true;
    }

    private boolean verifyDefaultVirtualServer(ActionReport report) {
        if (defaultVS == null && defaultVirtualServer == null) {
            report.setMessage(localStrings.getLocalString("create.http.listener.vs.blank",
                "A default virtual server is required.  Please use --default-virtual-server to specify this value."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        if (defaultVS != null && defaultVirtualServer != null && !defaultVS.equals(defaultVirtualServer)) {
            report.setMessage(localStrings.getLocalString("create.http.listener.vs.bothparams",
                "--defaultVS and --default-virtual-server conflict.  Please use only --default-virtual-server"
                    + " to specify this value."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        } else if (defaultVirtualServer == null && defaultVS != null) {
            defaultVirtualServer = defaultVS;
        }
        //no need to check the other things (e.g. id) for uniqueness
        // ensure that the specified default virtual server exists
        if (!defaultVirtualServerExists()) {
            report.setMessage(localStrings.getLocalString("create.http.listener.vs.notexists",
                "Virtual Server, {0} doesn't exist", defaultVirtualServer));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        return true;
    }

    private boolean verifyUniquePort(ActionReport report, NetworkConfig networkConfig) {
        //check port uniqueness, only for same address
        for (NetworkListener listener : networkConfig.getNetworkListeners()
            .getNetworkListener()) {
            if (listener.getPort().trim().equals(listenerPort) &&
                listener.getAddress().trim().equals(listenerAddress)) {
                String def = "Port is already taken by another listener, choose another port.";
                String msg = localStrings
                    .getLocalString("port.occupied", def, listenerPort, listener.getName(), listenerAddress);
                report.setMessage(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return false;
            }
        }
        return true;
    }

    private boolean verifyUniqueName(ActionReport report, NetworkConfig networkConfig) {
        // ensure we don't already have one of this name
        for (NetworkListener listener : networkConfig.getNetworkListeners().getNetworkListener()) {
            if (listener.getName().equals(listenerId)) {
                report.setMessage(localStrings.getLocalString("create.http.listener.duplicate",
                    "Http Listener named {0} already exists.", listenerId));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return false;
            }
        }
        return true;
    }

    private ThreadPool getThreadPool(NetworkConfig config) {
        final List<ThreadPool> pools = config.getParent(Config.class).getThreadPools().getThreadPool();
        ThreadPool target = null;
        for (ThreadPool pool : pools) {
            if ("http-thread-pool".equals(pool.getName())) {
                target = pool;
            }
        }
        if (target == null && !pools.isEmpty()) {
            target = pools.get(0);
        }
        return target;
    }

    private boolean createOrGetTransport(final AdminCommandContext context) throws TransactionFailure {
        boolean newTransport = false;
        if (habitat.getComponent(Transport.class, DEFAULT_TRANSPORT) == null) {
            final CreateTransport command = (CreateTransport) runner
                .getCommand("create-transport", context.getActionReport(), context.getLogger());
            command.transportName = listenerId;
            command.acceptorThreads = acceptorThreads;
            command.execute(context);
            checkProgress(context);
            newTransport = true;
        }
        return newTransport;
    }

    private boolean createProtocol(final AdminCommandContext context) throws TransactionFailure {
        final CreateProtocol command = (CreateProtocol) runner
            .getCommand("create-protocol", context.getActionReport(), context.getLogger());
        command.protocolName = listenerId;
        command.securityEnabled = securityEnabled;
        command.execute(context);
        checkProgress(context);
        return true;
    }

    private boolean createHttp(final AdminCommandContext context) throws TransactionFailure {
        final CreateHttp command = (CreateHttp) runner
            .getCommand("create-http", context.getActionReport(), context.getLogger());
        command.protocolName = listenerId;
        command.defaultVirtualServer = defaultVirtualServer;
        command.xPoweredBy = xPoweredBy;
        command.serverName = serverName;
        command.execute(context);
        checkProgress(context);
        return true;
    }

    private void checkProgress(final AdminCommandContext context) throws TransactionFailure {
        if(context.getActionReport().getActionExitCode() != ExitCode.SUCCESS) {
            throw new TransactionFailure(context.getActionReport().getMessage());
        }
    }

    private boolean deleteProtocol(final AdminCommandContext context) {
        final DeleteProtocol command = (DeleteProtocol) runner
            .getCommand("delete-protocol", context.getActionReport(), context.getLogger());
        command.protocolName = listenerId;
        command.execute(context);
        return true;
    }

    private boolean deleteTransport(final AdminCommandContext context) {
        final DeleteTransport command = (DeleteTransport) runner
            .getCommand("delete-transport", context.getActionReport(), context.getLogger());
        command.transportName = listenerId;
        command.execute(context);
        return true;
    }

    private boolean deleteListener(final AdminCommandContext context) {
        final DeleteNetworkListener command = (DeleteNetworkListener) runner
            .getCommand("delete-network-listener", context.getActionReport(), context.getLogger());
        command.networkListenerName = listenerId;
        command.execute(context);
        return true;
    }

    private boolean defaultVirtualServerExists() {
        return (defaultVirtualServer != null) && (habitat.getComponent(VirtualServer.class, defaultVirtualServer)
            != null);
    }
}
