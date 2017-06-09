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
