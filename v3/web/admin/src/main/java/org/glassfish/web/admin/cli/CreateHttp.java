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

import java.util.List;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.grizzly.config.dom.FileCache;
import com.sun.grizzly.config.dom.Http;
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
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Command to create http element within a protocol element
 *
 * Sample Usage : create-http protocol_name
 *
 * domain.xml element example
 *
 * <http max-connections="250" default-virtual-server="server" server-name=""> <file-cache enabled="false" /> </http>
 *
 * @author Justin Lee
 */
@Service(name = "create-http")
@Scoped(PerLookup.class)
@I18n("create.http")
public class CreateHttp implements AdminCommand {
    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CreateHttp.class);

    @Param(name = "protocolname", primary = true)
    String protocolName;

    @Param(name = "request-timeout-seconds", defaultValue = "30", optional = true)
    String requestTimeoutSeconds;

    @Param(name = "timeout-seconds", defaultValue = "30", optional = true)
    String timeoutSeconds;

    @Param(name = "max-connection", defaultValue = "256", optional = true)
    String maxConnections;

    @Param(name = "default-virtual-server")
    String defaultVirtualServer;

    @Param(name = "dns-lookup-enabled", defaultValue = "false", optional = true)
    Boolean dnsLookupEnabled = false;

    @Param(name = "servername", optional = true)
    String serverName;

    @Param(name = "xpowered", optional = true, defaultValue = "true")
    Boolean xPoweredBy = false;

    @Inject
    Configs configs;

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and
     * the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        // check for duplicates
        List<Config> configList = configs.getConfig();
        Config config = configList.get(0);
        Protocols protocols = config.getNetworkConfig().getProtocols();

        if (config.getNetworkConfig().findProtocol(protocolName) == null) {
            report.setMessage(localStrings.getLocalString("create.http.fail.protocolnotfound",
                "The specified protocol {0} is not yet configured. " +
                "Please create one", protocolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        Protocol protocol = null;
        for (Protocol p : protocols.getProtocol()) {
            if (p.getName().equalsIgnoreCase(protocolName) && p.getHttp() != null) {
                report.setMessage(localStrings.getLocalString("create.http.fail.duplicate",
                    "An http element for {0} already exists. Cannot add duplicate http", protocolName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            } else {
                protocol = p;
            }
        }

        // Add to the <network-config>
        try {
            ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                public Object run(Protocol param) throws TransactionFailure {
                    Http http = param.createChild(Http.class);
                    final FileCache cache = http.createChild(FileCache.class);
                    cache.setEnabled("false");
                    http.setFileCache(cache);
                    http.setDefaultVirtualServer(defaultVirtualServer);
                    http.setDnsLookupEnabled(dnsLookupEnabled == null ? null : dnsLookupEnabled.toString());
                    http.setMaxConnections(maxConnections);
                    http.setRequestTimeoutSeconds(requestTimeoutSeconds);
                    http.setTimeoutSeconds(timeoutSeconds);
                    http.setXpoweredBy(xPoweredBy == null ? null : xPoweredBy.toString());
                    http.setServerName(serverName);
                    param.setHttp(http);
                    return http;
                }
            }, protocol);
        } catch (TransactionFailure e) {
            report.setMessage(
                localStrings.getLocalString("create.http.fail",
                    "Failed to create http for {0}: " + (e.getMessage() == null ? "No reason given." : e.getMessage()),
                    protocolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}