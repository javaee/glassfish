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

package com.sun.appserv.ha.spi;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class for storing BackingStore implementation. This is a singleton and
 * contains a mapping between persistence-type and
 * <code>BackingStoreFactory</code>. An instance of this class can be
 * obtained using <code>getInstance()</code> method.
 * 
 * @see BackingStore
 */
public final class BackingStoreRegistry {

    private static BackingStoreRegistry _instance = new BackingStoreRegistry();

    private Map<String, BackingStoreInfo> type2BackingStoreInfo = new ConcurrentHashMap<String, BackingStoreInfo>();

    // Private constructor
    private BackingStoreRegistry() {
    }

    /**
     * Return the (singleton) instance of BackingStoreRegistry
     * 
     * @return BackingStoreRegistry
     */
    public static BackingStoreRegistry getInstance() {
        return _instance;
    }

    /**
     * Register a BackingStoreFactory class name and any associated Properties
     * for the given persistence type. The properties must contain any
     * additional configuration paramters to properly initialize and use the
     * store.
     * 
     * @param type
     *            The persistence-type
     * @param factoryClassName
     *            The factory class name for this type. This class must extend
     *            BackingStore.
     * @param props
     *            Properties that contain additional configration paramters.
     * @throws DuplicateFactoryRegistrationException
     *             If this type is already registered.
     * 
     * @see BackingStoreFactory#createBackingStore(Class, String, Properties)
     */
    public synchronized void register(String type, String factoryClassName, Properties props)
            throws DuplicateFactoryRegistrationException {
        if (type2BackingStoreInfo.get(type) != null) {
            throw new DuplicateFactoryRegistrationException(
                    "Duplicate factory class (" + factoryClassName
                            + ") for type: " + type);
        } else {
            type2BackingStoreInfo.put(type, new BackingStoreInfo(
                    factoryClassName, props));
        }
    }

    /**
     * Unregister the factory
     * 
     * @param type
     *            the type
     */
    public boolean remove(String type) {
        return (type2BackingStoreInfo.remove(type) != null);
    }

    /**
     * Get the registered factory for the type specified in the parameter
     * 
     * @param type
     *            the type
     * @return The BackingStoreFactory class name or null
     */
    public String getFactoryClassName(String type) {
        BackingStoreInfo info = type2BackingStoreInfo.get(type);
        return (info == null) ? null : info.className;
    }
    
    /**
     * Get the registered factory for the type specified in the parameter
     * 
     * @param type
     *            the type
     * @return The BackingStoreFactory env (Properties) or null
     */
    public Properties getFactoryClassEnv(String type) {
        BackingStoreInfo info = type2BackingStoreInfo.get(type);
        return (info == null) ? null : info.props;
    }    

    /**
     * Return an instance of BackingStoreFactory for the specified type. If a
     * factory instance for this persistence type has not yet been instantiated
     * then an instance is created using the public no-arg constructor. Then
     * {@link BackingStore#initialize(String, Properties)} will be called to
     * properly initialize the BsackingStore.
     * 
     * @param type
     *            the persistence type for which a factory is required
     * @return an instance of BackingStoreFactory for the specified type
     * @throws BackingStoreException
     *             if the store cannot be initialized properly.
     * @throws ClassNotFoundException
     *             If the BackingStore class cannot be found.
     * @throws InstantiationException
     *             If the BackingStore class cannot be instantiated.
     * @throws IllegalAccessException
     *             If the BackingStore class doen't provide a public no-arg
     *             constructor
     * @see BackingStore.initialize(String, Properties)
     */
    public BackingStoreFactory getFactoryInstance(String type)
            throws BackingStoreException, ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        BackingStoreFactory factory = null;
        BackingStoreInfo info = type2BackingStoreInfo.get(type);
        if (info != null) {
            if (info.factory == null) {
                synchronized (info) {
                    Class clazz = Class.forName(info.className);
                    info.factory = (BackingStoreFactory) clazz.newInstance();
                }
            }

            factory = info.factory;
        }

        return factory;
    }

    /**
     * Get a list of all registered store type
     * 
     * @return Collection<String> where each entry in the collection is the
     *         type
     */
    public Collection<String> getRegisteredTypes() {
        return type2BackingStoreInfo.keySet();
    }

    private static class BackingStoreInfo {
        String className;

        Properties props;

        volatile BackingStoreFactory factory;

        BackingStoreInfo(String className, Properties props) {
            this.className = className;
            this.props = props;
        }
    }
}
