/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import junit.framework.*;
import java.util.regex.Pattern;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.1 $
 */

public class ContextPathTest extends TestCase {
//     private static final String pattern="([a-zA-Z0-9$-_.+!*'(),]|%[0-9A-Fa-f][0-9A-Fa-f]|;|:|&|=)*(/([a-zA-Z0-9$-_.+!*'(),]|%[0-9A-Fa-f][0-9A-Fa-f]|;|:|&|=)*)*";
    private static final String pattern="([a-zA-Z0-9$\\-_.+!*'(),]|%[0-9A-Fa-f][0-9A-Fa-f]|;|:|&|=)*(/([a-zA-Z0-9$\\-_.+!*'(),]|%[0-9A-Fa-f][0-9A-Fa-f]|;|:|&|=)*)*";

    public void testPrint(){
        System.err.println("\""+pattern+"\"");
    }
    
    public void test() {
        assertTrue(Pattern.matches(pattern, "foo"));
        assertTrue(Pattern.matches(pattern, ""));
        assertTrue(Pattern.matches(pattern, "&"));
        assertTrue(Pattern.matches(pattern, "/"));
        assertTrue(Pattern.matches(pattern, "/foo-bar"));
        assertTrue(Pattern.matches(pattern, "/foo/bar!path&;x='y'"));
        assertTrue(Pattern.matches(pattern, "%aF/boo"));
        assertFalse(Pattern.matches(pattern, "invalid context path"));
        assertFalse(Pattern.matches(pattern, "invalid<context>path"));
        
        
    }

    public ContextPathTest(String name){
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
            junit.textui.TestRunner.run(ContextPathTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new ContextPathTest(args[i]));
        }
        return ts;
    }
}
