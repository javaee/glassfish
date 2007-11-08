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
package oracle.toplink.essentials.internal.security;

import java.security.*;
import java.lang.reflect.*;

/**
 * INTERNAL:
 * Privileged Access Helper provides a utility so all calls that require privileged access can use the same code
 * 
 * For users that wish to use a security manager and disable the use of doPrivileged, users can
 * set one of two system flags (through the java -Dxxxxx option):
 *
 * oracle.j2ee.toplink.security.usedoprivileged=false
 * oracle.j2ee.security.usedoprivileged=false
 */
public class PrivilegedAccessHelper {
    private static boolean shouldUsePrivilegedAccess = false;
    private static boolean shouldSecurityManagerBeChecked = true;

    /**
     * Finding a field within a class potentially has to navigate through it's superclasses to eventually
     * find the field.  This method is called by the public getDeclaredField() method and does a recursive
     * search for the named field in the given classes or it's superclasses.
     */
    private static Field findDeclaredField(Class javaClass, String fieldName) throws NoSuchFieldException {
        try {
            return javaClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            Class superclass = javaClass.getSuperclass();
            if (superclass == null) {
                throw ex;
            } else {
                return findDeclaredField(superclass, fieldName);
            }
        }
    }

    /**
     * Finding a method within a class potentially has to navigate through it's superclasses to eventually
     * find the method.  This method is called by the public getDeclaredMethod() method and does a recursive
     * search for the named method in the given classes or it's superclasses.
     */
    private static Method findMethod(Class javaClass, String methodName, Class[] methodParameterTypes) throws NoSuchMethodException {
        try {
            return javaClass.getDeclaredMethod(methodName, methodParameterTypes);
        } catch (NoSuchMethodException ex) {
            Class superclass = javaClass.getSuperclass();
            if (superclass == null) {
                throw ex;
            } else {
                try{
                    return findMethod(superclass, methodName, methodParameterTypes);
                }catch (NoSuchMethodException lastEx){
                    throw ex;
                }
            }
        }
    }

