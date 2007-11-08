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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

import java.util.zip.CRC32;
import java.util.zip.ZipOutputStream;
import java.util.zip.CheckedOutputStream;

import com.sun.enterprise.ee.util.zip.Zipper;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.SynchronizationResponse;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ServerHelper;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;
import com.sun.enterprise.config.ConfigException;

/**
 * Processor class that generates byte array as output.
 *
 * @author  Nazrul Islam
 * @since   JDK 1.4
 */
public class ByteProcessor extends BaseProcessor {

    /**
     * Constructor.
     * 
     * @param   ctx  request context
     *
     * @throws  IOException  if an i/o error
     */
    public ByteProcessor(RequestContext ctx) throws IOException {
        _ctx = ctx;
        _bos = new ByteArrayOutputStream(BUFFER_SIZE);
        _cos = new CheckedOutputStream(
                new BufferedOutputStream(_bos), new CRC32());
        _out = new ZipOutputStream(_cos);
    }

    /**
     * Processes the always included application names and sets the max 
     * buffer size for the byte array. 
     *
     * @param  req   synchronization request
     * @param  z   zipper object
     *
     * @throws ConfigException if a configuration parsing error
     */
    void initZipper(SynchronizationRequest req, Zipper z) 
            throws ConfigException {

        //sets the max buffer size of bytes
        long buffer = getMaxBuffer();
        z.setMaxBuffer(buffer);

        processAlwaysInclude(req, z);
    }

    /**
     * Assembles the synchronization response.
     *
     * @param  zipSize  size from the zipper
     * @param  results  processed synchronization requests
     *
     * @throws  IOException  if an i/o error
     */
    void postProcess(long zipSize, SynchronizationRequest[] results) 
            throws IOException {

        _cos.flush();
        byte[] zipBytes = null;

        // converts the byte array output stream
        zipBytes = _bos.toByteArray(); 
        if (zipBytes == null) {
            zipBytes = new byte[0];    
        }

        // checksum
        long checksum  = _cos.getChecksum().getValue();

        // closes the streams
        if (zipBytes.length > 0) {
            _out.close();
            _out = null;
        }

        // synchronization response
        _response = new SynchronizationResponse(zipBytes, results, 
                    checksum, _ctx.getStartTime(), System.currentTimeMillis());
    }

    /**
     * Returns the max byte buffer size to be used in synchronization. 
     */
    private long getMaxBuffer() {

        long maxBuffer = MAX_BUFFER_SIZE;

        try {
            // use buffer defined in DAS's system environment
            String buffer = System.getProperty(MAX_BUFFER_PROP);
            if ( (buffer != null) && (!"".equals(buffer)) ) {

                maxBuffer = Long.parseLong(buffer);

            } else {
                /*
                // compute based on number of servers in the domain
                ConfigContext cCtx = _ctx.getConfigContext();
                Object[] servers = ServerHelper.getServersInDomain(cCtx);

                // 128m / num of servers
                Double d = new Double (134217728 / servers.length);
                maxBuffer = d.longValue();
                */
            }
        } catch (Exception e) {
            maxBuffer = MAX_BUFFER_SIZE;
        }

        return maxBuffer;
    }
}
