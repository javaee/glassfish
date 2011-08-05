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

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Similar to the {@link Factory} contract, providing a means to access the
 * class type instance. The scope of the instances produces by the provider
 * is determined by the implementation (e.g., singleton, per lookup, etc).
 * 
 * <p/>
 * {@link Binding}s represent something that is registered in {@link
 * Services} whereas a Provider provides the runtime services for the given
 * registered {@link Binding} entry in the correct context appropriate for
 * the caller.
 * 
 * @author Jerome Dochez
 * @author Jeff Trent
 * @author Mason Taube
 *  
 * @see ManagedComponentProvider
 */
public interface Provider<T> {

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
     *      
     * @throws ComponentException
     *      If the factory failed to get/create an instance
     *      and would like to propagate the error to the caller.
     */
    T get() throws ComponentException;

    /**
     * The class type of the implementation. it is responsible also
     * for determining how (i.e., which loader) to use.
     * 
     * <p/>
     * The class type for what the {@link Factory} actually
     * produces.
     * 
     * <p/>
     * Note that there is some cost to this call during the first
     * invocation since it needs to perform classloading.  Care
     * should therefore be exercised accordingly.
     * 
     * @return
     * 	the class type for what this Provider produces, or null
     * 	only in the case where the Provider is a facade to a user
     * 	defined factory that
     * 
     */
    Class<? extends T> type();

    /**
     * The collection of annotations for this type.  Note that this
     * may not be the same as the annotations on the {@link #type()}.
     * 
     * @return
     * 	a non-null collection of annotation classes for
     *  this provider type.
     */
    Collection<Annotation> getAnnotations();

    // This is in ManagedComponentProvider
//    /**
//     * Returns true if the component has been instantiated.
//     *
//     * @return true if the component is active.
//     */
//    boolean isActive();

}
