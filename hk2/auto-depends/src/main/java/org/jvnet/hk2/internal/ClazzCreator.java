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
package org.jvnet.hk2.internal;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.hk2.PostConstruct;
import org.glassfish.hk2.PreDestroy;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;

/**
 * @author jwells
 * @param <T> The type of object this creator creates
 *
 */
public class ClazzCreator<T> implements Creator<T> {
    private final ServiceLocatorImpl locator;
    private final Set<ResolutionInfo> myInitializers = new HashSet<ResolutionInfo>();
    private final Set<ResolutionInfo> myFields = new HashSet<ResolutionInfo>();
    
    private final ResolutionInfo myConstructor;
    private List<Injectee> allInjectees;
    
    private Method postConstructMethod;
    private Method preDestroyMethod;
    
    /* package */ ClazzCreator(ServiceLocatorImpl locator, Class<?> implClass, Collector collector) {
        this.locator = locator;
        List<Injectee> baseAllInjectees = new LinkedList<Injectee>();
        
        AnnotatedElement element;
        List<Injectee> injectees;
        
        element = Utilities.findProducerConstructor(implClass, locator, collector);
        if (element == null) {
            myConstructor = null;
            System.out.println("JRW(10) CC myConstructor=" + myConstructor);
            return;
        }
        
        injectees = Utilities.getConstructorInjectees((Constructor<?>) element);
        if (injectees == null) {
            myConstructor = null;
            System.out.println("JRW(20) CC myConstructor=" + myConstructor);
            return;
        }
        
        baseAllInjectees.addAll(injectees);
        
        myConstructor = new ResolutionInfo(element, injectees);
        
        Set<Method> initMethods = Utilities.findInitializerMethods(implClass, locator, collector);
        for (Method initMethod : initMethods) {
            element = initMethod;
            
            injectees = Utilities.getMethodInjectees(initMethod);
            if (injectees == null) return;
            
            baseAllInjectees.addAll(injectees);
            
            myInitializers.add(new ResolutionInfo(element, injectees));
        }
        
        Set<Field> fields = Utilities.findInitializerFields(implClass, locator, collector);
        for (Field field : fields) {
            element = field;
            
            injectees = Utilities.getFieldInjectees(field);
            if (injectees == null) return;
            
            baseAllInjectees.addAll(injectees);
            
            myFields.add(new ResolutionInfo(element, injectees));
        }
        
        postConstructMethod = Utilities.findPostConstruct(implClass, collector);
        preDestroyMethod = Utilities.findPreDestroy(implClass, collector);
        
        allInjectees = Collections.unmodifiableList(baseAllInjectees);
    }
    
    private Map<Injectee, Object> resolveAllDependencies(ServiceHandle<?> root) throws IllegalStateException {
        Map<Injectee, Object> retVal = new HashMap<Injectee, Object>();
        
        InjectionResolver<?> resolver = Utilities.getInjectionResolver(locator, myConstructor.baseElement);
        for (Injectee injectee : myConstructor.injectees) {
            retVal.put(injectee, resolver.resolve(injectee, root));
        }
        
        for (ResolutionInfo fieldRI : myFields) {
            resolver = Utilities.getInjectionResolver(locator, fieldRI.baseElement);
            for (Injectee injectee : fieldRI.injectees) {
                retVal.put(injectee, resolver.resolve(injectee, root));
            }
        }
        
        for (ResolutionInfo methodRI : myInitializers) {
            resolver = Utilities.getInjectionResolver(locator, methodRI.baseElement);
            for (Injectee injectee : methodRI.injectees) {
                retVal.put(injectee, resolver.resolve(injectee, root));
            }
        }
        
        return retVal;
    }
    
    private Object createMe(Map<Injectee, Object> resolved) throws Throwable {
        Constructor<?> c = (Constructor<?>) myConstructor.baseElement;
        List<Injectee> injectees = myConstructor.injectees;
        
        Object args[] = new Object[injectees.size()];
        for (Injectee injectee : injectees) {
            args[injectee.getPosition()] = resolved.get(injectee);
        }
        
        c.setAccessible(true);
        
        return Utilities.makeMe(c, args);
    }
    
    private void fieldMe(Map<Injectee, Object> resolved, T t) throws Throwable {
        for (ResolutionInfo ri : myFields) {
            Field field = (Field) ri.baseElement;
            List<Injectee> injectees = ri.injectees;  // Should be only one injectee, itself!
            
            Injectee fieldInjectee = null;
            for (Injectee candidate : injectees) {
                fieldInjectee = candidate;
            }
            
            Object putMeIn = resolved.get(fieldInjectee);
            
            field.setAccessible(true);
            
            field.set(t, putMeIn);
        }
    }
    
    private void methodMe(Map<Injectee, Object> resolved, T t) throws Throwable {
        for (ResolutionInfo ri : myInitializers) {
            Method m = (Method) ri.baseElement;
            List<Injectee> injectees = ri.injectees;
            
            Object args[] = new Object[injectees.size()];
            for (Injectee injectee : injectees) {
                args[injectee.getPosition()] = resolved.get(injectee);
            }
            
            m.setAccessible(true);
            
            Utilities.invoke(t, m, args);
        }
    }
    
    private void postConstructMe(T t) throws Throwable {
        if (t == null) return;
        
        if (t instanceof PostConstruct) {
            ((PostConstruct) t).postConstruct();
            return;
        }
        
        if (postConstructMethod == null) return;
        
        postConstructMethod.setAccessible(true);
        Utilities.invoke(t, postConstructMethod, new Object[0]);
    }
    
    private void preDestroyMe(T t) throws Throwable {
        if (t == null) return;
        
        if (t instanceof PreDestroy) {
            ((PreDestroy) t).preDestroy();
            return;
        }
        
        if (preDestroyMethod == null) return;
        
        preDestroyMethod.setAccessible(true);
        Utilities.invoke(t, preDestroyMethod, new Object[0]);
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.internal.Creator#create()
     */
    @SuppressWarnings("unchecked")
    @Override
    public T create(ServiceHandle<?> root) {
        try {
            Map<Injectee, Object> allResolved = resolveAllDependencies(root);
            
            T retVal = (T) createMe(allResolved);
            
            fieldMe(allResolved, retVal);
            
            methodMe(allResolved, retVal);
            
            postConstructMe(retVal);
            
            return retVal;
        }
        catch (Throwable th) {
            if (th instanceof MultiException) {
                throw (MultiException) th;
            }
            
            throw new MultiException(th);
        }
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.internal.Creator#dispose(java.lang.Object)
     */
    @Override
    public void dispose(T instance) {
        try {
            preDestroyMe(instance);
        }
        catch (Throwable th) {
            // ignored
        }

    }
    
    /* (non-Javadoc)
     * @see org.jvnet.hk2.internal.Creator#getInjectees()
     */
    @Override
    public List<Injectee> getInjectees() {
        return allInjectees;
    }
    
    private static class ResolutionInfo {
        private final AnnotatedElement baseElement;
        private final List<Injectee> injectees = new LinkedList<Injectee>();
        
        private ResolutionInfo(AnnotatedElement baseElement, List<Injectee> injectees) {
            this.baseElement = baseElement;
            this.injectees.addAll(injectees);
        }
    }
}
