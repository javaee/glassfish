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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import junit.framework.*;
import junit.textui.TestRunner;

/**
 *
 * @author jane.young@sun.com
 * @version $Revision: 1.7 $
 */

/**
   Execute these tests using gmake (and Ant) by:
   cd <framework>
   gmake ANT_TARGETS=CommandTest.java
*/

public class CommandTest extends TestCase {
    public void testGetDelimeterIndex(){
        assertEquals(3, testCommand.getDelimeterIndex("0123", "3", 0));
    }
    
    public void testLocalizedString(){
        assertEquals("Key not found", testCommand.getLocalizedString("a", (Object []) null));
        assertEquals("Key not found", testCommand.getLocalizedString("InvalidCommand"));
    }
    
    public void testGetSetProperty2(){
        testCommand.setProperties(new Properties());
        testCommand.setProperty("prop", "value");
        assertEquals("value", testCommand.getProperty("prop"));
    }
    
    public void testGetSetProperty1(){
        testCommand.setProperties(new Properties());
        testCommand.setProperty("prop", "value");
        assertEquals("value", testCommand.getProperty("prop"));
    }
    
    
    public void testGetSetProperty(){
        testCommand.setProperties(new Properties());
        assertEquals(null, testCommand.getProperty("prop"));
    }
    

    public void testSetGetIntegerOptions(){
        testCommand.setOption("number 1", "1");
        assertEquals(1, testCommand.getIntegerOption("number 1"));
    }
    
    public void testSetOptionsMap() {
        OptionsMap om = new OptionsMap();
        om.addCLValue("bool", "true");
        om.addEnvValue("port", "1234");
        om.addDefaultValue("secure", "false");
        om.addOptionValue("foo", "bar");
        om.addCLValue("key", "value");
        testCommand.setOptionsMap(om);
        assertEquals("sampleCommand --foo bar --key value --secure=false --bool=true --port 1234", testCommand.toString());
    }
    
    
    public void testBooleanOptions(){
        assertTrue(!testCommand.getBooleanOption("bool"));
        testCommand.setOption("bool", "true");
        assertEquals("true", testCommand.getOption("bool"));
    }
    
        
    public void testGetSetOptions(){
        testCommand.setOption("key", "value");
        assertEquals("value", testCommand.getOption("key"));
        assertNull(testCommand.getOption("foo"));
        
    }
    
    public void testConstructorAndAccessors(){
        assertEquals("sampleCommand", testCommand.getName());
        assertTrue(testCommand.getOperands().isEmpty());
        assertTrue(testCommand.getOptions().isEmpty());
        assertEquals(null, testCommand.getUsageText());
        assertNull(testCommand.getProperties(""));
        assertEquals("Key not found", testCommand.getLocalizedString("fargle"));
        assertEquals("sampleCommand", testCommand.toString());
        
    }
    
      // test replacePattern method with different sets of operands
    public void testReplacePatternOperands() throws Exception{
        final String[] operands = new String[] {"abc=xyz", "$123", "#123", "", "//////", "abc:xyz=123:456$888=\"99:99\"", "foo-bar"};
        testCommand.setOperands(new Vector(Arrays.asList(operands)));
        assertEquals("first operand should be abc=xyz", "abc=xyz", testCommand.replacePattern("{#1}"));
        assertEquals("second operand should be $123", "$123", testCommand.replacePattern("{#2}"));
        assertEquals("third operand should be #123", "#123", testCommand.replacePattern("{#3}"));
        assertEquals("fourth operand should be empty", null, testCommand.replacePattern("{#4}"));
        assertEquals("fifth operand should be //////", "//////", testCommand.replacePattern("{#5}"));
        assertEquals("sixth operand should be abc:xyz=123:456$888=\"99:99\"", 
                     "abc:xyz=123:456$888=\"99:99\"", testCommand.replacePattern("{#6}"));
        assertEquals("seventh operand should be foo-bar", "foo-bar", testCommand.replacePattern("{#7}"));
        assertEquals("all operands should be",
                     "7=foo-bar," +
                     "6=abc:xyz=123:456$888=\"99:99\"," +
                     "5=//////,"+ 
                     "4=,"+
                     "3=#123,"+
                     "2=$123,"+
                     "1=abc=xyz", testCommand.replacePattern("7={#7},"+
                                                             "6={#6},"+
                                                             "5={#5},"+
                                                             "4={#4},"+
                                                             "3={#3},"+
                                                             "2={#2},"+
                                                             "1={#1}"));
    }


      // test replacePattern method with different sets of options
    public void testReplacePatternOptions() throws Exception{
        OptionsMap options = new OptionsMap();
        options.addOptionValue("user", "admin");
        options.addCLValue("password", "adminadmin");
        options.addEnvValue("host", "fuyako");
        options.addDefaultValue("port", "4848");
        options.addOptionValue("property", "--D:${abc.xyz}=123:\"456\":88:99$01");
        options.addCLValue("empty", "");
        options.addEnvValue("foo-bar", "foobar");
        testCommand.setOptionsMap(options);

        assertEquals("first option should be admin", "admin", testCommand.replacePattern("{$user}"));
        assertEquals("second option should be adminadmin", "adminadmin", testCommand.replacePattern("{$password}"));
        assertEquals("third option should be fuyako", "fuyako", testCommand.replacePattern("{$host}"));
        assertEquals("fourth option should be 4848", "4848", testCommand.replacePattern("{$port}"));
        assertEquals("fifth option should be --D:${abc.xyz}=123:\"456\":88:99$01", 
                     "--D:${abc.xyz}=123:\"456\":88:99$01", testCommand.replacePattern("{$property}"));
        assertEquals("sixth option should be empty", null, testCommand.replacePattern("{$empty}"));
        assertEquals("seventh option should be foobar", "foobar", testCommand.replacePattern("{$foo-bar}"));
        assertEquals("all options", "user=admin,password=adminadmin,host=fuyako,port=4848,"+
                     "property=--D:${abc.xyz}=123:\"456\":88:99$01,empty=,foo-bar=foobar",
                     testCommand.replacePattern("user={$user},password={$password},host={$host},"+
                                                "port={$port},property={$property},empty={$empty},foo-bar={$foo-bar}"));

    }
	
  
    public CommandTest(String name){
        super(name);
    }

    Command testCommand = null;

    protected void setUp() {
        testCommand = new Command() {
                public void runCommand()
                    throws CommandException, CommandValidationException
                {
                }
                public boolean validateOptions() throws CommandValidationException
                {
                    return true;
                }
            };
        testCommand.setName("sampleCommand");
    }
  
  

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static Test suite(){
        TestSuite suite = new TestSuite(CommandTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception {
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(CommandTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}

