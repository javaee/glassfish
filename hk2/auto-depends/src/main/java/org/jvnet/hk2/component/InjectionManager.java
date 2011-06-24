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
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.component.concurrent.WorkManager;

/**
 * InjectionManager is responsible for injecting resources into a component.
 * Injection targets are identified by the injection resolver type attribute.
 *
 * @author Jerome Dochez
 */
@SuppressWarnings("unchecked")
public class InjectionManager {

   /**
     * Initializes the component by performing injection.
     *
     * @param component component instance to inject
     * @param targets the injection resolvers to resolve all injection points
     * @throws ComponentException
     *      if injection failed for some reason.
     */
    public void inject(Object component, InjectionResolver... targets) {
        syncDoInject(component, null, component.getClass(), targets);
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
        syncDoInject(component, onBehalfOf, component.getClass(), targets);
    }

    /**
     * Initializes the component by performing injection.
     *
     * @param component component instance to inject
     * @param onBehalfOf the inhabitant to do injection on behalf of
     * @param es the ExecutorService to use in order to handle the work load
     * @param targets the injection resolvers to resolve all injection points
     * @throws ComponentException
     *      if injection failed for some reason.
     */    
    public void inject(Object component, Inhabitant<?> onBehalfOf, ExecutorService es, InjectionResolver... targets) {
      try {
        if (null == es) {
          syncDoInject(component, onBehalfOf, component.getClass(), targets);
        } else {
          asyncDoInject(new InjectContext(component, onBehalfOf, component.getClass(), es, targets));
        }
      } catch (Exception e) {
        // we do this to bolster debugging
        if (ComponentException.class.isInstance(e)) {
          // it's presumed that there will be sufficient causes already present for these types of exceptions, no need to do more
          throw ComponentException.class.cast(e);
        }

        throw new ComponentException("injection failed on " + component + "; onBehalfOf: " + onBehalfOf, e);
      }
    }
    
    protected static class InjectContext {
      public final Object component;
      public final Inhabitant<?> onBehalfOf;
      public final Class<?> type;
      public final ExecutorService es;
      public final InjectionResolver[] targets;

