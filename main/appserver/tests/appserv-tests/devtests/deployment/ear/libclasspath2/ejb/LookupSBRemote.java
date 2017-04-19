
package libclasspath2.ejb;

import java.io.IOException;
import javax.ejb.Remote;
import libclasspath2.ResourceHelper;


/**
 * This is the business interface for LookupSB enterprise bean.
 */
@Remote
public interface LookupSBRemote {
    public ResourceHelper.Result runTests(String[] tests, ResourceHelper.TestType testType) throws IOException;
    
}
