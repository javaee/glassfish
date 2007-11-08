/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package oracle.toplink.essentials.internal.identitymaps;

import java.util.*;

/**
 * <p><b>Purpose</b>: Provide the capability to not cache objects at all.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Do nothing when an object is cached
 * </ul>
 *    @since TOPLink/Java 1.0
 */
public class NoIdentityMap extends IdentityMap {
    public NoIdentityMap(int size) {
        super(size);
    }

    /**
     *    locking for no identity.
     */
    public CacheKey acquire(Vector primaryKey) {
        CacheKey cacheKey = new CacheKey(primaryKey);
        cacheKey.acquire();
        return cacheKey;
    }

    /**
     * INTERNAL:
     * Used to print all the Locks in every identity map in this session.
     * The output of this method will go to log passed in as a parameter.
     */
    public void collectLocks(HashMap threadList) {
    }

    /**
     * Allow for the cache to be iterated on.
     */
    public Enumeration elements() {
        return new Vector(1).elements();
    }

    /**
     *    Return the object cached in the identity map
     *  Return null as no object is cached in the no IM.
     */
    public Object get(Vector primaryKey) {
        return null;
    }

    /**
     *    Return null since no objects are actually cached.
     */
    protected CacheKey getCacheKey(CacheKey searchKey) {
        return null;
    }

    /**
     *    @return 0 (zero)
     */
    public int getSize() {
        return 0;
    }

    /**
     * Return the number of actual objects of type myClass in the IdentityMap.
     * Recurse = true will include subclasses of myClass in the count.
     */
    public int getSize(Class myClass, boolean recurse) {
        return 0;
    }

    /**
     *    Get the write lock value from the cache key associated to the primarykey
     */
    public Object getWriteLockValue(Vector primaryKey) {
        return null;
    }

    /**
     * Allow for the cache keys to be iterated on.
     */
    public Enumeration keys() {
        return new Vector(1).elements();
    }

    /**
     * DO NOTHING.
     */
    public CacheKey put(Vector aVector, Object object, Object writeLockValue, long readTime) {
        return null;
    }

    /**
     * DO NOTHING
     */
    public void put(CacheKey key) {
        return;
    }

    /**
     * Do Nothing.
     * Return null, since no objects are cached.
     */
    public Object remove(Vector primaryKey) {
        return null;
    }

    /**
     * Do Nothing
     * Return null, since no objects are cached.
     */
    public Object remove(CacheKey searchKey) {
        return null;
    }

    public void setWriteLockValue(Vector primaryKey, Object writeLockValue) {
    }
}
