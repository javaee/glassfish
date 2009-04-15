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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.persistence.jpa;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitsDescriptor;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.InstrumentableClassLoader;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.persistence.common.Java2DBProcessorHelper;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import javax.naming.NamingException;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;


/**
 * Deployer for JPA applications
 * @author Mitesh Meswani
 */
@Service
public class JPADeployer extends SimpleDeployer<JPAContainer, JPAApplication> {

    @Inject
    private ConnectorRuntime connectorRuntime;

    @Override public MetaData getMetaData() {

        return new MetaData(true /*invalidateCL */ ,
                null /* provides */,
                new Class[] {Application.class} /* requires Application from dol */);
    }

    protected void generateArtifacts(DeploymentContext dc) throws DeploymentException {
        // Noting to generate yet!!
    }

    protected void cleanArtifacts(DeploymentContext dc) throws DeploymentException {
        // Drop tables if needed on undeploy.
        OpsParams params = dc.getCommandParameters(OpsParams.class);
        if (params.origin == OpsParams.Origin.undeploy) {
            Java2DBProcessorHelper helper = new Java2DBProcessorHelper(dc);
            helper.init();
            helper.createOrDropTablesInDB(false, "JPA"); // NOI18N
        }
    }

    /**
     * @inheritDoc
     */
    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    /**
     * EMFs for refered pus are created and stored in JPAApplication instance.
     * The JPAApplication instance is stored in given DeploymentContext to be retrieved by load
     */
    @Override public boolean prepare(DeploymentContext context) {
        boolean prepared = super.prepare(context);

        Application application = context.getModuleMetaData(Application.class);
        Set<EntityManagerFactory> emfsInitializedByThisApp = application.getEntityManagerFactories();
        // TODO hack need to fix this properly
        // JPADeployer will be called 'n' times (once for each module for which JPASniffer/JPACompositeSniffer returns true)
        // during an application deployment.
        // We load all pus when the the first call comes in and put them in application object
        // If the above set is not empty => we have loaded all the emfs, do not attempt to load pus again.
        if(emfsInitializedByThisApp.isEmpty()) {
            if(prepared) {

                Set<BundleDescriptor> bundles = application.getBundleDescriptors();

                // Iterate through all the bundles for the app and collect pu references in referencedPus
                Map <String, PersistenceUnitDescriptor> referencedPusMap = new HashMap<String, PersistenceUnitDescriptor>();
                for (BundleDescriptor bundle : bundles) {
                    Collection<? extends PersistenceUnitDescriptor> pusReferencedFromBundle = bundle.findReferencedPUs();
                    for(PersistenceUnitDescriptor pu : pusReferencedFromBundle) {
                        // we can have 'n' bundles referring to same pu. But we want to instantiate the pu only once
                        // Put the pus in a map from absolutepuroot +puName  to pu to filter out duplicates.
                        // absolutePuRoot is full path to puroot within an app
                        // TODO implement equals in PersistenceUnitDescriptor that takes into account absolutepuroot +puName so that we can directly add puds to a set
                        PersistenceUnitsDescriptor persistenceUnitsDescriptor = pu.getParent();
                        referencedPusMap.put(persistenceUnitsDescriptor.getAbsolutePuRoot() + pu.getName(), pu);
                    }

                }

                // EMFs get created here. JPAApplication maintains list of created EMFs so that they
                // can be closed at undeploy
                List<PersistenceUnitDescriptor> referencedPus = new ArrayList<PersistenceUnitDescriptor>();
                for (Map.Entry<String, PersistenceUnitDescriptor> entry : referencedPusMap.entrySet()) {
                    referencedPus.add(entry.getValue());
                }

//     TODO  need to initialize pus for only this bundle here to enable transformers to work properly. This is because classloader from deploymentcontext is for this bundle only 
//            Put the emfs in DeploymentContext for this module as context. context.addModuleMetaData(List<emf>) defined in this bundle (Need to optimize it further to only initialize those emfs that are actually referred
//             Retrieve them in load() and put them in corresponding JPAContainer instance and close them in corresponding stop()
//                List<PersistenceUnitDescriptor> referencedPus = new ArrayList<PersistenceUnitDescriptor>();
//                Map <String, PersistenceUnitDescriptor> referencedPusMap = new HashMap<String, PersistenceUnitDescriptor>();
//                for (BundleDescriptor bundle : bundles) {
//                    Collection<PersistenceUnitsDescriptor> pusDescriptorForThisBundle = bundle.getExtensionsDescriptors(PersistenceUnitsDescriptor.class);
//                    for (PersistenceUnitsDescriptor persistenceUnitsDescriptor : pusDescriptorForThisBundle) {
//                        for (PersistenceUnitDescriptor pud : persistenceUnitsDescriptor.getPersistenceUnitDescriptors()) {
//
//
//                        }
//
//                    }

                JPAApplication jpaApp = new JPAApplication(referencedPus, new ProviderContainerContractInfoImpl(context, connectorRuntime));

                // Store jpaApp in DeploymentContext to retrieve it during load
                context.addModuleMetaData(jpaApp);
            }
        }

        return prepared;
    }

