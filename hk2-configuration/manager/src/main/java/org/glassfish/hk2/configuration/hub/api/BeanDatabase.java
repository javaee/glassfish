/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2016 Oracle and/or its affiliates. All rights reserved.
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

import java.io.PrintStream;
import java.util.Set;

/**
 * A database of beans organized as types, where a type
 * can have multiple instances of a configuration bean
 * 
 * @author jwells
 *
 */
public interface BeanDatabase {
    /**
     * Gets an unmodifiable set of all the types in the bean database
     * 
     * @return A non-null unmodifiable and possibly empty set of
     * all the types in the database
     */
    public Set<Type> getAllTypes();
    
    /**
     * Gets the type with the given name
     * 
     * @param type The non-null name
     * @return The type corresponding to the given name.  May return null
     */
    public Type getType(String type);
    
    /**
     * Returns the instance with the given instanceKey from the
     * type with the given name
     * 
     * @param type The non-null name of the type to get the instance from
     * @param instanceKey The non-null key of the instance
     * @return The bean from the given type with the given name.  Will return
     * null if the type does not exist or an instance with that key does not exist
     */
    public Instance getInstance(String type, String instanceKey);
    
    /**
     * Dumps the type and instance names to stderr
     */
    public void dumpDatabase();
    
    /**
     * Dumps the type and instance names to the given stream
     * @param output - The non-null outut stream to write the database to
     */
    public void dumpDatabase(PrintStream output);
    
    /**
     * Dumps the type and instance names to a String for debugging
     * 
     * @return A string with all type and instance names
     */
    public String dumpDatabaseAsString();

}
