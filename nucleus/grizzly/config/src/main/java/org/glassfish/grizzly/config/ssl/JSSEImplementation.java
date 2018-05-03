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
import javax.net.ssl.SSLEngine;

import org.glassfish.grizzly.ssl.SSLSupport;

/**
 * JSSEImplementation:
 *
 * Concrete implementation class for JSSE
 *
 * @author EKR
 */
public class JSSEImplementation extends SSLImplementation {
    static final String JSSE14Factory = "org.glassfish.grizzly.config.ssl.JSSE14Factory";
    static final String SSLSocketClass = "javax.net.ssl.SSLSocket";
    private JSSEFactory factory;

    public JSSEImplementation() throws ClassNotFoundException {
        // Check to see if JSSE is floating around somewhere
        Class.forName(SSLSocketClass);
        try {
            Class factcl = Class.forName(JSSE14Factory);
            factory = (JSSEFactory) factcl.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getImplementationName() {
        return "JSSE";
    }

    @Override
    public ServerSocketFactory getServerSocketFactory() {
        return factory.getSocketFactory();
    }

    @Override
    public SSLSupport getSSLSupport(Socket s) {
        return factory.getSSLSupport(s);
    }
    // START SJSAS 6439313

    @Override
    public SSLSupport getSSLSupport(SSLEngine sslEngine) {
        return factory.getSSLSupport(sslEngine);
    }
    // END SJSAS 6439313    
}
