/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2014 Oracle and/or its affiliates. All rights reserved.
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

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.MethodInterceptor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ClassAnalyzer;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.ContractIndicator;
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
import org.glassfish.hk2.api.InjectionPointIndicator;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.api.InterceptionService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.Proxiable;
import org.glassfish.hk2.api.ProxyCtl;
import org.glassfish.hk2.api.ProxyForSameScope;
import org.glassfish.hk2.api.Rank;
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
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.Constants;
import org.glassfish.hk2.utilities.reflection.MethodWrapper;
import org.glassfish.hk2.utilities.reflection.Pretty;
import org.glassfish.hk2.utilities.reflection.ParameterizedTypeImpl;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.utilities.reflection.ScopeInfo;
import org.glassfish.hk2.utilities.reflection.TypeChecker;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractsProvided;
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
    private final static String USE_SOFT_REFERENCE_PROPERTY = "org.jvnet.hk2.properties.useSoftReference";
    final static boolean USE_SOFT_REFERENCE;
    static {
        USE_SOFT_REFERENCE = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return Boolean.parseBoolean(GeneralUtilities.getSystemProperty(USE_SOFT_REFERENCE_PROPERTY, "true"));
            }

        });
    }
    
    private final static AnnotationInformation DEFAULT_ANNOTATION_INFORMATION = new AnnotationInformation(
            Collections.<Annotation>emptySet(),
            false,
            false,
            null);

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
    public static Class<?> getFactoryAwareImplementationClass(ActiveDescriptor<?> descriptor) {
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
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl != null) {
                try {
                    return ccl.loadClass(implementation);
                }
                catch (Throwable th2) {
                    MultiException me = new MultiException(th);
                    me.addError(th2);
                    
                    throw me;
                }
            }
            
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

            Type firstType = ReflectionHelper.getFirstTypeArgument(advertisedType);
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

            Type firstType = ReflectionHelper.getFirstTypeArgument(type);

            if (firstType instanceof WildcardType) {
                // This should not be possible
                collector.addThrowable(new IllegalArgumentException("The class " +
                        Pretty.clazz(factoryClass) + " has a Wildcard as its type"));
            }
        }

    }
    
    private static boolean hasContract(Class<?> clazz) {
        if (clazz == null) return false;
        
        // We give this one for free, to speed up performance
        if (clazz.isAnnotationPresent(Contract.class)) return true;
        
        for (Annotation clazzAnnotation : clazz.getAnnotations()) {
            if (clazzAnnotation.annotationType().isAnnotationPresent(ContractIndicator.class)) {
                return true;
            }
        }
        
        return false;
    }

    private static Set<Type> getAutoAdvertisedTypes(Type t) {
        LinkedHashSet<Type> retVal = new LinkedHashSet<Type>();
        retVal.add(t);

        Class<?> rawClass = ReflectionHelper.getRawClass(t);
        if (rawClass == null) return retVal;
        
        ContractsProvided provided = rawClass.getAnnotation(ContractsProvided.class);
        if (provided != null) {
            // Need to clear the retVal, since even the parent class may not be
            // in the provided set
            retVal.clear();
            
            for (Class<?> providedContract : provided.value()) {
                retVal.add(providedContract);
            }
            
            return retVal;
        }
        
        Set<Type> allTypes = ReflectionHelper.getAllTypes(t);
        for (Type candidate : allTypes) {
            if (hasContract(ReflectionHelper.getRawClass(candidate))) {
                retVal.add(candidate);
                
            }
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
        String serviceMetadata = null;
        
        // Qualifiers naming dance
        String serviceName = null;
        Service serviceAnno = clazz.getAnnotation(Service.class);
        if (serviceAnno != null) {
            if(!"".equals(serviceAnno.name())) {
              serviceName = serviceAnno.name();
            }
            if (!"".equals(serviceAnno.metadata())) {
                serviceMetadata = serviceAnno.metadata();
            }
        }
        
        qualifiers = ReflectionHelper.getQualifierAnnotations(clazz);
        name = ReflectionHelper.getNameFromAllQualifiers(qualifiers, clazz);
        
        if (serviceName != null && name != null) {
            // They must match
            if (!serviceName.equals(name)) {
                throw new IllegalArgumentException("The class " + clazz.getName() + " has an @Service name of " + serviceName +
                        " and has an @Named value of " + name +" which names do not match");
            }
        }
        else if (name == null && serviceName != null) {
            name = serviceName;
        }
        
        qualifiers = getAllQualifiers(clazz, name, collector);  // Fixes the @Named qualifier if it has no value

        contracts = getAutoAdvertisedTypes(clazz);
        ScopeInfo scopeInfo = getScopeInfo(clazz, null, collector);
        scope = scopeInfo.getAnnoType();
        analyzerName = locator.getPerLocatorUtilities().getAutoAnalyzerName(clazz);

        creator = new ClazzCreator<T>(locator, clazz);

        Map<String, List<String>> metadata = new HashMap<String, List<String>>();
        if (serviceMetadata != null) {
            try {
                ReflectionHelper.readMetadataMap(serviceMetadata, metadata);
            }
            catch (IOException ioe) {
                // If we can not read it, someone else may have
                // a different metadata parser
                metadata.clear();
                
                ReflectionHelper.parseServiceMetadataString(serviceMetadata, metadata);
            }
        }
        
        collector.throwIfErrors();
        
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
        
        int rank = 0;
        Rank ranking = clazz.getAnnotation(Rank.class);
        if (ranking != null) {
            rank = ranking.value();
        }

        AutoActiveDescriptor<T> retVal = new AutoActiveDescriptor<T>(
                clazz,
                creator,
                contracts,
                scope,
                name,
                qualifiers,
                visibility,
                rank,
                proxy,
                proxyForSameScope,
                analyzerName,
                metadata,
                DescriptorType.CLASS);
        
        retVal.setScopeAsAnnotation(scopeInfo.getScope());

        creator.initialize(retVal, analyzerName, collector);

        collector.throwIfErrors();

        return retVal;
    }
    
    /**
     * Creates a reified automatically generated descriptor
     *
     * @param parentClazz The class to create the desciptor for
     * @param locator The service locator for whom we are creating this
     * @return A reified active descriptor
     *
     * @throws MultiException if there was an error in the class
     * @throws IllegalArgumentException If the class is null
     * @throws IllegalStateException If the name could not be determined from the Named annotation
     */
    public static <T> AutoActiveDescriptor<T> createAutoFactoryDescriptor(Class<T> parentClazz, ActiveDescriptor<?> factoryDescriptor, ServiceLocatorImpl locator)
            throws MultiException, IllegalArgumentException, IllegalStateException {
        if (parentClazz == null) throw new IllegalArgumentException();
        
        Collector collector = new Collector();
        
        Type factoryProductionType = Utilities.getFactoryProductionType(parentClazz);
        
        Method provideMethod = Utilities.getFactoryProvideMethod(parentClazz);
        if (provideMethod == null) {
            collector.addThrowable(new IllegalArgumentException("Could not find the provide method on the class " + parentClazz.getName()));
            
            // Guaranteed to throw
            collector.throwIfErrors();
        }

        FactoryCreator<T> creator;
        Set<Annotation> qualifiers;
        Set<Type> contracts;
        Class<? extends Annotation> scope;
        String name;
        Boolean proxy = null;
        Boolean proxyForSameScope = null;
        
        qualifiers = ReflectionHelper.getQualifierAnnotations(provideMethod);
        name = ReflectionHelper.getNameFromAllQualifiers(qualifiers, provideMethod);

        contracts = getAutoAdvertisedTypes(factoryProductionType);
        ScopeInfo scopeInfo = getScopeInfo(provideMethod, null, collector);
        scope = scopeInfo.getAnnoType();

        creator = new FactoryCreator<T>(locator, factoryDescriptor);

        collector.throwIfErrors();

        Map<String, List<String>> metadata = new HashMap<String, List<String>>();
        if (scopeInfo.getScope() != null) {
            BuilderHelper.getMetadataValues(scopeInfo.getScope(), metadata);
        }

        for (Annotation qualifier : qualifiers) {
            BuilderHelper.getMetadataValues(qualifier, metadata);
        }

        UseProxy useProxy = provideMethod.getAnnotation(UseProxy.class);
        if (useProxy != null) {
            proxy = useProxy.value();
        }

        ProxyForSameScope pfss = provideMethod.getAnnotation(ProxyForSameScope.class);
        if (pfss != null) {
            proxyForSameScope = pfss.value();
        }

        DescriptorVisibility visibility = DescriptorVisibility.NORMAL;
        Visibility vi = provideMethod.getAnnotation(Visibility.class);
        if (vi != null) {
            visibility = vi.value();
        }
        
        int rank = 0;
        Rank ranking = provideMethod.getAnnotation(Rank.class);
        if (ranking != null) {
            rank = ranking.value();
        }

        AutoActiveDescriptor<T> retVal = new AutoActiveDescriptor<T>(
                factoryDescriptor.getImplementationClass(),
                creator,
                contracts,
                scope,
                name,
                qualifiers,
                visibility,
                rank,
                proxy,
                proxyForSameScope,
                null,  // provide methods do not have analyzers
                metadata,
                DescriptorType.PROVIDE_METHOD);
        
        retVal.setScopeAsAnnotation(scopeInfo.getScope());

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
            InjectionResolver<?> resolver = locator.getPerLocatorUtilities().getInjectionResolver(locator, field);

            List<SystemInjecteeImpl> injecteeFields = Utilities.getFieldInjectees(field, null);

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
            List<SystemInjecteeImpl> injectees = Utilities.getMethodInjectees(method, null);

            validateSelfInjectees(null, injectees, collector);
            collector.throwIfErrors();

            Object args[] = new Object[injectees.size()];

            for (SystemInjecteeImpl injectee : injectees) {
                InjectionResolver<?> resolver = locator.getPerLocatorUtilities().getInjectionResolver(locator, injectee);
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

        List<SystemInjecteeImpl> injectees = getConstructorInjectees(c, null);

        validateSelfInjectees(null, injectees, collector);
        collector.throwIfErrors();

        Object args[] = new Object[injectees.size()];

        for (SystemInjecteeImpl injectee : injectees) {
            InjectionResolver<?> resolver = locator.getPerLocatorUtilities().getInjectionResolver(locator, injectee);
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
        if (!scopeAnnotation.isAnnotationPresent(Proxiable.class)) return false;


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
     * Gets all the constructors for a given class
     *
     * @param clazz The class to find the constructors of
     * @return A set of Constructors for the given class
     */
    private static Set<Constructor<?>> getAllConstructors(final Class<?> clazz) {
        HashSet<Constructor<?>> retVal = new LinkedHashSet<Constructor<?>>();
        
        Constructor<?> constructors[] = AccessController.doPrivileged(new PrivilegedAction<Constructor<?>[]>() {

            @Override
            public Constructor<?>[] run() {
                return clazz.getDeclaredConstructors();
            }

        });

        for (Constructor<?> constructor : constructors) {
            retVal.add(constructor);
        }

        return retVal;
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
        ClassReflectionHelper crh = locator.getClassReflectionHelper();

        for (MethodWrapper methodWrapper : crh.getAllMethods(annotatedType)) {
            Method method = methodWrapper.getMethod();
            
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
     * Finds the post construct method on this class
     * @param clazz The class to search for the post construct
     * @param collector An error collector
     * @return The post construct method or null
     */
    public static Method findPostConstruct(Class<?> clazz, ServiceLocatorImpl locator, Collector collector) {
        try {
            return locator.getClassReflectionHelper().findPostConstruct(clazz, org.glassfish.hk2.api.PostConstruct.class);
        }
        catch (IllegalArgumentException iae) {
            collector.addThrowable(iae);
            return null;
        }
    }
    
    /**
     * Finds the pre destroy method on this class
     * @param clazz The class to search for the pre destroy method
     * @param collector An error collector
     * @return The pre destroy method or null
     */
    public static Method findPreDestroy(Class<?> clazz, ServiceLocatorImpl locator, Collector collector)  {
        try {
            return locator.getClassReflectionHelper().findPreDestroy(clazz, org.glassfish.hk2.api.PreDestroy.class);
        }
        catch (IllegalArgumentException iae) {
            collector.addThrowable(iae);
            return null;
        }
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
        ClassReflectionHelper crh = locator.getClassReflectionHelper();
        
        Set<Field> fields = crh.getAllFields(annotatedType);

        for (Field field : fields) {
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
            if (anno.annotationType().getAnnotation(InjectionPointIndicator.class) != null) {
                return true;
            }
            
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
                if (paramAnno.annotationType().getAnnotation(InjectionPointIndicator.class) != null) {
                    return true;
                }
                
                if (locator.isInjectAnnotation(paramAnno, isConstructor)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Represents a cache miss, and will fetch all of the information needed about the
     * AnnotatedElement in order to quickly determine what its resolver would be
     * 
     * @param annotatedElement The raw annotated element that can be used to
     * calculate the information needed to determine the resolver
     * @return An annotated element constructed from the information in the annotatedElement
     */
    static AnnotatedElementAnnotationInfo computeAEAI(AnnotatedElement annotatedElement) {

        if (annotatedElement instanceof Method) {

            final Method m = (Method) annotatedElement;
            return new AnnotatedElementAnnotationInfo(m.getAnnotations(), true, m.getParameterAnnotations(), false);

        } else if (annotatedElement instanceof Constructor) {

            final Constructor<?> c = (Constructor<?>) annotatedElement;
            return new AnnotatedElementAnnotationInfo(c.getAnnotations(), true, c.getParameterAnnotations(), true);

        } else {
            return new AnnotatedElementAnnotationInfo(annotatedElement.getAnnotations(), false, new Annotation[0][], false);
        }
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
                }
                
                if (current.annotationType().isAnnotationPresent(Inherited.class)) {
                    winnerScope = current;
                    break;
                }

                // This non-inherited annotation wipes out all scopes above it
                break;
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
            return new ScopeInfo(ServiceLocatorUtilities.getSingletonAnnotation(), Singleton.class);
        }

        if (defaultScope != null && defaultScope.getScope() != null) {
            Class<? extends Annotation> descScope = (Class<? extends Annotation>)
                    loadClass(defaultScope.getScope(), defaultScope, collector);
            if (descScope != null) {
                return new ScopeInfo(null, descScope);
            }
        }

        return new ScopeInfo(ServiceLocatorUtilities.getPerLookupAnnotation(), PerLookup.class);

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
    public static ScopeInfo getScopeAnnotationType(
            AnnotatedElement annotatedGuy,
            Descriptor defaultScope,
            Collector collector) {
        ScopeInfo si = getScopeInfo(annotatedGuy, defaultScope, collector);
        return si;
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
    
    private static AnnotationInformation getParamInformation(Annotation memberAnnotations[]) {
        boolean useDefault = true;
        
        Set<Annotation> qualifiers = null;
        boolean optional = false;
        boolean self = false;
        Unqualified unqualified = null;
        
        for (Annotation anno : memberAnnotations) {
            if (ReflectionHelper.isAnnotationAQualifier(anno)) {
                if (qualifiers == null) qualifiers = new HashSet<Annotation>();
                qualifiers.add(anno);
                useDefault = false;
            }
            else if (Optional.class.equals(anno.annotationType())) {
                optional = true;
                useDefault = false;
            }
            else if (Self.class.equals(anno.annotationType())) {
                self = true;
                useDefault = false;
            }
            else if (Unqualified.class.equals(anno.annotationType())) {
                unqualified = (Unqualified) anno;
                useDefault = false;
            }
        }
        
        if (useDefault) return DEFAULT_ANNOTATION_INFORMATION;
        
        if (qualifiers == null) qualifiers = DEFAULT_ANNOTATION_INFORMATION.qualifiers;
        
        return new AnnotationInformation(
                qualifiers,
                optional,
                self,
                unqualified);
    }

    /**
     * Returns all the injectees for a constructor
     * @param c The constructor to analyze
     * @param injecteeDescriptor The descriptor of the injectee
     * @return the list (in order) of parameters to the constructor
     */
    public static List<SystemInjecteeImpl> getConstructorInjectees(Constructor<?> c, ActiveDescriptor<?> injecteeDescriptor) {
        Type genericTypeParams[] = c.getGenericParameterTypes();
        Annotation paramAnnotations[][] = c.getParameterAnnotations();

        List<SystemInjecteeImpl> retVal = new LinkedList<SystemInjecteeImpl>();

        for (int lcv = 0; lcv < genericTypeParams.length; lcv++) {
            AnnotationInformation ai = getParamInformation(paramAnnotations[lcv]);
            
            retVal.add(new SystemInjecteeImpl(genericTypeParams[lcv],
                    ai.qualifiers,
                    lcv,
                    c,
                    ai.optional,
                    ai.self,
                    ai.unqualified,
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
    public static List<SystemInjecteeImpl> getMethodInjectees(Method c, ActiveDescriptor<?> injecteeDescriptor) {
        Type genericTypeParams[] = c.getGenericParameterTypes();
        Annotation paramAnnotations[][] = c.getParameterAnnotations();

        List<SystemInjecteeImpl> retVal = new LinkedList<SystemInjecteeImpl>();

        for (int lcv = 0; lcv < genericTypeParams.length; lcv++) {
            AnnotationInformation ai = getParamInformation(paramAnnotations[lcv]);
            
            retVal.add(new SystemInjecteeImpl(genericTypeParams[lcv],
                    ai.qualifiers,
                    lcv,
                    c,
                    ai.optional,
                    ai.self,
                    ai.unqualified,
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
    public static List<SystemInjecteeImpl> getFieldInjectees(Field f, ActiveDescriptor<?> injecteeDescriptor) {
        List<SystemInjecteeImpl> retVal = new LinkedList<SystemInjecteeImpl>();
        AnnotationInformation ai = getParamInformation(f.getAnnotations());

        retVal.add(new SystemInjecteeImpl(f.getGenericType(),
                getFieldAdjustedQualifierAnnotations(f),
                -1,
                f,
                ai.optional,
                ai.self,
                ai.unqualified,
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
                                             List<SystemInjecteeImpl> injectees,
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
            if (!proxiesAvailable()) {
                throw new IllegalStateException("A descriptor " + root + " requires a proxy, but the proxyable library is not on the classpath");
            }
            
            return locator.getPerLocatorUtilities().getProxyUtilities().generateProxy(requestedClass, locator, root, handle);
        }

        Context<?> context;
        try {
            context = locator.resolveContext(root.getScopeAnnotation());
        }
        catch (Throwable th) {
            if (injectee != null && injectee.isOptional()) return null;
            
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
    
    private final static Interceptors EMTPY_INTERCEPTORS = new Interceptors() {

        @Override
        public Map<Method, List<MethodInterceptor>> getMethodInterceptors() {
            return null;
        }

        @Override
        public List<ConstructorInterceptor> getConstructorInterceptors() {
            return null;
        }
        
    };
    
    /* package */ static Interceptors getAllInterceptors(
            ServiceLocatorImpl impl,
            ActiveDescriptor<?> descriptor,
            Class<?> clazz,
            Constructor<?> c) {
        if (descriptor == null || clazz == null || isFinal(clazz)) return EMTPY_INTERCEPTORS;
        ClassReflectionHelper crh = impl.getClassReflectionHelper();
        
        List<InterceptionService> interceptionServices = impl.getInterceptionServices();
        if (interceptionServices == null || interceptionServices.isEmpty()) return EMTPY_INTERCEPTORS;
        
        // Make sure it is not one of the special services
        for (String contract : descriptor.getAdvertisedContracts()) {
            if (NOT_INTERCEPTED.contains(contract)) return EMTPY_INTERCEPTORS;
        }
        
        final LinkedHashMap<Method, List<MethodInterceptor>> retVal =
                new LinkedHashMap<Method, List<MethodInterceptor>>();
        final ArrayList<ConstructorInterceptor> cRetVal = new ArrayList<ConstructorInterceptor>();
        
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
                for (MethodWrapper methodWrapper : crh.getAllMethods(clazz)) {
                    Method method = methodWrapper.getMethod();
                    
                    if (isFinal(method)) continue;
                    
                    List<MethodInterceptor> interceptors = interceptionService.getMethodInterceptors(method);
                    if (interceptors != null && !interceptors.isEmpty()) {
                        List<MethodInterceptor> addToMe = retVal.get(method);
                        if (addToMe == null) {
                            addToMe = new ArrayList<MethodInterceptor>();
                            retVal.put(method, addToMe);
                        }
                        
                        addToMe.addAll(interceptors);
                    }
                }
                
                List<ConstructorInterceptor> cInterceptors = interceptionService.getConstructorInterceptors(c);
                if (cInterceptors != null && !cInterceptors.isEmpty()) {
                    cRetVal.addAll(cInterceptors);
                }
            }
        }
        
        return new Interceptors() {

            @Override
            public Map<Method, List<MethodInterceptor>> getMethodInterceptors() {
                return retVal;
            }

            @Override
            public List<ConstructorInterceptor> getConstructorInterceptors() {
                return cRetVal;
            }
            
        };
    }
    
    /**
     * This code uses the TypeChecker but does some extra checking if
     * the types are annotations
     * 
     * @param requiredType The type this must conform to
     * @param beanType The type of the bean we are checking
     * @return true if beanType is safely assignable to requiredType
     */
    @SuppressWarnings("unchecked")
    public static boolean isTypeSafe(Type requiredType, Type beanType) {
        if (TypeChecker.isRawTypeSafe(requiredType, beanType)) return true;
        
        Class<?> requiredClass = ReflectionHelper.getRawClass(requiredType);
        if (requiredClass == null) {
            return false;
        }
        
        // We do some extra checking if we are looking at annotations
        if (!requiredClass.isAnnotation()) return false;
        
        Class<?> beanClass = ReflectionHelper.getRawClass(beanType);
        if (beanClass == null) {
            return false;
        }
        
        if (beanClass.isAnnotationPresent((Class<? extends Annotation>) requiredClass)) {
            return true;
        }
                
         Class<? extends Annotation> trueScope = Utilities.getScopeAnnotationType(beanClass, null);
         if (trueScope.equals((Class<? extends Annotation>) requiredClass)) {
             return true;
         }
         
         return false;
    }
    
    private static Boolean proxiesAvailable = null;
    
    /**
     * Returns true if the system can create proxies, false otherwise
     * 
     * @return true if the system can create proxies, false otherwise
     */
    public synchronized static boolean proxiesAvailable() {
        if (proxiesAvailable != null) {
            return proxiesAvailable;
        }
        
        ClassLoader loader = Utilities.class.getClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        
        try {
            loader.loadClass("javassist.util.proxy.MethodHandler");
            proxiesAvailable = true;
            return true;
        }
        catch (Throwable th) {
            proxiesAvailable = false;
            return false;
        }
    }
    
    /**
     * The return type from getAllInterceptors
     * 
     * @author jwells
     *
     */
    public interface Interceptors {
        /**
         * Gets the method interceptors
         * @return The possibly null set of method interceptors
         */
        public Map<Method, List<MethodInterceptor>> getMethodInterceptors();
        
        /**
         * Gets the constructor interceptors
         * @return The possibly null set of constructor interceptors
         */
        public List<ConstructorInterceptor> getConstructorInterceptors();
    }
    
    private static class AnnotationInformation {
        private final Set<Annotation> qualifiers;
        private final boolean optional;
        private final boolean self;
        private final Unqualified unqualified;
        
        private AnnotationInformation(Set<Annotation> qualifiers,
                boolean optional,
                boolean self,
                Unqualified unqualified) {
            this.qualifiers = qualifiers;
            this.optional = optional;
            this.self = self;
            this.unqualified = unqualified;
        }
    }
}
