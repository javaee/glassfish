/*
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

package org.glassfish.deployment.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.Cluster;
import org.glassfish.api.admin.ClusterExecutor;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.Supplemental;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.common.util.admin.ParameterMapExtractor;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

/**
 * Causes InstanceDeployCommand executions on the correct remote instances.
 *
 * @author tjquinn
 */
@Service(name="postdeploy")
@Supplemental(value="deploy", ifFailure=FailurePolicy.Warn)
@Scoped(PerLookup.class)
@Cluster(value={RuntimeType.DAS})

public class PostDeployCommand extends DeployCommandParameters implements AdminCommand {

    private final Collection<String> excludedDeployCommandParamNames =
            initExcludedDeployCommandParamNames();

    @Inject
    private ClusterExecutor clusterExecutor;

    @Override
    public void execute(AdminCommandContext context) {

        final DeployCommandSupplementalInfo suppInfo =
                context.getActionReport().getResultType(DeployCommandSupplementalInfo.class);
        final DeploymentContext dc = suppInfo.deploymentContext();
        final DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);

        final ParameterMap paramMap;
        final ParameterMapExtractor extractor = new ParameterMapExtractor(this);
        try {
            paramMap = extractor.extract(excludedDeployCommandParamNames);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

        final File genPolicyDir = dc.getScratchDir("policy");
        /*
         * The generated policy directory is not always created, so check it
         * before adding it to the parameters.
         */
        if (genPolicyDir.isDirectory()) {
            paramMap.set(
                    InstanceDeployCommand.ParameterNames.GENERATED_POLICY_DIRECTORY,
                    dc.getScratchDir("policy").getAbsolutePath());
        }
        
        paramMap.set(
                InstanceDeployCommand.ParameterNames.GENERATED_XML_DIRECTORY,
                dc.getScratchDir("xml").getAbsolutePath());


        paramMap.set(
                InstanceDeployCommand.ParameterNames.GENERATED_EJB_DIRECTORY,
                dc.getScratchDir("ejb").getAbsolutePath());
        paramMap.set(
                InstanceDeployCommand.ParameterNames.GENERATED_JSP_DIRECTORY,
                dc.getScratchDir("jsp").getAbsolutePath());
        paramMap.set("DEFAULT", suppInfo.archiveFile().getAbsolutePath());
        final File actualPlan = suppInfo.deploymentPlan();
        if (actualPlan != null) {
            paramMap.set(DeployCommandParameters.ParameterNames.DEPLOYMENT_PLAN, actualPlan.getAbsolutePath());
        }

        // always upload the archives to the instance side
        paramMap.set("upload", "true");

        // pass the calculated name so we don't need to recalculate it on
        // instance side
        paramMap.set("name", params.name);

        // pass the params we restored from the previous deployment in case of 
        // redeployment
        if (params.previousContextRoot != null) {
            paramMap.set("preservedcontextroot", params.previousContextRoot);
        }
        paramMap.set("virtualservers", params.virtualservers);
        paramMap.set("libraries", params.libraries);

        // pass the app props so we have the information to persist in the 
        // domain.xml
        Properties appProps = dc.getAppProps();
        // TODO: we need to come back to visit app config
        appProps.remove("appConfig");
        paramMap.set("appprops", extractor.propertiesValue(appProps, ':'));

        clusterExecutor.execute("_deploy", this, context, paramMap);

    }

    private Collection<String> initExcludedDeployCommandParamNames() {
        final Collection<String> result = new ArrayList<String>();
        result.add("path");
        result.add(DeployCommandParameters.ParameterNames.DEPLOYMENT_PLAN);
        result.add("upload"); // We'll force it to true ourselves.
        return result;
    }

    
}
