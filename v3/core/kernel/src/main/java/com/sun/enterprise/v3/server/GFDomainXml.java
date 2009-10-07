package com.sun.enterprise.v3.server;

import org.glassfish.config.support.GlassFishDocument;
import org.glassfish.config.support.DomainXml;
import org.glassfish.internal.api.*;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.DomDocument;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Subclass of domain.xml loader service to ensure that hk2 threads have access
 * to the common class loader classes.
 *
 * @author Jerome Dochez
 */
@Service
public class GFDomainXml extends DomainXml {

    /**
     * Returns the DomDocument implementation used to create config beans and persist
     * the DOM tree.
     *
     * @return an instance of a DomDocument (or subclass)
     */
    protected DomDocument getDomDocument() {
        return new GlassFishDocument(habitat,
                    Executors.newCachedThreadPool(new ThreadFactory() {

                        public Thread newThread(Runnable r) {
                            Thread t = Executors.defaultThreadFactory().newThread(r);
                            t.setDaemon(true);
                            t.setContextClassLoader(habitat.getComponent(ServerContext.class).getCommonClassLoader());
                            return t;
                        }

                    }));
    }    
}
