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

import java.util.HashMap;
import java.util.Vector;
import junit.framework.*;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.6 $
 */

public class CommandFactoryTest extends TestCase {
    public void testInvalidClass(){
        final ValidCommand vc = new ValidCommand();
        vc.setName("vc");
        vc.setNumberOfOperands("three");
        vc.setUsageText("vc 1 2 3");
        vc.setClassName("foobar");
        try {
            CommandFactory.createCommand(vc, new OptionsMap(), new Vector());
            fail("Expected a CommandValidationException indicating that the class could not be found");
        }
        catch (CommandValidationException cve){
            assertEquals("CLI002 Unable to create command object, vc.", cve.getMessage());
            assertEquals(ClassNotFoundException.class, cve.getCause().getClass());
            assertEquals("foobar", cve.getCause().getMessage());        }
    }
        
    public void testNullClass() {
        final ValidCommand vc = new ValidCommand();
        vc.setName("vc");
        vc.setNumberOfOperands("three");
        vc.setUsageText("vc 1 2 3");
        try {
            CommandFactory.createCommand(vc, new OptionsMap(), new Vector());
            fail("Expected a CommandValidationException indicating that no classname was given");
        }
        catch (CommandValidationException cve){
            assertEquals("CLI001 Invalid Command, vc.", cve.getMessage());
        }
    }
    
    public void testSimpleCommandCreation() throws Exception {
        final ValidCommand vc = new ValidCommand();
        vc.setName("vc");
        vc.setNumberOfOperands("three");
        vc.setUsageText("vc 1 2 3");
        vc.setClassName("com.sun.enterprise.cli.framework.TestCommand");
        assertNotNull(vc.getClassName());
        final Command c = CommandFactory.createCommand(vc, new OptionsMap(), new Vector());
        assertNotNull(c);
    }

    public void testNoArgsConstructor(){
        final CommandFactory c = new CommandFactory();
    }
    
    public CommandFactoryTest(String name){
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
            junit.textui.TestRunner.run(CommandFactoryTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new CommandFactoryTest(args[i]));
        }
        return ts;
    }
}

class TestCommand extends Command
{
    public void runCommand() throws com.sun.enterprise.cli.framework.CommandException, com.sun.enterprise.cli.framework.CommandValidationException{}
    public boolean validateOptions() throws com.sun.enterprise.cli.framework.CommandValidationException{
        return true;
    }
    
}

