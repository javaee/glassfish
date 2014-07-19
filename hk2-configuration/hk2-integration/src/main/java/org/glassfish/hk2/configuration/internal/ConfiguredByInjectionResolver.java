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
package org.glassfish.hk2.configuration.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.configuration.api.Configured;

/**
 * @author jwells
 *
 */
@Singleton
public class ConfiguredByInjectionResolver implements
        InjectionResolver<Configured> {
    @Inject @Named(InjectionResolver.SYSTEM_RESOLVER_NAME)
    private InjectionResolver<Inject> systemResolver;
    
    @Inject
    private ConfiguredByContext context;
    
    private final ConcurrentHashMap<ActiveDescriptor<?>, BeanInfo> beanMap = new ConcurrentHashMap<ActiveDescriptor<?>, BeanInfo>(); 
    
    private static String getParameterNameFromConstructor(Constructor<?> cnst, int position) {
        Annotation paramAnnotations[] = cnst.getParameterAnnotations()[position];
        
        Configured c = null;
        for (Annotation anno : paramAnnotations) {
            if (Configured.class.equals(anno.annotationType())) {
                c = (Configured) anno;
                break;
            }
        }
        if (c == null) return null;
        
        String key = c.value();
        if (BeanUtilities.isEmpty(key)) {
            throw new AssertionError("Not enough in @Configured annotation in constructor " + cnst + " at parameter index " + position);
        }
        
        return key;
    }
    
    private static String getParameterNameFromMethod(Method method, int position) {
        Annotation paramAnnotations[] = method.getParameterAnnotations()[position];
        
        Configured c = null;
        for (Annotation anno : paramAnnotations) {
            if (Configured.class.equals(anno.annotationType())) {
                c = (Configured) anno;
                break;
            }
        }
        if (c == null) return null;
        
        String key = c.value();
        if (BeanUtilities.isEmpty(key)) {
            throw new AssertionError("Not enough in @Configured annotation in method " + method + " at parameter index " + position);
        }
        
        return key;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.InjectionResolver#resolve(org.glassfish.hk2.api.Injectee, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public synchronized Object resolve(Injectee injectee, ServiceHandle<?> root) {
        ActiveDescriptor<?> injecteeParent = injectee.getInjecteeDescriptor();
        if (injecteeParent == null) return systemResolver.resolve(injectee, root);
        
        AnnotatedElement ae = injectee.getParent();
        if (ae == null) return systemResolver.resolve(injectee, root);
        
        String parameterName = null;
        if (ae instanceof Field) {
            parameterName = BeanUtilities.getParameterNameFromField((Field) ae, false);
        }
        else if (ae instanceof Constructor) {
            parameterName = getParameterNameFromConstructor((Constructor<?>) ae, injectee.getPosition());
        }
        else if (ae instanceof Method){
            parameterName = getParameterNameFromMethod((Method) ae, injectee.getPosition());
        }
        else {
            return systemResolver.resolve(injectee, root);
        }
        
        if (parameterName == null) return systemResolver.resolve(injectee, root);
        
        ActiveDescriptor<?> workingOn = context.getWorkingOn();
        if (workingOn == null) return systemResolver.resolve(injectee, root);
        
        BeanInfo beanInfo = beanMap.get(workingOn);
        if (beanInfo == null) {
            throw new IllegalStateException("Could not find a configuration bean for " + injectee + " with descriptor " + workingOn);
        }
        
        return BeanUtilities.getBeanPropertyValue(parameterName, beanInfo);
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
    
    /* package */ synchronized BeanInfo addBean(ActiveDescriptor<?> descriptor, Object bean, String type) {
        BeanInfo retVal = new BeanInfo(type, descriptor.getName(), bean);
        beanMap.put(descriptor, retVal);
        return retVal;
    }
    
    /* package */ synchronized void removeBean(ActiveDescriptor<?> descriptor) {
        beanMap.remove(descriptor);
    }
}
