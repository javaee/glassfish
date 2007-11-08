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
package oracle.toplink.essentials.config;

/**
 * FlushClearCache persistence property
 * defines modes of cache handling after em.flush call followed by em.clear call.
 * This property could be specified while creating either EntityManagerFactory 
 * (createEntityManagerFactory or persistence.xml)
 * or EntityManager (createEntityManager); the latter overrides the former.
 * 
 * <p>JPA persistence property Usage:
 * 
 * <p><code>properties.add(TopLinkProperties.FLUSH_CLEAR_CACHE, FlushClearCache.Drop);</code>
 * 
 * <p>Values are case-insensitive.
 * "" could be used instead of default value FlushClearCache.DEFAULT.
 */
public class FlushClearCache {
    /** Call to clear method causes to drop from EntityManager cache only the objects that haven't been flushed.
     * This is the most accurate mode: shared cache is perfect after commit; 
     * but the least memory effective: smbd repeatedly using flush followed by clear
     * may eventually run out of memory in a huge transaction.
     */
    public static final String  Merge = "Merge";
    
    /** Call to clear method causes to drop the whole EntityManager cache.
     * This is the fasteset and using the least memory mode - 
     * but after commit the shared cache potentially contains stale data.
     */
    public static final String  Drop = "Drop";

    /** Call to clear method causes to drops the whole EntityManager cache, 
     * on commit the classes that have at least one object updated or deleted 
     * are invalidated in the shared cache.
     * This is a compromise mode: potentially a bit slower than drop,
     * but has virtually the same memory efficiency.
     * After commit all potentially stale data is invalidated in the shared cache.
     */
    public static final String  DropInvalidate = "DropInvalidate";
 
    public static final String DEFAULT = DropInvalidate;
}
