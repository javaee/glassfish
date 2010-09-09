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

package org.glassfish.webservices.cli;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.webservices.WebServicesContainer;
import org.glassfish.webservices.deployment.DeployedEndpointData;
import org.glassfish.webservices.deployment.WebServicesDeploymentMBean;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;

import java.util.Map;
import java.util.Properties;

/**
 * @author Jitendra Kotamraju
 */
@Service(name = "__list-webservices")
@Scoped(PerLookup.class)
public class ListWebServicesCommand implements AdminCommand {
    @Inject
    private Habitat habitat;

    @Param(optional=true)
    String applicationName;

    @Param(optional=true)
    String moduleName;

    @Param(optional=true)
    String servletLink;

    @Param(optional=true)
    String ejbLink;

    @Param(optional=true)
    String endpointName;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        WebServicesContainer container = habitat.getComponent(WebServicesContainer.class);
        WebServicesDeploymentMBean bean = container.getDeploymentBean();

        if (applicationName != null && moduleName != null && servletLink != null) {
            // TODO
        } else if (applicationName != null && moduleName != null && ejbLink != null) {
            // TODO
        } else if (applicationName != null && moduleName != null && endpointName != null) {
            Map<String, Map<String, Map<String, DeployedEndpointData>>> endpoints =
                    bean.getEndpoint(applicationName, moduleName, endpointName);
            fillAllEndpoints(report, endpoints);
        } else if (applicationName != null && moduleName != null) {
            Map<String, Map<String, Map<String, DeployedEndpointData>>> endpoints =
                    bean.getEndpoints(applicationName, moduleName);
            fillAllEndpoints(report, endpoints);
        } else if (applicationName != null) {
            Map<String, Map<String, Map<String, DeployedEndpointData>>> endpoints =
                    bean.getEndpoints(applicationName);
            fillAllEndpoints(report, endpoints);
        } else {
            Map<String, Map<String, Map<String, DeployedEndpointData>>> endpoints = bean.getEndpoints();
            fillAllEndpoints(report, endpoints);
        }

    }

    private void fillAllEndpoints(ActionReport report, Map<String, Map<String, Map<String, DeployedEndpointData>>> endpoints) {
        if (!endpoints.isEmpty()) {
            Properties extra = new Properties();
            extra.putAll(endpoints);
            report.setExtraProperties(extra);
            ActionReport.MessagePart top = report.getTopMessagePart();
            for(Map.Entry<String, Map<String, Map<String, DeployedEndpointData>>> app : endpoints.entrySet()) {
                ActionReport.MessagePart child = top.addChild();
                child.setMessage("application:"+app.getKey());
                for(Map.Entry<String, Map<String, DeployedEndpointData>> module : app.getValue().entrySet()) {
                    child = child.addChild();
                    child.setMessage("  module:"+module.getKey());
                    for(Map.Entry<String, DeployedEndpointData> endpoint : module.getValue().entrySet()) {
                        child = child.addChild();
                        child.setMessage("    endpoint:"+endpoint.getKey());
                        for(Map.Entry<String, String> endpointData : endpoint.getValue().getStaticAsMap().entrySet()) {
                            child = child.addChild();
                            child.setMessage("      "+endpointData.getKey()+":"+endpointData.getValue());
                        }
                    }
                }
            }   
        }
    }

}