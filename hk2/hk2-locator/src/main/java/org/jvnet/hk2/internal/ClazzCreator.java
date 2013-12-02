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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;

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
import org.glassfish.hk2.utilities.cache.Cache;
import org.glassfish.hk2.utilities.cache.Computable;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

/**
 * @author jwells
 * @param <T> The type of object this creator creates
 *
 */
public class ClazzCreator<T> implements Creator<T> {
    private final static Cache<Class<?>, ProxyFactory> PFACTORIES = new Cache<Class<?>, ProxyFactory>(
            new Computable<Class<?>, ProxyFactory>() {

                @Override
                public ProxyFactory compute(Class<?> key) {
                    ProxyFactory proxyFactory = new ProxyFactory();
                    proxyFactory.setSuperclass(key);
                    proxyFactory.setFilter(new MethodFilter() {

                        @Override
                        public boolean isHandled(Method method) {
                            // We do not allow interception of finalize
                            if (method.getName().equals("finalize")) return false;
                            
                            return true;
                        }
                        
                    });
                    
                    return proxyFactory;
                }
                
            });
    
    private final ServiceLocatorImpl locator;
    private final Class<?> implClass;
    private final Set<ResolutionInfo> myInitializers = new LinkedHashSet<ResolutionInfo>();
    private final Set<ResolutionInfo> myFields = new LinkedHashSet<ResolutionInfo>();
    private ActiveDescriptor<?> selfDescriptor;

    private ResolutionInfo myConstructor;
    private List<Injectee> allInjectees;

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

        List<Injectee> baseAllInjectees = new LinkedList<Injectee>();

        AnnotatedElement element;
        List<Injectee> injectees;

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

            injectees = Utilities.getMethodInjectees(initMethod, selfDescriptor);
            if (injectees == null) return;

            baseAllInjectees.addAll(injectees);

            myInitializers.add(new ResolutionInfo(element, injectees));
        }

        Set<Field> fields = Utilities.getInitFields(implClass, analyzer, collector);
        for (Field field : fields) {
            element = field;

            injectees = Utilities.getFieldInjectees(field, selfDescriptor);
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
            if (!(injectee instanceof InjecteeImpl)) continue;

            ((InjecteeImpl) injectee).resetInjecteeDescriptor(selfDescriptor);
        }
    }

    private void resolve(Map<Injectee, Object> addToMe,
                         InjectionResolver<?> resolver,
                         Injectee injectee,
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

    private Map<Injectee, Object> resolveAllDependencies(final ServiceHandle<?> root) throws MultiException, IllegalStateException {
        Collector errorCollector = new Collector();

        final Map<Injectee, Object> retVal = new LinkedHashMap<Injectee, Object>();

        for (Injectee injectee : myConstructor.injectees) {
            InjectionResolver<?> resolver = Utilities.getInjectionResolver(locator, injectee);
            resolve(retVal, resolver, injectee, root, errorCollector);
        }

        for (ResolutionInfo fieldRI : myFields) {
            InjectionResolver<?> resolver = Utilities.getInjectionResolver(locator, fieldRI.baseElement);
            for (Injectee injectee : fieldRI.injectees) {
                resolve(retVal, resolver, injectee, root, errorCollector);
            }
        }

        for (ResolutionInfo methodRI : myInitializers) {
            for (Injectee injectee : methodRI.injectees) {
                InjectionResolver<?> resolver = Utilities.getInjectionResolver(locator, injectee);
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

    private Object createMe(Map<Injectee, Object> resolved) throws Throwable {
        final Constructor<?> c = (Constructor<?>) myConstructor.baseElement;
        List<Injectee> injectees = myConstructor.injectees;

        final Object args[] = new Object[injectees.size()];
        for (Injectee injectee : injectees) {
            args[injectee.getPosition()] = resolved.get(injectee);
        }
        
        final Map<Method, List<MethodInterceptor>> methodInterceptors = Utilities.getAllInterceptedMethods(locator, selfDescriptor, implClass);
        if (!methodInterceptors.isEmpty()) {
            
            final MethodInterceptorHandler methodInterceptor = new MethodInterceptorHandler(locator, methodInterceptors);
            
            final Set<String> methodNames = new HashSet<String>();
            for (Method m : methodInterceptors.keySet()) {
                methodNames.add(m.getName());
            }
            
            final ProxyFactory proxyFactory = PFACTORIES.compute(implClass);
            
            final boolean neutral = locator.getNeutralContextClassLoader();
            
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

                @Override
                public Object run() throws Exception {
                    ClassLoader currentCCL = null;
                    if (neutral) {
                        currentCCL = Thread.currentThread().getContextClassLoader();
                    }
          
                    try {
                      return proxyFactory.create(c.getParameterTypes(), args, methodInterceptor);
                    }
                    catch (InvocationTargetException ite) {
                        Throwable targetException = ite.getTargetException();
                        Logger.getLogger().debug(c.getDeclaringClass().getName(), c.getName(), targetException);
                        if (targetException instanceof Exception) {
                            throw (Exception) targetException;
                        }
                        throw new RuntimeException(targetException);
                    }
                    finally {
                        if (neutral) {
                            Thread.currentThread().setContextClassLoader(currentCCL);
                        }
                    }
                }
                
            });
            
            
        }
        
        return ReflectionHelper.makeMe(c, args, locator.getNeutralContextClassLoader());
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

            ReflectionHelper.setField(field, t, putMeIn);
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

            final Map<Injectee, Object> allResolved = resolveAllDependencies(root);

            if (eventThrower != null) {
                eventThrower.invokeInstanceListeners(new InstanceLifecycleEventImpl(InstanceLifecycleEventType.PRE_PRODUCTION,
                    null, allResolved, eventThrower));
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
                    retVal, allResolved, eventThrower));
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
