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

package com.sun.enterprise.config.serverbeans.validation;

import junit.framework.*;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.3 $
 */

public class FrameTest extends TestCase {
    public void testToString() {
        Frame f = Frame.newFrame();
        assertEquals("[ []]", f.toString());
        Frame f1 = Frame.newFrame();
        f1.inheritFrom(f);
        assertEquals("[ [ []]]", f1.toString());
        f1.put("k", "v");
        assertEquals("[k->v [ []]]", f1.toString());
        f1.put("k2", "v2");
        assertEquals("[k->v, k2->v2 [ []]]", f1.toString());
        f1.put("a", "b");
        assertEquals("[a->b, k->v, k2->v2 [ []]]", f1.toString());
        f.put("x", "y");
        assertEquals("[a->b, k->v, k2->v2 [x->y []]]", f1.toString());
        
        
    }
    
    public void testEquality() {
        Frame f = Frame.newFrame();
        Frame f1 = Frame.newFrame();
        assertEquals(f, f1);
        f1.inheritFrom(f);
        assertFalse(f.equals(f1));
        assertFalse(f1.equals(f));
    }
    
    public void testNoLoops() {
        Frame top = Frame.newFrame();
        Frame middle = Frame.newFrame(top);
        try {
            top.inheritFrom(middle);
            fail("expected expcetion indicaing loops not allowed");
        }
        catch (IllegalArgumentException iae){
            assertEquals("Inheriting from an ancestor is illegal - it causes loops!", iae.getMessage());
        }
    }
    
    public void testChangeInheritance(){
        Frame top = Frame.newFrame();
        top.put("top", "1");
        Frame middle = Frame.newFrame();
        assertEquals("${top}", middle.lookup("top"));
        middle.inheritFrom(top);
        assertEquals("1", middle.lookup("top"));
    }
    
    public void testInheritance() {
        Frame top = Frame.newFrame();
        top.put("top", "1");
        assertEquals("1", top.lookup("top"));
        Frame middle = Frame.newFrame(top);
        assertEquals("1", top.lookup("top"));
        middle.put("top", "2");
        assertEquals("1", top.lookup("top"));
        assertEquals("2", middle.lookup("top"));
        Frame bottom = Frame.newFrame(middle);
        bottom.put("top", "3");
        assertEquals("1", top.lookup("top"));
        assertEquals("2", middle.lookup("top"));
        assertEquals("3", bottom.lookup("top"));
        
    }
    
        
    public void testNoKeyFound() {
        Frame f = Frame.newFrame();
        assertEquals("${key}", f.lookup("key"));
    }
    
    public void testBasicPut() {
        Frame f = Frame.newFrame();
        f.put("x", "y");
        assertEquals("y", f.lookup("x"));
        f.put("java.vendor.url", "myUrl");
        assertEquals("myUrl", f.lookup("java.vendor.url"));
        assertEquals(System.getProperty("path.separator"), f.lookup("path.separator"));
    }
    
    public void testBasicLookup() {
        Frame f = Frame.newFrame();
        System.setProperty("a", "b");
        assertEquals("b", f.lookup("a"));
        assertEquals(System.getProperty("java.vendor.url"), f.lookup("java.vendor.url"));
        assertEquals(System.getProperty("path.separator"), f.lookup("path.separator"));
    }

    public FrameTest(String name){
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
            junit.textui.TestRunner.run(FrameTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new FrameTest(args[i]));
        }
        return ts;
    }
}
