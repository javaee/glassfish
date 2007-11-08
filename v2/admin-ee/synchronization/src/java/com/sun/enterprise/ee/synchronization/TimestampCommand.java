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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;


/**
 * Represents a concrete implementation of a command that processes 
 * the timestamps.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class TimestampCommand implements Command {

    private static Logger _logger = Logger.getLogger(
            EELogDomains.SYNCHRONIZATION_LOGGER);

    private static final StringManager _localStrMgr = 
               StringManager.getManager(TimestampCommand.class);

    /**
     * Initializes the arguments.
     *
     * @param  req   synchronization request
     * @param  res   synchronization response
     */
    TimestampCommand(SynchronizationRequest req, SynchronizationResponse res) {

        _request  = req;
        _response = res;
    }

    /**
     * Stores the synchronization start time in a timestamp file as 
     * specified (in the meta xml file).
     *
     * @throws  SynchronizationException  if an error while processing the zip
     */
    public void execute() throws SynchronizationException {

        try {
            long startTime    = _response.getSynchronizationStartTime();
            File cacheTSFile  = _request.getCacheTimestampFile();
            assert (cacheTSFile != null);
            File parentDir    = cacheTSFile.getParentFile();

            // validate the new time stamp
            if (cacheTSFile.exists()) {
                BufferedReader is = null;
                try {
                    is = new BufferedReader(
                            new FileReader( cacheTSFile) );
                    long lastModifiedTime = Long.parseLong(is.readLine());

                    is.close();
                    is = null;

                    if (startTime < lastModifiedTime) {
                        String msg = _localStrMgr.getString(
                                        "invalidSynchronizationTS", 
                                        cacheTSFile.getPath(), 
                                        Long.toString(startTime), 
                                        Long.toString(lastModifiedTime));

                        throw new SynchronizationException(msg);
                    }
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception e) { }
                    }
                }
            } else {
                // create the directories
                // bug 6383913 - don't create empty policy dir
                // .timestamp file should be always in a wellknown directory
                //cacheTSFile.getParentFile().mkdirs();
            }

            if (parentDir.exists()) {

                FileWriter fw = null;

                try {
                    // writes the synchronization start time to the file
                    fw = new FileWriter(cacheTSFile);
                    fw.write(Long.toString(startTime));
                    fw.flush();
                    fw.close();
                    fw = null;
                    _logger.log(Level.FINE, 
                        "synchronization.ts_created", cacheTSFile.getPath());
                } finally {
                    try {
                        if (fw != null) {
                            fw.close();
                        }
                    } catch (Exception e) { }
                }
            } else {
                _logger.log(Level.FINE, 
                    "synchronization.ts_exists", cacheTSFile.getPath());
            }
        } catch (Exception e) {
            _logger.log(Level.FINE, 
                 "synchronization.ts_not_created", _request.getMetaFileName());
            String msg = _localStrMgr.getString("updateTimestampError" ,  
                _request.getMetaFileName());
            throw new SynchronizationException(msg, e);
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
     * No-op.
     */
    public Object getResult() {
        return null;
    }

    // ---- VARIABLE(S) - PRIVATE -------------------------------
    private SynchronizationRequest _request    = null;
    private SynchronizationResponse _response  = null;
    private static final String NAME = 
                        "Synchronization-Timestamp-Command";
}
