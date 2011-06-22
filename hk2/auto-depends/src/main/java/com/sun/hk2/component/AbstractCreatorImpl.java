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

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.component.*;
import org.jvnet.hk2.tracing.TracingThreadLocal;
import org.jvnet.hk2.tracing.TracingUtilities;

/**
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractCreatorImpl<T> extends AbstractInhabitantImpl<T> implements Creator<T> {
    private final static Logger logger = Logger.getLogger(AbstractCreatorImpl.class.getName());
  
    protected final Class<? extends T> type;
    protected final Habitat habitat; 
    private final MultiMap<String,String> metadata;

    public AbstractCreatorImpl(Class<? extends T> type, Habitat habitat, MultiMap<String,String> metadata) {
        this.type = type;
        this.habitat = habitat;
        this.metadata = metadata;
    }

    public final String typeName() {
        return type.getName();
    }

    public final Class<? extends T> type() {
        return type;
    }

    public final T get(Inhabitant onBehalfOf) throws ComponentException {
        try {
            if (TracingUtilities.isEnabled())
                TracingThreadLocal.get().push(this);

            T o = create(onBehalfOf);
            logger.log(Level.FINER, "created object {0}", o);
            initialize(o, onBehalfOf);
            return o;
        } finally {
            if (TracingUtilities.isEnabled())
                TracingThreadLocal.get().pop();
        }
    }

    public boolean isActive() {
        return true;
    }

    @Override
    public void initialize(T t, Inhabitant onBehalfOf) throws ComponentException {
      // I could rely on injection, but the algorithm is slow enough for now that I
      // need a faster scheme.
      if (t instanceof InhabitantRequested) {
          ((InhabitantRequested) t).setInhabitant(onBehalfOf);
      }
    }

    public void release() {
        // Creator creates a new instance every time,
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
    protected void inject(Habitat habitat, T t, Inhabitant<?> onBehalfOf) {
        logger.log(Level.FINER, "injection starting on {0}", t);

        InjectionManager injectionMgr = createInjectionManager();
        InjectionResolver[] targets = getInjectionResolvers(habitat);
        ExecutorService es = getExecutorService(habitat, onBehalfOf);
        injectionMgr.inject(t, onBehalfOf, es, targets);

        // postContruct call if any
        if (t instanceof PostConstruct) {
            logger.log(Level.FINER, "calling PostConstruct on {0}", t);
            ((PostConstruct)t).postConstruct();
        }
        
        logger.log(Level.FINER, "injection finished on {0}", t);
    }

    protected InjectionManager createInjectionManager() {
      return new InjectionManager();
    }
    
    protected InjectionResolver[] getInjectionResolvers(Habitat h) {
      Collection<InjectionResolver> targets = habitat.getAllByType(InjectionResolver.class);
      assert(!targets.isEmpty());
      return targets.toArray(new InjectionResolver[targets.size()]);    
    }

    // TODO: toggle this to turn multi-threaded injection on or off
    protected ExecutorService getExecutorService(Habitat h, Inhabitant<?> onBehalfOf) {
      return h.getComponent(ExecutorService.class, Constants.EXECUTOR_INHABITANT_INJECTION_MANAGER);
//      return null;
    }
    
//    @Override
//    public void manage(Inhabitant<?> managedInhabitant) {
//      // TODO: test me
//      // NOP; creators should not manage anything!  Doing so may likely result in a memory leak.
//    }
    
}
