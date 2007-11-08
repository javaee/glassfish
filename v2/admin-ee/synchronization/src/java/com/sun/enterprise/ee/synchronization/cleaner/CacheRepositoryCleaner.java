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
import java.util.Iterator;
import java.util.Properties;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.ee.synchronization.ServerDirector;
import com.sun.enterprise.ee.synchronization.TextProcess;
import com.sun.enterprise.ee.synchronization.store.FilePersistenceStore;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.ee.synchronization.RequestMediator;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.ee.synchronization.store.StoreException;
import java.io.IOException;

/**
 * Responsible for managing the cache repository.
 *
 * @author Nazrul Islam
 */
class CacheRepositoryCleaner implements Cleaner {

    /**
     * Constructor!
     *
     * @param  ctx         config context
     * @param  serverName  name of this server instance
     * @param  mReqs  request mediators
     */
    CacheRepositoryCleaner(ConfigContext ctx, String serverName,
            RequestMediator[] mReqs ) {

        _serverDirector  = new ServerDirector(ctx, serverName);
        _serverName      = serverName;
        _mediators       = mReqs;

        // server instance root
        String instanceRoot = System.getProperty(
            SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);

        // trash directory where all removed files are moved first
        _mainTrash = CleanerUtils.getMainTrash();
        _trash     = CleanerUtils.getTemporaryTrash();


        // applications directory
        _applicationsDir = new File(instanceRoot + File.separator
                            + PEFileLayout.APPLICATIONS_DIR);

        // generated directory
        _generatedDir = new File(instanceRoot + File.separator
                            + PEFileLayout.GENERATED_DIR);

        // docroot directory
        _docrootDir = new File(instanceRoot + File.separator
                            + PEFileLayout.DOC_ROOT_DIR);

        // lib directory
        _libDir = new File(instanceRoot + File.separator
                            + PEFileLayout.LIB_DIR);

        // config directory
        _configDir = new File(instanceRoot + File.separator
                            + PEFileLayout.CONFIG_DIR);

        // java-web-start directory
        _jwsDir = new File(instanceRoot + File.separator
                            + PEFileLayout.JAVA_WEB_START_DIR);
    }

    /**
     * Cleans an instance during server startup. 
     */
    public void gc() {

        try {
            CookieHandler cHandler = new CookieHandler();

            // runs the garbage collector once every day
            if (cHandler.isExpired()) {

                try {
                    // removes un-associated applications
                    cleanApplications();
                } catch (Exception appEx) {
                    _logger.log(Level.FINE, 
                        "Application repository cleaner failed", appEx);
                }

                DirPruner[] pruners = 
                    CleanerUtils.getDirPruner(_mediators, _trash);

                try {
                    // start removing stale files
                    for (int i=0; i<pruners.length; i++) {
                        if (pruners[i] != null) {
                            pruners[i].start();
                        }
                    }
                } catch (Exception pruneEx) {
                    _logger.log(Level.FINE, 
                        "Error while pruning directory", pruneEx);
                }

                try {
                    if (Boolean.getBoolean(BKUP_KEY)) {
                        removeSaveDirs();
                    }
                } catch (Exception saveEx) {
                    _logger.log(Level.FINE, 
                        "Error while cleaning trash", saveEx);
                }

                try {
                    for (int i=0; i<pruners.length; i++) {
                        if (pruners[i] != null) {
                            pruners[i].join();
                        }
                    }

                    // empties the trash
                    cleanTrash();
                } catch (Exception trashEx) {
                    _logger.log(Level.FINE, 
                        "Error while cleaning trash", trashEx);
                }

                // updates the cookie
                cHandler.updateCookie();
            }
        } catch (Exception e) {
            _logger.log(Level.FINE, "Repository cleaner failed", e);
        }
    }

