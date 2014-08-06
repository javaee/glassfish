/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.testing.hk2mockito.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import static java.security.AccessController.doPrivileged;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import static org.glassfish.hk2.api.InjectionResolver.SYSTEM_RESOLVER_NAME;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.internal.SystemInjecteeImpl;
import static org.jvnet.hk2.internal.Utilities.getFieldInjectees;
import org.jvnet.testing.hk2mockito.HK2MockitoInjectionResolver;
import org.jvnet.testing.hk2mockito.MC;
import org.jvnet.testing.hk2mockito.SC;
import org.jvnet.testing.hk2mockito.SUT;
import org.jvnet.testing.hk2mockito.internal.cache.MemberCache;
import org.jvnet.testing.hk2mockito.internal.cache.ParentCache;
import org.mockito.MockSettings;
import static org.mockito.Mockito.withSettings;

/**
 *
 * A helper service for creating SUT, SC, MC or regular service.
 *
 * @author Sharmarke Aden
 */
@Service
public class MockitoService {

    private final MemberCache memberCache;
    private final ParentCache parentCache;
    private final ObjectFactory objectFactory;
    private final IterableProvider<InjectionResolver> resolvers;
    private final InjectionResolver<Inject> systemResolver;
    private final ServiceLocator locator;
    private Class sut;

    @Inject
    MockitoService(MemberCache memberCache,
            ParentCache parentCache,
            ObjectFactory objectFactory,
            ServiceLocator locator,
            IterableProvider<InjectionResolver> resolvers,
            @Named(SYSTEM_RESOLVER_NAME) InjectionResolver systemResolver) {
        this.memberCache = memberCache;
        this.parentCache = parentCache;
        this.objectFactory = objectFactory;
        this.locator = locator;
        this.resolvers = resolvers;
        this.systemResolver = systemResolver;

    }

    /**
     * Given an injectee find the service by iterating through all the resolvers
     * in the system to resolve it. This is necessary to resolve services that
     * are resolved via injection resolvers (assisted injection). Note that this
     * method returns the first resolved service.
     *
     * @param injectee The injection point this value is being injected into
     * @param root The service handle of the root class being created, which
     * should be used in order to ensure proper destruction of associated
     * &64;PerLookup scoped objects. This can be null in the case that this is
     * being used for an object not managed by HK2. This will only happen if
     * this object is being created with the create method of ServiceLocator.
     * @return A possibly null value to be injected into the given injection
     * point
     */
    public Object resolve(Injectee injectee, ServiceHandle<?> root) {
        Member member = (Member) injectee.getParent();
        Class<?> parentType = member.getDeclaringClass();

        //if the injectee an instance of InjectionResolver delegate it to the 
        //system resolver
        if (InjectionResolver.class.isAssignableFrom(parentType)) {
            return systemResolver.resolve(injectee, root);
        }

        for (InjectionResolver resolver : resolvers) {

            //ignore mockito inection resolver so we don't get into an infinit loop
            if (resolver instanceof HK2MockitoInjectionResolver) {
                continue;
            }

            Object service = resolver.resolve(injectee, root);

            //return the first resolved service
            if (service != null) {
                return service;
            }
        }

        return null;
    }

    /**
     * Given an SUT annotation and an injectee resolve the service associated
     * with the injectee and use the metadata in the SUT annotation to possibly
     * create a mockito spy.
     *
     * @param sut annotation containing sut config meta-data
     * @param injectee The injection point this value is being injected into
     * @param root The service handle of the root class being created
     * @return the service or a proxy spy of the service
     */
    public Object findOrCreateSUT(SUT sut, Injectee injectee, ServiceHandle<?> root) {
        this.sut = (Class) injectee.getRequiredType();
        Member member = (Member) injectee.getParent();
        Type parentType = member.getDeclaringClass();
        primeCache((Class) parentType);

        Object service;

        if (sut.value()) {
            service = objectFactory.newSpy(resolve(injectee, root));
        } else {
            service = resolve(injectee, root);
        }

        return service;
    }

