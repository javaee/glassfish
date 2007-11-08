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
package com.sun.enterprise.ee.util.zip;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;


/**
 * Class UnZipperTest
 *
 */
public class UnZipperTest extends TestCase {

    public UnZipperTest(String name) {
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testMkDirs() {        
        try {
            //Unzip the newly created directory and write it to the 
            //testZipper subdirectory
            Unzipper uz = new Unzipper("/tmp/");
            uz.safeMkDirs(new File("/tmp/./a/b"));
            //String dir = "/export/install/mar-30/nodeagents/ee-synchronization-agent/ee-synchronization-server-1/docroot/./samples/ejb/bmp/apps/robean/docs/";
            //uz.safeMkDirs(new File(dir));
            //BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dir + "index.html"));
            //BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(uz.normalizePath(dir) + "index.html"));
            uz.safeMkDirs(new File("/tmp/a/b/./c"));
            uz.safeMkDirs(new File("/tmp/a/b/./c/d"));
            uz.safeMkDirs(new File("/tmp/a/b/./c/e/."));
            assertTrue(new File("/tmp/a/b/c/e").exists());

            String result = null;


            result = new String("/tmp/a/b/./c/e/.");
            System.out.println(" result of normalize on " + result + " is " + uz.normalizePath(result));
            assertTrue( uz.normalizePath(result).equals("/tmp/a/b/c/e/."));
            result = new String("\\tmp\\a\\b\\.\\c\\e\\.");
            System.out.println(" result of normalize using NT routine on " + result + " is " + normalizeNTPath(result));
            assertTrue( normalizeNTPath(result).equals("\\tmp\\a\\b\\c\\e\\."));
            new File("/tmp/a").delete();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }        
    }

    // TMP test NT situation
    String normalizeNTPath(String path) {

        path = path.replace('\\', '/');
        path = path.replaceAll( "/\\./", "/");
        return path.replace( '/', '\\');
    }

    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(UnZipperTest.class);
    }
}