    /** 
     * Removes all left over backup files, if any, after the synchronization.
     * This acts as a safely net. At times, directories can not be removed 
     * because of file locks. This is another try to remove the stale dirs.
     */
    private void removeSaveDirs() {
        FilePersistenceStore store = new FilePersistenceStore();

        String applicationsSave = null;
        try {
            // removes the applications_save dir
            applicationsSave = (String) store.getBackupFileName(
                                    _applicationsDir.getCanonicalPath());
            FileHandler handler = 
                new FileHandler(new File(applicationsSave), _trash);
            handler.remove();
        } catch (Exception applicationsEx) {
            try {
                FileUtils.liquidate( new File(applicationsSave) );
            } catch (Exception e) { 
                _logger.log(Level.FINE, 
                    "Error while removing applications_save dir", e);
            }
        }

        String generatedSave = null;
        try {
            // removes the generated_save dir
            generatedSave = (String) store.getBackupFileName(
                                   _generatedDir.getCanonicalPath());
            FileHandler handler = 
                new FileHandler(new File(generatedSave), _trash);
            handler.remove();
        } catch (Exception generatedEx) {
            try {
                FileUtils.liquidate( new File(generatedSave) );
            } catch (Exception e) {
                _logger.log(Level.FINE, 
                    "Error while removing generated_save dir", e);
            }
        }

        String docrootSave = null;
        try {
            // removes the docroot_save dir
            docrootSave = (String) store.getBackupFileName(
                                    _docrootDir.getCanonicalPath());
            FileHandler handler = 
                new FileHandler(new File(docrootSave), _trash);
            handler.remove();
        } catch (Exception docrootEx) {
            try {
                FileUtils.liquidate( new File(docrootSave) );
            } catch (Exception e) {
                _logger.log(Level.FINE, 
                    "Error while removing docroot_save dir", e);
            }
        }

        String libSave = null;
        try {
            // removes the lib_save dir
            libSave = (String) store.getBackupFileName(
                                   _libDir.getCanonicalPath());
            FileHandler handler = 
                new FileHandler(new File(libSave), _trash);
            handler.remove();
        } catch (Exception libEx) {
            try {
                FileUtils.liquidate( new File(libSave) );
            } catch (Exception e) {
                _logger.log(Level.FINE, 
                    "Error while removing lib_save dir", e);
            }
        }

        String configSave = null;
        try {
            // removes the config_save dir
            configSave = (String) store.getBackupFileName(
                                    _configDir.getCanonicalPath());
            FileHandler handler = 
                new FileHandler(new File(configSave), _trash);
            handler.remove();
        } catch (Exception configEx) {
            try {
                FileUtils.liquidate( new File(configSave) );
            } catch (Exception e) {
                _logger.log(Level.FINE, 
                    "Error while removing config_save dir", e);
            }
        }

        String jwsSave = null;
        try {
            // removes the java-web-start_save dir
            jwsSave = (String) store.getBackupFileName(
                                    _jwsDir.getCanonicalPath());
            FileHandler handler = 
                new FileHandler(new File(jwsSave), _trash);
            handler.remove();
        } catch (Exception configEx) {
            try {
                FileUtils.liquidate( new File(jwsSave) );
            } catch (Exception e) {
                _logger.log(Level.FINE, 
                    "Error while removing java-web-start_save dir", e);
            }
        }

    }

    /**
     * Empties trash.
     */
    private void cleanTrash() {

        TrashCleaner tc = TrashCleaner.getInstance(_mainTrash);
        tc.start();
    }

    /**
     * Returns currently deployed application dirs.
     *
     * @return  currently deployed application dirs
     */
    private List getActiveAppList() {

        List list = new ArrayList();
        List applicationDirs = _serverDirector.constructIncludes();

        Iterator iter = applicationDirs.iterator();
        while (iter.hasNext()) {
            String file = (String) iter.next();    

            // ignore if file is null or does not start with instance root
            if ( (file == null) 
                    || (!file.startsWith(OPEN_PROP + 
                    SystemPropertyConstants.INSTANCE_ROOT_PROPERTY)) ) {

                continue;
            }

            try {
                // tokenize the file path; it contains com.sun.aas.instanceRoot
                file = TextProcess.tokenizeConfig(file, _serverName,
                                                new Properties());
                list.add(file);
            } catch (Exception e) {
                _logger.log(Level.INFO, 
                    "synchronization.cleaner.file_remove_error", file);
            }
        }

        return list;
    }

    /**
     * Returns true if the dir is in the given list.
     * 
     * @param  list  file list
     * @param  dir   target dir
     *
     * @return  true if the target dir exists in the list
     * @throws IOException  if an i/o error
     */
    private boolean isInList(List list, File dir) throws IOException {

        boolean found = false;

        if ((dir != null) && (list != null)) {
            String target = dir.getCanonicalPath();

            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                String file = (String) iter.next();    

                // normalize the path for current OS
                String f = new File(file).getCanonicalPath();

                if (target.equals(f)) {
                    found = true;
                    break;
                }
            }
        }
        _logger.fine("Found target in associated application list " 
            + dir.getPath() + " - " + found);

