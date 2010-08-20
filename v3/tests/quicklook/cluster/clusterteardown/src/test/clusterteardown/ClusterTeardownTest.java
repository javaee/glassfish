/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package test.clusterteardown;

import com.sun.appserv.test.AdminBaseDevTest;
import org.testng.annotations.Test;
import org.testng.Assert;

public class ClusterTeardownTest extends AdminBaseDevTest {

    @Override
    protected String getTestDescription() {
        return "QL cluster test helloworld";
    }
        final String tn = "QLCluster";
        final String cname = "eec1";
        final String i1name = "eein1-with-a-very-very-very-long-name";
        final String i2name = "eein2";
        
        public boolean retStatus, ret1,ret2;

    @Test
    public void deleteInstanceTest() throws Exception{
        report(tn + "stop-local-instance1", asadmin("stop-local-instance", i1name));
        ret1 = report(tn + "stop-local-instance2", asadmin("stop-local-instance", i2name));
        report(tn + "delete-local-instance1", asadmin("delete-local-instance", i1name));
        ret2 = report(tn + "delete-local-instance2", asadmin("delete-local-instance", i2name));
    }
        
    @Test(dependsOnMethods = { "deleteInstanceTest" })
    public void deleteClusterTest() throws Exception{
        retStatus = report(tn + "delete-cluster", asadmin("delete-cluster", cname));
        Assert.assertEquals(retStatus&&ret1&&ret2, true, "Cluster unsetup failed ...");
    }
}
