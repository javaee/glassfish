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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.ComponentException;
import org.glassfish.hk2.ComponentProvider;
import org.glassfish.hk2.ContractLocator;
import org.glassfish.hk2.Provider;
import org.glassfish.hk2.Scope;

/**
 * Implementation of the {@link ContractLocator} interface.
 * 
 * <p/>
 * This is used for location byType or byContract.
 *
 * @author Jerome Dochez
 * @author Jeff Trent
 */
//TODO: support scope for byContract & byType
//TODO: support name for byType
//TODO: support annotation for byContract & byType
//TODO: support metadata for byContract & byType
class ContractLocatorImpl<T> implements ContractLocator<T> {

    private final Logger logger = Logger.getLogger(ContractLocatorImpl.class.getName());

    private final SimpleServiceLocator habitat;
    private String name;
//    private Scope scope;
//    private final List<Class<? extends Annotation>> annotations = new ArrayList<Class<? extends Annotation>>();

    private final Provider<T> getter;

    // byContract, else byType
    private final boolean byContract;

    ContractLocatorImpl(SimpleServiceLocator habitat, Class<T> clazz, boolean byContract) {
        this.habitat = habitat;
        this.byContract = byContract;
        this.getter = (byContract) ?
                getterByContract(clazz) :
                getterByType(clazz);
    }

    ContractLocatorImpl(SimpleServiceLocator habitat, String clazzName, boolean byContract) {
        this.habitat = habitat;
        this.byContract = byContract;
        this.getter = (byContract) ?
                getterByContract(clazzName) :
                getterByType(clazzName);
    }

    private Provider<T> getterByContract(final Class<T> clazz) {
        return new Provider<T>() {
            @Override
            public T get() throws ComponentException {
                return habitat.getComponent(clazz, name);
            }
        };
    }

    private Provider<T> getterByContract(final String clazzName) {
        return new Provider<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T get() throws ComponentException {
                return (T) habitat.getComponent(clazzName, name);
            }
        };
    }

    private Provider<T> getterByType(final Class<T> clazz) {
        return new Provider<T>() {
            @Override
            public T get() throws ComponentException {
                return habitat.getByType(clazz);
            }
        };
    }

    private Provider<T> getterByType(final String clazzName) {
        return new Provider<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T get() throws ComponentException {
                return (T) habitat.getByType(clazzName);
            }
        };
    }

    private void warnOnUsage() {
        logger.log(Level.WARNING, "name and scope are currently only appropriate for byContract usage");
    }

    @Override
    public ContractLocator<T> named(String name) {
        this.name = name;
        if (null != name && !byContract) {
            warnOnUsage();
        }
        return this;
    }

    @Override
    public ContractLocator<T> in(Scope scope) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContractLocator<T> annotatedWith(Class<? extends Annotation>... annotations) {
        throw new UnsupportedOperationException();
    }

    //    @Override
    public Provider<T> getProvider() {
        return getter;
    }

    @Override
    public T get() {
        return getProvider().get();
    }

    @Override
    public Collection<ComponentProvider<T>> all() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public ComponentProvider<T> getComponentProvider() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

}
