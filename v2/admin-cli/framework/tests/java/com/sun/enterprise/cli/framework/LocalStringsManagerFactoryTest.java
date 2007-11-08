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

/**
   Note that this test requires resources for testing. These resources
   are construct4ed from the two files P1 & P2 located in the current
   directory. If these file names are changed then the corresponding
   names in this submodules build.xml file should be changed also
*/
import junit.framework.*;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import junit.textui.TestRunner;
import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.4 $
 */

/**
   Execute these tests using gmake (and Ant) by:
   cd <framework>
   gmake ANT_TARGETS=LocalStringsManagerFactoryTest
*/

public class LocalStringsManagerFactoryTest extends TestCase {
      // I used these to experiment with different names/types for the ResourceBundles
    private static final String P1_PROP = "P1";
    private static final String P2_PROP = "P2";
    private static final String PACKAGE = "com.sun.enterprise.cli.framework";

    public void testConstructor(){
        final LocalStringsManagerFactory lsmf = new LocalStringsManagerFactory();
    }
    public void testSetInstance() throws Exception {
        final LocalStringsManager lsm = new LocalStringsManager(PACKAGE, P1_PROP);
        LocalStringsManagerFactory.setInstance(PACKAGE, lsm);
        assertEquals(lsm, LocalStringsManagerFactory.getLocalStringsManager(PACKAGE, P1_PROP));
    }
    
      // requirement: Property values unique to each resource can be retrieved
      // try to show that unique values in each resource are present
    public void testMultiplePropertyUniqueValuesMissing() throws Exception{
        Set hs = new HashSet();
        hs.add(p1);
        hs.add(p2);
        LocalStringsManagerFactory.setCommandLocalStringsManagerProperties(hs.iterator());
        LocalStringsManager lsm = LocalStringsManagerFactory.getCommandLocalStringsManager();
        assertEquals("expected b to be from P1", "P1.b", lsm.getString("b"));
        assertEquals("expected c to be from P2", "P2.c", lsm.getString("c"));
    }
	
  
      // Requirement: First reference to a property is returned first
      // Try to show that second property is the one returned.
    public void testMultiplePropertyLookupOverride() throws Exception{
        List hs = new ArrayList();
        hs.add(p1);
        hs.add(p2);

        LocalStringsManagerFactory.setCommandLocalStringsManagerProperties(hs.iterator());
        LocalStringsManager lsm = LocalStringsManagerFactory.getCommandLocalStringsManager();
        assertEquals("expected a to be overridden and come from P1", "P1.a", lsm.getString("a"));
    }
	
      // try to find that a single property file cannot be properly
      // handled using the iterator initialization
    public void testP1Lookup() throws Exception {
        Set hs = new HashSet();
        hs.add(p1);
        LocalStringsManagerFactory.setCommandLocalStringsManagerProperties(hs.iterator());
        LocalStringsManager lsm = LocalStringsManagerFactory.getCommandLocalStringsManager();
        assertEquals("P1.a", lsm.getString("a"));
        assertEquals("P1.b", lsm.getString("b"));
    }

    public void testP2Lookup() throws Exception {
        Set hs = new HashSet();
        hs.add(p2);
        LocalStringsManagerFactory.setCommandLocalStringsManagerProperties(hs.iterator());
        LocalStringsManager lsm = LocalStringsManagerFactory.getCommandLocalStringsManager();
        assertEquals("P2.a", lsm.getString("a"));
        assertEquals("P2.c", lsm.getString("c"));
	
    }

    public LocalStringsManagerFactoryTest(String name){
        super(name);
    }

    Properties p1;
    Properties p2;
  
    protected void setUp() {
        p1 = new Properties();
        p2 = new Properties();
        initProperty(p1, P1_PROP);
        initProperty(p2, P2_PROP);
    }
  
    void initProperty(Properties prop, String file){
        prop.setProperty("base-package", PACKAGE);
        prop.setProperty("property-file-name", file);
    }
  

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static Test suite(){
        TestSuite suite = new TestSuite(LocalStringsManagerFactoryTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception {
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(LocalStringsManagerFactoryTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}

