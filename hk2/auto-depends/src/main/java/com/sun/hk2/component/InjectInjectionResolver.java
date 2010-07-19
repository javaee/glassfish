/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.hk2.component;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.tiger_types.Types;

/**
 * InjectInjectionResolver, handles all Inject annotations
 */
/*public*/ class InjectInjectionResolver extends InjectionResolver<Inject> {

    private final Habitat habitat;
    private final Inhabitant<?> onBehalfOf;

    
    /*public*/ InjectInjectionResolver(Habitat habitat, Inhabitant<?> onBehalfOf) {
        super(Inject.class);
        this.habitat = habitat;
        this.onBehalfOf = onBehalfOf;
    }
  
    public boolean isOptional(AnnotatedElement element, Inject annotation) {
        return annotation.optional();
    }

    /**
     * Obtains the value to inject, based on the type and {@link Inject} annotation.
     */
    public <V> V getValue(Object component, AnnotatedElement target, Class<V> type) throws ComponentException {
        V result;
        if (type.isArray()) {
            Class<?> ct = type.getComponentType();

            Collection<?> instances;
            if (habitat.isContract(ct)) {
                instances = habitat.getAllByContract(ct);
            } else {
                instances = habitat.getAllByType(ct);
            }
            result = type.cast(instances.toArray((Object[]) Array.newInstance(ct, instances.size())));
        } else if (Types.isSubClassOf(type, Holder.class)){
            Type t = Types.getTypeArgument(((java.lang.reflect.Field) target).getGenericType(), 0);
            Class<?> finalType = Types.erasure(t);
            if (habitat.isContract(finalType)) {
                return type.cast(habitat.getInhabitants(finalType, target.getAnnotation(Inject.class).name()));
            }
            try {
                if (finalType.cast(component)!=null) {
                    return type.cast(onBehalfOf);
                }
            } catch(ClassCastException e) {
                // ignore
            }
            result = type.cast(habitat.getInhabitantByType(finalType));
        } else {
            if (habitat.isContract(type)) {
                // service lookup injection
                V res = habitat.getComponent(type, target.getAnnotation(Inject.class).name());
                return res;
            }
            
            // ideally we should check if type has @Service or @Configured

            // component injection
            result = habitat.getByType(type);
        }
        
        return validate(component, result);
    }
    
    /**
     * Verifies the injection does not violate any integrity rules.
     * 
     * @param component the target component to be injected
     * @param toBeInjected the injected value
     */
    protected <V> V validate(Object component, V toBeInjected) {
        Inhabitants.validate(component, toBeInjected); // will toss exception if there is a problem
        return toBeInjected;
    }

}
