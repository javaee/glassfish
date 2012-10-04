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
package org.jvnet.hk2.component;

import org.glassfish.hk2.api.ActiveDescriptor;

/**
 * Represents a component in the world of {@link ServiceLocator}.
 *
 * <p>
 * {@link Inhabitant} extends from {@link Holder}, as one of its
 * purposes is to encapsulate how we obtain an instance of a component.
 * On topf of that, {@link Inhabitant} enhances {@link Holder} by
 * adding more metadata that {@link ServiceLocator} uses for finding
 * components and hooking them up together.
 *
 * <p>
 * All the methods exept {@link #get()} are immutable, meaning
 * they never change the value they return.
 *
 * <p>
 * See {@link Inhabitants} for several factory methods for typical
 * {@link Inhabitant} constructions.
 * 
 * @author Kohsuke Kawaguchi
 * @see Inhabitants
 */
// TODO: Eventually get rid of auto-depend's Holder
@Deprecated
public interface Inhabitant<T> extends ActiveDescriptor<T> {
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
     * Returns true if the component has been instantiated.
     *
     * @return true if the component is active.
     */
    boolean isActive();

    /**
     * Returns the instance of this inhabitant.
     *
     * <p>
     * <b>THIS METHOD SHOULD BE ONLY USED BY HK2 IMPLEMENTATION</b>.
     *
     * <p>
     * {@link Inhabitant}s are often used with the decorator pattern
     * yet during
     * the object initializtion inside the {@link #get()} method, we often
     * need the reference to the outer-most {@link Inhabitant} registered to
     * the {@link ServiceLocator} (for example so that we can request the injection
     * of {link Inhabita} that represents itself, or to inject companions.)
     *
     * <p>
     * So this overloaded version of the get method takes the outer-most
     * {@link Inhabitant}. This method is only invoked from within HK2
     * where the decorator pattern is used.
     */
    // TODO: this and the lead/companions method make you wonder whether we should
    // define Inhabitant as an abstract class.
    T get(Inhabitant onBehalfOf);

    /**
     * Called to orderly shutdown {@link ServiceLocator}.
     * <p>
     * The expected behavior is for objects to get its {@link org.glassfish.hk2.api.PreDestroy}
     * callback invoked, and its reference released. For singleton
     * objects, this method is expected to dispose that object.
     * <p>
     */
    void release();
}
