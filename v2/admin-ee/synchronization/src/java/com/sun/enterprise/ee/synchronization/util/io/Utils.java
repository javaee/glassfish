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
package com.sun.enterprise.ee.synchronization.util.io;

import com.sun.enterprise.util.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import com.sun.enterprise.ee.synchronization.inventory.InventoryMgr;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Utility file to manage moving and copying operations
 *
 * @author Satish Viswanatham
 * @since  JDK1.4
 */
public class Utils
{
    private static final StringManager sMgr = 
                            StringManager.getManager(Utils.class);

    /**
     * Returns a file handle to a temporary zip file.
     *
     * @return  a file handle to a temporary zip file
     */
    public static File getTempZipFile() {
        long ts = System.currentTimeMillis();
        File f = new File(tmpName, new Long(ts).toString()+".zip");
        while ( f.exists() ) {
            ts += 1;
            f = new File(tmpName, new Long(ts).toString()+".zip");
        }
        f.getParentFile().mkdirs();

        return f;
    }

    /**
     * Moves the the files and directories that exist in the soruce location 
     * to a new location, but only files/directories that exist in source, but
     * do not exist in destination directory are moved.
     *
     * @param   din  File pointing at root of tree to copy
     * @param   dout File pointing at root of new tree
     *
     * @exception  IOException  if an error while copying the content
     */
    public static void mergeTree(File din, File dout )
            throws IOException
    {
        // audit trail before tree merge
        audit(din, dout, "beforemerge");

        // merging backup with target dir
        moveTree(din,dout);

        // audit trail after tree merge
        audit(din, dout, "aftermerge");

        // removes the backup
        FileUtils.liquidate(din);

        // if removal failed
        if (din.exists()) {
            long ts = System.currentTimeMillis();
            File f = new File( tmpName, new Long(ts).toString());

            // If tmp file exists, increment the time stamp 
            // to come up with a new tmp file name
            while ( f.exists() ) {
                ts += 1;
                f = new File( tmpName, new Long(ts).toString());
            }    
            f.mkdirs();

            // moving the left over back up dir to newly created tmp dir
            boolean ok = din.renameTo(f);

            // rename failed
            if (!ok) {
                // attempting to remove again
                FileUtils.liquidate(din);

                // second attempt failed
                if (din.exists()) {
                    _logger.log(Level.INFO, 
                            "synchronization.remove.failed", din.getPath() );
                }
            } else {
                // rename suceeded, setting remove on exit.
                try {
                    f.deleteOnExit();
                } catch(Exception e) {
                    // ignore delete on exit errors
                }
            }
        }
    }

    static void audit(File din, File dout, String suffix) 
    { 
        if (_logger.getLevel() == Level.FINE)
        {
            try 
            {
                InventoryMgr newMgr = new InventoryMgr(dout);
                List newList = newMgr.getInventory();

                // inventory of the backup dir
                InventoryMgr backupMgr = new InventoryMgr(din);
                List dstList = backupMgr.getInventory();

                // diff between backup and newly synchronized files
                List diffList = backupMgr.getInventoryDiff(dstList, newList);

                // saves the inventory of backup, new synchronized files 
                // and their diff 
                newMgr.saveAuditList(dstList, "backup_"+suffix);
                newMgr.saveAuditList(newList, "new_"+suffix);
                newMgr.saveAuditList(diffList, "diff_"+suffix);
            } catch (Exception e) { }
        }
    }

