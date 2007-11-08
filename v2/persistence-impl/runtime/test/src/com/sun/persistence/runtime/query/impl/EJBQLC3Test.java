/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

package com.sun.persistence.runtime.query.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author Michael Bouschen
 */
public class EJBQLC3Test extends EJBQLCTest {
    /** Set of suites for testing. */
    private static Properties testsuite = new Properties();
    
    /** Set of named testcases */
    private static Map<String, PropTestCase> testcases = new HashMap<String, PropTestCase>();
    
    public EJBQLC3Test(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(EJBQLC3Test.class);
        
        return wrap(suite, EJBQLC3Test.class, testsuite, testcases);
    }
    
    /**
     * @return the static instance from <em>this</em> class.  So, each
     * subclass of EJBQLCTest <em>does</em> need to override getTestcases.
     */
    @Override protected Map<String, PropTestCase> getTestcases() {
        return testcases;
    }
    
    /* Protected implementation. */

    protected EJBQLC getCompilerDriver() {
        return EJBQLC3.getInstance();
    }
}
