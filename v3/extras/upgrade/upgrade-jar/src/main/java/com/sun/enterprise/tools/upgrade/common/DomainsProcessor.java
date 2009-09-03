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

package com.sun.enterprise.tools.upgrade.common;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.LogService;

import java.io.File;
import java.util.StringTokenizer;
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

    private boolean domainStarted = false;
		
    public DomainsProcessor(CommonInfoModel ci) {
        this.commonInfo = ci;
    }

    public int startDomain(String domainName) throws HarnessException {
        int exitValue = 0;
        if (!domainStarted) {
            exitValue = Commands.startDomain(domainName, commonInfo);
            if (exitValue == 0) {
                domainStarted = true;
            } else {
                throw new HarnessException(stringManager.getString("upgrade.common.domain_start_failed", domainName));
            }
        }
        return exitValue;
    }

    /**
     * Copy any user files in the src server's lib dir to the target server's
     * lib dir.
     */
    public void copyUserLibFiles() {
        logger.log(Level.INFO,
            stringManager.getString("upgrade.common.start_copy_user_libs"));
        String s = commonInfo.getSource().getInstallDir();
        File sLibDir = null;
        StringTokenizer t = new StringTokenizer(s, File.separator);
        if (t.countTokens() > 1) {
            File tmpF = new File(s);
            sLibDir = new File(tmpF.getParentFile().getParentFile(), "lib");
            if (!sLibDir.exists() || !sLibDir.isDirectory()) {
                logger.log(Level.WARNING,
                    stringManager.getString("upgrade.common.dir_not_found", sLibDir.getAbsolutePath()));
                sLibDir = null;
            }
        }
        if (sLibDir == null) {
            logger.log(Level.FINE,
                stringManager.getString("upgrade.common.src_lib_dir_not_found", s + "/lib"));
            logger.log(Level.WARNING,
                stringManager.getString("upgrade.common.warning_user_must_copy_file"));
        } else {
            File tLibDir = null;
            s = commonInfo.getTarget().getInstallDir();
            t = new StringTokenizer(s, File.separator);
            if (t.countTokens() > 1) {
                File tmpF = new File(s);
                tLibDir = new File(tmpF.getParentFile(), "lib");
                if (!tLibDir.exists() || !tLibDir.isDirectory()) {
                    logger.log(Level.FINE,
                        stringManager.getString("upgrade.common.dir_not_found", tLibDir.getAbsolutePath()));
                    tLibDir = null;
                }
            }
            if (tLibDir == null) {
                logger.log(Level.WARNING,
                    stringManager.getString("upgrade.common.trg_lib_dir_not_found", s + "/lib"));
                logger.log(Level.WARNING,
                    stringManager.getString("upgrade.common.warning_user_must_copy_file"));
            } else {
                UpgradeUtils u = UpgradeUtils.getUpgradeUtils(commonInfo);
                u.copyUserLibFiles(sLibDir, tLibDir);
            }
        }
        logger.log(Level.INFO,
            stringManager.getString("upgrade.common.finished_copy_user_libs"));
    }
}
