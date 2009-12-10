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
package org.glassfish.api.naming;

import org.jvnet.hk2.annotations.Contract;

import javax.naming.Context;

import javax.naming.Name;
import javax.naming.NamingException;
import java.util.Collection;
import org.omg.CORBA.ORB;

import java.rmi.Remote;

/**
 * The NamingManager provides an interface for various components to use naming
 * functionality. It provides methods for binding and unbinding environment
 * properties, resource and ejb references.
 */

@Contract
public interface GlassfishNamingManager {

    /**
     * Get the initial context.
     */

    public Context getInitialContext();

    /**
     *
     * Lookup a naming entry for a particular componentId
     */
    public Object lookup(String componentId, String name) throws NamingException;

    /**
     * Publish an object in the naming service.
     *
     * @param name   Object that needs to be bound.
     * @param obj    Name that the object is bound as.
     * @param rebind operation is a bind or a rebind.
     * @throws Exception
     */

    public void publishObject(String name, Object obj, boolean rebind)
            throws NamingException;

    /**
     * Publish an object in the naming service.
     *
     * @param name   Object that needs to be bound.
     * @param obj    Name that the object is bound as.
     * @param rebind operation is a bind or a rebind.
     * @throws Exception
     */

    public void publishObject(Name name, Object obj, boolean rebind)
            throws NamingException;

    /**
     * Publish a CosNaming object.  The object is published to both
     * the server's CosNaming service and the global naming service.
     * Objects published with this method must be unpublished via
     * unpublishCosNamingObject.
     *
     * @param name   Object that needs to be bound.
     * @param obj    Name that the object is bound as.
     * @param rebind operation is a bind or a rebind.
     * @throws Exception
     */

    public void publishCosNamingObject(String name, Object obj, boolean rebind)
            throws NamingException;

    /**
     * This method enumerates the env properties, ejb and resource references
     * etc for a J2EE component and binds them in the applicable java:
     * namespace.
     *
     * @param treatComponentAsModule true if java:comp and java:module refer to the same
     *         namespace
     *
     */
    public void bindToComponentNamespace(String appName, String moduleName,
                                         String componentId, boolean treatComponentAsModule, 
                                         Collection<? extends JNDIBinding> bindings)
            throws NamingException;


    public void bindToAppNamespace(String appName, Collection<? extends JNDIBinding> bindings)
            throws NamingException;

    /**
     * Remove an object from the naming service.
     *
     * @param name Name that the object is bound as.
     * @throws Exception
     */
    public void unpublishObject(String name) throws NamingException;

    /**
     * Remove an object from the CosNaming service and global naming service.
     *
     * @param name Name that the object is bound as.
     * @throws Exception
     */
    public void unpublishCosNamingObject(String name) throws NamingException;



    /**
     * Remove an object from the naming service.
     *
     * @param name Name that the object is bound as.
     * @throws Exception
     */
    public void unpublishObject(Name name) throws NamingException;


    /**
     *
     * Unbind component-level bindings
     */
    public void unbindComponentObjects(String componentId) throws NamingException;


    /**
     * Unbind app and module level bindings for the given app name.
     */
    public void unbindAppObjects(String appName) throws NamingException;

    /**
     * Recreate a context for java:comp/env or one of its sub-contexts given the
     * context name.
     */
    public Context restoreJavaCompEnvContext(String contextName)
            throws NamingException;

    /**
     * Initialize RMI-IIOP naming services 
     * @param orb
     * @return RemoteSerialProvider object instance
     */
    public Remote initializeRemoteNamingSupport(ORB orb) throws NamingException;

}