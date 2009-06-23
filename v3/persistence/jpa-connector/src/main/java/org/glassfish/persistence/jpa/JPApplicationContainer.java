/*
 * Copyright 2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package org.glassfish.persistence.jpa;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.logging.LogDomains;

/**
 * Represents Application Container for JPA
 * One instance of this object is created per deployed bundle.
 * @author Mitesh Meswani
 */
public class JPApplicationContainer implements ApplicationContainer {

    /**
     * emfs loaded from this bundle. These emfs are closed in stop()
     */
    List<EntityManagerFactory> emfs;

    // TODO change logger name from DPL_LOGGER to persistence logger
    private static Logger logger = LogDomains.getLogger(PersistenceUnitLoader.class, LogDomains.DPL_LOGGER);

    public JPApplicationContainer(List<EntityManagerFactory> emfs) {
        this.emfs = emfs;
    }

    private void closeAllEMFs() {
        for(EntityManagerFactory emf : emfs) {
            try {
                logger.logp(Level.FINE, "JPApplicationContainer", "closeEMF", "emf = {0}", emf);
                emf.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
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

}
