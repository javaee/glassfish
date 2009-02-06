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

package org.glassfish.ejb.startup;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.DeploymentOperationParameters;
import org.glassfish.api.admin.ParameterNames;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

import java.util.*;
import java.util.logging.Level;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ejb module deployer.
 *
 */
@Service
public class EjbDeployer
    extends JavaEEDeployer<EjbContainerStarter, EjbApplication> {

    @Inject
    protected ServerContext sc;

    @Inject
    protected Domain domain;

    @Inject
    protected ServerEnvironmentImpl env;

    @Inject
    protected Habitat habitat;

    protected ThreadLocal<Application> tldApp = new ThreadLocal<Application>();

    protected ConcurrentHashMap<String, EjbApplication> ejbApps = new ConcurrentHashMap();

    /**
     * Constructor
     */
    public EjbDeployer() {

    }


    protected String getModuleType () {
        return "ejb";
    }

    @Override
    public MetaData getMetaData() {
        return new MetaData(false,
                new Class[] {EjbBundleDescriptor.class}, new Class[] {Application.class});
    }

    @Override
    public EjbApplication load(EjbContainerStarter containerStarter, DeploymentContext dc) {
        super.load(containerStarter, dc);
        Application app = dc.getModuleMetaData(Application.class);
        if (app == null) {
            app = tldApp.get();
        }

        Collection<EjbDescriptor> ebds =
                (Collection<EjbDescriptor>) app.getEjbDescriptors();

        EjbApplication ejbApp = new EjbApplication(ebds, dc, dc.getClassLoader(), habitat);

        DeploymentOperationParameters params = dc.getCommandParameters(DeploymentOperationParameters.class);
        String appName = params.name();

        ejbApps.put(appName, ejbApp);

        ejbApp.loadAndStartContainers(dc);
        return ejbApp;
    }

    public void unload(EjbApplication ejbApplication, DeploymentContext dc) {

        // unload from ejb container
    }

    /**
     * Clean any files and artifacts that were created during the execution
     * of the prepare method.
     *
     * @param dc deployment context
     */
    public void clean(DeploymentContext dc) {

        DeploymentOperationParameters params = dc.getCommandParameters(DeploymentOperationParameters.class);
        String appName = params.name();

        EjbApplication ejbApp = ejbApps.get(appName);
        if (ejbApp != null) {
            ejbApp.undeploy();
        } else {
            dc.getLogger().log(Level.WARNING,
                    "EjbApplication is null for name " + appName);
        }
    }
}

