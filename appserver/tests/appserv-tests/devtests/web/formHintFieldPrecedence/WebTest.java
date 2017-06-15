/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test to ensure that <parameter-encoding> subelement of <sun-web-app>
 * takes precedence over <parameter-encoding> subelement of
 * <locale-charset-info>, which has been deprecated.
 *
 * In its sun-web.xml, this web module specifies two parameter-encoding
 * elements, each with a form-hint-field attribute: the value of the 
 * form-hint-field attribute of the <parameter-encoding> subelement of 
 * <sun-web-app> is 'sunWebAppFromHintField', whereas the value of the
 * form-hint-field attribute of the <parameter-encoding> subelement of
 * <locale-charset-info> is 'localeCharsetInfoFormHintField'.
 *
 * Client appends to the request URI two query parameters named after the
 * form-hint-field attributes. The two query parameters, which would normally
 * represent hidden form fields, specify different request charsets as their
 * values.
 *
 * Container is supposed to set the request encoding to the value of the query
 * parameter named 'sunWebAppFromHintField', which is supposed to take
 * precedence over the query parameter named 'localeCharsetInfoFormHintField'.
 *
 * JSP that is the target of the request retrieves the request encoding and
 * assigns it as the response encoding, which is checked by this client by
 * parsing the Content-Type response header.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "form-hint-field-precedence";

    private static final String LOCALE_CHARSET_INFO_FORM_HINT_FIELD
        = "localeCharsetInfoFormHintField";
    private static final String SUN_WEB_APP_FORM_HINT_FIELD
        = "sunWebAppFormHintField";

    private static final String LOCALE_CHARSET_INFO_FORM_HINT_FIELD_CHARSET
        = "GB18030";
    private static final String SUN_WEB_APP_FORM_HINT_FIELD_CHARSET
        = "Shift_JIS";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription(TEST_NAME);
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeJsp();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJsp() throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/jsp/formHintField.jsp?"
                          + LOCALE_CHARSET_INFO_FORM_HINT_FIELD
                          + "="
                          + LOCALE_CHARSET_INFO_FORM_HINT_FIELD_CHARSET
                          + "&"
                          + SUN_WEB_APP_FORM_HINT_FIELD
                          + "="
                          + SUN_WEB_APP_FORM_HINT_FIELD_CHARSET);
        System.out.println("Invoking URL: " + url.toString());

        URLConnection conn = url.openConnection();
        String contentType = conn.getContentType();
        System.out.println("Response Content-Type: " + contentType);

        if (contentType != null) {
            System.out.println(contentType);
            int index = contentType.indexOf(
                        "charset=" + SUN_WEB_APP_FORM_HINT_FIELD_CHARSET);
            if (index != -1) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                stat.addStatus(TEST_NAME,
                               stat.FAIL);
            }
        } else {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
