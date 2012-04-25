/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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


package org.jvnet.hk2.component;


import com.sun.hk2.component.LazyInhabitant;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.deprecated.utilities.MyInterface1;
import org.glassfish.hk2.deprecated.utilities.MyInterface2;
import org.glassfish.hk2.deprecated.utilities.MyInterface3;
import org.glassfish.hk2.deprecated.utilities.MyService;
import org.junit.Test;
import org.jvnet.hk2.deprecated.internal.HolderHK2LoaderImpl;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;


/**
 * Habitat tests.
 *
 * @author tbeerbower
 */
public class HabitatTest {

    // ----- Constants ------------------------------------------------------

    private static final LinkedHashMap<String, List<String>> EMPTY_METADATA =
            new LinkedHashMap<String, List<String>>();


    // ----- Tests ----------------------------------------------------------

    @Test
    public void testAdd() {
        Habitat habitat = new Habitat(null, "testAdd");
        HK2Loader loader = new HolderHK2LoaderImpl(null);

        assertNull(habitat.getService(MyService.class));

        Inhabitant<MyService> inhabitant =
                new LazyInhabitant<MyService>(habitat, loader, MyService.class.getName(), EMPTY_METADATA);

        habitat.add(inhabitant);

        MyService service = habitat.getService(MyService.class);
        assertNotNull(service);
    }

    @Test
    public void testAddIndex() {
        Habitat habitat = new Habitat(null, "testAddIndex");
        HK2Loader loader = new HolderHK2LoaderImpl(null);

        assertNull(habitat.getService(MyService.class));

        Inhabitant<MyService> inhabitant =
                new LazyInhabitant<MyService>(habitat, loader, MyService.class.getName(), EMPTY_METADATA);

        habitat.add(inhabitant);

        MyService s1 = habitat.getService(MyService.class);
        assertNotNull(s1);

        MyInterface1 s2 = habitat.getService(MyInterface2.class, "foo");
        assertNull(s2);

        habitat.addIndex(inhabitant, MyInterface2.class.getName(), "foo");

        s2 = habitat.getService(MyInterface2.class, "foo");
        assertSame(s1, s2);

        MyInterface1 s3 = habitat.getService(MyInterface3.class, "bar");
        assertNull(s3);

        habitat.addIndex(inhabitant, MyInterface3.class.getName(), "bar");

        s3 = habitat.getService(MyInterface3.class, "bar");
        assertSame(s1, s3);

        assertNull(habitat.getService(MyInterface1.class, "foo"));
        assertNull(habitat.getService(MyInterface1.class, "bar"));

        assertNull(habitat.getService(MyInterface2.class, ""));
        assertNull(habitat.getService(MyInterface2.class, "bar"));

        assertNull(habitat.getService(MyInterface3.class, ""));
        assertNull(habitat.getService(MyInterface3.class, "foo"));
    }

    @Test
    public void testRemove() {
        Habitat habitat = new Habitat(null, "testRemove");
        HK2Loader loader = new HolderHK2LoaderImpl(null);

        assertNull(habitat.getService(MyService.class));

        Inhabitant<MyService> inhabitant =
                new LazyInhabitant<MyService>(habitat, loader, MyService.class.getName(), EMPTY_METADATA);

        habitat.add(inhabitant);

        MyService service = habitat.getService(MyService.class);
        assertNotNull(service);

        habitat.remove(inhabitant);

        assertNull(habitat.getService(MyService.class));
    }

    @Test
    public void testRemoveIndex() {
        Habitat habitat = new Habitat(null, "testRemoveIndex");
        HK2Loader loader = new HolderHK2LoaderImpl(null);

        assertNull(habitat.getService(MyService.class));

        Inhabitant<MyService> inhabitant =
                new LazyInhabitant<MyService>(habitat, loader, MyService.class.getName(), EMPTY_METADATA);

        habitat.add(inhabitant);

        MyService s1 = habitat.getService(MyService.class);
        assertNotNull(s1);

        MyInterface1 s2 = habitat.getService(MyInterface2.class, "foo");
        assertNull(s2);

        habitat.addIndex(inhabitant, MyInterface2.class.getName(), "foo");

        s2 = habitat.getService(MyInterface2.class, "foo");
        assertSame(s1, s2);

        habitat.removeIndex(MyInterface2.class.getName(), "foo");

        s2 = habitat.getService(MyInterface2.class, "foo");
        assertNull(s2);
    }

    @Test
    public void testRemoveIndexWithService() {
        Habitat habitat = new Habitat(null, "testRemoveIndexWithService");
        HK2Loader loader = new HolderHK2LoaderImpl(null);

        assertNull(habitat.getService(MyService.class));

        Inhabitant<MyService> inhabitant =
                new LazyInhabitant<MyService>(habitat, loader, MyService.class.getName(), EMPTY_METADATA);

        habitat.add(inhabitant);

        MyService s1 = habitat.getService(MyService.class);
        assertNotNull(s1);

        MyInterface1 s2 = habitat.getService(MyInterface2.class, "foo");
        assertNull(s2);

        habitat.addIndex(inhabitant, MyInterface2.class.getName(), "foo");

        s2 = habitat.getService(MyInterface2.class, "foo");
        assertSame(s1, s2);

        habitat.removeIndex(MyInterface2.class.getName(), s1);

        s2 = habitat.getService(MyInterface2.class, "foo");
        assertNull(s2);
    }

    @Test
    public void testRemoveIndexWithInhabitant() {
        Habitat habitat = new Habitat(null, "testRemoveIndexWithInhabitant");
        HK2Loader loader = new HolderHK2LoaderImpl(null);

        assertNull(habitat.getService(MyService.class));

        Inhabitant<MyService> inhabitant =
                new LazyInhabitant<MyService>(habitat, loader, MyService.class.getName(), EMPTY_METADATA);

        habitat.add(inhabitant);

        MyService s1 = habitat.getService(MyService.class);
        assertNotNull(s1);

        MyInterface1 s2 = habitat.getService(MyInterface2.class, "foo");
        assertNull(s2);

        habitat.addIndex(inhabitant, MyInterface2.class.getName(), "foo");

        s2 = habitat.getService(MyInterface2.class, "foo");
        assertSame(s1, s2);

        habitat.removeIndex(MyInterface2.class.getName(), inhabitant);

        s2 = habitat.getService(MyInterface2.class, "foo");
        assertNull(s2);
    }
}
