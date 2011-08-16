/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.samples;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
import org.glassfish.flashlight.provider.ProbeRegistry;

/**
 *
 * @author arun
 */
@Singleton
@LocalBean
@Startup
@ProbeProvider(moduleProviderName = "JavaOne", moduleName = "JavaOneSamples", probeProviderName = "DatabaseSingletonBean")
public class DatabaseSingletonBean {
    @PersistenceContext
    EntityManager em;
    // Add business logic below. (Right-click in editor and choose
    @Resource
    private ProbeProviderFactory probeProviderFactory;
    @Resource
    private ProbeClientMediator listenerRegistrar;
    @Resource
    private ProbeRegistry probeRegistry;

    @PostConstruct
    @Probe(name = "initMovies")
    public void init() {
        initMonitoring();
        addMovie("Black Swan");
        addMovie("The Matrix");
        addMovie("Inception");
        addMovie("The Fighter");
        addMovie("Iron Man 2");
        addMovie("Invictus");
        addMovie("Up in the Air");
        addMovie("Million Dollar Baby");
    }

    @Probe(name = "addMovie")
    public void addMovie(String name) {
        Movie m = new Movie(name);
        em.persist(m);
    }

    private void initMonitoring() {
             if (probeProviderFactory == null)
            return;

        if (listenerRegistrar == null)
            return;

        try {
            // need to get the probe provider registered before the listener!
            probeProviderFactory.getProbeProvider(getClass());
            listenerRegistrar.registerListener(new DatabaseSingletonBeanListener());
        }
        catch (Exception e) {
            return;
        }
    }
}
