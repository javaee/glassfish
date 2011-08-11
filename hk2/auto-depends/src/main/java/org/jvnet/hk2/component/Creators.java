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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.util.List;

import org.jvnet.hk2.annotations.Factory;
import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.annotations.Inject;

import com.sun.hk2.component.ConstructorCreator;
import com.sun.hk2.component.FactoryCreator;
import com.sun.hk2.component.InjectableParametizedConstructorCreator;

/**
 * {@link Creator} factory.
 *
 * @author Kohsuke Kawaguchi
 */                                                                                                             D
public class Creators {
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Creator<T> create(Class<T> c, Habitat habitat, MultiMap<String,String> metadata) {
        Factory f = c.getAnnotation(Factory.class);
        if (f != null) {
            return new FactoryCreator<T>(c,f.value(),habitat,metadata);
        }

        Inhabitant factory = habitat.getInhabitantByAnnotation(FactoryFor.class, c.getName());
        if (factory != null) {
            return new FactoryCreator<T>(c,factory,habitat,metadata);
        }

        Reference<Constructor<T>> defaultCtorRef = new Reference();
        Reference<Constructor<T>> noArgCtorRef = new Reference();
        qualifyingConstructors(c, null, defaultCtorRef, noArgCtorRef);
        if (defaultCtorRef.get() != null) {
            return new InjectableParametizedConstructorCreator<T>(c, defaultCtorRef.get(), habitat, metadata);
        }
        if (noArgCtorRef.get() != null) {
            return new ConstructorCreator<T>(c,habitat,metadata);
        }

        return null;
    }
    
    /**
     * Returns all "eligible" constructors for injection. Eligibility is
     * determined by:
     * 
     * (a) the no-arg constructor, or (b) @Inject annotated constructor.
     * 
     * </p>
     * Note, however, that this does not verify that the arguments on the
     * constructor are indeed injectable out of the habitat.
     * 
     * @param c
     *      The class to find the constructors for
     * @param defaultCtor
     *      (optional) The "favored" default constructor reference; meaning that all arguments can be injected based on the contents of the habitat
     * @param noArgCtor
     *      (optional The no-arg constructor reference
     * @return
     *      The list of all eligible constructors
     */
    @SuppressWarnings("unchecked")
    public static <T> void qualifyingConstructors(Class<T> c,
            List<Constructor<?>> allList,
            Reference<Constructor<T>> defaultCtor,
            Reference<Constructor<T>> noArgCtor) {
        Constructor<T>[] ctors;
        try {
            ctors = (Constructor<T>[]) c.getDeclaredConstructors();
        } catch(Exception e) {
            ctors = (Constructor<T>[]) c.getConstructors();
        }
        
        for (Constructor<T> ctor : ctors) {
            if (0 == ctor.getParameterTypes().length) {
                if (null != allList) {
                    allList.add(ctor);
                }
                if (null != noArgCtor) {
                    noArgCtor.set(ctor);
                }
            } else if (isInjectable(ctor)) {
                if (null != allList) {
                    allList.add(ctor);
                }
                if (null != defaultCtor && null == defaultCtor.get()) {
                    defaultCtor.set(ctor);
                }
            } else {
                boolean allInjectable = true;
                for (Annotation[] a : ctor.getParameterAnnotations()) {
                    if (!isInject(a)) {
                        allInjectable = false;
                        break;
                    }
                }
                if (allInjectable) {
                    if (null != allList) {
                        allList.add(ctor);
                    }
                    if (null != defaultCtor && null == defaultCtor.get()) {
                        defaultCtor.set(ctor);
                    }
                }
            }
        }
    }
    
    private static boolean isInject(Annotation... a) {
        for (Annotation ae : a) {
            if (ae.annotationType().equals(Inject.class) ||
                    ae.annotationType().equals(javax.inject.Inject.class)) {
                return true;
            }
        }
        
        return false;
    }

    public static boolean isInjectable(AnnotatedElement ae) {
        return (null != ae.getAnnotation(Inject.class) ||
                null != ae.getAnnotation(javax.inject.Inject.class));
    }
}
