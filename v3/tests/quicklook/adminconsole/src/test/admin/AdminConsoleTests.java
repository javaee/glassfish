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

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.jar.Manifest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import test.admin.util.GeneralUtils;

/** Supposed to have JDBC connection pool and resource tests.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
public class AdminConsoleTests extends BaseAdminConsoleTest {

    private File path;
    private static final String JAVADB_POOL = "javadb_pool"; //same as in resources.xml
    private static final String ADD_RES     = "add-resources";
    
    @Test(groups={"pulse"}) // test method
    @Parameters({"admin.console.url"})
    void testApplicationAvailability( String url) {
        this.adminUrl = url;

        boolean framesFound = false;
        int iteration = 0;
        String result = "";

        while (!framesFound && (iteration <= 10)) {
            iteration++;
            result = requestUrl(adminUrl);
            framesFound = result.indexOf("name=\"loginform\"") > -1;
            if (!framesFound) {
                try {
                    System.err.println("***** Login page not found.  Sleeping to allow app to deploy....");
                    Thread.sleep(10000);
                } catch (InterruptedException ie) {
                    //
                }
            }
        }

        if (!framesFound) {
            throw new RuntimeException("The Admin Console has not been successfully deployed.");
        }
    }


    @Test(groups={"pulse"}) // test method
    public void testLogin() {
    }
}
