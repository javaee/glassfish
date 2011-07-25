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

import com.sun.hk2.component.AbstractCreatorImpl;
import com.sun.hk2.component.ConstructorCreator;
import org.glassfish.hk2.*;
import org.glassfish.hk2.Factory;
import org.glassfish.hk2.Scope;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: 5/31/11
 * Time: 11:06 AM
 * To change this template use File | Settings | File Templates.
 */
class BinderImpl<V> implements Binder<V>, ResolvedBinder<V> {

    String name;
    String typeName;
    Class<? extends Scope> scope;
    final List<Class<? extends Annotation>> annotations = new ArrayList<Class<? extends Annotation>>();
    final List<String> contracts = new ArrayList<String>();
    final BinderFactoryImpl owner;

    BinderImpl(BinderFactoryImpl owner) {
        this.owner = owner;
    }

    @Override
    public NamedBinder<V> named(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NamedBinder<V> annotatedWith(Class<? extends Annotation> annotation) {
        this.annotations.add(annotation);
        return this;
    }

    @Override
    public ResolvedBinder<V> to(String className) {
        typeName = className;
        return this;
    }

    @Override
    public <T extends V> ResolvedBinder<T> to(Class<? extends T> serviceClass) {
        AbstractResolvedBinder<T> resolvedBinder = new TypeBasedBinder<T>(this, serviceClass);
        owner.add(resolvedBinder);
        return resolvedBinder;
    }

    @Override
    public <T extends V> ResolvedBinder<T> to(TypeLiteral<T> typeLiteral) {
        AbstractResolvedBinder<T> resolvedBinder = new TypeLiteralBasedBinder<T>(this, typeLiteral);
        owner.add(resolvedBinder);
        return resolvedBinder;
    }

    @Override
    public <T extends V> void toInstance(T instance) {
        AbstractResolvedBinder<T> resolvedBinder = new InstanceBasedBinder<T>(this, instance);
        owner.add(resolvedBinder);
//        in(Singleton.class);
    }
    
    @Override
    public <T extends V> ResolvedBinder<T> toFactory(final org.glassfish.hk2.Factory<T> provider) {

        return new AbstractResolvedBinder<T>(this) {
            @Override
            void registerIn(Habitat habitat) {
                MultiMap<String, String> metadataMap = super.populateMetadata();
                // todo : we need to reconcile the fact we don't have the type and passing null below.
                Inhabitant<T> inh = new AbstractCreatorImpl<T>(null, habitat, metadataMap) {
                    @Override
                    public T create(Inhabitant onBehalfOf) throws ComponentException {
                        T t = provider.get();
                        inject(habitat, t, onBehalfOf);
                        return t;
                    }
                };
                super.registerIn(habitat, inh);
            }
        };
    }

    @Override
    public <T extends V> ResolvedBinder<T> toFactory(final Class<? extends org.glassfish.hk2.Factory<? extends T>> factoryType) {
        return new AbstractResolvedBinder<T>(this) {
            @Override
            void registerIn(Habitat habitat) {
                MultiMap<String, String> metadataMap = super.populateMetadata();
                // todo : we need to reconcile the fact we don't have the type and passing null below.
                Inhabitant<T> inh = new AbstractCreatorImpl<T>(null, habitat, metadataMap) {
                    @Override
                    public T create(Inhabitant onBehalfOf) throws ComponentException {
                        Inhabitant<? extends org.glassfish.hk2.Factory<? extends T>> factoryInhabitant =
                                habitat.getInhabitantByType(factoryType);
                        if (factoryInhabitant==null) {
                            factoryInhabitant = new ConstructorCreator<Factory<? extends T>>(factoryType, habitat, null);
                        }
                        Factory<? extends T> f = factoryInhabitant.get();
                        T t = f.get();
                        inject(habitat, t, onBehalfOf);
                        return t;
                    }
                };
                super.registerIn(habitat, inh);
            }
        };
    }

    @Override
    public <T extends V> ResolvedBinder<T> toFactory(TypeLiteral<? extends org.glassfish.hk2.Factory<? extends T>> providerType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void in(Class<? extends Scope> scope) {
        this.scope = scope;
    }

    void addContract(Class<?> contract) {
        addContract(contract.getName());
    }

    void addContract(String contractName) {
        contracts.add(contractName);
    }
}
