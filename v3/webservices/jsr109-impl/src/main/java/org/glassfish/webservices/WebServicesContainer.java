/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.webservices;

import com.sun.xml.ws.assembler.dev.HighAvailabilityProvider;
import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.webservices.deployment.WebServicesDeploymentMBean;
import org.glassfish.gmbal.ManagedObjectManager;
import org.glassfish.gmbal.ManagedObjectManagerFactory;
import org.glassfish.gms.bootstrap.GMSAdapterService;
import org.glassfish.external.amx.AMXGlassfish;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;

import javax.management.ObjectName;
import java.io.IOException;

/**
 * Web services container service
 *
 */
@Service(name="org.glassfish.webservices.WebServicesContainer")
@Scoped(Singleton.class)
public class WebServicesContainer implements Container, PostConstruct, PreDestroy {
//    @Inject
//    private Habitat habitat;

    @Inject
    GMSAdapterService gmsAdapterService;

    private final WebServicesDeploymentMBean deploymentBean = new WebServicesDeploymentMBean();
    private ManagedObjectManager mom;

    @Override
    public String getName() {
        return "webservices";
    }

    @Override
    public void postConstruct() {
        ObjectName MONITORING_SERVER = AMXGlassfish.DEFAULT.serverMon(AMXGlassfish.DEFAULT.dasName());
        mom = ManagedObjectManagerFactory.createFederated(MONITORING_SERVER);
        if (mom != null) {
            mom.setJMXRegistrationDebug(false);
            mom.stripPackagePrefix();
            mom.createRoot(deploymentBean, "webservices-deployment");
        }

        if(gmsAdapterService.isGmsEnabled()) {
            final String clusterName = gmsAdapterService.getGMSAdapter().getClusterName();
            final String instanceName = gmsAdapterService.getGMSAdapter().getModule().getInstanceName();
            
            HighAvailabilityProvider.INSTANCE.initHaEnvironment(clusterName, instanceName);
        }
    }

    /* package */ WebServicesDeploymentMBean getDeploymentBean() {
        return deploymentBean;
    }

    @Override
    public Class<? extends Deployer> getDeployer() {
        return WebServicesDeployer.class;
    }

    @Override
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

