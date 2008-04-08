/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.appserv.ha.spi;

import java.util.Properties;

/**
 * An object that stores a given Metadata against an id. This class defines the
 * set of operations that a container could perform on a store.
 * 
 * <p>
 * An instance of BackingStore is created by calling
 * <code>BackingStoreFactory.createSimpleStore()</code> method.
 * 
 * <p>
 * The BackingStore instance is created and used for storing data that belongs
 * to a single application or container.
 * 
 * <p>
 * The store implementation must be thread safe.
 */
public abstract class BackingStore<K extends Metadata> {

    private String appId;

    private Properties props;

    protected Properties getProperties() {
        return props;
    }

    protected String getAppId() {
        return this.appId;
    }

    protected void initialize(String appId, Properties props) {
        this.appId = appId;
        this.props = props;
    }

    /**
     * Load and return the daata for the given id. The store is expected to
     * return the largest ever version that was saved in the stored using the
     * <code>save()</code> method. Note that the return value could be null if
     * Metadata was not accessed for {@link Metadata#getMaxInactiveInterval()}
     * 
     * @param id
     *            the id whose value must be returned
     * @return the value if this store contains it or null. The implementation
     *         must return the exact same type as that was passed to it in the
     *         save method.
     * 
     * @throws NullPointerException
     *             if the id is null
     * @throws BackingStoreException
     *             if the underlying store implementation encounters any
     *             exception
     */
    public abstract K load(String id) throws BackingStoreException;

    /**
     * Save the value whose key is id.
     * 
     * @param id
     *            the id
     * @param value
     *            The Metadata to be stored
     * @throws BackingStoreException
     *             if the underlying store implementation encounters any
     *             exception
     */
    public abstract void save(String id, K value) throws BackingStoreException;

    /**
     * Update the last access time for this id.
     * 
     * @param id
     *            the id for the Metadata
     * @param time
     *            the time at which this data was last accessed
     * @param version
     *            the new version number for the data associated with this id. A
     *            newly created version will have version number zero.
     * @throws BackingStoreException
     *             if the underlying store implementation encounters any
     *             exception
     */
    public abstract void updateLastAccessTime(String id, long time, long version)
            throws BackingStoreException;

    /**
     * Remove the association for the id. After this call, any call to
     * <code>load(id)</code> <b>must</b> return null
     * 
     * @param id
     *            the id
     * @return true if the id was successfully removed false otherwise
     * @throws BackingStoreException
     *             if the underlying store implementation encounters any
     *             exception
     */
    public abstract void remove(String id) throws BackingStoreException;

    /**
     * Remove all instances that are idle. The implementation of this method
     * must use {@link Metadata#getLastAccessTime()} and
     * {@link Metadata#getMaxInactiveInterval()} to determine if a particular
     * Metadata is idle or not. A Metadata is considered idle if <code>
     * (Metadata.getMaxInactiveInterval > 0)
     * && ((System.currentTimeMillis() - Metadata.getLastAccessTime()) >=
     * Metadata.getMaxInactiveInterval())</code>
     * 
     * @return The number of (idle) sessions removed.
     * @throws BackingStoreException
     *             if the underlying store implementation encounters any
     *             exception
     */
    public abstract int removeExpired() throws BackingStoreException;

    /**
     * Get the current size of the store
     * 
     * @return the number of entries in the store
     * @throws BackingStoreException
     *             if the underlying store implementation encounters any
     *             exception
     */
    public abstract int size() throws BackingStoreException;

    /**
     * Called when the store is no longer needed. Must clean up and close any
     * opened resources.
     */
    public abstract void destroy() throws BackingStoreException;

}
