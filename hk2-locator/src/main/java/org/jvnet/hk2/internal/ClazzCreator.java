/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ClassAnalyzer;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.InstanceLifecycleEventType;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

/**
 * @author jwells
 * @param <T> The type of object this creator creates
 *
 */
public class ClazzCreator<T> implements Creator<T> {
    private final ServiceLocatorImpl locator;
    private final Class<?> implClass;
    private final Set<ResolutionInfo> myInitializers = new LinkedHashSet<ResolutionInfo>();
    private final Set<ResolutionInfo> myFields = new LinkedHashSet<ResolutionInfo>();
    private ActiveDescriptor<?> selfDescriptor;

    private ResolutionInfo myConstructor;
    private List<SystemInjecteeImpl> allInjectees;

    private Method postConstructMethod;
    private Method preDestroyMethod;

    /* package */ ClazzCreator(ServiceLocatorImpl locator,
            Class<?> implClass) {
        this.locator = locator;
        this.implClass = implClass;
    }

    /* package */ void initialize(
            ActiveDescriptor<?> selfDescriptor,
            String analyzerName,
            Collector collector) {
        this.selfDescriptor = selfDescriptor;

        if ((selfDescriptor != null) &&
                selfDescriptor.getAdvertisedContracts().contains(
                ClassAnalyzer.class.getName())) {
            String descriptorAnalyzerName = selfDescriptor.getName();
            if (descriptorAnalyzerName == null) descriptorAnalyzerName = locator.getDefaultClassAnalyzerName();

            String incomingAnalyzerName = analyzerName;
            if (incomingAnalyzerName == null) incomingAnalyzerName = locator.getDefaultClassAnalyzerName();

            if (descriptorAnalyzerName.equals(incomingAnalyzerName)) {
                collector.addThrowable(new IllegalArgumentException(
                        "The ClassAnalyzer named " + descriptorAnalyzerName +
                        " is its own ClassAnalyzer. Ensure that an implementation of" +
                        " ClassAnalyzer is not its own ClassAnalyzer"));
                myConstructor = null;
                return;
            }
        }

        ClassAnalyzer analyzer = Utilities.getClassAnalyzer(locator, analyzerName, collector);
        if (analyzer == null) {
            myConstructor = null;
            return;
        }

        List<SystemInjecteeImpl> baseAllInjectees = new LinkedList<SystemInjecteeImpl>();

        AnnotatedElement element;
        List<SystemInjecteeImpl> injectees;

        element = Utilities.getConstructor(implClass, analyzer, collector);
        if (element == null) {
            myConstructor = null;
            return;
        }

        injectees = Utilities.getConstructorInjectees((Constructor<?>) element, selfDescriptor);
        if (injectees == null) {
            myConstructor = null;
            return;
        }

        baseAllInjectees.addAll(injectees);

        myConstructor = new ResolutionInfo(element, injectees);

        Set<Method> initMethods = Utilities.getInitMethods(implClass, analyzer, collector);
        for (Method initMethod : initMethods) {
            element = initMethod;

            injectees = Utilities.getMethodInjectees(implClass, initMethod, selfDescriptor);
            if (injectees == null) return;

            baseAllInjectees.addAll(injectees);

            myInitializers.add(new ResolutionInfo(element, injectees));
        }

        Set<Field> fields = Utilities.getInitFields(implClass, analyzer, collector);
        for (Field field : fields) {
            element = field;

            injectees = Utilities.getFieldInjectees(implClass, field, selfDescriptor);
            if (injectees == null) return;

            baseAllInjectees.addAll(injectees);

            myFields.add(new ResolutionInfo(element, injectees));
        }

        postConstructMethod = Utilities.getPostConstruct(implClass, analyzer, collector);
        preDestroyMethod = Utilities.getPreDestroy(implClass, analyzer, collector);

        allInjectees = Collections.unmodifiableList(baseAllInjectees);

        Utilities.validateSelfInjectees(selfDescriptor, allInjectees, collector);
    }

    /* package */ void initialize(
            ActiveDescriptor<?> selfDescriptor,
            Collector collector) {
        initialize(selfDescriptor, (selfDescriptor == null) ? null :
            selfDescriptor.getClassAnalysisName(), collector);
    }

