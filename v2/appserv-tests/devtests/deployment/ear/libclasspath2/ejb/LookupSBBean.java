package libclasspath2.ejb;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import javax.ejb.*;
import libclasspath2.ResourceHelper;

/**
 * This is the bean class for the LookupSBBean enterprise bean.
 * Created Nov 11, 2005 2:18:56 PM
 * @author tjquinn
 */
@Stateless()
public class LookupSBBean implements LookupSBRemote {
    
    public ResourceHelper.Result runTests(String[] tests, ResourceHelper.TestType testType) throws IOException {
        return ResourceHelper.checkAll(tests, testType);
    }
}
