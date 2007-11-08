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

/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.connection;

import java.util.Map;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.TrustAnyTrustManager;
import com.sun.appserv.management.client.TLSParams;
import com.sun.mfwk.agent.appserv.util.Constants;
import com.sun.mfwk.agent.appserv.logging.LogDomains;

import java.io.IOException;

/**
 * Sets up a TLS connection.
 */
public class TrustAnyConnectionFactory {

    /**
     * Returns a mbean server connection.
     *
     * @param  info  connection info
     * @return  mbean server connection
     */
    public synchronized static MBeanServerConnection getConnection(Map info) 
            throws IOException {

        String user      = (String) info.get(Constants.USER_KEY);
        String password  = (String) info.get(Constants.PASSWORD_KEY);
        String host      = (String) info.get(Constants.HOST_KEY);
        String port      = (String) info.get(Constants.PORT_KEY);
        String server    = (String) info.get(Constants.SERVER_KEY);

        // mandatory arguments
        if ((user==null) || (password==null) || (host==null) 
                || (port==null) || (server==null)) {

            throw new IllegalArgumentException();
        }

        // checking the cache
        MBeanServerConnection mbs = (MBeanServerConnection) _cache.get(server);

        // connection is not created or stale
        if ( (mbs == null) || (isStaleConnection(mbs)) ) {
            
            LogDomains.getLogger().fine(
                "Creating new connection for server: " + server);

            TLSParams tlsParams = new TLSParams(
                TrustAnyTrustManager.getInstanceArray(), null);

            AppserverConnectionSource src = new AppserverConnectionSource(
                AppserverConnectionSource.PROTOCOL_RMI, host, 
                Integer.parseInt(port), user, password, tlsParams, null);
            
            mbs = src.getMBeanServerConnection(true);
            _cache.put(server, mbs);
        }

        return mbs;
    }

    /**
     * Returns true if connection is invalid.
     *
     * @param  mbs  mbean server connection
     */
    public static boolean isStaleConnection(MBeanServerConnection mbs) {

        boolean stale = false;
        try {
            mbs.getDefaultDomain();
        } catch (Exception e) {
            stale = true;
        }

        return stale;
    }

    // ---- VARIABLES - PRIVATE ---------------------------
    private static Map _cache = new Hashtable();
}
