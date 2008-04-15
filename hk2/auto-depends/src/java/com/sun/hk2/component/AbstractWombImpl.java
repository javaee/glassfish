/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.Womb;
import org.jvnet.hk2.component.InjectionManager;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Lead;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.Collection;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractWombImpl<T> extends AbstractInhabitantImpl<T> implements Womb<T> {
    protected final Class<T> type;
    private final MultiMap<String,String> metadata;

    public AbstractWombImpl(Class<T> type, MultiMap<String,String> metadata) {
        this.type = type;
        this.metadata = metadata;
    }

    public final String typeName() {
        return type.getName();
    }

    public final Class<T> type() {
        return type;
    }

    public final T get(Inhabitant onBehalfOf) throws ComponentException {
        T o = create(onBehalfOf);
        initialize(o,onBehalfOf);
        return o;
    }

    public void  initialize(T t, Inhabitant onBehalfOf) throws ComponentException {
        // default is no-op
    }

    public void release() {
        // Womb creates a new instance every time,
        // so there's nothing to release here.
    }

    public MultiMap<String, String> metadata() {
        return metadata;
    }

    /**
     * Performs resource injection on the given instance from the given habitat.
     *
     * <p>
     * This method is an utility method for subclasses for performing injection.
     */
    protected void inject(final Habitat habitat, T t, final Inhabitant onBehalfOf) {
        // TODO: make it a field of Habitat to avoid unnecessary InjectionManager allocations
        (new InjectionManager<Inject>() {
            public boolean isOptional(Inject annotation) {
                return annotation.optional();
            }

            /**
             * Obtains the value to inject, based on the type and {@link Inject} annotation.
             */
            @SuppressWarnings("unchecked")
            protected Object getValue(Object component, AnnotatedElement target, Class type) throws ComponentException {
                if (type.isArray()) {
                    Class<?> ct = type.getComponentType();

                    Collection instances;
                    if(habitat.isContract(ct))
                        instances = habitat.getAllByContract(ct);
                    else
                        instances = habitat.getAllByType(ct);
                    return instances.toArray((Object[]) Array.newInstance(ct, instances.size()));
                } else {
                    if(habitat.isContract(type))
                        // service lookup injection
                        return habitat.getComponent(type, target.getAnnotation(Inject.class).name());

                    // ideally we should check if type has @Service or @Configured

                    // component injection
                    return habitat.getByType(type);
                }
            }
        }).inject(t, Inject.class);

        (new InjectionManager<Lead>() {
            protected Object getValue(Object component, AnnotatedElement target, Class type) throws ComponentException {
                Inhabitant lead = onBehalfOf.lead();
                if(lead==null)
                    // TODO: we should be able to check this error at APT, too.
                    throw new ComponentException(component.getClass()+" requested @Lead injection but this is not a companion");

                if(type==Inhabitant.class) {
                    return lead;
                }

                // otherwise inject the target object
                return lead.get();
            }
        }).inject(t, Lead.class);

        // postContruct call if any
        if(t instanceof PostConstruct)
            ((PostConstruct)t).postConstruct();
    }
}
