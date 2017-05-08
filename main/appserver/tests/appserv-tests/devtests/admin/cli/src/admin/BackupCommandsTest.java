/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package admin;

import java.io.*;
import java.net.*;

/*
 * Dev test for DAS recovery commands (backup-domain, restore-domain,
 * list-backups) @author Byron Nevins @author Yamini K B @author Chris Kasso
 */
public class BackupCommandsTest extends AdminBaseDevTest {
    public BackupCommandsTest() {
        String host0 = null;

        try {
            host0 = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception e) {
            host0 = "localhost";
        }
        host = host0;
        System.out.println("Host= " + host);
        glassFishHome = getGlassFishHome();
        System.out.println("GF HOME = " + glassFishHome);
    }

    public static void main(String[] args) {
        if (isHadas())
            BACKUP_FILE = NEW_BACKUP_FILE;
        else
            BACKUP_FILE = OLD_BACKUP_FILE;

        new BackupCommandsTest().run();
    }

    @Override
    public String getTestName() {
        return "DAS backup and recovery";
    }

    @Override
    protected String getTestDescription() {
        return "Developer tests for backup-domain/restore-domain/list-backups";
    }

    @Override
    public void subrun() {
        testCommandsWithDefaultOptions();
        testCommandsWithDASRunning();
        testCommandsWithOperands();
        testBackupDirOption();
        testRestoreWithFileName();
        testCommandsWithNoDomains();
        testCommandsWithMultipleDomains();
        stat.printSummary();
    }

    private void testCommandsWithDASRunning() {
        asadmin("start-domain");

        // backup folder doesn't exist
        // perform a backup
        report("backup-domain-DAS-running", !asadmin("backup-domain"));

        // list backup
        report("list-backups-DAS-running", asadmin("list-backups"));

        // restore backup
        report("restore-domain-DAS-running", !asadmin("restore-domain", FORCE_OPTION));

        asadmin("stop-domain");
    }

    private void testCommandsWithDefaultOptions() {

        // perform a backup
        report("backup-domain-no-operand", asadmin("backup-domain"));

        // list backup
        report("list-backups-no-operand", asadmin("list-backups"));

        // restore backup
        report("restore-domain-no-operand", !asadmin("restore-domain"));

        // force restore backup
        report("force-restore-domain-no-operand", asadmin("restore-domain", FORCE_OPTION));
    }

    private void testCommandsWithOperands() {

        // perform a backup
        report("backup-domain-with-operand", asadmin("backup-domain", DOMAIN1));

        // list backup
        report("list-backups-with-operand", asadmin("list-backups", DOMAIN1));

        // restore backup
        report("restore-domain-with-operand", asadmin("restore-domain", DOMAIN1));

    }

    private void testBackupDirOption() {
        AsadminReturn ret;

        // perform a backup
        report("backup-domain-with-backupdir", asadmin("backup-domain", "--backupdir", BACKUP_DIR, DOMAIN1));

        // list backup
        report("list-backups-with-backupdir", asadmin("list-backups", "--backupdir", BACKUP_DIR));

        // list backup invalid domain
        report("list-backups-with-invalid-operand", !asadmin("list-backups", "--backupdir", BACKUP_DIR, "foo"));

        // test for absolute path
        report("list-backups-with-invalid-backupdir", !asadmin("list-backups", "--backupdir", "foo"));

        // test recovery from backupdir.  Ensure backups within the domain
        // directory are preserved.
        asadmin("backup-domain", "--backupdir", BACKUP_DIR, DOMAIN1);
        ret = asadminWithOutput("list-backups");
        String existingDomainBackups = ret.out;
        report("restore-domain-with-valid-backupdir", asadmin("restore-domain", "--backupdir", BACKUP_DIR, DOMAIN1));
        ret = asadminWithOutput("list-backups");

        report("restore-domain-with-backupdir-preserves-backups", ret.out.equals(existingDomainBackups));

        cleanupBackupDir();

    }

    private void cleanupBackupDir() {
        File path = new File(BACKUP_DIR, DOMAIN1);
        File[] paths = path.listFiles();
        if (paths != null) {
            for (File f : paths) {
                f.delete();
            }
        }
        path.delete();
    }

    private void testRestoreWithFileName() {

        // specify a different domain name
        report("restore-domain-with-filename", !asadmin("restore-domain", FILENAME_OPTION, "foo", DOMAIN2));

        // force restore
        report("force-restore-domain-with-operand", !asadmin("restore-domain", FORCE_OPTION, DOMAIN2));

        report("restore-domain-with-filename-1", !asadmin("restore-domain", FILENAME_OPTION, BACKUP_FILE));

        report("restore-domain-with-filename-2", asadmin("restore-domain", FILENAME_OPTION, BACKUP_FILE, FORCE_OPTION));

        report("restore-domain-with-filename-3", asadmin("restore-domain", FILENAME_OPTION, BACKUP_FILE, FORCE_OPTION, "r-domain2"));

        //remove the domains
        report("delete-domain-r-domain2", asadmin("delete-domain", "r-domain2"));
        report("delete-domain-domain2", asadmin("delete-domain", DOMAIN2));
    }

    private void testCommandsWithNoDomains() {

        // delete the domain
        asadmin("delete-domain", DOMAIN1);

        // perform a backup
        report("backup-domain-empty-domaindir", !asadmin("backup-domain"));

        // list backup
        report("list-backups-empty-domaindir", !asadmin("list-backups"));

        // restore backup
        report("restore-domain-empty-domaindir", !asadmin("restore-domain"));

        // put back domain1
        asadmin("create-domain", "--nopassword", DOMAIN1);
    }

    private void testCommandsWithMultipleDomains() {

        // create domain2
        report("create-domain2-for-backup", asadmin("create-domain", "--nopassword", DOMAIN2));

        // perform a backup
        report("backup-domain-multiple-domains-in-domaindir", !asadmin("backup-domain"));

        // perform a backup
        report("backup-domain1-multiple-explicit-arg", asadmin("backup-domain", DOMAIN1));

        // list backup
        report("list-backups-multiple-domains-in-domaindir", !asadmin("list-backups"));

        // list backup with operand
        report("list-backups-with-operand-multiple-domains-in-domaindir", asadmin("list-backups", DOMAIN1));

        // restore backup
        report("restore-domain-multiple-domains-in-domaindir", !asadmin("restore-domain", FORCE_OPTION));

        report("start-domain1-backup", asadmin("start-domain", DOMAIN1));

        // perform a backup on domain2 while domain1 is running (13463)
        report("backup-domain-multiple-domains-in-domaindir-13463", asadmin("backup-domain", DOMAIN2));

        report("stop-domain1", asadmin("stop-domain", DOMAIN1));

        //delete domain2
        report("delete-domain2", asadmin("delete-domain", DOMAIN2));

    }
    private final String host;
    private final File glassFishHome;
    private static boolean HADAS = false;
    private static final String DOMAIN1 = "domain1";
    private static final String DOMAIN2 = "domain2";
    private static final String FORCE_OPTION = "--force";
    private static final String FILENAME_OPTION = "--filename";
    private static String BACKUP_FILE;
    private static final String NEW_BACKUP_FILE = "resources/backups/domain2_2012_04_26_v00001.zip";
    private static final String OLD_BACKUP_FILE = "resources/backups/domain2_2010_07_19_v00001.zip";
    private static final String BACKUP_DIR = System.getenv("APS_HOME") + "/devtests/admin/cli/backupdir";
}
