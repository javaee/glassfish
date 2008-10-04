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

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.Manifest;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

/** The base class for asadmin tests. Designed for extension.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
public class BaseAsadminTest {
    
    String adminUrl;
    String adminUser;
    String adminPassword;
    
    @BeforeClass
    @Parameters({"admin.url", "admin.user", "admin.password"})
    void setUpEnvironment(String url, String adminUser, String adminPassword) {
        this.adminUrl      = url;
        this.adminUser     = adminUser;
        this.adminPassword = adminPassword;
    }
    
    protected Manifest invokeURLAndGetManifest(String urls) {
        try {
            URL url = new URL(urls);
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            uc.setRequestMethod("GET");
            uc.setRequestProperty("User-Agent", "hk2-agent");
            uc.connect();
            Manifest man = new Manifest(uc.getInputStream());
            return ( man );
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    protected void logEnv() {
        Properties props = System.getProperties();
        Enumeration<Object> keys = props.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Reporter.log((key + " = " + props.get(key)));
        }
    }
}