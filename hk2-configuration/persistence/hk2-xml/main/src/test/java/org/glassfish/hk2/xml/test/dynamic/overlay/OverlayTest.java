/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test.dynamic.overlay;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.Change.ChangeCategory;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.basic.beans.Commons;
import org.glassfish.hk2.xml.test.basic.beans.Museum;
import org.glassfish.hk2.xml.test.dynamic.rawsets.RawSetsTest;
import org.glassfish.hk2.xml.test.dynamic.rawsets.RawSetsTest.UpdateListener;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class OverlayTest {
    /**
     * Overlays original file with new file
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    @org.junit.Ignore
    public void testRooBeanOnlyOverlay() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        UpdateListener listener = locator.getService(UpdateListener.class);
        
        URL url = getClass().getClassLoader().getResource(Commons.MUSEUM1_FILE);
        
        XmlRootHandle<Museum> rootHandle = xmlService.unmarshal(url.toURI(), Museum.class);
        
        RawSetsTest.verifyPreState(rootHandle, hub);
        
        URL url2 = getClass().getClassLoader().getResource(RawSetsTest.MUSEUM2_FILE);
        
        XmlRootHandle<Museum> rootHandle2 = xmlService.unmarshal(url2.toURI(), Museum.class, false, false);
        
        // This just checks to make sure the original tree was not modified when creating the second handle
        RawSetsTest.verifyPreState(rootHandle, hub);
        
        rootHandle.overlay(rootHandle2);
        
        Museum museum = rootHandle.getRoot();
        
        // Now make sure new values show up
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, museum.getId());
        Assert.assertEquals(Commons.BEN_FRANKLIN, museum.getName());
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, museum.getAge());
        
        Instance instance = hub.getCurrentDatabase().getInstance(RawSetsTest.MUSEUM_TYPE, RawSetsTest.MUSEUM_INSTANCE);
        Map<String, Object> beanLikeMap = (Map<String, Object>) instance.getBean();
        
        Assert.assertEquals(Commons.BEN_FRANKLIN, beanLikeMap.get(Commons.NAME_TAG));
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, beanLikeMap.get(Commons.ID_TAG));
        Assert.assertEquals(RawSetsTest.ONE_OH_ONE_INT, beanLikeMap.get(RawSetsTest.AGE_TAG));  // The test
        
        List<Change> changes = listener.getChanges();
        Assert.assertNotNull(changes);
        
        Assert.assertEquals(2, changes.size());
        
        for (Change change : changes) {
            Assert.assertEquals(ChangeCategory.MODIFY_INSTANCE, change.getChangeCategory());
        }
    }
}
