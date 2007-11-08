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
import java.util.Iterator;
import java.util.Arrays;
import com.sun.enterprise.util.io.FileUtils;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.ee.synchronization.inventory.InventoryMgr;

/**
 * Responsible for removing stale files in a directory.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class DirPruner extends Thread {

    /**
     * Constructor.
     *
     * @param  dir    root directory
     * @param  trash  trash where files are moved before they are deleted
     * @param  crList list from central repository
     */
    DirPruner(File dir, File trash, List crList) {
        _root  = dir;
        _trash = trash;
        _crList = crList;
    }

    /**
     * Prunes this directory. 
     */
    void prune() {

        InventoryMgr mgr = null;
        if ((_root == null) || (!_root.exists()) 
                || (_trash == null) || (_crList == null)) {
            return;
        }

        try {
            mgr = new InventoryMgr(_root);

            if (_crList.size() == 0) {
                _logger.log(Level.FINE, 
                    "Found empty inventory list from central repository for: " 
                    + _root.getPath());
            }

            // converts the inventories to sorted array
            String[] cr = InventoryMgr.transformInventory(_crList);

            // inventory of the cache directory
            List inventory = mgr.getInventory();
            Iterator iter = inventory.iterator();
            while (iter.hasNext()) {
                String f = (String) iter.next();

                // file is not present in central repository
                if (isACandidate(f, cr)) {
                    File file = new File(_root, f);
                    FileHandler handler  = new FileHandler(file, _trash);
                    handler.remove();

                    // remove empty directory
                    File parent = file.getParentFile();
                    String[] children = parent.list();
                    if (children.length == 0) {
                        FileHandler dHandler = new FileHandler(parent, _trash);
                        dHandler.remove();
                    }
                }
            }

        } catch (Exception e) {
            // ignore
        } finally {
            if (mgr != null) {
                try {
                    mgr.removeInventoryFile();
                    mgr.removeGCTargetFile();
                } catch (Exception e) { }
            }
        }
    }

    
    /**
     * Executes prune in a thread.
     */
    public void run() {
        prune();

        // remove empty dirs under top level directories
        File[] child = _root.listFiles();
        if (child != null) {
            for (int i=0; i<child.length; i++) {
                if (child[i].isDirectory()) {
                    CleanerUtils.removeEmptyDir(child[i]);
                }
            }
        }
    }

    /**
     * Returns true if the given file is removeable.
     *
     * @param   f  relative file path
     * @param   cr  inventory of central repository in sorted order
     *
     * @return  true if the file can be removed
     */
    private boolean isACandidate(String f, String[] cr) {

        boolean candidate  = false;
        String file        = FileUtils.makeForwardSlashes(f);

        // file is in the exclude list
        if (CleanerUtils.isExcluded(file)) {
            return false;
        }
       
        // file does not exist in central repository
        if (Arrays.binarySearch(cr, file) < 0) {

            candidate = true;

            _logger.log(Level.FINE, "Found GC candidate: " 
                + _root.getPath() + File.separator + f);
        }

        return candidate;
    }

    // ---- INSTANCE VARIABLE(S) -----------------------------------------
    private File _root                          = null;
    private File _trash                         = null;
    private List _crList                        = null;

    private static Logger _logger = 
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
}
