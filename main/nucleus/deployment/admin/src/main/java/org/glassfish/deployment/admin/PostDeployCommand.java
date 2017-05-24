/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
import com.sun.enterprise.config.serverbeans.Domain;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.Supplemental;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext.Phase;
import javax.inject.Inject;
import org.glassfish.api.admin.AccessRequired;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * Causes InstanceDeployCommand executions on the correct remote instances.
 *
 * @author tjquinn
 */
@Service(name="_postdeploy")
@Supplemental(value="deploy", ifFailure=FailurePolicy.Warn)
@PerLookup
@ExecuteOn(value={RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST, 
        path="_postdeploy", 
        description="_postdeploy")
})
@AccessRequired(resource=DeploymentCommandUtils.APPLICATION_RESOURCE_NAME, action="write")
public class PostDeployCommand extends DeployCommandParameters implements AdminCommand {

    @Inject
    private ServiceLocator habitat;

    @Inject
    private Deployment deployment;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        final DeployCommandSupplementalInfo suppInfo =
                context.getActionReport().getResultType(DeployCommandSupplementalInfo.class);
        final DeploymentContext dc = suppInfo.deploymentContext();
        final DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
        final InterceptorNotifier notifier = new InterceptorNotifier(habitat, dc);
        

        // if the target is DAS, we do not need to do anything more
        if (DeploymentUtils.isDASTarget(params.target)) {
            return;
        }

        try {
            final ParameterMap paramMap = deployment.prepareInstanceDeployParamMap(dc);

            List<String> targets = new ArrayList<String>();
            if (!DeploymentUtils.isDomainTarget(params.target)) {
                targets.add(params.target);
            } else {
                targets = suppInfo.previousTargets();
            }
            ClusterOperationUtil.replicateCommand(
                "_deploy",
                FailurePolicy.Warn,
                FailurePolicy.Warn,
                FailurePolicy.Ignore,
                targets,
                context,
                paramMap,
                habitat);
            notifier.ensureAfterReported(Phase.REPLICATION);
        } catch (Exception e) {
            report.failure(logger, e.getMessage());
        }
    }
}