      public InjectContext(final Object component,
          final Inhabitant<?> onBehalfOf,
          final Class type,
          final ExecutorService es,
          final InjectionResolver[] targets) {
        assert(null != component);
        this.component = component;
        this.onBehalfOf = onBehalfOf;
        this.type = type;
        this.es = es;
        this.targets = targets;
      }
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
        syncDoInject(component, null, type, targets);
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
    protected void syncDoInject(Object component,
                Inhabitant<?> onBehalfOf,
                Class type,
                InjectionResolver... targets) {
        assert component!=null;

        try {
            Class currentClass = type;
            while (currentClass!=null && Object.class != currentClass) {
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
                                handleInjectable(component, value);
                            } else {
                                if (!target.isOptional(field, inject)) {
                                    throw new UnsatisfiedDependencyException(field, inject);
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
                        Type[] genericParamTypes = setter.getGenericParameterTypes();

                        if (allowInjection(method, paramTypes)) {
                            try {
                                if (1 == paramTypes.length) {
                                  Object value = target.getValue(component, onBehalfOf, method, genericParamTypes[0], paramTypes[0]);
                                  if (value != null) {
                                      setter.setAccessible(true);
                                      setter.invoke(component, value);
                                      handleInjectable(component, value);
                                  } else {
                                      if (!target.isOptional(method, inject)) {
                                          throw new UnsatisfiedDependencyException(method, inject);
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
                                        throw new UnsatisfiedDependencyException(method, inject);
                                      }
                                    }
                                }
                                  
                                setter.invoke(component, params);
                                for (Object value : params) {
                                  handleInjectable(component, value);
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

    /**
     * Prototype for the multi-threaded version of inject().
     * 
     * @param component
     * @param onBehalfOf
     * @param type
     * @param es
     * @param targets
     */
    protected void asyncDoInject(InjectContext ic) {
      ArrayList<Runnable> tasks = new ArrayList<Runnable>();
      Class<?> classType = ic.type;
      while (null != classType && Object.class != classType) {
        tasks.add(0, new InjectClass(classType, ic));
        classType = classType.getSuperclass();
      }
      
      WorkManager wm = new WorkManager(ic.es, tasks.size());
      wm.executeAll(tasks);
      
      try {
        wm.awaitCompletion();
      } catch (WorkManager.ExecutionException e) {
        LinkageError cause = e.getCause(LinkageError.class);
        if (null != cause) {
          // reflection could trigger additional classloading and resolution, so it
          // can cause linkage error.
          // report more information to assist diagnosis.
          // can't trust component.toString() as the object could be in an
          // inconsistent state.
          LinkageError x = new LinkageError("injection failed on " + ic.type + 
              " from " + ic.type.getClassLoader());
          x.initCause(e);
          throw x;
        }
        
        throw e;
      }
    }

    
    protected class InjectClass implements Runnable {
      private final Class<?> classType;
      private final InjectContext ic;

      public InjectClass(final Class type,
          final InjectContext ic) {
        this.classType = type;
        this.ic = ic;
      }

      @Override
      public void run() {
        WorkManager wm = new WorkManager(ic.es, 2);
        wm.execute(new InjectMethods(this));
        wm.execute(new InjectFields(this));
        wm.awaitCompletion();
      }

    }
    

    protected class InjectFields implements Runnable {
      private final InjectClass iClass;
      
      public InjectFields(InjectClass iClass) {
        this.iClass = iClass;
      }

      @Override
      public void run() {
        ArrayList<Runnable> tasks = new ArrayList<Runnable>();
        for (Field field : iClass.classType.getDeclaredFields()) {
          for (InjectionResolver target : iClass.ic.targets) {
            Annotation inject = field.getAnnotation(target.type);
            if (inject != null) {
              tasks.add(new InjectField(iClass, field, inject, target));
            }
          }
        }

        WorkManager wm = new WorkManager(iClass.ic.es, tasks.size());
        wm.executeAll(tasks);
        wm.awaitCompletion();
      }
    }

    
    protected class InjectMethods implements Runnable {
      private final InjectClass iClass;
      
      public InjectMethods(InjectClass iClass) {
        this.iClass = iClass;
      }

      @Override
      public void run() {
        ArrayList<Runnable> tasks = new ArrayList<Runnable>();
        for (Method method : iClass.classType.getDeclaredMethods()) {
          for (InjectionResolver target : iClass.ic.targets) {
            Annotation inject = method.getAnnotation(target.type);
            if (inject != null) {
              tasks.add(new InjectMethod(iClass, method, inject, target));
            }
          }
        }

        WorkManager wm = new WorkManager(iClass.ic.es, tasks.size());
        wm.executeAll(tasks);
        wm.awaitCompletion();
      }
    }
    
    
    protected class InjectField implements Runnable {
      private final InjectContext ic;
      private final Field field;
      private final Annotation inject;
      private final InjectionResolver target;
      
      public InjectField(InjectClass iClass, Field field, Annotation inject, InjectionResolver target) {
        this.ic = iClass.ic;
        this.field = field;
        this.inject = inject;
        this.target = target;
      }

      @Override
      public void run() {
        Type genericType = field.getGenericType();
        Class fieldType = field.getType();
        try {
          Object value = target.getValue(ic.component, ic.onBehalfOf, field, genericType, fieldType);
          if (value != null) {
            field.setAccessible(true);
            field.set(ic.component, value);
            handleInjectable(ic.component, value);
          } else {
            if (!target.isOptional(field, inject)) {
              throw new UnsatisfiedDependencyException(field, inject);
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
    
    protected class InjectMethod implements Runnable {
      private final InjectContext ic;
      private final Method method;
      private final Annotation inject;
      private final InjectionResolver target;
      
      public InjectMethod(InjectClass iClass, Method method, Annotation inject, InjectionResolver target) {
        this.ic = iClass.ic;
        this.method = method;
        this.inject = inject;
        this.target = target;
      }

      @Override
      public void run() {
        Method setter = target.getSetterMethod(method, inject);
        if (void.class != setter.getReturnType()) {
          if (Collection.class.isAssignableFrom(setter.getReturnType())) {
            injectCollection(ic.component, setter, 
                target.getValue(ic.component, ic.onBehalfOf, method, null, setter.getReturnType()));
          } else {
            error_InjectMethodIsNotVoid(method);
          }
        }

        Class<?>[] paramTypes = setter.getParameterTypes();
        if (allowInjection(method, paramTypes)) {
          try {
            if (1 == paramTypes.length) {
              Object value = target.getValue(ic.component, ic.onBehalfOf, method, null, paramTypes[0]);
              if (value != null) {
                setter.setAccessible(true);
                setter.invoke(ic.component, value);
                handleInjectable(ic.component, value);
              } else {
                if (!target.isOptional(method, inject)) {
                  throw new UnsatisfiedDependencyException(method, inject);
                }
              }
            } else {
              // multi params
              setter.setAccessible(true);

              Type gparamType[] = setter.getGenericParameterTypes();
              Object params[] = new Object[paramTypes.length];
              for (int i = 0; i < paramTypes.length; i++) {
                Object value = target.getValue(ic.component, ic.onBehalfOf,
                    method, gparamType[i], paramTypes[i]);
                if (value != null) {
                  params[i] = value;
                } else {
                  if (!target.isOptional(method, inject)) {
                    throw new UnsatisfiedDependencyException(method, inject);
                  }
                }
              }

              setter.invoke(ic.component, params);
              for (Object value : params) {
                handleInjectable(ic.component, value);
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

    
    protected void handleInjectable(final Object component, final Object value) {
      try {
        Injectable injectable = Injectable.class.cast(value);
        if (injectable != null) {
          injectable.injectedInto(component);
        }
      } catch (Exception e) {
        Logger.getAnonymousLogger().log(Level.FINER, "swallowing error", e);
      }
    }
    
    
    protected void error_injectionException(InjectionResolver target, Annotation inject, AnnotatedElement injectionPoint, Throwable e) {
      Logger.getAnonymousLogger().log(Level.FINE, "injection failure", e);
      
      if (UnsatisfiedDependencyException.class.isInstance(e)) {
        if (injectionPoint == ((UnsatisfiedDependencyException)e).getUnsatisfiedElement()) {
          // no need to wrap again
          throw (UnsatisfiedDependencyException)e;
        }
        
        if (target.isOptional(injectionPoint, inject)) {
          return;
        } else {
          throw new UnsatisfiedDependencyException(injectionPoint, inject, e);
        }
      }
    
      if (null != e.getCause() && InvocationTargetException.class.isInstance(e)) {
        e = e.getCause();
      }
      
      throw new ComponentException(UnsatisfiedDependencyException.injection_failed_msg(injectionPoint, inject, e), e);
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

    
}
