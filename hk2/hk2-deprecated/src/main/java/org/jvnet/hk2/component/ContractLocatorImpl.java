/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2012 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.hk2.component.InhabitantsFile;
import com.sun.hk2.component.ScopedInhabitant;
import org.glassfish.hk2.ContractLocator;
import org.glassfish.hk2.Provider;
import org.glassfish.hk2.Scope;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the {@link org.glassfish.hk2.ContractLocator} interface.
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
@Deprecated
public class ContractLocatorImpl<T> implements ContractLocator<T> {

    private static final Logger logger = Logger.getLogger(ContractLocatorImpl.class.getName());

    private final ServiceLocator serviceLocator;
    private String name;
    private Type type=null;
    private String typeName=null;
    private Collection<Class<? extends Annotation>> qualifiers = new ArrayList<Class<? extends Annotation>>();
    private Scope scope;

    // byContract, else byType
    private final boolean byContract;

    public ContractLocatorImpl(ServiceLocator serviceLocator, Type clazz, boolean byContract) {
        this.serviceLocator = serviceLocator;
        this.byContract = byContract;
        this.type=clazz;
    }

    ContractLocatorImpl(ServiceLocator serviceLocator, String clazzName, boolean byContract) {
        this.serviceLocator = serviceLocator;
        this.byContract = byContract;
        this.typeName=clazzName;
    }

    private void warnOnUsage() {
        logger.log(Level.WARNING,
                "name and scope are currently only appropriate for byContract usage; (name: {0}; type: {1})",
                new Object[] {name, getTypeName()});
    }
    
    public String getTypeName() {
        if (null == type) {
            return typeName;
        }
        
        return TypeLiteral.getRawType(type).getName();
    }

    @Override
    public ContractLocator<T> named(String name) {
        this.name = name;
        if (null != name && !name.isEmpty() && !byContract) {
            warnOnUsage();
        }
        return this;
    }

    @Override
    public ContractLocator<T> in(Scope scope) {
        this.scope = scope;
        return this;
    }

    @Override
    public ContractLocator<T> annotatedWith(Class<? extends Annotation> annotation) {
        qualifiers.add(annotation);
        return this;
    }

    @Override
    public T get() {
        Provider<T> provider = getProvider();

        return provider == null ? null : provider.get();
    }

    @Override
    public <U> U getByType(Class<U> type) {
        Provider<T> provider = getProvider();

        return provider == null ? null :
                (U) (type == null ? provider.get() :
                        provider.getByType(org.jvnet.tiger_types.Types.erasure(type)));
    }

    @Override
    public Provider<T> getProvider() {
        Collection<Provider<T>> providers = all(true);
        return (providers.isEmpty()?null:providers.iterator().next());
    }

    @Override
    public Collection<Provider<T>> all() {
        return all(false);
    }

    public Collection<Provider<T>> all(boolean stopAtFirstMatch) {

        Set<Provider<T>> providers = new HashSet<Provider<T>>();

        if (qualifiers.isEmpty()) {
            return getNonQualifiedInhabitants(stopAtFirstMatch);
        } else {
            List<String> tmpQualifiers = new ArrayList<String>();
            for (Class<? extends Annotation> annotation : qualifiers) {
                tmpQualifiers.add(annotation.getName());
            }
            for (Inhabitant<T> inh : inhabitants()) {
                List<String> declaredQualifiers = inh.metadata().get(InhabitantsFile.QUALIFIER_KEY);
                for (String declaredQualifier : declaredQualifiers) {
                    // todo : we need to look at the instances of the Annotations
                    // rather than the type on both the injection target and the
                    // candidate so we can ensure that annotation attribute matching is performed
                    tmpQualifiers.remove(declaredQualifier);
                }

                // if the injection point qualifiers are all satisfied, stop the query
                if (tmpQualifiers.isEmpty()) {
                    // check if the scope is fine.
                    if (scope==null) {
                        providers.add(inh);
                    } else {
                        if ((inh instanceof ScopedInhabitant) && ((ScopedInhabitant) inh).getScope().equals(scope)) {
                            providers.add(inh);
                        }
                    }
                    if (!providers.isEmpty() && stopAtFirstMatch) return providers;
                }
            }
        }
        return providers;
    }

    private Inhabitant<T> getNonQualifiedInhabitant() {
        if (name!=null && !name.isEmpty()) {
            return provider();
        }
        Inhabitant<T> inh = provider();
        if (inh==null) return null;
        if (inh.metadata().get(InhabitantsFile.QUALIFIER_KEY).isEmpty()) {
            return inh;
        }
        // we should find the inhabitant with no qualifier.
        for (Inhabitant<T> inhabitant : inhabitants()) {
            if (inhabitant.metadata().get(InhabitantsFile.QUALIFIER_KEY).isEmpty()) {
                return inhabitant;
            }
        }
        return null;
    }

    private Collection<Provider<T>> getNonQualifiedInhabitants(boolean stopOnFirst) {
        Set<Provider<T>> inhabitants = new HashSet<Provider<T>>();
        if (name!=null && !name.isEmpty()) {
            Inhabitant<T> provider = provider();
            if (provider!=null) {
                inhabitants.add(provider);
                if (stopOnFirst) return inhabitants;
            }
        }
        Inhabitant<T> inh = provider();
        if (inh==null) return inhabitants;
        if (inh.metadata().get(InhabitantsFile.QUALIFIER_KEY).isEmpty()) {
            inhabitants.add(inh);
            if (stopOnFirst) return inhabitants;
        }
        // we should find the inhabitant with no qualifier.
        for (Inhabitant<T> inhabitant : inhabitants()) {
            if (inhabitant.metadata().get(InhabitantsFile.QUALIFIER_KEY).isEmpty()) {
                //FIXME TODO if (inhabitant.getDescriptor().getNames().isEmpty()) {
                if (inhabitant.getDescriptor().getAdvertisedContracts().isEmpty()) {
                    inhabitants.add(inhabitant);
                    if (stopOnFirst) return inhabitants;
                }
            }
        }
        return inhabitants;
    }

    private Inhabitant<T> provider() {
        if (type!=null) {
            return serviceLocator.getService(type, name);
        } else {
            throw new ComponentException("**serviceLocator.getService(" + typeName + ", " + name + ") NOT supported");
            //TODO FIXME return serviceLocator.getService(typeName, name);
        }
    }

    private Collection<Inhabitant<T>> inhabitants() {
        return Collections.EMPTY_LIST;
//        if (type!=null) {
//            if (byContract) {
//                return serviceLocator.getService(type);
//            } else {
//                Class<T> classType = org.jvnet.tiger_types.Types.erasure(type);
//                return serviceLocator.getInhabitantsByType(classType);
//            }
//        } else {
//            if (byContract) {
//                return serviceLocator.getInhabitantsByContract(typeName);
//            } else {
//                return serviceLocator.getInhabitantsByType(typeName);
//            }
//        }
    }

}
