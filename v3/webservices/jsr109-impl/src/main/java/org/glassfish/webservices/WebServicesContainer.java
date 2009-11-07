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
import org.glassfish.webservices.deployment.WebServicesDeploymentMBean;
import org.glassfish.gmbal.ManagedObjectManager;
import org.glassfish.gmbal.ManagedObjectManagerFactory;
import org.glassfish.external.amx.AMXGlassfish;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Singleton;

import javax.management.ObjectName;
import java.io.IOException;

/**
 * Webservices container service
 *
 */
@Service(name="org.glassfish.webservices.WebServicesContainer")
@Scoped(Singleton.class)
public class WebServicesContainer implements Container, PostConstruct, PreDestroy {
    @Inject
    private Habitat habitat;

    private final WebServicesDeploymentMBean deploymentBean = new WebServicesDeploymentMBean();
    private ManagedObjectManager mom;

    public String getName() {
        return "webservices";
    }

    public void postConstruct() {
        ObjectName MONITORING_SERVER = AMXGlassfish.DEFAULT.serverMon(AMXGlassfish.DEFAULT.dasName());
        mom = ManagedObjectManagerFactory.createFederated(MONITORING_SERVER);
        if (mom != null) {
            mom.setJMXRegistrationDebug(false);
            mom.stripPackagePrefix();
            mom.createRoot(deploymentBean, "webservices-deployment");
        }
    }

    /* package */ WebServicesDeploymentMBean getDeploymentBean() {
        return deploymentBean;
    }

    public Class<? extends Deployer> getDeployer() {
        return WebServicesDeployer.class;
    }

    public void preDestroy() {
        try {
            if (mom != null) {
                mom.close();
            }
        } catch(IOException ioe) {
            // nothing much can be done
        }
    }
}