        return found;
    }

    /**
     * Removes all un associated applications and stand alone modules.
     * 
     * @throws IOException  if an i/o error
     */
    private void cleanApplications() throws IOException {

        // removes undeployed applications
        cleanRemovedApplications();

        // removes unassociated applications - not needed
        //cleanUnassociatedApplications();
    }

    /**
     * Removes undeployed application dirs from the repository.
     *
     * @throws IOException if an i/o error
     */
    private void cleanRemovedApplications() throws IOException {
        List activeAppList = getActiveAppList();

        // applications/j2ee-apps dir
        File apps_j2eeapps = new File(_applicationsDir, 
                                PEFileLayout.J2EE_APPS_DIR);
        pruneRemovedApplicationsFromDir(activeAppList, apps_j2eeapps);

        // applications/j2ee-modules dir
        File apps_j2eemodules = new File(_applicationsDir, 
                                PEFileLayout.J2EE_MODULES_DIR);
        pruneRemovedApplicationsFromDir(activeAppList, apps_j2eemodules);

        // applications/lifecycle-modules dir
        File apps_lifecyclemodules = new File(_applicationsDir, 
                                PEFileLayout.LIFECYCLE_MODULES_DIR);
        pruneRemovedApplicationsFromDir(activeAppList, apps_lifecyclemodules);

        // generated/ejb/j2ee-apps dir
        File gen_ejb_j2eeapps = new File(_generatedDir, 
                                    PEFileLayout.EJB_DIR 
                                    + File.separator 
                                    + PEFileLayout.J2EE_APPS_DIR);
        pruneRemovedApplicationsFromDir(activeAppList, gen_ejb_j2eeapps);

        // generated/ejb/j2ee-modules dir
        File gen_ejb_j2eemodules = new File(_generatedDir, 
                                        PEFileLayout.EJB_DIR 
                                        + File.separator 
                                        + PEFileLayout.J2EE_MODULES_DIR);
        pruneRemovedApplicationsFromDir(activeAppList, gen_ejb_j2eemodules);

        // generated/xml/j2ee-apps dir
        File gen_xml_j2eeapps = new File(_generatedDir, 
                                    PEFileLayout.XML_DIR 
                                    + File.separator 
                                    + PEFileLayout.J2EE_APPS_DIR);
        pruneRemovedApplicationsFromDir(activeAppList, gen_xml_j2eeapps);

        // generated/xml/j2ee-modules dir
        File gen_xml_j2eemodules = new File(_generatedDir, 
                                        PEFileLayout.XML_DIR 
                                        + File.separator 
                                        + PEFileLayout.J2EE_MODULES_DIR);
        pruneRemovedApplicationsFromDir(activeAppList, gen_xml_j2eemodules);

        // generated/jsp/j2ee-apps dir
        File gen_jsp_j2eeapps = new File(_generatedDir, 
                                        PEFileLayout.JSP_DIR 
                                        + File.separator 
                                        + PEFileLayout.J2EE_APPS_DIR);
        pruneRemovedApplicationsFromDir(activeAppList, gen_jsp_j2eeapps);

        // generated/jsp/j2ee-modules dir
        File gen_jsp_j2eemodules = new File(_generatedDir, 
                                        PEFileLayout.JSP_DIR 
                                        + File.separator 
                                        + PEFileLayout.J2EE_MODULES_DIR);
        pruneRemovedApplicationsFromDir(activeAppList, gen_jsp_j2eemodules);

        // generated/policy dir
        File gen_policy = new File(_generatedDir, PEFileLayout.POLICY_DIR); 
        pruneRemovedApplicationsFromDir(activeAppList, gen_policy);

        // java-web-start dir
        pruneRemovedApplicationsFromDir(activeAppList, _jwsDir);
    }

    /**
     * Prunes a given directory - removes all directories not found in the 
     * active application list.
     *
     * @param  activeAppList  currently deployed application dirs
     * @param  dir   a repository dir 
     * @throws IOException if an i/o error
     */
    private void pruneRemovedApplicationsFromDir(List activeAppList, File dir) 
            throws IOException {
        
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();

            for (int i=0; i<files.length; i++) {
                _logger.fine("Inspecting file: " + files[i].getPath());
                
                if ( (files[i].isDirectory())
                        && (!isInList(activeAppList, files[i])) ) {

                    try {
                        // removes the directory 
                        FileHandler handler  = 
                            new FileHandler(files[i], _trash);
                        handler.remove();
                    } catch (Exception e) {
                        _logger.log(Level.INFO, 
                            "synchronization.cleaner.file_remove_error", 
                            files[i].getPath());
                    }
                }
            }
        }
    }

    /**
     * Removes all un-associated (but deployed to domain) applications 
     * from repository.
     */
    private void cleanUnassociatedApplications() {

        // associated application directories
        List applicationDirs = _serverDirector.constructExcludes();

        // attempts to remove all non-referenced application directories
        Iterator iter = applicationDirs.iterator();
        while (iter.hasNext()) {

            String file = (String) iter.next();    

            // ignore if file is null or does not start with instance root
            if ( (file == null) 
                    || (!file.startsWith(OPEN_PROP + 
                    SystemPropertyConstants.INSTANCE_ROOT_PROPERTY)) ) {

                continue;
            }

            try {
                // tokenize the file path; it contains com.sun.aas.instanceRoot
                file = TextProcess.tokenizeConfig(file, _serverName,
                                                new Properties());
                File f               = new File(file);

                // removes the file or directory 
                FileHandler handler  = new FileHandler(f, _trash);
                handler.remove();

            } catch (Exception e) {
                _logger.log(Level.INFO, 
                    "synchronization.cleaner.file_remove_error", file);
            }
        }
    }

    // ---- INSTANCE VARIABLE(S) -----------------------------------------
    private ServerDirector _serverDirector = null;
    private String _serverName             = null;
    private File _trash                    = null;
    private File _mainTrash                = null;
    private File _applicationsDir          = null;
    private File _generatedDir             = null;
    private File _docrootDir               = null;
    private File _libDir                   = null;
    private File _configDir                = null;
    private File _jwsDir                   = null;
    private static final String OPEN_PROP  = "${";
    private static Logger _logger = 
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
    private RequestMediator[] _mediators   = null;
    static final String BKUP_KEY = "com.sun.appserv.synchronization.backup";
}
