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
import java.io.IOException;
import junit.framework.*;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.4 $
 */

public class UserOutputImplTest extends TestCase {
    public void testNullCOnstruction(){
        final UserOutputImpl ui = new UserOutputImpl(null, true);
        ui.print("message");
        ui.print((Object) "object");
        ui.println("eol");
        ui.println((Object) "eol2");
        ui.flush();
        ui.close();
    }
        
    public void testBasicConstruction() throws IOException {
        final ByteArrayOutputStreamWithClosure s = new ByteArrayOutputStreamWithClosure();
        final UserOutputImpl ui = new UserOutputImpl(s, true);
        ui.print("message");
        ui.print((Object) "object");
        ui.println("eol");
        ui.println((Object) "eol2");
        ui.flush();
        ui.close();
        assertEquals("messageobjecteol"+System.getProperty("line.separator") +"eol2"+System.getProperty("line.separator"), s.toString());
        assertTrue(s.isClosed());
    }

    public UserOutputImplTest(String name){
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
            junit.textui.TestRunner.run(UserOutputImplTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new UserOutputImplTest(args[i]));
        }
        return ts;
    }
}
class ByteArrayOutputStreamWithClosure extends ByteArrayOutputStream 
{
    boolean closed = false;
    public void close() throws IOException {
        super.close();
        closed = true;
    }
    public boolean isClosed(){
        return closed;
    }
}