    /**
     * Execute a java Class.forName().  Wrap the call in a doPrivileged block if necessary.
     * @param className
     */
    public static Class getClassForName(final String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    /**
     * Execute a java Class.forName() wrap the call in a doPrivileged block if necessary.
     * @param className
     * @param initialize
     * @param loader
     * @throws java.lang.ClassNotFoundException
     */
    public static Class getClassForName(final String className, final boolean initialize, final ClassLoader loader) throws ClassNotFoundException {
        return Class.forName(className, initialize, loader);
    }

    /**
     * Gets the class loader for a given class.  Wraps the call in a privileged block if necessary
     */
    public static ClassLoader getClassLoaderForClass(final Class clazz) {
        return clazz.getClassLoader();
    }

    /**
     * Get the public constructor for the given class and given arguments and wrap it in doPrivileged if
     * necessary.  The shouldSetAccessible parameter allows the the setAccessible API to be called as well.
     * This option was added to avoid making multiple doPrivileged calls within InstantiationPolicy.
     * @param javaClass The class to get the Constructor for
     * @param args An array of classes representing the argument types of the constructor
     * @param shouldSetAccessible whether or not to call the setAccessible API
     * @throws java.lang.NoSuchMethodException
     */
    public static Constructor getConstructorFor(final Class javaClass, final Class[] args, final boolean shouldSetAccessible) throws NoSuchMethodException {
        Constructor result = javaClass.getConstructor(args);
        if (shouldSetAccessible) {
            result.setAccessible(true);
        }
        return result;
    }

    /**
     *  Get the context ClassLoader for a thread.  Wrap the call in a doPrivileged block if necessary.
     */
    public static ClassLoader getContextClassLoader(final Thread thread) {
        return thread.getContextClassLoader();
    }

    /**
     * Get the constructor for the given class and given arguments (regardless of whether it is public
     * or private))and wrap it in doPrivileged if necessary.  The shouldSetAccessible parameter allows
     * the the setAccessible API to be called as well. This option was added to avoid making multiple
     * doPrivileged calls within InstantiationPolicy.
     * @param javaClass The class to get the Constructor for
     * @param args An array of classes representing the argument types of the constructor
     * @param shouldSetAccessible whether or not to call the setAccessible API
     * @throws java.lang.NoSuchMethodException
     */
    public static Constructor getDeclaredConstructorFor(final Class javaClass, final Class[] args, final boolean shouldSetAccessible) throws NoSuchMethodException {
        Constructor result = javaClass.getDeclaredConstructor(args);
        if (shouldSetAccessible) {
            result.setAccessible(true);
        }
        return result;
    }

    /**
     * Get a field in a class or its superclasses and wrap the call in doPrivileged if necessary.
     * The shouldSetAccessible parameter allows the the setAccessible API to be called as well.
     * This option was added to avoid making multiple doPrivileged calls within InstanceVariableAttributeAccessor.
     * @param javaClass The class to get the field from
     * @param fieldName The name of the field
     * @param shouldSetAccessible whether or not to call the setAccessible API
     * @throws java.lang.NoSuchFieldException
     */
    public static Field getField(final Class javaClass, final String fieldName, final boolean shouldSetAccessible) throws NoSuchFieldException {
        Field field = (Field)findDeclaredField(javaClass, fieldName);
        if (shouldSetAccessible) {
            field.setAccessible(true);
        }
            return field;
    }

    /**
     * Get a field actually declared in a class and wrap the call in doPrivileged if necessary.
     * The shouldSetAccessible parameter allows the the setAccessible API to be called as well.
     * This option was added to avoid making multiple doPrivileged calls within InstanceVariableAttributeAccessor.
     * @param javaClass The class to get the field from
     * @param fieldName The name of the field
     * @param shouldSetAccessible whether or not to call the setAccessible API
     * @throws java.lang.NoSuchFieldException
     */
    public static Field getDeclaredField(final Class javaClass, final String fieldName, final boolean shouldSetAccessible) throws NoSuchFieldException {
        Field field = javaClass.getDeclaredField(fieldName);
        if (shouldSetAccessible) {
            field.setAccessible(true);
        }
        return field;
    }

    /**
     * Get the list of fields in a class.  Wrap the call in doPrivileged if necessary
     * Excludes inherited fields.
     * @param clazz the class to get the fields from.
     */
    public static Field[] getDeclaredFields(final Class clazz) {
        return clazz.getDeclaredFields();
    }

    /**
     * Return a method on a given class with the given method name and parameter 
     * types. This call will NOT traverse the superclasses. Wrap the call in 
     * doPrivileged if necessary.
     * @param method the class to get the method from
     * @param methodName the name of the method to get
     * @param methodParameters a list of classes representing the classes of the
     *  parameters of the method.
     */
    public static Method getDeclaredMethod(final Class clazz, final String methodName, final Class[] methodParameterTypes) throws NoSuchMethodException {
         return clazz.getDeclaredMethod(methodName, methodParameterTypes);
    }
    
    /**
     * Get a method declared in the given class. Wrap the call in doPrivileged 
     * if necessary. This call will traver the superclasses. The 
     * shouldSetAccessible parameter allows the the setAccessible API to be 
     * called as well. This option was added to avoid making multiple 
     * doPrivileged calls within MethodBasedAttributeAccessor.
     * @param javaClass The class to get the method from
     * @param methodName The name of the method to get
     * @param methodParameterTypes A list of classes representing the classes of the parameters of the mthod
     * @param shouldSetAccessible whether or not to call the setAccessible API
     * @throws java.lang.NoSuchMethodException
     */
    public static Method getMethod(final Class javaClass, final String methodName, final Class[] methodParameterTypes, final boolean shouldSetAccessible) throws NoSuchMethodException {
        Method method = findMethod(javaClass, methodName, methodParameterTypes);
        if (shouldSetAccessible) {
            method.setAccessible(true);
        }
        return method;
    }
    
    /**
     * Get the list of methods in a class. Wrap the call in doPrivileged if 
     * necessary. Excludes inherited methods.
     * @param clazz the class to get the methods from.
     */
    public static Method[] getDeclaredMethods(final Class clazz) {
        return clazz.getDeclaredMethods();
    }
    
    /**
     * Get the return type for a given method. Wrap the call in doPrivileged if necessary.
     * @param field
     */
    public static Class getFieldType(final Field field) {
        return field.getType();
    }

    /**
     * Get the line separator character.
     * Previous versions of TopLink always did this in a privileged block so we will continue to do so.
     */
    public static String getLineSeparator() {
        if (shouldUsePrivilegedAccess()) {
            return (String)AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        return System.getProperty("file.separator");
                    }
                });
        } else {
            return oracle.toplink.essentials.internal.helper.Helper.cr();
        }
    }

    /**
     * Get the list of parameter types for a given method.  Wrap the call in doPrivileged if necessary.
     * @param method The method to get the parameter types of
     */
    public static Class[] getMethodParameterTypes(final Method method) {
        return method.getParameterTypes();
    }

    /**
     * Get the return type for a given method. Wrap the call in doPrivileged if necessary.
     * @param method
     */
    public static Class getMethodReturnType(final Method method) {
        return method.getReturnType();
    }
    
    /**
     * Get the list of methods in a class. Wrap the call in doPrivileged if 
     * necessary. This call will traver the superclasses.
     * @param clazz the class to get the methods from.
     */
    public static Method[] getMethods(final Class clazz) {
        return clazz.getMethods();
    }
    
    /**
     * Get the value of the given field in the given object.
     */
    public static Object getValueFromField(final Field field, final Object object) throws IllegalAccessException {
        return field.get(object);
    }

    /**
     * Construct an object with the given Constructor and the given array of arguments.  Wrap the call in a
     * doPrivileged block if necessary.
     */
    public static Object invokeConstructor(final Constructor constructor, final Object[] args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return constructor.newInstance(args);
    }

    /**
     * Invoke the givenMethod on a givenObject using the array of parameters given.  Wrap in a doPrivileged block
     * if necessary.
     */
    public static Object invokeMethod(final Method method, final Object object, final Object[] parameters) throws IllegalAccessException, InvocationTargetException {
        // Ensure the method is accessible.
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        return method.invoke(object, parameters);
    }

    /**
     * Get a new instance of a class using the default constructor.  Wrap the call in a privileged block
     * if necessary.
     */
    public static Object newInstanceFromClass(final Class clazz) throws IllegalAccessException, InstantiationException {
        return clazz.newInstance();
    }

    /**
     * Set the value of a given field in the given object with the value given.  Wrap the call in a privileged block
     * if necessary.
     */
    public static void setValueInField(final Field field, final Object object, final Object value) throws IllegalAccessException {
        field.set(object, value);
    }

    /**
     * This method checks to see if calls should be made to doPrivileged.
     * In general, if a security manager is enabled, it will return true and if one
     * is not enabled, it will return false.
     * It will, however, always return false if either of the following two java properties is
     * set.
     * oracle.j2ee.toplink.security.usedoprivileged=false
     * oracle.j2ee.security.usedoprivileged=false
     * Note: it is not possible to run TopLink using doPrivileged blocks when there is no SecurityManager
     * enabled.
     */
    public static boolean shouldUsePrivilegedAccess() {
        // We will only detect whether to use doPrivileged once.
        if (shouldSecurityManagerBeChecked) {
            shouldSecurityManagerBeChecked = false;

            Boolean privilegedPropertySet = (Boolean)AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        boolean propertySet;

                        // check TopLink and OC4j doPrivileged flag.
                        String usePrivileged = System.getProperty("oracle.j2ee.toplink.security.usedoprivileged");
                        String oc4jUsePrivileged = System.getProperty("oracle.j2ee.security.usedoprivileged");
                        propertySet = (((usePrivileged != null) && usePrivileged.equalsIgnoreCase("false")) || ((oc4jUsePrivileged != null) && oc4jUsePrivileged.equalsIgnoreCase("false")));
                        return new Boolean(propertySet);
                    }
                });
            if (privilegedPropertySet.booleanValue()) {
                shouldUsePrivilegedAccess = false;
            } else {
                shouldUsePrivilegedAccess = (System.getSecurityManager() != null);
            }
        }
        return shouldUsePrivilegedAccess;
    }
}
