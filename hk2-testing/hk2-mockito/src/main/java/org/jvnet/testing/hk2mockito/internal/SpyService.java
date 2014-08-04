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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.security.AccessController;
import static java.security.AccessController.doPrivileged;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import static org.glassfish.hk2.api.InjectionResolver.SYSTEM_RESOLVER_NAME;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.InjecteeImpl;
import org.glassfish.hk2.utilities.NamedImpl;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import static org.glassfish.hk2.utilities.reflection.ReflectionHelper.getQualifierAnnotations;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.testing.hk2mockito.HK2MockitoSpyInjectionResolver;
import org.jvnet.testing.hk2mockito.MC;
import org.jvnet.testing.hk2mockito.SC;
import org.jvnet.testing.hk2mockito.SUT;
import org.mockito.MockSettings;
import static org.mockito.Mockito.withSettings;

/**
 *
 * A helper service for creating SUT, SC, MC or regular service.
 *
 * @author Sharmarke Aden
 */
@Service
public class SpyService {

    private final MemberCache memberCache;
    private final ParentCache parentCache;
    private final ObjectFactory objectFactory;
    private final IterableProvider<InjectionResolver> resolvers;
    private final InjectionResolver<Inject> systemResolver;
    private final ServiceLocator locator;

    @Inject
    SpyService(MemberCache memberCache,
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

    public Object resolve(Injectee injectee, ServiceHandle<?> root) {
        Object result = null;

        for (InjectionResolver resolver : resolvers) {

            if (resolver.getClass().isAssignableFrom(HK2MockitoSpyInjectionResolver.class)) {
                continue;
            }

            result = resolver.resolve(injectee, root);

            if (result != null) {
                break;
            }
        }

        return result;
    }

    public Object findOrCreateSUT(SUT sut, Injectee injectee, ServiceHandle<?> root) {
        Member member = (Member) injectee.getParent();
        Type parentType = member.getDeclaringClass();
        prime((Class) parentType);

        Object service;

        if (sut.value()) {
            service = objectFactory.newSpy(resolve(injectee, root));
        } else {
            service = resolve(injectee, root);
        }

        return service;
    }

    public Object createOrFindService(Injectee injectee, ServiceHandle<?> root) {
        Member member = (Member) injectee.getParent();
        Class<?> parentType = member.getDeclaringClass();
        Type requiredType = injectee.getRequiredType();
        Object service;

        if (InjectionResolver.class.isAssignableFrom(parentType)) {
            service = systemResolver.resolve(injectee, root);
        } else {
            service = resolve(injectee, root);
        }

        Type serviceParent = parentCache.get(parentType);

        if (serviceParent == null) {
            return service;
        }

        Map<SpyCacheKey, Object> cache = memberCache.get(serviceParent);

        if (cache == null) {
            return service;
        }

        SpyCacheKey key;

        if (member instanceof Field) {
            key = objectFactory.newKey(requiredType, member.getName());
        } else {
            key = objectFactory.newKey(requiredType, injectee.getPosition());
        }

        return cache.get(key);
    }

    public Object findOrCreateCollaborator(int position,
            String fieldName,
            Injectee injectee,
            ServiceHandle<?> root) {
        Member member = (Member) injectee.getParent();
        Type parentType = member.getDeclaringClass();
        Type requiredType = injectee.getRequiredType();
        Map<SpyCacheKey, Object> cache = prime((Class) parentType);

        SpyCacheKey cacheKey;
        if (member instanceof Field) {
            cacheKey = objectFactory.newKey(requiredType, position);
        } else {
            String field = fieldName;
            if ("".equals(field)) {
                field = member.getName();
            }
            cacheKey = objectFactory.newKey(requiredType, field);
        }

        return cache.get(cacheKey);
    }

    private Map<SpyCacheKey, Object> prime(final Class parentType) {
        Map<SpyCacheKey, Object> cache = memberCache.get(parentType);

        if (cache != null) {
            return cache;
        }

        cache = memberCache.add(parentType);

        Field[] fields = doPrivileged(new PrivilegedAction<Field[]>() {

            @Override
            public Field[] run() {
                return parentType.getDeclaredFields();
            }

        });
        if (fields != null) {

            for (Field field : fields) {
                String name = field.getName();
                Class<?> fieldType = field.getType();
                SC sc = field.getAnnotation(SC.class);
                MC mc = field.getAnnotation(MC.class);

                if (sc != null) {
                    Set<Annotation> qualifiers = getQualifiers(field);

                    InjecteeImpl injectee = new InjecteeImpl(field.getGenericType());
                    injectee.setPosition(sc.value());
                    injectee.setRequiredQualifiers(qualifiers);
                    injectee.setParent(field.getDeclaringClass());

                    Object service = resolve(injectee, null);
                    if (service != null) {

                        SpyCacheKey executableKey = objectFactory.newKey(fieldType, sc.value());
                        String fieldName = sc.field();
                        if ("".equals(fieldName)) {
                            fieldName = name;
                        }
                        SpyCacheKey fieldKey = objectFactory.newKey(fieldType, fieldName);

                        Object spy = objectFactory.newSpy(service);
                        cache.put(executableKey, spy);
                        cache.put(fieldKey, spy);
                    }

                } else if (mc != null) {
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

                    Object service = objectFactory.newMock(fieldType, settings);

                    SpyCacheKey executableKey = objectFactory.newKey(fieldType, mc.value());
                    String fieldName = mc.field();
                    if ("".equals(fieldName)) {
                        fieldName = name;
                    }
                    SpyCacheKey fieldKey = objectFactory.newKey(fieldType, fieldName);

                    cache.put(executableKey, service);
                    cache.put(fieldKey, service);
                }

            }
        }
        return cache;
    }

    private Set<Annotation> getQualifiers(Field field) {
        Named named = field.getAnnotation(Named.class);
        Set<Annotation> qualifiers = getQualifierAnnotations(field);
        Set<Annotation> annotations = new HashSet<Annotation>(qualifiers.size() + 1);

        if (named != null) {
            annotations.add(new NamedImpl(named.value()));
        }

        annotations.addAll(qualifiers);

        return annotations;
    }

}
