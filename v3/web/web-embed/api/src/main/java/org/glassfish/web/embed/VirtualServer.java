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
import org.apache.catalina.Valve;

/**
 * Representation of a virtual server.
 *
 * <p>Instances of <tt>VirtualServer</tt> may be in one of two states:
 * <i>stopped</i> or <i>started</i>. Any requests mapped to a 
 * <tt>VirtualServer</tt> that was stopped will result in a response with
 * a status code equal to
 * javax.servlet.http.HttpServletResponse#SC_NOT_FOUND.
 * 
 * @author Rajiv Mordani
 * @author Jan Luehe
 */
public interface VirtualServer extends Lifecycle {

    /**
     * Gets the id of this <tt>VirtualServer</tt>.
     * 
     * @return the id of this <tt>VirtualServer</tt>
     */
    public String getId();
    
    /**
     * Gets the docroot of this <tt>VirtualServer</tt>.
     * 
     * @return the docroot of this <tt>VirtualServer</tt>
     */
    public File getDocRoot();

    /**
     * Gets the collection of <tt>WebListener</tt> instances from which
     * this <tt>VirtualServer</tt> receives requests.
     * 
     * @return the collection of <tt>WebListener</tt> instances from which
     * this <tt>VirtualServer</tt> receives requests.
     */
    public Collection<WebListener> getWebListeners();

    /**
     * Adds the given <tt>Valve</tt> to this <tt>VirtualServer</tt>
     * 
     * @param t the <tt>Valve</tt> to be added
     */
    public <T extends Valve> void addValve(T t);

    /**
     * Registers the given <tt>Context</tt> with this <tt>VirtualServer</tt>
     * at the given context root.
     *
     * <p>If this <tt>VirtualServer</tt> has been started, the given
     * <tt>Context</tt> will also be started (unless it was already
     * started).
     *
     * @param context the <tt>Context</tt> to register
     * @param contextRoot the context root at which to register
     *
     * @throws Exception if a <tt>Context</tt> already exists at the given
     * context root on this <tt>VirtualServer</tt>
     */
    public void addContext(Context context, String contextRoot)
        throws Exception;

    /**
     * Unregisters the <tt>Context</tt> at the given context from 
     * this <tt>VirtualServer</tt>.
     *
     * @param context the context root from which to unregister
     *
     */
    public void removeContext(Context context);

    /**
     * Finds the <tt>Context</tt> registered at the given context root.
     *
     * @param contextRoot the context root whose <tt>Context</tt> to get
     *
     * @return the <tt>Context</tt> registered at the given context root,
     * or <tt>null</tt> if no <tt>Context</tt> exists at the given context
     * root
     */
    public Context findContext(String contextRoot);

    /**
     * Gets the collection of <tt>Context</tt> instances registered with
     * this <tt>VirtualServer</tt>.
     * 
     * @return the collection of <tt>Context</tt> instances registered with
     * this <tt>VirtualServer</tt>
     */
    public Collection<Context> getContexts();

}
