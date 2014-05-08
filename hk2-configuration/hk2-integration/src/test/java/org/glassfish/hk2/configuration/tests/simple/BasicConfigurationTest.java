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
package org.glassfish.hk2.configuration.tests.simple;

import javax.inject.Inject;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hk2.testing.junit.HK2Runner;

/**
 * Tests the basic functionality of single-input configuration
 * 
 * @author jwells
 *
 */
public class BasicConfigurationTest extends HK2Runner {
    /* package */ final static String TEST_TYPE_ONE = "TestConfigurationType1";
    
    private final static String FIELD1 = "field1";
    private final static String FIELD2 = "field2";
    private final static String CONSTRUCTOR = "constructor";
    private final static String METHOD1 = "method1";
    private final static String METHOD2 = "method2";
    
    @Inject
    private Hub hub;
    
    private ConfiguredServiceBean createBean() {
        ConfiguredServiceBean csb = new ConfiguredServiceBean();
        
        csb.setConstructorOutput(CONSTRUCTOR);
        csb.setFieldOutput1(FIELD1);
        csb.setFieldOutput2(FIELD2);
        csb.setMethodOutput1(METHOD1);
        csb.setMethodOutput2(METHOD2);
        
        return csb;
    }
    
    private void addBean() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        WriteableType wt = wbd.findOrAddWriteableType(TEST_TYPE_ONE);
        
        wt.addInstance(CONSTRUCTOR, createBean());
        
        wbd.commit();
    }
    
    /**
     * Tests a service that has basic configuration information
     */
    @Test @Ignore
    public void testBasicConfiguration() {
        addBean();
        
        ActiveDescriptor<?> ad = testLocator.getBestDescriptor(BuilderHelper.createContractFilter(ConfiguredService.class.getName()));
        Assert.assertNotNull(ad);
        
        ConfiguredService cs = testLocator.getService(ConfiguredService.class);
        Assert.assertNotNull(cs);
        
        Assert.assertEquals(CONSTRUCTOR, cs.getConstructorOutput());
        Assert.assertEquals(METHOD1, cs.getMethodOutput1());
        Assert.assertEquals(METHOD2, cs.getMethodOutput2());
        Assert.assertEquals(FIELD1, cs.getFieldOutput1());
        Assert.assertEquals(FIELD2, cs.getFieldOutput2());
    }
}
