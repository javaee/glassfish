/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.cluster.SyncRequest;
import java.io.*;
import java.io.OutputStream;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import org.glassfish.admin.payload.PayloadImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.Payload;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.PostConstruct;

/**
 *
 * @author Byron Nevins
 */
@Service(name = "generate-sync-bundle")
@Scoped(PerLookup.class)
@I18n("generate-sync-bundle")
public class GenerateSyncBundle implements AdminCommand, PostConstruct {

    @Param(optional = false)
    private String instanceName;
    @Param(optional = false, primary = true)
    String localFilePath;

    @Override
    public final void postConstruct() {
        try {
            tempFile = File.createTempFile("GlassFishSyncBundle.", ".zip");
            tempFile.deleteOnExit();
        }
        catch (Exception e) {
            // return an error in execute().  This is exceedingly unlikely!
            tempFile = null;
        }
    }

    @Override
    public void execute(AdminCommandContext context) {
        report = context.getActionReport();
        report.setActionExitCode(ExitCode.SUCCESS);
        logger = context.getLogger();

        // we use our own private payload.  Don't use the one in the context!
        payload = PayloadImpl.Outbound.newInstance();


        try {
            if (!isValid())
                return;

            syncRequest = new SyncRequest();
            syncRequest.instance = instanceName;

            if (!sync())
                return;

            // write to the temp file
            write();

            //all OK...
            pumpItOut(context);
        }
        catch (Exception e) {
            setError("BAD WRITE!!!!");
            return;
        }
    }

    private void pumpItOut(AdminCommandContext context) throws IOException {
        Properties props = new Properties();
        File parent = localFile.getParentFile();
        props.setProperty("file-xfer-root", parent.getPath().replace('\\', '/'));
        URI parentURI = parent.toURI();
        context.getOutboundPayload().attachFile(
                "application/octet-stream",
                parentURI.relativize(localFile.toURI()),
                "sync-bundle",
                props,
                tempFile);
    }

    private boolean sync() throws IOException {
        for (String dir : ALL_DIRS) {
            syncRequest.dir = dir;

            if (!syncOne())
                return false;
        }

        return !hasError();
    }

    private void write() throws FileNotFoundException, IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(tempFile);
            payload.writeTo(out);
        }
        finally {
            if (out != null)
                out.close();
        }
    }

    private boolean syncOne() throws IOException {
        serverSynchronizer.synchronize(instance, syncRequest, payload, report, logger);

        // synchronize() will be set to FAILURE if there were problems
        return !hasError();
    }

    private boolean isValid() {
        // verify the server instance name corresponds to reality!
        if (servers != null)
            instance = servers.getServer(instanceName);
        if (instance == null) {
            setError(Strings.get("sync.unknown.instance", instanceName));
            return false;
        }
        if (tempFile == null) {
            setError(Strings.get("sync.bad_temp_file"));
            return false;
        }
        
        File f = new File(localFilePath);
        if(!f.isAbsolute()) {
            setError(Strings.get("sync.no_relative_path"));
            return false;
        }
        
        localFile = SmartFile.sanitize(new File(localFilePath));
        return true;
    }

    private void setError(String msg) {
        report.setActionExitCode(ExitCode.FAILURE);
        report.setMessage(msg);
    }

    private boolean hasError() {
        return report.getActionExitCode() != ExitCode.SUCCESS;
    }
    @Inject(optional = true)
    private Servers servers;
    @Inject
    private ServerSynchronizer serverSynchronizer;
    private ActionReport report;
    private File tempFile;
    private File localFile;
    private Logger logger;
    private Payload.Outbound payload;
    private Server instance;
    private SyncRequest syncRequest = new SyncRequest();
    private static final String[] ALL_DIRS = new String[]{
        "config", "applications", "lib", "docroot"
    };
}
