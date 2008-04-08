package com.sun.enterprise.naming.impl;

import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamingObjectsProvider;
import org.jvnet.hk2.component.Habitat;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements the JNDI SPI InitialContextFactory interface used to create
 * the InitialContext objects. It creates an instance of the serial context.
 */

public class SerialInitContextFactory implements InitialContextFactory {


    private Hashtable defaultEnv;
    private final Habitat habitat;

    private boolean useS1ASCtxFactory;

    private static boolean initialized = false;

    /**
     * Default constructor. Creates an ORB if one is not already created.
     */
    public SerialInitContextFactory(Hashtable environemnt, Habitat habitat) {

        this.defaultEnv = environemnt;
        this.habitat = habitat;

    }
    
    /**
     * Create the InitialContext object.
     */
    public Context getInitialContext(Hashtable env) throws NamingException {

        //Another Big TODO Sync with useS1ASCtxFactory

        // this lock needs to be reentrant as the lookup of the NamingObjectsProvider
        // will most likely trigger access to the naming manager and the serial init context.
        synchronized(SerialInitContextFactory.class) {
            if (!initialized) {
                // this must be set first as we don't want to get into infinite loop while
                // doing the first initialization
                initialized=true;

                // this should force the initialization of the resources providers
                if (habitat!=null) {
                    for (NamingObjectsProvider provider : habitat.getAllByContract(NamingObjectsProvider.class)) {
                        System.out.println("Provider " + provider);
                    }
                }
            }
        }

        if (env != null) {
            return new SerialContext(env, habitat);
        } else {
            return new SerialContext(defaultEnv, habitat);
        }
    }
}
