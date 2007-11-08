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

import java.lang.ref.*;
import java.util.Vector;

/**
 * <p><b>Purpose</b>: Container class for storing objects in an IdentityMap.
 * The weak cache key uses a weak reference to allow garbage collection of its object.
 * The cache key itself however will remain and thus should cleaned up every no and then.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Hold key and object.
 * <li> Maintain and update the current writeLockValue.
 * </ul>
 * @since TOPLink/Java 1.0
 */
public class WeakCacheKey extends CacheKey {

    /** Reference is maintained weak to allow garbage collection */
    protected WeakReference reference;

    /**
     * Initialize the newly allocated instance of this class.
     * @param primaryKey contains values extracted from the object
     * @param writeLockValue is the write lock value, null if optimistic locking not being used for this object.
     * @param readTime the time TopLInk read the cache key
     */
    public WeakCacheKey(Vector primaryKey, Object object, Object writeLockValue, long readTime) {
        super(primaryKey, object, writeLockValue, readTime);
    }

    public Object getObject() {
        return getReference().get();
    }

    public WeakReference getReference() {
        return reference;
    }

    public void setObject(Object object) {
        setReference(new WeakReference(object));
    }

    protected void setReference(WeakReference reference) {
        this.reference = reference;
    }
}
