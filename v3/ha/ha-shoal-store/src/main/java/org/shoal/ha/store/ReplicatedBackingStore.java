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

package org.shoal.ha.store;

import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreConfiguration;
import org.glassfish.ha.store.api.BackingStoreException;
import org.shoal.ha.cache.api.*;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Mahesh Kannan
 */
public class ReplicatedBackingStore<K extends Serializable, V extends Serializable>
        extends BackingStore<K, V> {

    DataStore<K, V> dataStore;

    @Override
    protected void initialize(BackingStoreConfiguration<K, V> conf)
            throws BackingStoreException {
        super.initialize(conf);
        DataStoreConfigurator<K, V> dsConf = new DataStoreConfigurator<K, V>();
        dsConf.setInstanceName(conf.getInstanceName())
                .setGroupName(conf.getClusterName())
                .setStoreName(conf.getStoreName())
                .setKeyClazz(conf.getKeyClazz())
                .setValueClazz(conf.getValueClazz());
        Map<String, Object> vendorSpecificMap = conf.getVendorSpecificSettings();

        Object stGMS = vendorSpecificMap.get("start.gms");
        boolean startGMS = false;
        if (stGMS != null) {
            if (stGMS instanceof String) {
                try {
                    startGMS = Boolean.valueOf((String) stGMS);
                } catch (Throwable th) {
                    //Ignore
                }
            } else if (stGMS instanceof Boolean) {
                startGMS = (Boolean) stGMS;
            }
        }

        Object cacheLocally = vendorSpecificMap.get("local.caching");;
        boolean enableLocalCaching = false;
        if (cacheLocally != null) {
            if (cacheLocally instanceof String) {
                try {
                    enableLocalCaching = Boolean.valueOf((String) cacheLocally);
                } catch (Throwable th) {
                    //Ignore
                }
            } else if (cacheLocally instanceof Boolean) {
                enableLocalCaching = (Boolean) stGMS;
            }
        }

        ClassLoader cl = (ClassLoader) vendorSpecificMap.get("class.loader");
        if (cl == null) {
            cl = conf.getValueClazz().getClassLoader();
        }
        dsConf.setClassLoader(cl)
                .setStartGMS(startGMS)
                .setCacheLocally(enableLocalCaching);

        dsConf.setObjectInputOutputStreamFactory(new DefaultObjectInputOutputStreamFactory());
        dataStore = DataStoreFactory.createDataStore(dsConf);
    }

    @Override
    public V load(K key, String version) throws BackingStoreException {
        try {
            return dataStore.get(key);
        } catch (DataStoreException dsEx) {
            throw new BackingStoreException("Error during load: " + key, dsEx);
        }
    }

    @Override
    public String save(K key, V value, boolean isNew) throws BackingStoreException {
        try {
            return dataStore.put(key, value);
        } catch (DataStoreException dsEx) {
            throw new BackingStoreException("Error during save: " + key, dsEx);
        }
    }

    @Override
    public void remove(K key) throws BackingStoreException {
        try {
            dataStore.remove(key);
        } catch (DataStoreException dsEx) {
            throw new BackingStoreException("Error during remove: " + key, dsEx);
        }
    }

    @Override
    public int removeExpired(long idleTime) throws BackingStoreException {
        return dataStore.removeIdleEntries(idleTime);
    }

    @Override
    public int size() throws BackingStoreException {
        return 0;
    }

    @Override
    public void destroy() throws BackingStoreException {
        dataStore.close();
    }

    @Override
    public void updateTimestamp(K key, long time) throws BackingStoreException {
        try {
            dataStore.touch(key, Long.MAX_VALUE - 1, time, 30 * 60 * 1000);
        } catch (DataStoreException dsEx) {
            throw new BackingStoreException("Error during load: " + key, dsEx);
        }
    }

}