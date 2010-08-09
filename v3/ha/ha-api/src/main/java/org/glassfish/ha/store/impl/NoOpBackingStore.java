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

package org.glassfish.ha.store.impl;

import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreConfiguration;
import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.ha.store.api.Storeable;

import java.io.Serializable;
import java.util.Properties;

/**
 * @author Mahesh Kannan
 */
public class NoOpBackingStore<K extends Serializable, V extends Serializable>
    extends BackingStore<K, V> {

    private String myName;

    NoOpBackingStore() {

    }

    @Override
    protected void initialize(BackingStoreConfiguration<K, V> conf)
        throws BackingStoreException {
        super.initialize(conf);

        myName = conf == null ? null : conf.getInstanceName();
    }

    @Override
    public V load(K key, String version) throws BackingStoreException {
        return null;
    }
    public V load(K key, Long version) throws BackingStoreException {
        return null;
    }

    @Override
    public String save(K key, V value, boolean isNew) throws BackingStoreException {
        return null;
    }

    @Override
    public void remove(K key) throws BackingStoreException {

    }

    @Override
    public void updateTimestamp(K key, long time) throws BackingStoreException {

    }

    @Override
    public String updateTimestamp(K key, Long version, Long accessTime, Long maxIdleTime) throws BackingStoreException {
        return myName;
    }

    @Override
    public int removeExpired(long idleForMillis) throws BackingStoreException {
        return 0;
    }

    @Override
    public int size() throws BackingStoreException {
        return 0;
    }

    @Override
    public void destroy() throws BackingStoreException {
        
    }
}
