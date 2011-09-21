/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.Assert.*;

import org.glassfish.hk2.Bindings;
import org.glassfish.hk2.DynamicBinderFactory;
import org.glassfish.hk2.tests.perthread.PerThreadService;
import org.glassfish.hk2.tests.perthread.SomeContract;
import org.junit.Test;

/**
 * Tests for DynamicBinderFactoryImpl
 * 
 * @author Jeff Trent, Tom Beerbower
 */
public class DynamicBinderFactoryImplTest {

    @Test
    public void commitAndRelease() throws Exception {
        Habitat habitat = new Habitat();
        assertNull(habitat.getComponent(SomeContract.class));
        assertNull(habitat.getComponent(MyContract.class));

        DynamicBinderFactory dbf = new DynamicBinderFactoryImpl(null, habitat);
        dbf.bind(SomeContract.class).to(PerThreadService.class);
        dbf.bind(MyContract.class).to(MyService.class);

        assertNull(habitat.getComponent(SomeContract.class));
        assertNull(habitat.getComponent(MyContract.class));
        
        Bindings bindings = dbf.commit();
        assertNotNull(habitat.getComponent(SomeContract.class));
        assertNotNull(habitat.getComponent(MyContract.class));

        assertSame(habitat.getComponent(SomeContract.class), habitat.getComponent(SomeContract.class));
        assertSame(habitat.getComponent(MyContract.class), habitat.getComponent(MyContract.class));

        assertNotNull(bindings);
        assertTrue(bindings.isActive());
        
        bindings.release();
        assertFalse(bindings.isActive());
        assertNull(habitat.getComponent(SomeContract.class));
        assertNull(habitat.getComponent(MyContract.class));
        
        // extraneous release
        try {
            bindings.release();
            fail();
        } catch (IllegalStateException e) {
            // expected
        }
    }
    
    
    public static interface MyContract {
        
    }
    
    public static class MyService implements MyContract {
        
    }
    
}
