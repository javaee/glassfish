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
 * A factory for creating BackingStore(s). Every provider must provide an
 * implementation of this interface.
 * 
 * <p>
 * The <code>createBackingStore(env)</code> method is called typically during
 * container creation time. A store instance is typically used to store state
 * for a single container whose id is passed (as appId) to
 * <code>createStore()</code> method.
 * 
 * <p>
 * The <code>createBatchBackingStore()</code> method will be called whenever
 * the container decides to save a set of states that belong different
 * applications. Thus the store returned by createBatchBackingStore method is
 * different from createBackingStore in that the data passed in the collection
 * may potentially be from different containers/applications.
 * 
 * <p>
 * Any runtime exception thrown from createBackingStore and
 * createBatchBackingStore method will cause the container to use a default
 * persistence-type (typically no replication) and a log message will be logged
 * at WARNING level.
 * 
 * <p>
 * Both <code>BackingStore</code> and <code>BatchBackingStore</code> must be
 * thread safe.
 * 
 * @see BackingStoreRegistry
 * 
 */
public interface BackingStoreFactory {

    /**
     * This method is called to create a BackingStore that will store
     * <code>SimpleMetadata</code> or <code>CompositeMetadata</code>. This
     * class must be thread safe.
     * <p>
     * The factory must return a fully initialized and operational BackingStore
     * 
     * @param type
     *            The type of data that will be saved (using the
     *            <code>save()</code> method in BackingStore) in the store.
     * @param appId
     *            the application id for which this store is created
     * @param env
     *            Properties that contain any additional configuration paramters
     *            to successfully initialize and use the store.
     * @return a BackingStore. The returned BackingStore will be used only to
     *         store data of type K. The returned BackingStore must be thread
     *         safe.
     * @throws BackingStoreException
     *             If the store could not be created
     */
    public <K extends Metadata> BackingStore<K> createBackingStore(
            Class<K> type, String appId, Properties env)
            throws BackingStoreException;

    /**
     * This method is called to store a set of BatchMetadata objects atomically.
     * The factory must return a fully initialized and operational
     * BatchBackingStore
     * <p>
     * The factory must return a fully initialized and operational
     * BatchBackingStore
     * 
     * @param env
     *            Properties that contain any additional configuration paramters
     *            to successfully initialize and use the store.
     * @return A BatchBackingStore
     * @throws BackingStoreException
     *             If the store could not be created
     */
    public BatchBackingStore createBatchBackingStore(Properties env)
            throws BackingStoreException;

}
