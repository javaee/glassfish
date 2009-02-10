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

import org.jvnet.hk2.annotations.Contract;

import java.io.File;

/**
 * This is the entry point for the Web container. This interface allows
 * a user to create context, add context, create http listeners and
 * virtual servers.
 */

@Contract
public interface EmbeddedWebContainer {

    // Entry point for the EmbeddedWebContainer in the embedded space

    /**
     * Create a context with the specified context root
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
    public Context createContext(String root, VirtualServer vServer, ClassLoader classLoader);

    // This allows retrieval of an Context from an
    // Application created using Server.deploy()
    /**
     * This method returns an instance of Context for
     * the specified context root.

     * @param contextRoot the context root for which an instance of the
     * Context is returned
     *
     * @return An instance of Context.
     */
    public Context getContext(String contextRoot);



    /**
     * Creates a WebListener with the specified Listener type. The supported ones are HttpListener and
     * HttpsListener
     *
     * @param c The type of listener to create.
     * 
     * @return An instance of WebListener.
     */
//    public HttpListener createHttpListener(String id, int port, boolean secure);

    public <T extends WebListener> T createListener(Class<T> c);

    /**
     * Creates a VirtualServer for the with the specified id, docroot
     * associated with the httpListenerIds
     *
     * @param virtualServerId the id for the virtual server being
     * created
     * @param docRoot the docroot for the virtual server
     * @param webListeners The list of web listeners that the
     * instance of virtual server is associated with
     * 
     * @return An instance of VirtualServer
     *
     */

    public VirtualServer createVirtualServer(String virtualServerId,
    				File docRoot, WebListener...  webListeners);
}
