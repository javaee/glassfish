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

package com.sun.enterprise.admin.cli.cluster;

import java.io.*;
import java.net.ConnectException;
import java.util.*;
import java.util.logging.*;
import java.util.zip.*;
import javax.xml.bind.*;

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;

import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.cli.remote.RemoteCommand;
import com.sun.enterprise.util.cluster.SyncRequest;
import com.sun.enterprise.util.io.FileUtils;

/**
 * Synchronize a local server instance.
 */
@Service(name = "_synchronize-instance")
@Scoped(PerLookup.class)
public class SynchronizeInstanceCommand extends LocalInstanceCommand {

    @Param(name = "instance_name", primary = true, optional = true)
    private String instanceName0;

    @Param(name = "fullsync", optional = true)
    private boolean fullSync;

    private RemoteCommand syncCmd;

    private static enum SyncLevel { TOP, FILES, DIRECTORY, RECURSIVE };

    @Override
    protected void validate() throws CommandException {
        if (ok(instanceName0))
            instanceName = instanceName0;
        super.validate();
    }

    /**
     */
    @Override
    protected int executeCommand() throws CommandException {
        if (synchronizeInstance())
            return SUCCESS;
        else {
            logger.info(Strings.get("Sync.noConnect",
                                    programOpts.getHost(),
                                    Integer.toString(programOpts.getPort())));
            return ERROR;
        }
    }

    /**
     * Synchronize this server instance.  Return true if
     * server is synchronized.
     */
    protected boolean synchronizeInstance() throws CommandException {

        File dasProperties = getServerDirs().getDasPropertiesFile();
        logger.finer("das.properties: " + dasProperties);

        if (!dasProperties.exists()) {
            logger.info(
                Strings.get("Sync.noDASConfigured", dasProperties.toString()));
            return false;
        }

        setDasDefaults(dasProperties);

        /*
         * Create the remote command object that we'll reuse for each request.
         */
        syncCmd = new RemoteCommand("_synchronize-files", programOpts, env);
        syncCmd.setFileOutputDirectory(instanceDir);

        /*
         * If --syncfull, we remove all local state related to the instance,
         * then do a sync.  We only remove the local directories that are
         * synchronized from the DAS; any other local directories (logs,
         * instance-private state) are left alone.
         */
        if (fullSync) {
            logger.fine(
                                Strings.get("Instance.fullsync", instanceName));
            removeSubdirectory("config");
            removeSubdirectory("applications");
            removeSubdirectory("generated");
            removeSubdirectory("lib");
            removeSubdirectory("docroot");
        }

        try {
            /*
             * First, synchronize the config directory.
             */
            File domainXml =
                new File(new File(instanceDir, "config"), "domain.xml");
            long dtime = domainXml.lastModified();

            SyncRequest sr = getModTimes("config", SyncLevel.FILES);
            synchronizeFiles(sr);

            /*
             * Was domain.xml updated?
             * If not, we're all done.
             */
            if (domainXml.lastModified() == dtime) {
                logger.fine(Strings.get("Sync.alreadySynced"));
                return true;
            }

            /*
             * Now synchronize the applications.
             */
            sr = getModTimes("applications", SyncLevel.DIRECTORY);
            synchronizeFiles(sr);

            /*
             * Did we get any archive files?  If so,
             * have to unzip them in the applications
             * directory.
             */
            File appsDir = new File(instanceDir, "applications");
            File archiveDir = new File(appsDir, "__internal");
            for (File adir : FileUtils.listFiles(archiveDir)) {
                File[] af = FileUtils.listFiles(adir);
                if (af.length != 1) {
                    logger.finer("IGNORING " + adir +
                                                ", # files " + af.length);
                    continue;
                }
                File archive = af[0];
                File appDir = new File(appsDir, adir.getName());
                logger.finer("UNZIP " + archive + " TO " + appDir);
                try {
                    expand(appDir, archive);
                } catch (Exception ex) { }
            }

            FileUtils.whack(archiveDir);

            /*
             * Next, the libraries.
             * We assume there's usually very few files in the
             * "lib" directory so we check them all individually.
             */
            sr = getModTimes("lib", SyncLevel.RECURSIVE);
            synchronizeFiles(sr);

            /*
             * Next, the docroot.
             * The docroot could be full of files, so we only check
             * one level.
             */
            sr = getModTimes("docroot", SyncLevel.DIRECTORY);
            synchronizeFiles(sr);

            /*
             * Check any subdirectories of the instance config directory.
             * We only expect one - the config-specific directory,
             * but since we don't have an easy way of knowing the
             * name of that directory, we include them all.  The
             * DAS will tell us to remove anything that shouldn't
             * be there.
             */
            sr = new SyncRequest();
            sr.instance = instanceName;
            sr.dir = "config-specific";
            File configDir = new File(instanceDir, "config");
            for (File f : configDir.listFiles()) {
                if (!f.isDirectory())
                    continue;
                getFileModTimes(f, configDir, sr, SyncLevel.DIRECTORY);
            }
            synchronizeFiles(sr);
        } catch (ConnectException cex) {
            logger.finer("Couldn't connect to DAS: " + cex);
            return false;
        }

        return true;
    }

