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

import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.CheckedInputStream;

import com.sun.enterprise.ee.util.zip.Unzipper;
import com.sun.enterprise.ee.synchronization.SynchronizationResponse;
import com.sun.enterprise.ee.synchronization.processor.ServletProcessor;

import java.io.IOException;


/**
 * Explodes a zip file under the target directory.
 */
class HttpUnzipper extends Unzipper {

    /**
     * Constructor.
     *
     * @param   targetDirectory   name of base directory where the zip content
     *                            will be exploded
     */
    HttpUnzipper(String targetDirectory) {
        super(targetDirectory);
    }

    /**
     * Explodes a zip file from the given input stream under the target 
     * directory. This method handles special case for HTTP processing.
     * It deserializes the synchronization response from the input stream
     * while processing the zip entries from input stream.
     *
     * @param  in  input stream from zip file
     * @return synchronization response
     *
     * @throws  IOException  if an i/o error
     */
    public SynchronizationResponse writeZip(InputStream in, String resEntry) 
            throws IOException, ClassNotFoundException {
                
        SynchronizationResponse response = null;
        ZipInputStream zin               = null;
        CheckedInputStream cis           = null;
        ZipEntry entry                   = null;
        ObjectInputStream oin            = null;

        try {
            cis   = new CheckedInputStream(in, new CRC32());            
            zin   = new ZipInputStream(cis);            
            entry = zin.getNextEntry();

            while (entry != null) {

                String eName = entry.getName();

                if (isIgnoredEntry(eName)) {

                    // do nothing, skip this entry

                } else if (ServletProcessor.RESPONSE_ENTRY_NAME.equals(eName)) {

                    oin = new ObjectInputStream(zin);
                    response = (SynchronizationResponse) oin.readObject();

                } else {

                    writeZipEntry(entry, zin); 
                }
                
                // next entry
                zin.closeEntry();
                entry = zin.getNextEntry();
            }                                    
            zin.close();
            zin = null;
            return response;

        } finally {
            if (cis != null) {
                try { cis.close(); } catch (Exception ex) { }
            }
            if (zin != null) {
                try { zin.close(); } catch (Exception ex) { }
            }
            if (oin != null) {
                try { oin.close(); } catch (Exception ex) { }
            }
        }
    }
}
