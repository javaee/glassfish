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
package com.sun.enterprise.cli.commands;

/**
   Note that this test requires resources for testing. These resources
   are construct4ed from the two files P1 & P2 located in the current
   directory. If these file names are changed then the corresponding
   names in this submodules build.xml file should be changed also
*/
import com.sun.enterprise.cli.framework.*;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Properties;
import java.util.Vector;
import junit.framework.*;
import junit.textui.TestRunner;
import java.io.File;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Map;
import java.util.Hashtable;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.appserv.management.util.misc.ExceptionUtil;

/**
 *
 * @author prashanth.abbagani@sun.com
 * @version $Revision: 1.4 $
 */

/**
   Execute these tests using gmake (and Ant) by:
   cd <commands>
   gmake ANT_TARGETS=CommandTest.java
*/

public class S1ASCommandTest extends TestCase {
    public void testCheckForFileExistence() throws Exception{
        final File f = File.createTempFile("S1ASCommandTest_testCheckForFileExistence", ".tmp");
        f.deleteOnExit();
        String tempdir = System.getProperty("java.io.tmpdir");
        assertTrue(testCommand.checkForFileExistence(tempdir, f.getName()).exists());
    }

    public void testCheckForFileExistenceWhenNoFile() throws Exception{
        String tempdir = System.getProperty("java.io.tmpdir");
        try{
            File f = testCommand.checkForFileExistence(tempdir, "FileDoesNotExist");
        }catch (Exception e){
            assertEquals(e.getMessage(), "CLI146 FileDoesNotExist does not exist in the file system or read permission denied.");
        }
    }

    public void testCreatePropertiesParam() throws Exception{
        String propStr = "name=value";
        Properties props = testCommand.createPropertiesParam(propStr);
        assertEquals(props.getProperty("name"),"value");
    }

    public void testCreatePropertiesParamWithEscapes() throws Exception{
        String propStr = "user=dbuser:password=dbpassword:DatabaseName=jdbc\\:pointbase:server=http\\://localhost\\:9292";
        Properties props = testCommand.createPropertiesParam(propStr);
        assertEquals(props.getProperty("user"),"dbuser");
        assertEquals(props.getProperty("password"), "dbpassword");
        assertEquals(props.getProperty("DatabaseName"), "jdbc:pointbase");
        assertEquals(props.getProperty("server"), "http://localhost:9292");
    }
    
    public void testCreatePropertiesParamInvalid() throws Exception{
        String propStr = "name1=value1:value2";
        try{
            Properties props = testCommand.createPropertiesParam(propStr);
        }catch (Exception e){
            assertEquals(e.getMessage(), "CLI131 Invalid property syntax.");
        }
    }

    public void testCreateStringArrayParam() throws Exception{
        String paramStr = "value1:value2";
        String[] params = testCommand.createStringArrayParam(paramStr);
        assertEquals(params[0],"value1");
        assertEquals(params[1],"value2");
    }

    public void testCreateStringArrayParamWithEscapes() throws Exception{
        String paramStr = "-XX\\:NewRatio=2";
        String[] params = testCommand.createStringArrayParam(paramStr);
        assertEquals(params[0],"-XX:NewRatio=2");
    }

    public void testDisplayExceptionMessage(){
        NullPointerException npe = new NullPointerException("a null pointer");
        try{
            testCommand.displayExceptionMessage(npe);
        }
        catch (CommandException ce){
            assertEquals(npe, ExceptionUtil.getRootCause(ce));
        }
    }

    public void testgetHost(){
        testCommand.setOption("host", "localhost");
        assertEquals(testCommand.getHost(),"localhost");
    }

    public void testgetInteractiveOptionWhenFalse() throws Exception{
        final CLIDescriptorsReader cliDescriptorsReader = CLIDescriptorsReader.getInstance();
        ValidCommand validCommand = cliDescriptorsReader.getCommand(null);
        LocalStringsManagerFactory.setCommandLocalStringsManagerProperties(
            CLIDescriptorsReader.getInstance().getProperties());
        
        testCommand.setOption("interactive", "false");
        try{
        testCommand.getInteractiveOption("test", "test");
        }
        catch (CommandValidationException cve){
            assertEquals(cve.getMessage(), "CLI152 test is a required option.");
        }
    }

