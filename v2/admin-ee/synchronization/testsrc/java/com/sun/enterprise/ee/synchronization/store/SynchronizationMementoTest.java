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
package com.sun.enterprise.ee.synchronization.store;

import java.util.List;
import java.util.Iterator;
import java.io.File;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.SynchronizationResponse;
import com.sun.enterprise.ee.synchronization.store.SynchronizationMemento;
import com.sun.enterprise.ee.synchronization.inventory.InventoryMgr;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManagerBase;

import java.io.IOException;

/**
 * Synchronization memento unit tests.
 *
 * @author Nazrul Islam
 */
public class SynchronizationMementoTest extends TestCase {
   
    public SynchronizationMementoTest(String name) {
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
     * Creates a synchronization response object.
     *
     * @return  synchronization response object
     */
    private SynchronizationResponse getResponseObj(boolean dir, String path) 
            throws IOException {

        SynchronizationRequest[] requests = new SynchronizationRequest[1];

        String fileName = null;

        // sets the file name
        if (path == null) {
            File[] files = (new File("/tmp")).listFiles();
            for (int i=0; i<files.length; i++) {
                if (dir) {
                    if (files[i].isDirectory()) {
                        fileName = files[i].getAbsolutePath();
                        break;
                    }
                } else {
                    if (files[i].isFile()) {
                        fileName = files[i].getAbsolutePath();
                        break;
                    }
                }
            }
            if (fileName == null) {
                if (dir) {
                    fileName = "/tmp/test/";
                    File test = new File(fileName);
                    test.mkdirs();
                } else {
                    RandomFile rf = new RandomFile(new File("/tmp"));
                    fileName = rf.nextFile().getPath();
                }
            }
        } else {
            fileName = path;
        }

        File file = new File(fileName);

        // synchronization request
        requests[0] = new SynchronizationRequest(
            fileName, ".", file.lastModified(),
            SynchronizationRequest.TIMESTAMP_MODIFICATION_TIME, null);            
        // synchronization response
        SynchronizationResponse res = 
            new SynchronizationResponse(null, requests, 0, 0, 0);

        return res;
    }


    /**
     * Returns the backup file.
     * 
     * @param  f  synchronized file 
     * @return  backup file 
     */
    private File getBackupFile(File f) {

        File backup          = null;
        final String SUFFIX  = "_save";

        if (f.isDirectory()) {
            String dir = f.getAbsolutePath();
            if (dir.endsWith(File.separator)) {
                dir = dir.substring(0, dir.length());
            }
            backup = new File(dir+SUFFIX);
        } else {
            backup = new File(f.getAbsolutePath()+SUFFIX);
        }

        return backup;
    }

    /**
     * Tests that memento creates a backup.
     */
    private void testSaveRollback(boolean dir) {

        try {
            // synchronization memento
            SynchronizationResponse res = getResponseObj(dir, null);
            SynchronizationMemento m = new SynchronizationMemento(res);
            SynchronizationRequest[] req = res.getReply();
            SynchronizationRequest r = req[0];

            // synchronized file
            File f = new File( r.getFileName() );

            if (dir) {
                System.out.println("Memento Directory: " + f.getPath());
            } else {
                System.out.println("Memento File: " + f.getPath());
            }

            // ensures that the file exists
            assertTrue( f.exists() );

            // create a backup
            m.saveState();

            // ensures that the backup exists
            assertTrue( getBackupFile(f).exists() );
            System.out.println("Memento after backup: " 
                + getBackupFile(f).getPath());

            // rollback
            m.rollback();

            // ensures that the original file exists
            assertTrue( f.exists() );
            assertFalse( getBackupFile(f).exists() );
            System.out.println("Memento after rollback: " + f.getPath());

        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    /**
     * Tests save and rollback for a file.
     */
    public void testSaveRollbackWithFile() {
        System.out.println("--- Test:Memento.save/rollbackState(file) ---");
        testSaveRollback(false);
    }
    
    /**
     * Tests save and rollback for a directory.
     */
    public void testSaveRollbackWithDir() {
        System.out.println("--- Test:Memento.save/rollbackState(dir) ---");
        testSaveRollback(true);
    }

    /**
     * Tests the commit for two dirs with copy enabled.
     */
    public void testCommitOfDirsWithCopy() {
        System.out.println("--- Test:Memento.commit(dir, dir) - copy ON ---");
        testCommitOfDirs(true);
    }

    /**
     * Tests the commit for two dirs with copy disabled.
     */
    public void testCommitOfDirsWithoutCopy() {
        System.out.println("--- Test:Memento.commit(dir, dir) - copy OFF ---");
        testCommitOfDirs(false);
    }

    /**
     * Tests the commit for two dirs.
     *
     * @param  copyEnabled  true when copy of files
     *         between two dir is enabled
     */
    private void testCommitOfDirs(boolean copyEnabled) {

        try {
            File tmpFile = new File(tmpName);
            tmpFile.mkdirs();

            // synchronization memento
            SynchronizationResponse res = getResponseObj(true, tmpName);
            SynchronizationMemento m = new SynchronizationMemento(res);

            // populate the dir
            RandomDirTree tree = new RandomDirTree(tmpName);
            tree.populate(true);
            InventoryMgr iMgr = new InventoryMgr(tmpFile);
            List inventory = iMgr.getInventory();

            System.out.println("Number of files in directory [" +tmpName+ "]: "
                + inventory.size());

            // saves a snapshot in memento
            m.saveState();

            // populates the new dir
            RandomFileMover mover = 
                new RandomFileMover(getBackupFile(tmpFile),tmpFile,copyEnabled);
            mover.move();
            List moved = mover.getMoveList();
            List copied = mover.getCopyList();

            System.out.println("Number of files moved to directory [" 
                + getBackupFile(tmpFile) + "]: " + moved.size());
        
            System.out.println("Number of files copied to directory [" 
                + getBackupFile(tmpFile) + "]: " + copied.size());
        
            // merges the two trees
            m.commit();

            List newInventory = iMgr.getInventory();

            System.out.println("Number of files in directory [" 
                + tmpFile + "]: " + newInventory.size());

            assertEquals(inventory.size(), newInventory.size());

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
        junit.textui.TestRunner.run(SynchronizationMementoTest.suite());
    }
    */

    public static void main(String args[]) {
        junit.textui.TestRunner.run(SynchronizationMementoTest.class);
    }

    // ---- INSTANCE VARIABLE(S) - PRIVATE -------------------------
    private static String tmpName = System.getProperty("java.io.tmpdir") 
                + File.separator + "SYNC_MERGE_TEST";
}
