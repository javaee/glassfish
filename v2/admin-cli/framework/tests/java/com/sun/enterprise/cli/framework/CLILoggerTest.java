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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import junit.framework.*;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.4 $
 */

public class CLILoggerTest extends TestCase {
    private static final String ls = System.getProperty("line.separator");
    private static final String m = "a message";
    private static final CLILogger log = CLILogger.getInstance();

    public void testPrintExceptionStackTrace(){
        log.setOutputLevel(Level.FINEST);
        final Throwable t = new Throwable(m);
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        log.printExceptionStackTrace(t);
        assertEquals(sw.toString() + ls, out.toString());
        assertEquals("", err.toString());
    }
    
        
    public void testPrintDebugMessage() {
        log.setOutputLevel(Level.FINEST);
        log.printDebugMessage(m);
        assertEquals("", err.toString());
        assertEquals(m + ls, out.toString());
    }

    public void testPrintWarning(){
        log.setOutputLevel(Level.FINEST);
        log.printWarning(m);
        assertEquals(m + ls, out.toString());
        assertEquals("", err.toString());
    }
        
    public void testPrintDetailMessage(){
        log.setOutputLevel(Level.FINEST);
        log.printDetailMessage(m);
        assertEquals(m + ls, out.toString());
        assertEquals("", err.toString());
    }
    
        
    public void testLevelsBlockOutput(){
        log.setOutputLevel(Level.SEVERE);
        log.printMessage(m);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        log.setOutputLevel(Level.INFO);
        log.printMessage(m);
        assertEquals(m + ls, out.toString());
        assertEquals("", err.toString());
    }
        
    public void testErrorLogging(){
        log.printError(m);
        assertEquals(m + ls, err.toString());
        assertEquals("", out.toString());
    }
        
    public void testBasicLogging(){
        log.printMessage(m);
        assertEquals(m + ls, out.toString());
        assertEquals("", err.toString());
    }
    public void testSetLevelDoesntWorkUnderDebug(){
        assertEquals(Level.INFO, log.getOutputLevel());
        System.setProperty("Debug", "on");
        log.setOutputLevel(Level.SEVERE);
        System.getProperties().remove("Debug");
        assertTrue(Level.SEVERE != log.getOutputLevel());
        assertEquals(Level.INFO, log.getOutputLevel());
    }
    
    public void testLevelGetSet() {
        assertEquals(Level.INFO, log.getOutputLevel());
        log.setOutputLevel(Level.SEVERE);
        assertEquals(Level.SEVERE, log.getOutputLevel());
        
    }
    
        public CLILoggerTest(String name){
        super(name);
    }
        ByteArrayOutputStream err;
        ByteArrayOutputStream out;
        InputsAndOutputs io;

    protected void setUp() {
        log.setOutputLevel(Level.INFO);
        err = new ByteArrayOutputStream();
        out = new ByteArrayOutputStream();
        io = InputsAndOutputs.getInstance();
        io.setErrorOutput(err);
        io.setUserOutput(out);
    }



    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(CLILoggerTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new CLILoggerTest(args[i]));
        }
        return ts;
    }
}
