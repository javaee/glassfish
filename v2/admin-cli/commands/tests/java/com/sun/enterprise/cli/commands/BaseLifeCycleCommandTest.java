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
 * Note that this test requires resources for testing. These resources
 * are construct4ed from the two files P1 & P2 located in the current
 * directory. If these file names are changed then the corresponding
 * names in this submodules build.xml file should be changed also
 */
import com.sun.enterprise.cli.framework.*;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Properties;
import junit.framework.*;
import junit.textui.TestRunner;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import java.io.File;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.util.Vector;
import java.util.HashMap;

/**
 *
 * @author prashanth.abbagani@sun.com
 * @version $Revision: 1.5 $
 */

/**
 * Execute these tests using gmake (and Ant) by:
 * cd <commands>
 * gmake ANT_TARGETS=CommandTest.java
 */

public class BaseLifeCycleCommandTest extends TestCase {
    
    public void testgetAdminUserWhenNotSet() throws Exception{
        try{
            String adminUser = testCommand.getAdminUser();
        }catch(Exception e){
            assertEquals(e.getMessage(), "CLI152 adminuser is a required option.");
        }
    }
    
    public void testgetAdminUserFromCommandLine() throws Exception{
        testCommand.setOption("adminuser", "userFromCommandLine");
        String adminUser = testCommand.getAdminUser();
        assertEquals(adminUser, "userFromCommandLine");
    }
    
    public void testgetAdminUserFromPrefsFile() throws Exception{
        final String enc = "ISO-8859-1";
        final File f = new File(System.getProperty("java.io.tmpdir"),
                testCommand.ASADMINPREFS);
        f.deleteOnExit();
        final PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(f), enc));
        pw.println("AS_ADMIN_ADMINUSER=adminuserFromPrefsFile");
        pw.close();
        System.setProperty("user.home", System.getProperty("java.io.tmpdir"));
        String adminUser = testCommand.getAdminUser();
        assertEquals(adminUser, "adminuserFromPrefsFile");
    }
    
    public void testgetAdminUserFromPrefsFileUsingUserOption() throws Exception{
        final String enc = "ISO-8859-1";
        final File f = new File(System.getProperty("java.io.tmpdir"),
                testCommand.ASADMINPREFS);
        f.deleteOnExit();
        final PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(f), enc));
        pw.println("AS_ADMIN_USER=userFromPrefsFile");
        pw.close();
        System.setProperty("user.home", System.getProperty("java.io.tmpdir"));
        String adminUser = testCommand.getAdminUser();
        assertEquals(adminUser, "userFromPrefsFile");
    }

