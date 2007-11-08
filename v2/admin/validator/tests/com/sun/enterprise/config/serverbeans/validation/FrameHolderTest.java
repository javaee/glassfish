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

public class FrameHolderTest extends TestCase {
    public void testEquality(){
        FrameHolder fh1 = new FrameHolder();
        FrameHolder fh2 = new FrameHolder();
        assertEquals(fh1, fh2);
        assertFalse(fh1.equals(new Object()));
        assertFalse(fh1.equals(null));
        assertEquals(fh1, fh1);
        fh1.getDomainFrame().put("k", "v");
        assertFalse(fh1.equals(fh2));
        assertFalse(fh2.equals(fh1));
    }
    
    public void testBasics() {
        FrameHolder fh = new FrameHolder();
        assertNotNull(fh.getDomainFrame());
        fh.getDomainFrame().put("k", "v");
        assertNotNull(fh.getConfigFrame("config0"));
        fh.getConfigFrame("config0").inheritFrom(fh.getDomainFrame());
        assertEquals("v", fh.getConfigFrame("config0").lookup("k"));
        assertNotNull(fh.getConfigFrame("config1"));
        assertFalse(fh.getConfigFrame("config0").equals(fh.getConfigFrame("config1")));
        assertNotNull(fh.getServerFrame("frame0"));
        fh.getServerFrame("frame0").inheritFrom(fh.getConfigFrame("config0"));
        assertEquals("v", fh.getServerFrame("frame0").lookup("k"));
        assertNotNull(fh.getClusterFrame("frame0"));
        fh.getConfigFrame("config1").put("k", "config");
        fh.getClusterFrame("frame0").inheritFrom(fh.getConfigFrame("config1"));
        fh.getServerFrame("frame1").inheritFrom(fh.getClusterFrame("frame0"));
        assertEquals("config", fh.getServerFrame("frame1").lookup("k"));
    }

    public FrameHolderTest(String name){
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
            junit.textui.TestRunner.run(FrameHolderTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new FrameHolderTest(args[i]));
        }
        return ts;
    }
}
