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
import junit.textui.TestRunner;
import java.util.Vector;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author jane.young@sun.com
 * @version $Revision: 1.7 $
 */

/**
   Execute these tests using gmake (and Ant) by:
   cd <framework>
   gmake ANT_TARGETS=CommandLineParserTest
*/

public class CommandLineParserTest extends TestCase {
    public void testInsertOperands() throws Exception {
        String [] args = {"samplecommand", "--user", "admin", "--password", "adminadmin", "-t=false", "operand"};
        CommandLineParser clp = new CLP(args, vc1);
        Map options = clp.getOptionsList();
        assertTrue(clp.getOperands().contains("operand"));
    }
        
    public void testOptionWithEqualsSign() throws Exception {
        String [] args = {"samplecommand", "--user", "admin", "--password", "adminadmin", "--terse=false"};
        CommandLineParser clp = new CLP(args, vc1);
        Map options = clp.getOptionsList();
        assertEquals("short option for terse if true", "false", (String)options.get("terse"));
    }
    
        
    public void testShortOptionsGroup() throws Exception {
        String [] args = {"samplecommand", "--user", "admin", "--password", "adminadmin", "-ti"};
        CommandLineParser clp = new CLP(args, vc1);
        Map options = clp.getOptionsList();
        assertEquals("short option for terse if true", "true", (String)options.get("terse"));
        assertEquals("short option for interactive is true", "true", (String)options.get("interactive"));
    }

    
    public void testShortOptionsWithEqual() throws Exception {
        String [] args = {"samplecommand", "--user", "admin", "-f=oo.bar.com", "--password", "adminadmin", "--terse=false"};
        CommandLineParser clp = new CLP(args, vc1);
        Map options = clp.getOptionsList();
        assertEquals("short option for terse if false", "false", (String)options.get("terse"));
        assertEquals("short option for f", "oo.bar.com", (String)options.get("foo"));        
    }
    
        
    public void testToString() throws Exception {
        CommandLineParser clp = new CLP();
        assertEquals("\n**********\nname = null\nOptions = \nOperands = []\n**********\n", clp.toString());
    }
        
    public void testLocalizedString() throws Exception {
        CommandLineParser clp = new CLP();
        assertEquals("Key not found (this key)", clp.getLocalizedString("this key", (Object []) null));
    }
    

    public void testNullCommand() throws Exception {
        CommandLineParser clp = new CLP(vc1);
        String [] args = {"samplecommand", "--user", "admin", "--password", "adminadmin"};
        clp.parseCommandLine(args);
        Map options = clp.getOptionsList();
        assertEquals("terse option is by default is false", "false", (String)options.get("terse"));
        assertEquals("interactive option is by default is true", "true", (String)options.get("interactive"));
        assertTrue(clp.getOperands().isEmpty());
    }
        
    public void testSimpleConstruction() throws HelpException {
        CommandLineParser clp = new CLP();
        String [] args = {"samplecommand", "--user", "admin", "--password", "adminadmin"};
        try {
            clp.parseCommandLine(args);
            fail("Expected to get a CommandValidationException saying that there was no command");
        }
        catch (CommandValidationException cve){
            assertEquals("CLI001 Invalid Command, samplecommand.", cve.getMessage());
        }
    }
    
      // test the default boolean options
    public void testDefaultBooleanOptions() throws Exception{
        String [] args = {"samplecommand", "--user", "admin", "--password", "adminadmin"};
        CommandLineParser clp = new CLP(args, vc1);
        Map options = clp.getOptionsList();
        assertEquals("terse option is by default is false", "false", (String)options.get("terse"));
        assertEquals("interactive option is by default is true", "true", (String)options.get("interactive"));
    }

	
      // test the default boolean short option
    public void testShortBooleanOptions() throws Exception{
        String [] args = {"samplecommand", "--user", "admin", "--password", "adminadmin", "-i", "-t"};
        CommandLineParser clp = new CLP(args, vc1);
        Map options = clp.getOptionsList();
        assertEquals("short option for terse if true", "true", (String)options.get("terse"));
        assertEquals("short option for interactive is true", "true", (String)options.get("interactive"));
    }

      // test the default boolean short option
    public void testLongBooleanOptions() throws Exception{
        String [] args = {"samplecommand", "--user", "admin", "--password", "adminadmin", "--interactive", "--terse"};
        CommandLineParser clp = new CLP(args, vc1);
        Map options = clp.getOptionsList();
        assertEquals("long option for terse if true", "true", (String)options.get("terse"));
        assertEquals("long option for interactive is true", "true", (String)options.get("interactive"));
    }

  
    public CommandLineParserTest(String name){
        super(name);
    }

    ValidCommand vc1;

    protected void setUp() {
        ValidOption vo1 = new ValidOption("user", "string", 1, "");
        ValidOption vo2 = new ValidOption("password", "string", 1, "");
        ValidOption vo3 = new ValidOption("host", "string", 1, "");
        ValidOption vo4 = new ValidOption("port", "string", 1, "");
        ValidOption vo5 = new ValidOption("interactive", "boolean", 3, "true");
        vo5.setShortName("i");
        ValidOption vo6 = new ValidOption("terse", "boolean", 3, "false");
        vo6.setShortName("t");
        ValidOption vo7 = new ValidOption("foo", "string", 1, "");
        vo7.setShortName("f");
        ValidOption[] validOptions = new ValidOption[] {vo3, vo4, vo6, vo7};
        ValidOption[] requiredOptions = new ValidOption[] {vo1, vo2, vo5};
        vc1 = new ValidCommand("sampleCommand",
                               "1",
                               new Vector(Arrays.asList(validOptions)),
                               new Vector(Arrays.asList(requiredOptions)),
							   new Vector(),
                               "sampleCommand");
    }
  
  

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static Test suite(){
        TestSuite suite = new TestSuite(CommandLineParserTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception {
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(CommandLineParserTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }

    private class CLP extends CommandLineParser 
    {
        CLP(ValidCommand vc){
            super(vc);
        }
        
        CLP(){
            super();
        }
        
        CLP(String [] s, ValidCommand vc) throws CommandValidationException, HelpException {
            super(s, vc);
        }
        
        HashMap getEnvironment(){
            return new HashMap();
        }
    }
    

}

