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

import com.sun.hk2.component.Holder;
import com.sun.hk2.component.ScopeInstance;
import org.jvnet.hk2.annotations.Service;

import java.util.Collection;

/**
 * Represents a component in the world of {@link Habitat}.
 *
 * <p>
 * {@link Inhabitant} extends from {@link Holder}, as one of its
 * purposes is to encapsulate how we obtain an instance of a component.
 * On topf of that, {@link Inhabitant} enhances {@link Holder} by
 * adding more metadata that {@link Habitat} uses for finding
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
public interface Inhabitant<T> extends Holder<T> {
    /**
     * The short-cut for {@code type().getName()}
     * but this allows us to defer loading the actual types.
     */
    String typeName();

    /**
     * Type of the inhabitant.
     *
     * <p>
     * The only binding contract that needs to be honored is that the {@link #get()}
     * method returns an instance assignable to this type. That is,
     * {@code get().getClass()==type()} doesn't necessarily have to hold,
     * but {@code type().isInstance(get())} must.
     *
     * <p>
     * This is particularly true when {@link Factory} is involved, as in such
     * case HK2 has no way of knowing the actual type.
     *
     * That said, this method is not designed for the semantics of
     * contract/implementation split --- implementations of a contract
     * should return the concrete type from this method, and use
     * {@link Habitat#addIndex(Inhabitant, String, String) habitat index}
     * to support look-up by contract. 
     *
     * @return
     *      Always non-null, same value.
     */
    Class<T> type();

    /**
     * Returns the instance of this inhabitant.
     *
     * <p>
     * Some {@link Inhabitant}s return the same instance for multiple
     * invocations (AKA singleton), but
     * the method may return different instances to invocations from different
     * context (AKA scope.) The extreme case is where the each invocation
     * returns a different object.
     */
    T get();

    /**
     * Returns the instance of this inhabitant.
     *
     * <p>
     * <b>THIS METHOD SHOULD BE ONLY USED BY HK2 IMPLEMENTATION</b>.
     *
     * <p>
     * {@link Inhabitant}s are often used with the decorator pattern
     * (see {@link com.sun.hk2.component.AbstractCreatorInhabitantImpl} for example), yet during
     * the object initializtion inside the {@link #get()} method, we often
     * need the reference to the outer-most {@link Inhabitant} registered to
     * the {@link Habitat} (for example so that we can request the injection
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
     * Gets the metadata associated with this inhabitant.
     *
     * <p>
     * This data is usually used by a sub-system of HK2, and not really meant to
     * be used by applications. (At least for now.)
     * The main benefit of metadata is that it's available right away
     * as soon as the {@link Habitat} is properly initialized, even before
     * component classes are loaded. In contrast, accessing annotations would require
     * classes to be loaded and resolved.
     *
     * @return
     *      can be empty but never null. The values are read-only.
     * @see Service#metadata() 
     */
    MultiMap<String,String> metadata();

    /**
     * Obtains the serialized metadata.
     *
     * <p>
     * This method is a wrapper around {@link #metadata()} and useful for
     * defining a highly structured metadata that doesn't easily fit
     * a simple string representation.
     *
     * <p>
     * The implementation of this method is to obtain the value associated with
     * this key as {@code metadata().getOne(key)}, and if that exists, treat
     * the value as base64-encoded binary, and deserializes it and returns the object.
     *
     * <p>
     * The classes used in the serialization need to be available during the build time
     * (normally during the HK2 compile mojo runs) so that the metadata can be
     * serialized. The evolution of these classes need to be careful done, otherwise
     * the deserialization of the metadata may fail unexpectedly.
     *
     * @throws Error
     *      If the deserialization fails. This can be for example because of
     *      the incompatible class change, or failure to resolve the classes.
     *      Sine these problems can only happen in a critical situation,
     *      this method throws unchecked error.
     *      TODO: switch this to IOError when we can depend on JDK6.
     *
     * @return
     *      the deserialized object.
     */
    <T> T getSerializedMetadata(Class<T> type, String key);

    /**
     * Obtains the metadata serialized into String.
     *
     * <p>
     * This is a convenient short-cut that does {@code getSerializedMetadata(type,type.getName())}
     */
    <T> T getSerializedMetadata(Class<T> type);

    /**
     * Called to orderly shutdown {@link Habitat}.
     * <p>
     * The expected behavior is for objects to get its {@link PreDestroy}
     * callback invoked, and its reference released. For singleton
     * objects, this method is expected to dispose that object.
     * <p>
     * For scoped objects, those are released when {@link ScopeInstance#release()}
     * is invoked.
     */
    void release();

    /**
     * Returns true if the underlying object has been instantiated.
     *
     * @return true if the instance of this imhabitant has been created, false
     * otherwise
     */
    boolean isInstantiated();

//
// methods below here are more or less used for book-keeping purpose by Habitat,
// and implementations of Inhabitant should implement them just by using
// AbstractInhabitantImpl
//

    /**
     * If this inhabitant is a companion to another inhabitant (called "lead"),
     * This method returns that inhabitant. Otherwise null.
     */
    Inhabitant lead();

    /**
     * Returns the companion inhabitants associated with this inhabitant.
     *
     * <p>
     * This method works with the {@link #lead()} method in pairs, such
     * that the following condition always holds:
     *
     * <pre>x.companions().contains(y) &lt;-> y.lead()==x</pre>
     *
     * @return
     *      Can be empty but never null.
     */
    Collection<Inhabitant> companions();

    /**
     * This method is only meant to be invoked by {@link Habitat}.
     */
    void setCompanions(Collection<Inhabitant> companions);

    /**
     * Gets or creates an inhabitant instance that is functionally equivalent to the
     * original with possible exception for scoping and lifecycle behavior.
     *
     * <p/>
     * Hk2's injection machinery requests a scopedClone for each inhabitant it
     * obtains out of the habitat for purposes of injection for each injection point
     * of a component undergoing injection.  If and when that component is released,
     * each scoped-clone inhabitant will also have release called on it.
     *
     * <p/>
     * For example, a PerLookup scoped inhabitant in the habitat will return a new
     * service instance for each and every call to get().  A scoped clone might create a
     * 1-to-1 relationship from the scoped-cloned inhabitant to the service it produces
     * allowing for the possibility of release() on the inhabitant / service.  More
     * sophisticated implementation may also exist that uses reference counting, soft or
     * weak references, etc.
     *
     * <p/>
     * Scoped clones of inhabitants that are themselves scoped clones will drive off of
     * the original underlying inhabitant, and not the scoped clone itself.
     *
     * @return an inhabitant instance that is appropriate for the scoping rules of that inhabitant;
     *          always non-null but may return the 'this' pointer as the default implementation.
     */
    Inhabitant<T> scopedClone();

    /**
     * Puts an inhabitant under management of this inhabitant instance.
     *
     * <p/>
     * FOR INTERNAL USE ONLY
     *
     * <p/>
     * Management implies the lifecycle of this inhabitant is reflected into the collection
     * of managed inhabitants associated with this instance (i.e., release cascades).
     *
     * @param managedInhabitant the inhabitant to associate lifecycle with
     */
    void manage(Inhabitant<?> managedInhabitant);

}
