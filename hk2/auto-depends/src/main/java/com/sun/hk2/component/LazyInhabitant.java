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

import java.util.logging.Level;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Creator;
import org.jvnet.hk2.component.Creators;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

/**
 * @author Kohsuke Kawaguchi
 */
public class LazyInhabitant<T> extends EventPublishingInhabitant<T> implements ClassLoaderHolder {
    private final String typeName;

    /**
     * Lazy reference to {@link ClassLoader}.
     */
    private final Holder<ClassLoader> classLoader;

    protected final Habitat habitat;

    private final MultiMap<String,String> metadata;
    
    private final Inhabitant<?> lead;

    
    public LazyInhabitant(Habitat habitat, Holder<ClassLoader> cl, String typeName, MultiMap<String,String> metadata) {
        this(habitat, cl, typeName, metadata, null);
    }

    public LazyInhabitant(Habitat habitat, Holder<ClassLoader> cl, String typeName, MultiMap<String,String> metadata, Inhabitant<?> lead) {
      assert metadata!=null;
      this.habitat = habitat;
      this.classLoader = cl;
      this.typeName = typeName;
      this.metadata = metadata;
      this.lead = lead;
    }

    @Override
    public Inhabitant<?> lead() {
        return lead;
    }
    
    @Override
    public String typeName() {
        return typeName;
    }
    
    @Override
    public Class<? extends T> type() {
      // fetching is too heavy of an operation because it will activate/write the class
//        fetch();

        Inhabitant<T> real = this.real;
        if (null != real) {
            return real.type();
        } else {
            return loadClass();
        }
    }

    @Override
    public MultiMap<String,String> metadata() {
        return metadata;
    }

    @Override
    protected synchronized void fetch() {
        if (null == real) {
          Class<T> c = loadClass();
          real = Inhabitants.wrapByScope(c,createCreator(c),habitat);
        }
    }
    
    public final ClassLoader getClassLoader() {
        return classLoader.get();
    }
    
    @SuppressWarnings("unchecked")
    protected Class<T> loadClass() {
        logger.log(Level.FINER, "loading class for: {0}", typeName);
        
        final ClassLoader cl = getClassLoader();
        try {
            Class<T> c = (Class<T>) cl.loadClass(typeName);
            return c;
        } catch (ClassNotFoundException e) {
            throw new ComponentException("Failed to load "+typeName+" from " + cl, e);
        }
    }

    @Override
    public synchronized void release() {
        super.release();
        real = null;
    }

    /**
     * Creates {@link Creator} for instantiating objects.
     */
    protected Creator<T> createCreator(Class<T> c) {
        return Creators.create(c,habitat,metadata);
    }

}
