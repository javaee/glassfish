/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package oracle.toplink.essentials.internal.ejb.cmp3.metadata;

import java.lang.annotation.Annotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.security.AccessController;
import java.security.PrivilegedActionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.persistence.*;

import oracle.toplink.essentials.descriptors.ClassDescriptor;

import oracle.toplink.essentials.exceptions.ValidationException;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.MetadataAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.listeners.MetadataEntityListener;

import oracle.toplink.essentials.internal.helper.Helper;

import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedClassForName;
import oracle.toplink.essentials.internal.security.PrivilegedGetDeclaredFields;
import oracle.toplink.essentials.internal.security.PrivilegedGetDeclaredMethods;
import oracle.toplink.essentials.internal.security.PrivilegedGetField;
import oracle.toplink.essentials.internal.security.PrivilegedGetMethod;
import oracle.toplink.essentials.internal.security.PrivilegedGetMethods;
import oracle.toplink.essentials.internal.security.PrivilegedNewInstanceFromClass;

import oracle.toplink.essentials.sessions.Project;

/**
 * Common helper methods for the metadata processing.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataHelper {
    public static final String IS_PROPERTY_METHOD_PREFIX = "is";
    public static final String GET_PROPERTY_METHOD_PREFIX = "get";
    public static final String SET_PROPERTY_METHOD_PREFIX = "set";
    public static final String SET_IS_PROPERTY_METHOD_PREFIX = "setIs";
    private static final int POSITION_AFTER_IS_PREFIX = IS_PROPERTY_METHOD_PREFIX.length();
    private static final int POSITION_AFTER_GET_PREFIX = GET_PROPERTY_METHOD_PREFIX.length();
    
    public static final String PERSISTENCE_PACKAGE_PREFIX = "javax.persistence";

    /**
     * Set of attribute level O/R annotations.
     */
    private static final Set<Class> fieldOrPropertyORAnnotations =
            new HashSet<Class>(Arrays.asList(
                AssociationOverride.class,
                AssociationOverrides.class,
                AttributeOverride.class,
                AttributeOverrides.class,
                Basic.class,
                Column.class,
                Embedded.class,
                EmbeddedId.class,
                Enumerated.class,
                GeneratedValue.class,
                Id.class,
                JoinColumn.class,
                JoinColumns.class,
                JoinTable.class,
                Lob.class,
                ManyToMany.class,
                ManyToOne.class,
                MapKey.class,
                OneToMany.class,
                OneToOne.class,
                OrderBy.class,
                PrimaryKeyJoinColumn.class,
                PrimaryKeyJoinColumns.class,
                SequenceGenerator.class,
                TableGenerator.class,
                Temporal.class,
                Transient.class,
                Version.class));

    /**
     * INTERNAL:
     * Search the given sessions list of ordered descriptors for a descriptor 
     * for the class named the same as the given class.
     * 
     * We do not use the session based getDescriptor() methods because they 
     * require the project be initialized with classes.  We are avoiding using 
     * a project with loaded classes so the project can be constructed prior to 
     * any class weaving.
     */
    public static ClassDescriptor findDescriptor(Project project, Class cls) {
        for (ClassDescriptor descriptor : (Vector<ClassDescriptor>) project.getOrderedDescriptors()) {
            if (descriptor.getJavaClassName().equals(cls.getName())){
                return descriptor;
            }
        }
        
        return null;
    }
    
    /**
     * INTERNAL:
     * Method to read an annotation. I think there is a bug in the JDK when
     * reading annotations from classes. It returns the wrong type. Anyhow,
     * this method fixes that.
     */
    private static <T extends Annotation> T getAnnotation(Class annotation, AnnotatedElement annotatedElement, ClassLoader loader) {
        return (T) annotatedElement.getAnnotation(getClassForName(annotation.getName(), loader));
    }
    
    /**
     * INTERNAL:
     * Wrapper to the getAnnotation() call to check if we should ignore
     * annotations.
     */
    public static <T extends Annotation> T getAnnotation(Class annotation, AnnotatedElement annotatedElement, MetadataDescriptor descriptor) {
        Annotation loadedAnnotation = getAnnotation(annotation, annotatedElement, descriptor.getJavaClass().getClassLoader());
        
        if (loadedAnnotation != null && descriptor.ignoreAnnotations()) {
            descriptor.getLogger().logWarningMessage(MetadataLogger.IGNORE_ANNOTATION, annotation, annotatedElement);
            return null;
        } else {
            return (T) loadedAnnotation;
        }
    }
    
    /**
     * INTERNAL:
     * Wrapper to the getAnnotation() call using an Accessor.
     */
    public static <T extends Annotation> T getAnnotation(Class annotation, MetadataAccessor accessor) {
        return (T) getAnnotation(annotation, accessor.getAnnotatedElement(), accessor.getDescriptor());
    }
    
    /**
     * INTERNAL:
     * Wrapper to the getAnnotation() call using an MetadataDescriptor.
     */
    public static <T extends Annotation> T getAnnotation(Class annotation, MetadataDescriptor descriptor) {
        return (T) getAnnotation(annotation, descriptor.getJavaClass(), descriptor);
    }
    
    /**
     * INTERNAL:
     * Method to convert a getXyz or isXyz method name to an xyz attribute name.
     * NOTE: The method name passed it may not actually be a method name, so
     * by default return the name passed in.
     */
    public static String getAttributeNameFromMethodName(String methodName) {
        String leadingChar = "";
        String restOfName = methodName;
        
        if (methodName.startsWith(GET_PROPERTY_METHOD_PREFIX)) {
            leadingChar = methodName.substring(POSITION_AFTER_GET_PREFIX, POSITION_AFTER_GET_PREFIX + 1);
            restOfName = methodName.substring(POSITION_AFTER_GET_PREFIX + 1);
        } else if (methodName.startsWith(IS_PROPERTY_METHOD_PREFIX)){
            leadingChar = methodName.substring(POSITION_AFTER_IS_PREFIX, POSITION_AFTER_IS_PREFIX + 1);
            restOfName = methodName.substring(POSITION_AFTER_IS_PREFIX + 1);
        }
        
        return leadingChar.toLowerCase().concat(restOfName);
    }
    
    /**
     * INTERNAL:
     * Returns the same candidate methods as an entity listener would.
     */
    public static Method[] getCandidateCallbackMethodsForDefaultListener(MetadataEntityListener listener) {
        return getCandidateCallbackMethodsForEntityListener(listener);
    }
    
    /**
     * INTERNAL:
     * Return only the actual methods declared on this entity class.
     */
    public static Method[] getCandidateCallbackMethodsForEntityClass(Class entityClass) {
        return getDeclaredMethods(entityClass);
    }
    
    /**
     * INTERNAL:
     * Returns a list of methods from the given class, which can have private, 
     * protected, package and public access, AND will also return public 
     * methods from superclasses.
     */
    public static Method[] getCandidateCallbackMethodsForEntityListener(MetadataEntityListener listener) {
        HashSet candidateMethods = new HashSet();
        Class listenerClass = listener.getListenerClass();
        
        // Add all the declared methods ...
        Method[] declaredMethods = getDeclaredMethods(listenerClass);
        for (int i = 0; i < declaredMethods.length; i++) {
            candidateMethods.add(declaredMethods[i]);
        }
        
        // Now add any public methods from superclasses ...
        Method[] methods = getMethods(listenerClass);
        for (int i = 0; i < methods.length; i++) {
            if (candidateMethods.contains(methods[i])) {
                continue;
            }
            
            candidateMethods.add(methods[i]);
        }
        
        return (Method[]) candidateMethods.toArray(new Method[candidateMethods.size()]);
    }
    
    /**
     * INTERNAL:
     * Return potential lifecyle callback event methods for a mapped superclass. 
     * We must 'convert' the method to the entity class context before adding it 
     * to the listener.
     */
    public static Method[] getCandidateCallbackMethodsForMappedSuperclass(Class mappedSuperclass, Class entityClass) {
        ArrayList candidateMethods = new ArrayList();
        Method[] allMethods = getMethods(entityClass);
        Method[] declaredMethods = getDeclaredMethods(mappedSuperclass);
        
        for (int i = 0; i < declaredMethods.length; i++) {
            Method method = getMethodForName(allMethods, declaredMethods[i].getName());
            
            if (method != null) {
                candidateMethods.add(method);
            }
        }
        
        return (Method[]) candidateMethods.toArray(new Method[candidateMethods.size()]);
    }
    
    /**
     * INTERNAL:
     * Load a class from a given class name.
     */
    public static Class getClassForName(String classname, ClassLoader loader) {
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return (Class) AccessController.doPrivileged(new PrivilegedClassForName(classname, true, loader));
                } catch (PrivilegedActionException exception) {
                    throw ValidationException.unableToLoadClass(classname, exception.getException());
                }
            } else {
                return PrivilegedAccessHelper.getClassForName(classname, true, loader);
            }
        } catch (ClassNotFoundException exception) {
            throw ValidationException.unableToLoadClass(classname, exception);
        }
    }
    
    /**
     * INTERNAL:
     * Create a new instance of the class given.
     */
    public static Object getClassInstance(Class cls) {
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(cls));
                } catch (PrivilegedActionException exception) {
                    throw ValidationException.errorInstantiatingClass(cls, exception.getException());
                }
            } else {
                return PrivilegedAccessHelper.newInstanceFromClass(cls);
            }
        } catch (IllegalAccessException exception) {
            throw ValidationException.errorInstantiatingClass(cls, exception);
        } catch (InstantiationException exception) {
            throw ValidationException.errorInstantiatingClass(cls, exception);
        }
    }
    
    /**
     * INTERNAL:
     * Create a new instance of the class name.
     */
    public static Object getClassInstance(String className, ClassLoader loader) {
        return getClassInstance(getClassForName(className, loader));
    }
    
    /**
     * INTERNAL:
     */
    public static int getDeclaredAnnotationsCount(AnnotatedElement annotatedElement, MetadataDescriptor descriptor) {
        if (descriptor.ignoreAnnotations()) {
            return 0;
        } else {
            // Look for javax.persistence annotations only.
            int count = 0;
            
            for (Annotation annotation : annotatedElement.getDeclaredAnnotations()) {
                if (annotation.annotationType().getName().startsWith(PERSISTENCE_PACKAGE_PREFIX)) {
                    count++;
                }
            }
            
            return count;
        }
    }
    
    /**
     * INTERNAL:
	 * Get the declared methods from a class using the doPriveleged security
     * access. This call returns all methods (private, protected, package and
     * public) on the give class ONLY. It does not traverse the superclasses.
     */
	public static Method[] getDeclaredMethods(Class cls) {
        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
            try {
                return(Method[])AccessController.doPrivileged(new PrivilegedGetDeclaredMethods(cls));
            } catch (PrivilegedActionException exception) {
                // we will not get here, there are no checked exceptions in this call
                return null;
            }
        } else {
            return oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getDeclaredMethods(cls);
        }
	}
    
    /**
     * INTERNAL:
     * Return the discriminator type class for the given discriminator type.
     */
    public static Class getDiscriminatorType(String discriminatorType) {
        if (discriminatorType.equals(MetadataConstants.CHAR)) {
            return Character.class;
        } else if (discriminatorType.equals(MetadataConstants.STRING)) {
            return String.class;
        } else if (discriminatorType.equals(MetadataConstants.INTEGER)) {
            return Integer.class;
        } else {
            // Should never hit because of validation.
            return null;
        }
    }
    
    /**
     * INTERNAL:
     * Return the field classification for the given temporal type.
     */
    public static Class getFieldClassification(String temporalType) {
        if (temporalType.equals(MetadataConstants.DATE)) {
            return java.sql.Date.class;
        } else if (temporalType.equals(MetadataConstants.TIME)) {
            return java.sql.Time.class;
        } else if (temporalType.equals(MetadataConstants.TIMESTAMP)) {
            return java.sql.Timestamp.class;
        } else {
            // Should never hit because of validation.
            return null;
        }
    }
    
    /**
     * INTERNAL:
     * Helper method that will return a given field based on the provided attribute name.
     */
    public static Field getFieldForName(String fieldName, Class javaClass) {
        Field field = null;
        
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    field = (Field)AccessController.doPrivileged(new PrivilegedGetField(javaClass, fieldName, false));
                } catch (PrivilegedActionException exception) {
                    return null;
                }
            } else {
                field = PrivilegedAccessHelper.getField(javaClass, fieldName, false);
            }
        } catch (NoSuchFieldException nsfex) {
            return null;
        }
        
        return field;
    }

    /**
     * INTERNAL:
	 * Get the declared fields from a class using the doPriveleged security
     * access.
     */
	public static Field[] getFields(Class cls) {
        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
            try {
                return (Field[])AccessController.doPrivileged(new PrivilegedGetDeclaredFields(cls));
            } catch (PrivilegedActionException exception) {
                // no checked exceptions are thrown, so we should not get here
                return null;
            }
        } else {
            return PrivilegedAccessHelper.getDeclaredFields(cls);
        }
	}  
    
    /**
     * INTERNAL:
     * Returns a fully qualified table name based on the values passed in.
     * eg. catalog.schema.name
     */
    public static String getFullyQualifiedTableName(String tableName, String catalog, String schema) {
        // schema, attach it if specified
        if (! schema.equals("")) {
            tableName = schema + "." + tableName;
        }
    
        // catalog, attach it if specified
        if (! catalog.equals("")) {
            tableName = catalog + "." + tableName;
        }
        
        return tableName;
    }
    
    /**
     * INTERNAL:
     * Returns a fully qualified table name based on the values passed in.
     * eg. schema.catalog.name
     */
    public static String getFullyQualifiedTableName(String name, String defaultName, String catalog, String schema) {
        // check if a name was specified otherwise use the default
        String tableName = name;
        if (tableName.equals("")) {
            tableName = defaultName;
        }
    
        return getFullyQualifiedTableName(tableName, catalog, schema);
    }
    
    /**
     * INTERNAL:
     * Method to return a generic method return type.
     */
	public static Type getGenericReturnType(Method method) {
        // WIP - should use PrivilegedAccessController
        return method.getGenericReturnType();
    }
    
    /**
     * INTERNAL:
     * Method to return a generic field type.
     */
	public static Type getGenericType(Field field) {
        // WIP - should use PrivilegedAccessController
        return field.getGenericType();
    }
    
    /**
     * INTERNAL:
     * Helper method to return the map key type of a generic map.
     * Example, Map<String, Employee> will return String.class.
     */
	public static Class getMapKeyTypeFromGeneric(Type type) {
        return (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
    }
    
    /**
     * INTERNAL:
	 * If the methodName passed in is a declared method on cls, then return
     * the methodName. Otherwise return null to indicate it does not exist.
	 */
    protected static Method getMethod(String methodName, Class cls, Class[] params) {
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return (Method)AccessController.doPrivileged(new PrivilegedGetMethod(cls, methodName, params, true));
                } catch (PrivilegedActionException exception) {
                    return null;
                }
            } else {
                return PrivilegedAccessHelper.getMethod(cls, methodName, params, true);
            }
        } catch (NoSuchMethodException e1) {
            return null;
        }
    }
    
    /**
     * INTERNAL:
     * Find the method in the list where method.getName() == methodName.
     */
    public static Method getMethodForName(Method[] methods, String methodName) {
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
        
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        
        return null;
    }
    
    /**
     * INTERNAL:
     * Method to convert an xyz property name into a getXyz or isXyz method.
     */
    public static Method getMethodForPropertyName(String propertyName, Class cls) {
        Method method;
        
        String leadingChar = String.valueOf(propertyName.charAt(0)).toUpperCase();
        String restOfName = propertyName.substring(1);
        
        // Look for a getPropertyName() method
        method = getMethod(GET_PROPERTY_METHOD_PREFIX.concat(leadingChar).concat(restOfName), cls, new Class[]{});
        
        if (method == null) {
            // Look for an isPropertyName() method
            method = getMethod(IS_PROPERTY_METHOD_PREFIX.concat(leadingChar).concat(restOfName), cls, new Class[]{});
        }
        
        return method;
    }
    
    /**
     * INTERNAL:
	 * Get the methods from a class using the doPriveleged security access. 
     * This call returns only public methods from the given class and its 
     * superclasses.
     */
	public static Method[] getMethods(Class cls) {
        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
            try {
                return (Method[])AccessController.doPrivileged(new PrivilegedGetMethods(cls));
            } catch (PrivilegedActionException exception) {
                return null;
            }
        } else {
            return PrivilegedAccessHelper.getMethods(cls);
        }
    }
    
    /**
     * INTERNAL:
     * Return the raw class of the generic type.
     */
	public static Class getRawClassFromGeneric(Type type) {
        return (Class)(((ParameterizedType) type).getRawType());
    }
    
    /**
     * INTERNAL:
     * Helper method to return the type class of a ParameterizedType. This will 
     * handle the case for a generic collection. It now supports multiple types, 
     * e.g. Map<String, Employee>
     */
	public static Class getReturnTypeFromGeneric(Type type) {
        ParameterizedType pType = (ParameterizedType) type;
        
        if (java.util.Map.class.isAssignableFrom((Class) pType.getRawType())) {
            return (Class) pType.getActualTypeArguments()[1];
        }
        
        return (Class) pType.getActualTypeArguments()[0];
    }
    
    /**
     * INTERNAL:
	 * Method to convert a getMethod into a setMethod. This method could return 
     * null if the corresponding set method is not found.
	 */ 
    public static Method getSetMethod(Method method, Class cls) {
        String getMethodName = method.getName();
		Class[] params = new Class[] { method.getReturnType() };
            
        if (getMethodName.startsWith(GET_PROPERTY_METHOD_PREFIX)) {
            // Replace 'get' with 'set'.
            return getMethod(SET_PROPERTY_METHOD_PREFIX + getMethodName.substring(3), cls, params);
        }
        
        // methodName.startsWith(IS_PROPERTY_METHOD_PREFIX)
        // Check for a setXyz method first, if it exists use it.
        Method setMethod = getMethod(SET_PROPERTY_METHOD_PREFIX + getMethodName.substring(2), cls, params);
        
        if (setMethod == null) {
            // setXyz method was not found try setIsXyz
            return getMethod(SET_IS_PROPERTY_METHOD_PREFIX + getMethodName.substring(2), cls, params);
        }
        
        return setMethod;
	}

    /**
     * INTERNAL:
     */
    public static boolean havePersistenceAnnotationsDefined(AnnotatedElement[] annotatedElements) {
        for (AnnotatedElement annotatedElement : annotatedElements) {
            for (Annotation annotation : annotatedElement.getDeclaredAnnotations()) {
                if (annotation.annotationType().getName().startsWith(PERSISTENCE_PACKAGE_PREFIX)) {
                    return true;
                }
            }
        }
        
        return false;
    }
 
    /** 
     * INTERNAL:
     * 
     * Indicates whether the specified annotation is actually not present on 
     * the specified class. Used for defaulting. Need this check since the
     * isAnnotationPresent calls can return a false when true because of the
     * meta-data complete feature.
     */
    public static boolean isAnnotationNotPresent(Class annotation, AnnotatedElement annotatedElement, MetadataDescriptor descriptor) {
        return ! isAnnotationPresent(annotation, annotatedElement, descriptor.getJavaClass().getClassLoader());
    }
    
    /** 
     * INTERNAL:
     * 
     * Indicates whether the specified annotation is present on the specified 
     * class.
     */
    private static boolean isAnnotationPresent(Class annotation, AnnotatedElement annotatedElement, ClassLoader loader) {
        return annotatedElement.isAnnotationPresent(getClassForName(annotation.getName(), loader));
    }
    
    /** 
     * INTERNAL:
     * 
     * Indicates whether the specified annotation is present on the specified 
     * class.
     */
    public static boolean isAnnotationPresent(Class annotation, AnnotatedElement annotatedElement, MetadataDescriptor descriptor) {
        boolean isAnnotationPresent = isAnnotationPresent(annotation, annotatedElement, descriptor.getJavaClass().getClassLoader());
        
        if (isAnnotationPresent && descriptor.ignoreAnnotations()) {
            descriptor.getLogger().logWarningMessage(MetadataLogger.IGNORE_ANNOTATION, annotation, annotatedElement);
            return false;
        } else {
            return isAnnotationPresent;
        }
    }
    
    /** 
     * INTERNAL:
     * 
     * Indicates whether the specified annotation is present on java class
     * for the given descriptor metadata. 
     */
    public static boolean isAnnotationPresent(Class annotation, MetadataDescriptor descriptor) {
        return isAnnotationPresent(annotation, descriptor.getJavaClass(), descriptor);
    }
    
    /**
     * INTERNAL:
     * 
	 * Return true if this accessor represents a basic mapping.
     */
	public static boolean isBasic(MetadataAccessibleObject annotatedAccessor, MetadataDescriptor descriptor) {
        AnnotatedElement annotatedElement = annotatedAccessor.getAnnotatedElement();
        return isAnnotationPresent(Basic.class, annotatedElement, descriptor) ||
               isAnnotationPresent(Lob.class, annotatedElement, descriptor) ||
               isAnnotationPresent(Temporal.class, annotatedElement, descriptor) ||
               isAnnotationPresent(Enumerated.class, annotatedElement, descriptor);
    }
    
    /**
     * INTERNAL:
     * 
     * Method to return whether a class is a collection or not. 
     */
	public static boolean isCollectionClass(Class cls) {
        return Collection.class.isAssignableFrom(cls);
    }
    
    /**
     * INTERNAL:
     * 
     * Return true if this accessor represents an aggregate mapping. True is
     * returned id and @Embedded is found or if an @Embeddable is found on the
     * raw/reference class.
     */
	public static boolean isEmbedded(MetadataAccessibleObject accessibleObject, MetadataDescriptor descriptor) {
        AnnotatedElement annotatedElement = accessibleObject.getAnnotatedElement();
        
        if (isAnnotationNotPresent(Embedded.class, annotatedElement, descriptor) && isAnnotationNotPresent(EmbeddedId.class, annotatedElement, descriptor)) {
            Class rawClass = accessibleObject.getRawClass();
            // Use the class loader from the descriptor's java class and not
            // that from the rawClass since it may be an int, String etc. which
            // would not have been loaded from the temp loader, hence will not
            // find the Embeddable.class.
            return (isAnnotationPresent(Embeddable.class, rawClass, descriptor.getJavaClass().getClassLoader()) || descriptor.getProject().hasEmbeddable(rawClass));
        } else {
            // Still need to make the call since we may need to ignore it
            // because of meta-data complete.
            return isAnnotationPresent(Embedded.class, annotatedElement, descriptor);
        }
    }
    
    /**
     * INTERNAL:
     * 
     * Return true if this accessor represents an aggregate mapping. True is
     * returned id and @Embedded is found or if an @Embeddable is found on the
     * reference class.
     */
	public static boolean isEmbeddedId(MetadataAccessibleObject accessibleObject, MetadataDescriptor descriptor) {
        return isAnnotationPresent(EmbeddedId.class, accessibleObject.getAnnotatedElement(), descriptor);
    }
    
    /**
     * INTERNAL:
     * 
     * Method to return whether a collection type is a generic.
     */
	public static boolean isGenericCollectionType(Type type) {
        return (type instanceof ParameterizedType);
    }

    /**
     * INTERNAL:
     * 
     * Return true if this field accessor represents a m-m relationship.
     */
	public static boolean isManyToMany(MetadataAccessibleObject accessibleObject, MetadataDescriptor descriptor) {
        Class rawClass = accessibleObject.getRawClass();
        AnnotatedElement annotatedElement = accessibleObject.getAnnotatedElement();
        
        if (isAnnotationPresent(ManyToMany.class, annotatedElement, descriptor)) {
            if (MetadataHelper.isSupportedCollectionClass(rawClass)) {
                return true;
            } else {
                descriptor.getValidator().throwInvalidCollectionTypeForRelationship(descriptor.getJavaClass(), rawClass, annotatedElement);
            }
        } 
        
        return false;
    }
    
    /**
     * INTERNAL:
     * 
	 * Return true if this accessor represents a m-1 relationship.
     */
	public static boolean isManyToOne(MetadataAccessibleObject annotatedAccessor, MetadataDescriptor descriptor) {
        return isAnnotationPresent(ManyToOne.class, annotatedAccessor.getAnnotatedElement(), descriptor);
    }
    
    /**
     * INTERNAL:
     * 
     * Method to return whether a class is a map or not. 
     */
    public static boolean isMapClass(Class cls) {
        return Map.class.isAssignableFrom(cls);
    }
    
    /**
     * INTERNAL:
     * 
	 * Return true if this accessor represents a 1-m relationship.
     */
	public static boolean isOneToMany(MetadataAccessibleObject accessibleObject, MetadataDescriptor descriptor) {
        AnnotatedElement annotatedElement = accessibleObject.getAnnotatedElement();
        
        if (isAnnotationNotPresent(OneToMany.class, annotatedElement, descriptor)) {
            if (MetadataHelper.isGenericCollectionType(accessibleObject.getRelationType()) && 
                MetadataHelper.isSupportedCollectionClass(accessibleObject.getRawClass()) && 
                descriptor.getProject().containsDescriptor(accessibleObject.getReferenceClassFromGeneric())) {
                
                descriptor.getLogger().logConfigMessage(MetadataLogger.ONE_TO_MANY_MAPPING, annotatedElement);
                return true;
            }
        } else {
            // Still need to make the call since we may need to ignore it
            // because of meta-data complete.
            if (isAnnotationPresent(OneToMany.class, annotatedElement, descriptor)) {
                if (MetadataHelper.isSupportedCollectionClass(accessibleObject.getRawClass())) {
                    return true;
                } else {
                    descriptor.getValidator().throwInvalidCollectionTypeForRelationship(descriptor.getJavaClass(), accessibleObject.getRawClass(), annotatedElement);
                }
            }
        }
        
        return false;
    }
    
	/**
     * INTERNAL: 
     * 
     * Return true if this accessor represents a 1-1 relationship.
     */
	public static boolean isOneToOne(MetadataAccessibleObject accessibleObject, MetadataDescriptor descriptor) {
        AnnotatedElement annotatedElement = accessibleObject.getAnnotatedElement();
        
        if (isAnnotationNotPresent(OneToOne.class, annotatedElement, descriptor)) {
            if (descriptor.getProject().containsDescriptor(accessibleObject.getRawClass()) && 
                ! isEmbedded(accessibleObject, descriptor)) {
                
                descriptor.getLogger().logConfigMessage(MetadataLogger.ONE_TO_ONE_MAPPING, annotatedElement);
                return true;
            } else {
                return false;
            }
        } else {
            // Still need to make the call since we may need to ignore it
            // because of meta-data complete.
            return isAnnotationPresent(OneToOne.class, annotatedElement, descriptor);
        }
    }
    
    /**
     * INTERNAL:
     * 
     * Returns true is the given class is primitive wrapper type.
     */
    public static boolean isPrimitiveWrapperClass(Class cls) {
        return Long.class.isAssignableFrom(cls) ||
               Short.class.isAssignableFrom(cls) ||
               Float.class.isAssignableFrom(cls) ||
               Byte.class.isAssignableFrom(cls) ||
               Double.class.isAssignableFrom(cls) ||
               Number.class.isAssignableFrom(cls) ||
               Boolean.class.isAssignableFrom(cls) ||
               Integer.class.isAssignableFrom(cls) ||
               Character.class.isAssignableFrom(cls) ||
               String.class.isAssignableFrom(cls) ||
               java.math.BigInteger.class.isAssignableFrom(cls) ||
               java.math.BigDecimal.class.isAssignableFrom(cls) ||
               java.util.Date.class.isAssignableFrom(cls) ||
               java.util.Calendar.class.isAssignableFrom(cls);
    }
     
    /**
     * INTERNAL:
     * 
     * Method to return whether a class is a supported Collection. EJB 3.0 spec 
     * currently only supports Collection, Set, List and Map.
     */
	public static boolean isSupportedCollectionClass(Class cls) {
        return cls.equals(Collection.class) || 
               cls.equals(Set.class) || 
               cls.equals(List.class) || 
               cls.equals(Map.class);
    }
    
    /**
     * INTERNAL:
     * 
     * Search the class for an attribute with a name matching 'attributeName'
     */
    public static boolean isValidAttributeName(String attributeName, Class javaClass) {
        Field attributes[] = getFields(javaClass);
        
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].getName().equals(attributeName)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * INTERNAL:
     * 
     * Returns true if the given class is a valid blob type.
     */ 
    public static boolean isValidBlobType(Class cls) {
        return cls.equals(byte[].class) ||
               cls.equals(Byte[].class) ||
               cls.equals(java.sql.Blob.class);
    }
    
    /**
     * INTERNAL:
     * 
     * Returns true if the given class is a valid clob type.
     */  
    public static boolean isValidClobType(Class cls) {
        return cls.equals(char[].class) ||
               cls.equals(String.class) ||
               cls.equals(Character[].class) ||
               cls.equals(java.sql.Clob.class);
    }
    
    /**
     * INTERNAL:
     * 
     * Returns true is the given class is or extends java.util.Date.
     */
    public static boolean isValidDateType(Class cls) {
        return java.util.Date.class.isAssignableFrom(cls);
    }
    
    /**
     * INTERNAL:
     * 
     * Return true if the given class is a valid enum type.
     */
    public static boolean isValidEnumeratedType(Class cls) {
        return cls.isEnum();    
    }
    
    /**
     * INTERNAL:
     * 
     * Returns true if the given class is a valid lob type.
     */
    public static boolean isValidLobType(Class cls) {
        return isValidClobType(cls) || isValidBlobType(cls);
    }
    
    /**
     * INTERNAL:
     */
    public static boolean isValidPersistenceMethodName(String methodName) {
        return methodName.startsWith(GET_PROPERTY_METHOD_PREFIX) || methodName.startsWith(IS_PROPERTY_METHOD_PREFIX);
    }
    
    /**
     * INTERNAL:
     * 
     * Returns true if the given class is valid for SerializedObjectMapping.
     */
    public static boolean isValidSerializedType(Class cls) {
        if (cls.isPrimitive()) {
            return false;
        }
        
        if (isPrimitiveWrapperClass(cls)) {    
            return false;
        }   
        
        if (isValidLobType(cls)) {
            return false;
        }
        
        if (isValidTemporalType(cls)) {
            return false;
        }
     
        return true;   
    }
     
    /**
     * INTERNAL:
     * 
     * Returns true if the given class is a valid temporal type and must be
     * marked temporal.
     */
    public static boolean isValidTemporalType(Class cls) {
        return (cls.equals(java.util.Date.class) ||
                cls.equals(java.util.Calendar.class) ||
                cls.equals(java.util.GregorianCalendar.class));
     }
     
    /**
     * INTERNAL:
     * 
     * Returns true if the given class is a valid timestamp locking type.
     */
    public static boolean isValidTimstampVersionLockingType(Class cls) {
        return (cls.equals(java.sql.Timestamp.class));
    }
     
    /**
     * INTERNAL:
     * 
     * Returns true if the given class is a valid version locking type.
     */
    public static boolean isValidVersionLockingType(Class cls) {
        return (cls.equals(int.class) ||
                cls.equals(Integer.class) ||
                cls.equals(short.class) ||
                cls.equals(Short.class) ||
                cls.equals(long.class) ||
                cls.equals(Long.class));
    }
    
    /** 
     * INTERNAL:
     * 
     * Indicates whether the class should ignore annotations. Note that such 
     * classes should already have their descriptors with PKs added to 
     * session's project.
     */
    public static boolean shouldIgnoreAnnotations(Class cls, HashMap<Class, MetadataDescriptor> metadataDescriptors) {
        MetadataDescriptor descriptor = metadataDescriptors.get(cls);
        
        if (descriptor != null) {
            return descriptor.ignoreAnnotations();
        } else {
            return false;
        }
    }
}
