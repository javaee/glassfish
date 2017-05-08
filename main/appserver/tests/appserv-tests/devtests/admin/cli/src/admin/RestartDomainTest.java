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
import admin.util.FileUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test restarting a domain. XXX - for now just a few regression tests
 *
 * @author Bill Shannon
 * @author Byron Nevins
 */
public class RestartDomainTest extends AdminBaseDevTest {

    private boolean useAsPropsArgs = true;

    @Override
    public String antProp(final String key) {
        // this is what normally goes in (from the base class):
        // --user %s --passwordfile %s --host %s --port %s --echo=true --terse=true

        if (useAsPropsArgs)
            return super.antProp(key);
        else
            return "--echo=true";
    }

    @Override
    public String getTestName() {
        return "restart-domain";
    }

    @Override
    protected String getTestDescription() {
        return "Tests restarting a domain.";
    }

    public static void main(String[] args) {
        new RestartDomainTest().runTests();
    }

    private void runTests() {
        stopDomain();   // make sure local domain is not running before we start
        startDomain();
        mainRestartDomainTests();
        stopDomain();
        testRestartFakeDomain();
        testRestartForce();
        stopDomain();   // and just in case, make sure it's still not running
        jira16197();
        stat.printSummary();
    }

    /*
     * Check that restarting a non-existant remote domain fails
     * This is a regression test for GLASSFISH-9552.
     */
    void testRestartFakeDomain() {
        report("restart-domain",
                !asadmin("--host", "no-such-host", "restart-domain"));
        // check that the local domain is still not running
        report("uptime", !asadmin("uptime"));
    }

    /*
     * Check that restarting a stopped domain with --force works.
     * (--force should be ignored)
     * This is a regression test for GLASSFISH-16175.
     */
    void testRestartForce() {
        report("restart-domain", asadmin("restart-domain", "--force"));
        // check that the local domain is running
        report("uptime", asadmin("uptime"));
        stopDomain();
    }

    /**
     * The case where user fouled-up his passwordfile after starting. restart
     * should detect and emit a useful message. Before the fix it just would
     * fail to start with a 10minute timeout
     *
     * We need 2 password files with the same passwords inside. Because we will
     * use one of them for the start-domain command (which is stored inside
     * DAS). Then we delete that file and call restart-domain with the second
     * file. That's because asadmin needs the passwords to authenticate to
     * DAS... Complicated test? YES!! This test took longer to develop than the
     * bugfix!!
     */
    private void jira16197() {
        report("Regression Test of issue 16197", true);
        createPasswordFiles();
        try {
            setCannedFrameworkArgs("--passwordfile " + pwFile1Name + " --user admin");
            deletePasswordDomain(true); // OK if it does not exist
            createPasswordDomain();
            startAndRestartAndStopPasswordDomain();
            deletePasswordDomain(false); // report an error if applicable
        }
        finally {
            restoreCannedFrameworkArgs();
            report("before-delete-pw1-file", pwFile1.canRead());
            report("verify-delete-pw1-file", pwFile1.delete());
            pwFile2.delete(); // just in case...
        }
    }

    private void deletePasswordDomain(boolean errorOK) {
        if (!errorOK) {
            try {
                FileUtils.copy(PW_DOMAIN_LOGFILE_FROM, PW_DOMAIN_LOGFILE_TO);
            }
            catch (IOException ex) {
                report(false, "Cant-copy-pw-logfile: " + ex);
            }
        }

        boolean ret = asadmin("delete-domain", PW_DOMAIN_NAME);

        if (errorOK)
            ret = true;

        report("delete-pw-domain", ret);
    }

    private void createPasswordFiles() {
        try {
            pwFile1 = TestUtils.createPasswordFile();
            pwFile1Name = pwFile1.getAbsolutePath().replace('\\', '/');
            pwFile2 = TestUtils.createPasswordFile();
            pwFile2Name = pwFile2.getAbsolutePath().replace('\\', '/');
            report("create-password-file", true); // no exception thrown is a passed test!
            report("verify-password-file-exists", pwFile1.exists());
            report("verify-password-file-readable", pwFile1.canRead());
            report("verify-password-file-has-stuff", pwFile1.length() > 10);
            report("verify-password-file-exists", pwFile2.exists());
            report("verify-password-file-readable", pwFile2.canRead());
            report("verify-password-file-has-stuff", pwFile2.length() > 10);
        }
        catch (IOException ex) {
            report("Catastrophic IO Error", false);
            pwFile1 = null;
            pwFile2 = null;
            pwFile1Name = null;
            pwFile2Name = null;
        }
    }

