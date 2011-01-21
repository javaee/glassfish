/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.component;

import com.sun.hk2.component.InjectionResolver;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * InjectionManager is responsible for injecting resources into a component.
 * Injection targets are identified by the injection resolver type attribute.
 *
 * @author Jerome Dochez
 */
@SuppressWarnings("unchecked")
public class InjectionManager {

/**    private final Habitat habitat;
    
    public InjectionManager(Habitat habitat) {
        this.habitat = habitat;
    }
**/
   /**
     * Initializes the component by performing injection.
     *
     * @param component component instance to inject
     * @param targets the injection resolvers to resolve all injection points
     * @throws ComponentException
     *      if injection failed for some reason.
     */
    public void inject(Object component,InjectionResolver... targets) {
        inject(component, null, component.getClass(), targets);
    }
  
   /**
     * Initializes the component by performing injection.
     *
     * @param component component instance to inject
     * @param onBehalfOf the inhabitant to do injection on behalf of
     * @param targets the injection resolvers to resolve all injection points
     * @throws ComponentException
     *      if injection failed for some reason.
     */    
    public void inject(Object component, Inhabitant<?> onBehalfOf, InjectionResolver... targets) {
        inject(component, onBehalfOf, component.getClass(), targets);
    }

    /**
      * Initializes the component by performing injection.
      *
      * @param component component instance to inject
      * @param type component class
      * @param targets the injection resolvers to resolve all injection points
      * @throws ComponentException
      *      if injection failed for some reason.
      */
    public void inject(Object component,
                Class type,
                InjectionResolver... targets) {

        inject(component, null, type, targets);
    }

