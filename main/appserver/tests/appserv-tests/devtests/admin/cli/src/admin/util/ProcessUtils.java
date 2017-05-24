/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package admin.util;

import java.io.*;
import java.lang.management.ManagementFactory;

import java.util.*;

/**
 * Lifted verbatim from GlassFish common-util
 * @author Byron Nevins
 */
public final class ProcessUtils {
    private ProcessUtils() {
        // all static class -- no instances allowed!!
    }

    // for informal testing.  Too difficult to make a unit test...
    public static void main(String[] args) {
        debug = true;
        for (String s : args) {
            String ret = killJvm(s);

            if(ret == null)
                ret = "SUCCESS!!";

            System.out.println(s + " ===> " + ret);
        }
    }

    public static File getExe(String name) {
        for (String path : paths) {
            File f = new File(path + "/" + name);

            if (f.canExecute()) {
                return f.getAbsoluteFile();
            }
        }
        return null;
    }

    /**
     * Try and find the Process ID of "our" process.
     * @return the process id or -1 if not known
     */
    public static final int getPid() {
        return pid;
    }

    /**
     * Kill the process with the given Process ID.
     * @param pid
     * @return a String if the process was not killed for any reason including if it does not exist.
     *  Return null if it was killed.
     */
    public static String kill(int pid) {
        try {
            String pidString = Integer.toString(pid);
            ProcessManager pm = null;
            String cmdline;

            if (isWindows()) {
                pm = new ProcessManager("taskkill", "/F", "/T", "/pid", pidString);
                cmdline = "taskkill /F /T /pid " + pidString;
            }
            else {
                pm = new ProcessManager("kill", "-9", "" + pidString);
                cmdline = "kill -9 " + pidString;
            }

            pm.setEcho(false);
            pm.execute();
            int exitValue = pm.getExitValue();

            if (exitValue == 0)
                return null;
            else
                return "Error killing pid #" + pid;
        }
        catch (ProcessManagerException ex) {
            return ex.getMessage();
        }
    }

    /**
     * Kill the JVM with the given main classname.  The classname can be fully-qualified
     * or just the classname (i.e. without the package name prepended).
     * @param pid
     * @return a String if the process was not killed for any reason including if it does not exist.
     *  Return null if it was killed.
     */
    public static String killJvm(String classname) {
        List<Integer> pids = Jps.getPid(classname);
        StringBuilder sb = new StringBuilder();
        int numDead = 0;

        for (int pid : pids) {
            String s = kill(pid);
            if (s != null)
                sb.append(s).append('\n');
            else {
                ++numDead;
                System.out.println("KILLED " + classname + " with PID: " + pid);
            }
        }
        String err = sb.toString();

        if (err.length() > 0 || numDead <= 0)
            return err + " " + numDead + " killed successfully.";

        return null;
    }

    /**
     * If we can determine it -- find out if the process that owns the given
     * process id is running.
     * @param aPid
     * @return true if it's running, false if not and null if we don't know.
     * I.e the return value is a true tri-state Boolean.
     */
    public static final Boolean isProcessRunning(int aPid) {
        try {
            if (isWindows())
                return isProcessRunningWindows(aPid);
            else
                return isProcessRunningUnix(aPid);
        }
        catch (Exception e) {
            return null;
        }
    }
    //////////////////////////////////////////////////////////////////////////
    //////////     all private below     /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    private static final int pid;
    private static final String[] paths;

    private static boolean isProcessRunningWindows(int aPid) throws ProcessManagerException {
        String pidString = Integer.toString(aPid);
        ProcessManager pm = new ProcessManager("tasklist", "/FI", "\"pid eq " + pidString + "\"");
        pm.setEcho(false);
        pm.execute();
        String out = pm.getStdout() + pm.getStderr();

        /* output is either 
        (1) 
        INFO: No tasks running with the specified criteria.
        (2) 
        Image Name                   PID Session Name     Session#    Mem Usage
        ========================= ====== ================ ======== ============
        java.exe                    3760 Console                 0     64,192 K
         */

        if (debug) {
            System.out.println("------------   Output from tasklist   ----------");
            System.out.println(out);
            System.out.println("------------------------------------------------");
        }

        if (ok(out)) {
            if (out.indexOf("" + aPid) >= 0)
                return true;
            else
                return false;
        }

        throw new ProcessManagerException("unknown");
    }

    private static Boolean isProcessRunningUnix(int aPid) throws ProcessManagerException {
        ProcessManager pm = new ProcessManager("kill", "-0", "" + aPid);
        pm.setEcho(false);
        pm.execute();
        int retval = pm.getExitValue();
        return retval == 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    static {
        // variables named with 'temp' are here so that we can legally set the
        // 2 final variables above.

        int tempPid = -1;

        try {
            String pids = ManagementFactory.getRuntimeMXBean().getName();
            int index = -1;

            if (ok(pids) && (index = pids.indexOf('@')) >= 0) {
                tempPid = Integer.parseInt(pids.substring(0, index));
            }
        }
        catch (Exception e) {
            tempPid = -1;
        }
        // final assignment
        pid = tempPid;

        String tempPaths = null;

        if (isWindows()) {
            tempPaths = System.getenv("Path");

            if (!ok(tempPaths))
                tempPaths = System.getenv("PATH"); // give it a try
        }
        else {
            tempPaths = System.getenv("PATH");
        }

        if (ok(tempPaths))
            paths = tempPaths.split(File.pathSeparator);
        else
            paths = new String[0];
    }
    private static boolean debug;
    public static boolean isWindows() {
        String osname = System.getProperty("os.name");
        
        if(osname == null || osname.length() <= 0)
            return false;
        
        // case insensitive compare...
        osname	= osname.toLowerCase();
        
        if(osname.indexOf("windows") >= 0)
            return true;
        
        return false;
        
    }

    static boolean ok(String s) {
        return s != null && s.length() > 0;
    }
}
