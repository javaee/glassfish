package com.sun.enterprise.naming.impl;

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

    private boolean useS1ASCtxFactory;

    /**
     * Default constructor. Creates an ORB if one is not already created.
     */
    public SerialInitContextFactory() {

        if (useS1ASCtxFactory) {
            //A big TODO
        } else {
            // create a default env
            defaultEnv = new Hashtable();
        }
    }

    /**
     * Create the InitialContext object.
     */
    public Context getInitialContext(Hashtable env) throws NamingException {

        //Another Big TODO Sync with useS1ASCtxFactory

        if (env != null) {
            return new SerialContext(env);
        } else {
            return new SerialContext(defaultEnv);
        }
    }
}
