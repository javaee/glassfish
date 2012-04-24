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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Future;

import org.glassfish.hk2.Binding;
import org.glassfish.hk2.ContractLocator;
import org.glassfish.hk2.Descriptor;
import org.glassfish.hk2.DynamicBinderFactory;
import org.glassfish.hk2.TypeLiteral;
import org.glassfish.hk2.inject.Injector;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.component.HabitatListener.EventType;
import org.jvnet.hk2.component.InhabitantTracker.Callback;

/**
 * A set of templates that constitute a world of objects.
 *
 * @author Kohsuke Kawaguchi
 * @author Jerome Dochez
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Habitat implements Injector, SimpleServiceLocator {

    public Habitat() {
        this(null, null, null);
    }

    public Habitat(Habitat parent, String name) {
        this(parent, name, null);
    }

    Habitat(Habitat parent, String name, Boolean concurrency_controls) {
        throw new UnsupportedOperationException("<clinit> in Habitat");
    }

    public Habitat getDefault() {
        throw new UnsupportedOperationException("getDefault in Habitat");
    }

    public Habitat getServices(String moduleName) {
        throw new UnsupportedOperationException("getServices(" + moduleName + ") in Habitat");
    }

    public Collection<Binding<?>> getDeclaredBindings() {
        throw new UnsupportedOperationException("getDeclaredBindings in Habitat");
    }

    public Collection<Binding<?>> getDeclaredBindings(Descriptor descriptor) {
        throw new UnsupportedOperationException("getDeclaredBindings(" + descriptor + ") in Habitat");
    }

    void includeBinding(LinkedHashSet<Binding<?>> result, Descriptor descriptor, Object i) {
        throw new UnsupportedOperationException("includeBinding(" + result + "," + descriptor + "," + i + ") in Habitat");
    }

    public Collection<Binding<?>> getBindings() {
        throw new UnsupportedOperationException("getBindings in Habitat");
    }

    public Collection<Binding<?>> getBindings(Descriptor descriptor) {
        throw new UnsupportedOperationException("getBindings(" + descriptor + ") in Habitat");
    }
    
    public DynamicBinderFactory bindDynamically() {
        throw new UnsupportedOperationException("bindDynamically in Habitat");
    }

    public <U> ContractLocator<U> forContract(Class<U> contract) {
        throw new UnsupportedOperationException("forContract(" + contract + ") in Habitat");
    }

    public ContractLocator<?> forContract(String contractName) {
        throw new UnsupportedOperationException("forContract(" + contractName + ") in Habitat");
    }

    public <U> ContractLocator<U> forContract(TypeLiteral<U> typeLiteral) {
        throw new UnsupportedOperationException("forContract(" + typeLiteral + ") in Habitat");
    }

    public <U> org.glassfish.hk2.ServiceLocator<U> byType(Class<U> type) {
        throw new UnsupportedOperationException("byType(" + type + ") in Habitat");
    }

    public org.glassfish.hk2.ServiceLocator<?> byType(String typeName) {
        throw new UnsupportedOperationException("byType(" + typeName + ") in Habitat");
    }

    /**
     * Add a habitat listener with no contract-level filtering. This API is
     * primarily intended for internal cases within Hk2.
     * <p/>
     * The listener with no contract-level filtering will be called for all
     * change events within the habitat pertaining to inhabitants.
     *
     * @param listener The habitat Listener to be added
     * @see {@link #addHabitatListener(HabitatListener, String...)} is
     *      recommended for most cases
     */
    public void addHabitatListener(HabitatListener listener) {
        throw new UnsupportedOperationException("addHabitatListener(" + listener + ") in Habitat");
    }

    /**
     * Add a habitat listener with contract-level filtering.
     * <p/>
     * The listener will be called based on the set of contract filters
     * provided.
     *
     * @param listener  The habitat Listener to be added
     * @param typeNames The contracts to filter on; this should be non-null
     */
    public void addHabitatListener(HabitatListener listener,
                                   String... typeNames) {
        throw new UnsupportedOperationException("addHabitatListener(" + listener + "," + typeNames + ") in Habitat");
    }

    protected void addHabitatListener(HabitatListener listener, Set<String> typeNames) {
        throw new UnsupportedOperationException("addHabitatListener(" + listener + "," + typeNames + ") in Habitat");
    }

    /**
     * Remove a habitat listener.
     *
     * @param listener The habitat Listener to be removed
     * @return true; if the listener was indeed removed
     */
    public boolean removeHabitatListener(HabitatListener listener) {
        throw new UnsupportedOperationException("removeHabitatListener(" + listener + ") in Habitat");
    }

    /**
     * Registers a dependency on the inhabitant with the given tracker context.
     * <p/>
     * Once the criteria is met, any callback provided is called. This callback
     * may occur asynchronously from the thread initiating the event.
     *
     * @param itc      The tracking criteria.
     * @param callback Optionally the callback.
     * @return The tracker
     * @throws ComponentException
     */
    public InhabitantTracker track(InhabitantTrackerContext itc,
                                   Callback callback) throws ComponentException {
        throw new UnsupportedOperationException("track(" + itc + "," + callback + ") in Habitat");
    }

    /**
     * Returns a future that can be checked asynchronously, and multiple times.
     * <p/>
     * <b>Implementation Note:</b> The Future that is returned does not behave
     * in the traditional sense in that it is NOT directly submitted to an
     * ExecutorService. Each call to get() or get(timeout) may result in a
     * [re]submission to an internally held executor. This means that a call to
     * get(...) may return a tracker, and a subsequent call to get(...) may
     * return null, or vice versa. This is true until the underlying tracker is
     * released at which point a tracker is no longer usable.
     *
     * @param itc The tracking criteria.
     * @return The tracker
     * @throws ComponentException
     */
    public Future<InhabitantTracker> trackFuture(InhabitantTrackerContext itc)
            throws ComponentException {
        throw new UnsupportedOperationException("trackFuture(" + itc + ") in Habitat");
    }

    /*
     * Why initialize/processInhabitantDecorations didn't work:
     * 
     * when a new CageBuilder comes into the habitat, it needs to build cages
     * for all existing components. when a caged component comes into the
     * habitat, it checks existing cage builders in the habitat.
     * 
     * Now, when a cage builder and a component are both initialized first and
     * then processInhabitantDecorations runs for both of them later, then you
     * end up creating a cage twice, because the builder think it got into
     * habitat after than the component, and the component think it got into
     * habitat after the builder.
     */

    /**
     * Removes all inhabitants for a particular type
     *
     * @param type of the component
     * @return true if any inhabitants were removed
     */
    public boolean removeAllByType(Class<?> type) {
        throw new UnsupportedOperationException("removeAllByType(" + type + ") in Habitat");
    }

    /**
     * Adds a new inhabitant.
     * <p/>
     * <p/>
     * See {@link Inhabitants} for typical ways to create {@link Inhabitant}s.
     */
    public void add(final Inhabitant<?> i) {
        throw new UnsupportedOperationException("add(" + i + ") in Habitat");
    }

    /**
     * Adds a new index to look up the given inhabitant.
     *
     * @param index Primary index name, such as contract FQCN.
     * @param name  Name that identifies the inhabitant among other inhabitants in
     *              the same index. Can be null for unnamed inhabitants.
     */
    public void addIndex(Inhabitant<?> i, String index, String name) {
        throw new UnsupportedOperationException("addIndex(" + i + ","  + index + "," + name + ") in Habitat");
    }

    protected void addIndex(Inhabitant<?> i, String index, String name, boolean notify) {
        throw new UnsupportedOperationException("addIndex(" + i + ","  + index + "," + name + "," + notify + ") in Habitat");
    }

    protected static Long getServiceRanking(Inhabitant<?> i, boolean wantNonNull) {
        throw new UnsupportedOperationException("getServiceRanking(" + i + ","  + wantNonNull + ") in Habitat");
    }

    /**
     * Removes an inhabitant
     *
     * @param inhabitant inhabitant to be removed
     */
    public boolean remove(Inhabitant<?> inhabitant) {
        throw new UnsupportedOperationException("remove(" + inhabitant + ") in Habitat");
    }

    /**
     * Removes a NamedInhabitant for a specific contract
     *
     * @param index contract name
     * @param name  instance name
     * @return true if the removal was successful
     */
    public boolean removeIndex(String index, String name) {
        throw new UnsupportedOperationException("removeIndex(" + index +"," + name + ") in Habitat");
    }

    /**
     * Removes a Contracted service
     *
     * @param index               the contract name
     * @param serviceOrInhabitant the service instance, or an Inhabitant instance
     */
    public boolean removeIndex(String index, Object serviceOrInhabitant) {
        throw new UnsupportedOperationException("removeIndex(" + index +"," + serviceOrInhabitant + ") in Habitat");
    }

    protected boolean matches(Inhabitant<?> inhabitant,
                              Object serviceOrInhabitant) {
        throw new UnsupportedOperationException("matches(" + inhabitant +"," + serviceOrInhabitant + ") in Habitat");
    }

    protected Object service(Object serviceOrInhabitant) {
        throw new UnsupportedOperationException("service(" + serviceOrInhabitant + ") in Habitat");
    }

    protected static interface NotifyCall {
        boolean inhabitantChanged(HabitatListener listener);
    }

    /**
     * Trigger a notification that an inhabitant has changed.
     *
     * @param inhabitant the inhabitant that has changed
     * @param contracts  the contracts associated with the inhabitant
     */
    public void notifyInhabitantChanged(Inhabitant<?> inhabitant,
                                        String... contracts) {
        throw new UnsupportedOperationException("notifyInhabitantChanged(" + inhabitant + "," + contracts + ") in Habitat");
    }

    /**
     * FOR INTERNAL USE ONLY
     */
    public synchronized void initialized() {
        throw new UnsupportedOperationException("initialized in Habitat");
    }

    static void contextualFactoriesPresent() {
        throw new UnsupportedOperationException("contextualFactoriesPresent in Habitat");
    }

    public boolean isInitialized() {
        throw new UnsupportedOperationException("isInitialized in Habitat");
    }

    /**
     * FOR INTERNAL USE
     */
    public static boolean isContextualFactoriesPresentAnywhere() {
        throw new UnsupportedOperationException("isContextualFactoriesPresentAnywhere in Habitat");
    }

    /**
     * FOR INTERNAL USE
     */
    public boolean isContextualFactoriesPresent() {
        throw new UnsupportedOperationException("isContextualFactoriesPresent in Habitat");
    }

    protected void notify(final Inhabitant<?> inhabitant,
                          final EventType event, final String index,
                          final Inhabitant<HabitatListener> extraListenerToBeNotified) {
        throw new UnsupportedOperationException("notify(" + inhabitant + "," + event + "," + index + "," + extraListenerToBeNotified + ") in Habitat");
    }

    protected void notify(final Inhabitant<?> inhabitant,
                          final EventType event, final String index, final String name,
                          final Object service,
                          final Inhabitant<HabitatListener> extraListenerToBeNotified) {
        throw new UnsupportedOperationException("notify(" + inhabitant + "," + event + "," + index + "," + name + "," + service + "," + extraListenerToBeNotified + ") in Habitat");
    }

    protected void notify(final NotifyCall innerCall,
                          final Inhabitant<?> inhabitant, final EventType event,
                          final String index,
                          final Inhabitant<HabitatListener> extraListenerToBeNotified) {
        throw new UnsupportedOperationException("notify(" + inhabitant + "," + event + "," + index + "," + extraListenerToBeNotified + ") in Habitat");
    }

    /**
     * Checks if the given type is a contract interface that has some
     * implementations in this {@link Habitat}.
     * <p/>
     * <p/>
     * There are two ways for a type to be marked as a contract. Either it has
     * {@link Contract}, or it's marked by {@link ContractProvided} from the
     * implementation.
     * <p/>
     * <p/>
     * Note that just having {@link Contract} is not enough to make this method
     * return true. It can still return false if the contract has no
     * implementation in this habitat.
     * <p/>
     * <p/>
     * This method is useful during the injection to determine what lookup to
     * perform, and it handles the case correctly when the type is marked as a
     * contract by {@link ContractProvided}.
     */
    public boolean isContract(Class<?> type) {
        throw new UnsupportedOperationException("isContract(" + type + ") in Habitat");
    }

    public boolean isContract(java.lang.reflect.Type type) {
        throw new UnsupportedOperationException("isContract(" + type + ") in Habitat");
    }
    
    public boolean isContract(String fullyQualifiedClassName) {
        throw new UnsupportedOperationException("isContract(" + fullyQualifiedClassName + ") in Habitat");
    }

    /**
     * A weaker test than {@link #isContract(Type)}.
     * 
     * <p/>
     * This will return true if either the type argument
     * is annotated with {@link Contract} or if the
     * {@link #isContract(Type)} returns true.
     */
    public boolean isContractExt(java.lang.reflect.Type type) {
        throw new UnsupportedOperationException("isContractExt(" + type + ") in Habitat");
    }
    
    /**
     * Gets all the inhabitants registered under the given {@link Contract}.
     * This is an example of heterogeneous type-safe container.
     *
     * @return can be empty but never null.
     */
    public <T> Collection<T> getAllByContract(Class<T> contractType) {
        throw new UnsupportedOperationException("getAllByContract(" + contractType + ") in Habitat");
    }

    public <T> Collection<T> getAllByContract(String contractType) {
        throw new UnsupportedOperationException("getAllByContract(" + contractType + ") in Habitat");
    }

    /**
     * Gets the object of the given type.
     *
     * @return can be empty but never null.
     */
    public <T> Collection<T> getAllByType(Class<T> implType) {
        throw new UnsupportedOperationException("getAllByType(" + implType + ") in Habitat");
    }

    /**
     * Add an already instantiated component to this manager. The component has
     * been instantiated by external code, however dependency injection,
     * PostConstruct invocation and dependency extraction will be performed on
     * this instance before it is store in the relevant scope's resource
     * manager.
     *
     * @param component component instance
     * @throws ComponentException if the passed object is not an HK2 component or
     *                            injection/extraction failed.
     */
    // TODO: mutating Habitat after it's created poses synchronization issue
    public <T> void addComponent(T component) throws ComponentException {
        throw new UnsupportedOperationException("addComponent(" + component + ") in Habitat");
    }

    /**
     * Obtains a reference to the component inside the manager.
     * <p/>
     * <p/>
     * This is the "new Foo()" equivalent in the IoC world.
     * <p/>
     * <p/>
     * Depending on the {@link Scope} of the component, a new instance might be
     * created, or an existing instance might be returned.
     *
     * @return non-null.
     * @throws ComponentException If failed to obtain a requested instance. In practice,
     *                            failure only happens when we try to create a new instance of
     *                            the component.
     */
    public <T> T getComponent(Class<T> clazz) throws ComponentException {
        throw new UnsupportedOperationException("getComponent(" + clazz + ") in Habitat");
    }

    @Override
    public <T> T getComponent(final Class<T> contract, String name)
            throws ComponentException {
        throw new UnsupportedOperationException("getComponent(" + contract + "," + name + ") in Habitat");
    }

    @Override
    public <T> Inhabitant<T> getProvider(String fullQualifiedName, String name) {
        throw new UnsupportedOperationException("getProvider(" + fullQualifiedName + "," + name + ") in Habitat");
    }

    @Override
    public <T> T getComponent(String fullQualifiedName, String name) {
        throw new UnsupportedOperationException("getComponent(" + fullQualifiedName + "," + name + ") in Habitat");
    }

    /**
     * Gets a lazy reference to the component.
     * <p/>
     * <p/>
     * This method defers the actual instantiation of the component until
     * {@link Inhabitant#get()} is invoked.
     *
     * @return null if no such component is found.
     */
    public <T> Inhabitant<T> getInhabitant(Class<T> contract, String name)
            throws ComponentException {
        throw new UnsupportedOperationException("getInhabitant(" + contract + "," + name + ") in Habitat");
    }


    public <T> Inhabitant<T> getInhabitant(java.lang.reflect.Type type, String name) {
        throw new UnsupportedOperationException("getInhabitant(" + type + "," + name + ") in Habitat");
    }

    /**
     * Gets a lazy reference to the component.
     * <p/>
     * <p/>
     * This method defers the actual instantiation of the component until
     * {@link Inhabitant#get()} is invoked.
     *
     * @return null if no such component is found.
     */
    public <T> Inhabitant<T> getInhabitantByType(Class<T> implType) {
        throw new UnsupportedOperationException("getInhabitantByType(" + implType + ") in Habitat");
    }

    public <T> Inhabitant<T> getInhabitantByType(java.lang.reflect.Type implType) {
        throw new UnsupportedOperationException("getInhabitantByType(" + implType + ") in Habitat");
    }

    public Inhabitant<?> getInhabitantByType(String fullyQualifiedClassName) {
        throw new UnsupportedOperationException("getInhabitantByType(" + fullyQualifiedClassName + ") in Habitat");
    }

    /**
     * Gets the inhabitant that has the given contract annotation and the given
     * name.
     * <p/>
     * <p/>
     * This method defers the actual instantiation of the component until
     * {@link Inhabitant#get()} is invoked.
     *
     * @return null if no such component is found.
     */
    public Inhabitant<?> getInhabitantByAnnotation(
            Class<? extends Annotation> contract, String name)
            throws ComponentException {
        throw new UnsupportedOperationException("getInhabitantByAnnotation(" + contract + "," + name + ") in Habitat");
    }

    /**
     * Gets all the inhabitants for a spcial contract.
     *
     * FOR COMPATIBILITY REASONS
     *
     * @param type the contract type
     * @param <T> the parameterized type
     * @return
     */
    public <T> Collection<Inhabitant<? extends T>> getInhabitants(Class<T> type) {
        throw new UnsupportedOperationException("getInhabitants(" + type + ") in Habitat");
    }

    /**
     * Gets all the inhabitants that has the given contract.
     */
    public <T> Collection<Inhabitant<T>> getInhabitantsByContract(
            Type contract) throws ComponentException {
        throw new UnsupportedOperationException("getInhabitantsByContract(" + contract + ") in Habitat");
    }

    @Override
    public <T> T inject(final T object) {
        throw new UnsupportedOperationException("inject(" + object + ") in Habitat");
    }

    /**
     * Instantiate the passed type and injects all the {@link org.jvnet.hk2.annotations.Inject}
     * annotated fields and methods
     *
     * @param type class of the requested instance
     * @param <T> type of the requested instance
     * @return the instantiated and injected instance
     */
    public <T> T inject(Class<T> type) {
        throw new UnsupportedOperationException("inject(" + type + ") in Habitat");
    }

    /**
     * Gets all the inhabitants that has the given implementation type.
     */
    public <T> Collection<Inhabitant<T>> getInhabitantsByType(Class<T> implType)
            throws ComponentException {
        throw new UnsupportedOperationException("getInhabitantsByType(" + implType + ") in Habitat");
    }

    /**
     * Gets all the inhabitants that has the given implementation type name.
     */
    public Collection<Inhabitant<?>> getInhabitantsByType(
            String fullyQualifiedClassName) {
        throw new UnsupportedOperationException("getInhabitantsByType(" + fullyQualifiedClassName + ") in Habitat");
    }

    /**
     * Get the first inhabitant by contract
     *
     * @param typeName fullyQualifiedClassName
     * @return
     */
    public Inhabitant<?> getInhabitantByContract(String typeName) {
        throw new UnsupportedOperationException("getInhabitantsByContract(" + typeName + ") in Habitat");
    }

    public Collection<Inhabitant<?>> getInhabitantsByContract(
            String fullyQualifiedClassName) {
        throw new UnsupportedOperationException("getInhabitantsByContract(" + fullyQualifiedClassName + ") in Habitat");
    }

    public Iterator<String> getAllContracts() {
        throw new UnsupportedOperationException("getAllContracts in Habitat");
    }

    public Iterator<String> getAllTypes() {
        throw new UnsupportedOperationException("getAllTypes in Habitat");
    }

    public Inhabitant getInhabitantByContract(String fullyQualifiedName,
                                              String name) {
        throw new UnsupportedOperationException("getInhabitantByContract(" + fullyQualifiedName + "," + name + ") in Habitat");
    }

    /**
     * Gets all the inhabitants that has the given contract and the given name
     * <p/>
     * <p/>
     * This method defers the actual instantiation of the component until
     * {@link Inhabitant#get()} is invoked.
     *
     * @return Can be empty but never null.
     */
    public <T> Iterable<Inhabitant<? extends T>> getInhabitants(
            Class<T> contract, String name) throws ComponentException {
        throw new UnsupportedOperationException("getInhabitants(" + contract + "," + name + ") in Habitat");
    }

    /**
     * Gets all the inhabitants that has the given contract annotation and the
     * given name.
     * <p/>
     * <p/>
     * This method defers the actual instantiation of the component until
     * {@link Inhabitant#get()} is invoked.
     *
     * @return Can be empty but never null.
     */
    public Iterable<Inhabitant<?>> getInhabitantsByAnnotation(
            Class<? extends Annotation> contract, String name)
            throws ComponentException {
        throw new UnsupportedOperationException("getInhabitantsByAnnotation(" + contract + "," + name + ") in Habitat");
    }

    @Override
    public <T> T getByType(Class<T> implType) {
        throw new UnsupportedOperationException("getByType(" + implType + ") in Habitat");
    }

    @Override
    public <T> T getByType(String implType) {
        throw new UnsupportedOperationException("getByType(" + implType + ") in Habitat");
    }

    @Override
    public <T> Inhabitant<T> getProvider(Type type, String name) {
        throw new UnsupportedOperationException("getByType(" + type + "," + name + ") in Habitat");
    }

    /**
     * Gets the object that has the given contract.
     * <p/>
     * <p/>
     * If there are more than one of them, this method arbitrarily return one of
     * them.
     */
    public <T> T getByContract(Class<T> contractType) {
        throw new UnsupportedOperationException("getByContract(" + contractType + ") in Habitat");
    }

    public <T> T getByContract(String contractType) {
        throw new UnsupportedOperationException("getByContract(" + contractType + ") in Habitat");
    }

    /**
     * Releases all the components. Should be called for orderly shut-down of
     * the system.
     * <p/>
     * TODO: more javadoc needed
     */
    public void release() {
        throw new UnsupportedOperationException("release in Habitat");
        
    }
}
