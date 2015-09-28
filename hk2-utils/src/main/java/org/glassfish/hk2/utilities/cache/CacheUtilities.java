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
package org.glassfish.hk2.utilities.cache;

import org.glassfish.hk2.utilities.cache.internal.WeakCARCacheImpl;

/**
 * Utilities for creating caches
 * 
 * @author jwells
 *
 */
public class CacheUtilities {
    /**
     * Returns a WEAKCarCache with the given computable and the given maximum value size of the cache.
     * The Cache returned will have weak keys, so that when the key becomes only weakly reachable it
     * will be removed from the cache.  However, values will only be removed from the Cache when an operation
     * is performed on the cache or the method {@link WeakCARCache#clearStaleReferences()} is called
     * 
     * @param computable The computable that is used to get the V from the given K
     * @param maxSize The maximumSize of the cache
     * @param isWeak if true this will keep weak keyes, if false the keys will
     * be hard and will not go away even if they do not exist anywhere else
     * but this cache
     * @return A WeakCARCache that is empty
     */
    public static <K,V> WeakCARCache<K,V> createWeakCARCache(Computable<K,V> computable, int maxSize, boolean isWeak) {
        return new WeakCARCacheImpl<K,V>(computable, maxSize, isWeak);
    }

}
