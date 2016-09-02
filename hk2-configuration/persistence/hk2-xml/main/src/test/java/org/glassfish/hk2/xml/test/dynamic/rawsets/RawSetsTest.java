/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2016 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.xml.test.dynamic.rawsets;

import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener;
import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Change.ChangeCategory;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.basic.UnmarshallTest;
import org.glassfish.hk2.xml.test.basic.beans.Museum;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class RawSetsTest {
    public final static String MUSEUM2_FILE = "museum2.xml";
    
    public final static String MUSEUM_TYPE = "/museum";
    public final static String MUSEUM_INSTANCE = "museum";
    
    public final static String AGE_TAG = "age";
    
    public final static int ONE_OH_ONE_INT = 101;
    
    /**
     * Just verifies that the original state of the Museum
     * object from the file is as expected
     */
    @SuppressWarnings("unchecked")
    public static void verifyPreState(XmlRootHandle<Museum> rootHandle, Hub hub) {
        Museum museum = rootHandle.getRoot();
        
        Assert.assertEquals(UnmarshallTest.HUNDRED_INT, museum.getId());
        Assert.assertEquals(UnmarshallTest.BEN_FRANKLIN, museum.getName());
        Assert.assertEquals(UnmarshallTest.HUNDRED_TEN_INT, museum.getAge());
        
        Instance instance = hub.getCurrentDatabase().getInstance(MUSEUM_TYPE, MUSEUM_INSTANCE);
        Map<String, Object> beanLikeMap = (Map<String, Object>) instance.getBean();
        
        Assert.assertEquals(UnmarshallTest.BEN_FRANKLIN, beanLikeMap.get(UnmarshallTest.NAME_TAG));
        Assert.assertEquals(UnmarshallTest.HUNDRED_INT, beanLikeMap.get(UnmarshallTest.ID_TAG));
        Assert.assertEquals(UnmarshallTest.HUNDRED_TEN_INT, beanLikeMap.get(AGE_TAG));
    }
    
    /**
     * Tests that single fields can be modified
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test // @org.junit.Ignore
    public void testModifySingleProperty() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        UpdateListener listener = locator.getService(UpdateListener.class);
        
        URL url = getClass().getClassLoader().getResource(UnmarshallTest.MUSEUM1_FILE);
        
        XmlRootHandle<Museum> rootHandle = xmlService.unmarshall(url.toURI(), Museum.class);
        
        verifyPreState(rootHandle, hub);
        
        Museum museum = rootHandle.getRoot();
        
        // All above just verifying the pre-state
        museum.setAge(ONE_OH_ONE_INT);  // getting younger?
        
        Assert.assertEquals(UnmarshallTest.HUNDRED_INT, museum.getId());
        Assert.assertEquals(UnmarshallTest.BEN_FRANKLIN, museum.getName());
        Assert.assertEquals(ONE_OH_ONE_INT, museum.getAge());
        
        Instance instance = hub.getCurrentDatabase().getInstance(MUSEUM_TYPE, MUSEUM_INSTANCE);
        Map<String, Object> beanLikeMap = (Map<String, Object>) instance.getBean();
        
        Assert.assertEquals(UnmarshallTest.BEN_FRANKLIN, beanLikeMap.get(UnmarshallTest.NAME_TAG));
        Assert.assertEquals(UnmarshallTest.HUNDRED_INT, beanLikeMap.get(UnmarshallTest.ID_TAG));
        Assert.assertEquals(ONE_OH_ONE_INT, beanLikeMap.get(AGE_TAG));  // The test
        
        List<Change> changes = listener.changes;
        Assert.assertNotNull(changes);
        
        Assert.assertEquals(1, changes.size());
        
        for (Change change : changes) {
            Assert.assertEquals(ChangeCategory.MODIFY_INSTANCE, change.getChangeCategory());
        }
    }
    
    @Singleton
    public static class UpdateListener implements BeanDatabaseUpdateListener {
        private List<Change> changes;

        /* (non-Javadoc)
         * @see org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener#prepareDatabaseChange(org.glassfish.hk2.configuration.hub.api.BeanDatabase, org.glassfish.hk2.configuration.hub.api.BeanDatabase, java.lang.Object, java.util.List)
         */
        @Override
        public void prepareDatabaseChange(BeanDatabase currentDatabase,
                BeanDatabase proposedDatabase, Object commitMessage,
                List<Change> changes) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener#commitDatabaseChange(org.glassfish.hk2.configuration.hub.api.BeanDatabase, org.glassfish.hk2.configuration.hub.api.BeanDatabase, java.lang.Object, java.util.List)
         */
        @Override
        public void commitDatabaseChange(BeanDatabase oldDatabase,
                BeanDatabase currentDatabase, Object commitMessage,
                List<Change> changes) {
            this.changes = changes;
            
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener#rollbackDatabaseChange(org.glassfish.hk2.configuration.hub.api.BeanDatabase, org.glassfish.hk2.configuration.hub.api.BeanDatabase, java.lang.Object, java.util.List)
         */
        @Override
        public void rollbackDatabaseChange(BeanDatabase currentDatabase,
                BeanDatabase proposedDatabase, Object commitMessage,
                List<Change> changes) {
            // TODO Auto-generated method stub
            
        }
        
        public List<Change> getChanges() {
            return changes;
        }
        
    }

}
