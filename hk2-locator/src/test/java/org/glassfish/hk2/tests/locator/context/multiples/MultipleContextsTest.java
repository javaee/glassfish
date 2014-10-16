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
package org.glassfish.hk2.tests.locator.context.multiples;

import java.util.Map;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class MultipleContextsTest {
    @Test @org.junit.Ignore
    public void testMultipleRollingContexts() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(MultiContextA.class,
                MultiContextB.class,
                MultiContextC.class,
                Service1.class,
                Service2.class,
                Service3.class,
                Service4.class,
                Service5.class,
                Service6.class);
        
        Assert.assertNotNull(locator.getService(Service1.class));
        Assert.assertNotNull(locator.getService(Service2.class));
        Assert.assertNotNull(locator.getService(Service3.class));
        Assert.assertNotNull(locator.getService(Service4.class));
        Assert.assertNotNull(locator.getService(Service5.class));
        Assert.assertNotNull(locator.getService(Service6.class));
        
        MultiContextA contextA = locator.getService(MultiContextA.class);
        Assert.assertNotNull(contextA);
        
        MultiContextB contextB = locator.getService(MultiContextB.class);
        Assert.assertNotNull(contextB);
        
        MultiContextC contextC = locator.getService(MultiContextC.class);
        Assert.assertNotNull(contextC);
        
        Map<ActiveDescriptor<?>, Object> aInstances = contextA.getInstances();
        Assert.assertEquals(2, aInstances.size());
        
        Map<ActiveDescriptor<?>, Object> bInstances = contextB.getInstances();
        Assert.assertEquals(2, bInstances.size());
        
        Map<ActiveDescriptor<?>, Object> cInstances = contextC.getInstances();
        Assert.assertEquals(2, cInstances.size());
    }

}
