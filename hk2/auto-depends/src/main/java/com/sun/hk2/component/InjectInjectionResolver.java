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
package com.sun.hk2.component;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.glassfish.hk2.Provider;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.*;
import org.jvnet.tiger_types.Types;

import javax.inject.Named;
import javax.inject.Qualifier;

/**
 * InjectInjectionResolver, handles all Inject annotations
 */
public class InjectInjectionResolver extends InjectionResolver<Inject> {

//    public static final boolean MANAGED_ENABLED = Habitat.MANAGED_INJECTION_POINTS_ENABLED;

    final Habitat habitat;

    public InjectInjectionResolver(Habitat habitat) {
        super(Inject.class);
        this.habitat = habitat;
    }

    public boolean isOptional(AnnotatedElement element, Inject annotation) {
        return annotation.optional();
    }

    /**
     * Obtains the value to inject, based on the type and {@link Inject} annotation.
     */
    @Override
    public <V> V getValue(final Object component,
                          final Inhabitant<?> onBehalfOf,
                          final AnnotatedElement target,
                          final Type genericType,
                          final Class<V> type) throws ComponentException {
        final Inject inject = target.getAnnotation(Inject.class);
        final Callable<V> callable = new Callable<V>() {
            @Override
            public V call() throws ComponentException {
                V result=null;

                if (type.isArray()) {
                    result = getArrayInjectValue(habitat, component, onBehalfOf, target, genericType, type);
                } else {
                    if (Types.isSubClassOf(type, org.glassfish.hk2.Factory.class)) {
                        result = getHolderInjectValue(habitat, component, onBehalfOf, target, genericType, type, inject);
                    } else {
                        if (genericType instanceof TypeVariable) {
                            // ok this is a bit more complicated, the user wants us to inject a parameterized type
                            // so we need to look at the class declaration to find out in the index of the
                            // declaration of that parameterized type and reconcile it with our inhabitant metadata.
                            TypeVariable[] typeVariables = component.getClass().getTypeParameters();
                            for (int i=0;i<typeVariables.length;i++) {
                                if (typeVariables[i].getName().equals(((TypeVariable) genericType).getName())) {
                                    // that's the parameterized type we are looking for.
                                    String parameterizedType = onBehalfOf.metadata().get(InhabitantsFile.PARAMETERIZED_TYPE).get(i);
                                    try {
                                        Class<?> clazz = component.getClass().getClassLoader().loadClass(parameterizedType);
                                        ContractLocatorImpl<V> contractLocator = new ContractLocatorImpl<V>(habitat, clazz, habitat.isContract(clazz));
                                        populateContractLocator(contractLocator, target, inject);
                                        result = contractLocator.get();
                                    } catch(ClassNotFoundException e) {
                                        Logger.getAnonymousLogger().warning("Cannot load class " + parameterizedType);
                                        return null;
                                    }
                                }
                            }
                        } else {
                            ContractLocatorImpl<V> contractLocator = new ContractLocatorImpl<V>(habitat, genericType, habitat.isContract(genericType));
                            populateContractLocator(contractLocator, target, inject);
                            result = contractLocator.get();
                        }
                    }
                }

                return result==null?null:validate(component, onBehalfOf, result);
            }
        };

        try {
            if (habitat.isContextualFactoriesPresent()) {
                InjectionPoint ip = new InjectionPointImpl(component, target, type, inject, onBehalfOf);
                return Hk2ThreadContext.captureIPandRun(ip, callable);
            } else {
                return callable.call();
            }
        } catch (Exception e) {
            if (e instanceof ComponentException) throw ComponentException.class.cast(e);
            throw new ComponentException(e);
        }
    }

    protected <V> V getArrayInjectValue(Habitat habitat,
                                        Object component,
                                        Inhabitant<?> onBehalfOf,
                                        AnnotatedElement target,
                                        Type genericType,
                                        Class<V> type) {
        V result;
        Class<?> ct = type.getComponentType();

        Collection<?> instances;
        if (habitat.isContract(ct)) {
            instances = getAllByContract(onBehalfOf, habitat, ct);
        } else {
            instances = getAllByType(onBehalfOf, habitat, ct);
        }

        result = type.cast(instances.toArray((Object[]) Array.newInstance(ct, instances.size())));
        // TODO: validate() here too
        return result;
    }

