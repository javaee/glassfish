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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

/**
 * Represents a kind of test whose parameters are string values taken from
 * a Properties object.
 * 
 * @author Dave Bristor
 */
public class PropTestCase {
    private final String name;
    
    private final Map<String, String> testData;
    
    /**
     * Create a <code>PropTestCase</code> with the given name.  The name
     * is used as a key in the given <code>Properties</code> to find the
     * parameter values specific to this test.  For example, each property
     * is expected to be of the form:
     * <pre>
     * Finder001.type=finder
     * </pre>
     * The given <code>name</code> above would be Finder001.  The resulting
     * instance of <code>PropTestCase</code> would have a property named
     * <code>type</code>.  The value of the property would be available from
     * the {@link #get(String} method, which would return <code>finder</code>.
     * 
     * @param name
     * @param p
     * @return
     */
    static PropTestCase create(String name, Properties p) {
        Map<String, String> setup = new HashMap<String, String>();
        
        for (Iterator i = p.entrySet().iterator(); i.hasNext();) {
            Entry e = (Entry) i.next();
            String key = (String) e.getKey();
            if (key.startsWith(name)) {
                String value = (String) e.getValue();
                int dotPos = key.indexOf('.');
                String indicator = key.substring(dotPos + 1);
                setup.put(indicator, value);
            }
        }
        
        return new PropTestCase(name, setup);
    }
    
    protected PropTestCase(String name, Map<String, String> testData) {
        this.name = name;
        this.testData = testData;
    }
    
    public String get(String key) {
        return testData.get(key);
    }
    
    public String getName() {
        return name;
    }
    
    void print(PrintStream ps) {
        for (Iterator i = testData.entrySet().iterator(); i.hasNext();) {
            Entry e = (Entry) i.next();
            ps.println("key: " + (String) e.getKey() + ", value: " + e.getValue());
        }
    }
}
