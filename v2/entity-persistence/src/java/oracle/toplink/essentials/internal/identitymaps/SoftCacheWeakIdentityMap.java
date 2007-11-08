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

/**
 * <p><b>Purpose</b>: A weak cache is identical to the weak identity map, however the weak
 * can be a performance problem for some types of apps because it can cause too much garbage collection
 * of objects read causing them to be re-read and re-built (this defeats the purpose of the cache).
 * The weak cache solves this through also holding a fixed number of objects is memory to improve caching.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Guarantees identity
 * <li> Allows garbage collection
 * <li> Increases performance through maintaining a fixed size cache of MRU objects when memory is available
 * <li> The default size of the reference cache is half the max size
 * </ul>
 * @since TOPLink/Java 1.2
 */
public class SoftCacheWeakIdentityMap extends HardCacheWeakIdentityMap {
    public SoftCacheWeakIdentityMap(int size) {
        super(size);
    }

    /**
     * Creates a Soft reference if Required
     * @param object is the domain object to cache.
     */
    public Object buildReference(Object object) {
        return new SoftReference(object);
    }

    /**
     * Checks if the object is null, or reference's object is null.
     * @param the object for hard or the reference for soft.
     */
    public boolean hasReference(Object reference) {
        return (reference != null) && (((SoftReference)reference).get() != null);
    }
}