    /**
     * This is done because sometimes when creating the creator we do not know
     * what the true system descriptor will be
     *
     * @param selfDescriptor The descriptor that should replace our self descriptor
     */
    /* package */ void resetSelfDescriptor(ActiveDescriptor<?> selfDescriptor) {
        this.selfDescriptor = selfDescriptor;

        for (Injectee injectee : allInjectees) {
            if (!(injectee instanceof SystemInjecteeImpl)) continue;

            ((SystemInjecteeImpl) injectee).resetInjecteeDescriptor(selfDescriptor);
        }
    }

    private void resolve(Map<SystemInjecteeImpl, Object> addToMe,
                         InjectionResolver<?> resolver,
                         SystemInjecteeImpl injectee,
                         ServiceHandle<?> root,
                         Collector errorCollection) {
        if (injectee.isSelf()) {
            addToMe.put(injectee, selfDescriptor);
            return;
        }

        Object addIn = null;
        try {
            addIn = resolver.resolve(injectee, root);
        } catch (Throwable th) {
            errorCollection.addThrowable(th);
        }

        if (addIn != null) {
            addToMe.put(injectee, addIn);
        }
    }

    private Map<SystemInjecteeImpl, Object> resolveAllDependencies(final ServiceHandle<?> root) throws MultiException, IllegalStateException {
        Collector errorCollector = new Collector();

        final Map<SystemInjecteeImpl, Object> retVal = new LinkedHashMap<SystemInjecteeImpl, Object>();

        for (SystemInjecteeImpl injectee : myConstructor.injectees) {
            InjectionResolver<?> resolver = locator.getInjectionResolverForInjectee(injectee);
            resolve(retVal, resolver, injectee, root, errorCollector);
        }

        for (ResolutionInfo fieldRI : myFields) {
            for (SystemInjecteeImpl injectee : fieldRI.injectees) {
                InjectionResolver<?> resolver = locator.getInjectionResolverForInjectee(injectee);
                resolve(retVal, resolver, injectee, root, errorCollector);
            }
        }

        for (ResolutionInfo methodRI : myInitializers) {
            for (SystemInjecteeImpl injectee : methodRI.injectees) {
                InjectionResolver<?> resolver = locator.getInjectionResolverForInjectee(injectee);
                resolve(retVal, resolver, injectee, root, errorCollector);
            }
        }

        if (errorCollector.hasErrors()) {
            errorCollector.addThrowable(new IllegalArgumentException("While attempting to resolve the dependencies of "
                    + implClass.getName() + " errors were found"));

            errorCollector.throwIfErrors();
        }

        return retVal;
    }

    private Object createMe(Map<SystemInjecteeImpl, Object> resolved) throws Throwable {
        final Constructor<?> c = (Constructor<?>) myConstructor.baseElement;
        List<SystemInjecteeImpl> injectees = myConstructor.injectees;

        final Object args[] = new Object[injectees.size()];
        for (Injectee injectee : injectees) {
            args[injectee.getPosition()] = resolved.get(injectee);
        }
        
        Utilities.Interceptors interceptors = Utilities.getAllInterceptors(locator, selfDescriptor, implClass, c);
        final Map<Method, List<MethodInterceptor>> methodInterceptors = interceptors.getMethodInterceptors();
        List<ConstructorInterceptor> constructorInterceptors = interceptors.getConstructorInterceptors();
        
        if ((methodInterceptors == null || methodInterceptors.isEmpty()) &&
            ((constructorInterceptors == null) || constructorInterceptors.isEmpty())) {
            // No need for any kind of interception
            return ReflectionHelper.makeMe(c, args, locator.getNeutralContextClassLoader()); 
        }
        
        if (!Utilities.proxiesAvailable()) {
            throw new IllegalStateException("A service " + selfDescriptor + " needs either method or constructor interception, but proxies are not available");
        }
        
        final boolean neutral = locator.getNeutralContextClassLoader();
        
        if (methodInterceptors == null || methodInterceptors.isEmpty()) {
            // No method interceptors means no need for proxy at all
            return ConstructorInterceptorHandler.construct(c, args, neutral, constructorInterceptors);
        }
        
        return ConstructorInterceptorHandler.construct(c,
                args,
                neutral,
                constructorInterceptors,
                new ConstructorActionImpl<T>(this, methodInterceptors));
    }

    private void fieldMe(Map<SystemInjecteeImpl, Object> resolved, T t) throws Throwable {
        for (ResolutionInfo ri : myFields) {
            Field field = (Field) ri.baseElement;
            List<SystemInjecteeImpl> injectees = ri.injectees;  // Should be only one injectee, itself!

            Injectee fieldInjectee = null;
            for (Injectee candidate : injectees) {
                fieldInjectee = candidate;
            }

            Object putMeIn = resolved.get(fieldInjectee);

            ReflectionHelper.setField(field, t, putMeIn);
        }
    }

