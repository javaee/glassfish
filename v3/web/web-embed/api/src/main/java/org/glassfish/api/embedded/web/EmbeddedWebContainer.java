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

package org.glassfish.api.embedded.web;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;
import org.glassfish.api.embedded.web.config.*;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.embedded.EmbeddedContainer;
import org.jvnet.hk2.annotations.Contract;

/**
 * Class representing an embedded web container, which supports the
 * programmatic creation of different types of web protocol listeners
 * and virtual servers, and the registration of static and dynamic
 * web resources into the URI namespace.
 */
@Contract
public interface EmbeddedWebContainer extends EmbeddedContainer {

    /**
     * Sets the embedded configuration for this embedded instance.
     * Such configuration should always override any xml based
     * configuration.
     *
     * @param builder the embedded instance configuration
     */
    public void setConfiguration(WebBuilder builder);

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
    public void start() throws LifecycleException;

    /**
     * Stops this <tt>EmbeddedWebContainer</tt> and any of the
     * <tt>WebListener</tt> and <tt>VirtualServer</tt> instances
     * registered with it.
     *
     * @throws Exception if an error occurs during the shut down of this
     * <tt>EmbeddedWebContainer</tt> or any of its registered
     * <tt>WebListener</tt> or <tt>VirtualServer</tt> instances 
     */
    public void stop() throws LifecycleException;

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
    public Context createContext(File docRoot, String contextRoot, 
                                 ClassLoader classLoader);

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
    public Context createContext(File docRoot, ClassLoader classLoader);

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
    public <T extends WebListener> T createWebListener(String id, Class<T> c)
        throws InstantiationException, IllegalAccessException;

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
    public void addWebListener(WebListener webListener)
        throws ConfigException, LifecycleException;

    /**
     * Finds the <tt>WebListener</tt> with the given id.
     *
     * @param id the id of the <tt>WebListener</tt> to find
     *
     * @return the <tt>WebListener</tt> with the given id, or
     * <tt>null</tt> if no <tt>WebListener</tt> with that id has been
     * registered with this <tt>EmbeddedWebContainer</tt>
     */
    public WebListener findWebListener(String id);

    /**
     * Gets the collection of <tt>WebListener</tt> instances registered
     * with this <tt>EmbeddedWebContainer</tt>.
     * 
     * @return the (possibly empty) collection of <tt>WebListener</tt>
     * instances registered with this <tt>EmbeddedWebContainer</tt>
     */
    public Collection<WebListener> getWebListeners();

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
    public void removeWebListener(WebListener webListener)
        throws LifecycleException;

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
    public VirtualServer createVirtualServer(String id,
        File docRoot, WebListener...  webListeners);
    
    /**
     * Creates a <tt>VirtualServer</tt> with the given id and docroot, and
     * maps it to all <tt>WebListener</tt> instances.
     * 
     * @param id the id of the <tt>VirtualServer</tt>
     * @param docRoot the docroot of the <tt>VirtualServer</tt>
     * 
     * @return the new <tt>VirtualServer</tt> instance
     */    
    public VirtualServer createVirtualServer(String id, File docRoot);
    
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
     * @throws org.glassfish.api.embedded.LifecycleException if the given <tt>virtualServer</tt> fails
     * to be started
     */
    public void addVirtualServer(VirtualServer virtualServer)
        throws ConfigException, LifecycleException;

    /**
     * Finds the <tt>VirtualServer</tt> with the given id.
     *
     * @param id the id of the <tt>VirtualServer</tt> to find
     *
     * @return the <tt>VirtualServer</tt> with the given id, or
     * <tt>null</tt> if no <tt>VirtualServer</tt> with that id has been
     * registered with this <tt>EmbeddedWebContainer</tt>
     */
    public VirtualServer findVirtualServer(String id);

    /**
     * Gets the collection of <tt>VirtualServer</tt> instances registered
     * with this <tt>EmbeddedWebContainer</tt>.
     * 
     * @return the (possibly empty) collection of <tt>VirtualServer</tt>
     * instances registered with this <tt>EmbeddedWebContainer</tt>
     */
    public Collection<VirtualServer> getVirtualServers();

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
    public void removeVirtualServer(VirtualServer virtualServer)
        throws LifecycleException;
    
    /**
     * Sets log level
     * 
     * @param level
     */
    public void setLogLevel(Level level);

}
