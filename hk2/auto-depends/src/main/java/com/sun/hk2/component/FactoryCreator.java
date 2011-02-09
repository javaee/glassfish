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

import java.security.AccessControlContext;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.ContextualFactory;
import org.jvnet.hk2.component.Factory;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InjectionPoint;
import org.jvnet.hk2.component.MultiMap;

/**
 * Creates an object from {@link Factory}.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("unchecked")
public class FactoryCreator<T> extends AbstractCreatorImpl<T> {
    private final static Logger logger = Logger.getLogger(FactoryCreator.class.getName());
    
    private final Inhabitant<? extends Factory> factory;

    public FactoryCreator(Class<T> type, Class<? extends Factory> factory, Habitat habitat, MultiMap<String,String> metadata) {
        this(type,habitat.getInhabitantByType(factory),habitat,metadata);
    }

    public FactoryCreator(Class<T> type, Inhabitant<? extends Factory> factory, Habitat habitat, MultiMap<String,String> metadata) {
        super(type, habitat, metadata);
        assert factory!=null;
        assert habitat!=null;
        this.factory = factory;
    }

    public T create(Inhabitant onBehalfOf) throws ComponentException {
        logger.log(Level.FINER, "factory {0} invoked", factory);
        Factory f = factory.get();
        T t;
        
        AccessControlContext acc = Hk2ThreadContext.getCallerACC();
        InjectionPoint ip = Hk2ThreadContext.getCallerIP();
        // these are not [currently] available for Holder based injection
//      assert(null != acc);
//      assert(null != ip);
        if (ContextualFactory.class.isInstance(f) && (null != ip || null != acc)) {
          t = type.cast(ContextualFactory.class.cast(f).getObject(ip, acc));
        } else {
          t = type.cast(f.getObject());
        }
        // potential security issue here if logged
//        logger.log(Level.FINER, "factory created object {0}", t);
        
        if (null == t) {
          // don't inject if its null!
          return null;
        }
        
        inject(habitat, t, onBehalfOf);
        return t;
    }
}
