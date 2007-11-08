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
 * Used to allow iterating over a maps cache keys.
 */
public class IdentityMapKeyEnumeration implements Enumeration {
    protected FullIdentityMap map;
    protected Enumeration cacheKeysEnum;
    protected CacheKey nextKey;

    public IdentityMapKeyEnumeration(FullIdentityMap map) {
        this.map = map;
        this.cacheKeysEnum = map.getCacheKeys().elements();
    }

    public boolean hasMoreElements() {
        this.nextKey = getNextCacheKey();
        return this.nextKey != null;
    }

    public Object nextElement() {
        if (this.nextKey == null) {
            throw new NoSuchElementException("IdentityMapKeyEnumeration nextElement");
        }

        // CR#... Must check the read lock to avoid
        // returning half built objects.
        this.nextKey.checkReadLock();
        return this.nextKey;
    }

    protected CacheKey getNextCacheKey() {
        CacheKey key = null;
        while (cacheKeysEnum.hasMoreElements() && (key == null)) {
            key = (CacheKey)cacheKeysEnum.nextElement();
        }
        return key;
    }
}
