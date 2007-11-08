/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.enterprise.server;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.loader.InstrumentableClassLoader;
import com.sun.logging.LogDomains;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

/**
 * {@inheritDoc}
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistenceUnitLoaderImpl implements PersistenceUnitLoader {

    /**
     * logger to log loader messages
     */
    private static Logger logger = LogDomains.getLogger(
            LogDomains.LOADER_LOGGER);

    private String applicationLocation;

    private InstrumentableClassLoader classLoader;

    private Application application;

    private static Map<String, String> integrationProperties;

    /**
     * {@inheritDoc}
     */
    public void load(ApplicationInfo appInfo) {
        application = appInfo.getApplication();
        applicationLocation = appInfo.getApplicationLocation();
        classLoader = appInfo.getClassLoader();
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("Loading persistence units for application: " +
                    applicationLocation);
        }
        for(PersistenceUnitDescriptor pu : appInfo.getReferencedPUs()) {
            load(pu);
        }
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("Finished loading persistence units for application: " +
                    applicationLocation);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unload(ApplicationInfo appInfo) {
        application = appInfo.getApplication();
        applicationLocation = appInfo.getApplicationLocation();
        classLoader = appInfo.getClassLoader();
        final boolean fineMsgLoggable = logger.isLoggable(Level.FINE);
        if(fineMsgLoggable) {
            logger.fine("Unloading persistence units for application: " + // NOI18N
                    applicationLocation);
        }
        closeEMFs(appInfo.getEntityManagerFactories());
        if(fineMsgLoggable) {
            logger.fine("Finished unloading persistence units for application: " + // NOI18N
                    applicationLocation);
        }
    }

    /**
     * Loads an individual PersistenceUnitDescriptor and registers the
     * EntityManagerFactory in appropriate DOL structure.
     *
     * @param pud PersistenceUnitDescriptor to be loaded.
     */
    private void load(PersistenceUnitDescriptor pud) {
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("loading pud " + pud.getPuRoot()); // NOI18N
        }
        PersistenceUnitInfo pInfo = new PersistenceUnitInfoImpl(
                pud,
                applicationLocation,
                classLoader);
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("PersistenceInfo for this pud is :\n" + pInfo); // NOI18N
        }
        PersistenceProvider provider;
        try {
            // See we use application CL as opposed to system CL to load
            // provider. This allows user to get hold of provider specific
            // implementation classes in their code. But this also means
            // provider must not use appserver implementation classes directly
            // because once we implement isolation in our class loader hierarchy
            // the only classes available to application class loader would be
            // our appserver interface classes. By Sahoo
            provider =
                    PersistenceProvider.class.cast(
                    ClassLoader.class.cast(classLoader)
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
        logger.logp(Level.FINE, "PersistenceUnitLoaderImpl", "load", // NOI18N
                    "emf = {0}", emf); // NOI18N

        RootDeploymentDescriptor rootDD = pud.getParent().getParent();
        if (rootDD.isApplication()) {
            Application.class.cast(rootDD).addEntityManagerFactory(
                    pInfo.getPersistenceUnitName(), pud.getPuRoot(), emf);
        } else {
            BundleDescriptor.class.cast(rootDD).addEntityManagerFactory(
                    pInfo.getPersistenceUnitName(), emf);
        }
    }

    private void closeEMFs(
            Collection<? extends EntityManagerFactory> entityManagerFactories) {
        logger.logp(Level.FINE, "PersistenceUnitLoaderImpl", "closeEMFs", // NOI18N
                "entityManagerFactories.size() = {0}", // NOI18N
                entityManagerFactories.size());
        for (EntityManagerFactory emf : entityManagerFactories) {
            try {
                logger.logp(Level.FINE, "PersistenceUnitLoaderImpl",
                        "closeEMFs", "emf = {0}", emf);
                emf.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
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

        // TopLink specific properties:
        // See https://glassfish.dev.java.net/issues/show_bug.cgi?id=249
        final String TOPLINK_SERVER_PLATFORM_CLASS_NAME_PROPERTY =
                "toplink.target-server"; // NOI18N
        props.put(TOPLINK_SERVER_PLATFORM_CLASS_NAME_PROPERTY,
                System.getProperty(TOPLINK_SERVER_PLATFORM_CLASS_NAME_PROPERTY,
                        "oracle.toplink.essentials.platform.server.sunas.SunAS9ServerPlatform")); // NOI18N
        
        // These constants are defined in the entity-persistence module. Redefining them for now.
        final String TOPLINK_DDL_GENERATION_MODE_PROPERTY =
                "toplink.ddl-generation.output-mode"; // NOI18N        
        props.put(TOPLINK_DDL_GENERATION_MODE_PROPERTY,
                System.getProperty(TOPLINK_DDL_GENERATION_MODE_PROPERTY,
                        "none")); // NOI18N

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
