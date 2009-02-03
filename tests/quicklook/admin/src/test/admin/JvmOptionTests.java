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

import java.util.Collections;
import java.util.Map;
import java.util.jar.Manifest;
import org.testng.annotations.Test;
import test.admin.util.GeneralUtils;

/** Test related to creating/deleting/listing JVM options as supported by GlassFish.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
public class JvmOptionTests extends BaseAsadminTest {
    private static final String TEST_JOE    = "-Dname= joe blo"; //sufficiently unique
    private static final String CJ          = "create-jvm-options";
    private static final String DJ          = "delete-jvm-options";
    private static final String LJ          = "list-jvm-options";
    
    @Test(groups={"pulse"}) // test method
    public void createJoe() {
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = TEST_JOE;
        String up = GeneralUtils.toFinalURL(adminUrl, CJ, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        GeneralUtils.handleManifestFailure(man);
    }

    @Test(groups={"pulse"}, dependsOnMethods={"createJoe"})
    public void ensureCreatedJoeExists() {
        Manifest man = runListJoesCommand();
        GeneralUtils.handleManifestFailure(man);
        // we are past failure, now test the contents
        String children = GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.CHILDREN);
        if (!children.contains(TEST_JOE)) {
            throw new RuntimeException("deleted http listener: " + TEST_JOE + " exists in the list: " + children);
        }        
    }
    
    @Test(groups={"pulse"}, dependsOnMethods={"ensureCreatedJoeExists"})
    public void deleteJoe() {
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = TEST_JOE;
        String up = GeneralUtils.toFinalURL(adminUrl, DJ, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        GeneralUtils.handleManifestFailure(man);        
    }

    @Test(groups={"pulse"}, dependsOnMethods={"deleteJoe"})
    public void deletedJoeDoesNotExist() {
        Manifest man = runListJoesCommand();
        GeneralUtils.handleManifestFailure(man);
        // we are past failure, now test the contents
        String children = GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.CHILDREN);
        if (children.contains(TEST_JOE)) {
            throw new RuntimeException("deleted http listener: " + TEST_JOE + " exists in the list: " + children);
        }         
    }

    private Manifest runListJoesCommand() {
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = null;
        String up = GeneralUtils.toFinalURL(adminUrl, LJ, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        return ( man );
    }    
}
