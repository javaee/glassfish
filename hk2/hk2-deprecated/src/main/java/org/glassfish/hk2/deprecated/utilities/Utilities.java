/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.deprecated.utilities;


import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.internal.IndexedFilterImpl;
import org.glassfish.hk2.utilities.AliasDescriptor;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.deprecated.internal.InhabitantImpl;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Core utilities.
 *
 * @author tbeerbower
 */
@Deprecated
public class Utilities {

    // ----- Constants ------------------------------------------------------

    private static final HashSet<String> EMPTY_QUALIFIERS = new HashSet<String>();


    // ----- Utilities ------------------------------------------------------

    /**
     * Bind the given descriptor to the given service locator.
     *
     * @param locator    the service locator to bind to
     * @param descriptor the descriptor that we are adding
     */
    public static ActiveDescriptor<?> add(ServiceLocator locator, Descriptor descriptor) {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        ActiveDescriptor<?> activeDescriptor = config.bind(descriptor);
        config.commit();

        return activeDescriptor;
    }

    /**
     * Add an alternate index to look up the given descriptor.
     *
     * @param locator    the service locator to associate this index with
     * @param descriptor the descriptor that we are adding the index for
     * @param contract   the contract for the index
     * @param name       the name for the index
     * @param <T>        the descriptor type
     */
    public static <T> void addIndex(ServiceLocator locator,
                                    ActiveDescriptor<T> descriptor,
                                    String contract,
                                    String name) {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        config.addActiveDescriptor(new AliasDescriptor<T>(locator, descriptor, contract, name));
        config.commit();
    }

    /**
     * Unbind the descriptor(s) found by the given filter from the given service
     * locator.
     *
     * @param locator the service locator
     * @param filter  the filter used to find descriptor(s) that we are
     *                unbinding
     * @return true if any descriptor(s) could be found using the given
     *         filter; false otherwise
     */
    public static boolean remove(ServiceLocator locator, Filter filter) {

        if (locator.getBestDescriptor(filter) == null) {
            return false;
        }

        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        config.addUnbindFilter(filter);
        config.commit();
        return true;
    }

    /**
     * Create a descriptor from the given type name.
     *
     * @param typeName the type name
     * @param cl       the loader
     * @param metadata the metadata
     *
     * @return a new descriptor
     */
    public static DescriptorImpl createDescriptor(
            String typeName,
            HK2Loader cl,
            Map<String, List<String>> metadata) {
        return new DescriptorImpl(
                Collections.singleton(typeName),
                null,
                null,
                typeName,
                metadata,
                EMPTY_QUALIFIERS,
                DescriptorType.CLASS,
                cl,
                0,
                null,
                null,
                null);
    }

    /**
     * Returns the type closure for the given contract.
     *
     * @param ofType   the type to check
     * @param contract the contract this type is allowed to handle
     * @return the type closure restricted to the contract; null if the
     *         given type does not implement the given contract
     */
    protected static Type getTypeClosure(Type ofType, String contract) {
        Set<Type> contractTypes = ReflectionHelper.getTypeClosure(
                ofType, Collections.singleton(contract));

        Iterator<Type> iterator = contractTypes.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Creates an {@link Inhabitant} from an {@link ActiveDescriptor}.
     *
     * @param fromMe  the {@link Descriptor} to turn into an {@link Inhabitant}
     * @param locator the locator to use for the {@link Inhabitant}
     *
     * @return an {@link Inhabitant}
     */
    public static <T> Inhabitant<T> getInhabitantFromActiveDescriptor(ActiveDescriptor<T> fromMe,
                                                                      ServiceLocator locator) {
        if (fromMe == null) return null;

        org.glassfish.hk2.api.Descriptor original = fromMe.getBaseDescriptor();
        if (original != null && (original instanceof Inhabitant)) {
            return (Inhabitant<T>) original;
        }

        return new InhabitantImpl<T>(fromMe, locator);
    }

    /**
     * Get the best descriptor for the given class and name.
     *
     * @param locator  the service locator
     * @param type     the type
     * @param name     the name
     *
     * @return a descriptor that matches the given type and name
     */
    public static Descriptor getDescriptor(ServiceLocator locator,
                                           final Class type,
                                           final String name) {
        return locator.getBestDescriptor(
                new IndexedFilterImpl(type.getName(), name));
    }

    /**
     * Get all of the descriptors for the given contract type.
     *
     * @param locator  the service locator
     * @param type     the contract type
     *
     * @return a list of descriptors for the given contract type
     */
    public static List<? extends Descriptor> getAllDescriptorsByContract(ServiceLocator locator,
                                                                         final Class type) {
        return locator.getDescriptors(new Filter() {
            public boolean matches(final Descriptor d) {
                return d.getAdvertisedContracts().contains(type.getName());
            }
        });
    }

    /**
     * Get the service from the given service locator for the given descriptor.
     *
     * @param locator     the service locator
     * @param descriptor  the descriptor
     * @param <T>         the type of the service
     *
     * @return the service; null if no binding matching the given descriptor
     *         can be found through the given service locator
     */
    public <T> T getService(ServiceLocator locator, final Descriptor descriptor) {
        final Filter filter = new Filter() {
            @Override
            public boolean matches(Descriptor d) {
                return descriptor.equals(d);
            }
        };

        ActiveDescriptor<T> bestDescriptor =
                (ActiveDescriptor<T>) locator.getBestDescriptor(filter);

        return bestDescriptor == null ?
                null :
                locator.getServiceHandle(bestDescriptor).getService();
    }
}
