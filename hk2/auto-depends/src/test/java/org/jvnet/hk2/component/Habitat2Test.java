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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.glassfish.hk2.Binding;
import org.glassfish.hk2.Descriptor;
import org.junit.Test;

/**
 * More Habitat tests, this time based on Hk2Runner and JUnit 4+
 * 
 */
//@RunWith(Hk2Runner.class)
public class Habitat2Test {
    TestHabitat h = new TestHabitat();

    /**
     * get*Bindings() related tests
     */
    
    /**
     * Try getting them all
     */
    @Test
    public void testGetDeclaredBindings() {
        Collection<Binding<?>> bindings = h.getDeclaredBindings();
        assertNotNull(bindings);
        System.out.println(bindings);
        assertTrue(bindings.toString(), bindings.size() > 0);
    }
    
    /**
     * Try getting them by type
     */
    @Test
    public void testBindingsByType() {
        Descriptor descriptor = new DescriptorImpl(null, "java.util.concurrent.ExecutorService");
        Collection<Binding<?>> bindings = h.getBindings(descriptor);
        assertNotNull(bindings);
        assertEquals(1, bindings.size());

        descriptor = new DescriptorImpl(null, "bogus");
        bindings = h.getBindings(descriptor);
        assertNotNull(bindings);
        assertEquals(0, bindings.size());
    }
    
    /**
     * Try getting them by name
     */
    @Test
    public void testGetBindingsByName() {
        Descriptor descriptor = new DescriptorImpl("habitat-listeners", null);
        Collection<Binding<?>> bindings = h.getBindings(descriptor);
        assertNotNull(bindings);
        assertEquals(1, bindings.size());

        descriptor = new DescriptorImpl("bogus", null);
        bindings = h.getBindings(descriptor);
        assertNotNull(bindings);
        assertEquals(0, bindings.size());
    }

    /**
     * Try getting them by contract(s)
     */
    @Test
    public void testGetBindingsByContract() {
        DescriptorImpl descriptor = new DescriptorImpl(null, null);
        descriptor.addContract("java.util.concurrent.ExecutorService");
        Collection<Binding<?>> bindings = h.getBindings(descriptor);
        assertNotNull(bindings);
        assertEquals(1, bindings.size());

        descriptor = new DescriptorImpl(null, null);
        descriptor.addContract("java.util.concurrent.ExecutorService");
        bindings = h.getBindings(descriptor);
        assertNotNull(bindings);
        assertEquals(1, bindings.size());

        descriptor = new DescriptorImpl(null, null);
        descriptor.addContract("java.util.concurrent.ExecutorService");
        descriptor.addContract("bogus");
        bindings = h.getBindings(descriptor);
        assertNotNull(bindings);
        assertEquals(0, bindings.size());
    }

    /**
     * Try getting them by name and contract(s)
     */
    @Test
    public void testBindingsByNameAndContract() {
        DescriptorImpl descriptor = new DescriptorImpl("habitat-listeners", null);
        descriptor.addContract("java.util.concurrent.ExecutorService");
        Collection<Binding<?>> bindings = h.getBindings(descriptor);
        assertNotNull(bindings);
        assertEquals(1, bindings.size());

        descriptor = new DescriptorImpl("habitat-listeners", null);
        descriptor.addContract("java.util.concurrent.ExecutorService");
        descriptor.addContract("bogus");
        bindings = h.getBindings(descriptor);
        assertNotNull(bindings);
        assertEquals(0, bindings.size());

        descriptor = new DescriptorImpl("bogus", null);
        descriptor.addContract("java.util.concurrent.ExecutorService");
        bindings = h.getBindings(descriptor);
        assertNotNull(bindings);
        assertEquals(0, bindings.size());
}
}
