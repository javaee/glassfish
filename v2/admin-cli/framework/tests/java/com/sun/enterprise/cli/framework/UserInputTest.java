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

import junit.framework.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.4 $
 */

public class UserInputTest extends TestCase {
    public void testEncodingProblem() {
        try {
            ui.setEncoding("fee");
            fail("Expected an exception");
        }
        catch (IOException ie){
            assertEquals("fee", ie.getMessage());
        }
    }

    public void testEncoding() throws Exception {
        in = new ByteArrayInputStream("one\ntwo\nthree".getBytes());
        ui = new UserInput(in);
        ui.setEncoding("ISO-8859-1");
        assertEquals('o', ui.getChar());
        assertEquals("ne", ui.getLine());
        assertEquals("two", ui.getLine());
        assertEquals("three", ui.getLine());
    }
    
        
    
    public void testReading() throws Exception {
        in = new ByteArrayInputStream("one\ntwo\nthree".getBytes());
        ui = new UserInput(in);
        assertEquals('o', ui.getChar());
        assertEquals("ne", ui.getLine());
        assertEquals("two", ui.getLine());
        assertEquals("three", ui.getLine());
    }
    
        
    public void testSimpleCase() throws IOException {
        assertTrue(ui.isInteractive());
        ui.close();
    }

    public UserInputTest(String name){
        super(name);
    }

    ByteArrayInputStream in;
    
    UserInput ui;
    

    protected void setUp() {
        in = new ByteArrayInputStream(new byte[0]);
        ui = new UserInput(in);    
    }

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(UserInputTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new UserInputTest(args[i]));
        }
        return ts;
    }
}
