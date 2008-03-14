/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.jvnet.hk2.component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.AnnotatedElement;
import java.lang.annotation.Annotation;
import java.util.logging.Logger;

/**
 * InjectionManager is responsible for injecting resources into a component.
 * Resources tagged for injection are identified via an annotation which is
 * a parameter type of this class.
 *
 * @author Jerome Dochez
 */
public abstract class InjectionManager<T extends Annotation> {

    /**
     * Initializes the component by performing injection.
     *
     * @param component component instance to inject
     * @throws ComponentException
     *      if injection failed for some reason.
     */
    public void inject(Object component, Class<T> type) throws ComponentException {
        try {
            assert component!=null;

            // TODO: faster implementation needed.

            Class currentClass = component.getClass();
            while (!currentClass.equals(Object.class)) {
                // get the list of the instances variable
                for (Field field : currentClass.getDeclaredFields()) {

                    T inject = field.getAnnotation(type);
                    if (inject == null)     continue;

                    Class fieldType = field.getType();
                    try {
                        Object value = getValue(component, field, fieldType);
                        if (value != null) {
                            field.setAccessible(true);
                            field.set(component, value);
                            Injectable injectable;
                            try {
                                injectable = Injectable.class.cast(value);
                                if (injectable!=null) {
                                    injectable.injectedInto(component);
                                }
                            } catch (Exception e) {
                            }

                        } else {
                            if(!isOptional(inject)) {
                                Logger.getAnonymousLogger().info("Cannot inject " + field + " in component" + component);   
                                throw new UnsatisfiedDepedencyException(field);
                            }
                        }
                    } catch (ComponentException e) {
                        if (!isOptional(inject)) {
                            throw new UnsatisfiedDepedencyException(field,e);
                        }
                    } catch (IllegalAccessException e) {
                        throw new ComponentException("Injection failed on " + field.toGenericString(), e);
                    }
                }
                for (Method method : currentClass.getDeclaredMethods()) {
                    T inject = method.getAnnotation(type);
                    if (inject == null)     continue;

                    if (method.getReturnType() != void.class) {
                        throw new ComponentException("Injection failed on %s : setter method is not declared with a void return type",method.toGenericString());
                    }

                    Class<?>[] paramTypes = method.getParameterTypes();

                    if (paramTypes.length > 1) {
                        throw new ComponentException("injection failed on %s : setter method takes more than 1 parameter",method.toGenericString());
                    }
                    if (paramTypes.length == 0) {
                        throw new ComponentException("injection failed on %s : setter method does not take a parameter",method.toGenericString());
                    }

                    try {
                        Object value = getValue(component, method, paramTypes[0]);
                        if (value != null) {
                            method.setAccessible(true);
                            method.invoke(component, value);
                            try {
                                Injectable injectable = Injectable.class.cast(value);
                                if (injectable!=null) {
                                    injectable.injectedInto(component);
                                }
                            } catch (Exception e) {
                            }
                        } else {
                            if (!isOptional(inject))
                                throw new UnsatisfiedDepedencyException(method);
                        }
                    } catch (IllegalAccessException e) {
                        throw new ComponentException("Injection failed on " + method.toGenericString(), e);
                    } catch (InvocationTargetException e) {
                        throw new ComponentException("Injection failed on " + method.toGenericString(), e);
                    }
                }
                currentClass = currentClass.getSuperclass();
            }
        } catch (LinkageError e) {
            // reflection could trigger additional classloading and resolution, so it can cause linkage error.
            // report more information to assist diagnosis.
            // can't trust component.toString() as the object could be in an inconsistent state.
            Class<?> cls = component.getClass();
            LinkageError x = new LinkageError("Failed to inject " + cls +" from "+cls.getClassLoader());
            x.initCause(e);
            throw x;
        }
    }

    protected boolean isOptional(T annotation) {
        return false;
    }

    protected abstract Object getValue(Object component, AnnotatedElement annotated, Class type) throws ComponentException;
}
