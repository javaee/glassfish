/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.installer.conf;

import java.io.File;
import org.glassfish.installer.util.FileIOUtils;
import org.glassfish.installer.util.OSUtils;

/** PasswordFile to be used for asadmin create-domain command.
 * @author sathyan
 */
public class PasswordFile {

    /* Domain administrator password. */
    private String adminPassword;
    /* Master password. */
    private String masterPassword;
    /* Path of the file to be generated. */
    private File filePath;
    /* Prefix to the filename to be generated. */
    private String filePrefix;

    public PasswordFile(String adminPassword, String masterPassword, String filePrefix) {
        this.adminPassword = adminPassword;
        this.masterPassword = masterPassword;
        this.filePrefix = filePrefix;
    }

    /* Create password file under <filePath> with <filePrefix>.
     * @return boolean true if the file creation is successful, false otherwise.
     */
    public boolean setupPasswordFile() {
        boolean retStatus = true;
        try {
            filePath = File.createTempFile(filePrefix, null);
            FileIOUtils fUtil = new FileIOUtils();
            fUtil.openFile(filePath.getAbsolutePath());

            /* To make sure that the file is deleted, mark it for deletion
             * upon JVM exit. The caller will also be removing this file
             * immediately after the run of create-domain command.
             */
            filePath.deleteOnExit();

            /* The password could be null to support unauthenticated logins. */
            if (adminPassword != null && adminPassword.trim().length() > 0) {
                fUtil.appendLine("AS_ADMIN_PASSWORD=" + adminPassword);
            } else {
                fUtil.appendLine("AS_ADMIN_PASSWORD=");
            }
            fUtil.appendLine("AS_ADMIN_MASTERPASSWORD=" + masterPassword);
            fUtil.saveFile();
            fUtil.closeFile();

            /* Make it readable, setExecutable does that though it adds the execute
             * permission also :-)
             */
            if (!OSUtils.isWindows()) {
                org.glassfish.installer.util.FileUtils.setExecutable(filePath.getAbsolutePath());
            }
        } catch (Exception ex) {
            retStatus = false;
        }
        return true;
    }

    /* return String domain administrator password. */
    public String getAdminPassword() {
        return adminPassword;
    }

    /* @param adminPassword domain administrator password. */
    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    /* @return String Password file path as a String. */
    public String getPasswordFilePath() {
        return filePath.getAbsolutePath();
    }

    /* @param filePrefix Prefix to password file name, default is "asadmin". */
    public void setPasswordFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    /* return String master password. */
    public String getMasterPassword() {
        return masterPassword;
    }

    /* @param masterPassword master password. */
    public void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;
    }

    /* @return File Password file as a File object */
    public File getPasswordFile() {
        return this.filePath;
    }
}
