/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.hk2.configuration.hub.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.KeyedType;
import org.glassfish.hk2.configuration.hub.api.SingletonType;

/**
 * @author jwells
 *
 */
public class BeanDatabaseImpl implements BeanDatabase {
    private final HashMap<String, SingletonType> singletons = new HashMap<String, SingletonType>();
    private final HashMap<String, KeyedType> keyed = new HashMap<String, KeyedType>();
    
    /**
     * Creates a new, fresh database
     */
    public BeanDatabaseImpl() {
        
    }
    
    /**
     * Does a deep copy
     * @param copyMe
     */
    public BeanDatabaseImpl(BeanDatabaseImpl copyMe) {
        throw new AssertionError("not yet implemented");
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#getAllSingetonTypes()
     */
    @Override
    public synchronized Set<SingletonType> getAllSingetonTypes() {
        return Collections.unmodifiableSet(new HashSet<SingletonType>(singletons.values()));
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#getSingletonType(java.lang.String)
     */
    @Override
    public synchronized SingletonType getSingletonType(String typeName) {
        return singletons.get(typeName);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#getAllKeyedTypes()
     */
    @Override
    public synchronized Set<KeyedType> getAllKeyedTypes() {
        return Collections.unmodifiableSet(new HashSet<KeyedType>(keyed.values()));
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#getKeyedType(java.lang.String)
     */
    @Override
    public synchronized KeyedType getKeyedType(String typeName) {
        return keyed.get(typeName);
    }

}
