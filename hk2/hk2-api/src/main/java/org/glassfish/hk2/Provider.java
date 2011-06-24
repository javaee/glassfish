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
 * Provides an instance of T either by creating a new one at each request
 * or by returning an existing one.
 * 
 * <p/>
 * Providers are used in a wide variety of ways in HK2:
 * 
 * <li>Developers use Providers while programmatically binding services
 * via methods line {@link NamedBinder#toProvider(Provider)}. In this
 * scenario the {@link Provider} the developer provides is a logically
 * equivalent to a factory. The provider [factory] is eventually called
 * at runtime whenever a consumer locates this component provider and
 * attempts to use it.
 * 
 * The {@link Scope} of services it produces is ultimately
 * determined by how the provider [factory] implementation behaves
 * when asked to provide service instances to the consumer.
 * 
 * <li>For services declared as "PerLookup", HK2 will create a
 * {@link ComponentProvider} that will behave by returning a new service
 * for each and every call to {@link #get()} regardless of the
 * caller context.
 * 
 * <li>For services declared as Singleton {@link Scope}, HK2 will return
 * the same service globally for every request made. 
 * 
 * <li>HK2 does not yet support JSR-299/330 "Dependent" services. Dependent
 * scoped services are similar in nature to PerLookup. The similarity is
 * that they both always return new, unique instances for every call
 * and/or injection point being satisfied. The difference, however, relates
 * to the lifecycle of the created component/service.  PerLookup services
 * are NOT tied to the lifecycle of the parent when used with injection. In
 * another words, {@link ManagedComponentProvider#release()} will not be
 * invoked on a PerLookup scoped service.  A Dependent service in
 * JSR-330/299, on the other hand, will be released when its parent
 * component/service/bean is released (i.e., goes out of scope).
 * 
 * <li>Is the super class for {@link ComponentProvider}.
 * 
 * @author Jerome Dochez
 * @author Jeff Trent
 * @author Mason Taube
 *
 * @see ComponentProvider
 */
public interface Provider<T> extends Holder<T> {

    /**
     * The system calls this method to obtain a reference
     * to the component/service.
     *
     * @return
     *      null is a valid return value. This is useful
     *      when a factory primarily does a look-up and it fails
     *      to find the specified component, yet you don't want that
     *      by itself to be an error. If the injection wants
     *      a non-null value (i.e., <tt>@Inject(optional=false)</tt>).
     * @throws ComponentException
     *      If the factory failed to get/create an instance
     *      and would like to propagate the error to the caller.
     */
    @Override
    T get() throws ComponentException;

}
