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
package org.glassfish.hk2.utilities;

import java.lang.reflect.Type;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.PerThread;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.internal.PerThreadContext;

/**
 * This is a set of useful utilities for working with {@link ServiceLocator}.
 *
 * @author jwells
 */
public abstract class ServiceLocatorUtilities {
    private final static String DEFAULT_LOCATOR_NAME = "default";

    /**
     * This method will add the ability to use the {@link PerThread} scope to
     * the given locator.  If the locator already has a {@link Context} implementation
     * that handles the {@link PerThread} scope this method does nothing.
     *
     * @param locator The non-null locator to enable the PerThread scope on
     * @throws MultiException if there were errors when committing the service
     */
    public static void enablePerThreadScope(ServiceLocator locator) {
        Context<PerThread> perThreadContext = locator.getService((new TypeLiteral<Context<PerThread>>() {}).getType());
        if (perThreadContext != null) return;

        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        final DescriptorImpl descriptor = BuilderHelper.link(PerThreadContext.class).
                to(Context.class).
                in(Singleton.class.getName()).
                build();

        ClassLoader loader = ServiceLocatorUtilities.class.getClassLoader();
        final ClassLoader binderClassLoader = loader == null ? ClassLoader.getSystemClassLoader() : loader;
        descriptor.setLoader(new HK2Loader() {
            @Override
            public Class<?> loadClass(String className) throws MultiException {
                try {
                    return binderClassLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new MultiException(e);
                }

            }
        });

        config.bind(descriptor);
        config.commit();
    }

    /**
     * This method will bind all of the binders given together in a
     * single config transaction.
     *
     * @param locator The non-null locator to use for the configuration action
     * @param binders The non-null list of binders to be added to the locator
     * @throws MultiException if any error was encountered while binding services
     */
    public static void bind(ServiceLocator locator, Binder... binders) {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        for (Binder binder : binders) {
            binder.bind(config);
        }

        config.commit();
    }

    /**
     * This method will create or find a ServiceLocator with the given name and
     * bind all of the binders given together in a single config transaction.
     *
     * @param name The non-null name of the locator to use for the configuration action
     * @param binders The non-null list of binders to be added to the locator
     * @return The service locator that was either found or created
     * @throws MultiException if any error was encountered while binding services
     */
    public static ServiceLocator bind(String name, Binder... binders) {
        ServiceLocatorFactory factory = ServiceLocatorFactory.getInstance();

        ServiceLocator locator = factory.create(name);
        bind(locator, binders);

        return locator;
    }

    /**
     * This method will create or find a ServiceLocator with the name "default" and
     * bind all of the binders given together in a single config transaction.
     *
     * @param binders The non-null list of binders to be added to the locator
     * @return The service locator that was either found or created
     * @throws MultiException if any error was encountered while binding services
     */
    public static ServiceLocator bind(Binder... binders) {
        return bind(DEFAULT_LOCATOR_NAME, binders);
    }
    
    /**
     * This method adds one existing object to the given service locator.  The caller
     * of this will not get a chance to customize the descriptor that goes into the
     * locator, and hence must rely completely on the analysis of the system to determine
     * the set of contracts and metadata associated with the descriptor.  The same algorithm
     * is used in this method as in the {@link BuilderHelper#createConstantDescriptor(Object)}
     * method.
     * 
     * @param locator The non-null locator to add this descriptor to
     * @param constant The non-null constant to add to the service locator
     * @return The descriptor that was added to the service locator
     */
    public static <T> ActiveDescriptor<T> addOneConstant(ServiceLocator locator, Object constant) {
        if (locator == null || constant == null) throw new IllegalArgumentException();
        
        return addOneDescriptor(locator, BuilderHelper.createConstantDescriptor(constant));
    }
    
