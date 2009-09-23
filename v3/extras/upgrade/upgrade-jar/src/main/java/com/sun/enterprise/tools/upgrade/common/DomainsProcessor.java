/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.tools.upgrade.common;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.LogService;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author  prakash
 * @author  hans
 */
public class DomainsProcessor {
    
    private CommonInfoModel commonInfo;
    private static final StringManager stringManager =
        StringManager.getManager(DomainsProcessor.class);
    private static final Logger logger = LogService.getLogger();

    public DomainsProcessor(CommonInfoModel ci) {
        this.commonInfo = ci;
    }

    public int startDomain(String domainName) throws HarnessException {
        return Commands.startDomain(domainName, commonInfo);
    }

    /*
     * Copy any user files in the src server's lib dir to the target server's
     * lib dir. This is brittle and only works for the case where the source
     * domain is in glassfish/domains. It would be good to prompt the user
     * for the lib directory when not found, but that will have to wait
     * for the next release. If jar files can't be copied, alert the user
     * to do it manually (if there are jars file dependencies).
     */
    public void copyUserLibFiles() {
        logger.info(stringManager.getString(
            "upgrade.common.start_copy_user_libs"));

        // source install dir is the actual domainX directory
        String dirName = commonInfo.getSource().getInstallDir();
        File sLibDir = findLibDir(dirName, true);
        if (sLibDir == null) {
            logger.warning(stringManager.getString(
                "upgrade.common.src_lib_dir_not_found", dirName));
            logUserMustCopyFiles();
            return;
        }

        // target install dir is the domains directory
        dirName = commonInfo.getTarget().getInstallDir();
        File tLibDir = findLibDir(dirName, false);
        if (tLibDir == null) {
            logger.warning(stringManager.getString(
                "upgrade.common.trg_lib_dir_not_found", tLibDir + "/../lib"));
            logUserMustCopyFiles();
            return;
        }

        // these should be canonical files
        if (sLibDir.equals(tLibDir)) {
            logger.info(stringManager.getString(
                "upgrade.common.sourceIsTarget"));
            return;
        }
        UpgradeUtils u = UpgradeUtils.getUpgradeUtils(commonInfo);
        u.copyUserLibFiles(sLibDir, tLibDir);
        logger.info(stringManager.getString(
            "upgrade.common.finished_copy_user_libs"));
    }

    private void logUserMustCopyFiles() {
        logger.warning(stringManager.getString(
            "upgrade.common.warning_user_must_copy_file"));
    }

    /*
     * Not an exhaustive search. In the case of a domain, look
     * for ../../lib. Otherwise, ../lib. Will return null if
     * the directory is not found.
     */
    private File findLibDir(String source, boolean isDomain) {
        StringBuilder path = new StringBuilder();
        path.append(source);
        if (isDomain) {
            path.append(File.separatorChar);
            path.append("..");
        }
        path.append(File.separatorChar);
        path.append("..");
        path.append(File.separatorChar);
        path.append("lib");
        String fullPath = path.toString();

        // let the File constructor do the works
        File retFile = new File(fullPath);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("Source path is %s", source));
            logger.fine(String.format("isDomain=%s", isDomain));
            logger.fine(String.format("built lib path is %s", fullPath));
        }
        if (retFile.exists() && retFile.isDirectory()) {
            try {
                // need canonical path or else equals() fails
                return retFile.getCanonicalFile();
            } catch (IOException ioe) {
                logger.log(Level.FINE, "Can't create canonical file from " +
                    retFile.getPath(), ioe);
                return null;
            }
        }
        return null;
    }

}
