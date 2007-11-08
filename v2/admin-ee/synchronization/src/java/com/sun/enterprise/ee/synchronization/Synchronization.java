/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.ee.synchronization;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.NodeAgentHelper;
import com.sun.enterprise.ee.synchronization.processor.RequestContext;
import com.sun.enterprise.ee.synchronization.processor.ByteProcessor;
import com.sun.enterprise.ee.synchronization.processor.ZipProcessor;
import com.sun.enterprise.ee.synchronization.processor.ListProcessor;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;
import java.nio.BufferOverflowException;
import com.sun.enterprise.config.ConfigException;

/**
 * Synchronization MBean. This MBean is not exposes to the public. It runs 
 * only on DAS and responds to synchronization requests.
 */
public class Synchronization extends BaseConfigMBean 
        implements SynchronizationMBean {

    // ---- VARIABLES - PRIVATE ------------------------------------------
    private static final String ZIP_FILE_SUFFIX = "-repository.zip";
    private static final String TEMP_FS_FILE_NM = ".synchronize";
    private static final StringManager _localStrMgr = 
            StringManager.getManager(Synchronization.class);
    private static Logger _logger = Logger.getLogger(
            EELogDomains.SYNCHRONIZATION_LOGGER);
    private static final String SYNC_NO_MATCHING_SERVER =
             "synchronization.no_matching_server";
    public static long _delta;
    static {
        _delta = getTimeDelta();
    }

    /**
     * Returns the time delta between file system (ex. NFS) and DAS's VM.
     * If DAS's VM is ahead of file system's time stamp, it returns 
     * the diff between the two. 
     *
     * For example, if file system clock is 10 am and DAS's clock is 
     * 10:01 am, the delta is -01 minute in long. Delta should be added
     * to the DAS's VM time.
     *
     * @return  delta between file system and DAS
     */
    private static long getTimeDelta() {

        long dlt = 0;

        try {
            // current VM timestamp
            long currentVMTime = System.currentTimeMillis();

            // temp file 
            File f = new File(TEMP_FS_FILE_NM);
            FileWriter fw = null;
            try {
                fw = new FileWriter(f);
                fw.write(Long.toString(currentVMTime));
                fw.flush();
                fw.close();
                fw = null;

                // current file system's time stamp
                long fsTime = f.lastModified();
                if (fsTime < currentVMTime) {
                    dlt = fsTime - currentVMTime;
                }
            } catch (Exception e) {
                _logger.log(Level.FINE, 
                    "Unable to compute synchronization time delta", e);
            } finally {
                try {
                    if (fw != null) {
                        fw.close();
                    }
                } catch (Exception ex) { }
            }

            // ignore when VM is behind file system clock
            if (dlt > 0) {
                dlt = 0;
            }

            _logger.log(Level.FINE, 
                "Computed synchronization time delta: " + dlt);

        } catch (Exception t) { }

        return dlt;
    }

    /**
     * Responds to synchronization requests.
     *
     * @param request synchronization request array
     *
     * @return  response object that has changes for the request
     *
     * @throws IOException  if an i/o error
     * @throws ConfigException if a configuration parsing error
     */
    public SynchronizationResponse synchronize(SynchronizationRequest[] request)
            throws IOException, ConfigException {

        SynchronizationResponse response = null;

        try {
            ConfigContext configCtx = AdminService.getAdminService().
                                getAdminContext().getAdminConfigContext();

            RequestContext rCtx = new RequestContext(configCtx, request);
            rCtx.setTimeDelta(_delta);

            ByteProcessor bProcessor = new ByteProcessor(rCtx);
            bProcessor.process();

            response = bProcessor.getResult();

        } catch (BufferOverflowException overflowEx) {

            _logger.log(Level.FINE, 
                "Zipper reached max buffer size. Attempting a redirect.");

            ConfigContext configCtx = AdminService.getAdminService().
                                getAdminContext().getAdminConfigContext();

            RequestContext rCtx = new RequestContext(configCtx, request);
            rCtx.setTimeDelta(_delta);

            ZipProcessor zProcessor = new ZipProcessor(rCtx);
            zProcessor.process();

            response = zProcessor.getResult();
        }

        return response;
    }

    /**
     * Health check method called from sever instance or node agents.
     *
     * This mehtod contains a light-weight implementation. It is called 
     * from server instance (or node agent) at the beginning of the 
     * synchronization. 
     *
     * @param    serverName    name of the server instance
     * @return   a synchronization response containing the names of the 
     *           application the given server instance is using
     *
     * @throws   IOException  if an error during health check
     * @throws   ConfigException if a configuration parsing error
     */
    public SynchronizationResponse ping(String serverName) 
            throws IOException, ConfigException {

        long startTime = System.currentTimeMillis();
        SynchronizationPingResponse response = null;

         ConfigContext configContext = AdminService.getAdminService().
             getAdminContext().getAdminConfigContext();

         // do a check to see if there is indeed ANY Node Agent or Instance
         // by this name in this DAS.
         Object server = null;
         try {
             server =
                 NodeAgentHelper.getNodeAgentByName(configContext, serverName);
         } catch (ConfigException ex) {
             // No node agent founds. So let's look for an instance
             try {
                 if (server == null)
                     server = ServerHelper.getServerByName(
                              configContext, serverName);
             } catch (ConfigException e) {
                 //no server instance either.
                 if (server == null) throw new ConfigException(
                   _localStrMgr.getString(SYNC_NO_MATCHING_SERVER, serverName));
             }
         }
        
        _logger.log(Level.INFO, 
            "synchronization.synchronization_start_time", 
            new Object[] { (new Date(startTime)).toString(), serverName});

        return response;
    }

    /**
     * Audit method called at the end of synchronization. This returns a 
     * list of files found in central repository for the synchronization
     * requests.
     *
     * @param request synchronization request array
     *
     * @throws   IOException  if an error during health audit
     * @throws   ConfigException if a configuration parsing error
     */
    public SynchronizationResponse audit(SynchronizationRequest[] request)
            throws IOException, ConfigException {

        ConfigContext configCtx = AdminService.getAdminService().
                            getAdminContext().getAdminConfigContext();

        RequestContext rCtx = new RequestContext(configCtx, request);
        rCtx.setTimeDelta(_delta);

        ListProcessor lProcessor = new ListProcessor(rCtx);
        lProcessor.process();

        SynchronizationResponse response = lProcessor.getResult();

        return response;
    }


    /**
     * Assembles the repository zip for the given target.
     *
     * @param   target   target server instance or node agent 
     *
     * @returns    path to the newly created zip
     *
     * @throws  SynchronizationException  if an error while assembling zip
     */
    public String createRepositoryZip(String target) 
            throws SynchronizationException {

        File zip = new File("..", target+ZIP_FILE_SUFFIX);

        try {
            ConfigContext configCtx = AdminService.getAdminService().
                                getAdminContext().getAdminConfigContext();

            SynchronizationRequest[] requests = getRequests(configCtx, target);

            RequestContext rCtx = new RequestContext(configCtx, requests);
            rCtx.setZipFile(zip);

            // turn off the process inventory
            rCtx.setProcessInventory(false);

            ZipProcessor zProcessor = new ZipProcessor(rCtx);

            // sets the delete flag to false
            zProcessor.setDeleteZipOnExitFlag(false);

            // constructs the zip
            zProcessor.process();

        } catch (Exception ex) {
            String msg = _localStrMgr.getString("createRepositoryZipFailed", 
                                                target);
            throw new SynchronizationException(msg, ex);
        }

        return zip.getPath();
    }

    /**
     * Returns the synchronization requests for a server instance.
     *
     * @param   ctx   config context
     * @param   target  target server instance or node agent name
     *
     * @return  synchronization requests for a server
     * @throws  ConfigException  if an error while validating target
     */
    private SynchronizationRequest[] getRequests(ConfigContext ctx, 
            String target) throws ConfigException {
        
        SynchronizationConfig sc = null;

        if (target != null && ServerHelper.isAServer(ctx, target)) {

            sc = new SynchronizationConfig(
                    SynchronizationDriverFactory.INSTANCE_CONFIG_URL, target);

        } else if (target != null 
                && NodeAgentHelper.isANodeAgent(ctx, target)) {

            sc = new SynchronizationConfig(
            SynchronizationDriverFactory.NODE_AGENT_STARTUP_CONFIG_URL, target);
        } else {
            String msg = _localStrMgr.getString("invalidTarget", target);
            throw new IllegalArgumentException(msg);
        }

        SynchronizationRequest[]  reqs = sc.getSyncRequests();

        // set zero timestamp so that all files are included in zip
        for (int i=0; i<reqs.length; i++) {
            reqs[i].setTimestamp(0L);
        }

        return reqs;
    }
}
