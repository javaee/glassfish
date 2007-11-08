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
package com.sun.enterprise.ee.synchronization.http;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.synchronization.Command;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.SynchronizationResponse;
import com.sun.enterprise.ee.synchronization.processor.ServletProcessor;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;
import java.net.UnknownHostException;
import com.sun.enterprise.ee.synchronization.SynchronizationException;

/**
 * Represents a concrete implementation of synchronization GET 
 * command using HTTP.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class HttpGetCommand implements Command {
    
    private static Logger _logger = Logger.getLogger(EELogDomains.
                                    SYNCHRONIZATION_LOGGER);

    // The following string manager is used for exception messages
    private static final StringManager _localStrMgr = StringManager.getManager(
                    HttpGetCommand.class);

    /**
     * Initializes the synchronization GET command.
     *
     * @param  req    synchronization request
     * @param  ctx    config context
     * @param  url    synchronization servlet url
     */
    HttpGetCommand(SynchronizationRequest req, DASPropertyReader dpr, 
            String url) {

        this._request = req;
        this._dpr     = dpr;
        this._url     = url;
    }

    /**
     * Executes a synchronization request. It unzips the content directy
     * into the file system to avoid additional i/o.
     *
     * @throws  SynchronizationException   if an error occurred during the
     *                                     execution of this command
     */
    public void execute() throws SynchronizationException {

        URLConnection conn       = null;
        BufferedInputStream bis  = null;

        try {            
            // get data from DAS for this request
            _logger.log(Level.FINE,
                "synchronization.request_start", _request.getMetaFileName());
            conn = HttpUtils.getConnection(_url, true);

            // send synchronization request
            OutputStreamWriter out = 
                new OutputStreamWriter(conn.getOutputStream());
            out.write(_request.getPostData());
            out.write("\r\n");
            out.flush();
            out.close();

            // response stream from synchronization servlet
            bis = new BufferedInputStream(conn.getInputStream());

            // unzip under the base dir
            String baseDir   = _request.getBaseDirectory();
            HttpUnzipper unzipper = new HttpUnzipper(baseDir);
            _result = 
                unzipper.writeZip(bis, ServletProcessor.RESPONSE_ENTRY_NAME);

            _logger.log(Level.FINE,
                "synchronization.file_received", _request.getMetaFileName());

        } catch (Exception e) {
            String msg = _localStrMgr.getString("fileRetrieveError"
                       , _request.getMetaFileName());
            throw new SynchronizationException(msg, e);

        } finally {
            try { 
                if (bis != null) {
                    bis.close(); 
                }
            } catch (Exception e) { }

            try {
                if (conn != null) {
                    ((HttpURLConnection) conn).disconnect();
                }
            } catch (Exception e) { }
        }

        if (_result == null) {
            String msg = _localStrMgr.getString("fileRetrieveError"
                       , _request.getMetaFileName());
            throw new SynchronizationException(msg);
        }
    }

    /**
     * Deserializes the synchronization response object from zip.
     * This method can be used when response is downloaded into a zip file
     * and then prossed. It sets up a response object that can be used
     * by response processor used in JMX code path.
     *
     * @param  zip  response zip from synchronization servlet
     *
     * @throws IOException if an i/o error
     * @throws ClassNotFoundException  if deserialization fails 
     */
    private  void createResponse(File zip) 
        throws IOException, ClassNotFoundException, SynchronizationException {

        // deserialize the response object
        ZipFile zipFile = new ZipFile(zip);
        ZipEntry response = 
            zipFile.getEntry(ServletProcessor.RESPONSE_ENTRY_NAME);

        if (response == null) {
            String msg = _localStrMgr.getString("responseEntryNotFound", 
                                            _request.getMetaFileName());
            throw new SynchronizationException(msg);
        }

        InputStream zis      = null;
        ObjectInputStream in = null;

        try {
            zis = zipFile.getInputStream(response);
            in = new ObjectInputStream(zis);
            _result = (SynchronizationResponse) in.readObject();
        } finally {
            if (zis != null) {
                zis.close();
            }
            if (in != null) {
                in.close();
            }
        }

        // set zip file information for response processing
        _result.setZipLocation(zip.getCanonicalPath());

        // set last modified time stamp
        _result.setLastModifiedOfZip(zip.lastModified());
        zip.deleteOnExit();

        try {
            // host name of DAS
            InetAddress host = InetAddress.getLocalHost();
            _result.setDasHostName(host.getHostName());
        } catch (UnknownHostException uhe) {
            // ignore
        } 
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
     * Returns the response for this synchronization request.
     *
     * @return  the synchronization response of type 
     *          com.sun.enterprise.ee.synchronization.SynchronizationResponse
     */
    public Object getResult() {
        return _result;
    }

    // ---- INSTANCE VARIABLE(S) - PRIVATE -----------------------
    private SynchronizationRequest _request   = null;
    private SynchronizationResponse _result   = null;
    private DASPropertyReader _dpr            = null;
    private String _url                       = null;

    private static final String NAME = "Synchronization-Get-Command";    
}
