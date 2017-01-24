/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test.dynamic.overlay.direct;

import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Change.ChangeCategory;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.dynamic.overlay.ChangeDescriptor;
import org.glassfish.hk2.xml.test.dynamic.overlay.OverlayUtilities;
import org.glassfish.hk2.xml.test.dynamic.rawsets.RawSetsTest.UpdateListener;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class OverlayDirectTest {
    private final static String TERMINAL_DATA_A = "terminalDataA";
    
    private static XmlRootHandle<OverlayRootBBean> createEmptyRoot(XmlService xmlService, boolean advertise) {
        XmlRootHandle<OverlayRootBBean> retVal = xmlService.createEmptyHandle(OverlayRootBBean.class, advertise, advertise);
        retVal.addRoot();
        
        return retVal;
    }
    
    
    /**
     * Tests adding a two-deep direct bean
     */
    @Test @org.junit.Ignore
    public void testDirectWithDirectAdded() {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        UpdateListener listener = locator.getService(UpdateListener.class);
        
        XmlRootHandle<OverlayRootBBean> originalHandle = createEmptyRoot(xmlService, true);
        XmlRootHandle<OverlayRootBBean> modifiedHandle = createEmptyRoot(xmlService, false);
        
        OverlayRootBBean modifiedRoot = modifiedHandle.getRoot();
        
        modifiedRoot.setDirectWithDirect(xmlService.createBean(DirectWithDirect.class));
        DirectWithDirect dwd = modifiedRoot.getDirectWithDirect();
        
        dwd.setDirectTerminal(xmlService.createBean(DirectTerminalBean.class));
        DirectTerminalBean dtb = dwd.getDirectTerminal();
        
        dtb.setTerminalData(TERMINAL_DATA_A);
        
        originalHandle.overlay(modifiedHandle);
        
        OverlayRootBBean originalRoot = originalHandle.getRoot();
        Assert.assertNotNull(originalRoot.getDirectWithDirect());
        Assert.assertNull(originalRoot.getDirectWithKeyed());
        Assert.assertNull(originalRoot.getDirectWithUnkeyed());
        
        DirectWithDirect overlayDWD = originalRoot.getDirectWithDirect();
        DirectTerminalBean overlayDTB = overlayDWD.getDirectTerminal();
        
        Assert.assertNotNull(overlayDTB);
        Assert.assertEquals(TERMINAL_DATA_A, overlayDTB.getTerminalData());
        
        List<Change> changes = listener.getChanges();
        
        OverlayUtilities.checkChanges(changes,
                new ChangeDescriptor(ChangeCategory.ADD_TYPE,
                        "/overlay-root-B/direct-with-direct",    // type name
                        null,      // instance name
                        null
                )
                , new ChangeDescriptor(ChangeCategory.ADD_INSTANCE,
                        "/overlay-root-B/direct-with-direct",    // type name
                        "overlay-root-B.direct-with-direct",      // instance name
                        null
                 )
                 , new ChangeDescriptor(ChangeCategory.ADD_TYPE,
                        "/overlay-root-B/direct-with-direct/direct-terminal",    // type name
                        null,      // instance name
                        null
                 )
                 , new ChangeDescriptor(ChangeCategory.ADD_INSTANCE,
                         "/overlay-root-B/direct-with-direct/direct-terminal",    // type name
                         "overlay-root-B.direct-with-direct.direct-terminal",      // instance name
                         null
                 )
                 , new ChangeDescriptor(ChangeCategory.MODIFY_INSTANCE,
                         "/overlay-root-B",    // type name
                         "overlay-root-B",      // instance name
                         "direct-with-direct"
                 )
        );
    }

}
