/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.ha.store.spi;

/**
 * A class that saves a set of EjbMetaData atomically. The implementation is
 * responsible for atomically saving all the <code>BatchMetadata</code> that
 * is passed as parameter to the <code>saveAll()</code> method.
 *
 * @author Mahesh.Kannan@Sun.Com
 * @author Larry.White@Sun.Com
 */
public interface BatchBackingStore<T> {

    /**
     * Save all the BatchMetadata atomically.
     * <p>
     * Note that not all <code>BatchMetadata.getAppId()</code> in the
     * collection may return the same value. The store is expected to save all
     * of them atomically
     * 
     * @param data
     *            the BatchMetadata to be stored
     * @throws BackingStoreException
     * @throws BackingStoreException
     *             if the underlying store implementation encounters any
     *             exception
     */
    public void saveAll(T... data) throws BackingStoreException;

    /**
     * Save the entry. Overwrites entry if it already exists.
     *
     * @param storeName the Store name
     * @param key the key of the entry
     * @param entry the StoreEntry
     * @param isNew true if the entry is new false if the entry has already been persisted.
     *
     * @throws IllegalArgumentException if entry is not a
     *                                  StoreEntry
     */
    public abstract void save(String storeName, Object key, T entry, boolean isNew);

    /**
     * Commit all the batch operations as (possibly) a single unit. This
     * BatchManager must not be used after a commit.
     *
     * The BatchManager may also implement the save as a write through method.
     * In which case, commit will be a no-op.
     */
    public abstract void commit();
}
