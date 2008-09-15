/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package test.admin;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Supposed to have JDBC connection pool and resource tests.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
public class AdminConsoleTests extends BaseAdminConsoleTest {

    /**
     * Request / and verify that the frameset was rendered.
     * @throws java.lang.Exception
     */
    @Test
    public void testFrameSet() throws Exception {
        Assert.assertTrue(getUrlAndTestForString(this.adminUrl, "frameset id=\"outerFrameset\""));
    }

    /**
     * Request /preTree.jsf and verify that the tree was rendered.
     * @throws java.lang.Exception
     */
    @Test
    public void testNavTree() throws Exception {
        Assert.assertTrue(getUrlAndTestForString(this.adminUrl + "peTree.jsf", "div id=\"form:tree\""));
    }

    /**
     * Request /header.jsf and verify that the form was rendered.
     * @throws java.lang.Exception
     */
    @Test
    public void testHeader() throws Exception {
        Assert.assertTrue(getUrlAndTestForString(this.adminUrl + "header.jsf", "form id=\"propertyForm\""));
    }

    /**
     * Request /commonTask.jsf and verify that the common task page was rendered.
     * @throws java.lang.Exception
     */
    @Test
    public void testCommonTasks() throws Exception {
        Assert.assertTrue(getUrlAndTestForString(this.adminUrl + "commonTask.jsf", "Common Task"));
    }

    /**
     * Request /web/webApp/webApplications.jsf and verify that the common task page was rendered.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeployedWebAppPage() throws Exception {
        Assert.assertTrue(getUrlAndTestForString(this.adminUrl + "web/webApp/webApplications.jsf", "Deployed Web Applications ("));
    }
}