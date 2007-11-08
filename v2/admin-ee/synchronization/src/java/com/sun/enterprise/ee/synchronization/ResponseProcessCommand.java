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

import com.sun.enterprise.ee.util.zip.Unzipper;

import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.InetAddress;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.ee.synchronization.impl.SynchronizationClientImpl;
import com.sun.enterprise.ee.synchronization.processor.ServletProcessor;
import com.sun.enterprise.ee.synchronization.util.io.Utils;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Represents a concrete implementation of a command that processes 
 * the synchronization response.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class ResponseProcessCommand implements Command {

    private static Logger _logger = Logger.getLogger(
                    EELogDomains.SYNCHRONIZATION_LOGGER);

    private static final StringManager _localStrMgr = 
        StringManager.getManager(ResponseProcessCommand.class);
 
    /**
     * Initializes the arguments.
     *
     * @param  req   synchronization request
     * @param  res   synchronization response
     */
    ResponseProcessCommand(SynchronizationRequest req, 
            SynchronizationResponse res) {

        _request  = req;
        _response = res;
    }

    /**
     * Renders the response zip into the local file system.
     *
     * @throws  SynchronizationException  if an error while processing the zip
     */
    public void execute() throws SynchronizationException {

        try {
            _logger.log(Level.FINE,
                 "synchronization.process_command", _request.getMetaFileName());

            String baseDir   = _request.getBaseDirectory();
            Unzipper z       = new Unzipper(baseDir);
            byte[] zipBytes  = _response.getZipBytes();
            long checksum    = 0;

            // if byte exists in response
            if (zipBytes != null) {

                // render the zip bytes
                checksum = z.writeZipBytes(zipBytes); 

            } else { // handle a re-direct
                File tempZip = null;

                // DAS and server instance are running on the same machine
                // skips download and uses the zip directly
                if (isSameHost()) {
                    String dasZipLoc  = _response.getZipLocation();
                    tempZip           = new File(dasZipLoc);
                } else {
                    // download the zip file
                    tempZip = downloadZip();
                }

                // render the zip file
                if (tempZip != null && tempZip.exists()) {

                    // ignore response entry for http based impl
                    z.ignoreEntry(ServletProcessor.RESPONSE_ENTRY_NAME);

                    checksum = z.writeZipFile(tempZip.getCanonicalPath());
                }
            }

            //assert(_response.getChecksum() == checksum);

            _logger.log(Level.FINE,
                 "synchronization.check_sum", new Long(checksum).toString());
        } catch (Exception e) {
            String msg = _localStrMgr.getString("syncResponseError" 
                       ,  _request.getMetaFileName());
            throw new SynchronizationException(msg, e);
        }
    }

    /**
     * Returns true if DAS and remote server is running in the 
     * same machine. This allows to perform same host optimization
     * during zip download.
     *
     * @return  true if DAS and remote server runs on the same machine
     */
    private boolean isSameHost() {

        boolean tf = false;

        String dasZipLoc  = _response.getZipLocation();
        File zip = new File(dasZipLoc);

        // the zip file name after the time stamp exists in the remote 
        // server instance
        if (zip.exists()) {
            long dasLastModified    = _response.getLastModifiedOfZip();
            long localLastModified  = zip.lastModified();

            // last modified of zip is the same
            if (dasLastModified == localLastModified) {

                // host name of DAS
                String dasHostName  = _response.getDasHostName();
                try {
                    InetAddress host    = InetAddress.getLocalHost();
                    // host name of DAS is same is this server instance
                    if ( (dasHostName != null) 
                            && (dasHostName.equals(host.getHostName())) ) {

                        tf = true;
                    }
                } catch (UnknownHostException uhe) {
                    // ignore
                }

            }
        }

        return tf;
    }

    /**
     * Downloads the zip file from DAS.
     *
     * @return  temporary location of the local zip file
     *   where zip is being down loaded
     *
     * @throws  SynchronizationException  if an error during zip download
     * @throws  IOException  if an i/o error during zip download
     */
    private File downloadZip() throws SynchronizationException, IOException {

        File zipfile      = null;
        String dasZipLoc  = _response.getZipLocation();

        // zip size is too big to stream in byte array
        // download the zip using chunking 
        if (dasZipLoc != null) {

            // synchronization client to DAS
            SynchronizationClientImpl sClient = 
                new SynchronizationClientImpl(
                SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME);

            // temp file in the remote sever 
            zipfile = Utils.getTempZipFile();

            // connects to DAS
            sClient.connect();

            // down loads the zip file
            sClient.getAbsolute(dasZipLoc, zipfile);

            // closes the connection to DAS
            sClient.disconnect();
        }

        return zipfile;
    }

    /**
     * Returns the name of this command.
     *
     * @return  the name of this command
     */
    public String getName() {
        return NAME;
    }

    /**
     * No-op.
     */
    public Object getResult() {
        return null;
    }

    // ---- VARIABLE(S) - PRIVATE -------------------------------
    private SynchronizationRequest _request    = null;
    private SynchronizationResponse _response  = null;
    private static final String ZIP_SUFFIX     = ".zip";
    private static final String NAME = 
                        "Synchronization-Response-Process-Command";
}
