/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.glassfish.hk2.Descriptor;
import org.junit.Test;

public class DescriptorImplTest {

    @Test
    public void sanity() {
        DescriptorImpl descriptor = new DescriptorImpl("name", "typeName");
        descriptor.addContract("contract1");
        descriptor.addContract("contract2");
        descriptor.addQualifierType("qualifier1");
        descriptor.addQualifierType("qualifier2");
        descriptor.addMetadata("key", "val1");
        descriptor.addMetadata("key", "val2");
        
        MultiMap<String, String> mm = new MultiMap<String, String>();
        mm.add("key", "val1");
        mm.add("key", "val2");
        
        assertEquals("name", descriptor.getName());
        assertEquals("typeName", descriptor.getTypeName());
        assertEquals(Arrays.asList(new String[] {"qualifier1", "qualifier2"}), descriptor.getQualifiers());
        assertEquals(Arrays.asList(new String[] {"contract1", "contract2"}), descriptor.getContracts());
        assertEquals(mm, descriptor.getMetadata());
        
        try {
            descriptor.getContracts().add("whatever");
            fail("expected to be read only");
        } catch (Exception e) {
            // expected
        }
        
        try {
            descriptor.getQualifiers().add("whatever");
            fail("expected to be read only");
        } catch (Exception e) {
            // expected
        }
        
        try {
            descriptor.getMetadata().add("key", "whatever");
            fail("expected to be read only");
        } catch (Exception e) {
            // expected
        }
        
        Descriptor copy = new DescriptorImpl(descriptor);
        assertEquals("name", copy.getName());
        assertEquals("typeName", copy.getTypeName());
        assertEquals(Arrays.asList(new String[] {"qualifier1", "qualifier2"}), copy.getQualifiers());
        assertEquals(Arrays.asList(new String[] {"contract1", "contract2"}), copy.getContracts());
        assertEquals(mm, copy.getMetadata());
    }
    
    @Test
    public void emptyDescriptor() {
        Descriptor descriptor = DescriptorImpl.EMPTY_DESCRIPTOR;
        assertNotNull(descriptor);
        assertTrue("need to ensure empty descriptor is read-only", descriptor.getClass().isAnonymousClass());
        
        DescriptorImpl descriptor2 = new DescriptorImpl(null, null);
        assertTrue("match checking", descriptor2.matches(descriptor));

        DescriptorImpl descriptor3 = new DescriptorImpl(descriptor2);
        assertTrue("match checking", descriptor3.matches(descriptor));
        assertTrue("match checking", descriptor3.matches(descriptor2));
        assertTrue("match checking", descriptor2.matches(descriptor3));
    }
    
    @Test
    public void matches() {
        DescriptorImpl descriptor = new DescriptorImpl("name", "typeName");
        descriptor.addContract("contract1");
        descriptor.addContract("contract2");
        descriptor.addQualifierType("qualifier1");
        descriptor.addQualifierType("qualifier2");
        descriptor.addMetadata("key", "val1");
        descriptor.addMetadata("key", "val2");
        
        DescriptorImpl copy = new DescriptorImpl(descriptor);
        assertTrue(descriptor.matches(copy));
        assertTrue(copy.matches(descriptor));
        
        copy.addContract("contract3");
        assertTrue(descriptor.matches(copy));
        assertFalse(copy.matches(descriptor));
        
        copy = new DescriptorImpl(descriptor);
        copy.addQualifierType("qualifier3");
        assertTrue(descriptor.matches(copy));
        assertFalse(copy.matches(descriptor));

        copy = new DescriptorImpl(descriptor);
        copy.addMetadata("key2", "val");
        assertTrue(descriptor.matches(copy));
        assertFalse(copy.matches(descriptor));
    }
    
}
