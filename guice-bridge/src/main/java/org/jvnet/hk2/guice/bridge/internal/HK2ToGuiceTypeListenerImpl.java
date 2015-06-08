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

package org.jvnet.hk2.guice.bridge.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedList;

import javax.inject.Qualifier;

import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.guice.bridge.api.HK2Inject;
import org.jvnet.hk2.guice.bridge.api.HK2IntoGuiceBridge;

import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * @author jwells
 *
 */
public class HK2ToGuiceTypeListenerImpl implements TypeListener {
    private final ServiceLocator locator;
    
    /**
     * Creates the {@link HK2IntoGuiceBridge} TypeLocator that must
     * be bound into the Module with a call to bindListener.  The
     * ServiceLocator will be consulted at this time for any types
     * Guice cannot find.  If this type is found in the ServiceLocator
     * then that service will be instantiated by hk2
     * 
     * @param locator The non-null locator that should be used to discover
     * services
     */
    public HK2ToGuiceTypeListenerImpl(ServiceLocator locator) {
        this.locator = locator;
    }
    
    private static boolean isQualifier(Annotation anno) {
        Class<? extends Annotation> annoClass = anno.annotationType();
        
        if (annoClass.isAnnotationPresent(Qualifier.class)) return true;
        return false;
        
    }

    /* (non-Javadoc)
     * @see com.google.inject.spi.TypeListener#hear(com.google.inject.TypeLiteral, com.google.inject.spi.TypeEncounter)
     */
    @Override
    public <I> void hear(TypeLiteral<I> literal, TypeEncounter<I> encounter) {
        Class<?> clazz = literal.getRawType();
        
        HashSet<String> dupFinder = new HashSet<String>();
        
        Class<?> walkingClass = clazz;
        while (walkingClass != null) {
            for (Field field : walkingClass.getDeclaredFields()) {
                if (dupFinder.contains(field.getName())) {
                    continue;
                }
                dupFinder.add(field.getName());
                
                if (!field.isAnnotationPresent(HK2Inject.class)) {
                    continue;
                }
                
                LinkedList<Annotation> qualifiers = new LinkedList<Annotation>();
                for (Annotation anno : field.getAnnotations()) {
                    if (!isQualifier(anno)) continue;
                    
                    qualifiers.add(anno);
                }
                
                encounter.register(new HK2FieldInjector<Object>(locator,
                        field.getGenericType(),
                        qualifiers.toArray(new Annotation[qualifiers.size()]),
                        field));
            }
            
            walkingClass = walkingClass.getSuperclass();
        }

    }

    private static class HK2FieldInjector<T> implements MembersInjector<T> {
        private final ServiceLocator locator;
        private final Type requiredType;
        private final Annotation qualifiers[];
        private final Field field;
        
        private HK2FieldInjector(ServiceLocator locator, Type requiredType, Annotation qualifiers[], Field field) {
            this.locator = locator;
            this.requiredType = requiredType;
            this.qualifiers = qualifiers;
            this.field = field;
            
            field.setAccessible(true);
        }

        @Override
        public void injectMembers(T arg0) {
            ServiceHandle<?> handle = locator.getServiceHandle(requiredType, qualifiers);
            if (handle == null) {
                throw new IllegalStateException("Could not find a service of type " +
                    requiredType);
            }
            
            Object injectMe = handle.getService();
            
            try {
                field.set(arg0, injectMe);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            
            
        }
        
    }
}
