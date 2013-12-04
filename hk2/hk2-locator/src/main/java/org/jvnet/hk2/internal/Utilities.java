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

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.aopalliance.intercept.MethodInterceptor;
import org.glassfish.hk2.utilities.cache.Cache;
import org.glassfish.hk2.utilities.cache.Computable;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.ref.SoftReference;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ClassAnalyzer;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.DynamicConfigurationListener;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ErrorService;
import org.glassfish.hk2.api.ErrorType;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.api.InterceptionService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.Proxiable;
import org.glassfish.hk2.api.ProxyCtl;
import org.glassfish.hk2.api.ProxyForSameScope;
import org.glassfish.hk2.api.Self;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.Unproxiable;
import org.glassfish.hk2.api.Unqualified;
import org.glassfish.hk2.api.UseProxy;
import org.glassfish.hk2.api.ValidationService;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.NamedImpl;
import org.glassfish.hk2.utilities.reflection.Pretty;
import org.glassfish.hk2.utilities.reflection.ParameterizedTypeImpl;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * This class contains a set of static utilities useful
 * for implementing HK2
 *
 * @author jwells
 *
 */
public class Utilities {
    private final static Object lock = new Object();

    // We don't want to hold onto these classes if they are released by others
    private static final Map<Class<?>, LinkedHashSet<MemberKey>> methodKeyCache = new WeakHashMap<Class<?>, LinkedHashSet<MemberKey>>();
    private static Map<Class<?>, LinkedHashSet<MemberKey>> fieldCache = new WeakHashMap<Class<?>, LinkedHashSet<MemberKey>>();
    private final static Map<Class<?>, SoftReference<Method>> postConstructCache = new WeakHashMap<Class<?>, SoftReference<Method>>();
    private final static Map<Class<?>, SoftReference<Method>> preDestroyCache = new WeakHashMap<Class<?>, SoftReference<Method>>();

    private final static String CONVENTION_POST_CONSTRUCT = "postConstruct";
    private final static String CONVENTION_PRE_DESTROY = "preDestroy";

    /**
     * Returns the class analyzer with the given name
     *
     * @param sli The ServiceLocator to search in.  May not be null
     * @param analyzerName The name of the analyzer (may be null for the default analyzer)
     * @param errorCollector A non-null collector of exceptions
     * @return The ClassAnalyzer corresponding to the name, or null if none was found
     */
    public static ClassAnalyzer getClassAnalyzer(
            ServiceLocatorImpl sli,
            String analyzerName,
            Collector errorCollector) {
        return sli.getAnalyzer(analyzerName, errorCollector);
    }

