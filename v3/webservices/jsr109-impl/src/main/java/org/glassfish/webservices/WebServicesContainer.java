/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */


package org.glassfish.webservices;

import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.webservices.monitoring.Deployment109ProbeProvider;
import org.glassfish.webservices.monitoring.WebServiceStatsProviderBootstrap;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;

/**
 * Webservices container service
 *
 */
@Service(name="org.glassfish.webservices.WebServicesContainer")
public class WebServicesContainer implements Container, PostConstruct {
    @Inject
    private Habitat habitat;

    private final Deployment109ProbeProvider wsProbeProvider = new Deployment109ProbeProvider();

    public String getName() {
        return "webservices";
    }

    public void postConstruct() {
        habitat.getByType(WebServiceStatsProviderBootstrap.class);
    }

    /* package */ Deployment109ProbeProvider getDeploymentProbeProvider() {
        return wsProbeProvider;
    }

    public Class<? extends Deployer> getDeployer() {
        return WebServicesDeployer.class;
    }
}