    /**
     * Return a SyncRequest with the mod times for all the
     * files in the specified directory.
     */
    private SyncRequest getModTimes(String dir, SyncLevel level) {
        SyncRequest sr = new SyncRequest();
        sr.instance = instanceName;
        sr.dir = dir;
        File fdir = new File(instanceDir, dir);
        if (!fdir.exists())
            return sr;
        getFileModTimes(fdir, fdir, sr, level);
        return sr;
    }

    /**
     * Get the mod times for the entries in dir and add them to the
     * SyncRequest, using names relative to baseDir.  If level is
     * RECURSIVE, check subdirectories and only include times for files,
     * not directories.
     */
    private void getFileModTimes(File dir, File baseDir, SyncRequest sr,
                                    SyncLevel level) {
        if (level == SyncLevel.TOP) {
            long time = dir.lastModified();
            SyncRequest.ModTime mt = new SyncRequest.ModTime(".", time);
            sr.files.add(mt);
            return;
        }
        for (String file : dir.list()) {
            File f = new File(dir, file);
            long time = f.lastModified();
            if (time == 0)
                continue;
            if (f.isDirectory()) {
                if (level == SyncLevel.RECURSIVE) {
                    getFileModTimes(f, baseDir, sr, level);
                    continue;
                } else if (level == SyncLevel.FILES)
                    continue;
            }
            String name = baseDir.toURI().relativize(f.toURI()).getPath();
            // if name is a directory, it will end with "/"
            if (name.endsWith("/"))
                name = name.substring(0, name.length() - 1);
            SyncRequest.ModTime mt = new SyncRequest.ModTime(name, time);
            sr.files.add(mt);
            logger.finer(f + ": mod time " + mt.time);
        }
    }

    /**
     * Ask the server to synchronize the files in the SyncRequest.
     */
    private void synchronizeFiles(SyncRequest sr)
                                throws CommandException, ConnectException {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("mt.", ".xml");
            tempFile.deleteOnExit();

            JAXBContext context = JAXBContext.newInstance(SyncRequest.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
            marshaller.marshal(sr, tempFile);
            if (logger.isLoggable(Level.FINER))
                marshaller.marshal(sr, System.out);

            File syncdir = new File(instanceDir, sr.dir);
            logger.finer("Sync directory: " + syncdir);
            // _synchronize-files takes a single operand of type File
            // Note: we throw the output away to avoid printing a blank line
            syncCmd.executeAndReturnOutput("_synchronize-files",
                tempFile.getPath());

            // the returned files are automatically saved by the command
        } catch (IOException ex) {
            logger.finer("Got exception: " + ex);
            throw new CommandException(
                Strings.get("Sync.failed", sr.dir, ex.toString()), ex);
        } catch (JAXBException jex) {
            logger.finer("Got exception: " + jex);
            throw new CommandException(
                Strings.get("Sync.failed", sr.dir, jex.toString()), jex);
        } catch (CommandException cex) {
            logger.finer("Got exception: " + cex);
            logger.finer("  cause: " + cex.getCause());
            if (cex.getCause() instanceof ConnectException)
                throw (ConnectException)cex.getCause();
            throw new CommandException(
                Strings.get("Sync.failed", sr.dir, cex.getMessage()), cex);
        } finally {
            // remove tempFile
            if (tempFile != null)
                tempFile.delete();
        }
    }

    /**
     * Remove the named subdirectory of the instance directory.
     */
    private void removeSubdirectory(String name) {
        File subdir = new File(instanceDir, name);
        logger.finer("Removing: " + subdir);
        FileUtils.whack(subdir);
    }

    /**
     * Expand the archive to the specified directory.
     * XXX - this doesn't handle all the cases required for a Java EE app,
     * but it's good enough for now for some performance testing
     */
    private static void expand(File dir, File archive) throws Exception {
        dir.mkdir();
        long modtime = archive.lastModified();
        ZipFile zf = new ZipFile(archive);
        Enumeration<? extends ZipEntry> e = zf.entries();
        while (e.hasMoreElements()) {
            ZipEntry ze = e.nextElement();
            File entry = new File(dir, ze.getName());
            if (ze.isDirectory()) {
                entry.mkdir();
            } else {
                FileUtils.copy(zf.getInputStream(ze),
                                new FileOutputStream(entry), 0);
            }
        }
        dir.setLastModified(modtime);
    }
}
