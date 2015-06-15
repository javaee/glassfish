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
package org.glassfish.hk2.utilities.reflection;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;

import org.glassfish.hk2.utilities.reflection.internal.MethodWrapperImpl;

/**
 * @author jwells
 *
 */
public final class ReflectionHelper {
    private final static HashSet<Character> ESCAPE_CHARACTERS = new HashSet<Character>();
    private final static char ILLEGAL_CHARACTERS[] = {
        '{' , '}', '[', ']', ':', ';', '=', ',', '\\'
    };
    private final static HashMap<Character, Character> REPLACE_CHARACTERS = new HashMap<Character, Character>();

    static {
        for (char illegal : ILLEGAL_CHARACTERS) {
            ESCAPE_CHARACTERS.add(illegal);
        }

        REPLACE_CHARACTERS.put('\n', 'n');
        REPLACE_CHARACTERS.put('\r', 'r');
    }

    private final static String EQUALS_STRING = "=";
    private final static String COMMA_STRING = ",";
    private final static String QUOTE_STRING = "\"";

    /**
     * Given the type parameter gets the raw type represented
     * by the type, or null if this has no associated raw class
     * @param type The type to find the raw class on
     * @return The raw class associated with this type
     */
    public static Class<?> getRawClass(Type type) {
        if (type == null) return null;

        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();

            if (!(componentType instanceof ParameterizedType)) {
                // type variable is not supported
                return null;
            }

            Class<?> rawComponentClass = getRawClass(componentType);

            String forNameName = "[L" + rawComponentClass.getName() + ";";
            try {
                return Class.forName(forNameName);
            }
            catch (Throwable th) {
                // ignore, but return null
                return null;
            }
        }

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
     * Resolves the generic type of a field given the actual class being instantiated
     * 
     * @param topclass The instantiation class.  Must not be null
     * @param field The field whose type to resolve
     * @return The resolved field type by way of its subclasses.  May return null if the
     * original type could not be converted into a Class or a ParameterizedType
     */
    public static Type resolveField(Class<?> topclass, Field field) {
        return resolveMember(topclass, field.getGenericType(), field.getDeclaringClass());
    }
    
    /**
     * Resolves the generic type of a type and declaring class given the actual class being instantiated
     * 
     * @param topclass The instantiation class.  Must not be null
     * @param lookingForType The type to resolve.  Must not be null
     * @param declaringClass The class of the entity declaring the lookingForType. Must not be null
     * @return The resolved type by way of its subclasses.  May return null if the
     * original type could not be converted into a Class or a ParameterizedType
     */
    public static Type resolveMember(Class<?> topclass, Type lookingForType, Class<?> declaringClass) {
        Map<String, Type> typeArguments = typesFromSubClassToDeclaringClass(topclass, declaringClass);
        if (typeArguments == null) return null;
        
        if (lookingForType instanceof ParameterizedType) {
            return fixTypeVariables((ParameterizedType) lookingForType, typeArguments);
        }
        
        if (!(lookingForType instanceof TypeVariable)) {
            return null;
        }
        
        TypeVariable<?> tv = (TypeVariable<?>) lookingForType;
        String typeVariableName = tv.getName();
        
        Type retVal = typeArguments.get(typeVariableName);
        if (retVal == null) return null;
        
        if (retVal instanceof Class) return retVal;
        
        if (retVal instanceof ParameterizedType) {
            return fixTypeVariables((ParameterizedType) retVal, typeArguments);
        }
        
        return null;
    }
    
