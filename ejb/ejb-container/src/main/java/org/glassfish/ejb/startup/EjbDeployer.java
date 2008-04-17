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
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.EjbInWarArchivist;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.v3.server.ServerEnvironment;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.Module;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.glassfish.deployment.common.DeploymentProperties;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

import java.util.*;
import java.util.logging.Level;

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
    protected ServerEnvironment env;

    @Inject
    protected Habitat habitat;

    protected ThreadLocal<Application> tldApp = new ThreadLocal<Application>();

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
        Module module = modulesRegistry.makeModuleFor("org.glassfish:javax.javaee",null);
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
                new Class[] {EjbBundleDescriptor.class}, new Class[] {Application.class});
    }

    /**
     * Loads the meta date associated with the application.
     *
     * @param type type of metadata that this deployer has declared providing.
     * @param dc deployment context
     */
    public <V> V loadMetaData(Class<V> type, DeploymentContext dc) {
        try {
            Application app = this.parseModuleMetaData(dc);
            Set<EjbBundleDescriptor> ejbBD = app.getEjbBundleDescriptors();

            EjbBundleDescriptor dummy = null;

            Application dcApp = dc.getModuleMetaData(Application.class);
            if (dcApp != null) {
                dcApp.setVirtual(false);
                for (EjbBundleDescriptor desc : ejbBD) {
                    dummy = desc;
                    dcApp.addBundleDescriptor(desc);
                }
            } else {
                dc.addModuleMetaData(app);
                dcApp = app;
            }
            
            //FIXME: Need to revist for .ear
            dcApp.setPackagedAsSingleModule(true);
            return (V) dummy;
        } catch (Exception e) {
            dc.getLogger().log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    protected Application parseModuleMetaData(DeploymentContext dc)
        throws Exception {

        ReadableArchive sourceArchive = dc.getSource();
        boolean isWar = sourceArchive.exists("WEB-INF");
        if (isWar) {
            ClassLoader cl = dc.getClassLoader();
            Properties props = dc.getCommandParameters();
            String name = props.getProperty(DeploymentProperties.NAME);

            Archivist archivist = habitat.getComponent(EjbInWarArchivist.class);
            archivist.setClassLoader(cl);
            archivist.setAnnotationProcessingRequested(true);
            archivist.setXMLValidation(false);
            archivist.setRuntimeXMLValidation(false);

            archivist.setDefaultBundleDescriptor(
                    new EjbBundleDescriptor());

            Application application = applicationFactory.openArchive(
                    name, archivist, sourceArchive, true);

            // this may not be the best location for this but it will suffice.
            if (deploymentVisitor!=null) {
                deploymentVisitor.accept(application);
            }


            return application;
        } else {
            return super.parseModuleMetaData(dc);
        }
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

