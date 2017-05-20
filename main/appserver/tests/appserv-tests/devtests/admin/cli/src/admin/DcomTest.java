/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
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

package admin;

import static admin.Constants.*;

public class DcomTest extends AdminBaseDevTest {
    private String host;
    private String domain;
    private String installdir;
    private String password;
    private String user;

    public DcomTest() {
    }

    public static void main(String[] args) {
        new DcomTest().run();
    }

    @Override
    final public void subrun() {
        try {
            validate();
            startDomain();
            setupDcom();
        }
        catch (Exception e) {
            report(e.getClass().getName(), false);
        }
        finally {
            stopDomain();
        }
    }

    @Override
    public String getTestName() {
        return "Testing DCOM";
    }

    @Override
    protected String getTestDescription() {
        return "Developer tests for clustering via DCOM";
    }

    private void setupDcom() {
        addPassword(password, PasswordType.DCOM_PASS);
        report("Added DCOM Password to password file", true);
        AsadminReturn ar = asadminWithOutput("setup-dcom",
                "--dcomuser", user,
                "--domain", domain,
                host);
        System.out.println(ar.outAndErr);
        report("test-dcom", ar.returnValue);
        removePasswords("DCOM");
    }

    private void validate() {
        host = TestUtils.getExpandedSystemProperty(DCOM_HOST_PROP);
        user = TestUtils.getExpandedSystemProperty(DCOM_USER_PROP);
        password = TestUtils.getExpandedSystemProperty(DCOM_PASSWORD_PROP);
        installdir = TestUtils.getExpandedSystemProperty(DCOM_INSTALLDIR_PROP);
        domain = TestUtils.getExpandedSystemProperty(DCOM_DOMAIN_PROP);

        if (!ok(domain))
            domain = host;

        report("host is " + host, ok(host));
        report("user is " + user, ok(user));
        report("password is " + "HIDDEN!!", ok(password));
        report("installdir is " + installdir, ok(installdir));
        report("domain is " + domain, ok(domain));
    }
}
