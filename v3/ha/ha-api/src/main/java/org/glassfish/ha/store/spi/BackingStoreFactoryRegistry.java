/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.ha.store.spi;

import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.ha.store.api.BackingStoreFactory;
import org.glassfish.ha.store.impl.NoOpBackingStoreFactory;

import java.util.Properties;
import java.util.HashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bhavanishankar@dev.java.net
 */

/**
 * A class for storing BackingStore implementation. This is a
 * singleton and contains a mapping between persistence-type and
 * <code>BackingStoreFactory</code>.
 *
 * @author Mahesh.Kannan@Sun.Com
 * @author Larry.White@Sun.Com
 *
 */
public final class BackingStoreFactoryRegistry {

    private static final HashMap<String, BackingStoreFactory> factories =
            new HashMap<String, BackingStoreFactory>();

    static {
        factories.put("noop", new NoOpBackingStoreFactory());
    }

    public static synchronized void register(String type, BackingStoreFactory factory)
            throws DuplicateFactoryRegistrationException {
        if (factories.get(type) != null) {
            throw new DuplicateFactoryRegistrationException("BackingStoreFactory " +
                    "for persistene-type " + type + " already exists");
        }
        factories.put(type, factory);
        Logger.getLogger(BackingStoreFactoryRegistry.class.getName()).log(Level.INFO, "Registered "
            + factory.getClass().getName() + " for persistence-type = " + type
            + " in BackingStoreFactoryRegistry");
    }

    /**
     * Return an instance of BackingStoreFactory for the
     * specified type. If a factory instance for this persistence
     * type has not yet been instantiated then an instance is
     * created using the public no-arg constructor.
     */
    public static synchronized BackingStoreFactory getFactoryInstance(String type)
            throws BackingStoreException {
        BackingStoreFactory factory = factories.get(type);
        if (factory == null) {
            throw new BackingStoreException("Backing store for " +
                        "persistence-type " + type + " is not registered.");
        }
        
        return factory;
    }

    /**
     * Will be called by Store's Lifecycle module to unregister
     * the factory class name.
     */
    public static synchronized void unregister(String type) {
        factories.remove(type);
    }
    
}

