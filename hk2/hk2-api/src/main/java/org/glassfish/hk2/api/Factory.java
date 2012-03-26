/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.api;

import org.jvnet.hk2.annotations.Contract;

/**
 * This interface should be implemented in order to provide
 * a factory for another type.  This is useful when the type
 * has some reason that it cannot be a created in the usual way.
 * <p>
 * A factory may not have a TypeVariable or a Wildcard as its
 * actual type.  A factory may have any scope, and the scope
 * of the factory is independent of the scope of the type it
 * is providing.
 * <p>
 * The scope and qualifiers of the objects this factory is producing
 * must be placed on the provide method itself.  Objects created with
 * this method will be put into the scope on the provide method, and
 * will have the qualifiers of the provide method.
 * <p>
 * A factory is generally added with the {@link FactoryDescriptors} helper
 * class, though factories can also be registered independently.
 * 
 * @author jwells
 * @param <T> This must be the type of entity for which this is a factory.
 * For example, if this were a factory for Foo, then your factory
 * must implement Factory&lt;Foo&gt;.
 *
 */
@Contract
public interface Factory<T> {
    /**
     * This method will create instances of the type of this factory.  The provide
     * method must be annotated with the desired scope and qualifiers.
     * 
     * @return The produces object
     */
    public T provide();
    
    /**
     * This method will dispose of objects created with this scope.  This method should
     * not be annotated, as it is naturally paired with the provide method
     * 
     * @param instance The instance to dispose of
     */
    public void dispose(T instance);

}
