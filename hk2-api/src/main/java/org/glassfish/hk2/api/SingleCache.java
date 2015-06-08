/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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

/**
 * This cache can be used in some circumstances when there can be only one
 * of a service.  This is useful and can avoid an expensive lookup in certain
 * context implementations
 * 
 * @author jwells
 * @param <T> The type of service stored and returned from this cache
 *
 */
public interface SingleCache<T> {
    /**
     * This can be used for scopes that will only every be created once.
     * The returned value must have been set previously with setCache.
     * If this is called when isCacheSet is false will result in a
     * RuntimeException
     * 
     * @return A value cached with this ActiveDescriptor
     */
    public T getCache();
    
    /**
     * Returns true if this cache has been set
     * 
     * @return true if there is a currently cached value, false
     * otherwise
     */
    public boolean isCacheSet();
    
    /**
     * Sets the value into the cache
     * 
     * @param cacheMe A single value that can be cached in this
     * active descriptor
     */
    public void setCache(T cacheMe);
    
    /**
     * Removes the cached value and makes it such
     * that this cache has not been set
     */
    public void releaseCache();

}
