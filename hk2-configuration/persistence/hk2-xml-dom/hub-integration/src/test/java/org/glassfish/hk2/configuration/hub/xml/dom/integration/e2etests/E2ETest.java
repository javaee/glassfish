/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.configuration.hub.xml.dom.integration.e2etests;

import java.beans.PropertyVetoException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import org.glassfish.hk2.configuration.api.ChildIterable;
import org.glassfish.hk2.configuration.api.ConfigurationUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.testing.junit.HK2Runner;

/**
 * @author jwells
 *
 */
public class E2ETest extends HK2Runner {
    /* package */ final static String ABEAN_TAG = "/a-end-to-end-bean";
    /* package */ final static String BBEAN_TAG = "/b-end-to-end-bean";
    /* package */ final static String CBEAN_TAG = "/b-end-to-end-bean/c-end-to-end-bean";
    
    private final static String ALICE = "alice";
    private final static String BOB = "bob";
    private final static String ALICE_INSTANCE_NAME = "b-end-to-end-bean.alice";
    private final static String BOB_INSTANCE_NAME = "b-end-to-end-bean.bob";
    
    private final static String HELLO = "hello";
    
    private final static String FIRST_BOURBON = "Jack Daniels";
    private final static String SECOND_BOURBON = "Rebel Yell";
    
    @Before
    public void before() {
        super.initialize("E2ETest", null, null);
        
        ConfigurationUtilities.enableConfigurationSystem(testLocator);
    }
    
    /**
     * Tests just adding one bean then checking the
     * cooresponding service is available
     */
    @Test // @org.junit.Ignore
    public void testAddOneBean() {
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("simplee2e.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        AService aService = testLocator.getService(AService.class);
        Assert.assertNotNull(aService);
        
        Assert.assertEquals(HELLO, aService.getStringParameter());
        Assert.assertEquals(10, aService.getIntParameter());
        Assert.assertEquals(100, aService.getLongParameter());
    }
    
    /**
     * Tests just adding one bean then checking the
     * cooresponding service is available
     */
    @Test // @org.junit.Ignore
    public void testComplexKeyedBean() {
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex1e2e.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        BService bService = testLocator.getService(BService.class);
        Assert.assertNotNull(bService);
        
        Assert.assertEquals(HELLO, bService.getParameter());
        
        CService alice = testLocator.getService(CService.class, ALICE_INSTANCE_NAME);
        Assert.assertNotNull(alice);
        Assert.assertEquals(ALICE, alice.getName());
        
        CService bob = testLocator.getService(CService.class, BOB_INSTANCE_NAME);
        Assert.assertNotNull(bob);
        Assert.assertEquals(BOB, bob.getName());
        
        ChildIterable<CService> children = bService.getChildren();
        
        
        HashSet<String> names = new HashSet<String>();
        for (CService child : children) {
            names.add(child.getName());
        }
        
        Assert.assertTrue(names.contains(ALICE));
        Assert.assertTrue(names.contains(BOB));
        Assert.assertEquals(2, names.size());
    }
    
    /**
     * Tests that a dynamic change to a bean is reflected in the backing service
     * @throws TransactionFailure 
     */
    @Test // @org.junit.Ignore
    public void testDynamicChange() throws TransactionFailure {
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("simple2.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        IService iService = testLocator.getService(IService.class);
        Assert.assertNotNull(iService);
        
        Assert.assertEquals(FIRST_BOURBON, iService.getBourbon());
        
        IBean iBean = testLocator.getService(IBean.class);
        Assert.assertNotNull(iBean);
        
        ConfigSupport.apply(new SingleConfigCode<IBean>() {

            @Override
            public Object run(IBean param) throws PropertyVetoException,
                    TransactionFailure {
                param.setBourbon(SECOND_BOURBON);
                return null;
            }
            
        }, iBean);
        
        Assert.assertEquals(SECOND_BOURBON, iService.getBourbon());
        Assert.assertEquals(1, iService.getNumTimesSet());
    }

}
