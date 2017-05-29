/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
//import com.sun.enterprise.admin.cli.RemoteCommand;
import java.io.File;


/**
 * junit test to test RemoteCommand class
 */
public class RemoteCommandTest {
    @Test
    public void fake() {
        System.out.println("Tests Suspended Temporarily");
    }
    
  /*************
    private RemoteCommand rc = null;

    @Test
    public void getUploadFileTest() {
            //returns false  if upload option is not specified and
            //command name is not deploy
        assertFalse(rc.getUploadFile(null, "undeploy", null));
            //returns true by default if upload option is not specified
            //and command name is deploy
        assertTrue(rc.getUploadFile(null, "deploy", "RemoteCommandTest.java"));
            //returns false if upload option is not specified and
            //command name is deploy and a valid directory is provided
        assertFalse(rc.getUploadFile(null, "deploy", System.getProperty("user.dir")));
            //return false
        assertFalse(rc.getUploadFile("yes", "dummy", null));
            //return true
        assertTrue(rc.getUploadFile("true", "dummy", null));                    
    }

    @Test
    public void getFileParamTest() {
        try {
                //testing filename
            assertEquals("uploadFile=false and fileName=test", "test",
                         rc.getFileParam(false, new File("test")));
                //testing absolute path
            final String userDir = new File(System.getProperty("user.dir")).getCanonicalPath();
            assertEquals("uploadFile=false and fileName=RemoteCommandTest",
                         userDir,
                         rc.getFileParam(false, new File(System.getProperty("user.dir"))));
                //testing relative path
            assertEquals("uploadFile=false and fileName=current-directory",
                         new File(".").getCanonicalPath(),
                         rc.getFileParam(false, new File(".")));
        }
        catch(java.io.IOException ioe) {}
    }
    
    @Before
    public void setup() {
        rc = new RemoteCommand();
    }
*/
}
