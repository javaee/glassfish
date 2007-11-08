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
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

import java.util.zip.CRC32;
import java.util.zip.ZipOutputStream;
import java.util.zip.CheckedOutputStream;
import java.net.InetAddress;

import com.sun.enterprise.ee.util.zip.Zipper;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.SynchronizationResponse;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;
import java.net.UnknownHostException;
import com.sun.enterprise.config.ConfigException;

/**
 * Processor class that generates zip file as output. 
 *
 * @author  Nazrul Islam
 * @since   JDK 1.4
 */
public class ZipProcessor extends BaseProcessor {

    /**
     * Constructor.
     *
     * @param  ctx  request context
     *
     * @throws IOException  if an i/o error 
     */
    public ZipProcessor(RequestContext ctx) throws IOException {
        _ctx = ctx;
        _zipFile = ctx.getZipFile();
        _fout = new FileOutputStream(_zipFile);
        _cos = new CheckedOutputStream(
                new BufferedOutputStream(_fout), new CRC32());
        _out = new ZipOutputStream(_cos);
    }

    /**
     * When true, marks the zip file for delete upon VM exit. 
     *
     * @param   tf   delete zip on exit flag
     */
    public void setDeleteZipOnExitFlag(boolean tf) {
        _deleteZipOnExit = tf;
    }

    /**
     * Returns the delete on exit flag.
     *
     * @return  delete on exit flag for the zip
     */
    public boolean getDeleteZipOnExitFlag() {
        return _deleteZipOnExit;
    }

    /**
     * Processes the always included application names.
     *
     * @param  req   synchronization request
     * @param  z   zipper object
     *
     * @throws ConfigException if a configuration parsing error
     */
    void initZipper(SynchronizationRequest req, Zipper z) 
            throws ConfigException { 

        processAlwaysInclude(req, z);
    }

    /**
     * Assembles the synchronization response.
     * 
     * @param  zipSize  zip size from the zipper
     * @param  results  processed synchronization requests
     *
     * @throws  IOException  if an i/o error
     */
    void postProcess(long zipSize, SynchronizationRequest[] results) 
            throws IOException {

        _cos.flush();

        // checksum
        long checksum  = _cos.getChecksum().getValue();

        // closes the streams
        if (zipSize > 0) {
            _out.close();
            _out = null;
        }

        // synchronization response
        _response = new SynchronizationResponse(null, results, 
                    checksum, _ctx.getStartTime(), System.currentTimeMillis());

        if (_zipFile.exists()) {
            // sets the zip file location in DAS for download
            _response.setZipLocation(_zipFile.getCanonicalPath());

            // sets the last modified timestamp of zip file 
            _response.setLastModifiedOfZip(_zipFile.lastModified());

            // file will be removed when DAS is shut down
            if (_deleteZipOnExit) {
                _zipFile.deleteOnExit();
            }

            try {
                // host name of DAS
                InetAddress host = InetAddress.getLocalHost();
                _response.setDasHostName(host.getHostName());
            } catch (UnknownHostException uhe) {
                // ignore
            }

        } else {
            _logger.fine("Zip intended for redirect does not exist: " 
                    + _zipFile.getPath());
        }
    }



    // ---- VARIABLES - PRIVATE ------------------------------------------
    private boolean _deleteZipOnExit = true;
}
