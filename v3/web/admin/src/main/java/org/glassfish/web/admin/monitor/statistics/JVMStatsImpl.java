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
package org.glassfish.web.admin.monitor.statistics;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.CountStatistic; 
import javax.management.j2ee.statistics.TimeStatistic;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.JVMStats;
import org.glassfish.admin.monitor.cli.MonitorContract;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.datatree.TreeNode;
import java.lang.management.MemoryUsage;
import org.glassfish.flashlight.datatree.MethodInvoker;
import java.util.logging.Logger;
import java.util.logging.Level;

/** 
 *
 * For v3 Prelude, following stats will be available
 * server.jvm.committedHeapSize java.lang.management.MemoryUsage
 * init, used, committed, max
 *
 */
//public class JVMStatsImpl implements JVMStats, MonitorContract {
@Service
public class JVMStatsImpl implements MonitorContract {

    @Inject
    private MonitoringRuntimeDataRegistry mrdr;

    @Inject
    Logger logger;

    private final String name = "jvm";
    private final String displayFormat = 
        "%1$-10s %2$-10s %3$-10s %4$-10s";

    public String getName() {
        return name;
    }

    public ActionReport process(final ActionReport report, final String filter) {

        if (mrdr == null) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage("MonitoringRuntimeDataRegistry is null");
            return report;
        }

        TreeNode serverNode = mrdr.get("server");
        if (serverNode == null) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage("MonitoringRuntimeDataRegistry server node is null");
            return report;
        }

        long init = 0;
        long used = 0;
        long committed = 0;
        long max = 0;

        MethodInvoker tn = (MethodInvoker) (serverNode.getNode("jvm")).getNode("committedHeapSize");
        //TreeNode tn = (serverNode.getNode("jvm")).getNode("committedHeapSize");

        logger.finest("JVMStatsImpl: tn name = " + tn.getName());
        logger.finest("JVMStatsImpl: tn class name = " + (tn.getClass()).getName());
        logger.finest("JVMStatsImpl: tn value = " + tn.getValue());
        logger.finest("JVMStatsImpl: tn value class name = " + ((tn.getValue()).getClass()).getName());
        logger.finest("JVMStatsImpl: tn instance = " + tn.getInstance());
        logger.finest("JVMStatsImpl: tn instance class name = " + ((tn.getInstance()).getClass()).getName());

        MemoryUsage mu = (MemoryUsage) tn.getInstance();

        report.setMessage(String.format(displayFormat, 
            mu.getInit(), mu.getUsed(), mu.getCommitted(), mu.getMax()));
        report.setActionExitCode(ExitCode.SUCCESS);
        return report;
    }

    public Statistic[] getStatistics() {
    	return null;
    }

    public String[] getStatisticNames() {
    	return null;
    }

    public Statistic getStatistic(String statisticName) {
    	return null;
    }
}
