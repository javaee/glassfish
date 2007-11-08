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


import junit.textui.TestRunner;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.framework.Test;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.5 $
 */

public class AllTest
{

    private static final Class []classes = {
        CLILoggerTest.class,
        CommandExceptionTest.class,
        CommandFactoryTest.class,
        CommandLineParserTest.class,
        CommandTest.class,
        CommandValidatorTest.class,
        CommandValidationExceptionTest.class,
        GlobalsManagerTest.class,
        InputsAndOutputsTest.class,
        LocalStringsManagerTest.class,
        LocalStringsManagerFactoryTest.class,
        NullOutputTest.class,
        OptionsMapTest.class,
        OutputTest.class,
        UserInputTest.class,
        UserOutputImplTest.class,
        ValidCommandTest.class,
        ValidCommandsListTest.class,
        ValidOptionTest.class,
        ValidPropertyTest.class
    };
    
    public static Test suite(){
        TestSuite suite = new TestSuite("cli framework tests");
        for (int i = 0; i < classes.length; i++){
            suite.addTest(new TestSuite(classes[i]));
        }
        return suite;
    }
    
        
    public static void main(String args[]) {
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(AllTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
// Local Variables:
// jde-ant-args: "-emacs -Dcover.class=All jcov.static jcov.report"
// End:
}