    public void testgetObjectName() throws Exception{
        Vector propertyValues = new Vector();
        propertyValues.add("test_objectname");
        testCommand.setProperty(testCommand.OBJECT_NAME, propertyValues);
        assertEquals(testCommand.getObjectName(), "test_objectname");
    }

    public void testgetOperationName() throws Exception{
        Vector propertyValues = new Vector();
        propertyValues.add("testMethod");
        testCommand.setProperty(testCommand.OPERATION, propertyValues);
        assertEquals(testCommand.getOperationName(), "testMethod");
    }

    public void testgetTypesInfo() throws Exception{
        Vector propertyValues = new Vector();
        for (int i=0; i<=3; i++){
            propertyValues.add("java.lang.String");
        }
        testCommand.setProperty(testCommand.PARAM_TYPES, propertyValues);
        String[] types = testCommand.getTypesInfo();
        for (int i=0; i<types.length; i++)
            assertEquals(types[i], "java.lang.String");
    }

    public void testreplacePattern() throws Exception{
        
        Vector typesValues = new Vector();
        typesValues.add("java.lang.String");
        testCommand.setProperty(testCommand.PARAM_TYPES, typesValues);
        testCommand.setOption("testOption", "test");
        Vector paramValues = new Vector();
        paramValues.add("{$testOption}");
        testCommand.setProperty(testCommand.PARAMS, paramValues);
        Object[] params = testCommand.getParamsInfo();
        assertEquals((String)params[0], "test");
    }

    public void testgetParamsInfo() throws Exception{
        
        Vector typesValues = new Vector();
        for (int i=0; i<3; i++){
            typesValues.add("java.lang.String");
        }
        testCommand.setProperty(testCommand.PARAM_TYPES, typesValues);
        
        Vector paramValues = new Vector();
        for (int i=0; i<3; i++){
            paramValues.add("x"+i);
        }
        testCommand.setProperty(testCommand.PARAMS, paramValues);
        Object[] params = testCommand.getParamsInfo();
        for (int i=0; i<params.length; i++)
            assertEquals((String)params[i], "x"+i);
    }

    public void testgetParamsInfoInvalid() throws Exception{
        //#of param typess is not equal to #of param values
        Vector typesValues = new Vector();
        typesValues.add("java.lang.String");
        typesValues.add("java.lang.String");
        testCommand.setProperty(testCommand.PARAM_TYPES, typesValues);
        
        Vector paramValues = new Vector();
        for (int i=0; i<3; i++){
            paramValues.add("x"+i);
        }
        testCommand.setProperty(testCommand.PARAMS, paramValues);
        try{
            Object[] params = testCommand.getParamsInfo();
        }catch (CommandException ce){
            assertEquals(ce.getMessage(), 
                    "CLI174 Error in CLIDescriptor.xml -- The number of params doesn''t match the number of param-types.  Solution: edit this command in CLIDescriptor.xml.");
        }
    }

    public void testgetValuesFromASADMINPREFS() throws Exception{
        final String enc = "ISO-8859-1";
        final File f = new File(System.getProperty("java.io.tmpdir"),
                                testCommand.ASADMINPREFS);
        f.deleteOnExit();
        final PrintWriter pw = new PrintWriter (
                new OutputStreamWriter(new FileOutputStream(f), enc));
        pw.println("AS_ADMIN_PASSWORD=test_password");
        pw.close();
        System.setProperty("user.home", System.getProperty("java.io.tmpdir"));
        assertEquals(testCommand.getValuesFromASADMINPREFS(testCommand.PASSWORD), 
                        "test_password");
    }
    
    public void testgetPasswordWhenNotSet() throws Exception{
        String pwd = testCommand.getPassword(testCommand.PASSWORD, null, null, 
                                    false, false, false, false, null, null, false, 
                                    false, false, false);
        assertEquals(pwd, null);
    }
    
    public void testgetPasswordFromPrefsFile() throws Exception{
        final String enc = "ISO-8859-1";
        final File f = new File(System.getProperty("java.io.tmpdir"),
                                testCommand.ASADMINPREFS);
        f.deleteOnExit();
        final PrintWriter pw = new PrintWriter (
                new OutputStreamWriter(new FileOutputStream(f), enc));
        pw.println("AS_ADMIN_PASSWORD=test_password");
        pw.close();
        System.setProperty("user.home", System.getProperty("java.io.tmpdir"));
        //set the readPrefsFile & readPasswordOptionFromPrefs to true
        String pwd = testCommand.getPassword(testCommand.PASSWORD, null, null, 
                                    false, true, true, false, null, null, false, 
                                    false, true, false);
        assertEquals(pwd, "test_password");
    }
    
