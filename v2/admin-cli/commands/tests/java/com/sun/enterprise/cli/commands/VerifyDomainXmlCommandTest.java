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
import junit.framework.*;
import junit.textui.TestRunner;
import java.util.Vector;
import java.io.File;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
/**
 *
 * @author prashanth.abbagani@sun.com
 * @version $Revision: 1.2 $
 */

/**
   Execute these tests using gmake (and Ant) by:
   cd <commands>
   gmake ANT_TARGETS=CommandTest.java
*/

public class VerifyDomainXmlCommandTest extends TestCase {

    public void testrunCommandWithNoDomainXMLFile() throws Exception{
        try{
            String domaindir = createDomainFileSystem("domains", "domain1", false);
            testCommand.setOption("domaindir", domaindir);
            Vector operands = new Vector();
            operands.add("domain1");
            testCommand.setOperands(operands);
            testCommand.runCommand();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "CLI155 domain1 does not exist.");
        }
    }

    public void testrunCommandWithEmptyDomainXMLFile() throws Exception{
        String domaindir = "";
        try{
            domaindir = createDomainFileSystem("domains", "domain1", true);
            testCommand.setOption("domaindir", domaindir);
            Vector operands = new Vector();
            operands.add("domain1");
            testCommand.setOperands(operands);
            testCommand.runCommand();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Error refreshing ConfigContext:" +
                domaindir + File.separator + "domain1" + File.separator +
                "config" + File.separator + "domain.xml" +
                "\ncause: Failed to create the XML-DOM Document.  Check your XML to make sure it is correct.\nPremature end of file.");
        }
    }

    private String createDomainFileSystem(String domainParent, String domainName, 
                                    boolean createDomainXML) throws Exception{
        final File domainParentDir = new File(System.getProperty("java.io.tmpdir"), domainParent);
        domainParentDir.mkdir();
        domainParentDir.deleteOnExit();
        final File domainDir = new File(domainParentDir, domainName);
        domainDir.mkdir();
        domainDir.deleteOnExit();
        if (!createDomainXML) return domainParentDir.getPath();
        final File configDir =  new File(domainDir, "config");
        configDir.mkdir();
        configDir.deleteOnExit();
        final File f = new File(configDir, "domain.xml");
        f.createNewFile();
        f.deleteOnExit();
        return domainParentDir.getPath();
    }

    public VerifyDomainXmlCommandTest(String name){
        super(name);
    }

    VerifyDomainXmlCommand testCommand = null;

    protected void setUp() throws Exception{
        //Properties systemProperties = new java.util.Propertis();
        //systemProperties.put("com.sun.aas.configRoot",)
        //String configProperty = SystemPropertyConstants.CONFIG_ROOT_PROPERTY;
        //System.out.println(configProperty + " = " + System.getProperty(configProperty));
        final CLIDescriptorsReader cliDescriptorsReader = CLIDescriptorsReader.getInstance();
        ValidCommand validCommand = cliDescriptorsReader.getCommand(null);
        LocalStringsManagerFactory.setCommandLocalStringsManagerProperties(
            CLIDescriptorsReader.getInstance().getProperties());
        testCommand = new VerifyDomainXmlCommand();
        testCommand.setName("sampleCommand");
    }
  
  

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static Test suite(){
        TestSuite suite = new TestSuite(VerifyDomainXmlCommandTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception {
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(VerifyDomainXmlCommandTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}

