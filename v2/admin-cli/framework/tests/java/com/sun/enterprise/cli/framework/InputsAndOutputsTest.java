
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.4 $
 */

public class InputsAndOutputsTest extends TestCase {
    public void testSetGetUserErrorStreamToFile() throws Exception {
        final File f = File.createTempFile("InputsAndOutputsTest_testSetGetUserOutputStreamToFile", "tmp");
        f.deleteOnExit();
        io.setErrorOutputFile(f.toString());
        final IErrorOutput uo = io.getErrorOutput();
        uo.println("foo");
        uo.close();
        final BufferedReader fr= new BufferedReader(new FileReader(f));
        assertEquals("foo", fr.readLine());
    }
    
    public void testSetGetErrorOutputStream(){
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        io.setErrorOutput(baos);
        final IErrorOutput uo = io.getErrorOutput();
        uo.print("foo");
        uo.close();
        assertEquals("foo", baos.toString());
    }
    
    public void testSettingUserInputFileWithEncoding() throws Exception{
        final String enc = "ISO-8859-1";
        final File f = File.createTempFile("InputsAndOutputsTest_testSettingUserInputFileWithEncoding", "tmp");
        f.deleteOnExit();
        final PrintWriter pw = new PrintWriter (new OutputStreamWriter(new FileOutputStream(f), enc));
        pw.println("line one");
        pw.println("line two");
        pw.close();
        io.setUserInputFile(f.toString(), enc);
        final IUserInput ui = io.getUserInput();
        assertEquals("line one", ui.getLine());
        assertEquals("line two", ui.getLine());
        assertNull(ui.getLine());
    }
        
    public void testSettingUserInputFile() throws Exception {
        final File f = File.createTempFile("InputsAndOutputsTest_testSettingUserInputFile", "tmp");
        f.deleteOnExit();
        final PrintWriter pw = new PrintWriter(new FileWriter(f));
        pw.println("line one");
        pw.println("line two");
        pw.close();
        io.setUserInputFile(f.toString());
        final IUserInput ui = io.getUserInput();
        assertEquals("line one", ui.getLine());
        assertEquals("line two", ui.getLine());
        assertNull(ui.getLine());
    }

    public void testSetGetUserInput() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream("one\ntwo".getBytes());
        final UserInput ui = new UserInput(in);
        io.setUserInput(ui);
        assertEquals(ui, io.getUserInput());
        assertEquals("one", io.getUserInput().getLine());
    }
    
    public void testSetGetUserInputEncoding() throws Exception {
        final ByteArrayInputStream in = new ByteArrayInputStream("one\ntwo".getBytes());
        io.setUserInput(in, "ISO-8859-1");
        final IUserInput ui = io.getUserInput();
        assertEquals("one", ui.getLine());
    }

    public void testSetGetUserInputStream() throws Exception {
        final ByteArrayInputStream in = new ByteArrayInputStream("one\ntwo".getBytes());
        io.setUserInput(in);
        final IUserInput ui = io.getUserInput();
        assertEquals("one", ui.getLine());
    }
    
        
    public void testSetGetUserOutputStreamToFile() throws Exception {
        final File f = File.createTempFile("InputsAndOutputsTest_testSettingOutputStreamToFile", "tmp");
        f.deleteOnExit();
        io.setUserOutputFile(f.toString());
        final IUserOutput uo = io.getUserOutput();
        uo.println("foo");
//        uo.close();
        final BufferedReader fr= new BufferedReader(new FileReader(f));
        assertEquals("foo", fr.readLine());
    }
    
        
    
    public void testSetGetUserOutputStream(){
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        io.setUserOutput(baos);
        final IUserOutput uo = io.getUserOutput();
        uo.print("foo");
//        uo.close();
        assertEquals("foo", baos.toString());
    }
    
    public void testAssignment(){
        io.setInstance(InputsAndOutputs.getInstance());
        assertEquals(io, InputsAndOutputs.getInstance());
    }
    
        
    public void testConstruction() {
        assertNotNull(io.getUserOutput());
    }

    public InputsAndOutputsTest(String name){
        super(name);
    }
    InputsAndOutputs io;
    

    protected void setUp() {
        io = InputsAndOutputs.getInstance();
    }

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(InputsAndOutputsTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new InputsAndOutputsTest(args[i]));
        }
        return ts;
    }
}
