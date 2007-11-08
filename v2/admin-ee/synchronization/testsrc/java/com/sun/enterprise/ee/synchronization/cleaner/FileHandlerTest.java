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
package com.sun.enterprise.ee.synchronization.cleaner;

import java.io.File;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.ee.synchronization.store.RandomFile;

import java.util.logging.Logger;
import com.sun.logging.ee.EELogDomains;

/**
 * Synchronization file handler unit tests.
 *
 * @author Nazrul Islam
 */
public class FileHandlerTest extends TestCase {
   
    public FileHandlerTest(String name) {
        super(name);        
    }

    protected void setUp() {
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
    }

    protected void tearDown() {
        File root  = new File(tmpName);
        FileUtils.whack(root);
    }

    /**
     * Tests the remove functionality of file handler.
     */
    public void testRemove() {
        System.out.println("-- Cleaner:FileHandler.remove() Test --");

        try {
            // current time
            long currentTime = System.currentTimeMillis();

            // java temp file as trash
            File trash = new File(System.getProperty("java.io.tmpdir")); 

            // temp file for this test
            File tmpFile = new File(tmpName);
            tmpFile.mkdirs();

            // new test file 
            RandomFile rf = new RandomFile(tmpFile);
            File f = rf.nextFile();
            assertTrue( f.exists() );
            System.out.println("File: " + f.getPath());

            FileHandler handler = new FileHandler(f, trash);
            handler.remove();

            // file should not be removed
            assertTrue( f.exists() );
            System.out.println("File exists after 1st removal: " + f.getPath());

            // set the time stamp greater than 30 mins old
            long diff = currentTime - DEF_WAIT_PERIOD;
            System.out.println("Last Modified Orig: " + f.lastModified());
            f.setLastModified(diff);
            handler.remove();
            System.out.println("Set Last Modified To: " + f.lastModified());

            // file should be removed
            assertFalse( f.exists() );
            System.out.println("File is removed after setting last modified");

        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }
    
    /*
    public static TestSuite suite() {
        //To run all tests
        return new TestSuite(SynchronizationMementoTest.class);
        //To run a subset of the tests
        TestSuite suite = new TestSuite();
        suite.addTest(new SynchronizationMementoTest("testEmptySync"));       
        suite.addTest(new SynchronizationMementoTest("testEmptySync2"));       
        return suite;
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(FileHandlerTest.suite());
    }
    */

    public static void main(String args[]) {
        junit.textui.TestRunner.run(FileHandlerTest.class);
    }

    // ---- INSTANCE VARIABLE(S) - PRIVATE -------------------------
    private static String tmpName = System.getProperty("java.io.tmpdir") 
                + File.separator + "SYNC_CLEANER_TEST";
    private static final long DEF_WAIT_PERIOD = 1800000;
}
