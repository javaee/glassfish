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

import java.io.File;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.ee.synchronization.util.io.Utils;
import java.io.IOException;

import com.sun.enterprise.util.i18n.StringManager;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManagerBase;

/**
 * File persistent store to store and receive sync checkpoints
 *
 * @author Nazrul Islam
 * @author Satish Viswanatham
 * @since  JDK1.4
 */
public class FilePersistenceStore implements PersistenceStore {

    private static final StringManager _localStrMgr =
        StringManager.getManager(FilePersistenceStore.class);

    private static Logger _logger = Logger.getLogger(
                EELogDomains.SYNCHRONIZATION_LOGGER);

    private static StringManagerBase _logStrMgr =
        StringManagerBase.getStringManager(_logger.getResourceBundleName());

    /**
     * Saves the checkpoint of the object into persistent store
     *
     * @param obj   the original object whose checkpoint needs to be taken
     *
     * @throws StoreException  if an error while saving the state
     */
    public void save(java.lang.Object obj) throws StoreException {
        if (isBkupON()) {
            saveWithCopy(obj);
        }
    }

    /**
     * Returns true if synchronization backup mechanism is turned on by the 
     * system property. Set -Dcom.sun.appserv.synchronization.backup=true 
     * to turn backup on. This saves a backup copy before rendering the 
     * synchronization response from DAS.
     */
    private boolean isBkupON() {
        return Boolean.getBoolean(BKUP_KEY);
    }

    /**
     * Saves the checkpoint by doing a deep copy. 
     *
     * <p> Note: When this method is used, method merge should not be used. 
     *
     * @param obj   the original object whose checkpoint needs to be taken
     *
     * @throws StoreException  if an error while saving the state
     */
    private void saveWithCopy(java.lang.Object obj) throws StoreException {

        assert(obj != null); 

        String origDir = (String) obj;
        String backupDir = (String) getBackupFileName(origDir);

        File fromDir = new File(origDir);
        File toDir = new File(backupDir);

        // remove old xxx_save dir
        if (toDir.exists()) {
            _logger.log(Level.FINEST, 
                    "synchronization.remove_olddir", toDir.getName());
            FileUtils.liquidate(toDir);
        }

        // takes a snapshot of the current content by doing a deep copy
        try {
            FileUtils.copy(fromDir, toDir);
        } catch (IOException ioe) {
            String msg = _localStrMgr.getString("notSavedError",fromDir,toDir);
            throw new StoreException(msg, ioe);
        }
    }

    /**
     * Saves the checkpoint by moving the current content into a backup.
     *
     * @param obj   the original object whose checkpoint needs to be taken
     *
     * @throws StoreException  if an error while saving the state
     */
    private void saveWithMove(java.lang.Object obj) throws StoreException {

        assert(obj != null); 

        String origDir = (String) obj;
        String backupDir = (String) getBackupFileName(origDir);

        File fromDir = new File(origDir);
        File toDir = new File(backupDir);

        // remove old garbage, if any
        if (toDir.exists()) {
            _logger.log(Level.FINEST, 
                    "synchronization.remove_olddir", toDir.getName());
            FileUtils.liquidate(toDir);
        }

        _logger.log(Level.FINER, _logStrMgr.getString(
             "synchronization.rename_dir", fromDir.getName(), toDir.getName()));
        boolean ok = fromDir.renameTo(toDir);
        if (!ok) {
            String msg = _localStrMgr.getString("notSavedError",fromDir,toDir);
            throw new StoreException(msg);
        }
    }

    /**
     * Gets the saved checkpoint of the object into persistent store
     *
     * @param  obj   the original object whose checkpoint was taken
     *
     * @throws StoreException  if an error while restoring the state
     */
    public void restore(java.lang.Object obj) throws StoreException {

        // abort if bkup is not turned on
        if (!isBkupON()) {
            return;
        }

        assert(obj != null);

        String origDir = (String) obj;
        String backupDir = (String) getBackupFileName(origDir);

        File fromDir = new File(backupDir);
        File toDir = new File(origDir);

        if (toDir.exists()) {
            _logger.log(Level.FINEST, 
                    "synchronization.remove_olddir", toDir.getName());
            FileUtils.liquidate(toDir);
        }

        if (fromDir.exists()) {
            _logger.log(Level.FINER, _logStrMgr.getString(
              "synchronization.rename_dir", fromDir.getName(), toDir.getName()));
            boolean ok = fromDir.renameTo(toDir);
            if (!ok) {
                String msg = _localStrMgr.getString("notSavedError" , fromDir , toDir);
                throw new StoreException(msg);
            }
        }
    }

    /**
     * Merge changes in state between the src and dst. 
     *
     * @param   src  the source object 
     * @param   dst  the destination object
     *
     * @throws  StoreException  if an error while merging the changes
     */
    public void merge(Object src, Object dst) throws StoreException {

        // abort if bkup is not turned on
        if (!isBkupON()) {
            return;
        }

        assert(src != null);
        assert(dst != null);

        File srcFile  = new File((String) src);
        File dstFile  = new File((String) dst);

        try {
            _logger.log(Level.FINEST, _logStrMgr.getString(
               "synchronization.merge_dirs", srcFile.getName(), 
                    dstFile.getName()));
            Utils.mergeTree(srcFile, dstFile);
        } catch (IOException ioe) {
            String msg = _localStrMgr.getString("notMergedError" , src , dst);
            throw new StoreException(msg, ioe);
        }
    }

    /**
     * Returns the backup file name for a directory or a file.
     *
     * @param  src   name of the file to backed up
     * @return the backup file name
     */
    public Object getBackupFileName(Object src) throws StoreException {

        assert(src != null);

        try { 
            File file = new File((String) src);
            File bkup = null;
            _logger.log(Level.FINER, 
              "synchronization.get_backup", file.getName());
            if (file.isDirectory()) {
                String dir = file.getCanonicalPath();
                if (dir.endsWith(File.separator)) {
                    dir = dir.substring(0, dir.length());
                }
                bkup = new File(dir + DEF_SUFFIX);
            } else {
                bkup = new File(file.getCanonicalPath() + DEF_SUFFIX);
            }

            return bkup.getCanonicalPath();
        } catch (Exception e) {
            String msg = _localStrMgr.getString("noBackupFileError" , src );
            throw new StoreException(msg, e);
        }
    }

    // ---- VARIABLE(S) - PRIVATE ---------------------------------
    static final String DEF_SUFFIX = "_save";
    static final String BKUP_KEY = "com.sun.appserv.synchronization.backup";
}
