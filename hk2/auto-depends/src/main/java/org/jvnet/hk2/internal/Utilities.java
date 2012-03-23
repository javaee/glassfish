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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.Proxiable;
import org.glassfish.hk2.api.ServiceLocator;
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
    /**
     * Returns all the interfaces the proxy must implement
     * @param contracts All of the advertised contracts
     * @return The array of contracts to add to the proxy
     */
    public static Class<?>[] getInterfacesForProxy(Set<Type> contracts) {
        LinkedList<Class<?>> retVal = new LinkedList<Class<?>>();
        
        for (Type type : contracts) {
            Class<?> rawClass = getRawClass(type);
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
        if (scope.isAnnotationPresent(Proxiable.class)) return true;
        return false;
    }
    /**
     * Returns the type that this is a factory for
     * 
     * @param factory The factory for this type
     * @return The type of this factory
     */
    private static Type getFactoryType(Type factory) {
        if (factory instanceof Class) return Object.class;
        if (!(factory instanceof ParameterizedType)) {
            throw new AssertionError("Unknown Factory type " + factory);
        }
        
        ParameterizedType pt = (ParameterizedType) factory;
        return pt.getActualTypeArguments()[0];
    }
    
    private static String getFactoryName(Type type) {
        Type factoryType = getFactoryType(type);
        
        Class<?> clazz = getRawClass(factoryType);
        if (clazz == null) {
            throw new IllegalArgumentException("A factory implmentation may not have a wildcard type or type variable as its actual type");
        }
        
        return clazz.getName();
    }
    
    /**
     * Gets the name of the factory from the implementation class
     * 
     * @param implClass This class must be an implementation of Factory
     * @return The name of this factory
     */
    public static String getFactoryName(Class<?> implClass) {
        Type types[] = implClass.getGenericInterfaces();
        
        for (Type type : types) {    
            Class<?> clazz = getRawClass(type);
            if (clazz == null || !Factory.class.equals(clazz)) continue;
            
            // Found the factory!
            return getFactoryName(type);
        }
        
        throw new AssertionError("getFactoryName was given a non-factory " + implClass);
    }
    
    /**
     * Gets the name of the factory from the implementation class
     * 
     * @param implClass This class must be an implementation of Factory
     * @return The name of this factory
     */
    public static Type getFactoryType(Class<?> implClass) {
        Type types[] = implClass.getGenericInterfaces();
        
        for (Type type : types) {    
            Class<?> clazz = getRawClass(type);
            if (clazz == null || !Factory.class.equals(clazz)) continue;
            
            // Found the factory!
            return getFactoryType(type);
        }
        
        throw new AssertionError("getFactoryType was given a non-factory " + implClass);
    }
    
    /**
     * Returns the first thing found in the set
     * 
     * @param set The set from which to get the first element
     * @return the first thing found in the set
     */
    public static <T> T getFirstThingInSet(Set<T> set) {
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
    private static Set<Field> getAllFields(Class<?> clazz) {
        HashSet<Field> retVal = new HashSet<Field>();
        
        HashSet<MemberKey> keys = new HashSet<MemberKey>();
        
        getAllFieldKeys(clazz, keys);
        
        for (MemberKey key : keys) {
            retVal.add((Field) key.getBackingMember());
        }
        
        return retVal;
    }
    
    private static void getAllFieldKeys(Class<?> clazz, Set<MemberKey> currentFields) {
        if (clazz == null) return;
        
        // Do superclasses first, so that inherited methods are
        // overriden in the set
        getAllFieldKeys(clazz.getSuperclass(), currentFields);
        
        for (Field field : clazz.getDeclaredFields()) {
            currentFields.add(new MemberKey(field));
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
                Integer.MAX_VALUE,
                locator.getLocatorId());
        
        return retVal;
    }
    
    /**
     * Checks to be sure a scope annotation is propertly annotated with Scope
     * 
     * @param annotation the annotation to check
     */
    public static void checkScopeAnnotation(Class<? extends Annotation> annotation) {
        if (annotation == null) throw new IllegalArgumentException();
        
        if (!annotation.isAnnotationPresent(Scope.class)) {
            throw new IllegalArgumentException("The annotation " + annotation + " is not a scope");
        }
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
            
            if (getInjectAnnotation(locator, constructor) != null) {
                if (aConstructorWithInjectAnnotation != null) {
                    collector.addThrowable(new IllegalArgumentException("There is more than one constructor on class " +
                      Pretty.clazz(annotatedType)));
                    return null;
                }
                
                aConstructorWithInjectAnnotation = constructor;
            }
            
        }
        
        if (aConstructorWithInjectAnnotation != null) {
            return aConstructorWithInjectAnnotation;
        }
        
        return zeroArgConstructor;
    }
    
    /**
     * Given the type parameter gets the raw type represented
     * by the type, or null if this has no associated raw class
     * @param type The type to find the raw class on
     * @return The raw class associated with this type
     */
    public static Class<?> getRawClass(Type type) {
        if (type == null) return null;
        
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return (Class<?>) rawType;
            }
        }
        
        return null;
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
    
    private static void getAllConstructorKeys(Class<?> clazz, Set<MemberKey> currentConstructors) {
        if (clazz == null) return;
        
        // Constructors for the superclass do not equal constructors for this class
        
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            currentConstructors.add(new MemberKey(constructor));
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
        HashSet<Method> retVal = new HashSet<Method>();
        
        HashSet<MemberKey> keys = new HashSet<MemberKey>();
        
        getAllMethodKeys(clazz, keys);
        
        for (MemberKey key : keys) {
            retVal.add((Method) key.getBackingMember());
        }
        
        return retVal;
    }
    
    private static void getAllMethodKeys(Class<?> clazz, Set<MemberKey> currentMethods) {
        if (clazz == null) return;
        
        // Do superclasses first, so that inherited methods are
        // overriden in the set
        getAllMethodKeys(clazz.getSuperclass(), currentMethods);
        
        for (Method method : clazz.getDeclaredMethods()) {
            currentMethods.add(new MemberKey(method));
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
        HashSet<Method> retVal = new HashSet<Method>();
        
        for (Method method : getAllMethods(annotatedType)) {
            if (getInjectAnnotation(locator, method) == null) {
                // Not an initializer method
                continue;
            }
            
            if (!hasCorrectInitializerMethodModifiers(method)) {
                errorCollector.addThrowable(new IllegalArgumentException(
                        "An initializer method " + Pretty.method(method) + 
                        " is static or abstract"));
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
        HashSet<Field> retVal = new HashSet<Field>();
        
        for (Field field : getAllFields(annotatedType)) {
            if (getInjectAnnotation(locator, field) == null) {
                // Not an initializer field
                continue;
            }
            
            if (!hasCorrectInitializerFieldModifiers(field)) {
                errorCollector.addThrowable(new IllegalArgumentException("The field " +
                  Pretty.field(field) + " may not be static or final"));
                continue;
            }
            
            retVal.add(field);
        }
        
        return retVal;
    }
    
    /**
     * Gets the annotation that was used for the injection
     * 
     * @param beanManager The bean manager to use (as it will get all
     * the annotations that were added on as well as the normal Inject)
     * @param member The member to check for
     * @return The annotation that is the inject annotation, or null
     * if no inject annotation was found
     */
    private static Annotation getInjectAnnotation(ServiceLocatorImpl locator, AnnotatedElement annotated) {
        for (Annotation anno : annotated.getAnnotations()) {
            if (locator.isInjectAnnotation(anno)) return anno;
        }
        
        return null;
    }
    
    private static boolean hasCorrectInitializerMethodModifiers(Method member) {
        if (isStatic(member)) return false;
        if (isAbstract(member)) return false;
        
        return true;
    }
    
    private static boolean hasCorrectInitializerFieldModifiers(Field field) {
        if (isStatic(field)) return false;
        if (isFinal(field)) return false;
        
        return true;
    }
    
    /**
     * Returns true if the underlying member is static
     * 
     * @param member The non-null member to test
     * @return true if the member is static
     */
    public static boolean isStatic(Member member) {
        int modifiers = member.getModifiers();
        
        return ((modifiers & Modifier.STATIC) != 0);
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
    
    /**
     * Returns the scope of this thing
     * 
     * @param annotatedGuy The annotated class or producer method
     * @param collector The error collector
     * @return The scope of this class or producer method.  If no scope is
     * found will return the dependent scope
     */
    public static Class<? extends Annotation> getScopeAnnotationType(
            AnnotatedElement annotatedGuy,
            Collector collector) {
        boolean epicFail = false;
        Class<? extends Annotation> retVal = null;
        for (Annotation annotation : annotatedGuy.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Scope.class)) {
                if (retVal != null) {
                    collector.addThrowable(new IllegalArgumentException("The type " + annotatedGuy +
                            " may not have more than one scope.  It has at least " + Pretty.clazz(retVal) +
                            " and " + Pretty.clazz(annotation.annotationType())));
                    epicFail = true;
                    continue;
                }
                
                retVal = annotation.annotationType();
            }
        }
        
        if (epicFail) return null;
        
        if (retVal != null) return retVal;
        
        return (annotatedGuy.isAnnotationPresent(Service.class)) ? Singleton.class : PerLookup.class; 
    }
    
    /**
     * Returns an injection resolver for this AnnotatedElement
     * 
     * @param locator The locator to use when finding the resolver
     * @param annotatedGuy The annotated class or producer method
     * @return The scope of this class or producer method.  If no scope is
     * found will return the dependent scope
     */
    public static InjectionResolver getInjectionResolver(
            ServiceLocatorImpl locator,
            AnnotatedElement annotatedGuy) {
        Annotation injectAnnotation = getInjectAnnotation(locator, annotatedGuy);
        
        Class<? extends Annotation> injectType = (injectAnnotation == null) ?
                Inject.class : injectAnnotation.annotationType() ;
        
        return locator.getInjectionResolver(injectType);
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
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }
    
    /**
     * Gets all the interfaces on this particular class (but not any
     * superclasses of this class).
     */
    private static void addAllGenericInterfaces(Type types[], Set<Type> closures) {
        
        for (Type type : types) {
            closures.add(type);
            
            Class<?> rawClass = getRawClass(type);
            if (rawClass != null) {
                addAllGenericInterfaces(rawClass.getGenericInterfaces(), closures);
            }
        }
    }
    
    /**
     * Returns the type closure of the given class
     * 
     * @param ofClass The full type closure of the given class
     * with nothing omitted (normal case).  May not be null
     * @return The non-null (and never empty) set of classes
     * that this class can be assigned to
     */
    private static Set<Type> getTypeClosure(Type ofType) {
        HashSet<Type> retVal = new HashSet<Type>();
        
        Type currentType = ofType;
        while (currentType != null) {
            Class<?> rawClass = getRawClass(currentType);
            if (rawClass == null) {
                break;
            }
            retVal.add(currentType);
            
            addAllGenericInterfaces(rawClass.getGenericInterfaces(), retVal);
            
            currentType = rawClass.getGenericSuperclass();
        }
        
        return retVal;
    }
    
    /**
     * Returns the type closure, as restricted by the classes listed in the
     * set of contracts implemented
     * 
     * @param ofType The type to check
     * @param contracts The contracts this type is allowed to handle
     * @return The type closure restricted to the contracts
     */
    public static Set<Type> getTypeClosure(Type ofType, Set<String> contracts) {
        Set<Type> closure = getTypeClosure(ofType);
        
        HashSet<Type> retVal = new HashSet<Type>();
        for (Type t : closure) {
            Class<?> rawClass = getRawClass(t);
            if (rawClass == null) continue;
            
            if (contracts.contains(rawClass.getName())) {
                retVal.add(t);
            }
        }
        
        return retVal;
    }
    
    private static boolean isAnnotationAQualifier(Annotation anno) {
        Class<? extends Annotation> annoType = anno.annotationType();
        return annoType.isAnnotationPresent(Qualifier.class);
    }
    
    private static String calculateNamedValue(AnnotatedElement ae) {
        Class<?> myClass;
        if (ae instanceof Class) {
            myClass = (Class<?>) ae;
        }
        else {
            Method m = (Method) ae;
            myClass = m.getDeclaringClass();
        }
        
        return Pretty.clazz(myClass);
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
        HashSet<Annotation> retVal = new HashSet<Annotation>();
        for (Annotation annotation : annotatedGuy.getAnnotations()) {
            if (isAnnotationAQualifier(annotation)) {
                retVal.add(annotation);
                if (annotation instanceof Named) {
                    namedQualifier = (Named) annotation;
                }
            }
        }
        
        if (name == null) {
            if (namedQualifier != null && namedQualifier.value().equals("") ) {
                retVal.remove(namedQualifier);
                
                namedQualifier = new NamedImpl(calculateNamedValue(annotatedGuy));
                
                retVal.add(namedQualifier);
            }
            
            return retVal;
        }
        
        if (namedQualifier == null || namedQualifier.value().equals("") ) {
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
            AnnotatedElement annotatedGuy) {
        
        HashSet<Annotation> retVal = new HashSet<Annotation>();
        for (Annotation annotation : annotatedGuy.getAnnotations()) {
            if (isAnnotationAQualifier(annotation)) {
                retVal.add(annotation);
            }
        }
        
        return retVal;
    }
    
    private static Set<Annotation> getAllQualifiers(
            Annotation memberAnnotations[]) {
        
        HashSet<Annotation> retVal = new HashSet<Annotation>();
        for (Annotation annotation : memberAnnotations) {
            if (isAnnotationAQualifier(annotation)) {
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
    
    /**
     * Returns all the injectees for a constructor
     * @param c The constructor to analyze
     * @return the list (in order) of parameters to the constructor
     */
    public static List<Injectee> getConstructorInjectees(Constructor<?> c) {
        Type genericTypeParams[] = c.getGenericParameterTypes();
        Annotation paramAnnotations[][] = c.getParameterAnnotations();
        
        List<Injectee> retVal = new LinkedList<Injectee>();
        
        for (int lcv = 0; lcv < genericTypeParams.length; lcv++) {
            retVal.add(new InjecteeImpl(genericTypeParams[lcv],
                    getAllQualifiers(paramAnnotations[lcv]),
                    lcv,
                    c,
                    isOptional(paramAnnotations[lcv])));
        }
        
        return retVal;
    }
    
    /**
     * Returns all the injectees for a constructor
     * @param c The constructor to analyze
     * @return the list (in order) of parameters to the constructor
     */
    public static List<Injectee> getMethodInjectees(Method c) {
        Type genericTypeParams[] = c.getGenericParameterTypes();
        Annotation paramAnnotations[][] = c.getParameterAnnotations();
        
        List<Injectee> retVal = new LinkedList<Injectee>();
        
        for (int lcv = 0; lcv < genericTypeParams.length; lcv++) {
            retVal.add(new InjecteeImpl(genericTypeParams[lcv],
                    getAllQualifiers(paramAnnotations[lcv]),
                    lcv,
                    c,
                    isOptional(paramAnnotations[lcv])));
        }
        
        return retVal;
    }
    
    /**
     * Returns the injectees for a field
     * @param f The field to analyze
     * @return the list (in order) of parameters to the constructor
     */
    public static List<Injectee> getFieldInjectees(Field f) {
        List<Injectee> retVal = new LinkedList<Injectee>();
        
        retVal.add(new InjecteeImpl(f.getGenericType(),
                getAllQualifiers(f),
                -1,
                f,
                isOptional(f.getAnnotations())));
        
        return retVal;
    }
    
    private final static String CONVENTION_POST_CONSTRUCT = "postConstruct";
    private final static String CONVENTION_PRE_DESTROY = "preDestroy";
    
    /**
     * Finds the post construct method on this class
     * @param clazz The class to search for the post construct
     * @param collector An error collector
     * @return The post construct method or null
     */
    public static Method findPostConstruct(Class<?> clazz, Collector collector) {
        if (org.glassfish.hk2.PostConstruct.class.isAssignableFrom(clazz)) {
            // A little performance optimization
            return null;  
        }
        
        for (Method method : getAllMethods(clazz)) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                if (method.getParameterTypes().length != 0) {
                    collector.addThrowable(new IllegalArgumentException("The method " + Pretty.method(method) +
                            " annotated with @PostConstruct must not have any arguments"));
                    return null;
                }
                
                return method;
            }
            
            if (method.getParameterTypes().length != 0) continue;
            if (!method.getName().equals(CONVENTION_POST_CONSTRUCT)) continue;
            
            return method;
        }
        
        return null;
    }
    
    /**
     * Finds the pre destroy method on this class
     * @param clazz The class to search for the pre destroy method
     * @param collector An error collector
     * @return The pre destroy method or null
     */
    public static Method findPreDestroy(Class<?> clazz, Collector collector) {
        if (org.glassfish.hk2.PreDestroy.class.isAssignableFrom(clazz)) {
            // A little performance optimization
            return null;  
        }
        
        for (Method method : getAllMethods(clazz)) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                if (method.getParameterTypes().length != 0) {
                    collector.addThrowable(new IllegalArgumentException("The method " + Pretty.method(method) +
                            " annotated with @PreDestroy must not have any arguments"));
                    return null;
                }
                
                return method;
            }
            
            if (method.getParameterTypes().length != 0) continue;
            if (!method.getName().equals(CONVENTION_PRE_DESTROY)) continue;
            
            return method;
        }
        
        return null;
    }
    
    /**
     * This version of invoke is CCL neutral (it will return with the
     * same CCL as what it went in with)
     * 
     * @param c the constructor to call
     * @param args The arguments to invoke (may not be null)
     * @return The return from the invocation
     * @throws Throwable The unwrapped throwable thrown by the method
     */
    public static Object makeMe(Constructor<?> c, Object args[])
            throws Throwable {
        ClassLoader currentCCL = Thread.currentThread().getContextClassLoader();
        
        try {
            return c.newInstance(args);
        }
        catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        }
        finally {
            Thread.currentThread().setContextClassLoader(currentCCL);
        }
    }
    
    /**
     * This version of invoke is CCL neutral (it will return with the
     * same CCL as what it went in with)
     * 
     * @param m the method to invoke
     * @param o the object on which to invoke it
     * @param args The arguments to invoke (may not be null)
     * @return The return from the invocation
     * @throws Throwable The unwrapped throwable thrown by the method
     */
    public static Object invoke(Object o, Method m, Object args[])
            throws Throwable {
        if (isStatic(m)) {
            o = null;
        }
        
        ClassLoader currentCCL = Thread.currentThread().getContextClassLoader();
        
        try {
            return m.invoke(o, args);
        }
        catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        }
        finally {
            Thread.currentThread().setContextClassLoader(currentCCL);
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
            if (!isAnnotationAQualifier(qualifier)) {
                throw new IllegalArgumentException(Pretty.clazz(qualifier.annotationType()) + " is not a qualifier");
            }
            
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
    
    private static class MemberKey {
        private final Member backingMember;
        
        private MemberKey(Member method) {
            backingMember = method;
        }
        
        private Member getBackingMember() {
            return backingMember;
        }
        
        public int hashCode() {
            int startCode = 0;
            if (backingMember instanceof Method) {
                startCode = 1;
            }
            else if (backingMember instanceof Constructor) {
                startCode = 2;
            }
            
            startCode ^= backingMember.getName().hashCode();
            
            Class<?> parameters[];
            if (backingMember instanceof Method) {
                parameters = ((Method) backingMember).getParameterTypes();
            }
            else if (backingMember instanceof Constructor) {
                parameters = ((Constructor<?>) backingMember).getParameterTypes();
            }
            else {
                parameters = new Class<?>[0];
            }
            
            for (Class<?> param : parameters) {
                startCode ^= param.hashCode();
            }
            
            return startCode;
        }
        
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof MemberKey)) return false;
            
            MemberKey omk = (MemberKey) o;
            
            Member oMember = omk.backingMember;
            
            if ((backingMember instanceof Method) && !(oMember instanceof Method)) {
                return false;
            }
            if ((backingMember instanceof Constructor) && !(oMember instanceof Constructor)) {
                return false;
            }
            
            if (!oMember.getName().equals(backingMember.getName())) return false;
            
            Class<?> oParams[];
            Class<?> bParams[];
            if (backingMember instanceof Method) {
                oParams = ((Method) oMember).getParameterTypes();
                bParams = ((Method) backingMember).getParameterTypes();
            }
            else if (backingMember instanceof Constructor) {
                oParams = ((Constructor<?>) oMember).getParameterTypes();
                bParams = ((Constructor<?>) backingMember).getParameterTypes();
            }
            else {
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

}
