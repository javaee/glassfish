/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.NetworkListeners;
import com.sun.grizzly.config.dom.Protocol;
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

/**
 * Command to create Network Listener
 */
@Service(name = "create-network-listener")
@Scoped(PerLookup.class)
@I18n("create.network.listener")
public class CreateNetworkListener implements AdminCommand {
    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CreateNetworkListener.class);
    @Param(name = "address", optional = true)
    String address;
    @Param(name = "listenerport", optional = false)
    String port;
    @Param(name = "threadpool", optional = true, defaultValue = "http-thread-pool")
    String threadPool;
    @Param(name = "protocol", optional = false)
    String protocol;
    @Param(name = "name", primary = true)
    String listenerName;
    @Param(name = "transport", optional = true, defaultValue = "tcp")
    String transport;
    @Param(name = "enabled", optional = true, defaultValue = "true")
    Boolean enabled;
    @Param(name="jkenabled", optional=true, defaultValue = "false")
    Boolean jkEnabled;

    @Inject
    Configs configs;
    @Inject
    Habitat habitat;

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
        NetworkListeners nls = networkConfig.getNetworkListeners();
        // ensure we don't have one of this name already
        for (NetworkListener networkListener : nls.getNetworkListener()) {
            if (networkListener.getName().equals(listenerName)) {
                report.setMessage(localStrings.getLocalString(
                    "create.network.listener.fail.duplicate",
                    "Network Listener named {0} already exists.",
                    listenerName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        if (!verifyUniquePort(networkConfig)) {
            String def = "Port is already taken by another listener, choose another port.";
            //String msg = localStrings
             //   .getLocalString("port.occupied", def, port, listenerName, address);
            report.setMessage(def);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        Protocol prot = networkConfig.findProtocol(protocol);
        if (prot == null || prot.getHttp() == null) {
               report.setMessage(localStrings.getLocalString(
                    "create.network.listener.fail.nohttp",
                    "Network Listener named {0} refers to protocol {1} that doesn't exist or has no http configured",
                    listenerName, protocol));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<NetworkListeners>() {
                public Object run(NetworkListeners param) throws TransactionFailure {
                    NetworkListener newNetworkListener = param.createChild(NetworkListener.class);
                    newNetworkListener.setProtocol(protocol);
                    newNetworkListener.setTransport(transport);
                    newNetworkListener.setEnabled(enabled.toString());
                    newNetworkListener.setJkEnabled(jkEnabled.toString());
                    newNetworkListener.setPort(port);
                    newNetworkListener.setThreadPool(threadPool);
                    newNetworkListener.setName(listenerName);
                    newNetworkListener.setAddress(address);
                    param.getNetworkListener().add(newNetworkListener);
                    return newNetworkListener;
                }
            }, nls);
            updateVirtualServer(prot);
        } catch (TransactionFailure e) {
            e.printStackTrace();
            report.setMessage(
                localStrings.getLocalString("create.network.listener.fail", "{0} create failed: "
                    + (e.getMessage() == null ? "No reason given" : e.getMessage()), listenerName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private void updateVirtualServer(final Protocol prot) throws TransactionFailure {
        //final Protocol prot = listener.findHttpProtocol();
        if(prot != null) {
            ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {
                public Object run(final VirtualServer param) throws PropertyVetoException {
                    param.addNetworkListener(listenerName);
                    return null;
                }
            }, habitat.getComponent(VirtualServer.class, prot.getHttp().getDefaultVirtualServer()));
        }
    }

    private boolean verifyUniquePort(NetworkConfig networkConfig) {
        //check port uniqueness, only for same address
        for (NetworkListener listener : networkConfig.getNetworkListeners()
            .getNetworkListener()) {
            if (listener.getPort().trim().equals(port) &&
                listener.getAddress().trim().equals(address)) {
                return false;
            }
        }
        return true;
    }
}