    protected <V> V getHolderInjectValue(final Habitat habitat,
                                         final Object component,
                                         final Inhabitant<?> onBehalfOf,
                                         final AnnotatedElement target,
                                         final Type genericType,
                                         final Class<V> type,
                                         final Inject inject) throws ComponentException {

        final Type t = Types.getTypeArgument(genericType, 0);
        final Class<?> finalType = Types.erasure(t);

        if (habitat.isContract(finalType)) {
            ContractLocatorImpl<V> contractLocator = new ContractLocatorImpl<V>(habitat, t, true);
            populateContractLocator(contractLocator, target, inject);
            return type.cast(contractLocator.getProvider());
        }

        // the receiver maybe requesting the inhabitant pointing to itself to have
        // access to its own metadata.
        try {
            if (finalType.cast(component) != null) {
                return type.cast(onBehalfOf);
            }
        } catch (ClassCastException e) {
            // ignore
        }

        ContractLocatorImpl<V> contractLocator = new ContractLocatorImpl<V>(habitat, finalType, false);
        return type.cast(contractLocator.getProvider());
    }

    protected <V> Provider<V> getProviderByContract(Habitat habitat, Inhabitant<?> onBehalfOf,
                                                  AnnotatedElement target, Type genericType, Inject inject)
            throws ComponentException {


        ContractLocatorImpl<V> contractLocator = new ContractLocatorImpl<V>(habitat, genericType, true);
        populateContractLocator(contractLocator, target, inject);
        return contractLocator.getProvider();
    }

    /**
     * Verifies the injection does not violate any integrity rules.
     *
     * @param component    the target component to be injected
     * @param toBeInjected the injected value
     */
    protected <V> V validate(Object component, Inhabitant<?> onBehalfOf, V toBeInjected) {
        Inhabitants.validate(component, toBeInjected); // will toss exception if there is a problem
        return toBeInjected;
    }

    protected Inhabitant<?> manage(Inhabitant<?> onBehalfOf, Inhabitant<?> inhabitant) {
        return inhabitant;
    }

//    /**
//     * Manage the fact that the inhabitant represented by "onBehalfOf" is being injected with 
//     * the component coming from "inhabitant".
//     * 
//     * @param onBehalfOf the inhabitant which is the target for injection
//     * @param inhabitant the inhabitant producing the service used to inject the onBehalfOf instance
//     * 
//     * @return a managed inhabitant, if under management; the unmanaged inhabitant argument otherwise
//     */
//    protected Inhabitant<?> manage(Inhabitant<?> onBehalfOf, Inhabitant<?> inhabitant) {
//      if (null == inhabitant || null == onBehalfOf || onBehalfOf == inhabitant || !MANAGED_ENABLED) {
//        return inhabitant;
//      }
//      Inhabitant<?> scopedClone = inhabitant.scopedClone();
//      onBehalfOf.manage(scopedClone);
//      return scopedClone;
//    }

    @SuppressWarnings("unchecked")
    protected <V> Collection<V> manage(Inhabitant<?> onBehalfOf, Iterable<?> inhabitants) {
        if (null == inhabitants) {
            return null;
        }

        final ArrayList<V> managed = new ArrayList<V>();
        for (Object iObj : inhabitants) {
            Inhabitant<V> i = (Inhabitant<V>) iObj;
            managed.add((V) manage(onBehalfOf, i).get());
        }

        return managed;
    }

    @SuppressWarnings("unchecked")
    protected <V> Collection<V> getAllByType(Inhabitant<?> onBehalfOf, Habitat habitat, Class<V> ct) {
        return (Collection<V>) manage(onBehalfOf, habitat.getInhabitantsByType(ct));
    }

    @SuppressWarnings("unchecked")
    protected <V> Collection<V> getAllByContract(Inhabitant<?> onBehalfOf, Habitat habitat, Class<V> ct) {
        return (Collection<V>) manage(onBehalfOf, habitat.getInhabitantsByContract(ct.getName()));
    }


    void populateContractLocator(ContractLocatorImpl contractLocator, AnnotatedElement target, Inject inject) {
        Named named = target.getAnnotation(Named.class);
        String name = inject.name();
        if (named!=null && !inject.name().isEmpty()) {
            throw new RuntimeException("Field or method [" + target.toString() + "] is annotated with both a @Named" +
                    " annotation as well as a @Inject.name() value, please choose");
        }
        if (named!=null) {
            name = named.value();
        }
        contractLocator.named(name);

        // now we need to obtain all qualifiers on the injection target so we narraw the search.
        Annotation[] targetAnnotations = target.getAnnotations();

        // if there is only one annotation, it's @Inject, we can ignore qualifiers narrowing
        if (name.isEmpty() && targetAnnotations.length > 1) {
            for (Annotation annotation : target.getAnnotations()) {
                for (Annotation annotationAnnotation : annotation.annotationType().getAnnotations()) {
                    if (annotationAnnotation.annotationType()==Qualifier.class) {
                        contractLocator.annotatedWith(annotation.annotationType());
                    }
                }
            }
        }
    }
}
