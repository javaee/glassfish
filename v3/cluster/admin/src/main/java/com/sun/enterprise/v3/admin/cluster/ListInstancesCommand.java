/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.cluster.InstanceInfo;
import com.sun.enterprise.util.net.NetUtils;
import com.sun.grizzly.config.dom.NetworkListener;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.logging.*;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.component.PerLookup;

/**
 * AdminCommand to list all instances and their states
 *
 * @author Byron Nevins
 */
@Service(name = "list-instances")
@I18n("list.instances.command")
@Scoped(PerLookup.class)

public class ListInstancesCommand implements AdminCommand {
    //@Inject(name = ServerEnvironment.DEFAULT_INSTANCE_NAME)
    //private Server dasServer;
    @Inject
    private ServerEnvironment env;
    @Inject
    private Servers servers;
    @Inject
    private Configs configs;
    private List<InstanceInfo> infos = new LinkedList<InstanceInfo>();
    final private static LocalStringsImpl strings = new LocalStringsImpl(ListInstancesCommand.class);

    @Override
    public void execute(AdminCommandContext context) {
        // setup
        ActionReport report = context.getActionReport();
        Logger logger = context.getLogger();
        List<Server> serverList = servers.getServer();

        // Require that we be a DAS
        if(!env.isDas()) {
            String msg = strings.get("list.instances.onlyRunsOnDas");
            logger.warning(msg);
            report.setActionExitCode(ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }
        
        // Gather a list of InstanceInfo -- one per instance in domain.xml
        for(Server server : serverList) {
            String name = server.getName();

            // skip ourself
            if(name != null && !name.equals(SystemPropertyConstants.DAS_SERVER_NAME)) {
                InstanceInfo ii = new InstanceInfo(
                        name, getAdminPort(server), server.getNodeAgentRef());
                infos.add(ii);
            }
        }

        // report the list
        StringBuilder sb = new StringBuilder("*** list-instances ***\n");


        for(InstanceInfo info : infos) {
            sb.append(info).append('\n');
        }
        
        report.setActionExitCode(ExitCode.SUCCESS);
        report.setMessage(sb.toString());
    }

    private int getAdminPort(Server server) {
        try {
            Config config = getConfig(server);

            if(config != null) {
                List<NetworkListener> listeners = config.getNetworkConfig().getNetworkListeners().getNetworkListener();

                for(NetworkListener listener : listeners) {
                    if("admin-listener".equals(listener.getProtocol()))
                        return Integer.parseInt(listener.getPort());
                }
            }
        }
        catch (Exception e) {
            // handled below...
        }
        return -1;
    }

    private Config getConfig(Server server) {
        if(server != null) {
            String cfgName = server.getConfigRef();

            if(StringUtils.ok(cfgName))
                for(Config config : configs.getConfig())
                    if(cfgName.equals(config.getName()))
                        return config;
        }
        return null;
    }
}



