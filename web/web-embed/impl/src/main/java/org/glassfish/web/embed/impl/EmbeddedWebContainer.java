/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 */

package org.glassfish.web.embed.impl;

import java.io.File;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.*;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.embedded.*;
import org.glassfish.api.embedded.web.ConfigException;
import org.glassfish.api.embedded.web.WebBuilder;
import org.glassfish.api.embedded.web.config.*;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.*;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Embedded;
import org.apache.catalina.Connector;
import org.omg.CORBA.DynAnyPackage.*;

/**
 * Class representing an embedded web container, which supports the
 * programmatic creation of different types of web protocol listeners
 * and virtual servers, and the registration of static and dynamic
 * web resources into the URI namespace.
 *  
 * @author Amy Roh
 */
@Service
public class EmbeddedWebContainer implements 
        org.glassfish.api.embedded.web.EmbeddedWebContainer {

    @Inject
    Habitat habitat;
    
    private static Logger log = 
            Logger.getLogger(EmbeddedWebContainer.class.getName());
      
    
    // ----------------------------------------------------------- Constructors
      
    
    public EmbeddedWebContainer() {
        //embedded = new Embedded();
        //embedded.setUseNaming(false);
        //engine = embedded.createEngine();
        //embedded.addEngine(engine);
        
    }
    

    // ----------------------------------------------------- Instance Variables

    Inhabitant<? extends org.glassfish.api.container.Container> webContainer;

    private VirtualServer defaultVirtualServer = null;
    
    private Embedded embedded = null;
    
    private Engine engine = null;
    
    private File path = null;
    
    private String defaultDomain = "com.sun.appserv";
    

    // --------------------------------------------------------- Public Methods

    public void setConfiguration(WebBuilder builder) {
        setPath(builder.getDocRootDir());
    }

    /**
     * Returns the list of sniffers associated with this embedded container
     * @return list of sniffers
     */
    public List<Sniffer> getSniffers() {
        List<Sniffer> sniffers = new ArrayList<Sniffer>();
        sniffers.add(habitat.getComponent(Sniffer.class, "web"));
        sniffers.add(habitat.getComponent(Sniffer.class, "weld"));        
        Sniffer security = habitat.getComponent(Sniffer.class, "Security");
        if (security!=null) {
            sniffers.add(security);
        }
        return sniffers;
    }

    public void bind(Port port, String protocol) {

    }

    /**
     * Starts this <tt>EmbeddedWebContainer</tt> and any of the
     * <tt>WebListener</tt> and <tt>VirtualServer</tt> instances
     * registered with it.
     *
     * <p>This method also creates and starts a default
     * <tt>VirtualServer</tt> with id <tt>server</tt> and hostname
     * <tt>localhost</tt>, as well as a default <tt>WebListener</tt>
     * with id <tt>http-listener-1</tt> on port 8080 if no other virtual server 
     * or listener configuration exists.
     * In order to change any of these default settings, 
     * {@link #start(WebContainerConfig)} may be called.
     * 
     * @throws Exception if an error occurs during the start up of this
     * <tt>EmbeddedWebContainer</tt> or any of its registered
     * <tt>WebListener</tt> or <tt>VirtualServer</tt> instances 
     */
    public void start() throws LifecycleException {
   
        /*log.info("EmbeddedWebContainer is starting");
        int port = 8080;
        String webListenerId = "http-listener-1";
        String virtualServerId = "server";
        String hostName = "localhost";
    
        try { 
            if (createDefaultConfig()) {
                engine.setName(defaultDomain);
                ((StandardEngine)engine).setDomain(defaultDomain);
                engine.setDefaultHost(virtualServerId);
                engine.setParentClassLoader(EmbeddedWebContainer.class.getClassLoader());
            
                WebListener webListener = 
                    createWebListener(webListenerId, WebListener.class);
                webListener.setPort(port);
                webListener.setDefaultHost(virtualServerId);
                webListener.setDomain(defaultDomain);
                WebListener[] webListeners = new WebListener[1];
                webListeners[0] = webListener;
            
                File docRoot = getPath();
                defaultVirtualServer = (VirtualServer)
                        createVirtualServer(virtualServerId, docRoot, webListeners);
                defaultVirtualServer.addAlias(hostName);
                engine.addChild(defaultVirtualServer);
            
                Context context = (Context) createContext(docRoot, null);
                defaultVirtualServer.addChild(context);
                                
                embedded.addEngine(engine);
      
                //addWebListener(webListener);
            }
            
            embedded.start();
            
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new LifecycleException(e);
        }
        */

        webContainer = habitat.getInhabitant(org.glassfish.api.container.Container.class,
                "com.sun.enterprise.web.WebContainer");
        if (webContainer==null) {
            log.severe("Cannot find webcontainer implementation");
            throw new LifecycleException(new Exception("Cannot find web container implementation"));
        }
        // force the start
        try {
            webContainer.get();
        } catch (Exception e) {
            throw new LifecycleException(e);
        }

    }

    /**
     * Stops this <tt>EmbeddedWebContainer</tt> and any of the
     * <tt>WebListener</tt> and <tt>VirtualServer</tt> instances
     * registered with it.
     *
     * @throws Exception if an error occurs during the shut down of this
     * <tt>EmbeddedWebContainer</tt> or any of its registered
     * <tt>WebListener</tt> or <tt>VirtualServer</tt> instances 
     */
    public void stop() throws LifecycleException {

       if (webContainer!=null && webContainer.isInstantiated()) {
           try {
               webContainer.release();
           } catch (Exception e) {
               throw new LifecycleException(e);
           }
       }
/*        try {
            embedded.stop();
        } catch (org.apache.catalina.LifecycleException e) {
            throw new LifecycleException(e);
        }
*/
    }
   
    /**
     * Creates a <tt>Context</tt>, configures it with the given
     * docroot and classloader, and registers it with the default
     * <tt>VirtualServer</tt>.
     *
     * <p>The given classloader will be set as the thread's context
     * classloader whenever the new <tt>Context</tt> or any of its
     * resources are asked to process a request.
     * If a <tt>null</tt> classloader is passed, the classloader of the
     * class on which this method is called will be used.
     *
     * @param docRoot the docroot of the <tt>Context</tt>
     * @param contextRoot
     * @param classLoader the classloader of the <tt>Context</tt>
     *
     * @return the new <tt>Context</tt>
     */
    public org.glassfish.api.embedded.web.Context createContext(File docRoot, 
                                String contextRoot, ClassLoader classLoader) {
        
        log.info("Creating context '" + contextRoot + "' with docBase '" +
                     docRoot.getPath() + "'");

        Context context = new Context();
        context.setDocBase(docRoot.getPath());
        context.setPath(contextRoot);
        if (classLoader != null) {
            context.setParentClassLoader(classLoader);
        } else {
            context.setParentClassLoader(
                    Thread.currentThread().getContextClassLoader());
        }        
                
        ContextConfig config = new ContextConfig();
        ((Lifecycle) context).addLifecycleListener(config);
        
        defaultVirtualServer.addChild((Container)context);
        
        return (org.glassfish.api.embedded.web.Context) context;
        
    }

    /**
     * Creates a <tt>Context</tt> and configures it with the given
     * docroot and classloader.
     *
     * <p>The given classloader will be set as the thread's context
     * classloader whenever the new <tt>Context</tt> or any of its
     * resources are asked to process a request.
     * If a <tt>null</tt> classloader is passed, the classloader of the
     * class on which this method is called will be used.
     *
     * <p>In order to access the new <tt>Context</tt> or any of its 
     * resources, the <tt>Context</tt> must be registered with a
     * <tt>VirtualServer</tt> that has been started.
     *
     * @param docRoot the docroot of the <tt>Context</tt>
     * @param classLoader the classloader of the <tt>Context</tt>
     *
     * @return the new <tt>Context</tt>
     *
     * @see VirtualServer#addContext
     */
    public org.glassfish.api.embedded.web.Context createContext(File docRoot, 
            ClassLoader classLoader) {
        
        String contextRoot = "";
        log.info("Creating context '" + contextRoot + "' with docBase '" +
                docRoot.getPath() + "'");

        Context context = new Context();
        context.setDocBase(docRoot.getPath());
        context.setPath(contextRoot);
        if (classLoader != null) {
            context.setParentClassLoader(classLoader);
        } else {
            context.setParentClassLoader(
                    Thread.currentThread().getContextClassLoader());
        }       
        
        ContextConfig config = new ContextConfig();
        ((Lifecycle) context).addLifecycleListener(config);
        
        return (org.glassfish.api.embedded.web.Context) context;
        
    }

    /**
     * Creates a <tt>WebListener</tt> from the given class type and
     * assigns the given id to it.
     *
     * @param id the id of the new <tt>WebListener</tt>
     * @param c the class from which to instantiate the
     * <tt>WebListener</tt>
     * 
     * @return the new <tt>WebListener</tt> instance
     *
     * @throws  IllegalAccessException if the given <tt>Class</tt> or
     * its nullary constructor is not accessible.
     * @throws  InstantiationException if the given <tt>Class</tt>
     * represents an abstract class, an interface, an array class,
     * a primitive type, or void; or if the class has no nullary
     * constructor; or if the instantiation fails for some other reason.
     * @throws ExceptionInInitializerError if the initialization
     * fails
     * @throws SecurityException if a security manager, <i>s</i>, is
     * present and any of the following conditions is met:
     *
     * <ul>
     * <li> invocation of <tt>{@link SecurityManager#checkMemberAccess
     * s.checkMemberAccess(this, Member.PUBLIC)}</tt> denies
     * creation of new instances of the given <tt>Class</tt>
     * <li> the caller's class loader is not the same as or an
     * ancestor of the class loader for the current class and
     * invocation of <tt>{@link SecurityManager#checkPackageAccess
     * s.checkPackageAccess()}</tt> denies access to the package
     * of this class
     * </ul>
     */
    public <T extends org.glassfish.api.embedded.web.WebListener> T 
            createWebListener(String id, Class<T> c) 
            throws InstantiationException, IllegalAccessException {
        
        T webListener = null;
        log.info("Creating connector "+id);
        
        try {
            webListener = c.newInstance();
            webListener.setId(id);
        } catch (Exception e) {
            log.severe("Couldn't create connector");
        } 
        
        return webListener;
        
    }

    /**
     * Adds the given <tt>WebListener</tt> to this
     * <tt>EmbeddedWebContainer</tt>.
     *
     * <p>If this <tt>EmbeddedWebContainer</tt> has already been started,
     * the given <tt>webListener</tt> will be started as well.
     *
     * @param webListener the <tt>WebListener</tt> to add
     *
     * @throws ConfigException if a <tt>WebListener</tt> with the
     * same id has already been registered with this
     * <tt>EmbeddedWebContainer</tt>
     * @throws LifecycleException if the given <tt>webListener</tt> fails
     * to be started
     */
    public void addWebListener(org.glassfish.api.embedded.web.WebListener webListener)
        throws ConfigException, LifecycleException {
        
        if (findWebListener(webListener.getId())==null) {
            embedded.addConnector((Connector)webListener);            
        } else {
            throw new ConfigException("Connector with name '"+
                    webListener.getId()+"' already exsits");           
        }
        log.info("Added connector "+webListener.getId());
        
    }

    /**
     * Finds the <tt>WebListener</tt> with the given id.
     *
     * @param id the id of the <tt>WebListener</tt> to find
     *
     * @return the <tt>WebListener</tt> with the given id, or
     * <tt>null</tt> if no <tt>WebListener</tt> with that id has been
     * registered with this <tt>EmbeddedWebContainer</tt>
     */
    public org.glassfish.api.embedded.web.WebListener findWebListener(String id) {
        
        WebListener webListener = null;
        for (Connector connector : embedded.findConnectors()) {
            if (connector.getName().equals(id)) {
                webListener = (WebListener) connector;
            }
        }
        
        return (org.glassfish.api.embedded.web.WebListener) webListener;
        
    }

    /**
     * Gets the collection of <tt>WebListener</tt> instances registered
     * with this <tt>EmbeddedWebContainer</tt>.
     * 
     * @return the (possibly empty) collection of <tt>WebListener</tt>
     * instances registered with this <tt>EmbeddedWebContainer</tt>
     */
    public Collection<org.glassfish.api.embedded.web.WebListener> getWebListeners() {
        
        org.glassfish.api.embedded.web.WebListener[] connectors = 
                (org.glassfish.api.embedded.web.WebListener[]) embedded.findConnectors();
        
        return Arrays.asList(connectors);
        
    }

    /**
     * Stops the given <tt>webListener</tt> and removes it from this
     * <tt>EmbeddedWebContainer</tt>.
     *
     * @param webListener the <tt>WebListener</tt> to be stopped
     * and removed
     *
     * @throws LifecycleException if an error occurs during the stopping
     * or removal of the given <tt>webListener</tt>
     */
    public void removeWebListener(org.glassfish.api.embedded.web.WebListener webListener)
        throws LifecycleException {
        
        try {
            embedded.removeConnector((org.apache.catalina.Connector)webListener);
        } catch (org.apache.catalina.LifecycleException e) {
            throw new LifecycleException(e);
        }
        
    }

    /**
     * Creates a <tt>VirtualServer</tt> with the given id and docroot, and
     * maps it to the given <tt>WebListener</tt> instances.
     * 
     * @param id the id of the <tt>VirtualServer</tt>
     * @param docRoot the docroot of the <tt>VirtualServer</tt>
     * @param webListeners the list of <tt>WebListener</tt> instances from 
     * which the <tt>VirtualServer</tt> will receive requests
     * 
     * @return the new <tt>VirtualServer</tt> instance
     */
    public org.glassfish.api.embedded.web.VirtualServer createVirtualServer(String id,
        File docRoot, org.glassfish.api.embedded.web.WebListener...  webListeners) {
        
        log.info("Created virtual server "+id+" with ports ");
        VirtualServer virtualServer = new VirtualServer();
        virtualServer.setName(id);
        if (docRoot!=null) {
            virtualServer.setAppBase(docRoot.getPath());
        } 
        int[] ports = new int[webListeners.length];
        for (int i=0; i<webListeners.length; i++) {
            ports[i] = webListeners[i].getPort();
            log.info(""+ports[i]);
        }
        virtualServer.setPorts(ports);
        
        return virtualServer;
        
    }
    
    /**
     * Creates a <tt>VirtualServer</tt> with the given id and docroot, and
     * maps it to all <tt>WebListener</tt> instances.
     * 
     * @param id the id of the <tt>VirtualServer</tt>
     * @param docRoot the docroot of the <tt>VirtualServer</tt>
     * 
     * @return the new <tt>VirtualServer</tt> instance
     */    
    public org.glassfish.api.embedded.web.VirtualServer createVirtualServer(String id, 
            File docRoot) {
        
        log.info("Created virtual server "+id+" with ports ");
        VirtualServer virtualServer = new VirtualServer();
        virtualServer.setName(id);
        if (docRoot!=null) {
            virtualServer.setAppBase(docRoot.getPath());
        }
        Connector[] connectors = embedded.findConnectors();
        int[] ports = new int[connectors.length];
        for (int i=0; i<connectors.length; i++) {
            ports[i] = ((org.apache.catalina.connector.Connector)connectors[i]).getPort();
            log.info(""+ports[i]);
        }
        virtualServer.setPorts(ports);
        
        return virtualServer;
        
    }

    /**
     * Adds the given <tt>VirtualServer</tt> to this
     * <tt>EmbeddedWebContainer</tt>.
     *
     * <p>If this <tt>EmbeddedWebContainer</tt> has already been started,
     * the given <tt>virtualServer</tt> will be started as well.
     *
     * @param virtualServer the <tt>VirtualServer</tt> to add
     *
     * @throws ConfigException if a <tt>VirtualServer</tt> with the
     * same id has already been registered with this
     * <tt>EmbeddedWebContainer</tt>
     * @throws LifecycleException if the given <tt>virtualServer</tt> fails
     * to be started
     */
    public void addVirtualServer(org.glassfish.api.embedded.web.VirtualServer virtualServer)
        throws ConfigException, LifecycleException {
        
        Engine[] engines = embedded.getEngines();
        if (engines.length==0) {
            Engine engine = embedded.createEngine();
            engine.setName(defaultDomain);
            ((StandardEngine)engine).setDomain(defaultDomain);
            engine.setParentClassLoader(EmbeddedWebContainer.class.getClassLoader());
            embedded.addEngine(engine);
        }
        engines = embedded.getEngines();
        if (engines[0].findChild(virtualServer.getId())!=null) {
            throw new ConfigException("VirtualServer with id "+
                    virtualServer.getId()+" is already registered");
        } else {
            
            engines[0].setDefaultHost(virtualServer.getId());
            engines[0].addChild((Container)virtualServer);
        }
        log.info("Added virtual server "+virtualServer.getId());
        
    }

    /**
     * Finds the <tt>VirtualServer</tt> with the given id.
     *
     * @param id the id of the <tt>VirtualServer</tt> to find
     *
     * @return the <tt>VirtualServer</tt> with the given id, or
     * <tt>null</tt> if no <tt>VirtualServer</tt> with that id has been
     * registered with this <tt>EmbeddedWebContainer</tt>
     */
    public VirtualServer findVirtualServer(String id) {
        
        Engine[] engines = embedded.getEngines();
        
        return (VirtualServer)engines[0].findChild(id);
        
    }

    /**
     * Gets the collection of <tt>VirtualServer</tt> instances registered
     * with this <tt>EmbeddedWebContainer</tt>.
     * 
     * @return the (possibly empty) collection of <tt>VirtualServer</tt>
     * instances registered with this <tt>EmbeddedWebContainer</tt>
     */
    public Collection<org.glassfish.api.embedded.web.VirtualServer> getVirtualServers(){
                
        Engine[] engines = embedded.getEngines();
        org.glassfish.api.embedded.web.VirtualServer[] virtualServers = 
                (org.glassfish.api.embedded.web.VirtualServer[]) engines[0].findChildren();
        
        return Arrays.asList(virtualServers);
        
    }

    /**
     * Stops the given <tt>virtualServer</tt> and removes it from this
     * <tt>EmbeddedWebContainer</tt>.
     *
     * @param virtualServer the <tt>VirtualServer</tt> to be stopped
     * and removed
     *
     * @throws LifecycleException if an error occurs during the stopping
     * or removal of the given <tt>virtualServer</tt>
     */
    public void removeVirtualServer(org.glassfish.api.embedded.web.VirtualServer virtualServer)
        throws LifecycleException {
           
        Engine[] engines = embedded.getEngines();
        engines[0].removeChild((Container)virtualServer);
   
    }   
      
    
    /**
     * Sets the value of the context path
     * 
     * @param path - the path
     */
    public void setPath(File path) {
        this.path = path;
    }

  
    /**
     * Returning the value of the context path
     *
     * @return - the context path
     */
    public File getPath() {
        return path;
    }

    
    /**
     * Sets log level
     * 
     * @param level
     */
    public void setLogLevel(Level level) {
        log.setLevel(level);
    }
    
    public boolean createDefaultConfig() {
        
        Engine[] engines = embedded.getEngines();
        if (engines.length==0 || engines[0]==null) {
            return true;
        }      
        
        if (embedded.findConnectors().length>0 && 
                engines[0].findChildren().length>0) {
            return false;
        } else {
            return true;
        }
    }
   
    
}
