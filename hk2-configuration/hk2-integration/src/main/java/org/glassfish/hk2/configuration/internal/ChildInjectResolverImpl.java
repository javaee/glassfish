/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.configuration.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.configuration.api.ChildInject;
import org.glassfish.hk2.configuration.api.ChildIterable;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

/**
 * @author jwells
 *
 */
@Singleton
@Visibility(DescriptorVisibility.LOCAL)
public class ChildInjectResolverImpl implements InjectionResolver<ChildInject> {
    @Inject
    private ServiceLocator locator;
    
    @Inject
    private InjectionResolver<Inject> systemResolver;
    
    @Inject
    private ConfiguredByContext configuredByContext;

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.InjectionResolver#resolve(org.glassfish.hk2.api.Injectee, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public Object resolve(Injectee injectee, ServiceHandle<?> root) {
        ActiveDescriptor<?> parentDescriptor = injectee.getInjecteeDescriptor();
        if (parentDescriptor == null) {
            // We give up, ask the normal resolver
            return systemResolver.resolve(injectee, root);
        }
        
        // Need to get the real descriptor, not the seed
        parentDescriptor = configuredByContext.getWorkingOn();
        if (parentDescriptor == null) {
            // We give up, ask the normal resolver
            return systemResolver.resolve(injectee, root);
        }
        
        Type requiredType = injectee.getRequiredType();
        Class<?> requiredClass = ReflectionHelper.getRawClass(requiredType);
        if (requiredClass == null) {
            return systemResolver.resolve(injectee, root);
        }
        
        ChildInject childInject = getInjectionAnnotation(injectee.getParent(), injectee.getPosition());
        String prefixName = parentDescriptor.getName();
        if (prefixName == null) prefixName = "";
        String separator = childInject.separator();
        
        prefixName = prefixName + childInject.value();
        
        if (ChildIterable.class.equals(requiredClass) && (requiredType instanceof ParameterizedType)) {
            ParameterizedType pt = (ParameterizedType) requiredType;
            
            // Replace the required type
            requiredType = pt.getActualTypeArguments()[0];
            requiredClass = ReflectionHelper.getRawClass(requiredType);
            if (requiredClass == null) {
                return systemResolver.resolve(injectee, root);
            }
            
            return new ChildIterableImpl<Object>(locator, requiredType, prefixName, separator);
        }
        
        List<ActiveDescriptor<?>> matches = locator.getDescriptors(new ChildFilter(requiredType, prefixName));
        
        if (matches.isEmpty()) {
            if (injectee.isOptional()) {
                return null;
            }
            
            throw new IllegalStateException("Could not find a child injection point for " + injectee);
        }
        
        return locator.getServiceHandle(matches.get(0)).getService();
    }
    
    private static ChildInject getInjectionAnnotation(AnnotatedElement element, int position) {
        if (element instanceof Field) {
            Field field = (Field) element;
            
            return field.getAnnotation(ChildInject.class);
        }
        
        Annotation annotations[];
        if (element instanceof Constructor) {
            Constructor<?> constructor = (Constructor<?>) element;
            
            annotations = constructor.getParameterAnnotations()[position];
        }
        else if (element instanceof Method) {
            Method method = (Method) element;
            
            annotations = method.getParameterAnnotations()[position];
        }
        else {
            throw new IllegalArgumentException();
        }
        
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(ChildInject.class)) {
                return (ChildInject) annotation;
            }
        }
        
        throw new IllegalArgumentException();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.InjectionResolver#isConstructorParameterIndicator()
     */
    @Override
    public boolean isConstructorParameterIndicator() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.InjectionResolver#isMethodParameterIndicator()
     */
    @Override
    public boolean isMethodParameterIndicator() {
        return true;
    }

}
