package com.sun.enterprise.naming.impl;

import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.jvnet.hk2.component.Habitat;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements the JNDI SPI InitialContextFactory interface used to create
 * the InitialContext objects. It creates an instance of the serial context.
 */

public class SerialInitContextFactory implements InitialContextFactory {


    private Hashtable defaultEnv;
    private final Habitat habitat;

    private boolean useS1ASCtxFactory;

    private static AtomicBoolean doneWithBindObjects
            = new AtomicBoolean(false);

    /**
     * Default constructor. Creates an ORB if one is not already created.
     */
    public SerialInitContextFactory(Hashtable environemnt, Habitat habitat) {

        this.defaultEnv = environemnt;
        this.habitat = habitat;

    }

    public static void checkForBind(Habitat habitat)
        throws NamingException {
        if (!doneWithBindObjects.get()) {
            if (habitat != null) {
                synchronized (doneWithBindObjects) {
                    if (!doneWithBindObjects.get()) {
                        GlassfishNamingManager nm =
                                habitat.getByContract(GlassfishNamingManager.class);
                        System.out.println("Got nm: " + nm.getClass().getName());
                        for (NamedNamingObjectProxy proxy : habitat.getAllByContract(NamedNamingObjectProxy.class)) {
                            System.out.println("Got NamedNamingObjectProxy: " + proxy.getClass().getName());
                            nm.publishObject(proxy.getName(), proxy, false);
                            System.out.println("BOUND " + proxy.getClass().getName() + "  @@ " + proxy.getName());
                        }
                        doneWithBindObjects.set(true);
                    }
                }
            }
        }
    }

    /**
     * Create the InitialContext object.
     */
    public Context getInitialContext(Hashtable env) throws NamingException {

        //Another Big TODO Sync with useS1ASCtxFactory

        if (env != null) {
            return new SerialContext(env, habitat);
        } else {
            return new SerialContext(defaultEnv, habitat);
        }
    }
}
