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

package org.glassfish.web.embed;

import java.io.File;
import java.util.Collection;
import org.jvnet.hk2.annotations.Contract;

/**
 * Entry point to the web container, which allows users to create
 * HTTP listeners and virtual servers, and register web applications
 * and their static and dynamic resources into the URI namespace.
 */

@Contract
public interface EmbeddedWebContainer {

    /**
     * Creates a <tt>Context</tt>.
     *
     * <p>The classloader of the class on which this method is called
     * will be set as the thread's context classloader whenever the
     * new <tt>Context</tt> or any of its resources are asked to process
     * a request.
     *
     * <p>In order to access the new <tt>Context</tt> or any of its 
     * resources, the <tt>Context</tt> must be registered with a
     * <tt>VirtualServer</tt>.
     *
     * @return the new <tt>Context</tt>
     *
     * @see VirtualServer#addContext
     */
    public Context createContext();

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
     * <tt>VirtualServer</tt>.
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
     * @param c the class type of the new <tt>WebListener</tt>
     * 
     * @return the new <tt>WebListener</tt>
     *
     * @throws Exception if a <tt>WebListener</tt> with the given id
     * already exists in this web container
     */
    public <T extends WebListener> T createWebListener(String id, Class<T> c)
        throws Exception;

    /**
     * Finds the <tt>WebListener</tt> with the given id.
     *
     * @param id the id of the <tt>WebListener</tt> to find
     *
     * @return the <tt>WebListener</tt> with the given id, or
     * <tt>null</tt> if no <tt>WebListener</tt> with that id exists
     * in this web container
     */
    public WebListener findWebListener(String id);

    /**
     * Gets the collection of <tt>WebListener</tt> instances registered
     * with this web container.
     * 
     * @return the collection of <tt>WebListener</tt> instances registered
     * with this web container
     */
    public Collection<WebListener> getWebListeners();

    /**
     * Creates a <tt>VirtualServer</tt> with the given id and docroot.
     *
     * <p>The new <tt>VirtualServer</tt> will receive requests from the
     * given <tt>WebListener</tt> instances.
     * 
     * @param id the id of the <tt>VirtualServer</tt>
     * @param docRoot the docroot of the <tt>VirtualServer</tt>
     * @param webListeners the list of <tt>WebListener</tt> instances from 
     * which the <tt>VirtualServer</tt> will receive requests
     * 
     * @return the new <tt>VirtualServer</tt>
     *
     * @throws Exception if a <tt>VirtualServer</tt> with the given id
     * already exists in this web container
     */
    public VirtualServer createVirtualServer(String id,
        File docRoot, WebListener...  webListeners) throws Exception;

    /**
     * Finds the <tt>VirtualServer</tt> with the given id.
     *
     * @param id the id of the <tt>VirtualServer</tt> to find
     *
     * @return the <tt>VirtualServer</tt> with the given id, or
     * <tt>null</tt> if no <tt>VirtualServer</tt> with that id exists
     * in this web container
     */
    public VirtualServer findVirtualServer(String id);

    /**
     * Gets the collection of <tt>VirtualServer</tt> instances registered
     * with this web container.
     * 
     * @return the collection of <tt>VirtualServer</tt> instances registered
     * with this web container
     */
    public Collection<VirtualServer> getVirtualServers();

}
