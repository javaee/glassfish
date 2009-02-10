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
 */

package org.glassfish.web.embed;

import java.io.File;
import org.jvnet.hk2.annotations.Contract;

/**
 * Entry point to the web container, which allows users to create
 * HTTP listeners and virtual servers, and register web applications
 * and their static and dynamic resources into the URI namespace.
 */

@Contract
public interface EmbeddedWebContainer {

    /**
     * Create a context with the specified context root
     *
     * @param root The context root for the app
     * @param classLoader The classloader to be used for the context. If null is passed then the caller's
     * class loader will be used (effectively this.getClass.getClassLoader());
     * @return Context an instance of Context that represents the context of the app or
     * null if there is already a context registered at the root
     * specified
     */
    public Context createContext(String root, ClassLoader classLoader);

    /**
     * Create a context with the specified context root and associate
     * it with a VirtualServer
     * @param root The context root for the app
     * @param vServer The virtual server with which the context is associated
     * @param classLoader The classloader to be used for the context. If null is passed then the caller's
     * class loader will be used (effectively this.getClass.getClassLoader());
     * @return Context an instance of
     * Context that represents the context of the app
     * fro the specified virtual server or null if there is already a 
     * context registered at the root specified.
     */
    public Context createContext(String root, VirtualServer vServer,
        ClassLoader classLoader);

    /**
     * Creates a WebListener with the specified Listener type. The 
     * supported ones are <code>HttpListener</code> and
     * <code>HttpsListener</code>
     *
     * @param c The type of listener to create.
     * 
     * @return An instance of WebListener.
     */
    public <T extends WebListener> T createWebListener(Class<T> c);

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
     * @return the new <tt>VirtualServer</tt>, or <tt>null</tt> if a 
     * <tt>VirtualServer</tt> with the given id already exists in this
     * web container
     */
    public VirtualServer createVirtualServer(String id,
        File docRoot, WebListener...  webListeners);
}