    /**
     * Given an injectee create and cache or resolve the service associated with
     * it.
     *
     * @param injectee The injection point this value is being injected into
     * @param root The service handle of the root class being created
     * @return the service or a proxy spy or mock of the service
     */
    public Object createOrFindService(Injectee injectee, ServiceHandle<?> root) {
        Member member = (Member) injectee.getParent();
        Class<?> parentType = member.getDeclaringClass();
        Type requiredType = injectee.getRequiredType();
        Object service = resolve(injectee, root);;

        //get the cache for the injectee's parent type. if one is not found that
        //means we are not dealing with a test class instance so return the 
        //resolved service.
        Type serviceParent = parentCache.get(parentType);

        if (serviceParent == null) {
            return service;
        }

        //get the service's parent (the test class) cache. if one is not found 
        //that means the test class didn't contain any inections that required 
        //mocking/spying so we return the original sevrice.
        Map<MockitoCacheKey, Object> cache = memberCache.get(serviceParent);

        if (cache == null) {
            return service;
        }

        // determine the cache key for the service
        MockitoCacheKey key;

        if (member instanceof Field) {
            key = objectFactory.newKey(requiredType, member.getName());
        } else {
            key = objectFactory.newKey(requiredType, injectee.getPosition());
        }

        //get the service from the cache.
        service = cache.get(key);

        //if the service is not found in the cache that means the test class
        //was not injected with services that required mocking or spying.
        //simply resolve these services since they werent created up front.
        if (service == null) {
            service = resolve(injectee, root);
        }

        return service;
    }

    /**
     * Given metadata about collborator an an injectee create or resolve the
     * collaborating service.
     *
     * @param position method or constructor the parameter position metadata
     * @param fieldName field name metadata
     * @param injectee The injection point this value is being injected into
     * @param root The service handle of the root class being created
     * @return the service or a proxy spy or mock of the service
     */
    public Object findOrCreateCollaborator(int position,
            String fieldName,
            Injectee injectee,
            ServiceHandle<?> root) {
        Member member = (Member) injectee.getParent();
        Type parentType = member.getDeclaringClass();
        Type requiredType = injectee.getRequiredType();

        //prime the cache for the test class.
        Map<MockitoCacheKey, Object> cache = primeCache((Class) parentType);

        //get the service from the cache.
        MockitoCacheKey key;
        if (member instanceof Field) {
            key = objectFactory.newKey(requiredType, position);
        } else {
            key = objectFactory.newKey(requiredType, getFieldName(fieldName, member.getName()));
        }

        return cache.get(key);
    }

    /**
     * Given a class analyze its fields, create services, create mock/spy
     * proxies of found services, and them to the cache.
     *
     * @param type the class that will be analyzed
     * @return a map containing nothing, or services or proxy/spy object
     */
    private Map<MockitoCacheKey, Object> primeCache(final Class type) {
        //if a cache already exists for the given class simply return that cache
        Map<MockitoCacheKey, Object> cache = memberCache.get(type);

        if (cache != null) {
            return cache;
        }
        
        //add the type to the cache
        cache = memberCache.add(type);

        Field[] fields = doPrivileged(new PrivilegedAction<Field[]>() {

            @Override
            public Field[] run() {
                return type.getDeclaredFields();
            }

        });

        //iterate over all the fields in the class
        for (Field field : fields) {
            String name = field.getName();
            Class<?> fieldClass = field.getType();
            Type fieldType = field.getGenericType();

            SC sc = field.getAnnotation(SC.class);
            MC mc = field.getAnnotation(MC.class);

            if (sc != null) {
                //if we are dealing with spy collaborator then we create an injectee 
                //for it and resolve that injectee
                List<SystemInjecteeImpl> injectees = getFieldInjectees(field, null);

                Object service = resolve(injectees.get(0), null);

                if (service != null) {
                    //if we found the service then we create two entries for it
                    //in the cache. one for field injection and another for
                    //method injection

                    MockitoCacheKey executableKey = objectFactory.newKey(fieldType, sc.value());
                    MockitoCacheKey fieldKey = objectFactory.newKey(fieldType, getFieldName(sc.field(), name));

                    Object spy = objectFactory.newSpy(service);
                    cache.put(executableKey, spy);
                    cache.put(fieldKey, spy);
                }

            } else if (mc != null) {
                //if we are dealing with a mock collaborator then get all the
                //metadata associated with the mock and create a mock of the 
                //service and add it to the cache twice. one for field injection
                //and one for method injection.
                Class<?>[] interfaces = mc.extraInterfaces();
                String mockName = mc.name();

                if ("".equals(mockName)) {
                    mockName = name;
                }

                MockSettings settings = withSettings()
                        .name(mockName)
                        .defaultAnswer(mc.answer().get());

                if (interfaces != null && interfaces.length > 0) {
                    settings.extraInterfaces(mc.extraInterfaces());
                }

                Object service = objectFactory.newMock(fieldClass, settings);

                MockitoCacheKey executableKey = objectFactory.newKey(fieldClass, mc.value());
                MockitoCacheKey fieldKey = objectFactory.newKey(fieldClass, getFieldName(mc.field(), name));

                cache.put(executableKey, service);
                cache.put(fieldKey, service);
            }
        }

        return cache;
    }

    private String getFieldName(String fieldName, String defaultName) {
        if ("".equals(fieldName)) {
           return defaultName;
        }
        
        return fieldName;
    }

}
