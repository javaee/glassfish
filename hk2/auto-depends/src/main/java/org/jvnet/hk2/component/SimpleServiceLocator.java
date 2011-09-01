/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.component;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Provide a simple abstraction for getting services by contract or type.
 *
 * @author Jerome Dochez
 * @author Jeff Trent
 */
public interface SimpleServiceLocator {

    /**
     * Loads a component that implements the given contract and has the given
     * name.
     * 
     * @param name
     *            can be null, in which case it'll only match to the unnamed
     *            component.
     * @return null if no such service exists.
     */
    <T> T getComponent(Class<T> contract, String name) throws ComponentException;

    /**
     * Analogous to the following:
     * <pre>
     * getComponent(contractClass.getName(), name);
     * </pre>

     * @param fullQualifiedName the contract class name
     * @param name
     *            can be null, in which case it'll only match to the unnamed
     *            component.
     * @return null if no such service exists.
     */
    <T> T getComponent(String fullQualifiedName, String name);
    
    /**
     * Gets the object of the given type.
     * 
     * @return null if not found.
     */
    <T> T getByType(Class<T> implType);

    /**
     * Gets the object of the given type.
     * 
     * @return null if not found.
     */
    <T> T getByType(String implType);

    /**
     * Gets an inhabitant from its type and optionally name
     * @param type requested inhabitant type
     * @param name optional name
     */
    <T> Inhabitant<T> getProvider(Type type, String name);

    <T> Inhabitant<T> getProvider(String fqcn, String name);

    /**
     * Gets all the inhabitants that has the given contract.
     */
    public <T> Collection<Inhabitant<T>> getInhabitantsByContract(
            Type contract) throws ComponentException;

    public <T> Collection<Inhabitant<T>> getInhabitantsByContract(
            String contractName) throws ComponentException;


    /**
     * Gets all the inhabitants that has the given type.
     */
    public <T> Collection<Inhabitant<T>> getInhabitantsByType(
            Class<T> type) throws ComponentException;

    public <T> Collection<Inhabitant<T>> getInhabitantsByType(
            String typeName) throws ComponentException;
}