    static void moveTree(File din, File dout)
            throws IOException
    {

        _logger.log(Level.FINE, 
                "\n[SYNC-MoveTree]Backup file name " + din.getPath());
        _logger.log(Level.FINE, 
                "\n[SYNC-MoveTree]Target file name " + dout.getPath());

        // if a target file/dir does not exist, move the back up file
        if (!dout.exists()) {

            _logger.log(Level.FINE, 
                    "\n[SYNC-MoveTree] Target file " 
                    + dout.getPath() + " does not exist. Renaming backup.");

            boolean renamed = din.renameTo(dout);
            if (!renamed) {
                String msg = sMgr.getString("RenameFailed", 
                      din.getAbsolutePath(), dout.getAbsolutePath());
                throw new RuntimeException(msg);
            }
            return;
        }
        

        // getting list of files under backup and target dirs
        String[] files   = din.list();
        String[] files2  = dout.list();

        // back up is an empty dir or file. The above if block made sure 
        // that taget exists or back up is moved
        if (files == null || files.length == 0) {
            _logger.log(Level.FINE, 
                    "\n[SYNC-MoveTree]Backup is an empty dir or file: " 
                    + din.getPath());
            return;
        }

        // Target is a file or empty directory. 
        // Moving back up directory to target.
        if ( files2 == null || files2.length == 0) 
        {
            // rename if it is a directory (empty)
            if (dout.isDirectory()) {

                _logger.log(Level.FINE, 
                        "\n[SYNC-MoveTree]Target is an empty dir: "
                        +dout.getPath());

                // removes the empty new dir
                FileUtils.liquidate(dout);

                // moves all of the backup dir
                boolean renamed = din.renameTo(dout);
                if (!renamed) {
                    String msg = sMgr.getString("RenameFailed", 
                       din.getAbsolutePath(), dout.getAbsolutePath());
                    throw new RuntimeException(msg);
                }
            }
            // else do nothing since target is a file
        }
        else {
            // ASSERTION:
            //   both backup and target are non-empty dirs

            // sort the target file name array
            Arrays.sort(files2);

            // target dir exists. Checking if backup files under this dir
            // needed to be merged back to corresponding target dirs/files.
            int sr_ky=0;
            for(int i = 0; i < files.length; i++)
            {
                _logger.log(Level.FINE, 
                        "\n[SYNC-MoveTree] Inspecting backup file: "+files[i]);

                sr_ky = 0;

                // Do a binary search to see if this file or directory exists

                // if a back file/directory does not exist
                if ((sr_ky = Arrays.binarySearch(files2,files[i])) < 0 )
                {
                    _logger.log(Level.FINE, 
                            "\n[SYNC-MoveTree] Backup file: " + files[i] 
                            + " is not available in target directory." );

                    File fin  = new File(din, files[i]);
                    File fout = new File(dout, files[i]);

                    _logger.log(Level.FINE, 
                            "\n[SYNC-MoveTree] Renaming file: " + fin.getPath()
                            + " to " + fout.getPath() );

                    // moving the backup file or dir to the corresponding 
                    // target because it does not exist
                    boolean renamed = fin.renameTo(fout);
                    if (!renamed) {
                        String msg = sMgr.getString("RenameFailed", 
                            fin.getAbsolutePath(), fout.getAbsolutePath());
                        throw new RuntimeException(msg);
                    }
                }
                else { // found backup file/dir in target 
                    _logger.log(Level.FINE, 
                            "\n[SYNC-MoveTree] Found backup file " 
                            + files[i] + " in target " + files2[sr_ky]);

                    File f = new File(din, files[i]);
                    if ( f.isDirectory() ){

                    _logger.log(Level.FINE, 
                                "\n[SYNC-MoveTree] Backup file " 
                                + files[i] + " is a directory. Calling again.");

                        // recursively merge these when backup is a dir
                        moveTree(f, new File(dout, files2[sr_ky]));
                    } else {
                        // there is a new version of the file
                        // in the repository now, ignore backup
                        _logger.log(Level.FINE, 
                                "\n[SYNC-MoveTree] Backup file " 
                                + files[i] + " is stale. Discarding it.");
                    }
                }
            }
        }
    }

    /**
     * Method usage
     */
    public static void usage() {
        System.out.println("usage: source-dir dest-dir");
        System.exit(1);
    }

    /**
     * Tests mergeTree function
     */
    public static void main(String argv[]) {
        try {
            if (argv.length != 2) {
                usage();
            }
            Utils.mergeTree( new File(argv[0]), new File(argv[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ---- VARIABLE(S) - PRIVATE ---------------------------------

    /**
     * temp  dir name
     */
    private static String tmpName = System.getProperty("java.io.tmpdir") 
                + File.separator + "appserver-tmp";

    /**
     * isInitialized flag
     */
    private static boolean isInit = false;

    private static Logger _logger = Logger.getLogger(
            EELogDomains.SYNCHRONIZATION_LOGGER);
}
