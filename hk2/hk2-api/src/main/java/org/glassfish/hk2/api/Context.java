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

import java.lang.annotation.Annotation;

import org.jvnet.hk2.annotations.Contract;

/**
 * An implementation of this must be put into the system in order to
 * create contextual instances of services.  If there is more than
 * one active implementation available for the same scope on the same
 * thread a runtime exception will be thrown when the scope is accessed.
 * <p>
 * An implementation of Context must be in the Singleton scope
 * 
 * @author jwells
 * @param <T> This must be the type for which this is a context.  For example,
 * if your scope is SecureScope, then your context must implement Context&lt;SecureScope&gt;
 *
 */
@Contract
public interface Context<T> {
    /**
     * The scope for which this is the context
     * 
     * @return may not return null, must return the
     * scope for which this is a context
     */
    public Class<? extends Annotation> getScope();
    
    /**
     * Creates a contextual instance of this ActiveDescriptor by calling its
     * create method if there is no other matching contextual instance.  If there
     * is already a contextual instance it is returned.  If parent is null then this
     * must work like the find call
     * 
     * @param activeDescriptor The descriptor to use when creating instances
     * @param root The extended provider for the outermost parent being created
     * 
     * @return A context instance.  This value may NOT be null
     */
    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor, ServiceHandle<?> root);
    
    /**
     * Determines if this context has a value for the given key
     * 
     * @param descriptor The descriptor to look for in this context
     * @return true if this context has a value associated with this descriptor
     */
    public boolean containsKey(ActiveDescriptor<?> descriptor);
    
    /**
     * This method is called when {@link ServiceHandle#destroy()} method is called.
     * It is up to the context implementation whether or not to honor this destruction
     * request based on the lifecycle requirements of the context
     * 
     * @param descriptor A non-null descriptor upon which {@link ServiceHandle#destroy()}
     * has been called
     */
    public void destroyOne(ActiveDescriptor<?> descriptor);
    
    /**
     * Returns true if the findOrCreate method can return null
     * 
     * @return true if null is a legal value from the findOrCreate method
     */
    public boolean supportsNullCreation();
    
    /**
     * True if this context is active, false otherwise
     * 
     * @return true if this context is active, false otherwise
     */
    public boolean isActive();

    /**
     * Shut down this context.
     */
    public void shutdown();
}
