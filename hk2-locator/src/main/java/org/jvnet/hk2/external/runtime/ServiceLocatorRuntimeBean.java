/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.external.runtime;

import org.jvnet.hk2.annotations.Contract;

/**
 * Runtime information about the ServiceLocator.
 * The ServiceLocatorRuntimeBean is specific to
 * this implementation of the HK2 API.  Further,
 * none of the values or operations on this bean
 * are guaranteed to be meaningful in the next
 * version of HK2, which may have used different
 * algorithms
 * 
 * @author jwells
 *
 */
@Contract
public interface ServiceLocatorRuntimeBean {
    /**
     * Returns the total number of descriptors
     * in this ServiceLocator.  Does not include
     * parent services
     * 
     * @return The number of services in this
     * ServiceLocator (does not include services
     * in the parent locator)
     */
    public int getNumberOfDescriptors();
    
    /**
     * Returns the current total number of children
     * attached to this ServiceLocator
     * 
     * @return The current number of children locators
     * attached to this ServiceLocator
     */
    public int getNumberOfChildren();
    
    /**
     * Returns the current size of the HK2 service
     * cache.  The service cache is used to optimize
     * frequent service lookups and injections
     * 
     * @return The current size of the HK2 service
     * cache
     */
    public int getServiceCacheSize();
    
    /**
     * Returns the maximum number of entries allowed
     * in the HK2 service cache.  The service cache is
     * used to optimize frequent service lookups and
     * injections
     * 
     * @return The maximum number of entries allowed
     * in the HK2 service cache
     */
    public int getServiceCacheMaximumSize();
    
    /**
     * Clears all entries from the HK2 service cache.
     * The service cache is used to optimize frequent
     * service lookups and injections.  Calling this
     * method may free up memory but will cause
     * degraded injection and lookup performance
     * until the cache can be built back up
     */
    public void clearServiceCache();
    
    /**
     * Returns the current size of the HK2 reflection
     * cache.  The reflection cache is used to minimize
     * the amount of reflection done by HK2
     * 
     * @return The current size of the HK2 reflection
     * cache
     */
    public int getReflectionCacheSize();
    
    /**
     * Clears all entries from the HK2 reflection
     * cache. The reflection cache is used to minimize
     * the amount of reflection done by HK2.  Calling this
     * method may free up memory but will cause
     * degraded service creation performance
     * until the cache can be built back up
     */
    public void clearReflectionCache();

}
