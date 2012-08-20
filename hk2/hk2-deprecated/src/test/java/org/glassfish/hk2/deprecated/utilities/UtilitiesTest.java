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

package org.glassfish.hk2.deprecated.utilities;


import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Test;

import javax.inject.Singleton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Utilities tests.
 *
 * @author tbeerbower
 */
public class UtilitiesTest {

    @Test
    public void testAddIndex() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("UtilitiesTest:testAddIndex");
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        ActiveDescriptor<MyService> descriptor =
                (ActiveDescriptor<MyService>)config.<MyService>bind(BuilderHelper.link(MyService.class).
                        to(MyInterface1.class).in(Singleton.class.getName()).build());

        config.commit();

        MyService s1 = locator.getService(MyService.class);
        assertNotNull(s1);

        assertEquals(s1, locator.getService(MyInterface1.class));

        Utilities.addIndex(locator, descriptor, MyInterface2.class.getName(), "foo");
        Utilities.addIndex(locator, descriptor, MyInterface3.class.getName(), "bar");

        MyInterface2 s2 = locator.getService(MyInterface2.class, "foo");

        assertEquals(s1, s2);

        MyInterface3 s3 = locator.getService(MyInterface3.class, "bar");

        assertEquals(s1, s3);

        assertNull(locator.getService(MyInterface1.class, "foo"));
        assertNull(locator.getService(MyInterface1.class, "bar"));

        assertNull(locator.getService(MyInterface2.class, ""));
        assertNull(locator.getService(MyInterface2.class, "bar"));

        assertNull(locator.getService(MyInterface3.class, ""));
        assertNull(locator.getService(MyInterface3.class, "foo"));
    }
}
