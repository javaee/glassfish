/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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


import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;

/*
 * ClusterCommandHelper is a helper class that knows how to execute an
 * asadmin command in the DAS for each instance in a cluster. For example
 * it is used by start-cluster and stop-cluster to execute start-instance
 * or stop-instance for each instance in a cluster. Note this is not the
 * same as cluster replication where general commands are executed on
 * each instances of a cluster.
 *
 * @author Joe Di Pol
 */
class ClusterCommandHelper {

    private static final String NL = System.getProperty("line.separator");

    private Domain domain;

    private CommandRunner runner;

    /**
     * Construct a ClusterCommandHelper
     *
     * @param domain The Domain we are running in
     * @param runner A CommandRunner to use for running commands
     */
    ClusterCommandHelper(Domain domain, CommandRunner runner) {
        this.domain = domain;
        this.runner = runner;
    }

    /**
     * Loop through all instances in a cluster and execute a command for
     * each one.
     *
     * @param command       The string of the command to run. The instance
     *                      name will be used as the operand for the command.
     * @param map           A map of parameters to use for the command. May be
     *                      null if no parameters. When the command is
     *                      executed for a server instance, the instance name
     *                      is set as the DEFAULT parameter (operand)
     * @param clusterName   The name of the cluster containing the instances
     *                      to run the command against.
     * @param context       The AdminCommandContext to use when executing the
     *                      command.
     * @param verbose       true for more verbose output
     * @return              An ActionReport containing the results
     * @throws CommandException
     */
    ActionReport runCommand(
            String  command,
            ParameterMap map,
            String  clusterName,
            AdminCommandContext context,
            boolean verbose) throws CommandException {

        Logger logger = context.getLogger();
        ActionReport report = context.getActionReport();

        // Get the cluster specified by clusterName
        Cluster cluster = domain.getClusterNamed(clusterName);
        if (cluster == null) {
            String msg = Strings.get("cluster.command.unknownCluster",
                    clusterName);
            throw new CommandException(msg);
        }

        // Get the list of servers in the cluster.
        List<Server> targetServers = domain.getServersInTarget(clusterName);

        // If the cluster is empty, say so
        if (targetServers == null || targetServers.isEmpty()) {
            report.setActionExitCode(ExitCode.SUCCESS);
            report.setMessage(Strings.get("cluster.command.noInstances",
                                            clusterName));
            return report;
        }

        // We will save the name of the instances that worked and did
        // not work so we can summarize our results.
        StringBuilder failedServerNames = new StringBuilder();
        StringBuilder succeededServerNames = new StringBuilder();
        ReportResult reportResult = new ReportResult();
        boolean failureOccurred = false;

        // Save command output to return in ActionReport
        StringBuilder output = new StringBuilder();

        CommandInvocation invocation = runner.getCommandInvocation(
                        command, report);

        if (map == null) {
            map = new ParameterMap();
        }

        // XXX dipol
        // We sequentially loop through the servers and execute the command.
        // We really should spin off threads so we can do some of these
        // in parallel. Rumor is the cluster command replication code will
        // provide a utility that we will be able to leverage.
        for (Server server : targetServers) {
            String msg;

            // Set the instance name as the operand for the commnd
            map.set("DEFAULT", server.getName());
            invocation.parameters(map);

            msg = command + " " + server.getName();
            logger.info(msg);
            if (verbose) {
                output.append(msg).append(NL);
            }
            report.setActionExitCode(ExitCode.SUCCESS);
            invocation.execute();
            if (report.getActionExitCode() != ExitCode.SUCCESS) {
                // Bummer, the command had an error. Log and save output
                failureOccurred = true;
                failedServerNames.append(server.getName()).append(" ");
                reportResult.failedServerNames.add(server.getName());
                msg = report.getMessage();
                logger.severe(msg);
                output.append(msg).append(NL);
            } else {
                // Command worked. Note that too.
                succeededServerNames.append(server.getName()).append(" ");
                reportResult.succeededServerNames.add(server.getName());
            }
        }

        report.setActionExitCode(ExitCode.SUCCESS);
        
        // too error prone to parse report message to get list of failed or succeeded server names.
        if (failureOccurred) {
            report.setResultType(List.class, reportResult.failedServerNames);
        } else {
            report.setResultType(List.class, reportResult.succeededServerNames);
        }


        // Display summary of started servers if in verbose mode or we
        // had one or more failures.
        if (succeededServerNames.length() > 0 && (verbose || failureOccurred)) {
            output.append(NL + Strings.get("cluster.command.instancesSucceeded",
                    command, succeededServerNames));
        }

        if (failureOccurred) {
            // Display summary of failed servers if we have any
            output.append(NL + Strings.get("cluster.command.instancesFailed",
                    command, failedServerNames));
            if (succeededServerNames.length() > 0) {
                // At least one instance started. Warning.
                report.setActionExitCode(ExitCode.WARNING);
            } else {
                // No instance started. Failure
                report.setActionExitCode(ExitCode.FAILURE);
            }
        }

        report.setMessage(output.toString());
        return report;
    }

    static public class ReportResult {
        final public List<String> succeededServerNames = new LinkedList<String>();
        final public List<String> failedServerNames = new LinkedList<String>();
    }
}


