/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.tests.locator.unqualified;

import java.util.List;

import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the &#064;Unqualified directive
 * 
 * @author jwells
 *
 */
public class UnqualifiedTest {
    private final static String TEST_NAME = "UnqualifiedTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new UnqualifiedModule());
    
    public final static String SERVER_HEALTH_COMMAND = "ServerHealthCommand";
    public final static String SERVER_DATA_COMMAND = "ServerDataCommand";
    public final static String CLIENT_HEALTH_COMMAND = "ClientHealthCommand";
    public final static String CLIENT_DATA_COMMAND = "ClientDataCommand";
    public final static String LIST_COMMAND_COMMAND = "ListCommandCommand";
    
    public final static String TRACTOR_ELEPHANT_SHOE_TOY = "TractorElephantShoeToy";
    public final static String SHOE_TOY = "ShoeToy";
    public final static String UNKNOWN_TOY = "UnknownToy";
    
    @Test
    public void testUnqualifiedOnlyGetsUnqualified() {
        ListCommandCommand lcc = locator.getService(ListCommandCommand.class);
        Assert.assertNotNull(lcc);
        
        {
            List<Command> remoteCommands = lcc.getRemoteCommands();
            Assert.assertSame(2, remoteCommands.size());
        
            Assert.assertSame(SERVER_DATA_COMMAND, remoteCommands.get(0).getName());
            Assert.assertSame(SERVER_HEALTH_COMMAND, remoteCommands.get(1).getName());
        }
        
        {
            List<Command> allCommands = lcc.getAllCommands();
            Assert.assertSame(5, allCommands.size());
        
            Assert.assertSame(SERVER_DATA_COMMAND, allCommands.get(0).getName());
            Assert.assertSame(CLIENT_DATA_COMMAND, allCommands.get(1).getName());
            Assert.assertSame(CLIENT_HEALTH_COMMAND, allCommands.get(2).getName());
            Assert.assertSame(SERVER_HEALTH_COMMAND, allCommands.get(3).getName());
            Assert.assertSame(LIST_COMMAND_COMMAND, allCommands.get(4).getName());
        }
        
        // And  now the real test
        {
            List<Command> localCommands = lcc.getLocalCommands();  // Local commands are unqualified
            Assert.assertSame(2, localCommands.size());
        
            Assert.assertSame(CLIENT_DATA_COMMAND, localCommands.get(0).getName());
            Assert.assertSame(CLIENT_HEALTH_COMMAND, localCommands.get(1).getName());
        }
        
        // Lets make sure it also got the correct "first" unqualified service
        Assert.assertSame(CLIENT_DATA_COMMAND, lcc.getFirstUnqualifiedCommand().getName());
        
        // Now not just unqualified, but those not qualified with @Remote
        {
            List<Command> notRemoteCommands = lcc.getNotRemoteCommands();  // Local commands are unqualified
            Assert.assertSame(3, notRemoteCommands.size());
        
            Assert.assertSame(CLIENT_DATA_COMMAND, notRemoteCommands.get(0).getName());
            Assert.assertSame(CLIENT_HEALTH_COMMAND, notRemoteCommands.get(1).getName());
            Assert.assertSame(LIST_COMMAND_COMMAND, notRemoteCommands.get(2).getName());
        }
        
    }
    
    @Test
    public void testDirectlyInjectedUnqualified() {
        ToyService ts = locator.getService(ToyService.class);
        
        Assert.assertSame(TRACTOR_ELEPHANT_SHOE_TOY, ts.getNaturalToy().getName());
        Assert.assertSame(SHOE_TOY, ts.getShoeToy().getName());
        Assert.assertSame(UNKNOWN_TOY, ts.getUnknownToy().getName());
        
    }
    
    /**
     * Tests the get of the unqualified iterable provider
     */
    @Test
    public void testGetOfUnqualifiedIterableProvider() {
        ListCommandCommand lcc = locator.getService(ListCommandCommand.class);
        Assert.assertNotNull(lcc);
        
        Command viaGet = lcc.getWithGetLocalCommand();
        
        Assert.assertSame(CLIENT_DATA_COMMAND, viaGet.getName());
    }
    
    /**
     * Tests the getHandle of the unqualified iterable provider
     */
    @Test
    public void testGetHandleOfUnqualifiedIterableProvider() {
        ListCommandCommand lcc = locator.getService(ListCommandCommand.class);
        Assert.assertNotNull(lcc);
        
        ServiceHandle<Command> viaGetHandle = lcc.getWithGetHandleLocalCommand();
        
        Assert.assertSame(CLIENT_DATA_COMMAND, viaGetHandle.getService().getName());
    }

}
