/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
import com.sun.enterprise.util.cluster.InstanceInfo;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.PostConstruct;

import java.util.List;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 *  This is a remote command that lists the clusters.
 * Usage: list-clusters
 	
 * @author Bhakti Mehta
 */
@Service(name = "list-clusters")
@Scoped(PerLookup.class)
public final class ListClustersCommand implements AdminCommand, PostConstruct {

    @Inject
    Domain domain;

    @Inject
    private ServerEnvironment env;

    @Inject
    private Servers servers;

    @Inject
    private Configs configs;

    private RemoteInstanceCommandHelper helper;

    private List<InstanceInfo> infos = new LinkedList<InstanceInfo>();

    private static final String NONE = "Nothing to list.";


    private static final String RUNNING = "running";
    private static final String NOT_RUNNING = "not running";
    private static final String PARTIALLY_RUNNING = "partially running";

     @Override
    public void postConstruct() {
        helper = new RemoteInstanceCommandHelper(env, servers, configs);
    }

    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        Logger logger = context.getLogger();

        Clusters clusters = domain.getClusters();
        List<Cluster> clusterList = clusters.getCluster();
        StringBuffer sb = new StringBuffer();
        if (clusterList.size()<1) {
            sb.append(NONE);
        }

        boolean atleastOneInstanceRunning = false ;
        boolean allInstancesRunning = true;
        int timeoutInMsec = 2000;

        //List the cluster and also the state
        //A cluster is a three-state entity and
        //list-cluster should return one of the following:

        //running (all instances running)
        //not running (no instance running)
        //partially running (at least 1 instance is not running)

        for (Cluster cluster : clusterList) {
            List<Server> servers = cluster.getInstances();
            for (Server server:servers) {
                String name = server.getName();
                if (name != null) {
                    InstanceInfo ii = new InstanceInfo(
                        name, helper.getAdminPort(server), helper.getHost(server), logger, timeoutInMsec);
                    infos.add(ii);

                    allInstancesRunning &= ii.isRunning();
                    if (ii.isRunning()) {
                        atleastOneInstanceRunning = true;
                    }

                }
                
            }
            String state = "";
            if (allInstancesRunning && atleastOneInstanceRunning){
                state = RUNNING;
            }  else if (!allInstancesRunning && !(atleastOneInstanceRunning)) {
                state = NOT_RUNNING;
            }  else state = PARTIALLY_RUNNING;
            sb.append(cluster.getName()).append(' ' ).append(state).append('\n');

        }
        report.addSubActionsReport().setMessage(sb.toString() );
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

}
