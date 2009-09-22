/*
 * Copyright 2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.jdo.spi.persistence.support.sqlstore.utility;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import com.sun.enterprise.config.serverbeans.*;

import java.util.Collection;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyVetoException;

/**
 * @author Mitesh Meswani
 */
@Service
public class PersistenceManagerFactoryResourceMigrator implements ConfigurationUpgrade, PostConstruct {
    @Inject
    Resources resources;

    public void postConstruct() {
        Collection<PersistenceManagerFactoryResource> pmfResources = resources.getResources(PersistenceManagerFactoryResource.class);
        for (final PersistenceManagerFactoryResource pmfResource : pmfResources) {
            String jdbcResourceName = pmfResource.getJdbcResourceJndiName();

            final JdbcResource jdbcResource = (JdbcResource )resources.getResourceByName(JdbcResource.class, jdbcResourceName);

            try {
                ConfigSupport.apply(new SingleConfigCode<Resources>() {

                    public Object run(Resources resources) throws PropertyVetoException, TransactionFailure {
                        // delete the persitence-manager-factory resource
                        resources.getResources().remove(pmfResource);

                        // create a jdbc resource which points to same connection pool and has same jndi name as pmf resource.
                        JdbcResource newResource = resources.createChild(JdbcResource.class);
                        newResource.setJndiName(pmfResource.getJndiName());
                        newResource.setDescription("Created to migrate persistence-manager-factory-resource from V2 domain");
                        newResource.setPoolName(jdbcResource.getPoolName());
                        newResource.setEnabled("true");
                        resources.getResources().add(newResource);
                        return newResource;
                    }
                }, resources);
            } catch (TransactionFailure tf) {
                Logger.getAnonymousLogger().log(Level.SEVERE,
                    "Failure while upgrading persistence-manager-factory-resource", tf);
                throw new RuntimeException(tf);

            }

        } // end of iteration
    }
}
