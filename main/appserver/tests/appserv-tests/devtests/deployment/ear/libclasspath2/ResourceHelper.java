package libclasspath2;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;


/**
 * Utility class used by both the server-side and client-side logic in the
 * library/class path tests to help determine whether resources are loaded from
 * the correct places with a variety of packaging and <library-directory> settings.
 *
 * @author tjquinn
 */
public class ResourceHelper {
    
    private static final String NO_RESOURCE = "NO_RESOURCE";
    private static final String NO_PROPERTY = "NO_PROPERTY";
    
    public static String find(String resourceName, String propertyName) throws IOException {
        String result;
        URL url = ResourceHelper.class.getResource(resourceName);
        if (url == null) {
            result = NO_RESOURCE;
        } else {
            /*
             * We found the resource.  Load it into a properties object
             * and then look for the requested property.
             */
            InputStream is = null;
            try {
                URLConnection cnx = url.openConnection();
                cnx.setUseCaches(false);
                
                is = cnx.getInputStream();
                Properties props = new Properties();
                props.load(is);
                String actualValue = props.getProperty(propertyName);
                if (actualValue == null) {
                    result = NO_PROPERTY;
                } else {
                    result = actualValue;
                }
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        return result;
    }
    
    /**
     * Makes sure that each test of the form <resourceName>:<propertyName>=<expectedValue>
     * passes; that is, that the resource is found, that the property is 
     * set by the properties file represented by that resource, and that the
     * property value is the same as the expected value.
     *@param tests 0 or more expressions <resourceName>:<propertyName>=<expectedValue>
     *@return Results object describing the results of the tests 
     */
    public static Result checkAll(String[] tests, TestType callerTestType) throws IOException {
        Result result = new Result();
        TestType currentArgumentTestType = TestType.BOTH;
        for (String test : tests) {
            TestType newTestType = null;
            if (test.startsWith("-")) {
                /*
                 * This argument may specify a change in which test type the following tests are.
                 */
                if ((newTestType = TestType.find(test.substring(1))) != null) {
                    currentArgumentTestType = newTestType;
                } else {
                    System.err.println("Did not recognize new test type of " + test + "; ignoring and continuing.");
                }
                continue;
            }
            
            if ( ! callerTestType.runs(currentArgumentTestType)) {
                continue;
            }
            int colon = test.indexOf(":");
            if (colon <= 0) {
                throw new IllegalArgumentException("Missing : in test " + test);
            }
            int equals = test.indexOf("=");
            if (equals <= colon) {
                throw new IllegalArgumentException("Missing = sign in test " + test);
            }
            String resourceName = test.substring(0,colon);
            String propertyName = test.substring(colon+1, equals);
            String expectedResult = test.substring(equals+1);
            String resultValue = find(resourceName, propertyName);
            if (resultValue.equals(expectedResult)) {
                result.recordResult(true, test + " passed@");
            } else {
                result.recordResult(false, test + " failed (actual " + resultValue + ")@");
            }
        }
        return result;
    }
    
    public static class Result implements Serializable {
        private boolean result = true;
        private StringBuilder results = new StringBuilder();
        
        private void recordResult(boolean result, String note) {
            this.result &= result;
            results.append(note).append("@");
        }
        
        public boolean getResult() {
            return result;
        }
        
        public StringBuilder getResults() {
            return results;
        }
    }
    
    public static enum TestType {
        CLIENT("clientonly"),
        SERVER("serveronly"),
        BOTH("both");
        
        private String name;
        TestType(String name) {
            this.name = name;
        }
        
        public String toString() {
            return name;
        }
        
        public static TestType find(String type) {
            for (TestType tt : values()) {
                if (tt.toString().equals(type)) {
                    return tt;
                }
            }
            return null;
        }
        
        boolean runs(TestType argumentTestType) {
            return argumentTestType.equals(BOTH) || (this.equals(argumentTestType));
        }
    }
}