    /**
     * Gets the constructor given the implClass and analyzer.  Checks service output
     *
     * @param implClass The implementation class (not null)
     * @param analyzer The analyzer (not null)
     * @param collector A collector for errors (not null)
     * @return null on failure (collector will have failures), non-null on success
     */
    public static <T> Constructor<T> getConstructor(Class<T> implClass, ClassAnalyzer analyzer, Collector collector) {
        Constructor<T> element = null;
        try {
            element = analyzer.getConstructor(implClass);
        }
        catch (MultiException me) {
            collector.addMultiException(me);
            return element;
        }
        catch (Throwable th) {
            collector.addThrowable(th);
            return element;
        }

        if (element == null) {
            collector.addThrowable(new AssertionError("null return from getConstructor method of analyzer " +
                analyzer + " for class " + implClass.getName()));
            return element;
        }
        final Constructor<T> result = element;
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                result.setAccessible(true);
                return null;
            }
        });

        return element;
    }

    /**
     * Gets the initializer methods from the given class and analyzer.  Checks service output
     *
     * @param implClass the non-null impl class
     * @param analyzer the non-null analyzer
     * @param collector for gathering errors
     * @return a non-null set (even in error cases, check the collector)
     */
    public static Set<Method> getInitMethods(Class<?> implClass, ClassAnalyzer analyzer, Collector collector) {
        Set<Method> retVal;
        try {
            retVal = analyzer.getInitializerMethods(implClass);
        }
        catch (MultiException me) {
            collector.addMultiException(me);
            return Collections.emptySet();
        }
        catch (Throwable th) {
            collector.addThrowable(th);
            return Collections.emptySet();
        }

        if (retVal == null) {
            collector.addThrowable(new AssertionError("null return from getInitializerMethods method of analyzer " +
                    analyzer + " for class " + implClass.getName()));
            return Collections.emptySet();
        }

        return retVal;
    }

    /**
     * Gets the initializer fields from the given class and analyzer.  Checks service output
     *
     * @param implClass the non-null impl class
     * @param analyzer the non-null analyzer
     * @param collector for gathering errors
     * @return a non-null set (even in error cases, check the collector)
     */
    public static Set<Field> getInitFields(Class<?> implClass, ClassAnalyzer analyzer, Collector collector) {
        Set<Field> retVal;
        try {
            retVal = analyzer.getFields(implClass);
        }
        catch (MultiException me) {
            collector.addMultiException(me);
            return Collections.emptySet();
        }
        catch (Throwable th) {
            collector.addThrowable(th);
            return Collections.emptySet();
        }

        if (retVal == null) {
            collector.addThrowable(new AssertionError("null return from getFields method of analyzer " +
                    analyzer + " for class " + implClass.getName()));
            return Collections.emptySet();
        }

        return retVal;
    }

    /**
     * Gets the post construct from the analyzer, checking output
     *
     * @param implClass The non-null implementation class
     * @param analyzer The non-null analyzer
     * @param collector The non-null error collector
     * @return The possibly null post-construct method (check the collector for errors)
     */
    public static Method getPostConstruct(Class<?> implClass, ClassAnalyzer analyzer, Collector collector) {
        try {
            return analyzer.getPostConstructMethod(implClass);
        }
        catch (MultiException me) {
            collector.addMultiException(me);
            return null;
        }
        catch (Throwable th) {
            collector.addThrowable(th);
            return null;
        }
    }

    /**
     * Gets the preDestroy from the analyzer, checking output
     *
     * @param implClass The non-null implementation class
     * @param analyzer The non-null analyzer
     * @param collector The non-null error collector
     * @return The possibly null pre-destroy method (check the collector for errors)
     */
    public static Method getPreDestroy(Class<?> implClass, ClassAnalyzer analyzer, Collector collector) {
        try {
            return analyzer.getPreDestroyMethod(implClass);
        }
        catch (MultiException me) {
            collector.addMultiException(me);
            return null;
        }
        catch (Throwable th) {
            collector.addThrowable(th);
            return null;
        }
    }

    /**
     * This utility will return the proper implementation class, taking into account that the
     * descriptor may be a factory
     *
     * @param descriptor The descriptor (reified and not null) that will be used to find the
     * implementation
     *
     * @return The real implementation class
     */
    private static Class<?> getFactoryAwareImplementationClass(ActiveDescriptor<?> descriptor) {
        if (descriptor.getDescriptorType().equals(DescriptorType.CLASS)) {
            return descriptor.getImplementationClass();
        }

        return getFactoryProductionClass(descriptor.getImplementationClass());
    }

    /**
     * Checks that the incoming lookup type is not improper in some way
     *
     * @param checkMe  class to check
     */
    public static void checkLookupType(Class<?> checkMe) {
        if (!checkMe.isAnnotation()) return;

        // If it is in annotation we need to ensure it is either a
        // scope or a qualifier
        if (checkMe.isAnnotationPresent(Scope.class)) return;
        if (checkMe.isAnnotationPresent(Qualifier.class)) return;

        throw new IllegalArgumentException("Lookup type " + checkMe + " must be a scope or annotation");
    }

    /**
     * This is used to check on the annotation set.  It must be done under protection because the annotations may
     * attempt to discover if they are equal using getDeclaredMembers permission
     *
     * @param candidateAnnotations The candidate annotations
     * @param requiredAnnotations The required annotations
     * @return true if the candidate set contains the entire required set
     */
    /* package */
    static boolean annotationContainsAll(final Set<Annotation> candidateAnnotations, final Set<Annotation> requiredAnnotations) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return candidateAnnotations.containsAll(requiredAnnotations);
            }

        });

    }

    private static Field[] getDeclaredFields(final Class<?> clazz) {
        return AccessController.doPrivileged(new PrivilegedAction<Field[]>() {

            @Override
            public Field[] run() {
                return clazz.getDeclaredFields();
            }

        });

    }

    /**
     * Get the declared methods of the given class.
     *
     * @param clazz  the class
     */
    private static Method[] getDeclaredMethods(final Class<?> clazz) {
        return AccessController.doPrivileged(new PrivilegedAction<Method[]>() {

            @Override
            public Method[] run() {
                return clazz.getDeclaredMethods();
            }

        });

    }

    /**
     * Converts the type to its java form, or returns the original
     *
     * @param type The type to convert
     * @return The translated type or the type itself
     */
    public static Class<?> translatePrimitiveType(Class<?> type) {
        Class<?> translation = Constants.PRIMITIVE_MAP.get(type);
        if (translation == null) return type;
        return translation;
    }

    /**
     * Calls the list of error services for the list of errors
     *
     * @param results    the results
     * @param callThese  the services to call
     */
    public static void handleErrors(NarrowResults results, LinkedList<ErrorService> callThese) {
        Collector collector = new Collector();
        for (ErrorResults errorResult : results.getErrors()) {
            for (ErrorService eService : callThese) {
                try {
                    eService.onFailure(new ErrorInformationImpl(
                            ErrorType.FAILURE_TO_REIFY,
                            errorResult.getDescriptor(),
                            errorResult.getInjectee(),
                            errorResult.getMe()));
                } catch (MultiException me) {
                    for (Throwable th : me.getErrors()) {
                        collector.addThrowable(th);
                    }
                } catch (Throwable th) {
                    collector.addThrowable(th);
                }
            }
        }

        collector.throwIfErrors();
    }

    /**
     * Loads the class using the loader from the given descriptor or the
     * classloader of the utilities class otherwise
     *
     * @param loadMe The fully qualified class name
     * @param fromMe The descriptor to use for the loader
     * @param collector The error collector to fill in if this returns null
     * @return null on failure to load (the failure will be added to the collector)
     */
    public static Class<?> loadClass(String loadMe, Descriptor fromMe, Collector collector) {
        HK2Loader loader = fromMe.getLoader();
        if (loader == null) {
            ClassLoader cl = Utilities.class.getClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }

            try {
                return cl.loadClass(loadMe);
            } catch (Throwable th) {
                collector.addThrowable(th);
                return null;
            }
        }

        try {
            return loader.loadClass(loadMe);
        } catch (Throwable th) {
            if (th instanceof MultiException) {
                MultiException me = (MultiException) th;

                for (Throwable th2 : me.getErrors()) {
                    collector.addThrowable(th2);
                }
            } else {
                collector.addThrowable(th);
            }

            return null;
        }

    }

    /**
     * Load the given class for the given injectee.
     *
     * @param implementation  the impl class name string
     * @param injectee        the injectee
     *
     * @return The class represented by this implementation and injectee
     */
    public static Class<?> loadClass(String implementation, Injectee injectee) {
        ClassLoader loader;
        if (injectee != null) {
            AnnotatedElement parent = injectee.getParent();

            if (parent instanceof Constructor) {
                loader = ((Constructor<?>) parent).getDeclaringClass().getClassLoader();
            } else if (parent instanceof Method) {
                loader = ((Method) parent).getDeclaringClass().getClassLoader();
            } else if (parent instanceof Field) {
                loader = ((Field) parent).getDeclaringClass().getClassLoader();
            } else {
                loader = injectee.getClass().getClassLoader();
            }
        } else {
            loader = Utilities.class.getClassLoader();
        }

        try {
            return loader.loadClass(implementation);
        } catch (Throwable th) {
            throw new MultiException(th);
        }
    }

    /**
     * Will return the class of the injection resolver annotation type, or null if
     * no injection resolver annotation can be found
     *
     * @param desc The reified descriptor to find the injection resolution on
     * @return The annotation type for this injection resolver
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Annotation> getInjectionResolverType(ActiveDescriptor<?> desc) {
        for (Type advertisedType : desc.getContractTypes()) {
            Class<?> rawClass = ReflectionHelper.getRawClass(advertisedType);

            if (!InjectionResolver.class.equals(rawClass)) continue;

            // Found the InjectionResolver
            if (!(advertisedType instanceof ParameterizedType)) {
                return null;
            }

            Type firstType = getFirstTypeArgument(advertisedType);
            if (!(firstType instanceof Class)) {
                return null;
            }

            Class<?> retVal = (Class<?>) firstType;

            if (!Annotation.class.isAssignableFrom(retVal)) {
                return null;
            }

            return (Class<? extends Annotation>) retVal;
        }

        return null;
    }

    /**
     * This method returns the class associated with the type of the
     * factory
     *
     * @param factoryClass The non-null factory class.  May not be null
     * @return the CLASS version of what the factory produces.  Will
     * not be null
     * @throws MultiException if there was an error analyzing the class
     */
    private static Class<?> getFactoryProductionClass(Class<?> factoryClass) {
        Type factoryProvidedType = getFactoryProductionType(factoryClass);

        Class<?> retVal = ReflectionHelper.getRawClass(factoryProvidedType);
        if (retVal != null) return retVal;

        throw new MultiException(new AssertionError("Could not find true produced type of factory " + factoryClass.getName()));
    }

    /**
     * This method returns the type produced by a factory class
     *
     * @param factoryClass The non-null factory class.  May not be null
     * @return the type version of what the factory produces.  Will
     * not be null
     * @throws MultiException if there was an error analyzing the class
     */
    public static Type getFactoryProductionType(Class<?> factoryClass) {
        Set<Type> factoryTypes = ReflectionHelper.getTypeClosure(factoryClass,
                Collections.singleton(Factory.class.getName()));

        ParameterizedType parameterizedType = (ParameterizedType) factoryTypes.iterator().next();

        Type factoryProvidedType = parameterizedType.getActualTypeArguments()[0];

        return factoryProvidedType;
    }

    /**
     * Checks to be sure the Factory class is ok
     *
     * @param factoryClass  the class to check
     * @param collector     the exception collector
     */
    public static void checkFactoryType(Class<?> factoryClass, Collector collector) {
        for (Type type : factoryClass.getGenericInterfaces()) {
            Class<?> rawClass = ReflectionHelper.getRawClass(type);
            if (rawClass == null) continue;

            if (!Factory.class.equals(rawClass)) continue;

            Type firstType = getFirstTypeArgument(type);

            if (firstType instanceof WildcardType) {
                // This should not be possible
                collector.addThrowable(new IllegalArgumentException("The class " +
                        Pretty.clazz(factoryClass) + " has a Wildcard as its type"));
            }
        }

    }

    private static Set<Type> getAutoAdvertisedTypes(Type t) {
        LinkedHashSet<Type> retVal = new LinkedHashSet<Type>();
        retVal.add(t);

        Class<?> rawClass = ReflectionHelper.getRawClass(t);
        if (rawClass == null) return retVal;

        Type genericSuperclass = rawClass.getGenericSuperclass();
        while (genericSuperclass != null) {
            Class<?> rawSuperclass = ReflectionHelper.getRawClass(genericSuperclass);
            if (rawSuperclass == null) break;

            if (rawSuperclass.isAnnotationPresent(Contract.class)) {
                retVal.add(genericSuperclass);
            }

            genericSuperclass = rawSuperclass.getGenericSuperclass();
        }

        while (rawClass != null) {
            for (Type iface : rawClass.getGenericInterfaces()) {
                Class<?> ifaceClass = ReflectionHelper.getRawClass(iface);
                if (ifaceClass.isAnnotationPresent(Contract.class)) {
                    retVal.add(iface);
                }
            }

            rawClass = rawClass.getSuperclass();
        }

        return retVal;
    }

    /**
     * Creates a reified automatically generated descriptor
     *
     * @param clazz The class to create the desciptor for
     * @param locator The service locator for whom we are creating this
     * @return A reified active descriptor
     *
     * @throws MultiException if there was an error in the class
     * @throws IllegalArgumentException If the class is null
     * @throws IllegalStateException If the name could not be determined from the Named annotation
     */
    public static <T> AutoActiveDescriptor<T> createAutoDescriptor(Class<T> clazz, ServiceLocatorImpl locator)
            throws MultiException, IllegalArgumentException, IllegalStateException {
        if (clazz == null) throw new IllegalArgumentException();

        Collector collector = new Collector();

        ClazzCreator<T> creator;
        Set<Annotation> qualifiers;
        Set<Type> contracts;
        Class<? extends Annotation> scope;
        String name;
        Boolean proxy = null;
        Boolean proxyForSameScope = null;
        String analyzerName;

        // Qualifiers naming dance
        qualifiers = ReflectionHelper.getQualifierAnnotations(clazz);
        name = ReflectionHelper.getNameFromAllQualifiers(qualifiers, clazz);
        qualifiers = getAllQualifiers(clazz, name, collector);  // Fixes the @Named qualifier if it has no value

        contracts = getAutoAdvertisedTypes(clazz);
        ScopeInfo scopeInfo = getScopeInfo(clazz, null, collector);
        scope = scopeInfo.getAnnoType();
        analyzerName = getAutoAnalyzerName(clazz);

        creator = new ClazzCreator<T>(locator, clazz);

        collector.throwIfErrors();

        Map<String, List<String>> metadata = new HashMap<String, List<String>>();
        if (scopeInfo.getScope() != null) {
            BuilderHelper.getMetadataValues(scopeInfo.getScope(), metadata);
        }

        for (Annotation qualifier : qualifiers) {
            BuilderHelper.getMetadataValues(qualifier, metadata);
        }

        UseProxy useProxy = clazz.getAnnotation(UseProxy.class);
        if (useProxy != null) {
            proxy = useProxy.value();
        }

        ProxyForSameScope pfss = clazz.getAnnotation(ProxyForSameScope.class);
        if (pfss != null) {
            proxyForSameScope = pfss.value();
        }

        DescriptorVisibility visibility = DescriptorVisibility.NORMAL;
        Visibility vi = clazz.getAnnotation(Visibility.class);
        if (vi != null) {
            visibility = vi.value();
        }

        AutoActiveDescriptor<T> retVal = new AutoActiveDescriptor<T>(
                clazz,
                creator,
                contracts,
                scope,
                name,
                qualifiers,
                visibility,
                0,
                proxy,
                proxyForSameScope,
                analyzerName,
                metadata);

        creator.initialize(retVal, analyzerName, collector);

        collector.throwIfErrors();

        return retVal;
    }

    /**
     * Pre Destroys the given object
     *
     * @param preMe pre destroys the thing
     * @param locator The non-null service locator associated with the operation (for finding the strategy)
     * @param strategy The strategy to use for analyzing the class
     */
    public static void justPreDestroy(Object preMe, ServiceLocatorImpl locator, String strategy) {
        if (preMe == null) throw new IllegalArgumentException();

        Collector collector = new Collector();

        ClassAnalyzer analyzer = getClassAnalyzer(locator, strategy, collector);
        collector.throwIfErrors();

        collector.throwIfErrors();

        Class<?> baseClass = preMe.getClass();

        Method preDestroy = getPreDestroy(baseClass, analyzer, collector);

        collector.throwIfErrors();

        if (preDestroy == null) return;

        try {
            ReflectionHelper.invoke(preMe, preDestroy, new Object[0], locator.getNeutralContextClassLoader());
        } catch (Throwable e) {
            throw new MultiException(e);
        }
    }

    /**
     * Post constructs the given object
     *
     * @param postMe post constructs the thing
     * @param locator The non-null service locator associated with the operation (for finding the strategy)
     * @param strategy The strategy to use for analyzing the class
     */
    public static void justPostConstruct(Object postMe, ServiceLocatorImpl locator, String strategy) {
        if (postMe == null) throw new IllegalArgumentException();

        Collector collector = new Collector();

        ClassAnalyzer analyzer = getClassAnalyzer(locator, strategy, collector);
        collector.throwIfErrors();

        Class<?> baseClass = postMe.getClass();

        Method postConstruct = getPostConstruct(baseClass, analyzer, collector);

        collector.throwIfErrors();

        if (postConstruct == null) return;

        try {
            ReflectionHelper.invoke(postMe, postConstruct, new Object[0], locator.getNeutralContextClassLoader());
        } catch (Throwable e) {
            throw new MultiException(e);
        }
    }

    /**
     * Just creates the thing, doesn't try to do anything else
     * @param injectMe The object to inject into
     * @param locator The locator to find the injection points with
     * @param strategy The strategy to use for analyzing the class
     */
    public static void justInject(Object injectMe, ServiceLocatorImpl locator, String strategy) {
        if (injectMe == null) throw new IllegalArgumentException();

        Collector collector = new Collector();

        ClassAnalyzer analyzer = getClassAnalyzer(locator, strategy, collector);
        collector.throwIfErrors();

        Class<?> baseClass = injectMe.getClass();

        Set<Field> fields = Utilities.getInitFields(baseClass, analyzer, collector);
        Set<Method> methods = Utilities.getInitMethods(baseClass, analyzer, collector);

        collector.throwIfErrors();

        for (Field field : fields) {
            InjectionResolver<?> resolver = getInjectionResolver(locator, field);

            List<Injectee> injecteeFields = Utilities.getFieldInjectees(field, null);

            validateSelfInjectees(null, injecteeFields, collector);
            collector.throwIfErrors();

            Injectee injectee = injecteeFields.get(0);

            Object fieldValue = resolver.resolve(injectee, null);

            try {
                ReflectionHelper.setField(field, injectMe, fieldValue);
            }
            catch (Throwable th) {
                throw new MultiException(th);
            }
        }

        for (Method method : methods) {
            List<Injectee> injectees = Utilities.getMethodInjectees(method, null);

            validateSelfInjectees(null, injectees, collector);
            collector.throwIfErrors();

            Object args[] = new Object[injectees.size()];

            for (Injectee injectee : injectees) {
                InjectionResolver<?> resolver = getInjectionResolver(locator, injectee);
                args[injectee.getPosition()] = resolver.resolve(injectee, null);
            }

            try {
                ReflectionHelper.invoke(injectMe, method, args, locator.getNeutralContextClassLoader());
            } catch (Throwable e) {
                throw new MultiException(e);
            }
        }

    }

    /**
     * Just creates the thing, doesn't try to do anything else
     * @param createMe The thing to create
     * @param locator The locator to find the injection points with
     * @param strategy The strategy to use for analyzing the class
     * @return The constructed thing, no further injection is performed
     */
    @SuppressWarnings("unchecked")
    public static <T> T justCreate(Class<T> createMe, ServiceLocatorImpl locator, String strategy) {
        if (createMe == null) throw new IllegalArgumentException();

        Collector collector = new Collector();
        ClassAnalyzer analyzer = getClassAnalyzer(locator, strategy, collector);
        collector.throwIfErrors();

        Constructor<?> c = getConstructor(createMe, analyzer, collector);

        collector.throwIfErrors();

        List<Injectee> injectees = getConstructorInjectees(c, null);

        validateSelfInjectees(null, injectees, collector);
        collector.throwIfErrors();

        Object args[] = new Object[injectees.size()];

        for (Injectee injectee : injectees) {
            InjectionResolver<?> resolver = getInjectionResolver(locator, injectee);
            args[injectee.getPosition()] = resolver.resolve(injectee, null);
        }

        try {
          return (T) ReflectionHelper.makeMe(c, args, locator.getNeutralContextClassLoader());
        } catch (Throwable th) {
            throw new MultiException(th);
        }
    }

    /**
     * Returns all the interfaces the proxy must implement
     * @param contracts All of the advertised contracts
     * @return The array of contracts to add to the proxy
     */
    public static Class<?>[] getInterfacesForProxy(Set<Type> contracts) {
        LinkedList<Class<?>> retVal = new LinkedList<Class<?>>();
        retVal.add(ProxyCtl.class);    // Every proxy implements this interface

        for (Type type : contracts) {
            Class<?> rawClass = ReflectionHelper.getRawClass(type);
            if (rawClass == null) continue;
            if (!rawClass.isInterface()) continue;

            retVal.add(rawClass);
        }

        return retVal.toArray(new Class<?>[retVal.size()]);
    }

    /**
     * Returns true if this scope is proxiable
     *
     * @param scope The scope annotation to test
     * @return true if this must be proxied
     */
    public static boolean isProxiableScope(Class<? extends Annotation> scope) {
        return scope.isAnnotationPresent(Proxiable.class);
    }

    /**
     * Returns true if this scope is unproxiable
     *
     * @param scope The scope annotation to test
     * @return true if this must be proxied
     */
    public static boolean isUnproxiableScope(Class<? extends Annotation> scope) {
        if (scope.isAnnotationPresent(Unproxiable.class)) return true;
        return false;
    }

    static Cache<Class<? extends Annotation>, Boolean> proxiableAnnotationCache =
            new Cache<Class<? extends Annotation>, Boolean>(new Computable<Class<? extends Annotation>, Boolean>(){

        @Override
        public Boolean compute(Class<? extends Annotation> a) {
            return a.isAnnotationPresent(Proxiable.class);
        }
    });

    /**
     * This method determines whether or not the descriptor should be proxied.
     * The given descriptor must be reified and valid.
     *
     * @param desc A non-null, reified ActiveDescriptor
     * @param injectee The injectee where this is being injected if known,
     * or null if not known
     * @return true if this descriptor must be proxied, false otherwise
     */
    private static boolean isProxiable(ActiveDescriptor<?> desc, Injectee injectee) {
        Boolean directed = desc.isProxiable();

        if (directed != null) {
            if (injectee == null) {
                // No other scope to compare to
                return directed;
            }

            if (!directed) {
                // Doesn't matter what the other scope is, not proxied
                return false;
            }

            ActiveDescriptor<?> injecteeDescriptor = injectee.getInjecteeDescriptor();
            if (injecteeDescriptor == null) {
                // No other scope to compare to
                return true;
            }

            Boolean sameScope = desc.isProxyForSameScope();
            if (sameScope == null || sameScope) {
                // The default case is to be lazy
                return true;
            }

            // OK, same scope is false, forced Proxy is true,
            // now we need to see if the scopes of the two
            // things are in fact the same
            if (desc.getScope().equals(injecteeDescriptor.getScope())) {
                // The scopes are the same, proxy-for-same-scope is false,
                // so the answer is no, do not proxy
                return false;
            }

            // The scopes are different, deal with it
            return true;
        }

        final Class<? extends Annotation> scopeAnnotation = desc.getScopeAnnotation();
        if (!proxiableAnnotationCache.compute(scopeAnnotation)) return false;


        if (injectee == null) {
            // No other scope to compare to
            return true;
        }

        ActiveDescriptor<?> injecteeDescriptor = injectee.getInjecteeDescriptor();
        if (injecteeDescriptor == null) {
            // No other scope to compare to
            return true;
        }

        Proxiable proxiable = scopeAnnotation.getAnnotation(Proxiable.class);
        Boolean proxyForSameScope = desc.isProxyForSameScope();

        if (proxyForSameScope != null) {
            if (proxyForSameScope) {
              return true;
            }
        }
        else if (proxiable == null || proxiable.proxyForSameScope()) {
            // The default case is to be lazy
            return true;
        }

        // OK, same scope is false, and we are in Proxiable scope,
        // now we need to see if the scopes of the two
        // things are in fact the same
        if (desc.getScope().equals(injecteeDescriptor.getScope())) {
            // The scopes are the same, proxy-for-same-scope is false,
            // so the answer is no, do not proxy

            return false;
        }

        // The scopes are different, deal with it
        return true;
    }

    /**
     * Returns the first thing found in the set
     *
     * @param set The set from which to get the first element
     * @return the first thing found in the set
     */
    public static <T> T getFirstThingInList(List<T> set) {
        for (T t : set) {
            return t;
        }

        return null;
    }

    /**
     * Get all fields on this class and all subclasses
     *
     * @param clazz The class to inspect
     * @return A set of all the fields on this class
     */
    private static Set<Field> getAllFields(Class<?> clazz, Collector collector) {
        LinkedHashSet<Field> retVal = new LinkedHashSet<Field>();

        LinkedHashSet<MemberKey> keys = new LinkedHashSet<MemberKey>();

        getAllFieldKeys(clazz, keys, collector);

        for (MemberKey key : keys) {
            retVal.add((Field) key.getBackingMember());
        }

        return retVal;
    }



    private static void getAllFieldKeys(Class<?> clazz, LinkedHashSet<MemberKey> currentFields, Collector collector) {
        if (clazz == null) return;

        Set<MemberKey> discovered;
        synchronized (lock) {
            discovered = fieldCache.get(clazz);
        }

        if (discovered != null) {
            currentFields.addAll(discovered);
            return;
        }

        // Do superclasses first, so that inherited methods are
        // overriden in the set
        getAllFieldKeys(clazz.getSuperclass(), currentFields, collector);

        try {
            for (Field field : getDeclaredFields(clazz)) {
                currentFields.add(new MemberKey(field, false, false));
            }

            synchronized (lock) {
                fieldCache.put(clazz, new LinkedHashSet<MemberKey>(currentFields));
            }
        } catch (Throwable th) {
            collector.addThrowable(new IllegalStateException("Error while getting fields of class " + clazz.getName(), th));
        }

    }

    /**
     * Returns a constant ActiveDescriptor for the basic ServiceLocator
     *
     * @param locator The service locator to get the ActiveDescriptor for
     * @return An active descriptor specifically for the ServiceLocator
     */
    public static ActiveDescriptor<ServiceLocator> getLocatorDescriptor(ServiceLocator locator) {
        HashSet<Type> contracts = new HashSet<Type>();
        contracts.add(ServiceLocator.class);

        Set<Annotation> qualifiers = Collections.emptySet();

        ActiveDescriptor<ServiceLocator> retVal =
                new ConstantActiveDescriptor<ServiceLocator>(
                        locator,
                        contracts,
                        PerLookup.class,
                        null,
                        qualifiers,
                        DescriptorVisibility.LOCAL,
                        0,
                        null,
                        null,
                        null,
                        locator.getLocatorId(),
                        null);

        return retVal;
    }

    /**
     * Creates a Three Thirty constant active descriptor
     *
     * @param locator The service locator to get the ActiveDescriptor for
     * @return An active descriptor specifically for the ServiceLocator
     */
    public static ActiveDescriptor<InjectionResolver<Inject>> getThreeThirtyDescriptor(
            ServiceLocatorImpl locator) {
        ThreeThirtyResolver threeThirtyResolver = new ThreeThirtyResolver(locator);

        HashSet<Type> contracts = new HashSet<Type>();

        Type actuals[] = new Type[1];
        actuals[0] = Inject.class;

        contracts.add(new ParameterizedTypeImpl(InjectionResolver.class, actuals));

        Set<Annotation> qualifiers = new HashSet<Annotation>();
        qualifiers.add(new NamedImpl(InjectionResolver.SYSTEM_RESOLVER_NAME));

        ActiveDescriptor<InjectionResolver<Inject>> retVal =
                new ConstantActiveDescriptor<InjectionResolver<Inject>>(
                        threeThirtyResolver,
                        contracts,
                        Singleton.class,
                        InjectionResolver.SYSTEM_RESOLVER_NAME,
                        qualifiers,
                        DescriptorVisibility.NORMAL,
                        0,
                        null,
                        null,
                        null,
                        locator.getLocatorId(),
                        null);

        return retVal;
    }

    /**
     * Validates the constructors of the annotated type and returns the
     * producer for the annotatedType (if there is no valid producer
     * constructor then this method returns null)
     *
     * @param annotatedType The type to find the producer constructor
     * @param locator The service locator to use when analyzing constructors
     * @param collector The error collector
     * @return The producer constructor or null if the type has no valid
     * producer constructor
     */
    public static Constructor<?> findProducerConstructor(Class<?> annotatedType, ServiceLocatorImpl locator, Collector collector) {
        Constructor<?> zeroArgConstructor = null;
        Constructor<?> aConstructorWithInjectAnnotation = null;

        Set<Constructor<?>> allConstructors = getAllConstructors(annotatedType);
        for (Constructor<?> constructor : allConstructors) {

            Type rawParameters[] = constructor.getGenericParameterTypes();
            if (rawParameters.length <= 0) {
                zeroArgConstructor = constructor;
            }

            if (hasInjectAnnotation(locator, constructor, true)) {
                if (aConstructorWithInjectAnnotation != null) {
                    collector.addThrowable(new IllegalArgumentException("There is more than one constructor on class " +
                            Pretty.clazz(annotatedType)));
                    return null;
                }

                aConstructorWithInjectAnnotation = constructor;
            }

            if (!isProperConstructor(constructor)) {
                collector.addThrowable(new IllegalArgumentException("The constructor for " +
                        Pretty.clazz(annotatedType) + " may not have an annotation as a parameter"));
                return null;

            }

        }

        if (aConstructorWithInjectAnnotation != null) {
            return aConstructorWithInjectAnnotation;
        }

        if (zeroArgConstructor == null) {
            collector.addThrowable(new NoSuchMethodException("The class " + Pretty.clazz(annotatedType) +
                    " has no constructor marked @Inject and no zero argument constructor"));
            return null;
        }

        return zeroArgConstructor;
    }

    private static boolean isProperConstructor(Constructor<?> c) {
        for (Class<?> pClazz : c.getParameterTypes()) {
            if (pClazz.isAnnotation()) return false;
        }

        return true;
    }

    /**
     * Gets the first type argument if this is a parameterized
     * type, otherwise it returns Object.class
     *
     * @param type The type to find the first type argument on
     * @return If this is a class, Object.class. If this is a parameterized
     * type, the type of the first actual argument
     */
    public static Type getFirstTypeArgument(Type type) {
        if (type instanceof Class) {
            return Object.class;
        }

        if (!(type instanceof ParameterizedType)) return Object.class;

        ParameterizedType pt = (ParameterizedType) type;
        Type arguments[] = pt.getActualTypeArguments();
        if (arguments.length <= 0) return Object.class;

        return arguments[0];
    }

    /**
     * Gets all the constructors for a given class
     *
     * @param clazz The class to find the constructors of
     * @return A set of Constructors for the given class
     */
    private static Set<Constructor<?>> getAllConstructors(Class<?> clazz) {
        HashSet<Constructor<?>> retVal = new HashSet<Constructor<?>>();

        HashSet<MemberKey> keys = new HashSet<MemberKey>();

        getAllConstructorKeys(clazz, keys);

        for (MemberKey key : keys) {
            retVal.add((Constructor<?>) key.getBackingMember());
        }

        return retVal;
    }

    private static void getAllConstructorKeys(final Class<?> clazz, Set<MemberKey> currentConstructors) {
        if (clazz == null) return;

        // Constructors for the superclass do not equal constructors for this class
        Constructor<?> constructors[] = AccessController.doPrivileged(new PrivilegedAction<Constructor<?>[]>() {

            @Override
            public Constructor<?>[] run() {
                return clazz.getDeclaredConstructors();
            }

        });

        for (Constructor<?> constructor : constructors) {
            currentConstructors.add(new MemberKey(constructor, false, false));
        }

    }

    /**
     * Gets all methods, public, private etc on this class and on all
     * subclasses
     *
     * @param clazz The class to check out
     * @return A set of all methods on this class
     */
    private static Set<Method> getAllMethods(Class<?> clazz) {
        LinkedHashSet<Method> retVal = new LinkedHashSet<Method>();

        LinkedHashSet<MemberKey> keys = new LinkedHashSet<MemberKey>();

        getAllMethodKeys(clazz, keys);

        Method postConstructMethod = null;
        Method preDestroyMethod = null;
        for (MemberKey key : keys) {
            retVal.add((Method) key.getBackingMember());
            if (key.isPostConstruct()) {
                postConstructMethod = (Method) key.getBackingMember();
            }
            if (key.isPreDestroy()) {
                preDestroyMethod = (Method) key.getBackingMember();
            }
        }

        synchronized (lock) {
            // It is ok for postConstructMethod to be null
            postConstructCache.put(clazz, new SoftReference<Method>(postConstructMethod));

            // It is ok for preDestroyMethod to be null
            preDestroyCache.put(clazz, new SoftReference<Method>(preDestroyMethod));
        }

        return retVal;
    }

    private static boolean isPostConstruct(Method m) {
        if (m.isAnnotationPresent(PostConstruct.class)) return true;

        if (m.getParameterTypes().length != 0) return false;
        return CONVENTION_POST_CONSTRUCT.equals(m.getName());
    }

    private static boolean isPreDestroy(Method m) {
        if (m.isAnnotationPresent(PreDestroy.class)) return true;

        if (m.getParameterTypes().length != 0) return false;
        return CONVENTION_PRE_DESTROY.equals(m.getName());
    }

    private static void getAllMethodKeys(Class<?> clazz, LinkedHashSet<MemberKey> currentMethods) {
        if (clazz == null) return;

        Set<MemberKey> discoveredMethods;
        synchronized (lock) {
            discoveredMethods = methodKeyCache.get(clazz);
        }

        if (discoveredMethods != null) {
            currentMethods.addAll(discoveredMethods);
            return;
        }

        // Do superclasses first, so that inherited methods are
        // overriden in the set
        getAllMethodKeys(clazz.getSuperclass(), currentMethods);

        for (Method method : getDeclaredMethods(clazz)) {
            boolean isPostConstruct = isPostConstruct(method);
            boolean isPreDestroy = isPreDestroy(method);

            currentMethods.add(new MemberKey(method, isPostConstruct, isPreDestroy));
        }

        synchronized (lock) {
            methodKeyCache.put(clazz, new LinkedHashSet<MemberKey>(currentMethods));
        }
    }

    /**
     * Get all the initializer methods of the annotatedType.  If there are definitional
     * errors they will be put into the errorCollector (so as to get all the errors
     * at one shot)
     *
     * @param annotatedType The type to find the errors in
     * @param locator The locator to use when analyzing methods
     * @param errorCollector The collector to add errors to
     * @return A possibly empty but never null set of initializer methods
     */
    public static Set<Method> findInitializerMethods(
            Class<?> annotatedType,
            ServiceLocatorImpl locator,
            Collector errorCollector) {
        LinkedHashSet<Method> retVal = new LinkedHashSet<Method>();

        for (Method method : getAllMethods(annotatedType)) {
            if (!hasInjectAnnotation(locator, method, true)) {
                // Not an initializer method
                continue;
            }

            if (!isProperMethod(method)) {
                errorCollector.addThrowable(new IllegalArgumentException(
                        "An initializer method " + Pretty.method(method) +
                                " is static, abstract or has a parameter that is an annotation"));
                continue;
            }

            retVal.add(method);
        }

        return retVal;
    }

    /**
     * Will find all the initialize fields in the class
     *
     * @param annotatedType The class to search for fields
     * @param locator The locator to use when analyzing the class
     * @param errorCollector The error collector
     * @return A non-null but possibly empty set of initializer fields
     */
    public static Set<Field> findInitializerFields(Class<?> annotatedType,
                                                   ServiceLocatorImpl locator,
                                                   Collector errorCollector) {
        LinkedHashSet<Field> retVal = new LinkedHashSet<Field>();

        for (Field field : getAllFields(annotatedType, errorCollector)) {
            if (!hasInjectAnnotation(locator, field, false)) {
                // Not an initializer field
                continue;
            }

            if (!isProperField(field)) {
                errorCollector.addThrowable(new IllegalArgumentException("The field " +
                        Pretty.field(field) + " may not be static, final or have an Annotation type"));
                continue;
            }

            retVal.add(field);
        }

        return retVal;
    }

    /**
     * Checks whether an annotated element has any annotation that was used for the injection
     *
     * @param locator The service locator to use (as it will get all
     * the annotations that were added on as well as the normal Inject)
     * @param annotated  the annotated element
     * @param checkParams  check the params if true
     * @return True if element contains at least one inject annotation
     */
    private static boolean hasInjectAnnotation(ServiceLocatorImpl locator, AnnotatedElement annotated, boolean checkParams) {
        for (Annotation anno : annotated.getAnnotations()) {
            if (locator.isInjectAnnotation(anno)) {
                return true;
            }
        }

        if (!checkParams) return false;

        boolean isConstructor;
        Annotation allAnnotations[][];
        if (annotated instanceof Method) {
            Method m = (Method) annotated;

            isConstructor = false;
            allAnnotations = m.getParameterAnnotations();
        } else if (annotated instanceof Constructor) {
            Constructor<?> c = (Constructor<?>) annotated;

            isConstructor = true;
            allAnnotations = c.getParameterAnnotations();
        } else {
            return false;
        }

        for (Annotation allParamAnnotations[] : allAnnotations) {
            for (Annotation paramAnno : allParamAnnotations) {
                if (locator.isInjectAnnotation(paramAnno, isConstructor)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static class AnnotatedElementAnnotationInfo {
        final Annotation[] elementAnnotations;
        final Annotation[][] paramAnnotations;
        final boolean hasParams;
        final boolean isConstructor;

        AnnotatedElementAnnotationInfo(Annotation[] elementAnnotation, boolean hasParams, Annotation[][] paramAnnotation, boolean isConstructor) {
            this.elementAnnotations = elementAnnotation;
            this.hasParams = hasParams;
            this.paramAnnotations = paramAnnotation;
            this.isConstructor = isConstructor;
        }
    }

    private static final Cache<AnnotatedElement, AnnotatedElementAnnotationInfo> AnnotationCache =
            new Cache<AnnotatedElement, AnnotatedElementAnnotationInfo>(new Computable<AnnotatedElement, AnnotatedElementAnnotationInfo>() {

        @Override
        public AnnotatedElementAnnotationInfo compute(AnnotatedElement annotatedElement) {

            if (annotatedElement instanceof Method) {

                final Method m = (Method) annotatedElement;
                return new AnnotatedElementAnnotationInfo(m.getAnnotations(), true, m.getParameterAnnotations(), false);

            } else if (annotatedElement instanceof Constructor) {

                final Constructor<?> c = (Constructor<?>) annotatedElement;
                return new AnnotatedElementAnnotationInfo(c.getAnnotations(), true, c.getParameterAnnotations(), true);

            } else {
                return new AnnotatedElementAnnotationInfo(annotatedElement.getAnnotations(), false, null, false);
            }
        }
    });

    /**
     * Gets the annotation that was used for the injection
     *
     * @param locator The service locator to use (as it will get all
     * the annotations that were added on as well as the normal Inject)
     * @param annotated the annotated annotated
     * @param checkParams  check the params if true
     * @param position index of constructor or method parameter which which will be checked
     *                 for inject annotations. The {@code position} parameter is only used when
     *                 {@code annotated} is method or constructor otherwise the value will be ignored.
     * @return The annotation that is the inject annotation, or null
     * if no inject annotation was found
     */
    private static Annotation getInjectAnnotation(final ServiceLocatorImpl locator, final AnnotatedElement annotated,
            final boolean checkParams, final int position) {

        final AnnotatedElementAnnotationInfo annotationInfo = AnnotationCache.compute(annotated);

        if (checkParams) {

            if (annotationInfo.hasParams) {
                for (Annotation paramAnno : annotationInfo.paramAnnotations[position]) {
                    if (locator.isInjectAnnotation(paramAnno, annotationInfo.isConstructor)) {
                        return paramAnno;
                    }
                }
            }
        }

        for (Annotation annotation : annotationInfo.elementAnnotations) {
            if (locator.isInjectAnnotation(annotation)) {
                return annotation;
            }
        }

        return null;
    }

  private static boolean isProperMethod(Method member) {
        if (ReflectionHelper.isStatic(member)) return false;
        if (isAbstract(member)) return false;
        for (Class<?> paramClazz : member.getParameterTypes()) {
            if (paramClazz.isAnnotation()) {
                return false;
            }
        }

        return true;
    }

    private static boolean isProperField(Field field) {
        if (ReflectionHelper.isStatic(field)) return false;
        if (isFinal(field)) return false;
        Class<?> type = field.getType();
        return !type.isAnnotation();
    }

    /**
     * Returns true if the underlying member is private
     *
     * @param member The non-null member to test
     * @return true if the member is private
     */
    public static boolean isPrivate(Member member) {
        int modifiers = member.getModifiers();

        return ((modifiers & Modifier.PRIVATE) != 0);
    }

    /**
     * Returns true if the underlying member is abstract
     *
     * @param member The non-null member to test
     * @return true if the member is abstract
     */
    public static boolean isAbstract(Member member) {
        int modifiers = member.getModifiers();

        return ((modifiers & Modifier.ABSTRACT) != 0);
    }

    /**
     * Returns true if the underlying member is abstract
     *
     * @param member The non-null member to test
     * @return true if the member is abstract
     */
    public static boolean isFinal(Member member) {
        int modifiers = member.getModifiers();

        return ((modifiers & Modifier.FINAL) != 0);
    }
    
    private static boolean isFinal(Class<?> clazz) {
        int modifiers = clazz.getModifiers();

        return ((modifiers & Modifier.FINAL) != 0);
    }

    private static final Cache<Class<?>, String> autoAnalyzerNameCache = new Cache<Class<?>, String>(new Computable<Class<?>, String>() {

        @Override
        public String compute(final Class<?> c) {

            Service s = c.getAnnotation(Service.class);
            if (s == null) return null;

            return s.analyzer();
        }
    });

    /**
     * Gets the analyzer name from the Service annotation
     *
     * @param c The class to get the analyzer name from
     * @return The name of the analyzer (null for default)
     */
    public static String getAutoAnalyzerName(Class<?> c) {
        return autoAnalyzerNameCache.compute(c);
    }

    @SuppressWarnings("unchecked")
    private static ScopeInfo getScopeInfo(
            AnnotatedElement annotatedGuy,
            Descriptor defaultScope,
            Collector collector) {
        AnnotatedElement topLevelElement = annotatedGuy;

        Annotation winnerScope = null;
        while (annotatedGuy != null) {
            Annotation current = internalGetScopeAnnotationType(
                    annotatedGuy,
                    collector);
            if (current != null) {
                if (annotatedGuy.equals(topLevelElement)) {
                    // We found a winner, no matter the inherited state
                    winnerScope = current;
                    break;
                } else {
                    if (current.annotationType().isAnnotationPresent(Inherited.class)) {
                        winnerScope = current;
                        break;
                    }

                    // This non-inherited annotation wipes out all scopes above it
                    break;
                }
            }

            if (annotatedGuy instanceof Class) {
                annotatedGuy = ((Class<?>) annotatedGuy).getSuperclass();
            } else {
                Method theMethod = (Method) annotatedGuy;
                Class<?> methodClass = theMethod.getDeclaringClass();

                annotatedGuy = null;
                Class<?> methodSuperclass = methodClass.getSuperclass();
                while (methodSuperclass != null) {
                    if (Factory.class.isAssignableFrom(methodSuperclass)) {
                        annotatedGuy = getFactoryProvideMethod(methodSuperclass);
                        break;
                    }

                    methodSuperclass = methodSuperclass.getSuperclass();
                }
            }
        }

        if (winnerScope != null) {
            return new ScopeInfo(winnerScope, winnerScope.annotationType());
        }


        if (topLevelElement.isAnnotationPresent(Service.class)) {
            return new ScopeInfo(null, Singleton.class);
        }

        if (defaultScope != null && defaultScope.getScope() != null) {
            Class<? extends Annotation> descScope = (Class<? extends Annotation>)
                    loadClass(defaultScope.getScope(), defaultScope, collector);
            if (descScope != null) {
                return new ScopeInfo(null, descScope);
            }
        }

        return new ScopeInfo(null, PerLookup.class);

    }

    /**
     * Returns the scope of this thing
     *
     * @param fromThis The annotated class or producer method
     * @param defaultScope The default scope if none other can be found
     * @return The scope of this class or producer method.  If no scope is
     * found will return the dependent scope
     */
    public static Class<? extends Annotation> getScopeAnnotationType(
            Class<?> fromThis,
            Descriptor defaultScope) {
        Collector collector = new Collector();

        ScopeInfo si = getScopeInfo(fromThis, defaultScope, collector);

        collector.throwIfErrors();

        return si.getAnnoType();
    }

    /**
     * Returns the scope of this thing
     *
     * @param annotatedGuy The annotated class or producer method
     * @param defaultScope The default scope if none other can be found
     * @param collector The error collector
     * @return The scope of this class or producer method.  If no scope is
     * found will return the dependent scope
     */
    public static Class<? extends Annotation> getScopeAnnotationType(
            AnnotatedElement annotatedGuy,
            Descriptor defaultScope,
            Collector collector) {
        ScopeInfo si = getScopeInfo(annotatedGuy, defaultScope, collector);
        return si.getAnnoType();
    }

    /**
     * This returns the scope annotation on this class *itself*, and no other
     * classes (like, not subclasses).
     */
    private static Annotation internalGetScopeAnnotationType(
            AnnotatedElement annotatedGuy,
            Collector collector) {
        boolean epicFail = false;
        Annotation retVal = null;
        for (Annotation annotation : annotatedGuy.getDeclaredAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Scope.class)) {
                if (retVal != null) {
                    collector.addThrowable(new IllegalArgumentException("The type " + annotatedGuy +
                            " may not have more than one scope.  It has at least " +
                            Pretty.clazz(retVal.annotationType()) +
                            " and " + Pretty.clazz(annotation.annotationType())));
                    epicFail = true;
                    continue;
                }

                retVal = annotation;
            }
        }

        if (epicFail) return null;

        return retVal;
    }

    /**
     * Returns an injection resolver for the injectee
     *
     * @param locator The locator to use when finding the resolver
     * @param injectee Injectee from which the annotation should be extracted
     * @return Injection resolver used to resolve the injection for the injectee
     * @throws IllegalStateException If we could not find a valid resolver
     */
    public static InjectionResolver<?> getInjectionResolver(
            ServiceLocatorImpl locator, Injectee injectee) throws IllegalStateException {
        return getInjectionResolver(locator, injectee.getParent(), injectee.getPosition());

    }

    private static InjectionResolver<?> getInjectionResolver(
            ServiceLocatorImpl locator, AnnotatedElement annotatedGuy, int position) throws IllegalStateException {
        boolean methodOrConstructor = annotatedGuy instanceof Method || annotatedGuy instanceof Constructor<?>;
        Annotation injectAnnotation = getInjectAnnotation(locator, annotatedGuy, methodOrConstructor, position);

        //Annotation injectAnnotation = getInjectAnnotation(locator, annotatedGuy, position);

        Class<? extends Annotation> injectType = (injectAnnotation == null) ?
                Inject.class : injectAnnotation.annotationType();

        InjectionResolver<?> retVal = locator.getInjectionResolver(injectType);
        if (retVal == null) {
            // Not possible to get here, we only are here if we already found a resolver
            throw new IllegalStateException("There is no installed injection resolver for " +
                    Pretty.clazz(injectType) + " for type " + annotatedGuy);
        }

        return retVal;
    }

    /**
     * Returns an injection resolver for this AnnotatedElement. The method cannot be used for constructors
     * or methods.
     *
     * @param locator The locator to use when finding the resolver
     * @param annotatedGuy The annotated class or producer method
     * @return The scope of this class or producer method.  If no scope is
     * found will return the dependent scope
     * @throws IllegalStateException If we could not find a valid resolver
     */
    public static InjectionResolver<?> getInjectionResolver(
            ServiceLocatorImpl locator, AnnotatedElement annotatedGuy) throws IllegalStateException {
        if (annotatedGuy instanceof Method || annotatedGuy instanceof Constructor<?>) {
            throw new IllegalArgumentException("Annotated element '" + annotatedGuy + "' cannot be Method neither Constructor.");
        }
        return getInjectionResolver(locator, annotatedGuy, -1);
    }

    private final static String PROVIDE_METHOD = "provide";

    /**
     * This method will retrieve the provide method from a Factory
     *
     * @param clazz This class must implement factory
     * @return The provide method from this class
     */
    public static Method getFactoryProvideMethod(Class<?> clazz) {
        try {
            return clazz.getMethod(PROVIDE_METHOD);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Returns the default name if one can be found.  Will only work on
     * classes and methods
     *
     * @param parent The parent annotated element
     * @param collector For errors
     * @return null if there is no default name (no Named)
     */
    public static String getDefaultNameFromMethod(Method parent, Collector collector) {
        Named named = parent.getAnnotation(Named.class);
        if (named == null) {
            return null;
        }

        if (named.value() == null || named.value().equals("")) {
            collector.addThrowable(new IllegalArgumentException(
                    "@Named on the provide method of a factory must have an explicit value"));
        }

        return named.value();
    }

    /**
     * Returns the full set of qualifier annotations on this class
     *
     * @param annotatedGuy The element we are searching for qualifiers
     * @param name The name this element must have
     * @param collector The error collector
     * @return A non-null but possibly empty set of qualifiers
     */
    public static Set<Annotation> getAllQualifiers(
            AnnotatedElement annotatedGuy,
            String name,
            Collector collector) {

        Named namedQualifier = null;
        Set<Annotation> retVal = ReflectionHelper.getQualifierAnnotations(annotatedGuy);
        for (Annotation anno : retVal) {
            if (anno instanceof Named) {
                namedQualifier = (Named) anno;
                break;
            }
        }

        if (name == null) {
            if (namedQualifier != null) {
                collector.addThrowable(new IllegalArgumentException("No name was in the descriptor, but this element(" +
                        annotatedGuy + " has a Named annotation with value: " + namedQualifier.value()));

                retVal.remove(namedQualifier);
            }

            return retVal;
        }

        if (namedQualifier == null || namedQualifier.value().equals("")) {
            if (namedQualifier != null) {
                retVal.remove(namedQualifier);
            }

            namedQualifier = new NamedImpl(name);

            retVal.add(namedQualifier);
        }

        if (!name.equals(namedQualifier.value())) {
            collector.addThrowable(new IllegalArgumentException("The class had an @Named qualifier that was inconsistent." +
                    "  The expected name is " + name +
                    " but the annotation has name " + namedQualifier.value()));
        }

        return retVal;
    }


    private static Set<Annotation> getAllQualifiers(
            Annotation memberAnnotations[]) {

        HashSet<Annotation> retVal = new HashSet<Annotation>();
        for (Annotation annotation : memberAnnotations) {
            if (ReflectionHelper.isAnnotationAQualifier(annotation)) {
                retVal.add(annotation);
            }
        }

        return retVal;
    }

    private static boolean isOptional(
            Annotation memberAnnotations[]) {

        for (Annotation annotation : memberAnnotations) {
            if (annotation.annotationType().equals(Optional.class)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSelf(
            Annotation memberAnnotations[]) {

        for (Annotation annotation : memberAnnotations) {
            if (annotation.annotationType().equals(Self.class)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns all the injectees for a constructor
     * @param c The constructor to analyze
     * @param injecteeDescriptor The descriptor of the injectee
     * @return the list (in order) of parameters to the constructor
     */
    public static List<Injectee> getConstructorInjectees(Constructor<?> c, ActiveDescriptor<?> injecteeDescriptor) {
        Type genericTypeParams[] = c.getGenericParameterTypes();
        Annotation paramAnnotations[][] = c.getParameterAnnotations();
        Unqualified unqualified = c.getAnnotation(Unqualified.class);

        List<Injectee> retVal = new LinkedList<Injectee>();

        for (int lcv = 0; lcv < genericTypeParams.length; lcv++) {
            retVal.add(new InjecteeImpl(genericTypeParams[lcv],
                    getAllQualifiers(paramAnnotations[lcv]),
                    lcv,
                    c,
                    isOptional(paramAnnotations[lcv]),
                    isSelf(paramAnnotations[lcv]),
                    unqualified,
                    injecteeDescriptor));
        }

        return retVal;
    }

    /**
     * Returns all the injectees for a constructor
     * @param c The constructor to analyze
     * @param injecteeDescriptor The descriptor of the injectee
     * @return the list (in order) of parameters to the constructor
     */
    public static List<Injectee> getMethodInjectees(Method c, ActiveDescriptor<?> injecteeDescriptor) {
        Type genericTypeParams[] = c.getGenericParameterTypes();
        Annotation paramAnnotations[][] = c.getParameterAnnotations();
        Unqualified unqualified = c.getAnnotation(Unqualified.class);

        List<Injectee> retVal = new LinkedList<Injectee>();

        for (int lcv = 0; lcv < genericTypeParams.length; lcv++) {
            retVal.add(new InjecteeImpl(genericTypeParams[lcv],
                    getAllQualifiers(paramAnnotations[lcv]),
                    lcv,
                    c,
                    isOptional(paramAnnotations[lcv]),
                    isSelf(paramAnnotations[lcv]),
                    unqualified,
                    injecteeDescriptor));
        }

        return retVal;
    }

    private static Set<Annotation> getFieldAdjustedQualifierAnnotations(Field f) {
        Set<Annotation> unadjustedAnnotations = ReflectionHelper.getQualifierAnnotations(f);

        // The getQualifierAnnotations will NOT add a Named annotation that has no
        // value.  So we must now determine if that is the case, and if so add
        // our own NamedImpl based on the name of the field
        Named n = f.getAnnotation(Named.class);
        if (n == null) return unadjustedAnnotations;

        if (n.value() == null || "".equals(n.value())) {
            unadjustedAnnotations.add(new NamedImpl(f.getName()));
        }

        return unadjustedAnnotations;
    }

    /**
     * Returns the injectees for a field
     * @param f The field to analyze
     * @param injecteeDescriptor The descriptor of the injectee
     * @return the list (in order) of parameters to the constructor
     */
    public static List<Injectee> getFieldInjectees(Field f, ActiveDescriptor<?> injecteeDescriptor) {
        List<Injectee> retVal = new LinkedList<Injectee>();
        Unqualified unqualified = f.getAnnotation(Unqualified.class);

        retVal.add(new InjecteeImpl(f.getGenericType(),
                getFieldAdjustedQualifierAnnotations(f),
                -1,
                f,
                isOptional(f.getAnnotations()),
                isSelf(f.getAnnotations()),
                unqualified,
                injecteeDescriptor));

        return retVal;
    }

    /**
     * This method validates a list of injectees to ensure that any self injectees have
     * the proper set of requirements.  It adds IllegalArgumentExceptions to the collector
     * if it finds errors
     *
     * @param givenDescriptor The descriptor associated with this injectee, or null if there are none
     * @param injectees The list of injectees to check.  Only self injectees are validates
     * @param collector The collector to add any errors to
     */
    public static void validateSelfInjectees(ActiveDescriptor<?> givenDescriptor,
                                             List<Injectee> injectees,
                                             Collector collector) {

        for (Injectee injectee : injectees) {
            if (!injectee.isSelf()) continue;

            Class<?> requiredRawClass = ReflectionHelper.getRawClass(injectee.getRequiredType());
            if (requiredRawClass == null || !(ActiveDescriptor.class.equals(requiredRawClass))) {
                collector.addThrowable(new IllegalArgumentException("Injection point " + injectee +
                        " does not have the required type of ActiveDescriptor"));
            }

            if (injectee.isOptional()) {
                collector.addThrowable(new IllegalArgumentException("Injection point " + injectee +
                        " is marked both @Optional and @Self"));
            }

            if (!injectee.getRequiredQualifiers().isEmpty()) {
                collector.addThrowable(new IllegalArgumentException("Injection point " + injectee +
                        " is marked @Self but has other qualifiers"));
            }

            if (givenDescriptor == null) {
                collector.addThrowable(new IllegalArgumentException("A class with injection point " + injectee +
                        " is being created or injected via the non-managed ServiceLocator API"));
            }
        }

    }



    /**
     * Finds the post construct method on this class
     * @param clazz The class to search for the post construct
     * @param collector An error collector
     * @return The post construct method or null
     */
    public static Method findPostConstruct(Class<?> clazz, Collector collector) {
        if (org.glassfish.hk2.api.PostConstruct.class.isAssignableFrom(clazz)) {
            // A little performance optimization
            try {
                return clazz.getMethod(CONVENTION_POST_CONSTRUCT, new Class<?>[0]);
            }
            catch (NoSuchMethodException e) {
                return null;
            }
        }

        boolean containsKey;
        Method retVal;
        synchronized (lock) {
            containsKey = postConstructCache.containsKey(clazz);
            SoftReference<Method> ref = postConstructCache.get(clazz);
            retVal = (ref == null) ? null : ref.get();
        }

        if (!containsKey) {
            getAllMethods(clazz);  // Fills in the cache

            synchronized (lock) {
                SoftReference<Method> ref = postConstructCache.get(clazz);
                retVal = (ref == null) ? null : ref.get();
            }
        }

        if (retVal == null) return null;

        if (retVal.isAnnotationPresent(PostConstruct.class) &&
                (retVal.getParameterTypes().length != 0)) {
            collector.addThrowable(new IllegalArgumentException("The method " + Pretty.method(retVal) +
                        " annotated with @PostConstruct must not have any arguments"));
            return null;
        }

        return retVal;
    }

    /**
     * Finds the pre destroy method on this class
     * @param clazz The class to search for the pre destroy method
     * @param collector An error collector
     * @return The pre destroy method or null
     */
    public static Method findPreDestroy(Class<?> clazz, Collector collector) {
        if (org.glassfish.hk2.api.PreDestroy.class.isAssignableFrom(clazz)) {
            try {
                return clazz.getMethod(CONVENTION_PRE_DESTROY, new Class<?>[0]);
            }
            catch (NoSuchMethodException e) {
                return null;
            }
        }

        boolean containsKey;
        Method retVal;
        synchronized (lock) {
            containsKey = preDestroyCache.containsKey(clazz);
            SoftReference<Method> ref = preDestroyCache.get(clazz);
            retVal = (ref == null) ? null : ref.get();
        }

        if (!containsKey) {
            getAllMethods(clazz);  // Fills in the cache

            synchronized (lock) {
                SoftReference<Method> ref = preDestroyCache.get(clazz);
                retVal = (ref == null) ? null : ref.get();
            }
        }

        if (retVal == null) return null;

        if (retVal.isAnnotationPresent(PreDestroy.class) &&
                (retVal.getParameterTypes().length != 0)) {
            collector.addThrowable(new IllegalArgumentException("The method " + Pretty.method(retVal) +
                    " annotated with @PreDestroy must not have any arguments"));
            return null;
        }

        return retVal;
    }

    /**
     * This method returns a set of qualifiers from an array of qualifiers.
     *
     * TODO  It can also do some sanity checking here (i.e., multiple
     * qualifiers of the same type, that sort of thing)
     *
     * @param qualifiers The qualifiers to convert.  May not be null, but
     * may be zero length
     * @param name The name this set of qualifiers must have
     * @return The set containing all the qualifiers
     */
    public static Set<Annotation> fixAndCheckQualifiers(Annotation qualifiers[], String name) {
        Set<Annotation> retVal = new HashSet<Annotation>();

        Set<String> dupChecker = new HashSet<String>();
        Named named = null;
        for (Annotation qualifier : qualifiers) {
            String annotationType = qualifier.annotationType().getName();
            if (dupChecker.contains(annotationType)) {
                throw new IllegalArgumentException(annotationType + " appears more than once in the qualifier list");
            }
            dupChecker.add(annotationType);

            retVal.add(qualifier);
            if (qualifier instanceof Named) {
                named = (Named) qualifier;

                if (named.value().equals("")) {
                    throw new IllegalArgumentException("The @Named qualifier must have a value");
                }

                if (name != null && !name.equals(named.value())) {
                    throw new IllegalArgumentException("The name passed to the method (" +
                            name + ") does not match the value of the @Named qualifier (" + named.value() + ")");
                }
            }
        }

        if (named == null && name != null) {
            retVal.add(new NamedImpl(name));
        }

        return retVal;
    }

    /**
     * Casts this thing to the given type
     * @param o The thing to cast
     * @return A casted version of o
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }

    private final static Object proxyCreationLock = new Object();
    
    private static <T> T secureCreate(final Class<?> superclass,
            final Class<?>[] interfaces,
            final MethodHandler callback,
            boolean useJDKProxy) {

        /* construct the classloader where the generated proxy will be created --
         * this classloader must have visibility into the javaassist classloader as well as
         * the superclass' classloader
         */
        final ClassLoader delegatingLoader = (ClassLoader) AccessController
                .doPrivileged(new PrivilegedAction<Object>() {

                    @Override
                    public Object run() {
                        // create a delegating classloader that attempts to
                        // load from the superclass' classloader first,
                        // then hk2-locator's classloader second.
                        return new DelegatingClassLoader<T>(
                                superclass.getClassLoader(),
                                ProxyFactory.class.getClassLoader(),
                                ProxyCtl.class.getClassLoader());
                    }
                });

        if (useJDKProxy) {
            return AccessController.doPrivileged(new PrivilegedAction<T>() {

                @SuppressWarnings("unchecked")
                @Override
                public T run() {
                    return (T) Proxy.newProxyInstance(delegatingLoader, interfaces,
                            new MethodInterceptorInvocationHandler(callback));
                }

            });

        }


        return AccessController.doPrivileged(new PrivilegedAction<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T run() {
                synchronized (proxyCreationLock) {
                    ProxyFactory.ClassLoaderProvider originalProvider = ProxyFactory.classLoaderProvider;
                    ProxyFactory.classLoaderProvider = new ProxyFactory.ClassLoaderProvider() {
                        
                        @Override
                        public ClassLoader get(ProxyFactory arg0) {
                            return delegatingLoader;
                        }
                    };
                    
                    try {
                        ProxyFactory proxyFactory = new ProxyFactory();
                        proxyFactory.setInterfaces(interfaces);
                        proxyFactory.setSuperclass(superclass);

                        Class<?> proxyClass = proxyFactory.createClass();

                        try {
                            T proxy = (T) proxyClass.newInstance();

                            ((ProxyObject) proxy).setHandler(callback);

                            return proxy;
                        } catch (Exception e1) {
                            throw new RuntimeException(e1);
                        }
                    }
                    finally {
                        ProxyFactory.classLoaderProvider = originalProvider;
                        
                    }
                }
            }

        });

    }

    /**
     * Creates the service (without the need for an intermediate ServiceHandle
     * to be created)
     *
     * @param root The ultimate parent of this operation
     * @param injectee the injectee we are creating this service for
     * @param locator The locator to use to find services
     * @param handle The ServiceHandle (or null if there is none)
     * @param requestedClass The class for the service we are looking for
     * @return The created service
     */
    @SuppressWarnings("unchecked")
    public static <T> T createService(ActiveDescriptor<T> root,
            Injectee injectee,
            ServiceLocatorImpl locator,
            ServiceHandle<T> handle,
            Class<?> requestedClass) {
        if (root == null) throw new IllegalArgumentException();

        T service = null;

        if (!root.isReified()) {
            root = (ActiveDescriptor<T>) locator.reifyDescriptor(root, injectee);
        }

        if (isProxiable(root, injectee)) {
            boolean isInterface = (requestedClass == null) ? false : requestedClass.isInterface() ;

            final Class<?> proxyClass;
            Class<?> iFaces[];
            if (isInterface) {
                proxyClass = requestedClass;
                iFaces = new Class<?>[2];
                iFaces[0] = proxyClass;
                iFaces[1] = ProxyCtl.class;
            }
            else {
                proxyClass = Utilities.getFactoryAwareImplementationClass(root);

                iFaces = Utilities.getInterfacesForProxy(root.getContractTypes());
            }

            T proxy;
            try {
                proxy = (T) secureCreate(proxyClass,
                    iFaces,
                    new MethodInterceptorImpl(locator, root, handle),
                    isInterface);
            }
            catch (Throwable th) {
                Exception addMe = new IllegalArgumentException("While attempting to create a Proxy for " + proxyClass.getName() +
                        " in proxiable scope " + root.getScope() + " an error occured while creating the proxy");

                if (th instanceof MultiException) {
                    MultiException me = (MultiException) th;

                    me.addError(addMe);

                    throw me;
                }

                MultiException me = new MultiException(th);
                me.addError(addMe);
                throw me;
            }

            return proxy;
        }

        Context<?> context;
        try {
            context = locator.resolveContext(root.getScopeAnnotation());
        }
        catch (Throwable th) {
            Exception addMe = new IllegalStateException("While attempting to create a service for " + root +
                    " in scope " + root.getScope() + " an error occured while locating the context");

            if (th instanceof MultiException) {
                MultiException me = (MultiException) th;

                me.addError(addMe);

                throw me;
            }

            MultiException me = new MultiException(th);
            me.addError(addMe);
            throw me;
        }

        try {
            service = context.findOrCreate(root, handle);
        }
        catch (MultiException me) {
            throw me;
        }
        catch (Throwable th) {
            throw new MultiException(th);
        }

        if (service == null && !context.supportsNullCreation()) {
            throw new MultiException(new IllegalStateException("Context " +
                context + " findOrCreate returned a null for descriptor " + root +
                " and handle " + handle));
        }

        return service;
    }

    private static class MemberKey {
        private final SoftReference<Member> weakBackingMember;
        private final int hashCode;
        private final boolean postConstruct;
        private final boolean preDestroy;

        private MemberKey(Member method, boolean isPostConstruct, boolean isPreDestroy) {
            weakBackingMember = new SoftReference<Member>(method);
            hashCode = calculateHashCode();
            postConstruct = isPostConstruct;
            preDestroy = isPreDestroy;
        }

        private Member getBackingMember() {
            return weakBackingMember.get();
        }

        private boolean isPostConstruct() {
            return postConstruct;
        }

        private boolean isPreDestroy() {
            return preDestroy;
        }

        private int calculateHashCode() {
            Member backingMember = weakBackingMember.get();
            
            int startCode = 0;
            if (backingMember instanceof Method) {
                startCode = 1;
            } else if (backingMember instanceof Constructor) {
                startCode = 2;
            }

            startCode ^= backingMember.getName().hashCode();

            Class<?> parameters[];
            if (backingMember instanceof Method) {
                parameters = ((Method) backingMember).getParameterTypes();
            } else if (backingMember instanceof Constructor) {
                parameters = ((Constructor<?>) backingMember).getParameterTypes();
            } else {
                parameters = new Class<?>[0];
            }

            for (Class<?> param : parameters) {
                startCode ^= param.hashCode();
            }

            return startCode;
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof MemberKey)) return false;
            
            Member backingMember = weakBackingMember.get();

            MemberKey omk = (MemberKey) o;
            if (hashCode != omk.hashCode) return false;

            Member oMember = omk.getBackingMember();

            if (oMember.equals(backingMember)) {
                // If they are the same, they are the same!
                return true;
            }

            if ((backingMember instanceof Field) || (oMember instanceof Field)) {
                // Fields do not inherit
                return false;
            }

            if ((backingMember instanceof Method) && !(oMember instanceof Method)) {
                return false;
            }
            if ((backingMember instanceof Constructor) && !(oMember instanceof Constructor)) {
                return false;
            }

            if (!oMember.getName().equals(backingMember.getName())) {
                return false;
            }

            if (isPrivate(backingMember) || isPrivate(oMember)) {
                // If either are private, they are not the same
                return false;
            }

            Class<?> oParams[];
            Class<?> bParams[];
            if (backingMember instanceof Method) {
                oParams = ((Method) oMember).getParameterTypes();
                bParams = ((Method) backingMember).getParameterTypes();
            } else if (backingMember instanceof Constructor) {
                oParams = ((Constructor<?>) oMember).getParameterTypes();
                bParams = ((Constructor<?>) backingMember).getParameterTypes();
            } else {
                oParams = new Class<?>[0];
                bParams = new Class<?>[0];
            }

            if (oParams.length != bParams.length) return false;
            for (int i = 0; i < oParams.length; i++) {
                if (oParams[i] != bParams[i]) return false;
            }

            return true;
        }
    }

    private static class ScopeInfo {
        private final Annotation scope;
        private final Class<? extends Annotation> annoType;

        private ScopeInfo(Annotation scope, Class<? extends Annotation> annoType) {
            this.scope = scope;
            this.annoType = annoType;
        }

        private Annotation getScope() {
            return scope;
        }

        private Class<? extends Annotation> getAnnoType() {
            return annoType;
        }
    }

    private static class MethodInterceptorInvocationHandler implements InvocationHandler {
        private final MethodHandler interceptor;

        private MethodInterceptorInvocationHandler(MethodHandler interceptor) {
            this.interceptor = interceptor;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            return interceptor.invoke (proxy, method, null, args);
        }

    }
    
    private static final HashSet<String> NOT_INTERCEPTED = new HashSet<String>();
    
    static {
        NOT_INTERCEPTED.add(ServiceLocator.class.getName());
        NOT_INTERCEPTED.add(InstanceLifecycleListener.class.getName());
        NOT_INTERCEPTED.add(InjectionResolver.class.getName());
        NOT_INTERCEPTED.add(ErrorService.class.getName());
        NOT_INTERCEPTED.add(ClassAnalyzer.class.getName());
        NOT_INTERCEPTED.add(DynamicConfigurationListener.class.getName());
        NOT_INTERCEPTED.add(DynamicConfigurationService.class.getName());
        NOT_INTERCEPTED.add(InterceptionService.class.getName());
        NOT_INTERCEPTED.add(ValidationService.class.getName());
        NOT_INTERCEPTED.add(Context.class.getName());
    }
    
    /* package */ static Map<Method, List<MethodInterceptor>> getAllInterceptedMethods(
            ServiceLocatorImpl impl,
            ActiveDescriptor<?> descriptor,
            Class<?> clazz) {
        LinkedHashMap<Method, List<MethodInterceptor>> retVal =
          new LinkedHashMap<Method, List<MethodInterceptor>>();
        if (descriptor == null || clazz == null || isFinal(clazz)) return retVal;
        
        // Make sure it is not one of the special services
        for (String contract : descriptor.getAdvertisedContracts()) {
            if (NOT_INTERCEPTED.contains(contract)) return retVal;
        }
        
        List<InterceptionService> interceptionServices = impl.getInterceptionServices();
        if (interceptionServices.isEmpty()) return retVal;
        
        for (InterceptionService interceptionService : interceptionServices) {
            Filter filter = interceptionService.getDescriptorFilter();
            if (filter instanceof IndexedFilter) {
                IndexedFilter indexedFilter = (IndexedFilter) filter;
                
                String indexedContract = indexedFilter.getAdvertisedContract();
                if (indexedContract != null) {
                    if (!descriptor.getAdvertisedContracts().contains(indexedContract)) continue;
                }
                String name = indexedFilter.getName();
                if (name != null) {
                    if (descriptor.getName() == null) continue;
                    if (!descriptor.getName().equals(name)) continue;
                }
            }
            
            if (filter.matches(descriptor)) {
                for (Method method : getAllMethods(clazz)) {
                    if (isFinal(method)) continue;
                    
                    List<MethodInterceptor> interceptors = interceptionService.getMethodInterceptors(method);
                    if (interceptors != null && !interceptors.isEmpty()) {
                        retVal.put(method, interceptors);
                    }
                }
            }
        }
        
        return retVal;
    }
}
