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
import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
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
import java.util.logging.Level;
import java.util.logging.Logger;


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

    @Inject
    private ApplicationRegistry applicationRegistry;

    private static Logger logger = LogDomains.getLogger(PersistenceUnitLoader.class, LogDomains.PERSISTENCE_LOGGER);

    /** Key used to get/put emflists in transientAppMetadata */
    private static final String EMF_KEY = EntityManagerFactory.class.toString();

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

            boolean hasScopedResource = false;
            if(prepared) {
               Application application = context.getModuleMetaData(Application.class);
               Set<BundleDescriptor> bundles = application.getBundleDescriptors();

                // Iterate through all the bundles for the app and collect pu references in referencedPus
                final List<PersistenceUnitDescriptor> referencedPus = new ArrayList<PersistenceUnitDescriptor>();
                for (BundleDescriptor bundle : bundles) {
                    Collection<? extends PersistenceUnitDescriptor> pusReferencedFromBundle = bundle.findReferencedPUs();
                    for(PersistenceUnitDescriptor pud : pusReferencedFromBundle) {
                        referencedPus.add(pud);
                        String jtaDataSource = pud.getJtaDataSource();
                        if(jtaDataSource != null && jtaDataSource.startsWith("java:")){
                            hasScopedResource = true;
                        }
                    }
                }
                if (hasScopedResource) {
                    connectorRuntime.registerDataSourceDefinitions(application);
                }

                //Iterate through all the PUDs for this bundle and if it is referenced, load the corresponding pu
                PersistenceUnitDescriptorIterator pudIterator = new PersistenceUnitDescriptorIterator() {
                    @Override void visitPUD(PersistenceUnitDescriptor pud, DeploymentContext context) {
                        if(referencedPus.contains(pud)) {
                            boolean isDas = isDas();
                            ProviderContainerContractInfo providerContainerContractInfo = serverEnvironment.isEmbedded() ?
                                    new EmbeddedProviderContainerContractInfo(context, connectorRuntime, isDas) :
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
    //@Override
    public JPApplicationContainer load(JPAContainer container, DeploymentContext context) {
        //TODO With changes to close emfs in APPLICATION_DISABLED, JPApplicationContainer does not have any functionality left. Remove it after talking with Hong.
        return new JPApplicationContainer();
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
        if(logger.isLoggable(Level.FINEST)) {
            logger.finest("Received event:" + event.name());
        }
        if (event.is(Deployment.APPLICATION_PREPARED) ) {
            ExtendedDeploymentContext context = (ExtendedDeploymentContext)event.hook();
            OpsParams params = context.getCommandParameters(OpsParams.class);
            if(logger.isLoggable(Level.FINE)) {
                logger.fine("Handling event: APPLICATION_PREPARED origin is:" + params.origin);
            }
            if(!params.origin.isCreateAppRef()) {
                // When create-application-ref is called for an already deployed app, APPLICATION_PREPARED will be sent on DAS
                // Obviously there is no new emf created for this even and we need not do java2db also. Ignore the event
                Map<String, ExtendedDeploymentContext> deploymentContexts = context.getModuleDeploymentContexts();

                for (DeploymentContext deploymentContext : deploymentContexts.values()) {
                    //bundle level pus
                    iterateInitializedPUsAtApplicationPrepare(deploymentContext);
                }
                //app level pus
                iterateInitializedPUsAtApplicationPrepare(context);
            }
        } else if(event.is(Deployment.APPLICATION_DISABLED)) {
            logger.fine("Handling event: APPLICATION_DISABLED");
            // APPLICATION_DISABLED will be generated when an app is disabled/undeployed/appserver goes down.
            //close all the emfs created for this app
            ApplicationInfo appInfo = (ApplicationInfo) event.hook();
            //Suppress warning required as there is no way to pass equivalent of List<EMF>.class to the method 
            @SuppressWarnings("unchecked")  List<EntityManagerFactory> emfsCreatedForThisApp = appInfo.getTransientAppMetaData(EMF_KEY, List.class);
            if(emfsCreatedForThisApp != null) { // Events are always dispatched to all registered listeners. emfsCreatedForThisApp will be null for an app that does not have PUs.
                for (EntityManagerFactory entityManagerFactory : emfsCreatedForThisApp) {
                    entityManagerFactory.close();
                }
                // We no longer have the emfs in open state clear the list.
                // On app enable(after a disable), for a cluster, the deployment framework calls prepare() for instances but not for DAS.
                // So on DAS, at a disable, the emfs will be closed and we will not attempt to close emfs when appserver goes down even if the app is re-enabled.
                emfsCreatedForThisApp.clear();
            }
        }
    }

    /**
     * Does java2db on DAS and saves emfs created during prepare to ApplicationInfo maintained by DOL.
     * ApplicationInfo is not available during prepare() so we can not directly use it there.
     * @param context
     */
    private void iterateInitializedPUsAtApplicationPrepare(final DeploymentContext context) {

        DeployCommandParameters commandParams = context.getCommandParameters(DeployCommandParameters.class);
        String appName = commandParams.name;
        final ApplicationInfo appInfo = applicationRegistry.get(appName);

        //iterate through all the PersistenceUnitDescriptor for this bundle.
        PersistenceUnitDescriptorIterator pudIterator = new PersistenceUnitDescriptorIterator() {
            @Override void visitPUD(PersistenceUnitDescriptor pud, DeploymentContext context) {
                //PersistenceUnitsDescriptor corresponds to  persistence.xml. A bundle can only have one persitence.xml except
                // when the bundle is an application which can have multiple persitence.xml under jars in root of ear and lib.
                PersistenceUnitLoader puLoader = context.getTransientAppMetaData(getUniquePuIdentifier(pud), PersistenceUnitLoader.class);
                if (puLoader != null) { // We have initialized PU
                    if(isDas()) { //We execute Java2DB only on DAS
                        OpsParams params = context.getCommandParameters(OpsParams.class);
                        if(params.origin.isDeploy()) { //APPLICATION_PREPARE will be called for create-application-ref also. We should perform java2db only on first deploy
                            puLoader.doJava2DB();
                        }
                    }

                    // Save emf in ApplicationInfo so that it can be retrieved and closed for cleanup
                    // The PUs
                    @SuppressWarnings("unchecked") //Suppress warning required as there is no way to pass equivalent of List<EMF>.class to the method
                    List<EntityManagerFactory> emfsCreatedForThisApp = appInfo.getTransientAppMetaData(EMF_KEY, List.class );
                    if(emfsCreatedForThisApp == null) {
                        //First EMF for this app, initialize
                        emfsCreatedForThisApp = new ArrayList<EntityManagerFactory>();
                        appInfo.addTransientAppMetaData(EMF_KEY, emfsCreatedForThisApp);
                    }
                    emfsCreatedForThisApp.add(puLoader.getEMF());
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
