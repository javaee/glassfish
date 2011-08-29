/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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
package org.glassfish.hk2.tests.basic.resolving;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import javax.inject.Named;
import javax.inject.Qualifier;

import org.glassfish.hk2.*;

import org.glassfish.hk2.inject.Injector;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Inhabitant;

import com.sun.hk2.component.InjectionResolver;
import org.jvnet.tiger_types.Types;

/**
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ContextInjectionResolver extends InjectionResolver<Context> {

    @Inject Services services;

    public ContextInjectionResolver() {
        super(Context.class);
    }

    @Override
    public boolean isOptional(AnnotatedElement annotated, Context annotation) {
        return true; // seems like all @Context are optional so far...
    }

    @Override
    public <V> V getValue(
            Object component,
            Inhabitant<?> onBehalfOf,
            AnnotatedElement annotated,
            Type genericType,
            Class<V> type) throws ComponentException {

        Class<V> targetType;
        if (Types.isSubClassOf(type, org.glassfish.hk2.Factory.class)) {
            targetType = Types.erasure(Types.getTypeArgument(genericType, 0));
        } else {
            targetType = type;
        }
        ContractLocator<V> locator = services.forContract(targetType);

        for (Annotation a : annotated.getAnnotations()) {
            final Class<? extends Annotation> ac = a.annotationType();
            if (Named.class.isAssignableFrom(ac)) {
                locator = locator.named(Named.class.cast(a).value());
            } else if (ac.isAnnotationPresent(Qualifier.class)) {
                locator = locator.annotatedWith(ac);
            }

            // todo: what to do about scopes?                
        }

        Provider<?> provider = locator.getProvider();
        if (provider==null) {
            if (Types.isSubClassOf(type, org.glassfish.hk2.Factory.class)) {
                return (V) services.byType(targetType).getProvider();
            } else {
                return services.byType(targetType).get();
            }
        }
        if (Types.isSubClassOf(type, org.glassfish.hk2.Factory.class)) {
            return (V) provider;
        } else {
            return (V) provider.get();
        }
    }


    public static class ContextInjectionModule implements Module {

        @Override
        public void configure(BinderFactory binderFactory) {
            binderFactory.bind(InjectionResolver.class).to(ContextInjectionResolver.class);
        }
    }
}
