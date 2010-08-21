/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.enterprise.v3.admin;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.experimentalgfapi.Deployer;
import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

import java.io.File;
import java.net.URI;
import java.util.Map;

/**
 * This is an implementation of {@link Deployer}.
 * Unlike the other EmbeddedDeployer, this deployer uses admin command execution
 * framework to execute the underlying command, as a result we don't by-pass things like command replication code.
 * 
 * @author Sanjeeb.Sahoo@Sun.COM
 */

@Service()
@ContractProvided(Deployer.class)
// bcos Deployer interface can't depend on HK2, we need ContractProvided here.
public class DeployerImpl implements Deployer {
    // TODO(Sahoo): Fix parameter mappings
    @Inject
    CommandRunner commandRunner;
    @Inject
    Habitat habitat;

    public String deploy(File archive, Map<String, String> params) {
        ActionReport actionReport = createActionReport();
        CommandRunner.CommandInvocation inv =
                commandRunner.getCommandInvocation("deploy", actionReport);
        ParameterMap commandParams = new ParameterMap();
        for (Map.Entry<String, String> e : params.entrySet()) {
            commandParams.add(e.getKey(), e.getValue());
        }
        commandParams.add("path", archive.getAbsolutePath());
        inv.parameters(commandParams).execute();
        switch (actionReport.getActionExitCode()) {
            case FAILURE:
            {
                throw new RuntimeException(actionReport.getMessage(), actionReport.getFailureCause());
            }
            case SUCCESS:
            {
                String name = params.get("name");
                if (name != null) {
                    return name;
                }
                break;
            }
            case WARNING:
            {
                System.out.println("Warnings: " + actionReport.getMessage());
                String name = params.get("name");
                if (name != null) {
                    return name;
                }
                break;
            }
        }
        return null;
    }

    private ActionReport createActionReport() {return habitat.getComponent(ActionReport.class, "plain");}

    public String deploy(URI archive) {
        return null;  //TODO(Sahoo): Not Yet Implemented
    }

    public void undeploy(String appName, Map<String, String> params) {
        ActionReport actionReport = createActionReport();
        CommandRunner.CommandInvocation inv =
                commandRunner.getCommandInvocation("undeploy", actionReport);
        ParameterMap commandParams = new ParameterMap();
        for (Map.Entry<String, String> e : params.entrySet()) {
            commandParams.add(e.getKey(), e.getValue());
        }
        commandParams.add("name", appName);
        inv.parameters(commandParams).execute();
        switch (actionReport.getActionExitCode()) {
            case FAILURE:
                throw new RuntimeException(actionReport.getMessage(), actionReport.getFailureCause());
            default:
                System.out.println(actionReport.getMessage());
        }
    }
}
