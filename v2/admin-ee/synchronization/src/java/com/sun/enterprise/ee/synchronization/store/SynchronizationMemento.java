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
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.SynchronizationResponse;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;

/**
 * Saves a snapshot of the internal state of a synchronization response.
 * This is called prior to rendering the response in the instance cache. 
 * This allows to go back to the previously saved state of the originator 
 * if necessary.
 *
 * @author Satish Viswanatham
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class SynchronizationMemento {

    private static Logger _logger = Logger.getLogger(
                EELogDomains.SYNCHRONIZATION_LOGGER);

    /**
     * Creates a new memento class.
     *
     * @param  res   synchronization response object
     */
    public SynchronizationMemento(SynchronizationResponse res) {

        _store  = new FilePersistenceStore();
        SynchronizationRequest[] reply = res.getReply();

        // Warning: uses only the first request from the reply
        assert(reply.length == 1);
        _file   = reply[0].getFileName();
    }

    /**
     * Saves a state of the source file.
     *
     * @throws  StoreException  if an error while saving the state
     */
    public void saveState() throws StoreException {
        
        File f = new File(_file);

        // file may not exists at the very first time
        _logger.log(Level.FINER, "synchronization.save_momento", _file);

        if (f.exists()) {
            _store.save(_file);
        }
    }

    /**
     * Rolls back to the previous saved state.
     *
     * @throws  StoreException  if an error while saving the state
     */
    public void rollback() throws StoreException {
        _logger.log(Level.FINER, "synchronization.rollback_momento", _file);
        _store.restore(_file);

    }

    /**
     * Commits the saved state to repository.
     *
     * @throws  StoreException  if an error while commiting the saved state
     */
    public void commit() throws StoreException {
        commitWithCopy();
    }

    /**
     * Commits the saved state when "copy" semantics was used to save the state.
     * This implementation removes the backup copy.
     *
     * @throws  StoreException  if an error while commiting the saved state
     */
    private void commitWithCopy() throws StoreException {

        String dst = 
            (String) ((FilePersistenceStore)_store).getBackupFileName(_file);
        _logger.log(Level.FINER, "synchronization.commit_momento", _file);
        File dstFile = new File(dst);

        if (dstFile.exists()) {
            FileUtils.liquidate(dstFile);                
        } else {
            _logger.log(Level.FINEST, "synchronization.no_backup_dir", dst);
        }
    }

    /**
     * Commits the changes by merging the old content with the new content.
     *
     * @throws  StoreException  if an error while commiting the saved state
     */
    private void commitWithMove() throws StoreException {

        String dst = 
            (String) ((FilePersistenceStore)_store).getBackupFileName(_file);
        _logger.log(Level.FINER, "synchronization.commit_momento", _file);
        File newFile = new File(_file);
        File dstFile = new File(dst);


        // if the backup file does not exists, do nothing
        if (dstFile.exists()) {

            // if the backup is a directory, merge the missing files from
            // the backup directory to the new directory
            if (newFile.isDirectory()) {
                _store.merge(dst, _file);
            } else  if ( dstFile.isDirectory() && !newFile.exists() ) {
                // if the new directory does not exists because of no new 
                // synchronization, move back the old dir
                _logger.log(Level.FINEST, 
                    "synchronization.no_sync_file", newFile.getName());
                boolean ok = dstFile.renameTo(newFile);
                if (!ok) {
                    throw new StoreException();
                }
            } else  if ( dstFile.isFile() && !newFile.exists() ) {
                // moving back a single file with no new synchronization update
                _logger.log(Level.FINEST, 
                    "synchronization.no_sync_file", newFile.getName());
                boolean ok = dstFile.renameTo(newFile);
                if (!ok) {
                    throw new StoreException();
                }
            } else if ( dstFile.isFile() && newFile.exists() ) {
                FileUtils.liquidate(dstFile);                
                // do nothing; nothing to merge
            }
        } else {
            // do nothing
            _logger.log(Level.FINEST, 
               "synchronization.no_backup_dir", dst);
        }
    }

    // ---- VARIABLE(S) - PRIVATE ------------------------------
    PersistenceStore _store  = null;
    String _file             = null;
}
