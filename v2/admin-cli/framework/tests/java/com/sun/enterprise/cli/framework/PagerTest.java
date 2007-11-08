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
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.StringReader;
import java.io.Writer;
import junit.framework.*;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.4 $
 */

public class PagerTest extends TestCase {
  public void testNegativeLinesPerPage() throws IOException {
        final Pager m = new Pager(-1, new StringReader("1\n2\n3"),
                                stdout.getWriter());
		m.nextPage();
		assertEquals("1", stdout.readLine());
		assertEquals("2", stdout.readLine());
		assertEquals("3", stdout.readLine());
		assertTrue("Output should no longer be ready", !stdout.ready());
  }
  
    public void testHasNext() throws IOException {
        final Pager m = new Pager(2, new StringReader("1\n2\n3"),
                                stdout.getWriter());
        assertTrue("Didn't expect anything in the output!",
                   !stdout.ready());
        m.nextPage();
        assertTrue(m.hasNext());
        m.nextPage();
        assertTrue(!m.hasNext());
    }
    

    public void testZeroLengthPage() throws IOException {
        final Pager m = new Pager(0, new StringReader("1\n2\n3\n4\n5\n6\n7"),
                                stdout.getWriter());
        assertTrue("Didn't expect anything in the output!",
                   !stdout.ready());
        m.nextPage();
        assertTrue("Didn't expect anything in the output!",
                   !stdout.ready());
    }
    

        
        
    public void testMultiplePages() throws IOException {
        final Pager m = new Pager(2, new StringReader("1\n2\n3\n4\n5\n6\n7"), stdout.getWriter());
        assertTrue("Didn't expect anything in the output!", !stdout.ready());
        m.nextPage();
        assertTrue("Expected something on the output!", stdout.ready());
        assertEquals("1", stdout.readLine());
        assertEquals("2", stdout.readLine());
        assertTrue("Expected end of page 1", !stdout.ready());
        m.nextPage();
        assertTrue("Expected page 2 to be ready", stdout.ready());
        assertEquals("3", stdout.readLine());
        assertEquals("4", stdout.readLine());
        assertTrue("Expected to be waiting for page 3", !stdout.ready());
        m.nextPage();
        assertEquals("5", stdout.readLine());
        assertEquals("6", stdout.readLine());
        assertTrue("Expected to be waiting for page 4", !stdout.ready());
        m.nextPage();
        assertEquals("7", stdout.readLine());
        assertTrue("Expected end of page 4", !stdout.ready());
    }
        
    public void testSinglePage() throws IOException {
        final Pager m = new Pager(2, new StringReader("1\n2\n"), stdout.getWriter());
        assertTrue("Didn't expect anything in the output!", !stdout.ready());
        m.nextPage();
        assertTrue("Expected something on the output!", stdout.ready());
        assertEquals("1", stdout.readLine());
        assertEquals("2", stdout.readLine());
        assertTrue("Expected no more output", !stdout.ready());
    }


    public PagerTest(String name){
        super(name);
    }
    Pipe stdout;
    protected void setUp() throws IOException {
        stdout = new Pipe();
    }

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(PagerTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new PagerTest(args[i]));
        }
        return ts;
    }

    class Pipe
    {
        PipedWriter pw = new PipedWriter();
        PipedReader pr = new PipedReader();
        BufferedReader br = new BufferedReader(pr);
        Writer getWriter(){ return pw; }
        BufferedReader getReader() { return br; }

        Pipe() throws IOException { pr.connect(pw); }
        String readLine() throws IOException {
            return br.readLine();
        }
        boolean ready() throws IOException {
            return br.ready();
        }

        
    }
        
        

}
