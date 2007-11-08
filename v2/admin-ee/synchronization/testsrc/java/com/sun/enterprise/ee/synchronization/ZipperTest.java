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
package com.sun.enterprise.ee.synchronization;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import com.sun.enterprise.ee.util.zip.Zipper;
import com.sun.enterprise.ee.util.zip.Unzipper;

/**
 * Class Zipper
 *
 */
public class ZipperTest extends TestCase {

    public ZipperTest(String name) {
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testEmptyZip() {        
        try {
            String resultDirectory = Utils.getTemporaryDirectory("testEmptyZip");
            
            //Zip up the current directory and write it the testZipFile.zip
            Zipper z = new Zipper(".");        
            byte[] bytes = z.createZipBytesFromDirectory("jxmp463retuyytyl");
            //Unzip the newly created directory and write it to the 
            //testZipper subdirectory
            assertEquals(bytes.length, 0);            
            Unzipper uz = new Unzipper(resultDirectory);
            uz.writeZipBytes(bytes);                                              
            assertTrue(!new File(resultDirectory).exists());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }        
    }
    
    public void testZipBytes() {         
        try {
            String resultDirectory = Utils.getTemporaryDirectory("testZipBytes");
            //Zip up the current directory and write it the testZipFile.zip
            Zipper z = new Zipper(".");        
            byte[] bytes = z.createZipBytesFromDirectory(".");            
            //Unzip the newly created directory and write it to the 
            //testZipper subdirectory
            Unzipper uz = new Unzipper(resultDirectory);
            uz.writeZipBytes(bytes);           
            //Check to make sure that the two match
            assertTrue(Utils.areIdentical(".", resultDirectory));            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }        
    }

    public void testZipFile() {         
        try {
            String zipName = System.getProperty("java.io.tmpdir") +
                File.separator + "testZipFile.zip";
            File f = new File(zipName);
            String resultDirectory = Utils.getTemporaryDirectory("testZipFile");
            //Zip up the current directory and write it the testZipFile.zip
            Zipper z = new Zipper(".");        
            z.createZipFileFromDirectory(".", zipName);            
            //Unzip the newly created directory and write it to the 
            //testZipper subdirectory
            Unzipper uz = new Unzipper(resultDirectory);
            uz.writeZipFile(zipName);              
            //Check to make sure that the two match            
            assertTrue(Utils.areIdentical(".", resultDirectory));
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(ZipperTest.class);
    }
}
