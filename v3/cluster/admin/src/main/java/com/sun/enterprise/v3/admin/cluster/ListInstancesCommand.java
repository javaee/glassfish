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

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.cluster.InstanceInfo;
import java.util.*;
import java.util.logging.*;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.*;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.component.*;

/**
 * AdminCommand to list all instances and their states
 *
 * @author Byron Nevins
 */
@Service(name = "list-instances")
@I18n("list.instances.command")
@Scoped(PerLookup.class)
public class ListInstancesCommand implements AdminCommand, PostConstruct {
    //@Inject(name = ServerEnvironment.DEFAULT_INSTANCE_NAME)
    //private Server dasServer;
    @Inject
    private ServerEnvironment env;
    @Inject
    private Servers servers;
    @Inject
    private Configs configs;
    @Param(optional=true, defaultValue="2000")
    String timeoutInMsecString;

    private List<InstanceInfo> infos = new LinkedList<InstanceInfo>();

    @Override
    public void postConstruct() {
        helper = new RemoteInstanceCommandHelper(env, servers, configs);
    }

    @Override
    public void execute(AdminCommandContext context) {
        // setup
        int timeoutInMsec;

        try {
            timeoutInMsec = Integer.parseInt(timeoutInMsecString);
        }
        catch(Exception e) {
            timeoutInMsec = 2000;
        }
        
        ActionReport report = context.getActionReport();
        Logger logger = context.getLogger();
        List<Server> serverList = servers.getServer();

        // Require that we be a DAS
        if(!helper.isDas()) {
            String msg = Strings.get("list.instances.onlyRunsOnDas");
            logger.warning(msg);
            report.setActionExitCode(ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        // Gather a list of InstanceInfo -- one per instance in domain.xml
        for(Server server : serverList) {
            String name = server.getName();

            // skip ourself
            // TODO I'm assuming for now that GetNodeAgentRef's value is the
            //remote hostname
            if(name != null && !name.equals(SystemPropertyConstants.DAS_SERVER_NAME)) {
                InstanceInfo ii = new InstanceInfo(
                        name, helper.getAdminPort(server), helper.getHost(server), logger, timeoutInMsec);
                infos.add(ii);
            }
        }

        // report the list
        StringBuilder sb = new StringBuilder();

		if(infos.size() < 1) {
			sb.append(Strings.get("list.instances.none"));
		}
		else {    
			sb.append(InstanceInfo.getFormattedHeader()).append('\n');
			boolean first = true;	// no useless extra "\n" at end

			for(InstanceInfo info : infos) {
				if(first)
					first = false;
				else
					sb.append('\n');

				sb.append(info.toFormattedString());
			}
        }

        report.setActionExitCode(ExitCode.SUCCESS);
        report.setMessage(sb.toString());
    }

    private RemoteInstanceCommandHelper helper;
}
