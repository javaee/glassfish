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

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.logging.LogDomains;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceProvider;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a JPA Application
 * @author Mitesh Meswani
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class JPAApplication implements ApplicationContainer {

    /**
     * All the PUS referenced by the components of this application.
     * A component references by a PU using one of the four methods:
     * \@PersistenceContext, @PersistenceUnit,
     * <persistence-context-ref> and <persistence-unit-ref>.

     */
    Collection<PersistenceUnitDescriptor> referencedPus = new ArrayList<PersistenceUnitDescriptor>();

    /**
     * Conduit to talk with container
     */
    ProviderContainerContractInfo providerContainerContractInfo;

    /**
     * All the EMFs loaded for this application
     */
    Collection<EntityManagerFactory> loadedEMFs = new ArrayList<EntityManagerFactory>();

    /**
     * logger to log loader messages
     */
    private static Logger logger = LogDomains.getLogger(JPAApplication.class, LogDomains.LOADER_LOGGER);

    /**
     * Default Integration properties for various providers
     */
    private static Map<String, String> integrationProperties;

    
   JPAApplication(Collection<PersistenceUnitDescriptor> allReferencedPus, ProviderContainerContractInfo providerContainerContractInfo) {
       this.referencedPus = allReferencedPus;
       this.providerContainerContractInfo = providerContainerContractInfo;

       // A hack to work around EclipseLink issue https://bugs.eclipse.org/bugs/show_bug.cgi?id=248328 for prelude
       // This should be removed once version of EclipseLink which fixes the issue is integrated.
       // set the system property required by EclipseLink before we load it.
       setSystemPropertyToEnableDoPrivilegedInEclipseLink();

       //PUs need to be loaded here so that transformers can be registered into Deploymentcontext in correct phase
       loadAllPus();

   }

    private void setSystemPropertyToEnableDoPrivilegedInEclipseLink() {
        final String PROPERTY_NAME = "eclipselink.security.usedoprivileged";
        // Need not invoke in doPrivileged block as the whole call stack consist of trusted code when this code
        // is invoked
        if(System.getProperty(PROPERTY_NAME) == null) {
            // property not set. Set it to true
            System.setProperty(PROPERTY_NAME, String.valueOf(Boolean.TRUE) );
        }
    }


    //-------------- Begin Methods implementing ApplicationContainer interface -------------- //
    public Object getDescriptor() {
        return null;
    }

    public boolean start(ApplicationContext startupContxt) {
        return true;
    }

    public boolean stop(ApplicationContext stopContext) {
        closeAllEMFs();
        return true;
    }

    /**
     * Suspends this application container.
     *
     * @return true if suspending was successful, false otherwise.
     */
    public boolean suspend() {
        // Not (yet) supported
        return false;
    }

    /**
     * Resumes this application container.
     *
     * @return true if resumption was successful, false otherwise.
     */
    public boolean resume() {
        // Not (yet) supported
        return false;
    }

    public ClassLoader getClassLoader() {
        //TODO: Check with Jerome. Should this return anything but null? currently it does not seem so.
        return null;
    }

    //-------------- End Methods implementing ApplicationContainer interface -------------- //

    private void loadAllPus() {
        String applicationLocation = providerContainerContractInfo.getApplicationLocation();
        final boolean fineMsgLoggable = logger.isLoggable(Level.FINE);
        if(fineMsgLoggable) {
            logger.fine("Loading persistence units for application: " +
                    applicationLocation);
        }
        for(PersistenceUnitDescriptor pu : referencedPus) {
            EntityManagerFactory emf = loadPU(pu);
            loadedEMFs.add(emf);
        }
        if(fineMsgLoggable) {
            logger.fine("Finished loading persistence units for application: " +
                    applicationLocation);
        }
    }

    /**
     * Loads an individual PersistenceUnitDescriptor and registers the
     * EntityManagerFactory in appropriate DOL structure.
     *
     * @param pud PersistenceUnitDescriptor to be loaded.
     */
    private EntityManagerFactory loadPU(PersistenceUnitDescriptor pud) {
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("loading pud " + pud.getPuRoot()); // NOI18N
        }
        PersistenceUnitInfo pInfo = new PersistenceUnitInfoImpl(pud, providerContainerContractInfo);
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("PersistenceInfo for this pud is :\n" + pInfo); // NOI18N
        }
        PersistenceProvider provider;
        try {
            // See we use application CL as opposed to system CL to loadPU
            // provider. This allows user to get hold of provider specific
            // implementation classes in their code. But this also means
            // provider must not use appserver implementation classes directly
            // because once we implement isolation in our class loader hierarchy
            // the only classes available to application class loader would be
            // our appserver interface classes. By Sahoo
            provider =
                    PersistenceProvider.class.cast(
                    ClassLoader.class.cast(providerContainerContractInfo.getClassLoader())
                    .loadClass(pInfo.getPersistenceProviderClassName())
                    .newInstance());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        EntityManagerFactory emf = provider.createContainerEntityManagerFactory(
                pInfo, integrationProperties);
        logger.logp(Level.FINE, "JPAApplication", "loadPU", // NOI18N
                    "emf = {0}", emf); // NOI18N

        RootDeploymentDescriptor rootDD = pud.getParent().getParent();
        Application application;
        if (rootDD.isApplication()) {
            application = Application.class.cast(rootDD);
        } else {
            application = BundleDescriptor.class.cast(rootDD).getApplication();
        }
        application.addEntityManagerFactory(pInfo.getPersistenceUnitName(), pud.getPuRoot(), emf);


        return emf;
    }


    private void closeAllEMFs() {
        String applicationLocation = providerContainerContractInfo.getApplicationLocation();
        final boolean fineMsgLoggable = logger.isLoggable(Level.FINE);
        if(fineMsgLoggable) {
            logger.fine("Unloading persistence units for application: " + // NOI18N
                    applicationLocation );
            logger.logp(Level.FINE, "JPAApplication", "closeEMFs", // NOI18N
                    "loadedEMFs.size() = {0}", // NOI18N
                    loadedEMFs.size());
        }
        for (EntityManagerFactory emf : loadedEMFs) {
            try {
                logger.logp(Level.FINE, "JPAApplication",
                        "closeEMFs", "emf = {0}", emf);
                emf.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        if(fineMsgLoggable) {
            logger.fine("Finished unloading persistence units for application: " + // NOI18N
                    applicationLocation);
        }

    }

    static {
        /*
         * We set all the provider specific integration level properties here.
         * It knows about all the integration level properties that
         * are needed to integrate a provider with our container. When we add
         * support for other containers, we should modify this code so that user
         * does not have to specify such properties in their persistence.xml file.
         * These properties can be overriden by persistence.xml as per
         * the spec. Before applying default values for properties, this method
         * first checks if the properties have been set in the system
         * (typically done using -D option in domain.xml).
         *
         */
        Map<String, String> props = new HashMap<String, String>();

        final String ECLIPSELINK_SERVER_PLATFORM_CLASS_NAME_PROPERTY =
                "eclipselink.target-server"; // NOI18N
        props.put(ECLIPSELINK_SERVER_PLATFORM_CLASS_NAME_PROPERTY,
                System.getProperty(ECLIPSELINK_SERVER_PLATFORM_CLASS_NAME_PROPERTY,
                        "SunAS9")); // NOI18N

        // TODO Let eclipselink handle java2db till java2db is in place here
//        final String ECLIPSELINK_DDL_GENERATION_MODE_PROPERTY =
//                "eclipselink.ddl-generation.output-mode"; // NOI18N
//        props.put(ECLIPSELINK_DDL_GENERATION_MODE_PROPERTY,
//                System.getProperty(ECLIPSELINK_DDL_GENERATION_MODE_PROPERTY,
//                        "none")); // NOI18N


        // TopLink specific properties:
        // See https://glassfish.dev.java.net/issues/show_bug.cgi?id=249
        final String TOPLINK_SERVER_PLATFORM_CLASS_NAME_PROPERTY =
                "toplink.target-server"; // NOI18N
        props.put(TOPLINK_SERVER_PLATFORM_CLASS_NAME_PROPERTY,
                System.getProperty(TOPLINK_SERVER_PLATFORM_CLASS_NAME_PROPERTY,
                        "SunAS9")); // NOI18N

        // These constants are defined in the entity-persistence module. Redefining them for now.
        // TODO Let toplink handle java2db till java2db is in place here
//        final String TOPLINK_DDL_GENERATION_MODE_PROPERTY =
//                "toplink.ddl-generation.output-mode"; // NOI18N
//        props.put(TOPLINK_DDL_GENERATION_MODE_PROPERTY,
//                System.getProperty(TOPLINK_DDL_GENERATION_MODE_PROPERTY,
//                        "none")); // NOI18N

        // Hibernate specific properties:
        final String HIBERNATE_TRANSACTION_MANAGER_LOOKUP_CLASS_PROPERTY =
                "hibernate.transaction.manager_lookup_class"; // NOI18N
        props.put(HIBERNATE_TRANSACTION_MANAGER_LOOKUP_CLASS_PROPERTY,
                System.getProperty(HIBERNATE_TRANSACTION_MANAGER_LOOKUP_CLASS_PROPERTY,
                        "org.hibernate.transaction.SunONETransactionManagerLookup")); // NOI18N

        // use an unmodifiable map as we pass this to provider and we don't
        // provider to change this.
        integrationProperties = Collections.unmodifiableMap(props);
    }


}
