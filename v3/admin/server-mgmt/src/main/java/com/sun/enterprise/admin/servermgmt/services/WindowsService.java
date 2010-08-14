/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.admin.servermgmt.services;

import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.ObjectAnalyzer;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.io.ServerDirs;
import java.io.*;
import java.util.*;
import java.util.Map;
import static com.sun.enterprise.admin.servermgmt.services.Constants.*;

/**
 * Warning: there is lots of file twiddling going on in this class.  It is the nature
 * of the beast.
 * @author Byron Nevins
 */
public class WindowsService extends NonSMFServiceAdapter {

    static boolean apropos() {
        return OS.isWindowsForSure();
    }

    WindowsService() {
        if (!apropos()) {
            // programmer error
            throw new IllegalArgumentException(Strings.get("internal.error",
                    "Constructor called but Windows Services are not available."));
        }
    }

    @Override
    public final boolean isConfigValid() {
        return true;
    }

    @Override
    public final void createService(Map<String, String> params) throws RuntimeException {
        try {
            init();
            trace("**********   Object Dump  **********\n" + this.toString());

            if (uninstall() == 0 && !isDryRun())
                System.out.println(Strings.get("windows.services.uninstall.good"));
            else
                trace("No preexisting Service with that id and/or name was found");

            install();
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        catch (ProcessManagerException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public final String getSuccessMessage() {
        if (isDryRun())
            return Strings.get("dryrun");

        return Strings.get("WindowsServiceCreated", getName(),
                serverName + " GlassFish Server", dirs.getServerDir(), targetXml, targetWin32Exe);
    }

    @Override
    public final void writeReadmeFile(String msg) {
        // TODO 1/19/2010 bnevins duplicated in SMFService
        File f = new File(getServerDirectory(), "PlatformServices.log");
        ServicesUtils.appendTextToFile(f, msg);
    }

    @Override
    public final File getServerDirectory() {
        return new File(getLocation());
    }

    @Override
    public String toString() {
        return ObjectAnalyzer.toString(this);
    }

    @Override
    public final String getLocationArgsStart(ServerDirs dirs) {
        if (isDomain()) {
            return makeStartArg("--domaindir")
                    + makeStartArg(dirs.getServerParentDir().getPath());
        }
        else {
            return makeStartArg("--nodedir")
                    + makeStartArg(dirs.getServerGrandParentDir().getPath().replace('\\', '/'))
                    + makeStartArg("--node")
                    + makeStartArg(dirs.getServerParentDir().getName());
        }
    }

    @Override
    public final String getLocationArgsStop(ServerDirs dirs) {
        if (isDomain()) {
            return makeStopArg("--domaindir")
                    + makeStopArg(dirs.getServerParentDir().getPath());
        }
        else {
            return makeStopArg("--nodedir")
                    + makeStopArg(dirs.getServerGrandParentDir().getPath().replace('\\', '/'))
                    + makeStopArg("--node")
                    + makeStopArg(dirs.getServerParentDir().getName());
        }
    }

    ///////////////////////////////////////////////////////////////////////
    //////////////////////////   ALL PRIVATE BELOW    /////////////////////
    ///////////////////////////////////////////////////////////////////////
    private void init() throws IOException {
        trace("In init");
        setInstallRootDir();
        setLibDir();
        setTemplateFile();
        setSourceWin32Exe();

        dirs = new ServerDirs(SmartFile.sanitize(new File(getLocation())));
        serverName = dirs.getServerName();
        targetDir = new File(dirs.getServerDir(), TARGET_DIR);
        targetDir.mkdirs(); // just in case...

        if (!targetDir.isDirectory())
            throw new RuntimeException(Strings.get("noTargetDir", targetDir));

        targetWin32Exe = new File(targetDir, serverName + "Service.exe");
        targetXml = new File(targetDir, serverName + "Service.xml");

        handlePreExisting(targetWin32Exe, targetXml, isForce());
        FileUtils.copy(sourceWin32Exe, targetWin32Exe);
        trace("Copied from " + sourceWin32Exe + " to " + targetWin32Exe);

        // TODO move constants to its own class
        Map<String, String> map = new HashMap<String, String>();
        map.put(ENTITY_NAME_TN, serverName);
        map.put(DATE_CREATED_TN, getDate());
        map.put(SERVICE_NAME_TN, getName());
        map.put(AS_ADMIN_PATH_TN, getAsadminPath().replace('\\', '/'));
        map.put(CFG_LOCATION_TN, dirs.getServerParentDir().getPath().replace('\\', '/'));
        map.put(CREDENTIALS_START_TN, getAsadminCredentials("startargument"));
        map.put(CREDENTIALS_STOP_TN, getAsadminCredentials("stopargument"));
        map.put(START_COMMAND_TN, getStartCommand());
        map.put(STOP_COMMAND_TN, getStopCommand());
        map.put(LOCATION_ARGS_START_TN, getLocationArgsStart(dirs));
        map.put(LOCATION_ARGS_STOP_TN, getLocationArgsStop(dirs));

        trace("MAP --> " + map.toString());

        ServicesUtils.tokenReplaceTemplateAtDestination(
                map, templateFile.getPath(), targetXml.getPath());
        trace("Target XML file written: " + targetXml);
    }

    private void setSourceWin32Exe() throws IOException {
        sourceWin32Exe = new File(libDir, SOURCE_WIN32_EXE_FILENAME);

        if (!sourceWin32Exe.isFile()) {
            // copy it from inside this jar to the file system
            InputStream in = getClass().getResourceAsStream("/lib/" + SOURCE_WIN32_EXE_FILENAME);
            FileOutputStream out = new FileOutputStream(sourceWin32Exe);
            copyStream(in, out);
            trace("Copied from inside the jar to " + sourceWin32Exe);
        }
        trace("Source executable: " + sourceWin32Exe);
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[16384];
        int len;
        while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private void setInstallRootDir() {
        String ir = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);

        if (!ok(ir))
            throw new RuntimeException(Strings.get("internal.error", "System Property not set: "
                    + SystemPropertyConstants.INSTALL_ROOT_PROPERTY));

        installRootDir = SmartFile.sanitize(new File(ir));

        if (!installRootDir.isDirectory())
            throw new RuntimeException(Strings.get("internal.error",
                    "Not a directory: " + installRootDir));
    }

    private void setLibDir() {
        libDir = SmartFile.sanitize(new File(installRootDir, "lib"));

        if (!libDir.isDirectory())
            throw new RuntimeException(Strings.get("internal.error",
                    "Not a directory: " + libDir));
    }

    private void setTemplateFile() {
        templateFile = new File(libDir, "install/templates/" + TEMPLATE_FILE_NAME);

        
        
        if(SUPER_DEBUG) {
            templateFile = new File(   
            "C:/gf/v3/admin/server-mgmt/src/main/java/com/sun/enterprise/admin/servermgmt/services/Domain-service-winsw.xml.template");
            System.out.println("WARNING !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("WARNING !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    /**
     * If we had a crude "Template Language" we could do some if/else stuff
     * right in the template.  We don't have that and it is not worth the development
     * cost to add it.  So what we do is just drop %%%CREDENTIALS%%% into the xml
     * template at the right place.  We replace with one space character for default
     * credentials.  If there ARE credentials we replace with XML elements
     *
     * @return the hunk of XML
     */
    private String getAsadminCredentials(String elem) {
        // 1 -- no auth of any kind needed -- by definition when there is no
        // password file
        // note: you do NOT want to give a "--user" arg -- it can only appear
        // if there is a password file too
        if (!ok(getPasswordFilePath()))
            return " ";

        // 2. --
        String user = getAppserverUser(); // might be null

        String begin = "<" + elem + ">";
        String end = "</" + elem + ">\n";
        StringBuilder sb = new StringBuilder();

        if (user != null) {
            sb.append(" " + begin + "--user" + end);
            sb.append("  " + begin + user + end);
        }
        sb.append("  " + begin + "--passwordfile" + end);
        sb.append("  " + begin + getPasswordFilePath() + end);
        sb.append("  "); // such obsessive attention to detail!!! :-)

        return sb.toString();
    }

    private void trace(String s) {
        if (isTrace())
            System.out.println(TRACE_PREPEND + s);
    }

    private int uninstall() throws ProcessManagerException {
        if (isDryRun() || !targetWin32Exe.canExecute())
            return 0;
        // it is NOT an error to not be able to uninstall
        ProcessManager mgr = new ProcessManager(targetWin32Exe.getPath(), "uninstall");
        mgr.execute();
        trace("Uninstall STDERR: " + mgr.getStderr());
        trace("Uninstall STDOUT: " + mgr.getStdout());
        return mgr.getExitValue();
    }

    private void install() throws ProcessManagerException {
        // it IS an error to not be able to install

        if (isDryRun()) {
            // dry-run not so useful on Windows.  Very useful on UNIX...
            if(!SUPER_DEBUG)
                targetXml.delete();

            targetWin32Exe.delete();
        }
        else {
            ProcessManager mgr = new ProcessManager(targetWin32Exe.getPath(), "install");
            mgr.execute();
            int ret = mgr.getExitValue();

            if (ret != 0)
                throw new RuntimeException(Strings.get("windows.services.install.bad",
                        "" + ret, mgr.getStdout(), mgr.getStderr()));

            trace("Install STDERR: " + mgr.getStderr());
            trace("Install STDOUT: " + mgr.getStdout());
        }
    }

    private void handlePreExisting(File targetWin32Exe, File targetXml, boolean force) {
        if (targetWin32Exe.exists() || targetXml.exists()) {
            if (force) {
                targetWin32Exe.delete();
                targetXml.delete();
                // we call this same method to make sur they were deleted
                handlePreExisting(targetWin32Exe, targetXml, false);
            }
            else {
                throw new RuntimeException(Strings.get("windows.services.alreadyCreated", new File(targetDir, serverName + "Service")));
            }
        }
    }

    private String makeStartArg(String s) {
        return "  " + START_ARG_START + s + START_ARG_END + "\n";
    }

    private String makeStopArg(String s) {
        return "  " + STOP_ARG_START + s + STOP_ARG_END + "\n";
    }


    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private static final String TRACE_PREPEND = "TRACE:  ";
    private static final String SOURCE_WIN32_EXE_FILENAME = "winsw.exe";
    private static final String TARGET_DIR = "bin";
    private static final String TEMPLATE_FILE_NAME = "Domain-service-winsw.xml.template";
    private static final String CREDENTIALS_START_TN = "CREDENTIALS_START";
    private static final String CREDENTIALS_STOP_TN = "CREDENTIALS_STOP";
    private String serverName;
    private File sourceWin32Exe;
    private File targetDir;
    private File targetXml;
    private File targetWin32Exe;
    private File installRootDir;
    private File libDir;
    private File templateFile;
    private ServerDirs dirs;
    private final static boolean SUPER_DEBUG;

    static {
        boolean a = System.getProperty("user.name").equals("bnevins");
        String s = System.getenv("AS_SUPER_DEBUG");
        boolean b = Boolean.parseBoolean(s);
        if(a && b) 
            SUPER_DEBUG = true;
        else
            SUPER_DEBUG = false;
    }
}
