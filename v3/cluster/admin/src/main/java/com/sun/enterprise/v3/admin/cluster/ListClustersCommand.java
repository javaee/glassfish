/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import com.sun.enterprise.admin.util.InstanceStateService;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.cluster.InstanceInfo;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.InstanceState;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.PostConstruct;

import java.util.List;
import java.util.LinkedList;
import java.util.logging.Logger;
import static com.sun.enterprise.v3.admin.cluster.Constants.*;
import com.sun.enterprise.admin.util.RemoteInstanceCommandHelper;

/**
 *  This is a remote command that lists the clusters.
 * Usage: list-clusters

 * @author Bhakti Mehta
 * @author Byron Nevins
 */
@Service(name = "list-clusters")
@Scoped(PerLookup.class)
@I18n("list.clusters.command")
public final class ListClustersCommand implements AdminCommand, PostConstruct {

    @Inject
    private Habitat habitat;
    @Inject
    Domain domain;
    @Inject
    InstanceStateService stateService;
    private RemoteInstanceCommandHelper helper;
    private List<InstanceInfo> infos = new LinkedList<InstanceInfo>();
    private static final String NONE = "Nothing to list.";
    private static final String EOL = "\n";

    @Override
    public void postConstruct() {
        helper = new RemoteInstanceCommandHelper(habitat);
    }

    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        Logger logger = context.getLogger();
        ActionReport.MessagePart top = report.getTopMessagePart();

        Clusters clusters = domain.getClusters();
        List<Cluster> clusterList = clusters.getCluster();
        StringBuilder sb = new StringBuilder();
        if (clusterList.size() < 1) {
            sb.append(NONE);
        }

        boolean atleastOneInstanceRunning = false;
        boolean allInstancesRunning = true;
        int timeoutInMsec = 2000;

        //List the cluster and also the state
        //A cluster is a three-state entity and
        //list-cluster should return one of the following:

        //running (all instances running)
        //not running (no instance running)
        //partially running (at least 1 instance is not running)

        // bnevins: hassle to not have an extra linefeed at the end
        boolean firstCluster = true;

        for (Cluster cluster : clusterList) {
            String clusterName = cluster.getName();

            List<Server> servers = cluster.getInstances();

            for (Server server : servers) {
                String name = server.getName();

                if (name != null) {
                    InstanceInfo ii = new InstanceInfo(
                            name, helper.getAdminPort(server), server.getAdminHost(),
                            clusterName, logger, timeoutInMsec);
                    infos.add(ii);
                    InstanceState.StateType state = (ii.isRunning()) ?
                            (stateService.setState(name, InstanceState.StateType.RUNNING, false)) :
                            (stateService.setState(name, InstanceState.StateType.NO_RESPONSE, false));
                    allInstancesRunning &= ii.isRunning();
                    if (ii.isRunning()) {
                        atleastOneInstanceRunning = true;
                    }
                }
            }

            String display;
            String value;

            if (servers.isEmpty() || !atleastOneInstanceRunning) {
                display = InstanceState.StateType.NOT_RUNNING.getDisplayString();
                value = InstanceState.StateType.NOT_RUNNING.getDescription();
            }
            else if (allInstancesRunning) {
                display = InstanceState.StateType.RUNNING.getDisplayString();
                value = InstanceState.StateType.RUNNING.getDescription();
            }
            else {
                display = PARTIALLY_RUNNING_DISPLAY;
                value = PARTIALLY_RUNNING;
            }

            // do not put an extraneous linefeed at the end!
            if (firstCluster)
                firstCluster = false;
            else
                sb.append(EOL);

            sb.append(clusterName).append(display);
            top.addProperty(clusterName, value);
        }
        report.setMessage(sb.toString());
    }
}