    /**
     * Create a domain that requires the username/pw via a password file
     */
    private void createPasswordDomain() {
        report("create-passwordfile-domain", asadmin(CREATE_PW_DOMAIN_CMD));
    }

    /**
     * Start the domain that requires the username/pw via a password file
     * IMPORTANT!!! Use pwfile #2 !!!!
     */
    private void startAndRestartAndStopPasswordDomain() {
        setCannedFrameworkArgs("--passwordfile " + pwFile2Name + " --user admin");
        report("start-passwordfile-domain", asadmin("start-domain", PW_DOMAIN_NAME));
        setCannedFrameworkArgs("--passwordfile " + pwFile1Name + " --user adPW_DOMAIN_NAMEmin");

        String renamedFileName = pwFile2Name + "xxx";
        File renamedFile = new File(renamedFileName);



        // the running domain will use pwFile2 for all future restarts
        report("verify-can-restart", asadmin("restart-domain", PW_DOMAIN_NAME));
        report("verify-renamed-1", !renamedFile.canRead());
        pwFile2.renameTo(renamedFile);
        report("verify-renamed-2", !pwFile2.canRead());
        report("verify-renamed-3", renamedFile.canRead());

        // should not restart
        report("verify-can-not-restart", !asadmin("restart-domain", PW_DOMAIN_NAME));

        // restore the pw file
        renamedFile.renameTo(pwFile2);
        report("verify-restored-pwfile-1", !renamedFile.canRead());
        report("verify-restored-pwfile-2", pwFile2.canRead());
        report("verify-can-restart-once-again", asadmin("restart-domain", PW_DOMAIN_NAME));

        report("stop-passwordfile-domain", asadmin("stop-domain", PW_DOMAIN_NAME));
    }

    private void setCannedFrameworkArgs(String newArgs) {
        // framework automatically adds its own pw file etc.  By setting as.props
        // all of the canned args are not used.
        System.setProperty("as.props", newArgs);
    }

    private void restoreCannedFrameworkArgs() {
        if (oldCannedArgs == null)
            System.getProperties().remove("as.props");
        else
            System.setProperty("as.props", oldCannedArgs);
    }
    private String oldCannedArgs = System.getProperty("as.props");
    private File pwFile1;
    private String pwFile1Name;
    private File pwFile2;
    private String pwFile2Name;
    private final static String PW_DOMAIN_NAME = "pwdomain";
    private static final String[] CREATE_PW_DOMAIN_CMD = new String[]{
        "create-domain",
        "--savemasterpassword=true",
        "--usemasterpassword=true",
        "--savelogin=false",
        "--nopassword=false",
        PW_DOMAIN_NAME
    };
    private static final File GF_HOME = TestEnv.getGlassFishHome();
    private static final File PW_DOMAIN_LOGFILE_FROM = TestEnv.getDomainLog(PW_DOMAIN_NAME);
    private static final File PW_DOMAIN_LOGFILE_TO = new File(GF_HOME, PW_DOMAIN_NAME + ".log");

    private void mainRestartDomainTests() {
        try {
            useAsPropsArgs = false;
            restartDomainAsLocal();
            restartDomainAsRemote("garbage.no.exists.com", false);
            restartDomainAsRemote("localhost", true);
            restartDomainAsRemote(InetAddress.getLocalHost().getHostName(), true);

            String host = InetAddress.getLocalHost().getHostName();
            System.out.println("HOSTNAME == " + host);
            restartDomainAsRemote(host, true);
        }
        catch (UnknownHostException ex) {
            report("unknown-host-exception", false);
        }
        finally {
            useAsPropsArgs = true;
        }
    }

    private void restartDomainAsLocal() {
        report("uptime", asadmin("uptime"));
        report("restart-domain", asadmin("restart-domain"));
        // check that the local domain is running
        report("uptime", asadmin("uptime"));
    }

    /**
     * This specifically tests the fix for JIRA #20110
     * restart-domain behaves differently when you give the hostname as localhost versus ANY OTHER name.
     * Also the local mode is different from the remote mode.  We have to fool with as.props because the \
     * base class ALWAYS sticks in host/port parameters.
     */
    private void restartDomainAsRemote(String hostname, boolean expected) {
        report("uptime", asadmin("uptime"));
        report("restart-domain", expected == asadmin("--port", "4848",
                "--host", hostname, "restart-domain"));
        // check that the local domain is running
        report("uptime", asadmin("uptime"));
    }
}

/*
 * asadmin --passwordfile d:/password.txt --user admin create-domain --savemasterpassword=true --usemasterpassword=true --savelogin=false --nopassword=false d2
 */
