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

import java.util.Properties;
import java.util.Vector;
import java.util.ResourceBundle;

import junit.framework.*;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.4 $
 */

public class LocalStringsManagerTest extends TestCase {
    public void testGettingAnAbsentKey(){
        final LocalStringsManager lsm = new LocalStringsManager("com.sun.enterprise.cli.framework", "P1");
        assertEquals("Key not found (absent)", lsm.getString("absent"));
    }
    
    public void testReplacementException() throws Exception {
        final LocalStringsManager lsm = new LocalStringsManager("com.sun.enterprise.cli.framework", "P1");
        try {
            assertEquals("15% of 1,000 makes 150", lsm.getString("bad_format", new Object[0]));
            fail("Expected CommandValidationException indicating that the formatting was incorrect");
        }
        catch (CommandValidationException cve){
            assertEquals("java.lang.IllegalArgumentException: unknown format type at ", cve.getMessage());
        }
    }

    private Properties makeProperty(String f){
        final Properties p = new Properties();
        p.setProperty("base-package", "com.sun.enterprise.cli.framework");
        p.setProperty("property-file-name", f);
        return p;
    }
    
    public void testInitializationFromProperties(){
        final Vector v = new Vector();
        v.add(makeProperty("P1"));
        v.add(makeProperty("P2"));
        final LocalStringsManager lsm = new LocalStringsManager(v);
        assertEquals("P1.a", lsm.getString("a"));
        
        ResourceBundle rb1 = 
                ResourceBundle.getBundle("com.sun.enterprise.cli.framework" +
                                         "." + "P1");
        ResourceBundle rb2 = 
                ResourceBundle.getBundle("com.sun.enterprise.cli.framework" +
                                         "." + "P2");                                    final Vector v2 = new Vector();
        v2.add(rb1);
        v2.add(rb2);
        assertEquals("Comparing Resource Properties", v2, lsm.getResourceBundles());
    }

        
    public void testReplacement() throws Exception {
        final Object[] arguments = {
            new Double(.15),
            new Integer(1000),
            new Integer(150)
        };
        final LocalStringsManager lsm = new LocalStringsManager("com.sun.enterprise.cli.framework", "P1");
        assertEquals("15% of 1,000 makes 150", lsm.getString("good_format", arguments));
    }


    public void testSimpleUse(){
        final LocalStringsManager lsm = new LocalStringsManager("com.sun.enterprise.cli.framework", "P1");
        assertEquals("P1.a", lsm.getString("a"));
    }

    public void testWithLocale()
    {
        final Vector v = new Vector();
        v.add("com.sun.enterprise.cli.framework.P1");
        final LocalStringsManager lsm = new LocalStringsManager(v, java.util.Locale.US);
        assertEquals("P1.a", lsm.getString("a"));
    }


    public void testSimpleCreation() {
        final LocalStringsManager lsm = new LocalStringsManager("com.sun.enterprise.cli.framework", "P1");
        assertEquals("P1", lsm.getPropertiesFile());
        assertEquals("com.sun.enterprise.cli.framework", lsm.getPackageName());
    }

    public LocalStringsManagerTest(String name){
        super(name);
    }

    protected void setUp() {
    }        


    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(LocalStringsManagerTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new LocalStringsManagerTest(args[i]));
        }
        return ts;
    }
}
