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
package oracle.toplink.essentials.internal.sessions;

import java.util.*;

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.internal.ejb.cmp3.base.RepeatableWriteUnitOfWork;
import oracle.toplink.essentials.internal.identitymaps.*;
import oracle.toplink.essentials.internal.sessions.IdentityMapAccessor;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.essentials.queryframework.*;

/**
 * INTERNAL:
 * IdentityMapAccessor subclass for UnitOfWork
 * Overrides some initialization functionality and some behavoir having to do with
 * getting objects from identity maps.
 */
public class UnitOfWorkIdentityMapAccessor extends IdentityMapAccessor {
    public UnitOfWorkIdentityMapAccessor(AbstractSession session, IdentityMapManager identityMapManager) {
        super(session, identityMapManager);
    }

    /**
     * INTERNAL:
     * Return if their is an object for the primary key.
     */
    public boolean containsObjectInIdentityMap(Vector primaryKey, Class theClass, ClassDescriptor descriptor) {
        if (getIdentityMapManager().containsKey(primaryKey, theClass, descriptor)) {
            return true;
        }
        return ((UnitOfWorkImpl)getSession()).getParent().getIdentityMapAccessorInstance().containsObjectInIdentityMap(primaryKey, theClass, descriptor);
    }

    /**
     * INTERNAL:
     * This method overrides the getAllFromIdentityMap method in Session.  Invalidated Objects
     * will always be returned from a UnitOfWork.
     */
    public Vector getAllFromIdentityMap(Expression selectionCriteria, Class theClass, AbstractRecord translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean shouldReturnInvalidatedObjects) throws QueryException {
        return super.getAllFromIdentityMap(selectionCriteria, theClass, translationRow, valueHolderPolicy, true);
    }

    /**
     * INTERNAL:
     * Overide the getFromIdentityMapWithDeferredLock method on the session to ensure that
     * invalidated objects are always returned since this is a UnitOfWork
     */
    public Object getFromIdentityMapWithDeferredLock(Vector primaryKey, Class theClass, boolean shouldReturnInvalidatedObjects, ClassDescriptor descriptor) {
        return super.getFromIdentityMapWithDeferredLock(primaryKey, theClass, true, descriptor);
    }

    /**
     * INTERNAL:
     * Return the object from the identity map with the primary key and class.
     * The parent's cache must be checked after the child's,
     * if found in the parent, it must be registered/cloned (but must avoid looping).
     * Note: in a UnitOfWork, invalidated objects will always be returned from the identity map
     * In the parent session, only return the object if it has not been Invalidated
     */
    public Object getFromIdentityMap(Vector primaryKey, Class theClass, boolean shouldReturnInvalidatedObjects, ClassDescriptor descriptor) {
        Object objectFromCache = super.getFromIdentityMap(primaryKey, theClass, true, descriptor);

        if (objectFromCache != null) {
            return objectFromCache;
        }
        //Bug#4613774  In the parent session, only return the object if it has not been Invalidated
        return getAndCloneCacheKeyFromParent(primaryKey, theClass, shouldReturnInvalidatedObjects, descriptor);
    }

    /**
     * INTERNAL:
     * This method will return the object from the parent and clone it.
     * If the uow is RepeatableWriteUnitOfWork and the original object
     * from the parent corresponds to deleted unregistered object clone,
     * then the latter returned.
     */
    protected Object getAndCloneCacheKeyFromParent(Vector primaryKey, Class theClass, boolean shouldReturnInvalidatedObjects, ClassDescriptor descriptor) {
        // Note: Objects returned from the parent's identity map should include invalidated
        // objects. This is important because this internal method is used in the existence
        // check in the UnitOfWork.
        CacheKey cacheKey = ((UnitOfWorkImpl)getSession()).getParent().getIdentityMapAccessorInstance().getCacheKeyForObject(primaryKey, theClass, descriptor);
        if ((cacheKey == null) && ((UnitOfWorkImpl)getSession()).getParent().isUnitOfWork()) {//for nested unit of work
            //make parent clone and register object
            ((UnitOfWorkIdentityMapAccessor)((UnitOfWorkImpl)getSession()).getParent().getIdentityMapAccessorInstance()).getAndCloneCacheKeyFromParent(primaryKey, theClass, shouldReturnInvalidatedObjects, descriptor);
            //get the cachekey that was created in the parent.
            cacheKey = ((UnitOfWorkImpl)getSession()).getParent().getIdentityMapAccessorInstance().getCacheKeyForObject(primaryKey, theClass, descriptor);
        }

        Object objectFromCache = null;

        // this check could be simplfied to one line but would create a window
        // in which GC could remove the object and we would end up with a null pointer
        // as well we must inspect the cacheKey without locking on it.
        if ((cacheKey != null) && (shouldReturnInvalidatedObjects || !descriptor.getCacheInvalidationPolicy().isInvalidated(cacheKey, System.currentTimeMillis()))) {
            synchronized (cacheKey.getMutex()) {
                //if the object in the cachekey is null but the key is acquired then
                //someone must be rebuilding it or creating a new one.  Sleep until
                // it's finished. A plain wait here would be more efficient but we may not
                // get notified for quite some time (ie deadlock) if the other thread
                //is building the object.  Must wait and not sleep in order for the monitor to be released
                objectFromCache = cacheKey.getObject();
                try {
                    while (cacheKey.isAcquired() && (objectFromCache == null)) {
                        cacheKey.getMutex().wait(5);
                    }
                } catch (InterruptedException ex) {
                }
                if (objectFromCache == null) {
                    return null;
                }
            }
        } else {
            return null;
        }

        // Consider read-only class CR#4094
        if (getSession().isClassReadOnly(theClass, descriptor)) {
            // PERF: Just return the original object.
            return objectFromCache;
        }

        if(getSession() instanceof RepeatableWriteUnitOfWork ) {
            Object unregisteredDeletedClone = ((RepeatableWriteUnitOfWork)getSession()).getUnregisteredDeletedCloneForOriginal(objectFromCache);
            if(unregisteredDeletedClone != null) {
                return unregisteredDeletedClone;
            }
        }
        
        return ((UnitOfWorkImpl)getSession()).cloneAndRegisterObject(objectFromCache, cacheKey);
    }

    /**
     * INTERNAL:
     * Reset the entire object cache,
     * ** be careful using this.
     * This method blows away both this session's and its parents caches,
     * this includes the server cache or any other cache.
     * This throws away any objects that have been read in.
     * Extreme caution should be used before doing this because object identity will no longer
     * be maintained for any objects currently read in.  This should only be called
     * if the application knows that it no longer has references to object held in the cache.
     */
    public void initializeAllIdentityMaps() {
        super.initializeAllIdentityMaps();
        ((UnitOfWorkImpl)getSession()).getParent().getIdentityMapAccessor().initializeAllIdentityMaps();
    }
}