    private void methodMe(Map<SystemInjecteeImpl, Object> resolved, T t) throws Throwable {
        for (ResolutionInfo ri : myInitializers) {
            Method m = (Method) ri.baseElement;
            List<SystemInjecteeImpl> injectees = ri.injectees;

            Object args[] = new Object[injectees.size()];
            for (Injectee injectee : injectees) {
                args[injectee.getPosition()] = resolved.get(injectee);
            }

            ReflectionHelper.invoke(t, m, args, locator.getNeutralContextClassLoader());
        }
    }

    private void postConstructMe(T t) throws Throwable {
        if (t == null) return;

        if (t instanceof PostConstruct) {
            ((PostConstruct) t).postConstruct();
            return;
        }

        if (postConstructMethod == null) return;

        ReflectionHelper.invoke(t, postConstructMethod, new Object[0], locator.getNeutralContextClassLoader());
    }

    private void preDestroyMe(T t) throws Throwable {
        if (t == null) return;

        if (t instanceof PreDestroy) {
            ((PreDestroy) t).preDestroy();
            return;
        }

        if (preDestroyMethod == null) return;

        ReflectionHelper.invoke(t, preDestroyMethod, new Object[0], locator.getNeutralContextClassLoader());
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.internal.Creator#create()
     */
    @SuppressWarnings("unchecked")
    @Override
    public T create(ServiceHandle<?> root, SystemDescriptor<?> eventThrower) {
        String failureLocation = "resolve";
        try {

            final Map<SystemInjecteeImpl, Object> allResolved = resolveAllDependencies(root);

            if (eventThrower != null) {
                eventThrower.invokeInstanceListeners(new InstanceLifecycleEventImpl(InstanceLifecycleEventType.PRE_PRODUCTION,
                    null, ReflectionHelper.<Map<Injectee,Object>>cast(allResolved), eventThrower));
            }

            failureLocation = "create";
            T retVal = (T) createMe(allResolved);

            failureLocation = "field inject";
            fieldMe(allResolved, retVal);

            failureLocation = "method inject";
            methodMe(allResolved, retVal);

            failureLocation = "post construct";
            postConstructMe(retVal);

            if (eventThrower != null) {
                eventThrower.invokeInstanceListeners(new InstanceLifecycleEventImpl(InstanceLifecycleEventType.POST_PRODUCTION,
                    retVal, ReflectionHelper.<Map<Injectee, Object>>cast(allResolved), eventThrower));
            }

            return retVal;
        } catch (Throwable th) {
            if (th instanceof MultiException) {
                MultiException me = (MultiException) th;

                me.addError(new IllegalStateException("Unable to perform operation: " + failureLocation + " on " + implClass.getName()));

                throw me;
            }

            MultiException me = new MultiException(th);
            me.addError(new IllegalStateException("Unable to perform operation: " + failureLocation + " on " + implClass.getName()));

            throw me;
        }
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.internal.Creator#dispose(java.lang.Object)
     */
    @Override
    public void dispose(T instance) {
        try {
            preDestroyMe(instance);
        } catch (Throwable th) {
            if (th instanceof MultiException) {
                throw (MultiException) th;
            }

            throw new MultiException(th);
        }

    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.internal.Creator#getInjectees()
     */
    @Override
    public List<Injectee> getInjectees() {
        return ReflectionHelper.cast(allInjectees);
    }
    
    /* package */ ServiceLocatorImpl getServiceLocator() {
        return locator;
    }
    
    /* package */ Class<?> getImplClass() {
        return implClass;
    }
    
    /* package */ ActiveDescriptor<?> getUnderlyingDescriptor() {
        return selfDescriptor;
    }
    
    public String toString() {
        return "ClazzCreator(" + locator + "," + implClass.getName() + "," + System.identityHashCode(this) + ")";
    }

    private static class ResolutionInfo {
        private final AnnotatedElement baseElement;
        private final List<SystemInjecteeImpl> injectees = new LinkedList<SystemInjecteeImpl>();

        private ResolutionInfo(AnnotatedElement baseElement, List<SystemInjecteeImpl> injectees) {
            this.baseElement = baseElement;
            this.injectees.addAll(injectees);
        }
    }
}
