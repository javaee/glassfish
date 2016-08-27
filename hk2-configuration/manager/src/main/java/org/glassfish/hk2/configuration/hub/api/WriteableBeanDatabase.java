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
package org.glassfish.hk2.configuration.hub.api;

import java.util.Set;

import org.glassfish.hk2.api.MultiException;

/**
 * A writeable version of a {@link BeanDatabase}. Types and instances can be
 * added to this in-memory database
 * 
 * @author jwells
 * 
 */
public interface WriteableBeanDatabase extends BeanDatabase {
    /**
     * Gets an unmodifiable set of all the types in the bean database
     * 
     * @return A non-null unmodifiable and possibly empty set of
     * all the types in the database
     */
    public Set<WriteableType> getAllWriteableTypes();
    
    /**
     * Adds a type of the given name
     * 
     * @param typeName
     *            The name of the type to add
     * @return The non-null type that has been added to the database
     */
    public WriteableType addType(String typeName);

    /**
     * Removed the given type and all of its instances from the database. The
     * set of changes will include the instances removed prior to the change
     * indicating that the type was removed
     * 
     * @param typeName
     *            The non-null type name
     * @return The type that was removed
     */
    public Type removeType(String typeName);

    /**
     * Gets a writeable type of the given name
     * 
     * @param typeName
     *            The non-null name of the type to fetch
     * @return The existing type, or null if the type does not already exist
     */
    public WriteableType getWriteableType(String typeName);

    /**
     * Gets or creates a writeable type with the given name
     * 
     * @param typeName
     *            The non-null name of the type to find or create
     * @return The non-null writeable type that was created or found
     */
    public WriteableType findOrAddWriteableType(String typeName);

    /**
     * This method should be called when the writeable database should become
     * the current database. All changes will be communicated to the listeners.
     * If the current database has been modified since this writeable database
     * was created then this method will throw an IllegalStateException
     * @throws IllegalStateException if the current database has been modified
     * since this writeable database copy was created
     * @throws MultiException if there were user implementations of {@link BeanDatabaseUpdateListener}
     * that failed by throwing exceptions this exception will be thrown wrapping those exceptions
     */
    public void commit() throws IllegalStateException, MultiException;
    
    /**
     * This method should be called when the writeable database should become
     * the current database. All changes will be communicated to the listeners.
     * If the current database has been modified since this writeable database
     * was created then this method will throw an IllegalStateException
     * @param commitMessage An object to pass to any {@link BeanDatabaseUpdateListener}
     * that is registered
     * @throws IllegalStateException if the current database has been modified
     * since this writeable database copy was created
     * @throws MultiException if there were user implementations of {@link BeanDatabaseUpdateListener}
     * that failed by throwing exceptions this exception will be thrown wrapping those exceptions
     */
    public void commit(Object commitMessage) throws IllegalStateException, MultiException;

}
