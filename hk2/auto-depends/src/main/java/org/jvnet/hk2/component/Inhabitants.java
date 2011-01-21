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
package org.jvnet.hk2.component;

import java.util.Collection;

/**
 * Factory for {@link Inhabitant}.
 * @author Kohsuke Kawaguchi
 * 
 * @deprecated Use {@link com.sun.hk2.component.Inhabitants} instead.
 */
public class Inhabitants {
    /**
     * Creates a singleton wrapper around existing object.
     * @deprecated Use {@link com.sun.hk2.component.Inhabitants} instead.
     */
    public static <T> Inhabitant<T> create(T instance) {
      return com.sun.hk2.component.Inhabitants.create(instance);
    }
    
    /**
     * Creates a {@link Inhabitant} by looking at annotations of the given type.
     * @deprecated Use {@link com.sun.hk2.component.Inhabitants} instead.
     */
    public static <T> Inhabitant<T> create(Class<T> c, Habitat habitat, MultiMap<String,String> metadata) {
      return com.sun.hk2.component.Inhabitants.create(c, habitat, metadata);
    }

    /**
     * Returns the list of names the service implementation in known. Services in hk2 are
     * indexed by the contract name and an optional name. There can also be some aliasing
     * so the same service can be known under different names.
     *
     * @param i instance of inhabitant to obtain its registration name
     * @param indexName the contract name this service is implementing
     * @param <T> contract type, optional
     * @return a collection of names (usually there is only one) under which this service
     * is registered for the passed contract name
     * 
     * @deprecated Use {@link com.sun.hk2.component.Inhabitants} instead.
     */
    public static <T> Collection<String> getNamesFor(Inhabitant<T> i, String indexName) {
      return com.sun.hk2.component.Inhabitants.getNamesFor(i, indexName);
    }

}