    private static Map<String, Type> typesFromSubClassToDeclaringClass(Class<?> topClass, Class<?> declaringClass) {
        if (topClass.equals(declaringClass)) {
            return null;
        }
        
        Type superType = topClass.getGenericSuperclass();
        Class<?> superClass = getRawClass(superType);
        
        while (superType != null && superClass != null) {
            if (!(superType instanceof ParameterizedType)) {
                // superType MUST be a Class in this case
                if (superClass.equals(declaringClass)) return null;
                
                superType = superClass.getGenericSuperclass();
                superClass = ReflectionHelper.getRawClass(superType);
                
                continue;
            }
            
            ParameterizedType superPT = (ParameterizedType) superType;
            
            Map<String, Type> typeArguments = getTypeArguments(superClass, superPT);
            
            if (superClass.equals(declaringClass)) {
                return typeArguments;
            }
            
            superType = superClass.getGenericSuperclass();
            superClass = ReflectionHelper.getRawClass(superType);

            if (superType instanceof ParameterizedType) {
                superType = fixTypeVariables((ParameterizedType) superType, typeArguments);
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

    private static String getNamedName(Named named, Class<?> implClass) {
        String name = named.value();
        if (name != null && !name.equals("")) return name;

        String cn = implClass.getName();

        int index = cn.lastIndexOf(".");
        if (index < 0) return cn;

        return cn.substring(index + 1);
    }

    /**
     * Returns the name that should be associated with this class
     *
     * @param implClass The class to evaluate
     * @return The name this class should have
     */
    public static String getName(Class<?> implClass) {
        Named named = implClass.getAnnotation(Named.class);

        String namedName = (named != null) ? getNamedName(named, implClass) : null ;

        if (namedName != null) return namedName;

        return null;
    }

    /**
     * Gets all the interfaces on this particular class (but not any
     * superclasses of this class).
     */
    private static void addAllGenericInterfaces(Class<?> rawClass ,
                                                Type type,
                                                Set<Type> closures) {

        Map<String, Type> typeArgumentsMap = null;

        for (Type currentType : rawClass.getGenericInterfaces()) {

            if (type instanceof ParameterizedType &&
                    currentType instanceof ParameterizedType) {

                if (typeArgumentsMap == null ) {
                    typeArgumentsMap = getTypeArguments(rawClass, (ParameterizedType) type);
                }

                currentType = fixTypeVariables((ParameterizedType) currentType, typeArgumentsMap);
            }

            closures.add(currentType);

            rawClass = ReflectionHelper.getRawClass(currentType);
            if (rawClass != null) {
                addAllGenericInterfaces(rawClass, currentType, closures);
            }
        }
    }

    /**
     * Replace any TypeVariables in the given type's arguments with
     * the actual argument types.  Return the given type if no replacing
     * is required.
     */
    private static Type fixTypeVariables(ParameterizedType type,
                                         Map<String, Type> typeArgumentsMap) {

        Type[] newTypeArguments = getNewTypeArguments(type, typeArgumentsMap);

        if (newTypeArguments != null) {
            type = new ParameterizedTypeImpl(type.getRawType(), newTypeArguments);
        }
        return type;
    }

    /**
     * Get a new array of type arguments for the given ParameterizedType, replacing any TypeVariables with
     * actual types.  The types should be found in the given arguments map, keyed by variable name.  Return
     * null if no arguments needed to be replaced.
     */
    private static Type[] getNewTypeArguments(final ParameterizedType type,
                                              final Map<String, Type> typeArgumentsMap) {

        Type[]  typeArguments    = type.getActualTypeArguments();
        Type[]  newTypeArguments = new Type[typeArguments.length];
        boolean newArgsNeeded    = false;

        int i = 0;
        for (Type argType : typeArguments) {
            if (argType instanceof TypeVariable) {
                newTypeArguments[i++] = typeArgumentsMap.get(((TypeVariable<?>) argType).getName());
                newArgsNeeded = true;
            }
            else if (argType instanceof ParameterizedType) {
                ParameterizedType original = (ParameterizedType) argType;
                
                Type[] internalTypeArgs = getNewTypeArguments(original, typeArgumentsMap);
                if (internalTypeArgs != null) {
                    newTypeArguments[i++] = new ParameterizedTypeImpl(original.getRawType(), internalTypeArgs);
                    newArgsNeeded = true;
                }
                else {
                    newTypeArguments[i++] = argType;
                }
            }
            else {
                newTypeArguments[i++] = argType;
            }
        }
        return newArgsNeeded ? newTypeArguments : null;
    }

    /**
     * Gets a mapping of type variable names of the raw class to type arguments of the
     * parameterized type.
     */
    private static Map<String, Type> getTypeArguments(Class<?> rawClass,
                                                      ParameterizedType type) {

        Map<String, Type> typeMap       = new HashMap<String, Type>();
        Type[]            typeArguments = type.getActualTypeArguments();

        int i = 0;
        for (TypeVariable<?> typeVariable : rawClass.getTypeParameters() ) {
            typeMap.put(typeVariable.getName(), typeArguments[i++]);
        }
        return typeMap;
    }

    /**
     * Returns the type closure of the given class
     *
     * @param ofType The full type closure of the given class
     * with nothing omitted (normal case).  May not be null
     * @return The non-null (and never empty) set of classes
     * that this class can be assigned to
     */
    private static Set<Type> getTypeClosure(Type ofType) {
        Set<Type> retVal   = new HashSet<Type>();
        Class<?>  rawClass = ReflectionHelper.getRawClass(ofType);

        if (rawClass != null) {
            Map<String, Type> typeArgumentsMap = null;
            Type              currentType      = ofType;

            while (currentType != null && rawClass != null) {

                retVal.add(currentType);

                addAllGenericInterfaces(rawClass, currentType, retVal);

                if (typeArgumentsMap == null && currentType instanceof ParameterizedType){
                    typeArgumentsMap = getTypeArguments(rawClass, (ParameterizedType) currentType);
                }

                currentType = rawClass.getGenericSuperclass();
                if (currentType != null) {
                    rawClass = ReflectionHelper.getRawClass(currentType);

                    if (typeArgumentsMap != null && currentType instanceof ParameterizedType){
                        currentType = fixTypeVariables((ParameterizedType) currentType, typeArgumentsMap);
                    }
                }
            }
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
            Class<?> rawClass = ReflectionHelper.getRawClass(t);
            if (rawClass == null) continue;

            if (contracts.contains(rawClass.getName())) {
                retVal.add(t);
            }
        }

        return retVal;
    }

    /**
     * Returns the set of types this class advertises
     * @param type The outer type to analyze
     * @param markerAnnotation The annotation to use to discover the advertised types
     * @return The type itself and the contracts it implements
     */
    public static Set<Type> getAdvertisedTypesFromClass(Type type, Class<? extends Annotation> markerAnnotation) {
        Set<Type> retVal = new LinkedHashSet<Type>();
        if (type == null) return retVal;

        retVal.add(type);

        Class<?> originalRawClass = getRawClass(type);
        if (originalRawClass == null) return retVal;

        Type genericSuperclass = originalRawClass.getGenericSuperclass();
        while (genericSuperclass != null) {
            Class<?> rawClass = getRawClass(genericSuperclass);
            if (rawClass == null) break;

            if (rawClass.isAnnotationPresent(markerAnnotation)) {
                retVal.add(genericSuperclass);
            }

            genericSuperclass = rawClass.getGenericSuperclass();
        }

        Set<Class<?>> alreadyHandled = new HashSet<Class<?>>();
        while (originalRawClass != null) {
            getAllContractsFromInterfaces(originalRawClass,
                markerAnnotation,
                retVal,
                alreadyHandled);

            originalRawClass = originalRawClass.getSuperclass();
        }

        return retVal;
    }

    private static void getAllContractsFromInterfaces(Class<?> clazzOrInterface,
            Class<? extends Annotation> markerAnnotation,
            Set<Type> addToMe,
            Set<Class<?>> alreadyHandled) {
        Type interfacesAsType[] = clazzOrInterface.getGenericInterfaces();

        for (Type interfaceAsType : interfacesAsType) {
            Class<?> interfaceAsClass = getRawClass(interfaceAsType);
            if (interfaceAsClass == null) continue;
            if (alreadyHandled.contains(interfaceAsClass)) continue;
            alreadyHandled.add(interfaceAsClass);

            if (interfaceAsClass.isAnnotationPresent(markerAnnotation)) {
                addToMe.add(interfaceAsType);
            }

            getAllContractsFromInterfaces(interfaceAsClass, markerAnnotation, addToMe, alreadyHandled);
        }
    }

    /**
     * Returns the set of types this class advertises
     * @param t the object we are analyzing
     * @param markerAnnotation The annotation to use to discover the advertised types
     * @return The type itself and the contracts it implements
     */
    public static Set<Type> getAdvertisedTypesFromObject(Object t, Class<? extends Annotation> markerAnnotation) {
        if (t == null) return Collections.emptySet();

        return getAdvertisedTypesFromClass(t.getClass(), markerAnnotation);
    }

    /**
     * Returns the set of types this class advertises
     * @param clazz the class we are analyzing
     * @param markerAnnotation The annotation to use to discover annotated types
     * @return The type itself and the contracts it implements
     */
    public static Set<String> getContractsFromClass(Class<?> clazz, Class<? extends Annotation> markerAnnotation) {
        Set<String> retVal = new LinkedHashSet<String>();
        if (clazz == null) return retVal;

        retVal.add(clazz.getName());

        Class<?> extendsClasses = clazz.getSuperclass();
        while (extendsClasses != null) {
            if (extendsClasses.isAnnotationPresent(markerAnnotation)) {
                retVal.add(extendsClasses.getName());
            }

            extendsClasses = extendsClasses.getSuperclass();
        }

        while (clazz != null) {
            Class<?> interfaces[] = clazz.getInterfaces();
            for (Class<?> iFace : interfaces) {
                if (iFace.isAnnotationPresent(markerAnnotation)) {
                    retVal.add(iFace.getName());
                }
            }

            clazz = clazz.getSuperclass();
        }

        return retVal;
    }

    /**
     * Gets the scope annotation from the object
     * @param t The object to analyze
     * @return The class of the scope annotation
     */
    public static Annotation getScopeAnnotationFromObject(Object t) {
        if (t == null) throw new IllegalArgumentException();


        return getScopeAnnotationFromClass(t.getClass());
    }

    /**
     * Gets the scope annotation from the object
     * @param clazz The class to analyze
     * @return The class of the scope annotation
     */
    public static Annotation getScopeAnnotationFromClass(Class<?> clazz) {
        if (clazz == null) throw new IllegalArgumentException();

        for (Annotation annotation : clazz.getAnnotations()) {
            Class<? extends Annotation> annoClass = annotation.annotationType();

            if (annoClass.isAnnotationPresent(Scope.class)) {
                return annotation;
            }

        }

        return null;
    }

    /**
     * Gets the scope annotation from the object
     * @param t The object to analyze
     * @param annoDefault The default that this should have if no scope could be found
     * @return The class of the scope annotation
     */
    public static Annotation getScopeFromObject(Object t, Annotation annoDefault) {
        if (t == null) return annoDefault;

        return getScopeFromClass(t.getClass(), annoDefault);
    }

    /**
     * Gets the scope annotation from the object
     * @param clazz The class to analyze
     * @param annoDefault The scope that should be returned if no scope could be found
     * @return The class of the scope annotation
     */
    public static Annotation getScopeFromClass(Class<?> clazz, Annotation annoDefault) {
        if (clazz == null) return annoDefault;

        for (Annotation annotation : clazz.getAnnotations()) {
            Class<? extends Annotation> annoClass = annotation.annotationType();

            if (annoClass.isAnnotationPresent(Scope.class)) {
                return annotation;
            }

        }

        return annoDefault;
    }

    /**
     * Returns true if the given annotation is a qualifier
     * @param anno The annotation to check
     * @return true if this is an annotation
     */
    public static boolean isAnnotationAQualifier(Annotation anno) {
        Class<? extends Annotation> annoType = anno.annotationType();
        return annoType.isAnnotationPresent(Qualifier.class);
    }

    /**
     * Gets all the qualifiers from the object
     *
     * @param t The object to analyze
     * @return The set of qualifiers.  Will not return null but may return an empty set
     */
    public static Set<Annotation> getQualifiersFromObject(Object t) {
        if (t == null) return Collections.emptySet();

        return getQualifierAnnotations(t.getClass());
    }

    /**
     * Gets all the qualifiers from the object
     *
     * @param clazz The class to analyze
     * @return The set of qualifiers.  Will not return null but may return an empty set
     */
    public static Set<String> getQualifiersFromClass(Class<?> clazz) {
        Set<String> retVal = new LinkedHashSet<String>();
        if (clazz == null) return retVal;

        for (Annotation annotation : clazz.getAnnotations()) {
            if (isAnnotationAQualifier(annotation)) {
                retVal.add(annotation.annotationType().getName());
            }

        }

        while (clazz != null) {
            for (Class<?> iFace : clazz.getInterfaces()) {
                for (Annotation annotation : iFace.getAnnotations()) {
                    if (isAnnotationAQualifier(annotation)) {
                        retVal.add(annotation.annotationType().getName());
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }

        return retVal;
    }

    private static Set<Annotation> internalGetQualifierAnnotations(AnnotatedElement annotatedGuy) {
        Set<Annotation> retVal = new LinkedHashSet<Annotation>();
        if (annotatedGuy == null) return retVal;

        for (Annotation annotation : annotatedGuy.getAnnotations()) {
            if (isAnnotationAQualifier(annotation)) {
                if ((annotatedGuy instanceof Field) &&
                        Named.class.equals(annotation.annotationType())) {
                    Named n = (Named) annotation;
                    if (n.value() == null || "".equals(n.value())) {
                        // Because we do not have access to AnnotationLiteral
                        // we cannot "fix" a Named annotation that has no explicit
                        // value here, and we must rely on the caller of this
                        // method to "fix" that case for us
                        continue;
                    }
                }
                retVal.add(annotation);
            }
        }

        if (!(annotatedGuy instanceof Class)) return retVal;

        Class<?> clazz = (Class<?>) annotatedGuy;
        while (clazz != null) {
            for (Class<?> iFace : clazz.getInterfaces()) {
                for (Annotation annotation : iFace.getAnnotations()) {
                    if (isAnnotationAQualifier(annotation)) {
                        retVal.add(annotation);
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }

        return retVal;

    }

    /**
     * Gets all the qualifier annotations from the object
     * <p>
     * A strange behavior of this method is that if the annotatedGuy is
     * a field and that field has the Named annotation on it with no
     * value, then that Named annotation will NOT be added to the return
     * list.  This is because we have no access at this level to
     * AnnotationLiteral, and hence cannot create a NamedImpl with which
     * to fix the annotation.  It is the responsibility of the caller
     * of this method to add in the NamedImpl in that circumstance
     *
     * @param annotatedGuy The thing to analyze
     * @return The set of qualifiers.  Will not return null but may return an empty set
     */
    public static Set<Annotation> getQualifierAnnotations(final AnnotatedElement annotatedGuy) {
        Set<Annotation> retVal = AccessController.doPrivileged(new PrivilegedAction<Set<Annotation>>() {

            @Override
            public Set<Annotation> run() {
                return internalGetQualifierAnnotations(annotatedGuy);
            }

        });

        return retVal;
    }

    /**
     * Writes a set in a way that can be read from an input stream as well
     *
     * @param set The set to write
     * @return a representation of a list
     */
    public static String writeSet(Set<?> set) {
        return writeSet(set, null);
    }

    /**
     * Writes a set in a way that can be read from an input stream as well
     *
     * @param set The set to write
     * @param excludeMe An object to exclude from the list of things written
     * @return a representation of a list
     */
    public static String writeSet(Set<?> set, Object excludeMe) {
        if (set == null) return "{}";

        StringBuffer sb = new StringBuffer("{");

        boolean first = true;
        for (Object writeMe : set) {
            if (excludeMe != null && excludeMe.equals(writeMe)) {
                // Excluded
                continue;
            }
            if (first) {
                first = false;
                sb.append(escapeString(writeMe.toString()));
            }
            else {
                sb.append("," + escapeString(writeMe.toString()));
            }
        }

        sb.append("}");

        return sb.toString();
    }

    /**
     * Writes a set in a way that can be read from an input stream as well.  The values in
     * the set may not contain the characters "{},"
     *
     * @param line The line to read
     * @param addToMe The set to add the strings to
     * @throws IOException On a failure
     */
    public static void readSet(String line, Collection<String> addToMe) throws IOException {
        char asChars[] = new char[line.length()];
        line.getChars(0, line.length(), asChars, 0);

        internalReadSet(asChars, 0, addToMe);
    }

    /**
     * Writes a set in a way that can be read from an input stream as well.  The values in
     * the set may not contain the characters "{},"
     *
     * @param asChars The line to read
     * @param addToMe The set to add the strings to
     * @return The number of characters read until the end of the set
     * @throws IOException On a failure
     */
    private static int internalReadSet(char asChars[], int startIndex, Collection<String> addToMe) throws IOException {
        int dot = startIndex;
        int startOfSet = -1;
        while (dot < asChars.length) {
            if (asChars[dot] == '{') {
                startOfSet = dot;
                dot++;
                break;
            }
            dot++;
        }

        if (startOfSet == -1) {
            throw new IOException("Unknown set format, no initial { character : " + new String(asChars));
        }

        StringBuffer elementBuffer = new StringBuffer();
        int endOfSet = -1;
        while (dot < asChars.length) {
            char dotChar = asChars[dot];

            if (dotChar == '}') {
                addToMe.add(elementBuffer.toString());

                endOfSet = dot;
                break;  // Done!
            }

            if (dotChar == ',') {
                // Terminating a single element
                addToMe.add(elementBuffer.toString());

                elementBuffer = new StringBuffer();
            }
            else {
                // This character is either an escape character or a real character
                if (dotChar != '\\') {
                    elementBuffer.append(dotChar);
                }
                else {
                    // This is an escape character
                    if (dot + 1 >= asChars.length) {
                        // This is an error, escape at end of buffer
                        break;
                    }

                    dot++;  // Moves it forward
                    dotChar = asChars[dot];

                    if (dotChar == 'n') {
                        elementBuffer.append('\n');
                    }
                    else if (dotChar == 'r') {
                        elementBuffer.append('\r');
                    }
                    else {
                        elementBuffer.append(dotChar);
                    }
                }

            }

            dot++;
        }

        if (endOfSet == -1) {
            throw new IOException("Unknown set format, no ending } character : " + new String(asChars));
        }

        return dot - startIndex;
    }

    private static int readKeyStringListLine(char asChars[], int startIndex, Map<String, List<String>> addToMe) throws IOException {
        int dot = startIndex;

        int equalsIndex = -1;
        while (dot < asChars.length) {
            char dotChar = asChars[dot];

            if (dotChar == '=') {
                equalsIndex = dot;
                break;
            }

            dot++;
        }

        if (equalsIndex < 0) {
            throw new IOException("Unknown key-string list format, no equals: " + new String(asChars));
        }

        String key = new String(asChars, startIndex, (equalsIndex - startIndex));  // Does not include the =
        dot++;  // Move it past the equals

        if (dot >= asChars.length) {
            // Key with no values, this is illegal
            throw new IOException("Found a key with no value, " + key + " in line " + new String(asChars));

        }

        LinkedList<String> listValues = new LinkedList<String>();

        int addOn = internalReadSet(asChars, dot, listValues);
        if (!listValues.isEmpty()) {
            addToMe.put(key, listValues);
        }

        dot += addOn + 1;
        if (dot < asChars.length) {
            char skipComma = asChars[dot];
            if (skipComma == ',') {
                dot++;
            }
        }

        return dot - startIndex;  // The +1 gets us to the next character in the stream
    }

    /**
     * Writes a set in a way that can be read from an input stream as well
     * @param line The line to read
     * @param addToMe The set to add the strings to
     * @throws IOException On a failure
     */
    public static void readMetadataMap(String line, Map<String, List<String>> addToMe) throws IOException {
        char asChars[] = new char[line.length()];
        line.getChars(0, line.length(), asChars, 0);

        int dot = 0;
        while (dot < asChars.length) {
            int addMe = readKeyStringListLine(asChars, dot, addToMe);
            dot += addMe;
        }
    }

    private static String escapeString(String escapeMe) {
        char asChars[] = new char[escapeMe.length()];

        escapeMe.getChars(0, escapeMe.length(), asChars, 0);

        StringBuffer sb = new StringBuffer();
        for (int lcv = 0; lcv < asChars.length; lcv++) {
            char candidateChar = asChars[lcv];

            if (ESCAPE_CHARACTERS.contains(candidateChar)) {
                sb.append('\\');
                sb.append(candidateChar);
            }
            else if (REPLACE_CHARACTERS.containsKey(candidateChar)) {
                char replaceWithMe = REPLACE_CHARACTERS.get(candidateChar);
                sb.append('\\');
                sb.append(replaceWithMe);
            }
            else {
                sb.append(candidateChar);
            }
        }

        return sb.toString();
    }

    private static String writeList(List<String> list) {
        StringBuffer sb = new StringBuffer("{");

        boolean first = true;
        for (String writeMe : list) {
            if (first) {
                first = false;
                sb.append(escapeString(writeMe));
            }
            else {
                sb.append("," + escapeString(writeMe));
            }
        }

        sb.append("}");

        return sb.toString();
    }

    /**
     * Used to write the metadata out
     *
     * @param metadata The metadata to externalize
     * @return The metadata in an externalizable format
     */
    public static String writeMetadata(Map<String, List<String>> metadata) {
        StringBuffer sb = new StringBuffer();

        boolean first = true;
        for (Map.Entry<String, List<String>> entry : metadata.entrySet()) {
            if (first) {
                first = false;
                sb.append(entry.getKey() + '=');
            }
            else {
                sb.append("," + entry.getKey() + '=');
            }

            sb.append(writeList(entry.getValue()));
        }

        return sb.toString();
    }

    /**
     * Adds a value to the list of values associated with this key
     *
     * @param metadatas The base metadata object
     * @param key The key to which to add the value.  May not be null
     * @param value The value to add.  May not be null
     */
    public static void addMetadata(Map<String, List<String>> metadatas, String key, String value) {
        if (key == null || value == null) return;
        if (key.indexOf('=') >= 0) {
            throw new IllegalArgumentException("The key field may not have an = in it:" + key);
        }

        List<String> inner = metadatas.get(key);
        if (inner == null) {
            inner = new LinkedList<String>();
            metadatas.put(key, inner);
        }

        inner.add(value);
    }

    /**
     * Removes the given value from the given key
     *
     * @param metadatas The base metadata object
     * @param key The key of the value to remove.  May not be null
     * @param value The value to remove.  May not be null
     * @return true if the value was removed
     */
    public static boolean removeMetadata(Map<String, List<String>> metadatas, String key, String value) {
        if (key == null || value == null) return false;

        List<String> inner = metadatas.get(key);
        if (inner == null) return false;

        boolean retVal = inner.remove(value);
        if (inner.size() <= 0) metadatas.remove(key);

        return retVal;
    }

    /**
     * Removes all the metadata values associated with key
     *
     * @param metadatas The base metadata object
     * @param key The key of the metadata values to remove
     * @return true if any value was removed
     */
    public static boolean removeAllMetadata(Map<String, List<String>> metadatas, String key) {
        List<String> values = metadatas.remove(key);
        return (values != null && values.size() > 0);
    }

    /**
     * This method does a deep copy of the incoming meta-data, (which basically means we will
     * also make copies of the value list)
     *
     * @param copyMe The guy to copy (if null, null will be returned)
     * @return A deep copy of the metadata
     */
    public static Map<String, List<String>> deepCopyMetadata(Map<String, List<String>> copyMe) {
        if (copyMe == null) return null;

        Map<String, List<String>> retVal = new LinkedHashMap<String, List<String>>();

        for (Map.Entry<String, List<String>> entry : copyMe.entrySet()) {
            String key = entry.getKey();
            if (key.indexOf('=') >= 0) {
                throw new IllegalArgumentException("The key field may not have an = in it:" + key);
            }

            List<String> values = entry.getValue();
            LinkedList<String> valuesCopy = new LinkedList<String>();
            for (String value : values) {
                valuesCopy.add(value);
            }

            retVal.put(key, valuesCopy);
        }

        return retVal;
    }

    /**
     * Sets the given field to the given value
     *
     * @param field The non-null field to set
     * @param instance The non-null instance to set into
     * @param value The value to which the field should be set
     * @throws Throwable If there was some exception while setting the field
     */
    public static void setField(Field field, Object instance, Object value) throws Throwable {
        setAccessible(field);

        try {
            field.set(instance, value);
        }
        catch (IllegalArgumentException e) {
            Logger.getLogger().debug(field.getDeclaringClass().getName(), field.getName(), e);
            throw e;
        }
        catch (IllegalAccessException e) {
            Logger.getLogger().debug(field.getDeclaringClass().getName(), field.getName(), e);
            throw e;
        }
    }

    /**
     * This version of invoke is CCL neutral (it will return with the
     * same CCL as what it went in with)
     *
     * @param m the method to invoke
     * @param o the object on which to invoke it
     * @param args The arguments to invoke (may not be null)
     * @param neutralCCL true if the ContextClassLoader shoult remain null with this call
     * @return The return from the invocation
     * @throws Throwable The unwrapped throwable thrown by the method
     */
    public static Object invoke(Object o, Method m, Object args[], boolean neutralCCL)
            throws Throwable {
        if (isStatic(m)) {
            o = null;
        }
        
        setAccessible(m);
        
        ClassLoader currentCCL = null;
        if (neutralCCL) {
            currentCCL = getCurrentContextClassLoader();
        }

        try {
            return m.invoke(o, args);
        }
        catch (InvocationTargetException ite) {
            Throwable targetException = ite.getTargetException();
            Logger.getLogger().debug(m.getDeclaringClass().getName(), m.getName(), targetException);
            throw targetException;
        }
        catch (Throwable th) {
            Logger.getLogger().debug(m.getDeclaringClass().getName(), m.getName(), th);
            throw th;
        }
        finally {
            if (neutralCCL) {
                setContextClassLoader(Thread.currentThread(), currentCCL);
            }
        }
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
     * Sets the context classloader under the privileged of this class
     * @param t The thread on which to set the classloader
     * @param l The classloader to set
     */
    private static void setContextClassLoader(final Thread t, final ClassLoader l) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                t.setContextClassLoader(l);
                return null;
            }

        });

    }

    /**
     * Sets this accessible object to be accessible using the permissions of
     * the hk2-locator bundle (which will need the required grant)
     *
     * @param ao The object to change
     */
    private static void setAccessible(final AccessibleObject ao) {
        if (ao.isAccessible()) return;
        
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                ao.setAccessible(true);
                return null;
            }

        });
    }

    /**
     * This version of invoke is CCL neutral (it will return with the
     * same CCL as what it went in with)
     *
     * @param c the constructor to call
     * @param args The arguments to invoke (may not be null)
     * @param neutralCCL true if the context class loader should remain null through this call
     * @return The return from the invocation
     * @throws Throwable The unwrapped throwable thrown by the method
     */
    public static Object makeMe(Constructor<?> c, Object args[], boolean neutralCCL)
            throws Throwable {

        
        ClassLoader currentCCL = null;
        if (neutralCCL) {
            currentCCL = getCurrentContextClassLoader();
        }

        try {
            return c.newInstance(args);
        } catch (InvocationTargetException ite) {
            Throwable targetException = ite.getTargetException();
            Logger.getLogger().debug(c.getDeclaringClass().getName(), c.getName(), targetException);
            throw targetException;
        } catch (Throwable th) {
            Logger.getLogger().debug(c.getDeclaringClass().getName(), c.getName(), th);
            throw th;
        } finally {
            if (neutralCCL) {
                setContextClassLoader(Thread.currentThread(), currentCCL);
            }
        }
    }

    /**
     * This method parses the string that is found in the &#86;Service metadata field.
     *
     * @param metadataField A non-null metadata field that normally comes from the Service
     * metadata field
     * @param metadata The metadata field to add to
     * @throws IllegalStateException if a string with an invalid format is found
     */
    public static void parseServiceMetadataString(String metadataField, Map<String, List<String>> metadata) {
        StringBuffer sb = new StringBuffer(metadataField);

        int dot = 0;
        int nextEquals = sb.indexOf(EQUALS_STRING, dot);
        while (nextEquals > 0) {
            String key = sb.substring(dot, nextEquals);

            dot = nextEquals + 1;

            int commaPlace;
            String value = null;
            if (sb.charAt(dot) == '\"') {
                dot++;  // Get past the quote

                int nextQuote = sb.indexOf(QUOTE_STRING, dot);
                if (nextQuote < 0) {
                    // What to do?
                    throw new IllegalStateException("Badly formed metadata \"" + metadataField + "\" for key " + key +
                            " has a leading quote but no trailing quote");
                }

                value = sb.substring(dot, nextQuote);
                dot = nextQuote + 1;

                commaPlace = sb.indexOf(COMMA_STRING, dot);  // Should be right at dot
            }
            else {
                commaPlace = sb.indexOf(COMMA_STRING, dot);


                if (commaPlace < 0) {
                    value = sb.substring(dot);
                }
                else {
                    value = sb.substring(dot, commaPlace);
                }
            }

            List<String> addToMe = metadata.get(key);
            if (addToMe == null) {
                addToMe = new LinkedList<String>();
                metadata.put(key, addToMe);
            }
            addToMe.add(value);

            if (commaPlace >= 0) {
                dot = commaPlace + 1;
                nextEquals = sb.indexOf(EQUALS_STRING, dot);
            }
            else {
                nextEquals = -1;
            }
        }
    }

    /**
     * Gets the name from the &46;Named qualifier in this set of qualifiers
     *
     * @param qualifiers The set of qualifiers that may or may not have Named in it
     * @param parent The parent element for which we are searching
     * @return null if no Named was found, or the appropriate name otherwise
     * @throws IllegalStateException If the parent is annotated with a blank Named but is not
     * a Class or a Field
     */
    public static String getNameFromAllQualifiers(Set<Annotation> qualifiers, AnnotatedElement parent) throws IllegalStateException {
        for (Annotation qualifier : qualifiers) {
            if (!Named.class.equals(qualifier.annotationType())) continue;

            Named named = (Named) qualifier;
            if ((named.value() == null) || named.value().equals("")) {
                if (parent != null) {
                    if (parent instanceof Class) {
                        return Pretty.clazz((Class<?>) parent);
                    }

                    if (parent instanceof Field) {
                        return ((Field) parent).getName();
                    }
                }

                throw new IllegalStateException("@Named must have a value for " + parent);
            }

            return named.value();
        }

        return null;
    }

    /**
     * Gets the current context class loader with privs
     * 
     * @return The current context class loader
     */
    private static ClassLoader getCurrentContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }
    
    /**
     * This is used to check on the annotation set.  It must be done under protection because the annotations may
     * attempt to discover if they are equal using getDeclaredMembers permission
     *
     * @param candidateAnnotations The candidate annotations
     * @param requiredAnnotations The required annotations
     * @return true if the candidate set contains the entire required set
     */
    public static boolean annotationContainsAll(final Set<Annotation> candidateAnnotations, final Set<Annotation> requiredAnnotations) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return candidateAnnotations.containsAll(requiredAnnotations);
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
     * Returns true if the underlying member is private
     *
     * @param member The non-null member to test
     * @return true if the member is private
     */
    public static boolean isPrivate(Member member) {
        int modifiers = member.getModifiers();

        return ((modifiers & Modifier.PRIVATE) != 0);
    }
    
    public static Set<Type> getAllTypes(Type t) {
        LinkedHashSet<Type> retVal = new LinkedHashSet<Type>();
        retVal.add(t);

        Class<?> rawClass = ReflectionHelper.getRawClass(t);
        if (rawClass == null) return retVal;

        Type genericSuperclass = rawClass.getGenericSuperclass();
        while (genericSuperclass != null) {
            Class<?> rawSuperclass = ReflectionHelper.getRawClass(genericSuperclass);
            if (rawSuperclass == null) break;

            retVal.add(genericSuperclass);

            genericSuperclass = rawSuperclass.getGenericSuperclass();
        }

        while (rawClass != null) {
            for (Type iface : rawClass.getGenericInterfaces()) {
                addAllInterfaceContracts(iface, retVal);
            }

            rawClass = rawClass.getSuperclass();
        }
        
        LinkedHashSet<Type> altRetVal = new LinkedHashSet<Type>();
        HashMap<Class<?>, ParameterizedType> class2TypeMap = new HashMap<Class<?>, ParameterizedType>();
        
        for (Type foundType : retVal) {
            if (!(foundType instanceof ParameterizedType)) {
                altRetVal.add(foundType);
                continue;
            }
            
            ParameterizedType originalPt = (ParameterizedType) foundType;
            Class<?> rawType = getRawClass(foundType);
            
            class2TypeMap.put(rawType, originalPt);
            
            if (isFilledIn(originalPt)) {
                altRetVal.add(foundType);
                continue;
            }
            
            ParameterizedType pti = fillInPT(originalPt, class2TypeMap);
            altRetVal.add(pti);
            class2TypeMap.put(rawType, pti);
        }

        return altRetVal;
    }
    
    @SuppressWarnings("unchecked")
    private static ParameterizedType fillInPT(ParameterizedType pt,
            HashMap<Class<?>, ParameterizedType> class2TypeMap) {
        if (isFilledIn(pt)) return pt;
        
        // At this point, this guy may need to get filled in
        Type newActualArguments[] = new Type[pt.getActualTypeArguments().length];
        for (int outerIndex = 0 ; outerIndex < newActualArguments.length; outerIndex++) {
            Type fillMeIn = pt.getActualTypeArguments()[outerIndex];
            
            // All else failing ensure it is filled in with the original value
            newActualArguments[outerIndex] = fillMeIn;
            
            if (fillMeIn instanceof ParameterizedType) {
                newActualArguments[outerIndex] = fillInPT((ParameterizedType) fillMeIn, class2TypeMap);
                continue;
            }
            
            if (!(fillMeIn instanceof TypeVariable)) {
                continue;
            }
            
            TypeVariable<Class<?>> tv = (TypeVariable<Class<?>>) fillMeIn;
            Class<?> genericDeclaration = tv.getGenericDeclaration();
            
            boolean found = false;
            int count = -1;
            for (Type parentVariable : genericDeclaration.getTypeParameters()) {
                count++;
                if (parentVariable.equals(tv)) {
                    found = true;
                    break;
                }
            }
            if (found == false) continue;
            
            ParameterizedType parentPType = class2TypeMap.get(genericDeclaration);
            if (parentPType == null) continue;
            
            newActualArguments[outerIndex] = parentPType.getActualTypeArguments()[count];
        }
        
        ParameterizedTypeImpl pti = new ParameterizedTypeImpl(getRawClass(pt), newActualArguments);
        return pti;
    }
    
    private static boolean isFilledIn(ParameterizedType pt, HashSet<ParameterizedType> recursionKiller) {
        if (recursionKiller.contains(pt)) return false;
        recursionKiller.add(pt);
        
        for (Type t : pt.getActualTypeArguments()) {
            if (t instanceof TypeVariable) return false;
            if (t instanceof WildcardType) return false;
            if (t instanceof ParameterizedType) {
                return (isFilledIn((ParameterizedType) t, recursionKiller));
            }
        }
        
        return true;
    }
    
    private static boolean isFilledIn(ParameterizedType pt) {
        return isFilledIn(pt, new HashSet<ParameterizedType>());
    }
    
    private static void addAllInterfaceContracts(Type interfaceType, LinkedHashSet<Type> addToMe) {
        Class<?> interfaceClass = ReflectionHelper.getRawClass(interfaceType);
        if (interfaceClass == null) return;
        if (addToMe.contains(interfaceType)) return;
        
        addToMe.add(interfaceType);
        
        for (Type extendedInterfaces : interfaceClass.getGenericInterfaces()) {
            addAllInterfaceContracts(extendedInterfaces, addToMe);
        }
    }
    
    /**
     * Creates a method wrapper for the given method
     * 
     * @param wrapMe The non-null method to wrap
     * @return A method wrapper that has a proper equals/hashCode
     */
    public static MethodWrapper createMethodWrapper(Method wrapMe) {
        return new MethodWrapperImpl(wrapMe);
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
}
