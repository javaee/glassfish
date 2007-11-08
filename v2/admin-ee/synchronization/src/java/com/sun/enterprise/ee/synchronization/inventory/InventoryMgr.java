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
package com.sun.enterprise.ee.synchronization.inventory;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.io.FileLister;
import com.sun.enterprise.util.io.FileListerRelative;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;

/**
 * Responsible for generating repository inventory.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class InventoryMgr {

    /**
     * Constructor.
     *
     * @param  root  repository root dir
     */
    public InventoryMgr(File root) {
        this._root = root;

        if ( !root.isDirectory() ) {
            String msg = _localStrMgr.getString("notADirectory", _root);
            throw new RuntimeException(msg);
        }
    }

    /**
     * Retunrs the inventory list.
     *
     * @return  list of java.lang.String containing list of 
     *          relative file paths for the current repository root
     */
    public List getInventory() {
        FileLister lister = new FileListerRelative(_root);
        String[] files = lister.getFiles();

        return Arrays.asList(files);
    }

    /**
     * Returns any extra files that does not exist in the target list.
     *
     * @param  origList    original list
     * @param  targetList  target list
     * 
     * @return  diff between original list and target list
     */
    public static List getInventoryDiff(List origList, List targetList) {
        InventoryDiff d = new InventoryDiff(origList, targetList);
        return d.diff();
    }

    /**
     * Saves the given inventory of files under the current
     * respository root. 
     *
     * @param   inventory  list of relative file paths
     * @param   file       name of the inventory file
     *
     * @throws  IOException  if an i/o error
     */
    private void saveInternal(List inventory, String file) 
            throws IOException {

        File f  = new File(_root, file);
        InventoryWriter writer = new InventoryWriter(inventory, f);
        writer.write();
    }

    /**
     * Saves the inventory under the repositoy root. 
     *
     * @param   inventory    list of files that are currently available
     *                       in the central repository
     * @throws  IOException  if an i/o error
     */
    public void saveInventory(List inventory) throws IOException {

        saveInternal(inventory, INVENTORY_FILE_NAME);
    }

    /**
     * Loads the persisted inventory under this root.
     *
     * @return  persisted inventory list as java.lang.String
     *
     * @throws  IOException  if an i/o error
     */
    public List loadInventory() throws IOException {

        File f = new File(_root, INVENTORY_FILE_NAME);
        InventoryReader reader = new InventoryReader(f);

        return reader.read();
    }

    /**
     * Deletes the persisted inventory file.
     *
     * @throws  IOException  if an i/o error
     */
    public void removeInventoryFile() throws IOException {

        File f = new File(_root, INVENTORY_FILE_NAME);
        FileUtils.whack(f);
    }

    /**
     * Saves the current inventory list. This is used for auditing only.
     */
    public void save() throws IOException {
        List list  = getInventory();
        saveInternal(list, INVENTORY_FILE_NAME);
    }

    /**
     * Saves the file list for garbage collection.
     *
     * @param   extra  list of files targeted for garbage collection
     *
     * @throws  IOException  if an i/o error
     */
    public void saveGCTargetList(List extra) throws IOException {
        saveInternal(extra, GC_TARGET_FILE_NAME);
    }

    /**
     * Saves the file list for future audits.
     *
     * @param   audit  list of files
     *
     * @throws  IOException  if an i/o error
     */
    public void saveAuditList(List audit, String suffix) throws IOException {
        saveInternal(audit, INVENTORY_AUDIT_FILE_NAME+suffix);
    }


    /**
     * Loads the persisted GC target list under this root.
     *
     * @return  persisted GC target list
     *
     * @throws  IOException  if an i/o error
     */
    public List loadGCTargetList() throws IOException {
        
        File f = new File(_root, GC_TARGET_FILE_NAME);
        InventoryReader reader = new InventoryReader(f);

        return reader.read();
    }

    /**
     * Deletes the persisted GC target inventory file.
     *
     * @throws  IOException  if an i/o error
     */
    public void removeGCTargetFile() throws IOException {

        File f = new File(_root, GC_TARGET_FILE_NAME);
        FileUtils.whack(f);
    }

    /**
     * Converts the file paths to forward slashes and sorts the list.
     *
     * @param  list  inventory list
     *
     * @return  transformed list
     */
    public static String[] transformInventory(List list) {
        return InventoryDiff.transformInventory(list);
    }

    public static void main(String[] args) {
        try {
            File f = new File(args[0]);
            InventoryMgr mgr = new InventoryMgr(f);
            mgr.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---- VARIABLE(S) - PRIVATE -------------------------------
    private File _root = null;
    private static final String INVENTORY_FILE_NAME = 
                                    ".com_sun_appserv_inventory";
    private static final String GC_TARGET_FILE_NAME = 
                                    ".com_sun_appserv_inventory_gc_targets";
    private static final String INVENTORY_AUDIT_FILE_NAME = 
                                    ".com_sun_appserv_inventory_audit_";
    private static Logger _logger = 
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
    private static final StringManager _localStrMgr = 
        StringManager.getManager(InventoryMgr.class);
}
