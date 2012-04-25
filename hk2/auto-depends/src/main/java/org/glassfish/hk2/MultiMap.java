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
package org.glassfish.hk2;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A read-nly Map from a key to multiple values. Order is significant among
 * values, and null values are allowed, although null keys are not.
 * 
 * @author Kohsuke Kawaguchi
 * @author Jerome Dochez
 */
public interface MultiMap<K, V> {
    
    /**
     * Returns the keys of type K.
     * 
     * @return Can be empty but never null. Read-only.
     */
    Set<K> keySet();
    
    /**
     * Returns the elements indexed by the provided key
     * 
     * @param k
     *            key for the values
     * @return Can be empty but never null. Read-only.
     */
    List<V> get(K k);
    
    /**
     * Checks if the map contains the given key.
     * 
     * @param k
     *            key to test
     * @return true if the map contains at least one element for this key
     */
    boolean containsKey(K k);
    
    /**
     * Checks if the map contains the given key(s), also extending the search to
     * including the sub collection.
     * 
     * @param k1
     *            key from top collection
     * @param k2
     *            key (value) from inner collection
     * 
     * @return true if the map contains at least one element for these keys
     */
    boolean contains(K k1, V k2);

    /**
     * Gets the first value if any, or null.
     * 
     * <p>
     * This is useful when you know the given key only has one value and you'd
     * like to get to that value.
     * 
     * @param k
     *            key for the values
     * @return null if the key has no values or it has a value but the value is
     *         null.
     */
    V getFirst(K k);

    /**
     * The complete read-only entry set.
     * 
     * @return a {@link java.util.Set} of {@link java.util.Map.Entry} of entries
     */
    Set<Entry<K,List<V>>> entrySet();
    
    /**
     * Returns the size of the map.
     * 
     * @return integer or 0 if the map is empty
     */
    int size();

}
