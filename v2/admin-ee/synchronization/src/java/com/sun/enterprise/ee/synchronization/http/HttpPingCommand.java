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

import com.sun.enterprise.ee.synchronization.Ping;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;

/**
 * Represents a concrete implementation of a command that pings 
 * central repository admin. 
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class HttpPingCommand implements Ping {

    private static Logger _logger = 
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);


    /**
     * Initializes the arguments.
     *
     * @param   dpr   obj that provides access to DAS info
     */
    HttpPingCommand(DASPropertyReader dpr, String url) {
        _dpr = dpr;
        _url = url;
    }

    /**
     * Pings central repository admin.
     */
    public void execute() {

        String url = null;
        try {
            int count = 0;

            // (re) attempts to contact DAS 
            while (count < MAX_RETRY) {
                try {
                    _alive = HttpUtils.ping(_url);

                    if (_alive) {
                        // made contact with DAS
                        _response = null;
                        break;
                    }

                } catch (Exception e) {
                    _response = e;
                    _logger.log(Level.FINE, 
                        SYNC_NO_CONNECTION, url);
                    Thread.currentThread().sleep(DEFAULT_RETRY_INTV);
                }

                // failed to contact DAS. 
                // Increment the retry count and try again
                ++count;
            }
        } catch (Exception e) {
            _response = e;
            _logger.log(Level.FINE, SYNC_NO_CONNECTION, url);
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
     * Returns true if central repository admin is alive.
     * 
     * @return  true if central repository admin is alive
     */
    public boolean isAlive() {
        return _alive;
    }

    /**
     * No-op.
     */
    public Object getResult() {
        return _response;
    }

    // ---- VARIABLE(S) - PRIVATE -------------------------------
    private Object _response            = null;
    private boolean _alive              = false;
    private DASPropertyReader _dpr      = null;
    private String _url                 = null;
    private static final int MAX_RETRY  = 3;
    private static final String NAME    = "Synchronization-Ping-Command";    
    private static final long DEFAULT_RETRY_INTV  = 3000;
    private static final String SERVER_NAME       = 
            System.getProperty(SystemPropertyConstants.SERVER_NAME);
    private static final String SYNC_NO_CONNECTION = 
                                        "synchronization.no_connection";
}