/*        
    public void testgetDomainConfigWithVerbose() throws Exception{
        testCommand.setOption("verbose", "true");
        //assuming there is a default domain
        DomainConfig dc = testCommand.getDomainConfig(testCommand.getDomainName());
        assertEquals(dc.get(DomainConfig.K_VERBOSE), Boolean.TRUE);
        assertEquals(dc.get(DomainConfig.K_DEBUG), null);
    }

    public void testgetDomainConfigWithDebug() throws Exception{
        testCommand.setOption("debug", "true");
        //assuming there is a default domain
        DomainConfig dc = testCommand.getDomainConfig(testCommand.getDomainName());
        assertEquals(dc.get(DomainConfig.K_DEBUG), Boolean.TRUE);
        assertEquals(dc.get(DomainConfig.K_VERBOSE), null);
    }
*/
    
    public void testgetDomainNameOptionFromCommandLine() throws Exception{
        testCommand.setOption("domain", "test_domain");
        //assuming there is a default domain
        assertEquals(testCommand.getDomainName(), "test_domain");
    }
    
    public void testgetDomainNameOperandFromCommandLine() throws Exception{
        Vector operands = new Vector();
        operands.add("test_domain");
        testCommand.setOperands(operands);
        //assuming there is a default domain
    }
    
    public void testgetDomainNameWhenZero() throws Exception{
        try{
            Vector operands = new Vector();
            operands.add("UndefinedDomain");
            testCommand.setOperands(operands);
            testCommand.getDomainName();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "CLI156 Could not start the domain UndefinedDomain.");
        }
    }

    public void testgetDomainNameFromFileSystem() throws Exception{
        String domainName = "test_domain";
        String dirName = createDomainFileSystem("testgetDomainNameFromFileSystem", domainName);
        System.setProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY, dirName);
        assertEquals(testCommand.getDomainName(), domainName);
    }
    
    public void testgetDomainNameFromFileSystemWithMultipleDomains() throws Exception{
        String dirName = "";
        try{
        String domainName1 = "test_domain1";
        dirName = createDomainFileSystem("testgetDomainNameFromFileSystemWithMultipleDomains", domainName1);
        String domainName2 = "test_domain2";
        createDomainFileSystem("testgetDomainNameFromFileSystemWithMultipleDomains", domainName2);
        System.setProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY, dirName);
        String domainName = testCommand.getDomainName();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "There is more than one domain in " +
                            dirName + 
                            ". Please use operand to specify the domain.");
        }
    }
    
    public void testgetDomainNameFromFileSystemWhenNone() throws Exception{
        String dirName = "domainsDirXYZ";
        File domaindir = new File(System.getProperty("java.io.tmpdir"), dirName);
        try{
        domaindir.mkdir();
        domaindir.deleteOnExit();
        System.setProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY, domaindir.getAbsolutePath());
        String domainName = testCommand.getDomainName();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "CLI142 No domains in " + 
                                domaindir.getAbsolutePath() + ".");
        }
    }
    
    public void testgetDomainsWhenZero() throws Exception{
        String dirName = createDomainFileSystem("testgetDomainsWhenZero", null);
        System.setProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY, dirName);
        String[] domains = testCommand.getDomains();
        assertEquals(domains.length, 0);
    }

    public void testgetDomainsWhenOne() throws Exception{
        String domainName = "test_domain1";
        String dirName = createDomainFileSystem("testgetDomainsWhenOne", domainName);
        System.setProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY, dirName);
        String[] domains = testCommand.getDomains();
        assertEquals(domains[0], domainName);
    }
    
    public void testgetDomainsWhenMultiple() throws Exception{
        String domainName1 = "test_domain1";
        String dirName = createDomainFileSystem("testgetDomainsWhenMultiple", domainName1);
        String domainName2 = "test_domain2";
        createDomainFileSystem("testgetDomainsWhenMultiple", domainName2);
        System.setProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY, dirName);
        String[] domains = testCommand.getDomains();
        assertEquals(domains[0], domainName1);
        assertEquals(domains[1], domainName2);
    }

    private String createDomainFileSystem(String domainParent, String domainName) throws Exception{
        final File domainParentDir = new File(System.getProperty("java.io.tmpdir"), domainParent);
        domainParentDir.mkdir();
        domainParentDir.deleteOnExit();
        if (domainName == null) return domainParentDir.getPath();
        final File domainDir = new File(domainParentDir, domainName);
        domainDir.mkdir();
        domainDir.deleteOnExit();
        final File binDir =  new File(domainDir, PEFileLayout.BIN_DIR);
        binDir.mkdir();
        binDir.deleteOnExit();
        //System.out.println(PEFileLayout.START_SERV_OS);
        final File f = new File(binDir, PEFileLayout.START_SERV_OS);
        f.createNewFile();
        f.deleteOnExit();
        return domainParentDir.getPath();
    }

    public void testgetDomainsRootFromCommandLine() throws Exception{
        testCommand.setOption("domaindir", "test_domain");
        assertEquals(testCommand.getDomainsRoot(), "test_domain");
    }
    
    public void testgetDomainsRootFromSystemProperty() throws Exception{
        System.setProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY, "test_domain");
        assertEquals(testCommand.getDomainsRoot(), "test_domain");
    }
    
    /*
    //Donno how to unset the DOMAINS_ROOT system property
    public void testgetDomainsRootInvalid() throws Exception{
        System.out.println("Domainroot = "+ System.getProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY));
        assertEquals(testCommand.getDomainsRoot(), "test_domain");
    }
    */
    /*
    public void testgetDomainsRootWithoutSettingAny() throws Exception{
        try{
            System.setProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY, null);
            System.out.println("Domains Root = " + testCommand.getDomainsRoot());
        }
        catch(CommandException ce){
            System.out.println(ce.getMessage());
           assertEquals(ce.getMessage(), "NoDomains");
        }
    }
    */

    public void testIsSpaceInPathTrue() throws Exception{
        assertEquals(testCommand.isSpaceInPath("String has space"), true);
    }
    
    public void testIsSpaceInPathFalse() throws Exception{
        assertEquals(testCommand.isSpaceInPath("StringHasNospace"), false);
    }
    
    public void testisWindows(){
        String os = System.getProperty("os.name").toLowerCase();
        assertEquals(testCommand.isWindows(), (os.indexOf("windows") != -1) ? true : false);
    }
    public BaseLifeCycleCommandTest(String name){
        super(name);
    }
    
    BaseLifeCycleCommand testCommand = null;
    
    protected void setUp() throws Exception{
        //Properties systemProperties = new java.util.Propertis();
        //systemProperties.put("com.sun.aas.configRoot",)
        //String configProperty = SystemPropertyConstants.CONFIG_ROOT_PROPERTY;
        //System.out.println(configProperty + " = " + System.getProperty(configProperty));
        final CLIDescriptorsReader cliDescriptorsReader = CLIDescriptorsReader.getInstance();
        ValidCommand validCommand = cliDescriptorsReader.getCommand(null);
        LocalStringsManagerFactory.setCommandLocalStringsManagerProperties(
                CLIDescriptorsReader.getInstance().getProperties());
        testCommand = new BaseLifeCycleCommand() {
            public void runCommand()
            throws CommandException, CommandValidationException {
            }
            public boolean validateOptions() throws CommandValidationException {
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
        TestSuite suite = new TestSuite(BackupCommandsTest.class);
        return suite;
    }
    
    public static void main(String args[]) throws Exception {
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(BaseLifeCycleCommandTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}

