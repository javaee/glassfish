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

import org.glassfish.hk2.PreDestroy;
import org.glassfish.hk2.Provider;
import org.glassfish.hk2.ScopeInstance;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A particular instanciation of a {@link org.glassfish.hk2.Scope}.
 *
 * <p>
 * For example, for the "request scope", an instance
 * of {@link ScopeInstanceImpl} is created for each request.
 * 
 * @author Kohsuke Kawaguchi
 * @see org.glassfish.hk2.Scope#current()
 */
@SuppressWarnings("unchecked")
public final class ScopeInstanceImpl implements ScopeInstance, PreDestroy {
    private static final Logger logger = Logger.getLogger(ScopeInstanceImpl.class.getName());
    
    /**
     * Human readable scope instance name for debug assistance. 
     */
    public final String name;

    private final Map backend;

    public ScopeInstanceImpl(String name, Map backend) {
        this.name = name;
        this.backend = backend;
    }

    public ScopeInstanceImpl(Map backend) {
        this.name = super.toString();
        this.backend = backend;
    }
    
    public String toString() {
        return name;
    }

    public <T> T get(Provider<T> inhabitant) {
        return (T) backend.get(inhabitant);
    }

    public <T> T put(Provider<T> inhabitant, T value) {
        return (T) backend.put(inhabitant,value);
    }

    public void release() {
        synchronized(backend) {
            for (Object o : backend.values()) {
                if(o instanceof PreDestroy) {
                    logger.log(Level.FINER, "calling PreDestroy on {0}", o);
                    ((PreDestroy)o).preDestroy();
                }
            }
            backend.clear();
        }
    }

    public void preDestroy() {
        release();
    }
}
