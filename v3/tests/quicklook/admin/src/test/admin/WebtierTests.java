/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test.admin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import test.admin.util.GeneralUtils;

/** Provides web-tier configuration tests. We should also use this class to
 * test out the runtime behavior of web-tier. e.g. Are the webtier components
 * really available the moment we create them?
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
public class WebtierTests extends BaseAsadminTest {
    private final String listenerName = "ls123456"; //sufficiently unique, I believe
    
    @BeforeClass
    private void setup() {
        
    }
    @Test(groups={"pulse"}) // test method
    public void createListener() {
        String CMD = "create-http-listener";
        Map<String, String> options = getCreateOptions();
        String operand = listenerName;
        String up = GeneralUtils.ToFinalURL(adminUrl, CMD, options, operand);
        Reporter.log("url: " + up);
        invokeURL(up);
    }
    
    @Test(groups={"pulse"})
    public void deleteListener() { //should be run after createListener method
        String CMD = "delete-http-listener";
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = listenerName;
        String up = GeneralUtils.ToFinalURL(adminUrl, CMD, options, operand);
        Reporter.log("url: " + up);
        invokeURL(up);        
    }
    
    private Map<String, String> getCreateOptions() {
        Map<String, String> opts = new HashMap<String, String>();
        opts.put("listeneraddress", "0.0.0.0");
        opts.put("listenerport", "1234");
        opts.put("defaultvs", "server");
        return ( opts );
    }
}