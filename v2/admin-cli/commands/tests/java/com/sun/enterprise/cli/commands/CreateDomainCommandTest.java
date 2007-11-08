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
import junit.framework.*;
import junit.textui.TestRunner;

/**
 *
 * @author prashanth.abbagani@sun.com
 * @version $Revision: 1.1 $
 */

/**
 * Execute these tests using gmake (and Ant) by:
 * cd <commands>
 * gmake ANT_TARGETS=CommandTest.java
 */

public class CreateDomainCommandTest extends TestCase {
    
    public void testgetAdminPasswordFromCommandLine() throws Exception{
        testCommand.setOption("adminpassword", "test_password");
        String pwd = testCommand.getAdminPassword();
        assertEquals(pwd, "test_password");
    }
    
    public CreateDomainCommandTest(String name){
        super(name);
    }
    
    CreateDomainCommand testCommand = null;
    
    protected void setUp() throws Exception{
        final CLIDescriptorsReader cliDescriptorsReader = CLIDescriptorsReader.getInstance();
        ValidCommand validCommand = cliDescriptorsReader.getCommand(null);
        LocalStringsManagerFactory.setCommandLocalStringsManagerProperties(
                CLIDescriptorsReader.getInstance().getProperties());
        testCommand = new CreateDomainCommand() {
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
        final TestResult result = runner.doRun(CreateDomainCommandTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}

