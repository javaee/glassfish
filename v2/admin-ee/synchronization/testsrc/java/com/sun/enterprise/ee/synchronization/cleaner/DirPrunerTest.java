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
import java.util.List;
import java.util.ArrayList;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.ee.synchronization.store.RandomFile;
import com.sun.enterprise.ee.synchronization.inventory.InventoryMgr;

import java.util.logging.Logger;
import com.sun.logging.ee.EELogDomains;

/**
 * Synchronization directory pruner unit tests.
 *
 * @author Nazrul Islam
 */
public class DirPrunerTest extends TestCase {
   
    public DirPrunerTest(String name) {
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
     * Tests the remove functionality of dir pruner.
     */
    public void testCRRemoval() {
        System.out.println("\n-- Cleaner:DirPruner.prune() Test [Not removed from CR] --");
        testRemoval(true, false);
    }

    /**
     * Tests the remove functionality of dir pruner.
     */
    public void testRemoval() {
        System.out.println("\n-- Cleaner:DirPruner.prune() Test [Removed from CR] --");
        testRemoval(true, true);
    }

    /**
     * When updateTS is true, the test sets an older last updated time stamp
     * of the attempted file.
     */
    private void testRemoval(boolean updateTS, boolean testCR) {

        try {
            // java temp file as trash
            File trash = new File(System.getProperty("java.io.tmpdir")); 
            File root  = new File(tmpName);

            // temp dir for synchronization cleaner test 
            File tmpFile = new File(tmpName);
            tmpFile.mkdirs();

            RandomFile rf = new RandomFile(tmpFile);

            // creates first file
            File f1 = rf.nextFile();
            assertTrue( f1.exists() );
            System.out.println("File: " + f1.getPath());

            // creates second file
            File f2 = rf.nextFile();
            assertTrue( f2.exists() );
            System.out.println("File: " + f2.getPath());

            // saves inentory
            InventoryMgr iMgr = new InventoryMgr(root);
            List inventory = iMgr.getInventory();

            // sets up GC removal list
            List gcTarget = new ArrayList();
            String file = null;
            for (int i=0; i<inventory.size(); i++) {
                String t = (String) inventory.get(i);
                File tFile = new File(root, t);
                if (tFile.getPath().equals(f1.getPath())) {
                    file = t;
                    if (testCR) {
                        // remove from the CR 
                        // inventory.set(i, t+"-TARGET");
                        inventory.remove(i);
                    }
                    break;
                }
            }
            assertTrue(file != null);
            assertTrue(f1.getPath().endsWith(file));
            gcTarget.add(file);
            iMgr.saveGCTargetList(gcTarget);

            // sets up CR inventory
            iMgr.saveInventory(inventory);

            // updated inventory list
            List uInventory = iMgr.getInventory();
            assertTrue(uInventory.size() > 1);

            // updates the time stamp
            System.out.println("Attempting to remove file: " + f1.getPath());
            assertTrue(f1.exists());
            if (updateTS) {
                long currentTime = System.currentTimeMillis();
                long diff = currentTime - DEF_WAIT_PERIOD;
                System.out.println("Last Modified Orig: "+f1.lastModified());
                f1.setLastModified(diff);
                System.out.println("Last Modified Updated: "+f1.lastModified());
            }

            // calls pruner
            DirPruner pruner = new DirPruner(root, trash, inventory);
            pruner.prune();

            // check asserts
            if (updateTS) {
                if (testCR) {
                    assertTrue(!f1.exists());
                    System.out.println(f1.getPath() + " is REMOVED.");
                } else {
                    assertTrue(f1.exists());
                    System.out.println(f1.getPath() + " is NOT removed.");
                }
            } else {
                assertTrue(f1.exists());
                System.out.println(f1.getPath() + " is NOT removed.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    /**
     * Tests the remove functionality of dir pruner.
     */
    public void testGCRemoval() {
        System.out.println("\n-- Cleaner:DirPruner.prune() Test [Not in GC list] --");
        testRemoval(false, false);
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
        junit.textui.TestRunner.run(DirPrunerTest.suite());
    }
    */

    public static void main(String args[]) {
        junit.textui.TestRunner.run(DirPrunerTest.class);
    }

    // ---- INSTANCE VARIABLE(S) - PRIVATE -------------------------
    private static String tmpName = System.getProperty("java.io.tmpdir") 
                + File.separator + "SYNC_CLEANER_TEST";
    private static final long DEF_WAIT_PERIOD = 1800001;
}
