/*
 * Copyright (c) 2007-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.glassfish.grizzly.config.ssl;

import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLEngine;
import org.glassfish.grizzly.config.GrizzlyConfig;

import org.glassfish.grizzly.ssl.SSLSupport;

/**
 * SSLImplementation:
 *
 * Abstract factory and base class for all SSL implementations.
 *
 * @author EKR
 */
public abstract class SSLImplementation {
    /**
     * Default Logger.
     */
    private final static Logger logger = GrizzlyConfig.logger();
    // The default implementations in our search path
    private static final String JSSEImplementationClass = JSSEImplementation.class.getName();
    private static final String[] implementations = {JSSEImplementationClass};

    public static SSLImplementation getInstance() throws ClassNotFoundException {
        for (String implementation : implementations) {
            try {
                return getInstance(implementation);
            } catch (Exception e) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Error creating " + implementation, e);
                }
            }
        }
        // If we can't instantiate any of these
        throw new ClassNotFoundException("Can't find any SSL implementation");
    }

    public static SSLImplementation getInstance(String className) throws ClassNotFoundException {
        if (className == null) {
            return getInstance();
        }
        try {
            return (SSLImplementation) ((Class) Class.forName(className)).newInstance();
        } catch (Exception e) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Error loading SSL Implementation " + className, e);
            }
            throw new ClassNotFoundException("Error loading SSL Implementation " + className + " :" + e.toString());
        }
    }

    public abstract String getImplementationName();

    public abstract ServerSocketFactory getServerSocketFactory();

    public abstract SSLSupport getSSLSupport(Socket sock);

    public abstract SSLSupport getSSLSupport(SSLEngine sslEngine);
}    
