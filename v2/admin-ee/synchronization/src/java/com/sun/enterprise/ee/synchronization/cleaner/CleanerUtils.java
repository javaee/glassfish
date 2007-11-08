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
import java.util.StringTokenizer;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.SynchronizationResponse;
import com.sun.enterprise.ee.synchronization.RequestMediator;
import com.sun.enterprise.util.i18n.StringManager;
import java.io.IOException;

/**
 * Utility class for synchronization cleaner.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class CleanerUtils {

    /**
     * Returns true if file is part of the exclude list.
     *
     * @param   file  relative file path
     * @return  true if file is excluded 
     */
    public static boolean isExcluded(String file) {

        if (file == null) {
            return false;
        }

        // exclude will always have forward slash
        String f = FileUtils.makeForwardSlashes(file);

        // if file matches partially with exclude list
        for (int i=0; i<CleanerConstants.EXCLUDE_LIST.length; i++) {

            String s = CleanerConstants.EXCLUDE_LIST[i];

            if ( f.indexOf(s) != -1 ) {
                return true;
            }
        }

        try {
            initDoNotRemoveList();

            // search the do-not-remove list
            if (_dnrList != null) {

                // look for match
                for (int j=0; j<_dnrList.length; j++) {
                    if ( f.indexOf(_dnrList[j]) != -1 ) {
                        return true;
                    }
                    // if parent of a exlcuded file, return true
                    if (_dnrList[j].indexOf(f) != -1) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            _logger.log(Level.FINE, "synchronization.mbean.failed", e);
        }

        return false;
    }

    /**
     * Returns true if the given file is a parent or the directory 
     * in excluded file or directory list.
     *
     * @param  dir  a directory  
     * @return true when the given directory is a parent of excluded file or dir
     */
    static boolean isParentOrExcluded(String dir) {

        boolean tf = false;

        if (dir != null) {
            String d = FileUtils.makeForwardSlashes(dir);

            try {
                initDoNotRemoveList();
                if (_dnrList != null) {
                    for (int i=0; i<_dnrList.length; i++) {
                        if ( d.indexOf(_dnrList[i]) != -1 ) {
                            tf = true;
                            break;
                        }
                        if (_dnrList[i].indexOf(d) != -1) {
                            tf = true;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                _logger.log(Level.FINE, "synchronization.mbean.failed", e);
            }
        }

        return tf;
    }

    /**
     * Initializes the do not remove list.
     */
    private static void initDoNotRemoveList() {

         try {
            // check for do-not-remove property
            if ((_dnrList == null) && (_dnrListInit==false)) {
                String sysProperty =
                    System.getProperty(CleanerConstants.DO_NOT_REMOVE_LIST);

                _logger.fine("DO NOT REMOVE LIST=" + sysProperty);

                // do not remove property is defined
                if (sysProperty != null) {
                    // instance root
                    String iRoot = System.getProperty(
                        SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);

                    if (iRoot == null) {
                        String msg = _localStrMgr.getString("iRootIsNull", 
                                SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
                        throw new IllegalArgumentException(msg);
                    }

                    // tokenize the coma separated list
                    StringTokenizer st = new StringTokenizer(sysProperty, ",");
                    int tokenCount = st.countTokens();
                    _dnrList = new String[tokenCount];

                    int i = 0;
                    while (st.hasMoreTokens()) {
                        String tk = null;
                        if ( (iRoot.endsWith("/")) || (iRoot.endsWith("\\")) ) {
                            tk = FileUtils.makeForwardSlashes(
                                            iRoot+st.nextToken());
                        } else {
                            tk = FileUtils.makeForwardSlashes(
                                        iRoot+"/"+st.nextToken());
                        }

                        if (tk.endsWith("/")) {
                            // remove last forward slash
                            _dnrList[i++] = tk.substring(0, tk.length()-1);
                        } else {
                            _dnrList[i++] = tk;
                        }
                    }
                }
                _dnrListInit = true;
            }
        } catch (Exception e) {
            _logger.log(Level.FINE, "synchronization.mbean.failed", e);
        }
    }

    /**
     * Removes empty directories.
     *
     * @param  dir  root directory
     */
    public static void removeEmptyDir(File dir) {


        if (dir != null) {
            File[] child = dir.listFiles();


            // examine each children
            for (int i=0; i<child.length; i++) {
                if (child[i].isDirectory()) {

                    // child is an empty dir
                    String[] list = child[i].list();
                    if (list.length == 0) {
                        try {
                            String childDir = child[i].getCanonicalPath();
                            if (!CleanerUtils.isParentOrExcluded(childDir)) {
                                child[i].delete();
                            }
                        } catch (IOException e) { 
                            _logger.log(Level.FINE, 
                                "Error while removing directory", e);
                        }
                    } else {
                        removeEmptyDir(child[i]);
                    }
                }
            }

            try {
                // check after child removal
                String[] again  = dir.list();
                String dirPath  = dir.getCanonicalPath();

                if (again.length == 0) {
                    if (!CleanerUtils.isParentOrExcluded(dirPath)) {
                        _logger.fine("Removing empty directory: " + dirPath);

                        // remove empty dir
                        dir.delete();
                    }
                } else {
                    _logger.fine("Directory is not empty: " + dirPath);
                }
            } catch (IOException ioe) {
                _logger.log(Level.FINE, "Error while removing directory", ioe);
            }
        }
    }


    /**
     * Returns the main trash folder for a server instance.
     * Note: This method depends on instance root property.
     *
     * @retrn  file handle to the main trash
     */
    static File getMainTrash() {

        // server instance root
        String iRoot = System.getProperty(
            SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);

        // trash director for this cleaning session
        File trash = new File(iRoot + File.separator 
                + PEFileLayout.GENERATED_DIR + File.separator + TRASH_FILE);

        // create the trash folder if it does not exist
        if ( !trash.exists() ) {
            trash.mkdirs();
            _logger.fine("Created main trash folder: " + trash.getPath());
        }

        return trash;
    }

    /**
     * Returns a temporary trash folder under the main trash folder.
     * A temporary trash folder is used during synchronization 
     * while server is running.
     *
     * @return  temporary trash folder
     */
    static File getTemporaryTrash() {

        // main trash director
        File temp = getMainTrash();

        long ts = System.currentTimeMillis();
        File trash = new File(temp, Long.toString(ts));
        while ( trash.exists() ) {
            ts += 1;
            trash = new File(temp, Long.toString(ts));
        }
        // temp trash dir for a cleaning session
        trash.mkdirs();
        _logger.fine("Created temporary trash folder: " + trash.getPath());

        return trash;
    }

    /**
     * Returns an array of directory pruners from the given request 
     * mediators. This method is called on the client side after 
     * synchronization responsed to the client.
     *
     * @param  mReqs  request mediators
     * @param  trash  trash folder for the pruner
     *
     * @return array of directory pruners
     */
    static DirPruner[] getDirPruner(RequestMediator[] mReqs, File trash) {

        DirPruner[] pruners = null;

        if ((mReqs == null) || (trash == null)) {
            return null;
        }

        pruners = new DirPruner[mReqs.length];
        
        for (int i=0; i<pruners.length; i++) {

            SynchronizationResponse res = mReqs[i].getResponse();
            SynchronizationRequest[] reqs = res.getReply();
            if (reqs[0] != null) {
                String baseDir   = reqs[0].getBaseDirectory();
                String targetDir = reqs[0].getTargetDirectory();
                File reqDir = new File(baseDir + File.separator + targetDir);

                pruners[i] = new DirPruner(reqDir, trash,
                                       mReqs[i].getCRInventory());
            }
        }

        return pruners;
    }

    // ---- INSTANCE VARIABLE(S) -----------------------------------------
    private static final String TRASH_FILE = ".com_sun_appserv_trash";
    private static String[] _dnrList       = null;
    private static boolean _dnrListInit    = false;
    private static Logger _logger = 
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
    private static final StringManager _localStrMgr = 
                StringManager.getManager(CleanerUtils.class);
}
