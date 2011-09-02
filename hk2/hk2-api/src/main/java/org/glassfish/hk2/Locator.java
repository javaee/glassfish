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

package org.glassfish.hk2;

import org.jvnet.hk2.annotations.Contract;

/**
 * This contract provides the starting point for querying HK2's
 * backing service registry either by contract (e.g., classes
 * annotated with @{@link Contract}) or by concrete service type
 * (e.g., classes annotated with @{@link org.jvnet.hk2.annotations.Service}).
 *
 * <p>
 * Typically, DI is used to wire services together. Hk2 internals
 * uses the {@link Locator} to resolve services used with 
 * &#064;Inject during normal dependency injection.
 * 
 * <p>
 * The {@link Locator} interface can also be used for programmatic
 * resolution of services.  Note, however, that it is recommended 
 * for most developers to avoid programmatic service resolution, 
 * and to instead rely upon DI wherever possible.
 * 
 * @author Jerome Dochez, Jeff Trent, Mason Taube
 */
public interface Locator {

    /**
     * Retrieve a service locator via a contract class.
     * 
     * <pre>
     * // ExampleContract is an example of a contract type
     * &#064;Contract
     * public interface ExampleContract {
     * }
     *  
     * &#064;Service
     * public class Example implements ExampleContract {
     * } 
     * </pre>
     * 
     * @param contract the contract class
     * @return a contract locator
     */
    <U> ContractLocator<U> forContract(Class<U> contract);

    /**
     * See {@link #forContract(Class)}, with the exception that the
     * type is provided as a string instead of a class instance.
     * 
     * @param contractName the contract class type name
     * @return a contract locator
     */
    ContractLocator<?> forContract(String contractName);

    /**
     * See {@link #forContract(Class)}, with the exception that the
     * type is a {@link TypeLiteral}, a parameterized type.
     * 
     * @param typeLiteral the parameterized contract type literal name
     * @return a contract locator
     */
    <U> ContractLocator<U> forContract(TypeLiteral<U> typeLiteral);
    
    /**
     * Retrieve a service locator via a concrete service class type.
     * 
     * <pre>
     * // Example is an example of a service type
     * &#064;Service
     * public class Example ... {
     * } 
     * </pre>
     * 
     * @param type the service class type
     * @return a service locator
     */
    <U> ServiceLocator<U> byType(Class<U> type);

    /**
     * See {@link #forContract(Class)}, with the exception that the
     * type is provided as a string instead of a class instance.
     * 
     * @param typeName the service class type name
     * @return a service locator
     */
    ServiceLocator<?> byType(String typeName);

}

 