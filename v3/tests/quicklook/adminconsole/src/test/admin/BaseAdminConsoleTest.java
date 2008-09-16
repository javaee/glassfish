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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;

/** The base class for asadmin tests. Designed for extension.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
public class BaseAdminConsoleTest {

    protected String adminUrl;
    protected HttpClient client;
    protected static final int AC_TEST_DELAY = 5000;

    /**
     * This BeforeTest method will verify that the login form is available.  Once
     * it is found, the login form is submitted.  If the login succeeds, then
     * the tests are allowed to continue.  If the login fails, the each test will
     * fail.
     * @param url
     * @throws java.lang.Exception
     */
    @BeforeTest
    @Parameters({"admin.console.url"})
    void loginBeforeTest( String url) throws Exception {
        this.adminUrl = url;
        client = new HttpClient();
        
        boolean formFound = false;
        int iterations = 0;

        while (!formFound && iterations < 10) {
            iterations++;
            formFound = getUrlAndTestForString(url+"login.jsf", "name=\"loginform\"");
            if (!formFound) {
                System.err.println("***** Login page not found.  Sleeping to allow app to deploy....");
                Thread.sleep(AC_TEST_DELAY);
            }
        }

        Assert.assertTrue(formFound);
        
        PostMethod post = new PostMethod(url+"j_security_check");
        post.setRequestBody(new NameValuePair[] {
           new NameValuePair("j_username", "anonymous")
           ,new NameValuePair("j_password", "")
        });
        post.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        
        int statusCode = client.executeMethod(post);
        if (statusCode == 302) {
            Header locationHeader = post.getResponseHeader("location");
            if (locationHeader != null) {
                Assert.assertEquals(this.adminUrl, locationHeader.getValue());
            } else {
                Assert.fail("Failed to login: no redirect header");
            }
        } else if (statusCode != HttpStatus.SC_OK) {
            Assert.fail("Login failed: " + post.getStatusLine() + ": " + statusCode);
        }
    }

    @AfterTest
    public void shutdownClient() {
        client = null;
    }

    /**
     * This method will request the specified URL and examine the response for the
     * needle specified.
     * @param url
     * @param needle
     * @return
     * @throws java.lang.Exception
     */
    protected boolean getUrlAndTestForString(String url, String needle) throws Exception {
        GetMethod get = new GetMethod(url);
        get.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        get.setFollowRedirects(true);

        int statusCode = client.executeMethod(get);
        if (statusCode != HttpStatus.SC_OK) {
            Assert.fail("BaseAdminConsoleTest.getUrlAndTestForString() failed.  HTTP Status Code:  " + statusCode);
        }
        String haystack = get.getResponseBodyAsString();
        get.releaseConnection();
        return haystack.indexOf(needle) > -1;
    }
}