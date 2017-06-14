/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.admin.rest.composite;

import org.glassfish.admin.rest.composite.metadata.RestModelMetadata;
import java.util.Map;
import java.util.Set;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author jdlee
 */
public class RestCollectionTest {
    private RestCollection<TestModel> rc;

    @BeforeMethod(alwaysRun=true)
    public void setUp() {
        rc = new RestCollection();
    }

    @Test
    public void testAdd() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);
        assertEquals(1, rc.size());
        assertFalse(rc.isEmpty());
    }

    @Test
    public void testGet() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);

        RestModel<TestModel> rm = rc.get("1");
        assertEquals(tm, rm);
    }

    @Test
    public void testRemove() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);
        assertEquals(1, rc.size());
        assertFalse(rc.isEmpty());
        rc.remove("1");
        assertEquals(0, rc.size());
        assertTrue(rc.isEmpty());
    }

    @Test
    public void testContainsKey() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);
        assertEquals(1, rc.size());
        assertFalse(rc.isEmpty());
        assertTrue(rc.containsKey("1"));
    }

    @Test
    public void testContainsValue() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);
        assertEquals(1, rc.size());
        assertTrue(rc.containsValue(tm));
    }

    @Test
    public void testClear() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);
        assertEquals(1, rc.size());
        rc.clear();
        assertEquals(0, rc.size());
        assertTrue(rc.isEmpty());
    }

    @Test
    public void testGetKeySet() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);
        assertTrue(rc.keySet().contains(new RestModelMetadata("1")));
    }

    @Test
    public void testGetValues() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);
        assertEquals(1, rc.values().size());
    }

    @Test
    public void testEntrySet()  throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);
        tm.setName("one");
        rc.put("1", tm);
        tm = CompositeUtil.instance().getModel(TestModel.class);
        tm.setName("two");
        rc.put("2", tm);

        Set<Map.Entry<RestModelMetadata, TestModel>> entries = rc.entrySet();
        assertEquals(2, entries.size());
        // Test contents...
    }

    public interface TestModel extends RestModel {
        public String getName();
        public void setName(String name);
    }
}
