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
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.InstrumentableClassLoader;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.persistence.common.Java2DBProcessorHelper;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import javax.naming.NamingException;
import javax.persistence.spi.ClassTransformer;
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
     * @InheritDoc
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

        if(prepared) {

            Application application = context.getModuleMetaData(Application.class);
            Set<BundleDescriptor> bundles = application.getBundleDescriptors();

            //TODO Need to modify this to be more generic.
            // Iterate through all the bundles for the app and collect pu references in referencedPus
            Map <String, PersistenceUnitDescriptor> referencedPusMap = new HashMap<String, PersistenceUnitDescriptor>();
            for (BundleDescriptor bundle : bundles) {
                Collection<? extends PersistenceUnitDescriptor> pusReferencedFromBundle = bundle.findReferencedPUs();
                for(PersistenceUnitDescriptor pu : pusReferencedFromBundle) {
                    // TODO : This is a hack to localize and minimize. changes Need to fix this properly post prelude to take care of ear case.
                    // For EJBs inside war, we get two bundles inside applications. If both of them are referring
                    // to same pu, we will get two pus being instantiaed. Put the pus in a map to just enlist one
                    // pu of given name for creating emf. For prelude is guaranted that pu of given name in an application
                    // has to refer to same pu
                    referencedPusMap.put(pu.getName(), pu);
                }

            }
            // EMFs get created here. JPAApplication maintains list of created EMFs so that they
            // can be closed at undeploy
            List<PersistenceUnitDescriptor> referencedPus = new ArrayList<PersistenceUnitDescriptor>();
            for (Map.Entry<String, PersistenceUnitDescriptor> entry : referencedPusMap.entrySet()) {
                referencedPus.add(entry.getValue());
            }
            JPAApplication jpaApp = new JPAApplication(referencedPus, new ProviderContainerContractInfoImpl(context, connectorRuntime));

            // Store jpaApp in DeploymentContext to retrieve it during load
            context.addModuleMetaData(jpaApp);
        }

        return prepared;
    }

    /**
     * @InheritDoc
     */
    @Override public JPAApplication load(JPAContainer container, DeploymentContext context) {
        // Return the JPAApplication stored in DeploymentContext during prepaare phase
        JPAApplication jpaApp = context.getModuleMetaData(JPAApplication.class);
        jpaApp.doJava2DB(context);
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

        public String getApplicationLocation() {
            //TODO : This needs to be cleaned up post TP2 to be more generic than dc.getSourceDir().
            return deploymentContext.getSourceDir().getAbsolutePath();
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
