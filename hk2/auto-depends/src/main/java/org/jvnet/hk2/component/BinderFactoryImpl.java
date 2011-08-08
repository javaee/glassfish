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

import org.glassfish.hk2.Binder;
import org.glassfish.hk2.BinderFactory;
import org.glassfish.hk2.NamedBinder;
import org.glassfish.hk2.TypeLiteral;

import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.*;

/**
 * Implementation of the {@link org.glassfish.hk2.BinderFactory} interface.
 *
 * @author Jerome Dochez
 */
class BinderFactoryImpl implements BinderFactory {

    final BinderFactory parent;
    final List<AbstractResolvedBinder> binders = new ArrayList<AbstractResolvedBinder>();

    BinderFactoryImpl(BinderFactory parent) {
        this.parent = parent;
    }

    @Override
    public BinderFactory inParent() {
        return parent;
    }

    @Override
    public Binder<Object> bind(String contractName) {
        BinderImpl<Object> binder = new BinderImpl<Object>(this);
        binder.addContract(contractName);
        return binder;
    }

    @Override
    public Binder<Object> bind(String... contractNames) {
        BinderImpl<Object> binder = new BinderImpl<Object>(this);
        for (String contractName: contractNames) {
            binder.addContract(contractName);
        }
        return binder;
    }

    @Override
    public <T> Binder<T> bind(Class<T> contract, Class<?>... contracts) {
        BinderImpl<T> binder = new BinderImpl<T>(this);
        binder.addContract(contract);
        for (Class<?> c : contracts) {
            binder.addContract(c);
        }
        return binder;
    }

    @Override
    public <T> Binder<T> bind(TypeLiteral<T> typeLiteral) {
        BinderImpl<T> binder = new BinderImpl<T>(this);
        binder.addContract(exploreType(typeLiteral), typeLiteral.getRawType());
        return binder;
    }

    @Override
    public NamedBinder<Object> bind() {
        return new BinderImpl<Object>(this);
    }

    void add(AbstractResolvedBinder<?> binder) {
        binders.add(binder);
    }

    void registerIn(Habitat habitat) {
        for (AbstractResolvedBinder<?> binder : binders) {
            binder.registerIn(habitat);
        }
        binders.clear();

    }


    private static void exploreType(Type type, StringBuilder builder) {
        if (type instanceof ParameterizedType) {
            builder.append(TypeLiteral.getRawType(type).getName());

            // we ignore wildcard types.
            Collection<Type> types = Arrays.asList(((ParameterizedType) type).getActualTypeArguments());
            Iterator<Type> typesEnum = types.iterator();
            List<Type> nonWildcards = new ArrayList<Type>();
            while(typesEnum.hasNext()) {
                Type genericType = typesEnum.next();
                if (!(genericType instanceof WildcardType))
                    nonWildcards.add(genericType);
            }
            if (!nonWildcards.isEmpty()) {
                builder.append("<");
                Iterator<Type> typesItr = nonWildcards.iterator();
                while(typesItr.hasNext()) {
                    exploreType(typesItr.next(), builder);
                    if (typesItr.hasNext()) builder.append(",");
                }
                builder.append(">");
            }
        } else {
            builder.append(TypeLiteral.getRawType(type).getName());
        }
    }

    static String exploreType(Type type) {
        StringBuilder builder = new StringBuilder();
        exploreType(type, builder);
        return builder.toString();
    }

    static String exploreType(TypeLiteral<?> typeLiteral) {
        StringBuilder builder = new StringBuilder();
        exploreType(typeLiteral.getType(), builder);
        return builder.toString();
    }
}
