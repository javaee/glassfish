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
import com.sun.enterprise.util.io.FileUtils;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;


/**
 * Represents a concrete implementation of a command that processes 
 * timestamp removal requests. This is called when synchronization 
 * fails to remove the timestamp files.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class TimestampRemoveCommand implements Command {

    /**
     * Initializes the arguments.
     *
     * @param  req   synchronization request
     * @param  res   synchronization response
     */
    public TimestampRemoveCommand(SynchronizationRequest req, 
            SynchronizationResponse res) {

        _request  = req;
        _response = res;
    }

    /**
     * Removes the synchronization timestamp file as specified 
     * (in the meta xml file).
     *
     * @throws  SynchronizationException  if an error while processing the zip
     */
    public void execute() throws SynchronizationException {

        try {
            File cacheTSFile  = _request.getCacheTimestampFile();
            assert (cacheTSFile != null);

            // removes the TS file
            if (cacheTSFile.exists()) {
                FileUtils.whack(cacheTSFile);
            }
        } catch (Exception e) {
            throw new SynchronizationException(e);
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
                        "Synchronization-Timestamp-Remove-Command";

    private static Logger _logger = Logger.getLogger(
            EELogDomains.SYNCHRONIZATION_LOGGER);

    private static final StringManager _localStrMgr = 
               StringManager.getManager(TimestampRemoveCommand.class);
}
