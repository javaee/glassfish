/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package versionedappclient;

import java.io.InputStream;
import java.util.Properties;

/**
 * This class is the application being tested and also the client which
 * is used to test if the given version is enabled. This is because an appclient/jws
 * application is executed on the client side.
 *
 * We have to setup a system to retrieve the client-stubs of the currently enabled
 * version. This is because "asadmin get-client-stubs" always returns the client-stubs
 * regardless the given version is enabled or not. On the other side lauching
 * the appclient from the stubs either by hand or with appclient script will succeed
 * regardless the version enabled.
 *
 * Using this appclient after a deployment (without enabled=false) or an
 * activation process (enable) will always succeed with testPositive=true
 * (expectedVersionIdentifier will always be equals to retrievedVersionIdentifier).
 *
 * Moreover using this appclient after a deactivation process will always fail
 * with testPositive=false.
 *
 * As a conclusion we can say that this test isn't consistant if launched from a
 * client-stub retrieved with the "asadmin get-client-stubs" command. Instead,
 * we choose to use the Java Web Start URL ([host:port/contextRoot], the
 * contextroot equals to the untagged if not provided in the deployment descriptor)
 * 
 * The stubs are retrieved with JWS mechanism, it allows us to change the enabled status
 * and make some tests.
 *
 * @author Romain GRECOURT - SERLI (romain.grecourt@serli.com)
 */
public class SimpleVersionedAppClient {
    String url;
    String versionIdentifier;
    Boolean testPositive;

    public SimpleVersionedAppClient(String[] args){
        url = args[0];
        testPositive = (Boolean.valueOf(args[1])).booleanValue();
        if(args.length > 2) {
            versionIdentifier = args[2];
        } else {
            versionIdentifier = "";
        }
    }

    public void doTest(){
        try {
            // retrieve the version information
            Properties prop = new Properties();
            InputStream in =
                    this.getClass().getResource("version-infos.properties").openStream();
            prop.load(in);
            in.close();
            String retrievedVersionIdentifier =
                    prop.getProperty("version.identifier", "");

            // this provides some usefull informations to investigate
            log("Test: devtests/deployment/versioning/simple-versioned-appclient");
            if(testPositive){
                log("this test is expected to succeed");
            } else {
                log("this test is expected to fail");
            }
            log("Expected version identifier = " + versionIdentifier);
            log("Retrieved version identifier = " + retrievedVersionIdentifier);

            boolean isExpectedVersionIdentifier =
                    versionIdentifier.equals(retrievedVersionIdentifier);
            if(testPositive){
                if(isExpectedVersionIdentifier){
                    pass();
                } else {
                    fail();
                }
            } else {
                if(isExpectedVersionIdentifier){
                    fail();
                } else {
                    pass();
                }
            }
        } catch (Exception ex) {
            log(ex.getMessage());
        }
    }

    private void log(String message) {
        System.err.println("[versionedappclient.client.SimpleVersionedClient]:: " + message);
    }

    private void pass() {
        log("PASSED: devtests/deployment/versioning/simple-versioned-appclient");
        System.exit(0);
    }

    private void fail() {
        log("FAILED: devtests/deployment/versioning/simple-versioned-appclient");
        System.exit(1);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {       
        SimpleVersionedAppClient app = new SimpleVersionedAppClient(args);
        app.doTest();
    }
}
