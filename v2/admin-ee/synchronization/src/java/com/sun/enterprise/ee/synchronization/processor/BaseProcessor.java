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
package com.sun.enterprise.ee.synchronization.processor;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.zip.CRC32;
import java.util.zip.ZipOutputStream;
import java.util.zip.CheckedOutputStream;
import java.net.InetAddress;

import com.sun.enterprise.ee.util.zip.Zipper;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ServerHelper;

import com.sun.enterprise.ee.synchronization.TextProcess;
import com.sun.enterprise.ee.synchronization.ServerDirector;
import com.sun.enterprise.ee.synchronization.inventory.InventoryMgr;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.SynchronizationResponse;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;
import com.sun.enterprise.config.ConfigException;

/**
 * Base class for synchronization request processor.
 *
 * @author  Nazrul Islam
 * @sine    JDK1.4
 */
abstract class BaseProcessor {

    /**
     * Returns true if there are potential changes for this request.
     *
     * @param request synchronization request
     *
     * @return  true if there are potential changes for this request
     *
     * @throws IOException  if an i/o error while reading the timestamp file
     */
    boolean isModified(SynchronizationRequest request)
            throws IOException {

        long modifiedTime = 0;

        if (request.getTimestampType()
                == SynchronizationRequest.TIMESTAMP_NONE) {
            return true;
        } else if (request.getTimestampType()
                == SynchronizationRequest.TIMESTAMP_MODIFICATION_TIME) {
            modifiedTime = request.getFile().lastModified();
        } else if (request.getTimestampType()
                == SynchronizationRequest.TIMESTAMP_MODIFIED_SINCE) {                
            return true;
        } else if (request.getTimestampType()
                   == SynchronizationRequest.TIMESTAMP_FILE) {
            BufferedReader is = null;

            try {
                is = new BufferedReader(
                    new FileReader(request.getTimestampFile()));
                modifiedTime = Long.parseLong(is.readLine());

                is.close();

                is = null;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            }
        } else {
            assert false;
        }

        assert (request.getTimestamp() <= modifiedTime);
        if (request.getTimestamp() < modifiedTime) {
            request.setTimestamp(modifiedTime);
            return true;
        } else {
            return false;
        }
    }

    /**
     * If the request is GC enabled, it sets the inventory of 
     * the central repository to the request.
     *
     * @param  req  synchronization request
     */
    void processInventory(SynchronizationRequest req) {

        try {
            if (req.isGCEnabled()) {
                String serverName = req.getServerName();
                File f = req.getFile();
                InventoryMgr mgr = new InventoryMgr(f);

                // inventory list for this directory
                List crList = mgr.getInventory();

                // re-sets the inventory with central repository inventory
                // this had client inventory 
                if (crList != null) {
                    req.setInventory(crList);
                }
            }
        } catch (Exception ex) {
            _logger.log(Level.FINE, "Error during inventory processing for " 
                + req.getMetaFileName(), ex );
        }
    }

    /**
     * Sets the list of file paths to be excluded for this request.
     *
     * @param  req     synchronization request
     * @param  zipper  util class that zips up the synchronization response
     */
    void setExcludeList(SynchronizationRequest req, Zipper zipper) {

        try { 
            String serverName = req.getServerName();
            List list = (List)_excludeCache.get(serverName);
            if (list == null) {
                Properties env = req.getEnvironmentProperties();
                // admin config context
                ConfigContext ctx = _ctx.getConfigContext();
                Domain domain = (Domain) ctx.getRootConfigBean();
                Server server = domain.getServers().getServerByName(serverName);
                if (server != null) {
                    ServerDirector director=new ServerDirector(ctx, serverName);
                    List excludes = director.constructExcludes();
                    list = new ArrayList();
                    int size = excludes.size();
                    for (int i=0; i<size; i++) {
                        String path = (String) excludes.get(i);
                        String tPath = 
                            TextProcess.tokenizeConfig(path, serverName, env);
                        list.add(tPath);
                    }
                    // add the list to the cache
                    _excludeCache.put(serverName, list);
                }
            }
            _logger.log(Level.FINE, "Excluded List " + list);
            zipper.addToExcludeList(list);
        } catch (Exception e) {
            _logger.log(Level.FINE, "Excluded List can not be set", e);
        }
    }

    /**
     * Sets the list of files (application names) that must always be included.
     * This is done to take care of offline configuration changes in DAS.
     *
     * @param  req     synchronization request
     * @param  zipper  util class that zips up the synchronization response
     *
     * @throws ConfigException  if a configuration parsing error
     */
    void processAlwaysInclude(SynchronizationRequest req, Zipper zipper) 
            throws ConfigException {

        if (req.isClientRepositoryInfoSent()) {

            // list of application names in client
            Set clientInfo = req.getClientRepositoryInfo();

            // name of server instance
            String clientName = req.getServerName();

            // config context
            ConfigContext ctx = _ctx.getConfigContext();

            // client is a server instance
            if (ServerHelper.isAServer(ctx, clientName)) {

                // associated applications for the server
                ApplicationRef[] appRefs = 
                    ServerHelper.getApplicationReferences(ctx, clientName);

                for (int i=0; i<appRefs.length; i++) {
                    String appName = appRefs[i].getRef();

                    // if client does not have the application
                    if (!clientInfo.contains(appName)) {
                        zipper.addToAlwaysIncludeList(appName);
                    }
                }
            }
        }
    }