    /**
      * Initializes the component by performing injection.
      *
      * @param component component instance to inject
      * @param onBehalfOf the inhabitant to do injection on behalf of
      * @param type component class
      * @param targets the injection resolvers to resolve all injection points
      * @throws ComponentException
      *      if injection failed for some reason.
      */
    public void inject(Object component,
                Inhabitant<?> onBehalfOf,
                Class type,
                InjectionResolver... targets) {
        try {
            assert component!=null;

            // TODO: faster implementation needed.

            Class currentClass = type;
            while (currentClass!=null && !currentClass.equals(Object.class)) {
                // get the list of the instances variable
                for (Field field : currentClass.getDeclaredFields()) {

                    for (InjectionResolver target : targets) {
                        Annotation inject = field.getAnnotation(target.type);
                        if (inject == null)     continue;

                        Type genericType = field.getGenericType();
                        Class fieldType = field.getType();
                        try {
                            Object value = target.getValue(component, onBehalfOf, field, genericType, fieldType);
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
                                if (!target.isOptional(field, inject)) {
                                    throw new UnsatisfiedDependencyException(field);
                                }
                            }
                        } catch (ComponentException e) {
                            error_injectionException(target, inject, field, e);
                        } catch (IllegalAccessException e) {
                            error_injectionException(target, inject, field, e);
                        } catch (RuntimeException e) {
                            error_injectionException(target, inject, field, e);
                        }
                    }
                }

                for (Method method : currentClass.getDeclaredMethods()) {
                    for (InjectionResolver target : targets) {
                        Annotation inject = method.getAnnotation(target.type);
                        if (inject == null)     continue;

                        Method setter = target.getSetterMethod(method, inject);

                        if (setter.getReturnType() != void.class) {
                            if (Collection.class.isAssignableFrom(setter.getReturnType())) {
                                injectCollection(component, setter, 
                                    target.getValue(component, onBehalfOf, method, null, setter.getReturnType()));
                                continue;
                            }
                            
                            error_InjectMethodIsNotVoid(method);
                        }

                        Class<?>[] paramTypes = setter.getParameterTypes();

                        if (allowInjection(method, paramTypes)) {
                            try {
                                if (1 == paramTypes.length) {
                                  Object value = target.getValue(component, onBehalfOf, method, null, paramTypes[0]);
                                  if (value != null) {
                                      setter.setAccessible(true);
                                      setter.invoke(component, value);
                                      try {
                                          Injectable injectable = Injectable.class.cast(value);
                                          if (injectable!=null) {
                                              injectable.injectedInto(component);
                                          }
                                      } catch (Exception e) {
                                      }
                                  } else {
                                      if (!target.isOptional(method, inject)) {
                                          throw new UnsatisfiedDependencyException(method);
                                      }
                                  }
                                } else {
                                  // multi params
                                  setter.setAccessible(true);
                                  
                                  Type gparamType[] = setter.getGenericParameterTypes();
                                  
                                  Object params[] = new Object[paramTypes.length]; 
                                  for (int i = 0; i < paramTypes.length; i++) {
                                    Object value = target.getValue(component, onBehalfOf, method, gparamType[i], paramTypes[i]);
                                    if (value != null) {
                                      params[i] = value;
                                    } else {
                                      if (!target.isOptional(method, inject)) {
                                        throw new UnsatisfiedDependencyException(method);
                                      }
                                    }
                                }
                                  
                                setter.invoke(component, params);
                                for (Object value : params) {
                                  try {
                                    Injectable injectable = Injectable.class.cast(value);
                                    if (injectable!=null) {
                                      injectable.injectedInto(component);
                                    }
                                  } catch (Exception e) {
                                  }
                                }
                              }
                            } catch (ComponentException e) {
                                error_injectionException(target, inject, setter, e);
                            } catch (IllegalAccessException e) {
                                error_injectionException(target, inject, setter, e);
                            } catch (InvocationTargetException e) {
                                error_injectionException(target, inject, setter, e);
                            } catch (RuntimeException e) {
                                error_injectionException(target, inject, setter, e);
                            }
                        }
                    }
                }
                currentClass = currentClass.getSuperclass();
            }
        } catch (LinkageError e) {
            // reflection could trigger additional classloading and resolution, so it can cause linkage error.
            // report more information to assist diagnosis.
            // can't trust component.toString() as the object could be in an inconsistent state.
            Class<?> cls = type;
            LinkageError x = new LinkageError("injection failed on " + cls + " from " + cls.getClassLoader());
            x.initCause(e);
            throw x;
        }

    }

    protected void error_injectionException(InjectionResolver target, Annotation inject, AnnotatedElement injectionPoint, Exception e) {
      Logger.getAnonymousLogger().log(Level.FINE, "injection failure", e);
      
      if (UnsatisfiedDependencyException.class.isInstance(e)) {
        if (injectionPoint == ((UnsatisfiedDependencyException)e).getUnsatisfiedElement()) {
          // no need to wrap again
          throw (UnsatisfiedDependencyException)e;
        }
        
        if (target.isOptional(injectionPoint, inject)) {
          return;
        } else {
          throw new UnsatisfiedDependencyException(injectionPoint, e);
        }
      }
      
      throw new ComponentException(UnsatisfiedDependencyException.injection_failed_msg(injectionPoint, e), e);
    }

    protected boolean allowInjection(Method method, Class<?>[] paramTypes) {
      if (paramTypes.length > 1) {
        error_InjectMethodHasMultipleParams(method);
      }
  
      if (paramTypes.length == 0) {
        error_InjectMethodHasNoParams(method);
      }
  
      return true;
    }

    protected void error_InjectMethodHasMultipleParams(Method method) {
      throw new ComponentException(
          "injection failed on %s : setter method takes more than 1 parameter",
          method.toGenericString());
    }

    protected void error_InjectMethodHasNoParams(Method method) {
      throw new ComponentException(
          "injection failed on %s : setter method does not take a parameter",
          method.toGenericString());
    }

    protected void error_InjectMethodIsNotVoid(Method method) {
      throw new ComponentException("Injection failed on %s : setter method is not declared with a void return type", method.toGenericString());
    }

    private void injectCollection(Object component, Method method, Object value) {
        if (value==null) {
            return;
        }
        Collection c = Collection.class.cast(value);
        Collection target = null;
        try {
            target = Collection.class.cast(method.invoke(component));
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return;
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return;
        }
        target.addAll(c);
    }
    
    /**
     * Initializes the component by performing injection.
     *
     * @param component component instance to inject
     * @throws ComponentException
     *      if injection failed for some reason.
     */
/*     public void inject(Object component, Class<T extends Annotation> type) throws ComponentException {
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
                    } catch (RuntimeException e) {
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
                    } catch (RuntimeException e) {
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
    */
}
