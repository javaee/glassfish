/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test.admin;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import test.admin.util.GeneralUtils;

/**
 *
 * @author kedar
 */
public class JdbcConnectionPoolTests extends BaseAsadminTest {

    private File path;
    private static final String JAVADB_POOL = "javadb_pool"; //same as in resources.xml
    private static final String CMD         = "add-resources";
    
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
        String up = GeneralUtils.ToFinalURL(adminUrl, CMD, options, operand);
        Reporter.log("url: " + up);
        invokeURL(up);
    }
}
