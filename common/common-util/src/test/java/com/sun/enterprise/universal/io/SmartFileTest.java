/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal.io;

import com.sun.enterprise.util.OS;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author bnevins
 */
public class SmartFileTest {

    public SmartFileTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of sanitize method, of class SmartFile.
     */
    @Test
    public void sanitize() {
        for(String path : FILENAMES) {
            System.out.println(path + " --> " + SmartFile.sanitize(path));
        }
    }
    /**
     * Test of sanitizePaths method, of class SmartFile.
     */
    @Test
    public void sanitizePaths() {
        String sep = File.pathSeparator;

        // where are we now?
        String here = SmartFile.sanitize(".");

        String cp1before = "/a/b/c" + sep + "qqq"+ sep + "qqq"+ sep + "qqq"+ sep + "qqq"+ sep + "qqq"+ sep + "./././qqq/./." + sep + "z/e";
        String cp1expected = "/a/b/c" + sep + here + "/qqq" + sep + here + "/z/e";

        if(sep.equals(";")) {
            // Windows -- drive letter is needed...
            String drive = here.substring(0, 2);
            cp1expected = drive + "/a/b/c;" + here + "/qqq;" + here + "/z/e";
        }

        System.out.println("******** Sanitized ClassPath Test ******************");

        String cp = System.getProperty("java.class.path");
        System.out.println("Current Classpath: " + cp + "\nSanitized Classpath: "
                + SmartFile.sanitizePaths(cp));

        System.out.println("here: " + here);
        System.out.println("before: " + cp1before);
        System.out.println("after: " + SmartFile.sanitizePaths(cp1before));
        System.out.println("Expected: " + cp1expected);
        System.out.println("***************************************************");

	// All the QL and devtests are very unstable right now.
	// I'll uncomment this later whn things are rock solid.
        //assertEquals(cp1expected, SmartFile.sanitizePaths(cp1before));
    }
    /**
     * Test of sanitizePaths method, of class SmartFile.
     */
    @Test
    public void sanitizePaths2() {
        String sep = File.pathSeparator;
        if(OS.isWindows()) {
            String badPaths="c:/xyz;\"c:\\a b\";c:\\foo";
            String convert = SmartFile.sanitizePaths(badPaths);
            String expect = "C:/xyz;C:/a b;C:/foo";
            assertEquals(convert, expect);
        }
        else {
            String badPaths="/xyz:\"/a b\":/foo";
            String convert = SmartFile.sanitizePaths(badPaths);
            String expect = "/xyz:/a b:/foo";
            assertEquals(convert, expect);
        }
    }

    private static final String[] FILENAMES = new String[]
    {
        "c:/",
        "c:",
        "",
        "\\foo",
        "/",
        "/xxx/yyy/././././../yyy",
        "/x/y/z/../../../temp",
        //"\\\\",
        //"\\\\foo\\goo\\hoo",
        "x/y/../../../..",
        "/x/y/../../../..",
		"/./../.././../",
		"/::::/x/yy",
    };
}
