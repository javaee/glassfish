package com.sun.enterprise.naming.impl;

import org.jvnet.hk2.component.Habitat;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * Implements the JNDI SPI InitialContextFactory interface used to create
 * the InitialContext objects. It creates an instance of the serial context.
 */

public class SerialInitContextFactory implements InitialContextFactory {


    private Hashtable defaultEnv;
    private final Habitat habitat;

    private boolean useS1ASCtxFactory;

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

        if (env != null) {
            return new SerialContext(env, habitat);
        } else {
            return new SerialContext(defaultEnv, habitat);
        }
    }
}