    public void testgetPasswordFromCommandLine() throws Exception{
        testCommand.setOption("password", "test_password");
        String pwd = testCommand.getPassword(testCommand.PASSWORD, null, null, 
                                    true, false, false, false, null, null, false, 
                                    false, true, false);
        assertEquals(pwd, "test_password");
    }
    
    public void testgetPort() throws Exception{
        testCommand.setOption(testCommand.PORT, "4848");
        assertEquals(testCommand.getPort(), 4848);
    }
    
    public void testgetPortInvalid() throws Exception{
        try{
            testCommand.setOption(testCommand.PORT, "xyz");
            testCommand.getPort();
        }catch (Exception e){
            assertEquals(e.getMessage(), "CLI136 Port xyz should be a numeric value.");
        }
    }
    
    public void testgetReturnType() throws Exception{
        Vector propertyValues = new Vector();
        propertyValues.add("java.lang.String");
        testCommand.setProperty(testCommand.RETURN_TYPE, propertyValues);
        assertEquals(testCommand.getReturnType(), "java.lang.String");
    }

    public void testgetUserWhenNotSet() throws Exception{
        try{
            String adminUser = testCommand.getUser();
        }catch(Exception e){
            assertEquals(e.getMessage(), "CLI152 user is a required option.");
        }
    }
    
    public void testgetUserFromPrefsFile() throws Exception{
        final String enc = "ISO-8859-1";
        final File f = new File(System.getProperty("java.io.tmpdir"),
                                testCommand.ASADMINPREFS);
        f.deleteOnExit();
        final PrintWriter pw = new PrintWriter (
                new OutputStreamWriter(new FileOutputStream(f), enc));
        pw.println("AS_ADMIN_USER=admin");
        pw.close();
        System.setProperty("user.home", System.getProperty("java.io.tmpdir"));
        //set the readPrefsFile & readPasswordOptionFromPrefs to true
        String user = testCommand.getUser();
        assertEquals(user, "admin");
    }
    
    public void testgetUserFromCommandLine() throws Exception{
        testCommand.setOption("user", "admin");
        String user = testCommand.getUser();
        assertEquals(user, "admin");
    }
    
    public void testisPasswordValid() throws Exception{
        boolean isValid = testCommand.isPasswordValid("eightOrMoreCharacters");
        assertEquals(isValid, true);
        isValid = testCommand.isPasswordValid("7chars");
        assertEquals(isValid, false);
    }
    
    public void testsetLoggerLevel() throws Exception{
        testCommand.setOption(testCommand.TERSE, "false");
        testCommand.setLoggerLevel();
        assertEquals(CLILogger.getInstance().getOutputLevel(),java.util.logging.Level.FINE);
        testCommand.setOption(testCommand.TERSE, "true");
        testCommand.setLoggerLevel();
        assertEquals(CLILogger.getInstance().getOutputLevel(),java.util.logging.Level.INFO);
    }
    
    public S1ASCommandTest(String name){
        super(name);
    }

    S1ASCommand testCommand = null;

    protected void setUp() throws Exception{
        //Properties systemProperties = new java.util.Propertis();
        //systemProperties.put("com.sun.aas.configRoot",)
        String configProperty = SystemPropertyConstants.CONFIG_ROOT_PROPERTY;
        //System.out.println(configProperty + " = " + System.getProperty(configProperty));
        final CLIDescriptorsReader cliDescriptorsReader = CLIDescriptorsReader.getInstance();
        ValidCommand validCommand = cliDescriptorsReader.getCommand(null);
        LocalStringsManagerFactory.setCommandLocalStringsManagerProperties(
            CLIDescriptorsReader.getInstance().getProperties());
        testCommand = new S1ASCommand() {
                public void runCommand()
                    throws CommandException, CommandValidationException
                {
                    setProperties(new Properties());
                }
                public boolean validateOptions() throws CommandValidationException
                {
                    return true;
                }
            };
        testCommand.setName("sampleCommand");
        testCommand.runCommand();
    }
  
  

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static Test suite(){
        TestSuite suite = new TestSuite(S1ASCommandTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception {
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(S1ASCommandTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}

