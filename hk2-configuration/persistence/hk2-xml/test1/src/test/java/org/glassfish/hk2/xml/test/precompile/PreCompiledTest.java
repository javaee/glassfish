/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test.precompile;

import java.net.URL;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test1.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class PreCompiledTest {
    private final static String CLASS_ADD_ON_NAME = "_$$_Hk2_Jaxb";
    private final static String PRE_COMPILED_FILE = "pre-compiled.xml";
    private final static String ALICE = "Alice";
    private final static String BOB = "Bob";
    private final static String CAROL = "Carol";
    private final static String DAVE = "Dave";
    
    private Class<?> getAssociatedClass(Class<?> forInterface) {
        ClassLoader loader = getClass().getClassLoader();
        if (loader == null) loader = ClassLoader.getSystemClassLoader();
        Assert.assertNotNull(loader);
        
        String implClass = forInterface.getName() + CLASS_ADD_ON_NAME;
        
        try {
            return loader.loadClass(implClass);
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    /**
     * Checks to see that the pre-compiled class
     * is available prior to it getting generated,
     * and then that the system can see everything
     * properly
     */
    @Test @org.junit.Ignore
    public void testPreCompiledGotPreCompile() throws Exception {
        Assert.assertNull(getAssociatedClass(MultiChild.class));
        Assert.assertNull(getAssociatedClass(DirectChild.class));
        Assert.assertNotNull(getAssociatedClass(PreCompiledRoot.class));
        Assert.assertNotNull(getAssociatedClass(PreCompiledMultiChild.class));
        Assert.assertNotNull(getAssociatedClass(PreCompiledDirectChild.class));
        
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(PRE_COMPILED_FILE);
        
        XmlRootHandle<PreCompiledRoot> rootHandle = xmlService.unmarshall(url.toURI(), PreCompiledRoot.class);
        Assert.assertNotNull(rootHandle);
        
        PreCompiledRoot root = rootHandle.getRoot();
        
        Assert.assertEquals(ALICE, root.getPreCompiledMultiChild().get(0).getName());
        Assert.assertEquals("d1", root.getPreCompiledMultiChild().get(0).getData());
        
        Assert.assertEquals(BOB, root.getPreCompiledMultiChild().get(1).getName());
        Assert.assertEquals("d2", root.getPreCompiledMultiChild().get(1).getData());
        
        Assert.assertEquals(CAROL, root.getMultiChild().get(0).getName());
        Assert.assertEquals(1, root.getMultiChild().get(0).getFoo());
        
        Assert.assertEquals(DAVE, root.getMultiChild().get(1).getName());
        Assert.assertEquals(2, root.getMultiChild().get(1).getFoo());
        
        Assert.assertEquals(7001, root.getPreCompiledDirectChild().getPort());
        Assert.assertEquals("thirteen", root.getDirectChild().getIdentifier());
    }

}
