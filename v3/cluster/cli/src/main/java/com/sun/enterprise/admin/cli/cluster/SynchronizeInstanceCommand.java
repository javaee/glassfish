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

package com.sun.enterprise.admin.cli.cluster;

import java.io.*;
import java.util.*;
import javax.xml.bind.*;

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;

import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.cli.remote.RemoteCommand;
import com.sun.enterprise.util.cluster.SyncRequest;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 * Synchronize a local server instance.
 */
@Service(name = "_synchronize-instance")
@Scoped(PerLookup.class)
public class SynchronizeInstanceCommand extends LocalInstanceCommand {

    @Param(name = "instance_name", primary = true, optional = true)
    private String instanceName0;

    private RemoteCommand syncCmd;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(SynchronizeInstanceCommand.class);

    @Override
    protected void validate()
                        throws CommandException, CommandValidationException {
        instanceName = instanceName0;
        super.validate();
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
        if (synchronizeInstance())
            return SUCCESS;
        else
            return ERROR;
    }

    /**
     * Synchronize this server instance.  Return true if
     * server is synchronized.
     */
    protected boolean synchronizeInstance() throws CommandException {

        if (!dasProperties.exists()) {
            logger.printMessage(strings.get("NoDASConfigured"));
            return false;
        }

        /*
         * Create the remote command object that we'll reuse for each request.
         */
        syncCmd = new RemoteCommand("_synchronize-files", programOpts, env);
        syncCmd.setFileOutputDirectory(instanceDir);

        /*
         * First, synchronize the config directory.
         */
        File domainXml =
            new File(new File(instanceDir, "config"), "domain.xml");
        long dtime = domainXml.lastModified();

        SyncRequest sr = getModTimes("config");
        synchronizeFiles(sr);

        /*
         * Was domain.xml updated?
         * If not, we're all done.
         */
        if (domainXml.lastModified() == dtime) {
            logger.printDebugMessage("Nothing to update");
            return true;
        }

        /*
         * Now synchronize the applications.
         */
        sr = getModTimes("applications");
        synchronizeFiles(sr);

        return true;
    }

    /**
     * Return a SyncRequest with the mod times for all the
     * files in the specified directory (non-recursive).
     */
    private SyncRequest getModTimes(String dir) {
        SyncRequest sr = new SyncRequest();
        sr.instance = instanceName;
        sr.dir = dir;
        File fdir = new File(instanceDir, dir);
        if (!fdir.exists())
            return sr;
        for (String file : fdir.list()) {
            File f = new File(fdir, file);
            // XXX - what about subdirectories?
            long time = f.lastModified();
            if (time == 0)
                continue;
            SyncRequest.ModTime mt = new SyncRequest.ModTime(file, time);
            sr.files.add(mt);
            logger.printDebugMessage(file + ": mod time " + mt.time);
        }
        return sr;
    }

    /**
     * Ask the server to synchronize the files in the SyncRequest.
     */
    private void synchronizeFiles(SyncRequest sr) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("mt.", ".xml");
            tempFile.deleteOnExit();

            JAXBContext context = JAXBContext.newInstance(SyncRequest.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
            marshaller.marshal(sr, tempFile);
            marshaller.marshal(sr, System.out);

            File syncdir = new File(instanceDir, sr.dir);
            logger.printDebugMessage("Sync directory: " + syncdir);
            if (!syncdir.exists())
                syncdir.mkdir();
            // _synchronize-files takes a single operand of type File
            syncCmd.execute("_synchronize-files", tempFile.getPath());

            // the returned files are automatically saved by the command
        } catch (IOException ioex) {
        } catch (JAXBException jbex) {
        } catch (CommandException cex) {
        } catch (CommandValidationException cex) {
        } finally {
            // remove tempFile
            if (tempFile != null)
                tempFile.delete();
        }
    }
}
