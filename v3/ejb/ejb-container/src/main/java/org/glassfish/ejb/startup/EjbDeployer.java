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
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.v3.server.ServerEnvironment;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.Module;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import java.util.*;

/**
 * Ejb module deployer.
 *
 */
@Service
public class EjbDeployer
    extends JavaEEDeployer<EjbContainerStarter, EjbApplication> {

    @Inject
    ServerContext sc;

    @Inject
    Domain domain;

    @Inject
    ServerEnvironment env;                                             

    ThreadLocal<Application> tldApp = new ThreadLocal<Application>();

    /**
     * Constructor
     */
    public EjbDeployer() {
        System.out.println("**********************************");
        System.out.println("*********** EjbDeployer **********");
        System.out.println("**********************************");
    }
    

    protected String getModuleType () {
        return "ejb";
    }

    protected RootDeploymentDescriptor getDefaultBundleDescriptor() {
        System.out.println("**EjbDeployer: getDefaultBundleDesc..");
        return null;
    }

    @Override
    public MetaData getMetaData() {
        List<ModuleDefinition> apis = new ArrayList<ModuleDefinition>();
        Module module = modulesRegistry.makeModuleFor("javax.javaee:javaee", "5.0");
        if (module!=null) {
            apis.add(module.getModuleDefinition());
        }

        String[] otherExportedPackages = new String[] {
                "org.glassfish.ejb:ejb-container"};

        for (String otherExportedPackage : otherExportedPackages) {
            module = modulesRegistry.makeModuleFor(otherExportedPackage, null);
            if (module != null) {
                apis.add(module.getModuleDefinition());
            }
        }

        return new MetaData(false, apis.toArray(new ModuleDefinition[apis.size()]),
                new Class[] {Application.class}, null);
    }

    
    @Override
    protected Application parseModuleMetaData(DeploymentContext dc) throws Exception {
        Application app = super.parseModuleMetaData(dc);
        if (app.isVirtual()) {
            System.out.println("JAR???");
        }
        tldApp.set(app);
        return app;
    }


    @Override
    public boolean prepare(DeploymentContext dc) {
        boolean prepared = super.prepare(dc);
        if (prepared) {
            Application application = dc.getModuleMetaData(Application.class);
            System.out.println("App: " + application);
        }
        return true;
    }

    public EjbApplication load(EjbContainerStarter containerStarter, DeploymentContext dc) {

        Application app = dc.getModuleMetaData(Application.class);
        if (app == null) {
            app = tldApp.get();
        }

        Collection<EjbDescriptor> ebds =
                (Collection<EjbDescriptor>) app.getEjbDescriptors();

        Collection<EjbApplication> ejbApps =
                new HashSet<EjbApplication>();

        EjbApplication ejbApp = new EjbApplication(ebds, dc, dc.getClassLoader());

        System.out.println("**EjbDeployer: " + ejbApp
            + ";  CL => " + dc.getClassLoader()
            + "; TCCL => " + Thread.currentThread().getContextClassLoader());
        return ejbApp;
    }

    public void unload(EjbApplication ejbApplication, DeploymentContext dc) {
        Properties params = dc.getCommandParameters();

        // unload from ejb container
    }
}

