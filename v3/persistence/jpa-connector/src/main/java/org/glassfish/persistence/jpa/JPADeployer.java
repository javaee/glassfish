/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.persistence.jpa;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.enterprise.deployment.*;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.persistence.common.Java2DBProcessorHelper;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

import javax.persistence.EntityManagerFactory;
import java.util.*;


/**
 * Deployer for JPA applications
 * @author Mitesh Meswani
 */
@Service
public class JPADeployer extends SimpleDeployer<JPAContainer, JPApplicationContainer> {

    @Inject
    private ConnectorRuntime connectorRuntime;

    @Inject
    private Habitat habitat;

    @Inject
    private ServerEnvironment serverEnvironment;

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
        if (params.origin.isUndeploy() && serverEnvironment.isDas()) {
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

            if(prepared) {
               Application application = context.getModuleMetaData(Application.class);
               Set<BundleDescriptor> bundles = application.getBundleDescriptors();

                // Iterate through all the bundles for the app and collect pu references in referencedPus
                List<PersistenceUnitDescriptor> referencedPus = new ArrayList<PersistenceUnitDescriptor>();
                for (BundleDescriptor bundle : bundles) {
                    Collection<? extends PersistenceUnitDescriptor> pusReferencedFromBundle = bundle.findReferencedPUs();
                    for(PersistenceUnitDescriptor pud : pusReferencedFromBundle) {
                        referencedPus.add(pud);
                    }
                }

                RootDeploymentDescriptor currentBundle = context.getModuleMetaData(BundleDescriptor.class);
                if (currentBundle == null) {
                    // We are being called for an application
                    currentBundle = application;
                }

                // Initialize pus for only this bundle here to enable transformers to work properly.
                // This is because classloader from deploymentContext is for this bundle only
                Collection<PersistenceUnitsDescriptor> pusDescriptorForThisBundle = currentBundle.getExtensionsDescriptors(PersistenceUnitsDescriptor.class);
                for (PersistenceUnitsDescriptor persistenceUnitsDescriptor : pusDescriptorForThisBundle) {
                    for (PersistenceUnitDescriptor pud : persistenceUnitsDescriptor.getPersistenceUnitDescriptors()) {
                        if(referencedPus.contains(pud)) {
                            ProviderContainerContractInfo providerContainerContractInfo = serverEnvironment.getRuntimeType().isEmbedded() ?
                                    new EmbeddedProviderContainerContractInfo(context, connectorRuntime, habitat, serverEnvironment.isDas()) :
                                    new ServerProviderContainerContractInfo(context, connectorRuntime, serverEnvironment.isDas());
                            PersistenceUnitLoader puLoader = new PersistenceUnitLoader(pud, providerContainerContractInfo);
                            // Store the puLoader in context. It is retrieved in load() to execute java2db and to
                            // store the loaded emfs in a JPAApplicationContainer object for cleanup
                            context.addTransientAppMetaData(getUniquePuIdentifier(pud), puLoader );
                        }
                    }
                }
            }

//        StatsProviderManager.register("jpa", PluginPoint.SERVER, "jpa/eclipselink", new EclipseLinkStatsProvider());

        return prepared;
    }

    /**
     * @inheritDoc
     */
    @Override
    public JPApplicationContainer load(JPAContainer container, DeploymentContext context) {
        RootDeploymentDescriptor currentBundle = context.getModuleMetaData(BundleDescriptor.class);
        if (currentBundle == null) {
            // We are being called for an application
            currentBundle = context.getModuleMetaData(Application.class);
        }

        List<EntityManagerFactory> emfsInitializedForThisBundle = new ArrayList<EntityManagerFactory>();
        Collection<PersistenceUnitsDescriptor> pusDescriptorForThisBundle = currentBundle.getExtensionsDescriptors(PersistenceUnitsDescriptor.class);
        for (PersistenceUnitsDescriptor persistenceUnitsDescriptor : pusDescriptorForThisBundle) {
            for (PersistenceUnitDescriptor pud : persistenceUnitsDescriptor.getPersistenceUnitDescriptors()) {
                //PersistenceUnitsDescriptor corresponds to  persistence.xml. A bundle can only have one persitence.xml except
                // when the bundle is an application which can have multiple persitence.xml under jars in root of ear and lib.
                PersistenceUnitLoader puLoader = context.getTransientAppMetaData(getUniquePuIdentifier(pud), PersistenceUnitLoader.class);
                if (puLoader != null) {
                    emfsInitializedForThisBundle.add(puLoader.getEMF());
                    puLoader.doJava2DB();
                }
            }
        }
        return new JPApplicationContainer(emfsInitializedForThisBundle);
    }

    /**
     * Returns unique identifier for this pu within application
     * @param pud The given pu
     * @return Absolute pu root + pu name
     */
    private static String getUniquePuIdentifier(PersistenceUnitDescriptor pud) {
        return pud.getAbsolutePuRoot() + pud.getName();
     }

}
