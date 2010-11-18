/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.enterprise.admin.servermgmt.services;

import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.ObjectAnalyzer;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.io.ServerDirs;
import java.io.File;
import java.lang.String;
import java.util.*;
import java.util.LinkedList;
import static com.sun.enterprise.admin.servermgmt.services.Constants.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Byron Nevins
 */
public class LinuxService extends NonSMFServiceAdapter {
    static boolean apropos() {
        if (LINUX_HACK)
            return true;

        return OS.isLinux();
    }

    LinuxService(ServerDirs dirs, AppserverServiceType type) {
        super(dirs, type);
        if (!apropos()) {
            // programmer error
            throw new IllegalArgumentException(Strings.get("internal.error",
                    "Constructor called but Linux Services are not available."));
        }
        etcDir = new File("/etc");

        // might be different for different flavors
        for (int i = 0; i < 7; i++) {
            // On OEL for instance the files are links to the real dir like this:
            //  /etc/rc0.d --> /etc/rc.d/rc0.d
            // let's use the REAL dirs just to be safe...
            rcDirs[i] = FileUtils.safeGetCanonicalFile(new File(etcDir, "rc" + i + ".d"));
        }
    }

    @Override
    public void initializeInternal() {
        try {
            getTokenMap().put(SERVICEUSER_START_TN, getServiceUserStart());
            getTokenMap().put(SERVICEUSER_STOP_TN, getServiceUserStop());
            setTemplateFile(TEMPLATE_FILE_NAME);
            checkFileSystem();
            setTarget();
            handlePreExisting(info.force);
            ServicesUtils.tokenReplaceTemplateAtDestination(getTokenMap(), getTemplateFile().getPath(), target.getPath());
            trace("Target file written: " + target);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public final void createServiceInternal() {
        try {
            trace("**********   Object Dump  **********\n" + this.toString());

            if (uninstall() == 0 && !info.dryRun)
                System.out.println(Strings.get("linux.services.uninstall.good"));
            else
                trace("No preexisting Service with that name was found");

            install();
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public final String getSuccessMessage() {
        if (info.dryRun)
            return Strings.get("dryrun");

        return Strings.get("LinuxServiceCreated",
                info.serviceName,
                info.type.toString(),
                target,
                getFinalUser(),
                target.getName());
    }

    // called by outside caller (createService)
    @Override
    public final void writeReadmeFile(String msg) {
        File f = new File(getServerDirs().getServerDir(), README);

        ServicesUtils.appendTextToFile(f, msg);
    }

    @Override
    public final String toString() {
        return ObjectAnalyzer.toStringWithSuper(this);
    }

    @Override
    public final String getLocationArgsStart() {
        if (isDomain()) {
            return " --domaindir " + getServerDirs().getServerParentDir().getPath() + " ";
        }
        else {
            return " --nodedir " + getServerDirs().getServerGrandParentDir().getPath()
                    + " --node " + getServerDirs().getServerParentDir().getName() + " ";
        }
    }

    @Override
    public final String getLocationArgsStop() {
        // exactly the same on Linux
        return getLocationArgsStart();
    }
    ///////////////////////////////////////////////////////////////////////
    //////////////////////////   ALL PRIVATE BELOW    /////////////////////
    ///////////////////////////////////////////////////////////////////////

    private void setRcDirs() {
        // Yes -- they differ on different platforms.
        // I Know what Sol10, Ubuntu, Debian, SuSE, RH and OEL look like

        
    }
    private void checkFileSystem() {
        File initd = new File(INITD);
        checkDir(initd, "no_initd");

        for (File f : rcDirs)
            checkDir(f, "no_rc");
    }

    /**
     * Make sure that the dir exists and that we can write into it
     */
    private void checkDir(File dir, String notDirMsg) {
        if (!dir.isDirectory())
            throw new RuntimeException(Strings.get(notDirMsg, dir));

        if (!dir.canWrite())
            throw new RuntimeException(Strings.get("no_write_dir", dir));
    }

    private void handlePreExisting(boolean force) {
        if (target.isFile()) {
            if (force) {
                target.delete();
                // we call this same method to make sure they were deleted
                handlePreExisting(false);
            }
            else {
                throw new RuntimeException(Strings.get("services.alreadyCreated", target, "rm"));
            }
        }
    }

    private void install() throws ProcessManagerException {
        createLinks();
    }

    // meant to be overridden bu subclasses
    int uninstall() {
        return deleteLinks();
    }

    private int deleteLinks() {
        trace("Deleting link files...");
        List<File> deathRow = new LinkedList<File>();
        if (!StringUtils.ok(targetName)) // invariant
            throw new RuntimeException("Programmer Internal Error");

        String regexp = REGEXP_PATTERN_BEGIN + targetName;
        for (File dir : rcDirs) {
            File[] matches = FileUtils.findFilesInDir(dir, regexp);

            if (matches.length < 1)
                continue; // perfectly normal
            else if (matches.length == 1)
                deathRow.add(matches[0]);
            else {
                tooManyLinks(matches);  // error!!
            }
        }

        // YES -- Error handling properly is ~~ 95% of the code in here!
        for (File f : deathRow) {
            if (!f.canWrite()) {
                throw new RuntimeException(Strings.get("cant_delete", f));
            }
        }

        // OK we shook out all the errors possible.  Now we do the irreversible stuff
        for (File f : deathRow) {
            if (info.dryRun) {
                dryRun("Would have deleted: " + f);
            }
            else {
                if (!f.delete())
                    throw new RuntimeException(Strings.get("cant_delete", f));
                else
                    trace("Deleted " + f);
            }
        }
        return deathRow.size();
    }

    private void createLinks() {
        for (File f : links) {
            String cmd = "ln -s " + target.getAbsolutePath() + " " + f.getAbsolutePath();
            if (LINUX_HACK)
                trace(cmd);
            else if (info.dryRun)
                dryRun(cmd);
            else
                createLink(f, cmd);
        }
    }

    private void createLink(File link, String cmd) {
        try {
            String[] cmds = new String[4];
            cmds[0] = "ln";
            cmds[1] = "-s";
            cmds[2] = target.getAbsolutePath();
            cmds[3] = link.getAbsolutePath();
            ProcessManager mgr = new ProcessManager(cmds);
            mgr.execute();
            trace("Create Link Output: " + mgr.getStdout() + mgr.getStderr());
            link.setExecutable(true, false);
        }
        catch (ProcessManagerException e) {
            throw new RuntimeException(Strings.get("ln_error", cmd, e));
        }
    }

    private void tooManyLinks(File[] matches) {
        // this is complicated enough to turn it into a method
        String theMatches = "";
        boolean first = true;
        for (File f : matches) {
            if (first)
                first = false;
            else
                theMatches += "\n";

            theMatches += f.getAbsolutePath();
        }
        throw new RuntimeException(Strings.get("too_many_links", theMatches));
    }

    private void setTarget() {
        targetName = "GlassFish_" + info.serverDirs.getServerName();
        target = new File(INITD + "/" + targetName);
        kFile = "K" + info.kPriority + targetName;
        sFile = "S" + info.sPriority + targetName;

        // Here is where we have the intricate knowledge of how *NIX Services work!
        links[0] = new File(rcDirs[0], kFile);
        links[1] = new File(rcDirs[1], kFile);
        links[6] = new File(rcDirs[6], kFile);

        links[2] = new File(rcDirs[2], sFile);
        links[3] = new File(rcDirs[3], sFile);
        links[4] = new File(rcDirs[4], sFile);
        links[5] = new File(rcDirs[5], sFile);

        if (info.trace) {
            trace("sfile: " + sFile);
            trace("kfile: " + kFile);
            trace("Link Files:");

            for (File f : links) {
                trace(f.getAbsolutePath());
            }
        }
    }

    private String getServiceUserStart() {
        // if the user is root (e.g. called with sudo and no serviceuser arg given)
        // then do NOT specify a user.
        // on the other hand -- if they specified one or they are logged in as a'privileged'
        // user then use that account.
        String u = getFinalUserButNotRoot();

        if (u != null)
            return "su --login " + u + " --command \"";

        return "";
    }

    private String getServiceUserStop() {
        if (StringUtils.ok(info.serviceUser))
            return "\"";
        return "";
    }

    private String[] getInstallCommand() {
        String[] cmds = new String[3];
        cmds[0] = UPDATER;
        cmds[1] = target.getName();
        cmds[2] = "defaults";

        return cmds;
    }

    private String[] getUninstallCommand() {
        String[] cmds = new String[3];
        cmds[0] = UPDATER;
        cmds[1] = target.getName();
        cmds[2] = "remove";

        return cmds;
    }

    private String getFinalUser() {
        if (StringUtils.ok(info.serviceUser))
            return info.serviceUser;
        else
            return info.osUser;
    }

    private String getFinalUserButNotRoot() {
        String u = getFinalUser();

        if ("root".equals(u))
            return null;

        return u;
    }
    private String targetName;
    File target;
    private static final String TEMPLATE_FILE_NAME = "linux-service.template";
    private static final String UPDATER = "update-rc.d";
    private final File[] rcDirs = new File[7];
    private final File[] links = new File[7];
    private final File etcDir;
    private String sFile;
    private String kFile;
}
