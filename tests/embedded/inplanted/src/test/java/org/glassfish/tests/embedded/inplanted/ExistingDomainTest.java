/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
 *
 */

package org.glassfish.tests.embedded.inplanted;

import org.glassfish.api.embedded.*;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.admin.*;
import org.glassfish.api.container.Sniffer;
import org.glassfish.tests.embedded.utils.EmbeddedServerUtils;
import org.junit.BeforeClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.AfterClass;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import java.io.File;
import java.util.Enumeration;
import java.util.Collection;
import java.lang.reflect.Method;

/**
 * Test embedded API with an existing domain.xml
 *
 * @author Jerome Dochez
 */
public class ExistingDomainTest {
    static Server server;

    @BeforeClass
    public static void setupServer() throws Exception {
        File serverLocation = EmbeddedServerUtils.getServerLocation();
        EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
        efsb.installRoot(serverLocation).instanceRoot(EmbeddedServerUtils.getDomainLocation(serverLocation));
        server = EmbeddedServerUtils.createServer(efsb.build());
    }

    @Test
    public void Test() throws Exception {

        Habitat habitat = server.getHabitat();
        System.out.println("Process type is " + habitat.getComponent(ProcessEnvironment.class).getProcessType());
        Collection<Inhabitant<?>> listeners = habitat.getInhabitantsByType("com.sun.grizzly.config.dom.NetworkListener");
        Assert.assertTrue(listeners.size()>1);
        for (Inhabitant<?> s : listeners) {
            Object networkListener = s.get();
            Method m = networkListener.getClass().getMethod("getPort");
            Assert.assertNotNull("Object returned does not implement getPort, is it a networkListener ?", m);
            String port = (String) m.invoke(networkListener);
            System.out.println("Network Listener " + port);
            Assert.assertNotNull("Got a null networkListener port", port);
        }
    }

    @AfterClass
    public static void shutdownServer() throws Exception {
        EmbeddedServerUtils.shutdownServer(server);
    }
    
}
