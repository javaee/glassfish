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

package org.glassfish.ha.api.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.glassfish.ha.store.api.*;
import org.glassfish.ha.store.spi.BackingStoreFactoryRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Unit test for simple App.
 */
public class BackingStoreFactoryRegistryTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public BackingStoreFactoryRegistryTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(BackingStoreFactoryRegistryTest.class);
    }


    public void testBackingStoreFactoryRegistry() {
        boolean result = false;
        try {
            BackingStoreFactory nbsf = BackingStoreFactoryRegistry.getFactoryInstance("noop");
            result = true;
        } catch (BackingStoreException bsEx) {

        }

        assert(result);
    }

    public void testBackingStore() {
        boolean result = false;
        try {
            BackingStoreFactory nbsf = BackingStoreFactoryRegistry.getFactoryInstance("noop");
            BackingStoreConfiguration<String, NoopData> conf = null;
            BackingStore<String, NoopData> bs = nbsf.createBackingStore(conf);

            result = true;
        } catch (BackingStoreException bsEx) {

        }
    }

    public void testBackingStoreSave() {
        boolean result = false;
        try {
            BackingStoreFactory nbsf = BackingStoreFactoryRegistry.getFactoryInstance("noop");
            BackingStoreConfiguration<String, NoopData> conf = null;
            BackingStore<String, NoopData> bs = nbsf.createBackingStore(conf);

            bs.save("k1", null, true);
            bs.save("k1", new NoopData(), true);
            bs.save("k1", null, false);
            bs.save("k1", null, true);

            bs.load(null,null);
            bs.load(null, "6");
            bs.load("k1", null);
            bs.load("k1", "6");


            bs.remove(null);
            bs.remove("k1");


            bs.updateTimestamp(null, 6);
            bs.updateTimestamp("k1", 6);


            bs.removeExpired(6);

            result = true;
        } catch (BackingStoreException bsEx) {

        }

        assert(result);
    }

    public void testBackingStoreLoad() {
        boolean result = false;
        try {
            BackingStoreFactory nbsf = BackingStoreFactoryRegistry.getFactoryInstance("noop");
            BackingStoreConfiguration<String, NoopData> conf = null;
            BackingStore<String, NoopData> bs = nbsf.createBackingStore(conf);

            bs.load(null,null);
            bs.load(null, "6");
            bs.load("k1", null);
            bs.load("k1", "6");

            result = true;
        } catch (BackingStoreException bsEx) {

        }

        assert(result);
    }
    public void testBackingStoreRemove() {
        boolean result = false;
        try {
            BackingStoreFactory nbsf = BackingStoreFactoryRegistry.getFactoryInstance("noop");
            BackingStoreConfiguration<String, NoopData> conf = null;
            BackingStore<String, NoopData> bs = nbsf.createBackingStore(conf);

            bs.remove(null);
            bs.remove("k1");

            result = true;
        } catch (BackingStoreException bsEx) {

        }

        assert(result);
    }
    public void testBackingStoreUpdateTimestamp() {
        boolean result = false;
        try {
            BackingStoreFactory nbsf = BackingStoreFactoryRegistry.getFactoryInstance("noop");
            BackingStoreConfiguration<String, NoopData> conf = null;
            BackingStore<String, NoopData> bs = nbsf.createBackingStore(conf);

            bs.updateTimestamp(null, 6);
            bs.updateTimestamp("k1", 6);

            result = true;
        } catch (BackingStoreException bsEx) {

        }

        assert(result);
    }
    public void testBackingStoreRemoveExpired() {
        boolean result = false;
        try {
            BackingStoreFactory nbsf = BackingStoreFactoryRegistry.getFactoryInstance("noop");
            BackingStoreConfiguration<String, NoopData> conf = null;
            BackingStore<String, NoopData> bs = nbsf.createBackingStore(conf);

            bs.removeExpired(6);

            result = true;
        } catch (BackingStoreException bsEx) {

        }

        assert(result);
    }

    private static final class NoopData
        implements Storeable {

        @Override
        public long _storeable_getVersion() {
            return 0;
        }

        @Override
        public void _storeable_setVersion(long version) {
        }

        @Override
        public long _storeable_getLastAccessTime() {
            return 0;
        }

        @Override
        public void _storeable_setLastAccessTime(long version) {
        }

        @Override
        public long _storeable_getMaxIdleTime() {
            return 0;
        }

        @Override
        public void _storeable_setMaxIdleTime(long version) {
        }

        @Override
        public String[] _storeable_getAttributeNames() {
            return new String[0];
        }

        @Override
        public boolean[] _storeable_getDirtyStatus() {
            return new boolean[0];
        }

        @Override
        public void _storeable_writeState(OutputStream os) throws IOException {
        }

        @Override
        public void _storeable_readState(InputStream is) throws IOException {
        }
    }


}