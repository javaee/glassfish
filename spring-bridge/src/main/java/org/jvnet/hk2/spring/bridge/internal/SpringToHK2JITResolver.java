/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.spring.bridge.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.JustInTimeInjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.springframework.beans.factory.BeanFactory;

/**
 * @author jwells
 *
 */
@Singleton
public class SpringToHK2JITResolver implements JustInTimeInjectionResolver {
    private final ServiceLocator locator;
    private final BeanFactory beanFactory;
    
    /* package */ SpringToHK2JITResolver(ServiceLocator locator, BeanFactory beanFactory) {
        this.locator = locator;
        this.beanFactory = beanFactory;
    }
    
    private void addMe(Class<?> lookForMe, String name, Injectee injectee) {
        HashSet<Type> contracts = new HashSet<Type>();
        contracts.add(injectee.getRequiredType());
        
        Set<Annotation> qualifiers = new HashSet<Annotation>(injectee.getRequiredQualifiers());
        
        SpringServiceHK2Bean<Object> springHK2Bean = new SpringServiceHK2Bean<Object>(
                name,
                contracts,
                qualifiers,
                lookForMe,
                beanFactory);
        
        ServiceLocatorUtilities.addOneDescriptor(locator, springHK2Bean, false);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.JustInTimeInjectionResolver#justInTimeResolution(org.glassfish.hk2.api.Injectee)
     */
    @Override
    public boolean justInTimeResolution(Injectee failedInjectionPoint) {
        Class<?> lookForMe = getClassFromType(failedInjectionPoint.getRequiredType());
        String name = getName(failedInjectionPoint);
        
        try {
            if (name != null) {
                if (beanFactory.containsBean(name) && beanFactory.isTypeMatch(name, lookForMe)) {
                    addMe(lookForMe, name, failedInjectionPoint);
                    return true;
                }
            }
            else {
                if (beanFactory.getBean(lookForMe) != null) {
                    addMe(lookForMe, null, failedInjectionPoint);
                    return true;
                }
                
            }
        }
        catch (Throwable th) {
            return false;
        }
        
        return false;
    }
    
    private static String getName(Injectee injectee) {
        for (Annotation anno : injectee.getRequiredQualifiers()) {
            if (Named.class.equals(anno.annotationType())) {
                Named named = (Named) anno;
                
                return named.value();
            }
            
        }
        
        return null;
    }
    
    /**
     * Gets the class from the given type
     * 
     * @param type The type to find the class from
     * @return The class associated with this type, or null
     * if the class cannot be found
     */
    private static Class<?> getClassFromType(Type type) {
        if (type instanceof Class) return (Class<?>) type;
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            
            return (Class<?>) pt.getRawType();
        }
        
        return null;
    }

}
