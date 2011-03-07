/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Sun Microsystems, Inc. All rights reserved.
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
 */
package admin.monitoring;

import com.sun.appserv.test.BaseDevTest.AsadminReturn;
import java.io.*;
import static admin.monitoring.Constants.*;

/**
 * Test fixed issues from JIRA
 * @author Byron Nevins
 */
public class Jira extends MonTest {
    @Override
    void runTests(TestDriver driver) {
        setDriver(driver);
        report(true, "Hello from JIRA Tests!");
        test15397();
    }

    private void test15397() {
        String prepend = "15397::";
        report(earFile.isFile() && earFile.canRead(), prepend + "Ear File exists");
        report(asadmin("deploy", earFile.getAbsolutePath()), prepend + "deploy earfile with dot");
        report(asadminWithOutput("list-components").outAndErr.indexOf("webapp2") >= 0, prepend + "verify-deploy");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.applications.webapp2.*"), MAGIC_NAME_IN_APP),
                prepend + "check-getm-1");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.applications.webapp2.webapp2webmod1\\.war*"), MAGIC_NAME_IN_APP),
                prepend + "check-getm-1");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.applications.webapp2.webapp2webmod1.war*"), MAGIC_NAME_IN_APP),
                prepend + "check-getm-1");
    }

    private boolean checkForString(AsadminReturn r, String s) {
        if (r.outAndErr == null)
            return false;

        return r.outAndErr.indexOf(s) >= 0;
    }
    
    private static final File earFile = new File("apps/webapp2.ear");
    private static final String MAGIC_NAME_IN_APP = "webapp2webmod1_Servlet2";
}
