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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

import com.sun.enterprise.ee.util.zip.Zipper;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.SynchronizationResponse;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;
import com.sun.enterprise.config.ConfigException;

/**
 * Processor class that writes the output as a zipfile to the 
 * servlet output stream.
 *
 * @author  Nazrul Islam
 * @since   JDK 1.4
 */
public class ServletProcessor extends BaseProcessor {

    // synchronization response zip entry name
    public static final String RESPONSE_ENTRY_NAME = 
                    "com.sun.appserv.SynchronizationResponse";

    /**
     * Constructor.
     * 
     * @param   ctx  request context
     * @param   response  servlet response
     *
     * @throws  IOException  if an i/o error
     */
    public ServletProcessor(RequestContext ctx, HttpServletResponse response) 
            throws IOException {

        _ctx = ctx;

        response.setHeader("Content-Type", "application/zip");
        ServletOutputStream sout = response.getOutputStream();

        _out = new ZipOutputStream(sout);
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
     * Closes the zip output stream.
     *
     * @param  zipSize  size from the zipper
     * @param  results  processed synchronization requests
     *
     * @throws  IOException  if an i/o error
     */
    void postProcess(long zipSize, SynchronizationRequest[] results) 
            throws IOException {

        // synchronization response
        _response = new SynchronizationResponse(null, results, 
                    0, _ctx.getStartTime(), System.currentTimeMillis());

        // add this response as a zip entry
        addResponseToZip(_response);

        if (_out != null) {
            try {
                _out.close();
            } catch (Exception e) {
                // ignore - throws exception when zip is empty
            }
            _out = null;
        }
    }

    /**
     * Adds synchronization response as a zip entry.
     *
     * @param  response synchronization response
     *
     * @throws IOException if an error while serializing the response obj
     */
    private void addResponseToZip(SynchronizationResponse response) 
            throws IOException {

        // add a zip entry for response
        ZipEntry entry = new ZipEntry(RESPONSE_ENTRY_NAME);
        entry.setTime(System.currentTimeMillis());
        _out.putNextEntry(entry);

        // serialize response
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(response);
        oos.close();

        // add serialized bytes
        byte[] buf = bos.toByteArray();
        _out.write(buf, 0, buf.length);
        _out.closeEntry();
    }
}
