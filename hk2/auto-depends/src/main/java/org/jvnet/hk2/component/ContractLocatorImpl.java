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

import org.glassfish.hk2.*;
import org.glassfish.hk2.ComponentException;
import org.glassfish.hk2.MultiMap;
import org.glassfish.hk2.Scope;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Implementation of the {@link ContractLocator} interface
 * @author Jerome Dochez
 */
class ContractLocatorImpl<T> implements ContractLocator<T> {

    String name;
    Scope scope;
    final List<Class<? extends Annotation>> annotations = new ArrayList<Class<? extends Annotation>>();
    final Class<T> contract;
    final boolean byContract;
    final Habitat habitat;

    ContractLocatorImpl(Habitat habitat, Class<T> contract, boolean byContract) {
        this.habitat = habitat;
        this.contract = contract;
        this.byContract = byContract;
    }

    @Override
    public ContractLocator<T> named(String name) {
        this.name = name;
        return this;
    }

    @Override
    public ContractLocator<T> in(Scope scope) {
        this.scope = scope;
        return this;
    }

    @Override
    public ContractLocator<T> annotatedWith(Class<? extends Annotation>... annotations) {
        this.annotations.addAll(Arrays.asList(annotations));
        return this;
    }

    @Override
    public Providers<T> getProviders() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Provider<T> getProvider() {
        if (byContract) {
            return new Provider<T>() {
                @Override
                public T get() throws ComponentException {
                    return habitat.getComponent(contract, name);

                }
            };
        } else {
            return new Provider<T>() {
                @Override
                public T get() throws ComponentException {
                    return habitat.getByType(contract);
                }
            };
        }
    }

    @Override
    public T get() {
        return getProvider().get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public MultiMap<String, String> metadata() {
        return getInhabitant(getProvider()).metadata();
    }

    @Override
    public Collection<Class<? extends Annotation>> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    @Override
    public Collection<String> getContracts() {
        List<String> cc = new ArrayList<String>();
        cc.add(contract.getName());
        return cc;
    }

    @Override
    public String getTypeName() {
        return getInhabitant(getProvider()).typeName();
    }

    private Inhabitant<T> getInhabitant(Provider<T> provider) {
        if (provider instanceof Inhabitant) {
            return  (Inhabitant<T>) getProvider();
        }
        throw new IllegalArgumentException("Provider instance not an inhabitant");

    }
}
