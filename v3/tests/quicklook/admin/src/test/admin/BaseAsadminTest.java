package test.admin;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.Manifest;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import test.admin.util.GeneralUtils;

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
    
    protected void invokeURL(String urls) {
        try {
            URL url = new URL(urls);
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            uc.setRequestMethod("GET");
            uc.setRequestProperty("User-Agent", "hk2-agent");
            uc.connect();
            Manifest man = new Manifest(uc.getInputStream());
            GeneralUtils.analyzeManifest(man);
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