/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.admin.connector.rmi;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.logging.Logger;


/** A package-private class that deals with the RMI registry in the VM.
 * This RMI registry is used as the naming service to find the rmi stub.
 *
 * @author  kedar
 * @since Sun Java System Application Server 8.1
 */
class RmiStubRegistryHandler {
    private final Logger logger;
    
    /** Starts the RMI registry at the given port. If the security flag is
     * false an in-process "insecure" rmi registry will be created. If the flag
     * is true, an attempt will be made if the registry could be made secure.
     * Running on JDK 1.4.x has a limitation of not being able to create multiple
     * registries in the same VM.
     */
    RmiStubRegistryHandler(final int port, final boolean secureRegistry, final Logger logger) {
        if (logger == null)
            throw new IllegalArgumentException("Internal: Null logger");
        this.logger = logger;
        if (secureRegistry) {
            throw new UnsupportedOperationException("Yet to be implemented");
        }
        else {
            startInsecureRegistry(port);
        }
    }
    /* Not needed as the setup of RMIConnectorServer would do it - we only
       need to start the registry. No need to pass on the Socket Factories. */
    /*
    RmiStubRegistryHandler(final int port, final boolean secureRegistry, final RMIClientSocketFactory cf, final RMIServerSocketFactory sf) {
        if (secureRegistry) {
            throw new UnsupportedOperationException("Yet to be implemented");
        }
        else {
            startInsecureRegistry(port, cf, sf);
        }
    }
    */
    private void startInsecureRegistry(final int port) {
        try {
            final Registry r = LocateRegistry.createRegistry(port);
            logBindings(r, port);
        }
        catch (final Exception e) {
            throw new RuntimeException("Port " + port + " is not available for the internal rmi registry. " + 
                "This means that a call was made with the same port, without closing earlier " +
                "registry instance. This has to do with the system jmx connector configuration " +
                "in admin-service element of the configuration associated with this instance" );
        }
    }
    /* Not needed as the setup of RMIConnectorServer would do it - we only
       need to start the registry. No need to pass on the Socket Factories. */
    /*
    private void startInsecureRegistry(final int port, final RMIClientSocketFactory cf, final RMIServerSocketFactory sf) {
        try {
            LocateRegistry.createRegistry(port, cf, sf);
        }
        catch (final Exception e) {
        }
    }
    */
    private void logBindings(final Registry r, final int port) {
        try {
            final String[] bs = r.list();
            logger.fine("Initial Bindings in RmiRegistry at port: [" + port + "] :");
            for (int i = 0 ; i < bs.length ; i++) {
                logger.fine("JMX Connector RMI Registry binding: " + bs[i]);
            }
        }
        catch(final Exception e) {
            e.printStackTrace();
            //squelching this exception is okay, as only logging is affected.
        }
    }
}