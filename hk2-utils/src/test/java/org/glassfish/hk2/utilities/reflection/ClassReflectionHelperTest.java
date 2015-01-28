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
package org.glassfish.hk2.utilities.reflection;

import java.util.Set;

import org.glassfish.hk2.utilities.reflection.internal.ClassReflectionHelperImpl;
import org.glassfish.hk2.utilities.reflection.types2.BaseInterface;
import org.glassfish.hk2.utilities.reflection.types2.ServiceInterface;
import org.glassfish.hk2.utilities.reflection.types2.ServiceInterface2;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ClassReflectionHelperTest {
    /**
     * Tests we get all the methods from an interface
     */
    @Test
    public void testInterface() {
        ClassReflectionHelper helper = new ClassReflectionHelperImpl();
        
        Set<MethodWrapper> wrappers = helper.getAllMethods(OverallInterface.class);
        Assert.assertEquals(4, wrappers.size());
        
        boolean foundName = false;
        boolean foundOne = false;
        boolean foundTwo = false;
        boolean foundThree = false;
        for (MethodWrapper wrapper : wrappers) {
            String name = wrapper.getMethod().getName();
            if ("getName".equals(name)) foundName = true;
            else if ("fromBaseInterfaceOne".equals(name)) foundOne = true;
            else if ("fromBaseInterfaceTwo".equals(name)) foundTwo = true;
            else if ("fromBaseInterfaceThree".equals(name)) foundThree = true;
            else {
                Assert.fail("Uknown method name=" + name);
            }
        }
        
        Assert.assertTrue(foundName);
        Assert.assertTrue(foundOne);
        Assert.assertTrue(foundTwo);
        Assert.assertTrue(foundThree);
        
    }
    
    /**
     * Tests that an interface extending another works
     */
    @Test
    public void testInterfaceExtendsAnotherInterface() {
        ClassReflectionHelper helper = new ClassReflectionHelperImpl();
        
        Set<MethodWrapper> wrappers = helper.getAllMethods(ServiceInterface2.class);
        Assert.assertEquals(3, wrappers.size());
        
        boolean foundBase = false;
        boolean foundService = false;
        boolean foundService2 = false;
        for (MethodWrapper wrapper : wrappers) {
            String name = wrapper.getMethod().getName();
            if ("fromServiceInterface".equals(name)) foundService = true;
            else if ("fromBaseInterface".equals(name)) foundBase = true;
            else if ("fromServiceInterface2".equals(name)) foundService2 = true;
            else {
                Assert.fail("Uknown method name=" + name);
            }
        }
        
        Assert.assertTrue(foundBase);
        Assert.assertTrue(foundService);
        Assert.assertTrue(foundService2);
    }

}
