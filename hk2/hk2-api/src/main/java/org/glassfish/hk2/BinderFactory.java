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

/**
 * Factory for adding services instances to a {@link Services}. Services
 * definition can vary per service, some services implement well defined
 * interfaces usually annotated with {@link org.jvnet.hk2.annotations.Contract}
 * which are called contracts. Some services do not implement such
 * interfaces and are registered under their implementation type.
 *
 * <p/>
 * When registering services using this programmatic API, it is not
 * necessary to use the HK2 annotations. Optional presence of such
 * annotations will be ignored when registering servics.
 * 
 * <p/>
 * Important Note: A new binding is completed only after calling
 * one of the "to*(...)" methods of {@link NamedBinder}.
 *
 * @author Jerome Dochez
 * @auhor Jeff Trent
 */
public interface BinderFactory {

    /**
     * Return the parent binder factory instance which can be used to
     * bind services for wider visibility outside of this module
     * definition.
     *
     * @return the parent binder factory or null if there is no parent.
     */
    BinderFactory inParent();

    /**
     * Binds a service using a {@link String} interface name.
     *
     * @param contractName the interface fqcn.
     * @return a {@link Binder} instance that can be used to further
     * qualify the binding request.
     */
    Binder<Object> bind(String contractName);

    /**
     * Starts the binding process of a service using {@link String}
     * interfaces names that can be used to lookup the service.
     *
     * @param contractNames the interfaces fully qualified class names.
     * @return a {@link Binder} instance that can be used to further
     * qualify the binding request.
     */
    Binder<Object> bind(String... contractNames);

    /**
     * Starts a binding process of a service using at least one
     * interface or abstract class {@link Class} reference.
     *
     * Supplemental interfaces can be used to register the service
     * under different contracts. Each contract can be used to look
     * up the service.
     *
     * The service must implements all the contracts passed in
     * this method.
     *
     * @param contract the main contract {@link Class} reference
     * @param contracts supplemental contracts references
     * @param <T> the main contract type
     * @return a {@link Binder} instance for the main contract type type to further
     * qualify the binding request.
     */
    <T> Binder<T> bind(Class<T> contract, Class<?>... contracts);

    /**
     * Binds a parameterized type by forcing users to create a subclass of
     * {@link TypeLiteral} which will allow HK2 to retrieve the parameterized
     * type at runtime.
     *
     * @param typeLiteral a {@link TypeLiteral} subclass instance
     * @param <T> the parameterized type to use as a contract definition. This
     * will allow users to inject instances of the parameterized type. HK2
     * cannot bind a generic type like Set<E> although it can injects it as
     * long as E is specified at the injection point and the parameterized
     * type can be looked up.
     *
     * @return a {@link Binder} instance for that parameterized type to further
     * qualify the binding request.
     */
    <T> Binder<T> bind(TypeLiteral<T> typeLiteral);

    /**
     * Binds a service which does not implement a contract or
     * interface that can be used to look it up. The bound service
     * can only be looked up using the service type directly.
     *
     * A service registered by its type cannot be named.
     *
     * @return a {@link NamedBinder} to register the service.
     */
    NamedBinder<Object> bind();
}