    /**
     * Initializes the zipper class. This is called at the beginning of 
     * request processing. 
     *
     * @param  req  synchronization request
     * @param  z   zipper object
     * 
     * @throws IOException  if an i/o error
     * @throws ConfigException  if a configuration parsing error
     */
    abstract void initZipper(SynchronizationRequest req, Zipper z) 
        throws IOException, ConfigException;

    /**
     * Adds the file or directory to the zip.
     *
     * @param  file  file representing a file or directory
     * @param  z     zipper
     * @param  req   synchronization request
     */
    void addFileToZip(File file, Zipper z, SynchronizationRequest req) 
            throws IOException {

        if (file.isFile()) {
            z.setBaseDirectory(req.getTargetDirectory());
            z.addFileToZip(file, _out);
        } else if (file.isDirectory()) {
            z.setBaseDirectory(req.getTargetDirectory());
            z.addDirectoryToZip(file, _out);
        } else {
            assert false;
        }
    }

    /**
     * Services the request.
     *
     * @throws  IOException  if an i/o error while processing the request
     * @throws  ConfigException  if a configuration parsing error
     */
    public void process() throws IOException, ConfigException {

        SynchronizationRequest[] request = _ctx.getRequests();

        SynchronizationRequest results[] =
            new SynchronizationRequest[request.length];

        File file  = null;
        try {
            long startTime  = System.currentTimeMillis() + _ctx.getTimeDelta();
            _ctx.setStartTime(startTime);
            long zipSize = 0;

            for (int i = 0; i < request.length; i++) {
                Zipper z = new Zipper(null, 0L);

                // init zipper
                initZipper(request[i], z);

                // generate inventory for this request
                if (_ctx.isProcessInventory()) {
                    processInventory(request[i]);
                }

                if (isModified(request[i]) || z.hasAlwaysInclude()) {
                    _logger.log(Level.FINE, "synchronization.req_mod_file_info",
                        request[i].getFileName());

                    // set exclude list for unassociated applications
                    if (request[i].isExclude()) {
                        setExcludeList(request[i], z);
                    }

                    // add the exclude regular expression for this request
                    List ePattern = request[i].getExcludePatternList();
                    z.addToExcludePatternList(ePattern);

                    // sets the shallow copy flag
                    z.setShallowCopyEnabled(request[i].isShallowCopyEnabled());

                    // add the include regular expression for this request
                    // include patterns has precidence over exclude patterns
                    List iPattern = request[i].getIncludePatternList();
                    z.addToIncludePatternList(iPattern);

                    if (request[i].getTimestampType()
                        == SynchronizationRequest.TIMESTAMP_MODIFIED_SINCE) {
                        z.setLastModifiedTime(request[i].getTimestamp());
                    } else {
                        z.setLastModifiedTime(0L);
                    }

                    // add the file or directory to zipper
                    file = request[i].getFile();
                    addFileToZip(file, z, request[i]);

                    results[i] = request[i];
                    zipSize += z.getZipSize();
                } else {
                    _logger.log(Level.FINE, 
                        "synchronization.req_not_mod_file_info", 
                        request[i].getFileName());

                    results[i] = request[i];
                }
            }                        

            // puts together the synchronization response
            postProcess(zipSize, results);

        } finally { // close all streams

            if (_fout != null) {
                try {
                    _fout.close();
                } catch (Exception e) { }
            }

            if (_bos != null) {
                try {
                    _bos.close();
                } catch (Exception e) { }
            }

            if (_cos != null) {
                try {
                    _cos.close();
                } catch (Exception e) { }
            }

            if (_out != null) {
                try {
                    _out.close();
                } catch (Exception e) { }
            }
        }
    }

    /**
     * This is called at the end of process method. Synchronization response
     * is assembled in this method by the sub-classes.
     *
     * @param  zipSize  zip size from the zipper
     * @param  results  processed synchronization requests
     *
     * @throws  IOException  if an i/o error while assembling the response
     */
    abstract void postProcess(long zipSize, SynchronizationRequest[] results)
        throws IOException;

    /**
     * Returns the synchronization response for the requests.
     *
     * @return  synchronization response
     */
    public SynchronizationResponse getResult() {
        return _response;
    }

    // ---- VARIABLES - PRIVATE --------------------------------------
    protected RequestContext _ctx               = null;
    protected ZipOutputStream _out              = null;
    protected CheckedOutputStream _cos          = null;
    protected ByteArrayOutputStream _bos        = null;
    protected FileOutputStream _fout            = null;
    protected File _zipFile                     = null;
    protected SynchronizationResponse _response = null;

    protected static final boolean DEBUG          = false;
    protected static final int BUFFER_SIZE        = 16384;    // 16 KB
    protected static final long MAX_BUFFER_SIZE   = 10485760; // 10 MB
    protected static final String MAX_BUFFER_PROP = 
                    "com.sun.appserv.synchronizationBufferSize";

    protected static final StringManager _localStrMgr = 
        StringManager.getManager(BaseProcessor.class);
    protected static Logger _logger = Logger.getLogger(
        EELogDomains.SYNCHRONIZATION_LOGGER);

    private static WeakHashMap _excludeCache = new WeakHashMap();
}