    /**
     * @inheritDoc
     */
    @Override public JPAApplication load(JPAContainer container, DeploymentContext context) {
        // Return the JPAApplication stored in DeploymentContext during prepaare phase
        JPAApplication jpaApp = context.getModuleMetaData(JPAApplication.class);
        if(jpaApp != null) {
            jpaApp.doJava2DB(context);
        } else {
            jpaApp = new JPAApplication(); //TODO Needs to be removed once prepare clean up is done
        }
        return jpaApp; // XXX context.getModuleMetaData(JPAApplication.class);
    }

    private static class ProviderContainerContractInfoImpl
            implements ProviderContainerContractInfo {
        private final DeploymentContext deploymentContext;
        private final ConnectorRuntime connectorRuntime;
        private final ClassLoader finalClassLoader;
        
        public ProviderContainerContractInfoImpl(DeploymentContext deploymentContext, ConnectorRuntime connectorRuntime) {
            this.deploymentContext = deploymentContext;
            this.connectorRuntime = connectorRuntime;
            this.finalClassLoader=deploymentContext.getFinalClassLoader();
        }

        public ClassLoader getClassLoader() {
            return finalClassLoader;
        }

        public ClassLoader getTempClassloader() {
            return ( (InstrumentableClassLoader)deploymentContext.getClassLoader() ).copy();
        }

        public void addTransformer(final ClassTransformer transformer) {
            // Bridge between java.lang.instrument.ClassFileTransformer that DeploymentContext accepts
            // and javax.persistence.spi.ClassTransformer that JPA supplies.
            deploymentContext.addTransformer(new ClassFileTransformer() {
                public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                        ProtectionDomain protectionDomain, byte[] classfileBuffer)
                        throws IllegalClassFormatException {
                    return transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
                }
            });
        }

        /**
         * Returns Application location for current application
         * @return
         */
        public String getApplicationLocation() {
            // Get source for current bundle. If it has not parent, it is the top level application
            // else continute traversing up till we find one with not parent.
            ReadableArchive archive = deploymentContext.getSource();
            boolean appRootFound = false;
            while (!appRootFound) {
                ReadableArchive parentArchive = archive.getParentArchive();
                if(parentArchive != null) {
                    archive = parentArchive;
                } else {
                    appRootFound = true;
                }
            }
            return archive.getURI().getPath();
        }

        public DataSource lookupDataSource(String dataSourceName) throws NamingException {
            return DataSource.class.cast(connectorRuntime.lookupPMResource(dataSourceName, false) );
        }

        public DataSource lookupNonTxDataSource(String dataSourceName) throws NamingException {
            return DataSource.class.cast(connectorRuntime.lookupNonTxResource(dataSourceName, false));
        }

        public DeploymentContext getDeploymentContext() {
            return deploymentContext;
        }

    } // class ProviderContainerContractInfoImpl

}
