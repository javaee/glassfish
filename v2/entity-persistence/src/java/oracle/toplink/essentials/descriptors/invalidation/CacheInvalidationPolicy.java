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
package oracle.toplink.essentials.descriptors.invalidation;

import oracle.toplink.essentials.internal.identitymaps.CacheKey;

/**
 * PUBLIC:
 * A CacheInvalidationPolicy is used to set objects in TopLink's identity maps to be invalid
 * following given rules.  CacheInvalidationPolicy is the abstract superclass for all
 * policies used for cache invalidation.
 * By default in TopLink, objects do not expire in the cache.  Several different policies
 * are available to allow objects to expire.  These can be set on the Descriptor.
 * @see oracle.toplink.essentials.publicinterface.Descriptor
 * @see oracle.toplink.essentials.descriptors.cacheinvalidation.NoExpiryCacheInvalidationPolicy
 * @see oracle.toplink.essentials.descriptors.cacheinvalidation.DailyCacheInvalidationPolicy
 * @see oracle.toplink.essentials.descriptors.cacheinvalidation.TimeToLiveCacheInvalidationPolicy
 */
public abstract class CacheInvalidationPolicy implements java.io.Serializable {
    public static final long NO_EXPIRY = -1;

    /** this will represent objects that do not expire */
    protected boolean shouldUpdateReadTimeOnUpdate = false;

    /**
       * INTERNAL:
       * Get the next time when this object will become invalid
       */
    public abstract long getExpiryTimeInMillis(CacheKey key);

    /**
     * INTERNAL:
     * Return the remaining life of this object
     */
    public long getRemainingValidTime(CacheKey key) {
        long expiryTime = getExpiryTimeInMillis(key);
        long remainingTime = expiryTime - System.currentTimeMillis();
        if (remainingTime > 0) {
            return remainingTime;
        }
        return 0;
    }

    /**
     * INTERNAL:
     * return true if this object is expire, false otherwise.
     */
    public abstract boolean isInvalidated(CacheKey key, long currentTimeMillis);

    /**
     * PUBLIC:
     * Set whether to update the stored time an object was read when an object is updated.
     * When the read time is updated, it indicates to TopLink that the data in the object
     * is up to date.  This means that cache invalidation checks will occur relative to the
     * new read time.
     * By default, the read time will not be updated when an object is updated.
     * Often it is possible to be confident that the object is up to date after an update
     * because otherwise the update will fail because of the locking policies in use.
     */
    public void setShouldUpdateReadTimeOnUpdate(boolean shouldUpdateReadTime) {
        shouldUpdateReadTimeOnUpdate = shouldUpdateReadTime;
    }

    /**
     * PUBLIC:
     * Return whether objects affected by this CacheInvalidationPolicy should have
     * the read time on their cache keys updated when an update occurs.
     */
    public boolean shouldUpdateReadTimeOnUpdate() {
        return shouldUpdateReadTimeOnUpdate;
    }
}
