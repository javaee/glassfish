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

/*
 * @(#) ApplicationRegistry.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.server;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.Collection;

import com.sun.ejb.Container;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbDescriptor;

/**
 * Registry for all j2ee application and stand alone ejb modules 
 * in a server instance. 
 *
 * @author  Mahesh Kannan
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
public final class ApplicationRegistry {

    /** application id vs class loader */
    protected Hashtable appID2ClassLoader;

    /** stand alone module id vs class loader */
    protected Hashtable moduleID2ClassLoader;

    /** ClassLoader vs Application Objects */
    protected Hashtable classLoader2Application;

    /** EJBDescriptor vs Container */
    protected Hashtable descriptor2Container;

    /** holds unique ids of ejb containers for each deployed applications */ 
    private HashSet uniqueIds;

    /** the singleton instance */
    private static ApplicationRegistry _instance;

    static {
        _instance = new ApplicationRegistry();
    }

    /**
     * Returns the singleton instance.
     *
     * @return   the singleton instance
     */
    public static ApplicationRegistry getInstance() {
        return _instance;
    }

    /**
     * Initializes the registry.
     */
    private ApplicationRegistry() {
        this.appID2ClassLoader        = new Hashtable();
        this.moduleID2ClassLoader     = new Hashtable();
        this.descriptor2Container     = new Hashtable();
        this.classLoader2Application  = new Hashtable();
        this.uniqueIds                = new HashSet();
    }

    /******** OLD RI METHODS ********/

    /** 
     * Return the Container for the given EjbDescriptor.
     * This is called at runtime from the Persistence Manager.
     *
     * @param    desc    ejb deployment descriptor
     *
     * @return   the ejb container for the given descriptor obj
     */
    public Container getContainer(EjbDescriptor desc) {
        return (Container) descriptor2Container.get(desc);
    }

    /** 
     * Return the Application for the given classloader.
     * It is assumed that there is a 1-to-1 mapping
     * from app to loader. This is called at runtime from the 
     * Persistence Manager.
     *
     * @param    loader    class loader
     * 
     * @return   deployment descriptor hierarchy for the given class loader
     */
    public Application getApplication(ClassLoader loader) {
        return (Application) classLoader2Application.get(loader);
    }

    /**
     * Get the class loader of a given application.
     * Used in ServletDeployerImpl
     *
     * @param    appID    registration name of an application
     *
     * @return   the class loader for the given registration name
     */
    public ClassLoader getClassLoaderForApplication(String appID) {
        return (ClassLoader) appID2ClassLoader.get(appID);
    }

    /******** OLD RI METHODS ********/

    /**
     * Get the class loader of the given stand alone module.
     *
     * @param    moduleID    registration name of a stand alone module
     *
     * @return   the class loader for the given stand alone
     *           module registration name
     */
    public ClassLoader getClassLoaderForModule(String moduleID) {
        return (ClassLoader) moduleID2ClassLoader.get(moduleID);
    }

    /**
     * Stores the given deployment descriptor and ejb container in a table.
     * 
     * @param    desc       deployment descriptor of an ejb
     * @param    container  ejb container
     */
    void addDescriptor2Container(EjbDescriptor desc, 
            Container container) {

        descriptor2Container.put(desc, container);
    }

    /**
     * Stores class loader against the given registration name for application.
     * 
     * @param    appID          registration name of the application
     * @param    classLoader    class loader associated with this application
     */
    void addAppId2ClassLoader(String appID, ClassLoader classLoader)  {
        appID2ClassLoader.put(appID, classLoader);
    }

    /**
     * Stores class loader against the given registration name for stand alone
     * module. We are keeping separate table because of possible name 
     * space collision.
     * 
     * @param    moduleID       registration name of stand alone module
     * @param    classLoader    class loader associated with this module
     */
    void addModuleId2ClassLoader(String moduleID, ClassLoader classLoader)  {
        moduleID2ClassLoader.put(moduleID, classLoader);
    }

    /**
     * Stores the deployment descriptor hierarchy against the class loader.
     * 
     * @param    classLoader    class loader used in an app
     * @param    app            deployment descriptor hierarchy
     */
    void addClassLoader2Application(ClassLoader classLoader, 
            Application app) {

        classLoader2Application.put(classLoader, app);
    }

    /**
     * Removes the ejb container associated with the given deployment 
     * descriptor.
     *
     * @param    desc    ejb deployment descriptor
     * @return   the removed ejb container
     */
    Container removeDescriptor2Container(EjbDescriptor desc)  {
        return (Container) descriptor2Container.remove(desc);
    }

    /**
     * Removes the class loader associated with the given registration name.
     * 
     * @param    appID    registration name of an app
     * @return   the removed class loader
     */
    ClassLoader removeAppId2ClassLoader(String appID)  {
        return (ClassLoader) appID2ClassLoader.remove(appID);
    }

    /**
     * Removes the class loader associated with the given stand alone module 
     * registration name. We are keeping separate table because of possible
     * name space collision.
     * 
     * @param    moduleID    registration name of stand alone module
     * @return   the removed class loader
     */
    ClassLoader removeModuleId2ClassLoader(String moduleID)  {
        return (ClassLoader) moduleID2ClassLoader.remove(moduleID);
    }

    /**
     * Removes the deployment descriptor hierarchy associated with the given 
     * class loader.
     * 
     * @param    cl    class loader used in an app
     * @return   the removed deployment descriptor hierarchy
     */
    Application removeClassLoader2Application(ClassLoader cl) {
        return (Application) classLoader2Application.remove(cl);
    }

    /**
     * Returns true if the given id is not already present in this registry.
     * It adds the specified unique id to the registry if not already
     * present. This is used to detect collision between two ejb bean 
     * containers.
     *
     * @param    uniqueId    unique id of an ejb bean container
     *
     * @return   true if the given id is not already present in this registry
     */
    boolean isUnique(long uniqueId) {
        return this.uniqueIds.add( new Long(uniqueId) );
    }

    /**
     * Removes the given id from this registry.
     *
     * @param    uniqueId    unique if of an ejb bean container
     *
     * @return   true if the registry contained the specified id
     */
    boolean removeUniqueId(long uniqueId) {
        return this.uniqueIds.remove( new Long(uniqueId) );
    }

    /**
     * Returns all the ejb containers available in this registry.
     * This includes J2EE applications and stand alone ejb modules.
     *
     * @return   a collection of ejb containers
     */
    Collection getAllEjbContainers() {

        Collection containers = null;

        if (this.descriptor2Container != null) {
            containers =  this.descriptor2Container.values();
        }

        return containers;
    }
}
