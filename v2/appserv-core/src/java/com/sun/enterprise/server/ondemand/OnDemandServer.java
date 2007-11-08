/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.server.ondemand;

import java.util.*;
import java.util.logging.*;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import com.sun.logging.LogDomains;
import com.sun.enterprise.server.*;
import com.sun.enterprise.server.ss.*;
import com.sun.enterprise.server.ondemand.entry.*;

import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.LifecycleEvent;
import com.sun.appserv.server.ServerLifecycleException;                                                                                                                             
import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;
import com.sun.enterprise.server.pluggable.PluggableFeatureFactoryImpl;
import com.sun.enterprise.server.pluggable.InternalServicesList;

/**
 * Represents on-demand server. This is the main class that ties ondemand logic
 * with rest of the application server.
 */
public class OnDemandServer extends ApplicationServer implements EntryPoint{
    private static boolean onDemandStartup = true;
    private static volatile ServerEntryListener listener = null;
    private static volatile SystemAppLoader systemAppLoader = null;
    private volatile ServiceGroup sg = null;

    /**
     * Server is initializing subsystems and setting up the runtime environment.
     * Prepare for the beginning of active use of the public methods of this
     * subsystem. This method is called before any of the public methods of 
     * this subsystem are utilized.  
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception IllegalStateException if this subsystem has already been
     *  started
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onInitialization(ServerContext context) 
                        throws ServerLifecycleException {
        listener = new ServerEntryListenerImpl(this);
        ServiceGroupBuilder builder = new ServiceGroupBuilder();
        sg = builder.buildServiceGroup(this);

        // Start the lazy startup framework.
        // This will open up network ports and block any incoming connections
        // till server startup completes.
        try {
            super.setServerContext(context);
            PluggableFeatureFactory pff = context.getPluggableFeatureFactory();
            ASLazyKernel lazyStartupKernel = pff.getASLazyKernel();
            onDemandStartup = lazyStartupKernel.startASSocketServices(context) ;
        } catch (Exception e) {
            onDemandStartup = false;
            // Quick startup is not enabled. Start with normal sequence.
        }
        //ORB is initialized lazily. If this variable is not set, 
        //Naming code somehow initializes ORB.
        com.sun.enterprise.naming.SerialInitContextFactory.
        setInitializeOrbManager(false);
        super.onInitialization(context);
        try {
            systemAppLoader = new SystemAppLoader();
        } catch (Exception e) {
            throw new ServerLifecycleException(e);
        }
    }

    /**
     * Server is starting up applications
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onStartup() 
                        throws ServerLifecycleException {
        generateEntryContext(new Boolean(onDemandStartup));
        super.onStartup();
    }

    /**
     * Server is shutting down applications
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onShutdown() 
                        throws ServerLifecycleException {
        super.onShutdown();
        try {
            sg.stop(null);
        } catch (Exception e) {
            throw new ServerLifecycleException(e);
        }
    }

    public void generateEntryContext(Object event) {
        ServerEntryHelper.generateStartUpEntryContext((Boolean) event);
    }

        protected List<ServerLifecycle>
    instantiateRuntimeServices(
        final ServerContext serverContext,
        final String[][]    defaultServices )
            throws ServerLifecycleException {
        if (onDemandStartup) {
            InternalServicesList services = serverContext.getPluggableFeatureFactory().getInternalServicesList();
            final InternalServicesList ondemandservices = new OnDemandServices();
            final String[][] servicesByName = services.getServicesByName();
            final String[][] odsByName = ondemandservices.getServicesByName();
            if (servicesByName == null) {
               _logger.log(Level.SEVERE, "services.null");
               throw new ServerLifecycleException();
            }

            final List<ServerLifecycle> serviceList = new ArrayList<ServerLifecycle>();

            // Instantiate service objects
            allServices :
            for (final String[] service : servicesByName) {
               for (final String[] ods : odsByName) {
                    if (service[0].equals(ods[0])) {
                        continue allServices;
                    }
               }
               try {
                   final ServerLifecycle serverLifecycle =
                    super.instantiateOneServerLifecycle( serverContext, service[1] );
                   serviceList.add( serverLifecycle );
               } catch (Exception ex) {
                   _logger.log(Level.SEVERE, "server.exception", ex);
                   throw new ServerLifecycleException(ex.getMessage());
               }
            }
            return serviceList;
        } else {
            return super.instantiateRuntimeServices( serverContext, defaultServices);
        }
    }

    // Return the server entry listener.
    public static ServerEntryListener getServerEntryListener() {
        return listener;
    }

    // Return the system apploader.
    public static SystemAppLoader getSystemAppLoader() {
        return systemAppLoader;
    }

    // Return the main service group.
    public ServiceGroup getServiceGroup() {
        return sg;
    }

    public static boolean isOnDemandOff() {
        return onDemandStartup == false;
    }

    // Retrieves the key representing the channel's registration 
    // with the given selector. Work around for 6562829.
    public static SelectionKey keyFor(SelectableChannel channel, Selector sel) {
        if (onDemandStartup == false) {
            return channel.keyFor(sel);
        }

        return ASSocketService.keyFor(channel, sel);
    }
    
}
