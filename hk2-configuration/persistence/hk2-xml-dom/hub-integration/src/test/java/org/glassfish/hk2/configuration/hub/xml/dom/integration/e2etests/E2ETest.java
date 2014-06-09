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

import java.net.URL;

import org.glassfish.hk2.configuration.api.ConfigurationUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.testing.junit.HK2Runner;

/**
 * @author jwells
 *
 */
public class E2ETest extends HK2Runner {
    /* package */ final static String ABEAN_TAG = "/a-bean";
    private final static String HELLO = "hello";
    
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
        URL url = getClass().getClassLoader().getResource("simple.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        AService aService = testLocator.getService(AService.class);
        Assert.assertNotNull(aService);
        
        Assert.assertEquals(HELLO, aService.getStringParameter());
        Assert.assertEquals(10, aService.getIntParameter());
        Assert.assertEquals(100, aService.getLongParameter());
    }

}
