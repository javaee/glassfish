/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test.naked;

import java.net.URL;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.basic.beans.Commons;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class NakedTest {
    private final static String NAKED_FILE = "naked.xml";
    
    /**
     * Tests that children with no XmlElement
     * attribute will work properly
     */
    @Test // @org.junit.Ignore
    public void testChildrenWithNoAnnotation() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(NAKED_FILE);
        
        XmlRootHandle<ParentBean> rootHandle = xmlService.unmarshall(url.toURI(), ParentBean.class);
        
        ParentBean parentBean = rootHandle.getRoot();
        Assert.assertNotNull(parentBean);
        
        // Lets check the data
        for (int lcv = 0; lcv < 2; lcv++) {
            ChildOne child = parentBean.getOne().get(lcv);
            
            switch(lcv) {
            case 0 : 
                Assert.assertEquals("d1", child.getData());
                break;
            case 1:
                Assert.assertEquals("d2", child.getData());
                break;
            default:
                Assert.fail();
            }
                    
        }
        
        for (int lcv = 0; lcv < 3; lcv++) {
            ChildTwo child = parentBean.getTwo().get(lcv);
            
            switch(lcv) {
            case 0 : 
                Assert.assertEquals(Commons.ALICE, child.getName());
                break;
            case 1:
                Assert.assertEquals(Commons.BOB, child.getName());
                break;
            case 2:
                Assert.assertEquals(Commons.CAROL, child.getName());
                break;
            default:
                Assert.fail();
            }
                    
        }
        
        for (int lcv = 0; lcv < 1; lcv++) {
            ChildOne child3 = parentBean.getThree()[lcv];
            ChildTwo child4 = parentBean.getFour()[lcv];
            
            switch(lcv) {
            case 0 : 
                Assert.assertEquals("d3", child3.getData());
                Assert.assertEquals(Commons.DAVE, child4.getName());
                break;
            default:
                Assert.fail();
            }
                    
        }
        
        Assert.assertEquals("d4", parentBean.getFive().getData());
        Assert.assertEquals(Commons.ENGLEBERT, parentBean.getFive().getName());
        
        Assert.assertEquals("d5", parentBean.getSix().getData());
        Assert.assertEquals(Commons.FRANK, parentBean.getSix().getName());
    }

}
