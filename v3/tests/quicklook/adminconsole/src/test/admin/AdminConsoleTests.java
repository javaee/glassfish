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
@Test(groups = {"adminconsole"}, description = "Admin Console tests")
public class AdminConsoleTests extends BaseAdminConsoleTest {

    /**
     * Request /commonTask.jsf and verify that the common task page was rendered.
     * @throws java.lang.Exception
     */
    @Test
    public void testCommonTasks() throws Exception {
        Assert.assertTrue(getUrlAndTestForStrings(this.adminUrl + "commonTask.jsf?bare=true",
                "id=\"form:commonTasksSection\""),
                "The Common Task page does not appear to have been rendered.");
    }

    /**
     * Request /applications/applications.jsf and verify that the applications page was rendered.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeployedAppPage() throws Exception {
        Assert.assertTrue(getUrlAndTestForStrings(this.adminUrl + "applications/applications.jsf?bare=true",
                "id=\"propertyForm:deployTable\""),
                "The Deployed Applications table does not appear to have been rendered.");
    }

    /**
     * Request /common/security/realms/realms.jsf to test that pages from plugin module can be rendered.
     * @throws java.lang.Exception
     */
    @Test
    public void testRealmsList() throws Exception {
        Assert.assertTrue(getUrlAndTestForStrings(this.adminUrl + "common/security/realms/realms.jsf?bare=true",
                "id=\"propertyForm:realmsTable\""),
                "The Security realms table does not appear to have been rendered.");
    }

    /*
     * Disabling for now, we have a new help system in place -- the old help system has been removed.
    @Test
    public void testHelpPage() throws Exception {
        Assert.assertTrue(getUrlAndTestForStrings(this.adminUrl + "com_sun_webui_jsf/help/helpwindow.jsf?&windowTitle=Help+Window&helpFile=CONTEXT_HELP.html",
                "id=\"navFrame\"", "id=\"buttonNavFrame\"", "id=\"contentFrame\""));
        Assert.assertTrue(getUrlAndTestForStrings(this.adminUrl + "com_sun_webui_jsf/help/navigator.jsf",
                "id=\"helpNavigatorForm:javaHelpTabSet\""));
        Assert.assertTrue(getUrlAndTestForStrings(this.adminUrl + "com_sun_webui_jsf/help/buttonnav.jsf",
                "input id=\"helpButtonNavForm_hidden\""));
        Assert.assertTrue(getUrlAndTestForStrings(this.adminUrl + "html/en/help/CONTEXT_HELP.html",
                "body class=\"HlpBdy\""));
    }
    */
}
