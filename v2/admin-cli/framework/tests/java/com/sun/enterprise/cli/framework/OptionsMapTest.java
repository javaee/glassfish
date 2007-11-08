/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.enterprise.cli.framework;

import junit.framework.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 *
 * @author <a href="mailto:jane.young@sun.com">Jane Young</a>
 * @version $Revision: 1.1 $
 */

public class OptionsMapTest extends TestCase {
    public static void assertFalse(boolean t) {
        assertTrue(!t);
    }

    public void testGetOption() {
        assertEquals("1", om.getOption("one"));
        assertEquals("2", om.getOption("two"));
        assertEquals("3", om.getOption("three"));
        assertEquals("4", om.getOption("four"));
        assertEquals("5", om.getOption("five"));                
    }

    public void testGetOptions() {
        Map m = new HashMap();
        m.put("one", "1");
        m.put("two", "2");
        m.put("three", "3");
        m.put("four", "4");
        m.put("five", "5");                        
        assertEquals(m, om.getOptions());
    }

    public void testGetCLOptions() {
        Map m = new HashMap();
        m.put("two", "2");
        assertEquals(m, om.getCLOptions());        
    }
    
    public void testGetEnvOptions() {
        Map m = new HashMap();
        m.put("three", "3");
        assertEquals(m, om.getEnvOptions());        
    }

    public void testGetDefaultOptions() {
        Map m = new HashMap();
        m.put("five", "5");
        assertEquals(m, om.getDefaultOptions());        
    }

    public void testGetOtherOptions() {
        Map m = new HashMap();
        m.put("one", "1");
        assertEquals(m, om.getOtherOptions());        
    }

    public void testContainsName() {
        assertTrue(om.containsName("one"));
        assertFalse(om.containsName("ten"));        
    }

    public void testNameSet() {
        Set<String> s = new HashSet<String>();
        s.add("one");
        s.add("two");
        s.add("three");
        s.add("four");
        s.add("five");        
        assertEquals(s, om.nameSet());
    }


    public void testToString() {
        assertEquals("<one,1>\n<two,2>\n<five,5>\n<four,4>\n<three,3>\n", om.toString());
    }

    public void testRemove() {
        om.addOptionValue("six", "6");
        assertTrue(om.containsName("six"));
        om.remove("six");
        assertFalse(om.containsName("six"));        
    }
    

    
    public OptionsMapTest(String name){
        super(name);
    }

    OptionsMap om;
    
    protected void setUp() {
        om = new OptionsMap();
        om.addOptionValue("one", "1");
        om.addCLValue("two", "2");
        om.addEnvValue("three", "3");
        om.addPrefValue("four", "4");
        om.addDefaultValue("five", "5");        
    }

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(OptionsMapTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new OptionsMapTest(args[i]));
        }
        return ts;
    }
}
