/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.persistence.jpa;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.enterprise.deployment.*;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.persistence.common.Java2DBProcessorHelper;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;

import javax.persistence.EntityManagerFactory;
import java.util.*;


/**
 * Deployer for JPA applications
 * @author Mitesh Meswani
 */
@Service
public class JPADeployer extends SimpleDeployer<JPAContainer, JPApplicationContainer> implements PostConstruct, EventListener {

    @Inject
    private ConnectorRuntime connectorRuntime;

    @Inject
    private Habitat habitat;

    @Inject
    private ServerEnvironmentImpl serverEnvironment;

    @Inject
    private Events events;

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
        if (params.origin.isUndeploy() && isDas()) {
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
                final List<PersistenceUnitDescriptor> referencedPus = new ArrayList<PersistenceUnitDescriptor>();
                for (BundleDescriptor bundle : bundles) {
                    Collection<? extends PersistenceUnitDescriptor> pusReferencedFromBundle = bundle.findReferencedPUs();
                    for(PersistenceUnitDescriptor pud : pusReferencedFromBundle) {
                        referencedPus.add(pud);
                    }
                }

                //Iterate through all the PUDs for this bundle and if it is referenced, load the corresponding pu
                PersistenceUnitDescriptorIterator pudIterator = new PersistenceUnitDescriptorIterator() {
                    @Override void visitPUD(PersistenceUnitDescriptor pud, DeploymentContext context) {
                        if(referencedPus.contains(pud)) {
                            boolean isDas = isDas();
                            ProviderContainerContractInfo providerContainerContractInfo = serverEnvironment.isEmbedded() ?
                                    new EmbeddedProviderContainerContractInfo(context, connectorRuntime, habitat, isDas) :
                                    new ServerProviderContainerContractInfo(context, connectorRuntime, isDas);
                            PersistenceUnitLoader puLoader = new PersistenceUnitLoader(pud, providerContainerContractInfo);
                            // Store the puLoader in context. It is retrieved to execute java2db and to
                            // store the loaded emfs in a JPAApplicationContainer object for cleanup
                            context.addTransientAppMetaData(getUniquePuIdentifier(pud), puLoader );
                        }
                    }
                };
                pudIterator.iteratePUDs(context);
            }

//        StatsProviderManager.register("jpa", PluginPoint.SERVER, "jpa/eclipselink", new EclipseLinkStatsProvider());

        return prepared;
    }

    /**
     * @inheritDoc
     */
    @Override
    public JPApplicationContainer load(JPAContainer container, DeploymentContext context) {
        final List<EntityManagerFactory> emfsInitializedForThisBundle = new ArrayList<EntityManagerFactory>();

        //iterate through all the PersistenceUnitDescriptor for this bundle. If it is initialized, add it to emfsInitializedForThisBundle
        PersistenceUnitDescriptorIterator pudIterator = new PersistenceUnitDescriptorIterator() {
            @Override void visitPUD(PersistenceUnitDescriptor pud, DeploymentContext context) {
                //PersistenceUnitsDescriptor corresponds to  persistence.xml. A bundle can only have one persistence.xml except
                // when the bundle is an application which can have multiple persistence.xml under jars in root of ear and lib.
                PersistenceUnitLoader puLoader = context.getTransientAppMetaData(getUniquePuIdentifier(pud), PersistenceUnitLoader.class);
                if (puLoader != null) {
                    emfsInitializedForThisBundle.add(puLoader.getEMF());
                }
            }
        };
        pudIterator.iteratePUDs(context);

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

    private boolean isDas() {
        return serverEnvironment.isDas() || serverEnvironment.isEmbedded();
    }

    @Override
    public void postConstruct() {
        events.register(this);
    }

    @Override
    public void event(Event event) {
        if (event.is(Deployment.APPLICATION_PREPARED) && isDas() ) { //We do java2db only on das
            ExtendedDeploymentContext context = (ExtendedDeploymentContext)event.hook();
            Map<String, ExtendedDeploymentContext> deploymentContexts = context.getModuleDeploymentContexts();

            for (DeploymentContext deploymentContext : deploymentContexts.values()) {
                //execute java2db for each bundle level pus
                executeJava2DB(deploymentContext);
            }
            //execute java2db for the app level pus
            executeJava2DB(context);
        }
    }

    private void executeJava2DB(final DeploymentContext context) {

        //iterate through all the PersistenceUnitDescriptor for this bundle. If it is initialized, execute jdava2db on it
        PersistenceUnitDescriptorIterator pudIterator = new PersistenceUnitDescriptorIterator() {
            @Override void visitPUD(PersistenceUnitDescriptor pud, DeploymentContext context) {
                //PersistenceUnitsDescriptor corresponds to  persistence.xml. A bundle can only have one persitence.xml except
                // when the bundle is an application which can have multiple persitence.xml under jars in root of ear and lib.
                PersistenceUnitLoader puLoader = context.getTransientAppMetaData(getUniquePuIdentifier(pud), PersistenceUnitLoader.class);
                if (puLoader != null) {
                    puLoader.doJava2DB();
                }
            }
        };

        pudIterator.iteratePUDs(context);

    }

    /**
     * Helper class to centralize the code for loop that iterates through all the PersistenceUnitDescriptor for a given DeploymentContext (and hence the corresponding bundle)
     */
    private static abstract class PersistenceUnitDescriptorIterator {
        /**
         * Iterate through all the PersistenceUnitDescriptors for the given context (and hence corresponding bundle) and call visitPUD for each of them
         * @param context
         */
        void iteratePUDs(DeploymentContext context) {
            RootDeploymentDescriptor currentBundle = context.getModuleMetaData(BundleDescriptor.class);
            if (currentBundle == null) {
                    // We are being called for an application
                    currentBundle = context.getModuleMetaData(Application.class);
                }

            Collection<PersistenceUnitsDescriptor> pusDescriptorForThisBundle = currentBundle.getExtensionsDescriptors(PersistenceUnitsDescriptor.class);
            for (PersistenceUnitsDescriptor persistenceUnitsDescriptor : pusDescriptorForThisBundle) {
                    for (PersistenceUnitDescriptor pud : persistenceUnitsDescriptor.getPersistenceUnitDescriptors()) {
                        visitPUD(pud, context);
                    }
            }

        }

        /**
         * Called for each PersistenceUnitDescriptor visited by this iterator.
         */
        abstract void visitPUD(PersistenceUnitDescriptor pud, DeploymentContext context);

    }
}
