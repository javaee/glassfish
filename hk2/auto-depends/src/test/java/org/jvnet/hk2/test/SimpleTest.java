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
package org.jvnet.hk2.test;

import static junit.framework.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.junit.Hk2Runner;
import org.jvnet.hk2.test.contracts.Simple;
import org.jvnet.hk2.test.impl.RandomService;

/**
 * Tests basics of Hk2Runner with some simple Habitat injection points.
 *
 * @author Jerome Dochez
 */
@RunWith(Hk2Runner.class)
public class SimpleTest {

    @Inject
    RandomService myService; // injection by type versus by contract

    @Inject(name="one")
    Simple one;

    @Inject(name="other")
    Simple other;

    // This one is NOT annotated at field level, but is at method level
    Simple anotherSimple;
    
    @Inject(name="other")
    void setSimple(Simple simple) {
      anotherSimple = simple;
    }
    
    @Test
    public void byType() {
        assertTrue(myService!=null);
        assertEquals(myService.someMethod("bar"), "barbar");
    }

    @Test
    public void byName() {
        assertNotNull(one);
        assertNotNull(other);
        assertEquals(one.get(), "one");
        assertEquals(other.get(), "other");
    }

    @Ignore
    @Test
    public void ignored() {
        fail("should be ignored");
    }

    @Test
    public void setter() {
        assertTrue(anotherSimple!=null);
        assertEquals(anotherSimple.get(), "other");
    }
}
