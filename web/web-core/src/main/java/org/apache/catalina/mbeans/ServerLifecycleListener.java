/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
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



package org.apache.catalina.mbeans;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.StringTokenizer;
import java.util.logging.*;
import javax.management.MBeanException;

import org.apache.catalina.Connector;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.DefaultContext;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.NamingResources;


/**
 * Implementation of <code>LifecycleListener</code> that
 * instantiates the set of MBeans associated with the components of a
 * running instance of Catalina.
 *
 * @author Craig R. McClanahan
 * @author Amy Roh
 * @version $Revision: 1.3 $ $Date: 2006/03/12 01:27:03 $
 */

public class ServerLifecycleListener
    implements ContainerListener, LifecycleListener, PropertyChangeListener {

    private static Logger log = Logger.getLogger(ServerLifecycleListener.class.getName());



    // ------------------------------------------------------------- Properties


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;

    public int getDebug() {
        return (this.debug);
    }

    public void setDebug(int debug) {
        this.debug = debug;
    }


    /**
     * Semicolon separated list of paths containing MBean desciptor resources.
     */
    protected String descriptors = null;

    public String getDescriptors() {
        return (this.descriptors);
    }

    public void setDescriptors(String descriptors) {
        this.descriptors = descriptors;
    }


    /**
     * MX4J adaptor name.
     */
    protected String adaptor = null;

    public String getAdaptor() {
        return (this.adaptor);
    }

    public void setAdaptor(String adaptor) {
        this.adaptor = adaptor;
    }

    /**
     * MX4J jrmp/iiop listen host
     */ 
    protected String adaptorHost = null;

    public String getAdaptorHost() {
        return (this.adaptorHost);
    }

    public void setAdaptorHost(String adaptorHost) {
        this.adaptorHost = adaptorHost;
    }

    /**
     * MX4J jrmp/iiop listen port
     */ 
    protected int adaptorPort = -1;

    public int getAdaptorPort() {
        return (this.adaptorPort);
    }

    public void setAdaptorPort(int adaptorPort) {
        this.adaptorPort = adaptorPort;
    }


    // ---------------------------------------------- ContainerListener Methods


    /**
     * Handle a <code>ContainerEvent</code> from one of the Containers we are
     * interested in.
     *
     * @param event The event that has occurred
     */
    public void containerEvent(ContainerEvent event) {

        try {
            String type = event.getType();
            if (Container.ADD_CHILD_EVENT.equals(type)) {
                processContainerAddChild(event.getContainer(),
                                         (Container) event.getData());
            } else if (Container.REMOVE_CHILD_EVENT.equals(type)) {
                processContainerRemoveChild(event.getContainer(),
                                            (Container) event.getData());
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception processing event " + event, e);
        }

    }


    // ---------------------------------------------- LifecycleListener Methods


    /**
     * Primary entry point for startup and shutdown events.
     *
     * @param event The event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        Lifecycle lifecycle = event.getLifecycle();
        if (Lifecycle.START_EVENT.equals(event.getType())) {

            if (lifecycle instanceof Server) {

                // Loading additional MBean descriptors
                loadMBeanDescriptors();

                createMBeans();

                if (adaptor != null) {
                    try {
                        MBeanUtils.createRMIAdaptor(adaptor, adaptorHost, adaptorPort);
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "createAdaptor: Exception", e);
                    }
                }

            }

            // We are embedded.
            if( lifecycle instanceof Service ) {
                try {
                    MBeanFactory factory = new MBeanFactory();
                    createMBeans(factory);
                    loadMBeanDescriptors();
                    createMBeans((Service)lifecycle);
                } catch( Exception ex ) {
                    log.severe("Create mbean factory");
                }
            }

            /*
            // Ignore events from StandardContext objects to avoid
            // reregistering the context
            if (lifecycle instanceof StandardContext)
                return;
            createMBeans();
            */

        } else if (Lifecycle.STOP_EVENT.equals(event.getType())) {
            try {
                if (lifecycle instanceof Server) {
                    destroyMBeans((Server)lifecycle);
                }
                if (lifecycle instanceof Service) {
                    destroyMBeans((Service)lifecycle);
                }
            } catch (MBeanException t) {

                Exception e = t.getTargetException();
                if (e == null) {
                    e = t;
                }
                log.log(Level.SEVERE, "destroyMBeans: MBeanException", e);

            } catch (Throwable t) {

                log.log(Level.SEVERE, "destroyMBeans: Throwable", t);

            }
            // FIXME: RMI adaptor should be stopped; however, this is
            // undocumented in MX4J, and reports exist in the MX4J bug DB that
            // this doesn't work

        }

        if ((Context.RELOAD_EVENT.equals(event.getType()))
            || (Lifecycle.START_EVENT.equals(event.getType()))) {

            // Give context a new handle to the MBean server if the
            // context has been reloaded since reloading causes the
            // context to lose its previous handle to the server
            if (lifecycle instanceof StandardContext) {
                // If the context is privileged, give a reference to it
                // in a servlet context attribute
                StandardContext context = (StandardContext)lifecycle;
                if (context.getPrivileged()) {
                    context.getServletContext().setAttribute
                        (Globals.MBEAN_REGISTRY_ATTR,
                         MBeanUtils.createRegistry());
                    context.getServletContext().setAttribute
                        (Globals.MBEAN_SERVER_ATTR,
                         MBeanUtils.createServer());
                }
            }

        }

    }


    // ----------------------------------------- PropertyChangeListener Methods


    /**
     * Handle a <code>PropertyChangeEvent</code> from one of the Containers
     * we are interested in.
     *
     * @param event The event that has occurred
     */
    public void propertyChange(PropertyChangeEvent event) {

        if (event.getSource() instanceof Container) {
            try {
                processContainerPropertyChange((Container) event.getSource(),
                                               event.getPropertyName(),
                                               event.getOldValue(),
                                               event.getNewValue());
            } catch (Exception e) {
                log.log(Level.SEVERE,
                        "Exception handling Container property change", e);
            }
        } else if (event.getSource() instanceof DefaultContext) {
            try {
                processDefaultContextPropertyChange
                    ((DefaultContext) event.getSource(),
                     event.getPropertyName(),
                     event.getOldValue(),
                     event.getNewValue());
            } catch (Exception e) {
                log.log(Level.SEVERE,
                        "Exception handling DefaultContext property change",
                        e);
            }
        } else if (event.getSource() instanceof NamingResources) {
            try {
                processNamingResourcesPropertyChange
                    ((NamingResources) event.getSource(),
                     event.getPropertyName(),
                     event.getOldValue(),
                     event.getNewValue());
            } catch (Exception e) {
                log.log(Level.SEVERE,
                        "Exception handling NamingResources property change",
                        e);
            }
        } else if (event.getSource() instanceof Server) {
            try {
                processServerPropertyChange((Server) event.getSource(),
                                            event.getPropertyName(),
                                            event.getOldValue(),
                                            event.getNewValue());
            } catch (Exception e) {
                log.log(Level.SEVERE,
                        "Exception handing Server property change", e);
            }
        } else if (event.getSource() instanceof Service) {
            try {
                processServicePropertyChange((Service) event.getSource(),
                                             event.getPropertyName(),
                                             event.getOldValue(),
                                             event.getNewValue());
            } catch (Exception e) {
                log.log(Level.SEVERE,
                        "Exception handing Service property change", e);
            }
        }

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Load additional MBean descriptor resources.
     */
    protected void loadMBeanDescriptors() {

        if (descriptors != null) {
            StringTokenizer tokenizer = new StringTokenizer(descriptors, ";");
            while (tokenizer.hasMoreTokens()) {
                String resource = tokenizer.nextToken();
                MBeanUtils.loadMBeanDescriptors(resource);
            }
        }

    }


    /**
     * Create the MBeans that correspond to every existing node of our tree.
     */
    protected void createMBeans() {

        try {

            MBeanFactory factory = new MBeanFactory();
            createMBeans(factory);
            createMBeans(ServerFactory.getServer());

        } catch (MBeanException t) {

            Exception e = t.getTargetException();
            if (e == null)
                e = t;
            log.log(Level.SEVERE, "createMBeans: MBeanException", e);

        } catch (Throwable t) {

            log.log(Level.SEVERE, "createMBeans: Throwable", t);

        }

    }


    /**
     * Create the MBeans for the specified Connector and its nested components.
     *
     * @param connector Connector for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Connector connector) throws Exception {

        // Create the MBean for the Connnector itself
//        if (log.isDebugEnabled())
//            log.fine("Creating MBean for Connector " + connector);
//        MBeanUtils.createMBean(connector);

    }


    /**
     * Create the MBeans for the specified Context and its nested components.
     *
     * @param context Context for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Context context) throws Exception {

        // Create the MBean for the Context itself
//        if (log.isDebugEnabled())
//            log.fine("Creating MBean for Context " + context);
//        MBeanUtils.createMBean(context);
        context.addContainerListener(this);
        if (context instanceof StandardContext) {
            ((StandardContext) context).addPropertyChangeListener(this);
            ((StandardContext) context).addLifecycleListener(this);
        }

        // If the context is privileged, give a reference to it
        // in a servlet context attribute
        if (context.getPrivileged()) {
            context.getServletContext().setAttribute
                (Globals.MBEAN_REGISTRY_ATTR,
                 MBeanUtils.createRegistry());
            context.getServletContext().setAttribute
                (Globals.MBEAN_SERVER_ATTR,
                 MBeanUtils.createServer());
        }

        // Create the MBeans for the associated nested components
        Loader cLoader = context.getLoader();
        if (cLoader != null) {
            if (log.isLoggable(Level.FINE))
                log.fine("Creating MBean for Loader " + cLoader);
            //MBeanUtils.createMBean(cLoader);
        }
        org.apache.catalina.Logger hLogger = context.getParent().getLogger();
        org.apache.catalina.Logger cLogger = context.getLogger();
        if ((cLogger != null) && (cLogger != hLogger)) {
            if (log.isLoggable(Level.FINE))
                log.fine("Creating MBean for Logger " + cLogger);
            //MBeanUtils.createMBean(cLogger);
        }
        Manager cManager = context.getManager();
        if (cManager != null) {
            if (log.isLoggable(Level.FINE))
                log.fine("Creating MBean for Manager " + cManager);
            //MBeanUtils.createMBean(cManager);
        }
        Realm hRealm = context.getParent().getRealm();
        Realm cRealm = context.getRealm();
        if ((cRealm != null) && (cRealm != hRealm)) {
            if (log.isLoggable(Level.FINE))
                log.fine("Creating MBean for Realm " + cRealm);
            //MBeanUtils.createMBean(cRealm);
        }

        // Create the MBeans for the NamingResources (if any)
        NamingResources resources = context.getNamingResources();
        createMBeans(resources);

    }


    /**
     * Create the MBeans for the specified ContextEnvironment entry.
     *
     * @param environment ContextEnvironment for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(ContextEnvironment environment)
        throws Exception {

        // Create the MBean for the ContextEnvironment itself
        if (log.isLoggable(Level.FINE)) {
            log.fine("Creating MBean for ContextEnvironment " + environment);
        }
        MBeanUtils.createMBean(environment);

    }


    /**
     * Create the MBeans for the specified ContextResource entry.
     *
     * @param resource ContextResource for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(ContextResource resource)
        throws Exception {

        // Create the MBean for the ContextResource itself
        if (log.isLoggable(Level.FINE)) {
            log.fine("Creating MBean for ContextResource " + resource);
        }
        MBeanUtils.createMBean(resource);

    }


    /**
     * Create the MBeans for the specified ContextResourceLink entry.
     *
     * @param resourceLink ContextResourceLink for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(ContextResourceLink resourceLink)
        throws Exception {

        // Create the MBean for the ContextResourceLink itself
        if (log.isLoggable(Level.FINE)) {
            log.fine("Creating MBean for ContextResourceLink " + resourceLink);
        }
        MBeanUtils.createMBean(resourceLink);

    }


    /**
     * Create the MBeans for the specified DefaultContext and its nested components.
     *
     * @param dcontext DefaultContext for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(DefaultContext dcontext) throws Exception {

        // Create the MBean for the DefaultContext itself
        if (log.isLoggable(Level.FINE))
            log.fine("Creating MBean for DefaultContext " + dcontext);
        MBeanUtils.createMBean(dcontext);

        dcontext.addPropertyChangeListener(this);

        // Create the MBeans for the associated nested components
        Loader dLoader = dcontext.getLoader();
        if (dLoader != null) {
            if (log.isLoggable(Level.FINE))
                log.fine("Creating MBean for Loader " + dLoader);
            //MBeanUtils.createMBean(dLoader);
        }

        Manager dManager = dcontext.getManager();
        if (dManager != null) {
            if (log.isLoggable(Level.FINE))
                log.fine("Creating MBean for Manager " + dManager);
            //MBeanUtils.createMBean(dManager);
        }

        // Create the MBeans for the NamingResources (if any)
        NamingResources resources = dcontext.getNamingResources();
        createMBeans(resources);

    }


    /**
     * Create the MBeans for the specified Engine and its nested components.
     *
     * @param engine Engine for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Engine engine) throws Exception {

        // Create the MBean for the Engine itself
        if (log.isLoggable(Level.FINE)) {
            log.fine("Creating MBean for Engine " + engine);
        }
        //MBeanUtils.createMBean(engine);
        engine.addContainerListener(this);
        if (engine instanceof StandardEngine) {
            ((StandardEngine) engine).addPropertyChangeListener(this);
        }

        // Create the MBeans for the associated nested components
        org.apache.catalina.Logger eLogger = engine.getLogger();
        if (eLogger != null) {
            if (log.isLoggable(Level.FINE))
                log.fine("Creating MBean for Logger " + eLogger);
            //MBeanUtils.createMBean(eLogger);
        }
        Realm eRealm = engine.getRealm();
        if (eRealm != null) {
            if (log.isLoggable(Level.FINE))
                log.fine("Creating MBean for Realm " + eRealm);
            //MBeanUtils.createMBean(eRealm);
        }

        // Create the MBeans for each child Host
        Container hosts[] = engine.findChildren();
        for (int j = 0; j < hosts.length; j++) {
            createMBeans((Host) hosts[j]);
        }

        // Create the MBeans for DefaultContext
        DefaultContext dcontext = engine.getDefaultContext();
        if (dcontext != null) {
            dcontext.setParent(engine);
            createMBeans(dcontext);
        }

    }


    /**
     * Create the MBeans for the specified Host and its nested components.
     *
     * @param host Host for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Host host) throws Exception {

        // Create the MBean for the Host itself
        if (log.isLoggable(Level.FINE)) {
            log.fine("Creating MBean for Host " + host);
        }
        //MBeanUtils.createMBean(host);
        host.addContainerListener(this);
        if (host instanceof StandardHost) {
            ((StandardHost) host).addPropertyChangeListener(this);
        }

        // Create the MBeans for the associated nested components
        org.apache.catalina.Logger eLogger = host.getParent().getLogger();
        org.apache.catalina.Logger hLogger = host.getLogger();
        if ((hLogger != null) && (hLogger != eLogger)) {
            if (log.isLoggable(Level.FINE))
                log.fine("Creating MBean for Logger " + hLogger);
            //MBeanUtils.createMBean(hLogger);
        }
        Realm eRealm = host.getParent().getRealm();
        Realm hRealm = host.getRealm();
        if ((hRealm != null) && (hRealm != eRealm)) {
            if (log.isLoggable(Level.FINE))
                log.fine("Creating MBean for Realm " + hRealm);
            //MBeanUtils.createMBean(hRealm);
        }

        // Create the MBeans for each child Context
        Container contexts[] = host.findChildren();
        for (int k = 0; k < contexts.length; k++) {
            createMBeans((Context) contexts[k]);
        }

        // Create the MBeans for DefaultContext
        DefaultContext dcontext = host.getDefaultContext();
        if (dcontext != null) {
            dcontext.setParent(host);
            createMBeans(dcontext);
        }

    }


    /**
     * Create the MBeans for MBeanFactory.
     *
     * @param factory MBeanFactory for which to create MBean
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(MBeanFactory factory) throws Exception {

        // Create the MBean for the MBeanFactory
        if (log.isLoggable(Level.FINE))
            log.fine("Creating MBean for MBeanFactory " + factory);
        MBeanUtils.createMBean(factory);

    }


    /**
     * Create the MBeans for the specified NamingResources and its
     * nested components.
     *
     * @param resources NamingResources for which to create MBeans
     */
    protected void createMBeans(NamingResources resources) throws Exception {

        // Create the MBean for the NamingResources itself
        if (log.isLoggable(Level.FINE)) {
            log.fine("Creating MBean for NamingResources " + resources);
        }
        MBeanUtils.createMBean(resources);
        resources.addPropertyChangeListener(this);

        // Create the MBeans for each child environment entry
        ContextEnvironment environments[] = resources.findEnvironments();
        for (int i = 0; i < environments.length; i++) {
            createMBeans(environments[i]);
        }

        // Create the MBeans for each child resource entry
        ContextResource cresources[] = resources.findResources();
        for (int i = 0; i < cresources.length; i++) {
            createMBeans(cresources[i]);
        }

        // Create the MBeans for each child resource link entry
        ContextResourceLink cresourcelinks[] = resources.findResourceLinks();
        for (int i = 0; i < cresourcelinks.length; i++) {
            createMBeans(cresourcelinks[i]);
        }

    }


    /**
     * Create the MBeans for the specified Server and its nested components.
     *
     * @param server Server for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Server server) throws Exception {

        // Create the MBean for the Server itself
        if (log.isLoggable(Level.FINE))
            log.fine("Creating MBean for Server " + server);
        //MBeanUtils.createMBean(server);
        if (server instanceof StandardServer) {
            ((StandardServer) server).addPropertyChangeListener(this);
        }

        // Create the MBeans for the global NamingResources (if any)
        NamingResources resources = server.getGlobalNamingResources();
        if (resources != null) {
            createMBeans(resources);
        }

        // Create the MBeans for each child Service
        Service services[] = server.findServices();
        for (int i = 0; i < services.length; i++) {
            // FIXME - Warp object hierarchy not currently supported
            if (services[i].getContainer().getClass().getName().equals
                ("org.apache.catalina.connector.warp.WarpEngine")) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Skipping MBean for Service " + services[i]);
                }
                continue;
            }
            createMBeans(services[i]);
        }

    }


    /**
     * Create the MBeans for the specified Service and its nested components.
     *
     * @param service Service for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Service service) throws Exception {

        // Create the MBean for the Service itself
        if (log.isLoggable(Level.FINE))
            log.fine("Creating MBean for Service " + service);
        //MBeanUtils.createMBean(service);
        if (service instanceof StandardService) {
            ((StandardService) service).addPropertyChangeListener(this);
        }

        // Create the MBeans for the corresponding Connectors
        Connector connectors[] = service.findConnectors();
        for (int j = 0; j < connectors.length; j++) {
            createMBeans(connectors[j]);
        }

        // Create the MBean for the associated Engine and friends
        Engine engine = (Engine) service.getContainer();
        if (engine != null) {
            createMBeans(engine);
        }

    }




    /**
     * Deregister the MBeans for the specified Connector and its nested
     * components.
     *
     * @param connector Connector for which to deregister MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(Connector connector, Service service)
        throws Exception {

//        // deregister the MBean for the Connector itself
//        if (log.isDebugEnabled())
//            log.fine("Destroying MBean for Connector " + connector);
//        MBeanUtils.destroyMBean(connector, service);

    }


    /**
     * Deregister the MBeans for the specified Context and its nested
     * components.
     *
     * @param context Context for which to deregister MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(Context context) throws Exception {

        // Deregister ourselves as a ContainerListener
        context.removeContainerListener(this);

        // Destroy the MBeans for the associated nested components
        Realm hRealm = context.getParent().getRealm();
        Realm cRealm = context.getRealm();
        if ((cRealm != null) && (cRealm != hRealm)) {
            if (log.isLoggable(Level.FINE))
                log.fine("Destroying MBean for Realm " + cRealm);
            //MBeanUtils.destroyMBean(cRealm);
        }
        Manager cManager = context.getManager();
        if (cManager != null) {
            if (log.isLoggable(Level.FINE))
                log.fine("Destroying MBean for Manager " + cManager);
            //MBeanUtils.destroyMBean(cManager);
        }
        org.apache.catalina.Logger hLogger = context.getParent().getLogger();
        org.apache.catalina.Logger cLogger = context.getLogger();
        if ((cLogger != null) && (cLogger != hLogger)) {
            if (log.isLoggable(Level.FINE))
                log.fine("Destroying MBean for Logger " + cLogger);
            //MBeanUtils.destroyMBean(cLogger);
        }
        Loader cLoader = context.getLoader();
        if (cLoader != null) {
            if (log.isLoggable(Level.FINE))
                log.fine("Destroying MBean for Loader " + cLoader);
            //MBeanUtils.destroyMBean(cLoader);
        }

        // Destroy the MBeans for the NamingResources (if any)
        NamingResources resources = context.getNamingResources();
        if (resources != null) {
            destroyMBeans(resources);
        }

        // deregister the MBean for the Context itself
        if (log.isLoggable(Level.FINE))
            log.fine("Destroying MBean for Context " + context);
        //MBeanUtils.destroyMBean(context);
        if (context instanceof StandardContext) {
            ((StandardContext) context).
                removePropertyChangeListener(this);
        }

    }


    /**
     * Deregister the MBeans for the specified ContextEnvironment entry.
     *
     * @param environment ContextEnvironment for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(ContextEnvironment environment)
        throws Exception {

        // Destroy the MBean for the ContextEnvironment itself
        if (log.isLoggable(Level.FINE)) {
            log.fine("Destroying MBean for ContextEnvironment " + environment);
        }
        MBeanUtils.destroyMBean(environment);

    }


    /**
     * Deregister the MBeans for the specified ContextResource entry.
     *
     * @param resource ContextResource for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(ContextResource resource)
        throws Exception {

        // Destroy the MBean for the ContextResource itself
        if (log.isLoggable(Level.FINE)) {
            log.fine("Destroying MBean for ContextResource " + resource);
        }
        MBeanUtils.destroyMBean(resource);

    }


    /**
     * Deregister the MBeans for the specified ContextResourceLink entry.
     *
     * @param resourceLink ContextResourceLink for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(ContextResourceLink resourceLink)
        throws Exception {

        // Destroy the MBean for the ContextResourceLink itself
        if (log.isLoggable(Level.FINE)) {
            log.fine("Destroying MBean for ContextResourceLink " + resourceLink);
        }
        MBeanUtils.destroyMBean(resourceLink);

    }


    /**
     * Deregister the MBeans for the specified DefaultContext and its nested
     * components.
     *
     * @param dcontext DefaultContext for which to deregister MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(DefaultContext dcontext) throws Exception {

        Manager dManager = dcontext.getManager();
        if (dManager != null) {
            if (log.isLoggable(Level.FINE))
                log.fine("Destroying MBean for Manager " + dManager);
            //MBeanUtils.destroyMBean(dManager);
        }

        Loader dLoader = dcontext.getLoader();
        if (dLoader != null) {
            if (log.isLoggable(Level.FINE))
                log.fine("Destroying MBean for Loader " + dLoader);
            //MBeanUtils.destroyMBean(dLoader);
        }

        // Destroy the MBeans for the NamingResources (if any)
        NamingResources resources = dcontext.getNamingResources();
        if (resources != null) {
            destroyMBeans(resources);
        }

        // deregister the MBean for the DefaultContext itself
        if (log.isLoggable(Level.FINE))
            log.fine("Destroying MBean for Context " + dcontext);
        MBeanUtils.destroyMBean(dcontext);
        dcontext.removePropertyChangeListener(this);

    }


    /**
     * Deregister the MBeans for the specified Engine and its nested
     * components.
     *
     * @param engine Engine for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(Engine engine) throws Exception {

        // Deregister ourselves as a ContainerListener
        engine.removeContainerListener(this);

        // Deregister the MBeans for each child Host
        Container hosts[] = engine.findChildren();
        for (int k = 0; k < hosts.length; k++) {
            destroyMBeans((Host) hosts[k]);
        }

        // Deregister the MBeans for the associated nested components
        Realm eRealm = engine.getRealm();
        if (eRealm != null) {
            if (log.isLoggable(Level.FINE))
                log.fine("Destroying MBean for Realm " + eRealm);
            //MBeanUtils.destroyMBean(eRealm);
        }
        org.apache.catalina.Logger eLogger = engine.getLogger();
        if (eLogger != null) {
            if (log.isLoggable(Level.FINE))
                log.fine("Destroying MBean for Logger " + eLogger);
            //MBeanUtils.destroyMBean(eLogger);
        }

        // Deregister the MBean for the Engine itself
        if (log.isLoggable(Level.FINE)) {
            log.fine("Destroying MBean for Engine " + engine);
        }
        //MBeanUtils.destroyMBean(engine);

    }


    /**
     * Deregister the MBeans for the specified Host and its nested components.
     *
     * @param host Host for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(Host host) throws Exception {

        // Deregister ourselves as a ContainerListener
        host.removeContainerListener(this);

        // Deregister the MBeans for each child Context
        Container contexts[] = host.findChildren();
        for (int k = 0; k < contexts.length; k++) {
            destroyMBeans((Context) contexts[k]);
        }


        // Deregister the MBeans for the associated nested components
        Realm eRealm = host.getParent().getRealm();
        Realm hRealm = host.getRealm();
        if ((hRealm != null) && (hRealm != eRealm)) {
            if (log.isLoggable(Level.FINE))
                log.fine("Destroying MBean for Realm " + hRealm);
            //MBeanUtils.destroyMBean(hRealm);
        }
        org.apache.catalina.Logger eLogger = host.getParent().getLogger();
        org.apache.catalina.Logger hLogger = host.getLogger();
        if ((hLogger != null) && (hLogger != eLogger)) {
            if (log.isLoggable(Level.FINE))
                log.fine("Destroying MBean for Logger " + hLogger);
            //MBeanUtils.destroyMBean(hLogger);
        }

        // Deregister the MBean for the Host itself
        if (log.isLoggable(Level.FINE)) {
            log.fine("Destroying MBean for Host " + host);
        }
        //MBeanUtils.destroyMBean(host);

    }


    /**
     * Deregister the MBeans for the specified NamingResources and its
     * nested components.
     *
     * @param resources NamingResources for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(NamingResources resources) throws Exception {

        // Destroy the MBeans for each child resource entry
        ContextResource cresources[] = resources.findResources();
        for (int i = 0; i < cresources.length; i++) {
            destroyMBeans(cresources[i]);
        }

        // Destroy the MBeans for each child resource link entry
        ContextResourceLink cresourcelinks[] = resources.findResourceLinks();
        for (int i = 0; i < cresourcelinks.length; i++) {
            destroyMBeans(cresourcelinks[i]);
        }

        // Destroy the MBeans for each child environment entry
        ContextEnvironment environments[] = resources.findEnvironments();
        for (int i = 0; i < environments.length; i++) {
            destroyMBeans(environments[i]);
        }

        // Destroy the MBean for the NamingResources itself
        if (log.isLoggable(Level.FINE)) {
            log.fine("Destroying MBean for NamingResources " + resources);
        }
        MBeanUtils.destroyMBean(resources);
        resources.removePropertyChangeListener(this);

    }


    /**
     * Deregister the MBeans for the specified Server and its related
     * components.
     *
     * @param server Server for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(Server server) throws Exception {

        // Destroy the MBeans for the global NamingResources (if any)
        NamingResources resources = server.getGlobalNamingResources();
        if (resources != null) {
            destroyMBeans(resources);
        }

        // Destroy the MBeans for each child Service
        Service services[] = server.findServices();
        for (int i = 0; i < services.length; i++) {
            // FIXME - Warp object hierarchy not currently supported
            if (services[i].getContainer().getClass().getName().equals
                ("org.apache.catalina.connector.warp.WarpEngine")) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Skipping MBean for Service " + services[i]);
                }
                continue;
            }
            destroyMBeans(services[i]);
        }

        // Destroy the MBean for the Server itself
        if (log.isLoggable(Level.FINE)) {
            log.fine("Destroying MBean for Server " + server);
        }
        //MBeanUtils.destroyMBean(server);
        if (server instanceof StandardServer) {
            ((StandardServer) server).removePropertyChangeListener(this);
        }

    }


    /**
     * Deregister the MBeans for the specified Service and its nested
     * components.
     *
     * @param service Service for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(Service service) throws Exception {

        // Deregister the MBeans for the associated Engine
        Engine engine = (Engine) service.getContainer();
        if (engine != null) {
            //destroyMBeans(engine);
        }

//        // Deregister the MBeans for the corresponding Connectors
//        Connector connectors[] = service.findConnectors();
//        for (int j = 0; j < connectors.length; j++) {
//            destroyMBeans(connectors[j], service);
//        }

        // Deregister the MBean for the Service itself
        if (log.isLoggable(Level.FINE)) {
            log.fine("Destroying MBean for Service " + service);
        }
        //MBeanUtils.destroyMBean(service);
        if (service instanceof StandardService) {
            ((StandardService) service).removePropertyChangeListener(this);
        }

    }


    /**
     * Process the addition of a new child Container to a parent Container.
     *
     * @param parent Parent container
     * @param child Child container
     */
    protected void processContainerAddChild(Container parent,
                                            Container child) {

        if (log.isLoggable(Level.FINE))
            log.fine("Process addChild[parent=" + parent + ",child=" + child + "]");

        try {
            if (child instanceof Context) {
                createMBeans((Context) child);
            } else if (child instanceof Engine) {
                createMBeans((Engine) child);
            } else if (child instanceof Host) {
                createMBeans((Host) child);
            }
        } catch (MBeanException t) {
            Exception e = t.getTargetException();
            if (e == null)
                e = t;
            log.log(Level.SEVERE, "processContainerAddChild: MBeanException",
                    e);
        } catch (Throwable t) {
            log.log(Level.SEVERE, "processContainerAddChild: Throwable", t);
        }

    }




    /**
     * Process a property change event on a Container.
     *
     * @param container The container on which this event occurred
     * @param propertyName The name of the property that changed
     * @param oldValue The previous value (may be <code>null</code>)
     * @param newValue The new value (may be <code>null</code>)
     *
     * @exception Exception if an exception is thrown
     */
    protected void processContainerPropertyChange(Container container,
                                                  String propertyName,
                                                  Object oldValue,
                                                  Object newValue)
        throws Exception {

        if (log.isLoggable(Level.FINEST)) {
            log.finest("propertyChange[container=" + container +
                ",propertyName=" + propertyName +
                ",oldValue=" + oldValue +
                ",newValue=" + newValue + "]");
        }
        if ("defaultContext".equals(propertyName)) {
            if (oldValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Removing MBean for DefaultContext " + oldValue);
                }
                destroyMBeans((DefaultContext) oldValue);
            }
            if (newValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Creating MBean for DefaultContext " + newValue);
                }
                createMBeans((DefaultContext) newValue);
            }
        } else if ("loader".equals(propertyName)) {
            if (oldValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Removing MBean for Loader " + oldValue);
                }
                MBeanUtils.destroyMBean((Loader) oldValue);
            }
            if (newValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Creating MBean for Loader " + newValue);
                }
                MBeanUtils.createMBean((Loader) newValue);
            }
        } else if ("logger".equals(propertyName)) {
            if (oldValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Removing MBean for Logger " + oldValue);
                }
               // MBeanUtils.destroyMBean((Logger) oldValue);
            }
            if (newValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Creating MBean for Logger " + newValue);
                }
                //MBeanUtils.createMBean((Logger) newValue);
            }
        } else if ("manager".equals(propertyName)) {
            if (oldValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Removing MBean for Manager " + oldValue);
                }
                //MBeanUtils.destroyMBean((Manager) oldValue);
            }
            if (newValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Creating MBean for Manager " + newValue);
                }
                //MBeanUtils.createMBean((Manager) newValue);
            }
        } else if ("realm".equals(propertyName)) {
            if (oldValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Removing MBean for Realm " + oldValue);
                }
                MBeanUtils.destroyMBean((Realm) oldValue);
            }
            if (newValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Creating MBean for Realm " + newValue);
                }
                //MBeanUtils.createMBean((Realm) newValue);
            }
        } else if ("service".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((Service) oldValue);
            }
            if (newValue != null) {
                createMBeans((Service) newValue);
            }
        }

    }


    /**
     * Process a property change event on a DefaultContext.
     *
     * @param defaultContext The DefaultContext on which this event occurred
     * @param propertyName The name of the property that changed
     * @param oldValue The previous value (may be <code>null</code>)
     * @param newValue The new value (may be <code>null</code>)
     *
     * @exception Exception if an exception is thrown
     */
    protected void processDefaultContextPropertyChange(DefaultContext defaultContext,
                                                  String propertyName,
                                                  Object oldValue,
                                                  Object newValue)
        throws Exception {

        if (log.isLoggable(Level.FINEST)) {
            log.finest("propertyChange[defaultContext=" + defaultContext +
                ",propertyName=" + propertyName +
                ",oldValue=" + oldValue +
                ",newValue=" + newValue + "]");
        }
        if ("loader".equals(propertyName)) {
            if (oldValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Removing MBean for Loader " + oldValue);
                }
                MBeanUtils.destroyMBean((Loader) oldValue);
            }
            if (newValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Creating MBean for Loader " + newValue);
                }
                MBeanUtils.createMBean((Loader) newValue);
            }
        } else if ("logger".equals(propertyName)) {
            if (oldValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Removing MBean for Logger " + oldValue);
                }
                //MBeanUtils.destroyMBean((Logger) oldValue);
            }
            if (newValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Creating MBean for Logger " + newValue);
                }
                //MBeanUtils.createMBean((Logger) newValue);
            }
        } else if ("manager".equals(propertyName)) {
            if (oldValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Removing MBean for Manager " + oldValue);
                }
                MBeanUtils.destroyMBean((Manager) oldValue);
            }
            if (newValue != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Creating MBean for Manager " + newValue);
                }
                MBeanUtils.createMBean((Manager) newValue);
            }
        } else if ("realm".equals(propertyName)) {
            if (oldValue != null) {
//                if (log.isLoggable(Level.FINE)) {
//                    log.fine("Removing MBean for Realm " + oldValue);
//                }
//                //MBeanUtils.destroyMBean((Realm) oldValue);
            }
            if (newValue != null) {
//                if (log.isLoggable(Level.FINE)) {
//                    log.fine("Creating MBean for Realm " + newValue);
//                }
//                //MBeanUtils.createMBean((Realm) newValue);
            }
        } else if ("service".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((Service) oldValue);
            }
            if (newValue != null) {
                createMBeans((Service) newValue);
            }
        }

    }


    /**
     * Process the removal of a child Container from a parent Container.
     *
     * @param parent Parent container
     * @param child Child container
     */
    protected void processContainerRemoveChild(Container parent,
                                               Container child) {

        if (log.isLoggable(Level.FINE))
            log.fine("Process removeChild[parent=" + parent + ",child=" +
                     child + "]");

        try {
            if (child instanceof Context) {
                Context context = (Context) child;
                if (context.getPrivileged()) {
                    context.getServletContext().removeAttribute
                        (Globals.MBEAN_REGISTRY_ATTR);
                    context.getServletContext().removeAttribute
                        (Globals.MBEAN_SERVER_ATTR);
                }
                if (log.isLoggable(Level.FINE))
                    log.fine("  Removing MBean for Context " + context);
                destroyMBeans(context);
                if (context instanceof StandardContext) {
                    ((StandardContext) context).
                        removePropertyChangeListener(this);
                }
            } else if (child instanceof Host) {
                Host host = (Host) child;
                destroyMBeans(host);
                if (host instanceof StandardHost) {
                    ((StandardHost) host).
                        removePropertyChangeListener(this);
                }
            }
        } catch (MBeanException t) {
            Exception e = t.getTargetException();
            if (e == null)
                e = t;
            log.log(Level.SEVERE, "processContainerRemoveChild: MBeanException", e);
        } catch (Throwable t) {
            log.log(Level.SEVERE, "processContainerRemoveChild: Throwable", t);
        }

    }


    /**
     * Process a property change event on a NamingResources.
     *
     * @param resources The global naming resources on which this
     *  event occurred
     * @param propertyName The name of the property that changed
     * @param oldValue The previous value (may be <code>null</code>)
     * @param newValue The new value (may be <code>null</code>)
     *
     * @exception Exception if an exception is thrown
     */
    protected void processNamingResourcesPropertyChange
        (NamingResources resources, String propertyName,
         Object oldValue, Object newValue)
        throws Exception {

        if (log.isLoggable(Level.FINEST)) {
            log.finest("propertyChange[namingResources=" + resources +
                ",propertyName=" + propertyName +
                ",oldValue=" + oldValue +
                ",newValue=" + newValue + "]");
        }

        // FIXME - Add other resource types when supported by admin tool
        if ("environment".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((ContextEnvironment) oldValue);
            }
            if (newValue != null) {
                createMBeans((ContextEnvironment) newValue);
            }
        } else if ("resource".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((ContextResource) oldValue);
            }
            if (newValue != null) {
                createMBeans((ContextResource) newValue);
            }
        } else if ("resourceLink".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((ContextResourceLink) oldValue);
            }
            if (newValue != null) {
                createMBeans((ContextResourceLink) newValue);
            }
        }

    }


    /**
     * Process a property change event on a Server.
     *
     * @param server The server on which this event occurred
     * @param propertyName The name of the property that changed
     * @param oldValue The previous value (may be <code>null</code>)
     * @param newValue The new value (may be <code>null</code>)
     *
     * @exception Exception if an exception is thrown
     */
    protected void processServerPropertyChange(Server server,
                                               String propertyName,
                                               Object oldValue,
                                               Object newValue)
        throws Exception {

        if (log.isLoggable(Level.FINEST)) {
            log.finest("propertyChange[server=" + server +
                ",propertyName=" + propertyName +
                ",oldValue=" + oldValue +
                ",newValue=" + newValue + "]");
        }
        if ("globalNamingResources".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((NamingResources) oldValue);
            }
            if (newValue != null) {
                createMBeans((NamingResources) newValue);
            }
        } else if ("service".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((Service) oldValue);
            }
            if (newValue != null) {
                createMBeans((Service) newValue);
            }
        }

    }


    /**
     * Process a property change event on a Service.
     *
     * @param service The service on which this event occurred
     * @param propertyName The name of the property that changed
     * @param oldValue The previous value (may be <code>null</code>)
     * @param newValue The new value (may be <code>null</code>)
     *
     * @exception Exception if an exception is thrown
     */
    protected void processServicePropertyChange(Service service,
                                                String propertyName,
                                                Object oldValue,
                                                Object newValue)
        throws Exception {

        if (log.isLoggable(Level.FINEST)) {
            log.finest("propertyChange[service=" + service +
                ",propertyName=" + propertyName +
                ",oldValue=" + oldValue +
                ",newValue=" + newValue + "]");
        }
        if ("connector".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((Connector) oldValue, service);
            }
            if (newValue != null) {
                createMBeans((Connector) newValue);
            }
        } else if ("container".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((Engine) oldValue);
            }
            if (newValue != null) {
                createMBeans((Engine) newValue);
            }
        }

    }


}
