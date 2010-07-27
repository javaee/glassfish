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

import org.jvnet.hk2.component.*;

/**
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("unchecked")
public abstract class AbstractWombImpl<T> extends AbstractInhabitantImpl<T> implements Womb<T> {
    protected final Class<T> type;
    private final MultiMap<String,String> metadata;
    private final InjectionManager injectionMgr = new InjectionManager();

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
        initialize(o, onBehalfOf);
        return o;
    }

    public boolean isInstantiated() {
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
    protected void inject(Habitat habitat, T t, Inhabitant<?> onBehalfOf) {
        InjectionResolver<?>[] targets = {
            new InjectInjectionResolver(habitat, onBehalfOf),
            new LeadInjectionResolver(onBehalfOf),
        };
        injectionMgr.inject(t, targets);

        // postContruct call if any
        if (t instanceof PostConstruct) {
            ((PostConstruct)t).postConstruct();
        }
    }
    
}
