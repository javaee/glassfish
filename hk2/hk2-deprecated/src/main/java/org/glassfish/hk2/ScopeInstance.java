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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.hk2.api.PreDestroy;

/**
 * A particular instantiation of a {@link Scope}. Will be
 * used to store and retrieve components for that particular
 * {@link Scope}
 *
 * <p>
 * For example, for the "request scope", an instance
 * of {@link ScopeInstance} is created for each request.
 *
 * @author Jerome Dochez
 * @see Scope#current()
 */
@Deprecated
public interface ScopeInstance {

    /**
     * Retrieves a stored inhabitant if present in the scope instance. Otherwise
     * returns {@code null}. Note that {@code null} returned value may be a valid
     * inhabitant value stored in the scope instance. To check if this is the case
     * {@link #contains(org.glassfish.hk2.Provider)} method can be used.
     *
     * @param <T> the requested inhabitant instance type.
     * @param provider the {@link Provider} instance we request the inhabitant for.
     * @return the instance of T for that provider. Method may return {@code null}
     *     in case the {@code null} inhabitant value has been stored in the scope
     *     or in case the inhabitant instance has never been stored in the scope.
     * @see #contains(org.glassfish.hk2.Provider)
     *
     */
    public <T> T get(Provider<T> provider);

    /**
     * Returns {@code true} if this scope instance contains a stored inhabitant
     * for a given provider, returns {@code false} otherwise. Note that this method
     * will return {@code true} even if the previously stored inhabitant value is
     * {@code null}.
     *
     * @param <T> the requested inhabitant instance type.
     * @param provider the {@link Provider} instance we request the inhabitant for.
     * @return {@code true} if this scope instance contains an inhabitant value
     *     for the specified {@link Provider} instance.
     * @see #get(org.glassfish.hk2.Provider)
     */
    public <T> boolean contains(Provider<T> provider);

    /**
     * Stores a inhabitant component instance.
     *
     * @param <T> type of the component.
     * @param provider component description as an {@link Provider}.
     * @param value inhabitant component instance. May be {@code null}.
     * @return the previous value associated with this provider (if any).
     */
    public <T> T put(Provider<T> provider, T value);

    /**
     * release the backend storage and call {@link org.glassfish.hk2.api.PreDestroy#preDestroy()}
     * on all instantiated components that implement the {@link PreDestroy} interface.
     */
    public void release();
}
