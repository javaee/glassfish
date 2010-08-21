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

package org.glassfish.deployment.admin;

import com.sun.enterprise.admin.util.ClusterOperationUtil;
import java.util.logging.Logger;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.Cluster;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.Supplemental;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.api.Param;
import org.glassfish.internal.deployment.Deployment;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.glassfish.deployment.common.VersioningDeploymentUtil;
import org.jvnet.hk2.component.Habitat;

/**
 * When the create-application-ref command is invoked with a remote target,
 * we are essentially doing a remote deploy on that target.
 *
 */
@Service(name="postcreateapplicationref")
@Supplemental(value="create-application-ref", ifFailure=FailurePolicy.Warn)
@Scoped(PerLookup.class)
@Cluster(value={RuntimeType.DAS})

public class PostCreateApplicationRefCommand implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(PostCreateApplicationRefCommand.class);

    @Param(primary=true)
    public String name = null;

    @Param(optional=true)
    public String target = "server";

    @Param(optional=true)
    public String virtualservers = null;

    @Param(optional=true, defaultValue="true")
    public Boolean enabled = true;

    @Param(optional=true, defaultValue="true")
    public Boolean lbenabled = true;

    @Inject
    private Deployment deployment;   

    @Inject
    private Habitat habitat;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        final DeployCommandSupplementalInfo suppInfo =
                context.getActionReport().getResultType(DeployCommandSupplementalInfo.class);

        if (suppInfo.isAppRefExists()) {
            // the application ref already exists

            // some versioning specific warning messages are already setup
            // if a versioned name has been provided to the command
            if(VersioningDeploymentUtil.isUntagged(name)){
                report.setMessage(localStrings.getLocalString("appref.already.exists","Application reference {0} already exists in target {1}.", name, target));
                report.setActionExitCode(ActionReport.ExitCode.WARNING);
            }
            return;
        }

        final DeploymentContext dc = suppInfo.deploymentContext();
        final DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);

        // if the target is DAS, we do not need to do anything more
        if (DeploymentUtils.isDASTarget(params.target)) {
            return;
        }

        try {
            final ParameterMap paramMap = deployment.prepareInstanceDeployParamMap(dc);
            final List<String> targets = new ArrayList<String>(Arrays.asList(params.target.split(",")));

            ClusterOperationUtil.replicateCommand(
                "_deploy",
                FailurePolicy.Error,
                FailurePolicy.Warn,
                targets,
                context,
                paramMap,
                habitat);
        } catch (Exception e) {
            report.failure(logger, e.getMessage());
        }
    }
}