    /**
     * This method adds one existing object to the given service locator.  The caller
     * of this will not get a chance to customize the descriptor that goes into the
     * locator, and hence must rely completely on the analysis of the system to determine
     * the set of contracts and metadata associated with the descriptor.  The same algorithm
     * is used in this method as in the {@link BuilderHelper#createConstantDescriptor(Object)}
     * method.
     * 
     * @param locator The non-null locator to add this descriptor to
     * @param constant The non-null constant to add to the service locator
     * @param name The name this descriptor should take (may be null)
     * @param contracts The full set of contracts this descriptor should take
     * @return The descriptor that was added to the service locator
     */
    public static <T> ActiveDescriptor<T> addOneConstant(ServiceLocator locator, Object constant, String name, Type... contracts) {
        if (locator == null || constant == null) throw new IllegalArgumentException();
        
        return addOneDescriptor(locator, BuilderHelper.createConstantDescriptor(constant, name, contracts));
    }

    /**
     * It is very often the case that one wishes to add a single descriptor to
     * a service locator.  This method adds that one descriptor.  If the descriptor
     * is an {@link ActiveDescriptor} and is reified, it will be added as an
     * {@link ActiveDescriptor}.  Otherwise it will be bound as a {@link Descriptor}.
     *
     * @param locator The non-null locator to add this descriptor to
     * @param descriptor The non-null descriptor to add to this locator
     * @throws MultiException On a commit failure
     */
    @SuppressWarnings("unchecked")
    public static <T> ActiveDescriptor<T> addOneDescriptor(ServiceLocator locator, Descriptor descriptor) {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        ActiveDescriptor<T> retVal;
        if (descriptor instanceof ActiveDescriptor) {
            ActiveDescriptor<T> active = (ActiveDescriptor<T>) descriptor;

            if (active.isReified()) {
                retVal = config.addActiveDescriptor(active);
            }
            else {
                retVal = config.bind(descriptor);
            }

        }
        else {
            retVal = config.bind(descriptor);
        }

        config.commit();

        return retVal;
    }
    
    /**
     * This method will attempt to remove descriptors matching the passed in descriptor from
     * the given locator.  If the descriptor has its locatorId and serviceId values set then
     * only a descriptor matching those exact locatorId and serviceId will be removed.  Otherwise
     * any descriptor that returns true from the {@link DescriptorImpl#equals(Object)} method
     * will be removed from the locator.  Note that if more than one descriptor matches they
     * will all be removed.  Hence more than one descriptor may be removed by this method.
     * 
     * @param locator The non-null locator to remove the descriptor from
     * @param descriptor The non-null descriptor to remove from the locator
     */
    public static void removeOneDescriptor(ServiceLocator locator, Descriptor descriptor) {
        if (locator == null || descriptor == null) throw new IllegalArgumentException();
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        if (descriptor.getLocatorId() != null && descriptor.getServiceId() != null) {
            Filter destructionFilter = BuilderHelper.createSpecificDescriptorFilter(descriptor);
            
            config.addUnbindFilter(destructionFilter);
            
            config.commit();
            
            return;
        }
        
        // Must use second algorithm, which is not as precise, but which still mainly works
        final DescriptorImpl di;
        if (descriptor instanceof DescriptorImpl) {
            di = (DescriptorImpl) descriptor;
        }
        else {
            di = new DescriptorImpl(descriptor);
        }
        
        Filter destructionFilter = new Filter() {

            @Override
            public boolean matches(Descriptor d) {
                return di.equals(d);
            }
            
        };
        
        config.addUnbindFilter(destructionFilter);
        
        config.commit();
    }
    
    /**
     * Removes all the descriptors from the given locator that match the
     * given filter
     * 
     * @param locator The non-null locator to remove the descriptors from
     * @param filter The non-null filter which will determine what descriptors to remove
     */
    public static void removeFilter(ServiceLocator locator, Filter filter) {
        if (locator == null || filter == null) throw new IllegalArgumentException();
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.addUnbindFilter(filter);
        
        config.commit();
    }
}
