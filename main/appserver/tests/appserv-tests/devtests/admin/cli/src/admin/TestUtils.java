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

import admin.util.ProcessUtils;
import com.sun.appserv.test.BaseDevTest;
import com.sun.appserv.test.BaseDevTest.AsadminReturn;
import java.io.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Byron Nevins
 */
final class TestUtils {

    static boolean resetErrorFile() {
        boolean hadErrors = false;

        if(Constants.ERRORS_WERE_REPORTED_FILE.isFile())
            hadErrors = true;

        Constants.ERRORS_WERE_REPORTED_FILE.delete();

        return hadErrors;
    }

    static void setErrorFile() {
        try {
            Constants.ERRORS_WERE_REPORTED_FILE.createNewFile();
        }
        catch (IOException ex) {
            Logger.getLogger(TestUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private TestUtils() {
        // all-static class!
    }

    static File createPasswordFile() throws IOException {
        File f = File.createTempFile("password_junk", ".txt");
        //f.deleteOnExit();   // just in case
        PrintStream pwfile = new PrintStream(f);
        pwfile.println("AS_ADMIN_PASSWORD=admin123");
        pwfile.println("AS_ADMIN_MASTERPASSWORD=admin123");
        pwfile.close();
        return f;
    }

    public static String unecho(String s) {
        // remove the huge echo'd command from some output
        // it will be [enormous ugly command]EOL[output from command]

        // note that this will work for "\r\n" as well
        int index = s.indexOf('\n');

        if (index > 0)
            return s.substring(index);

        return s;
    }

    /**
     * If a system property has a value of the form "${propname}", then expand
     * it. If "propname" is not an existing Java system property then return
     * null.
     *
     * We use this mainly because in ant the test may be invoked with something
     * like this:
     * <jvmarg value="-Dssh.installdir=${ssh.installdir}"/>
     * If if ssh.installdir is not a defined property then ant will just pass
     * "${ssh.installdir}" as the value for ssh.installdir. In this case we
     * rather have the value be null to know the property was not set.
     *
     * @param propName
     * @return
     */
    static String getExpandedSystemProperty(String propName) {
        String value = System.getProperty(propName);
        if (value == null) {
            return null;
        }
        if (value.startsWith("${")) {
            int index1 = value.indexOf("{");
            int index2 = value.indexOf("}");
            String substring = value.substring(index1 + 1, index2);
            if (propName.equals(substring)) {
                // Have something like foo=${foo}. Can't expand, return null;
                return null;
            }
            return getExpandedSystemProperty(substring);
        }
        else {
            return value;
        }
    }

    public static void writeCommandToDebugLog(String message) {
        if (LOGFILE == null)
            return;

        File log = new File(LOGFILE);

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(log, true));
            //out.write("\n");
            out.write(message);
            //out.write("\n");
        }
        catch (IOException e) {
            // It is just a debug file.
        }
        finally {
            if (out != null) {
                try {
                    out.write("\n");
                }
                catch (Exception e) {
                    // ignore
                }
                try {
                    out.close();
                }
                catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    public static void writeErrorToDebugLog(AsadminReturn ret) {
        writeErrorToDebugLog(ret, null);
    }

    public static void writeErrorToDebugLog(AsadminReturn ret, String extraMessage) {
        StringBuilder msg = new StringBuilder(STARS).append('\n');
        msg.append(new Date().toString());
        msg.append("   TEST Failure.  Expected asadmin to return ").append(!ret.returnValue).append('\n');
        msg.append("OUTPUT: \n").append(ret.outAndErr);

        if (extraMessage != null && !extraMessage.isEmpty()) {
            msg.append(extraMessage);
            msg.append('\n');
        }
        msg.append('\n');
        msg.append(STARS);
        writeCommandToDebugLog(msg.toString());

        if (getEnvOrPropBoolean(Constants.FAIL_FAST)) {
            System.out.println("*************************************************************");
            System.out.println("*************************************************************");
            System.out.println("***   AS_TESTS_FAIL_FAST is true -- exiting out NOW!!!! *****");
            System.out.println("*************************************************************");
            System.out.println("*************************************************************");
            SystemExit();
        }
    }

    public static boolean getEnvOrPropBoolean(String name) {
        // if the string is null -- it simply returns false
        return Boolean.parseBoolean(getEnvOrPropString(name));
    }

    public static int getEnvOrPropInt(String name) {
        // if the string is null -- it returns -1
        try {
            return Integer.parseInt(getEnvOrPropString(name));
        }
        catch (Exception e) {
            return -1;
        }
    }

    public static String getEnvOrPropString(String name) {
        // System properties override env. variables
        String envVal = System.getenv(name);
        String sysPropVal = System.getProperty(name);

        if (sysPropVal != null)
            return sysPropVal;
        else
            return envVal;
    }

    public static void SystemExit() {
        ProcessUtils.killJvm("ASMain");
        ProcessUtils.killJvm("AsadminMain");
        ProcessUtils.killJvm("DerbyControl");
        ProcessUtils.killJvm("admin-cli.jar");
        ProcessUtils.killJvm("derbyrun.jar");
        ProcessUtils.killJvm("glassfish.jar");
        System.exit(1);
    }
    private final static String LOGFILE = System.getenv("AS_LOGFILE");
    private static final String STARS = "****************************************";
}
