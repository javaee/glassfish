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
public class JdbcConnectionPoolTests extends BaseAsadminTest {

    private File path;
    private static final String JAVADB_POOL = "javadb_pool"; //same as in resources.xml
    private static final String ADD_RES     = "add-resources";
    
    @Parameters({"resources.xml.relative.path"})
    @BeforeClass
    public void setupEnvironment(String relative) {
        String cwd = System.getProperty("user.dir");
        path = new File(cwd, relative);
    }
    @Test(groups={"pulse"}) // test method
    public void createPool() {
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = path.getAbsolutePath();
        String up = GeneralUtils.toFinalURL(adminUrl, ADD_RES, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        GeneralUtils.handleManifestFailure(man);
    }
    @Test(groups={"pulse"}, dependsOnMethods={"createPool"})
    public void pingPool() {
        String CMD = "ping-connection-pool";
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = JAVADB_POOL;
        String up = GeneralUtils.toFinalURL(adminUrl, CMD, options, operand);
        Manifest man = super.invokeURLAndGetManifest(up);
        GeneralUtils.handleManifestFailure(man);
        //ping succeeded!
    }

    @Test(groups={"pulse"}, dependsOnMethods={"createPool"})
    public void ensureCreatedPoolExists() {
        Manifest man = runListPoolsCommand();
        GeneralUtils.handleManifestFailure(man);
        // we are past failure, now test the contents
        String children = GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.CHILDREN);
        if (!children.contains(JAVADB_POOL)) {
            throw new RuntimeException("deleted http listener: " + JAVADB_POOL + " exists in the list: " + children);
        }        
    }
    
    @Test(groups={"pulse"}, dependsOnMethods={"ensureCreatedPoolExists"})
    public void deletePool() {
        String CMD = "delete-jdbc-connection-pool";
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = JAVADB_POOL;
        String up = GeneralUtils.toFinalURL(adminUrl, CMD, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        GeneralUtils.handleManifestFailure(man);        
    }

    @Test(groups={"pulse"}, dependsOnMethods={"deletePool"})
    public void deletedPoolDoesNotExist() {
        Manifest man = runListPoolsCommand();
        GeneralUtils.handleManifestFailure(man);
        // we are past failure, now test the contents
        String children = GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.CHILDREN);
        if (children.contains(JAVADB_POOL)) {
            throw new RuntimeException("deleted http listener: " + JAVADB_POOL + " exists in the list: " + children);
        }         
    }

    private Manifest runListPoolsCommand() {
        String CMD = "list-jdbc-connection-pools";
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = null;
        String up = GeneralUtils.toFinalURL(adminUrl, CMD, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        return ( man );
    }    
}
