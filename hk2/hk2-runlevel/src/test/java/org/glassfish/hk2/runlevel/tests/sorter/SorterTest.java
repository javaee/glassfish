/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.runlevel.tests.sorter;

import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.tests.utilities.Utilities;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class SorterTest {
    /* package */ final static String FOO = "foo";
    /* package */ final static String BAR = "bar";
    /* package */ final static String BAZ = "baz";
    
    /**
     * Tests that a sorter changes the order
     */
    @Test
    public void testBasicSort() {
        ServiceLocator locator = Utilities.getServiceLocator(Foo.class,
                Bar.class,
                Baz.class,
                RecorderService.class);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.setMaximumUseableThreads(1);
        
        // Ensure the order without the sorter is as expected
        controller.proceedTo(1);
        
        RecorderService recorder = locator.getService(RecorderService.class);
        
        {
            List<String> withoutSorter = recorder.getRecord();
            Assert.assertEquals(3, withoutSorter.size());
        
            Assert.assertEquals(FOO, withoutSorter.get(0));
            Assert.assertEquals(BAR, withoutSorter.get(1));
            Assert.assertEquals(BAZ, withoutSorter.get(2));
        }
        
        ServiceLocatorUtilities.addClasses(locator, BazBarFooSorter.class);
        
        controller.proceedTo(0);
        
        recorder.clear();
        
        controller.proceedTo(1);
        
        {
            List<String> withSorter = recorder.getRecord();
            Assert.assertEquals(3, withSorter.size());
        
            Assert.assertEquals(BAZ, withSorter.get(0));
            Assert.assertEquals(BAR, withSorter.get(1));
            Assert.assertEquals(FOO, withSorter.get(2));
        }
        
    }

}
