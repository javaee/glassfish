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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Creator;
import org.jvnet.hk2.component.Creators;
import org.jvnet.hk2.component.Inhabitant;

/**
 * @author Kohsuke Kawaguchi
 */
@Deprecated
@SuppressWarnings("rawtypes")
public abstract class AbstractCreatorImpl<T> extends AbstractInhabitantImpl<T> implements Creator<T> {
    private final static Logger logger = Logger.getLogger(AbstractCreatorImpl.class.getName());

    protected final Class<? extends T> type;
    protected final ServiceLocator serviceLocator;

    public AbstractCreatorImpl(Class<? extends T> type, ServiceLocator serviceLocator, Map<String, List<String>> metadata) {
        super(org.glassfish.hk2.deprecated.utilities.Utilities.createDescriptor(type.getName(), null, metadata));

        this.type = type;
        this.serviceLocator = serviceLocator;
        
        clearMetadata();
        
        if (metadata != null) {
            for (Map.Entry<String, List<String>> entry : metadata.entrySet()) {
                String key = entry.getKey();
                for (String value : entry.getValue()) {
                    addMetadata(key, value);
                }
            }
        }
        
    }

    public String typeName() {
        return type.getName();
    }

    public final Class<? extends T> type() {
        return type;
    }

    public final T get(Inhabitant onBehalfOf) throws ComponentException {
        T o = create(onBehalfOf);
        logger.log(Level.FINER, "created object {0}", o);
        initialize(o, onBehalfOf);
        return o;
    }

    public boolean isActive() {
        return true;
    }

    @Override
    public void initialize(T t, Inhabitant onBehalfOf) throws ComponentException {
        serviceLocator.inject(t);
      // I could rely on injection, but the algorithm is slow enough for now that I
      // need a faster scheme.
        /**
         * TODO:  JRW I don't know what this is
      if (t instanceof InhabitantRequested) {
          ((InhabitantRequested) t).setInhabitant(onBehalfOf);
      }
      */
    }

    public void release() {
        // Creator creates a new instance every time,
        // so there's nothing to release here.
    }

    /**
     * Performs resource injection on the given instance from the given habitat.
     *
     * <p>
     * This method is an utility method for subclasses for performing injection.
     */
    protected void inject(ServiceLocator habitat, T t, Inhabitant<?> onBehalfOf) {
        logger.log(Level.FINER, "injection starting on {0}", t);
        
        habitat.inject(t);
        
        habitat.postConstruct(t);

        logger.log(Level.FINER, "injection finished on {0}", t);
    }

    /**
     * TODO:  Lets not do this one now and see if config needs it..
     * @return
     *
    protected InjectionManager createInjectionManager() {
      return new InjectionManager();
    }
     */

    /**
     * TODO:  JRW This is another one that is going to need some thought
     * @param t
     * @param h
     * @return
     *
    protected InjectionResolver[] getInjectionResolvers(T t, ServiceLocator h) {
        Collection<Inhabitant<? extends InjectionResolver>> targets = Creators.getAllInjectionResolvers(h);
        List<InjectionResolver> result = new ArrayList<InjectionResolver>();
        for (Inhabitant<? extends InjectionResolver> injectionResolverInhabitant : targets) {
            if (t!=null && t instanceof InjectionResolver) {
                if (injectionResolverInhabitant.isActive()) {
                    result.add(injectionResolverInhabitant.get());
                }
            } else {
                result.add(injectionResolverInhabitant.get());
            }
        }
        assert (!result.isEmpty());
        return result.toArray(new InjectionResolver[result.size()]);
    }
     */

    /**
     * TODO:  This one is also removed, if needed by config we will have to think about it
     * @param h
     * @param onBehalfOf
     * @return
     *
    // TODO: toggle this to turn multi-threaded injection on or off
    protected ExecutorService getExecutorService(Habitat h, Inhabitant<?> onBehalfOf) {
      return h.getComponent(ExecutorService.class, Constants.EXECUTOR_INHABITANT_INJECTION_MANAGER);
//      return null;
    }
    */

//    @Override
//    public void manage(Inhabitant<?> managedInhabitant) {
//      // TODO: test me
//      // NOP; creators should not manage anything!  Doing so may likely result in a memory leak.
//    }

}
