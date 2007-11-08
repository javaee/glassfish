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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv;

import java.io.IOException;
import java.util.Map;
import java.util.Hashtable;

import com.sun.mfwk.agent.appserv.logging.LogDomains;


/**
 * Main driver class to get a ASServerManager instance. 
 */
public class ASServerManagerRegistry {

    /**
     * Returns the singleton instance of this class.
     *
     * @return  singleton instance
     */
    public static ASServerManagerRegistry getInstance() {
        return _instance;
    }

    /**
     * Private constructor.
     */
    private ASServerManagerRegistry() {
        _serverManagers = new Hashtable();
    }

    /**
     * Returns ASServerManager for the given server instance. 
     *
     * @param  serverName  application server instance name
     * @param  domainName  application server domain name
     * @return ASServerManager instance
     * 
     * @throws  IOException if an i/o error
     */
    public ASServerManager getASServerManager(String serverName, 
            String domainName) throws IOException {

        String instanceName = domainName + serverName;

        // checking with cache
        ASServerManager serverManager = 
            (ASServerManager) _serverManagers.get(instanceName);

        if (serverManager == null) {
            //ASServerManager is not available for given server
            throw new IllegalArgumentException();
        }

        return serverManager;
    }


    /**
     * Sets the ASServerManager for a server instance.
     *
     * @param  moduleName  JEMF module name for Application Server plugin.
     * @param  serverName  application server instance name
     * @param  domainName  application server domain name
     * 
     * @throws  IOException if an i/o error
     */
    public void addASServerManager(String moduleName, 
        String serverName, String domainName) 
            throws IOException { 

        String instanceName = domainName + serverName;

        LogDomains.getLogger().fine(
            "Setting ASServerManager for server: " + instanceName);

        ASServerManager serverManager = 
            new ASServerManager(moduleName, serverName, domainName);

        synchronized (this) {
            _serverManagers.put(instanceName, serverManager);
        }
    }


    /**
     * Cleans up ASServerManager for the given server instance. 
     * This method is called when a server instance is detected to 
     * be going down from the plugin.
     *
     * @param  serverName  application server instance name
     * @param  domainName  application server domain name
     */
    public synchronized void removeASServerManager(String serverName, 
            String domainName) {

        String instanceName = domainName + serverName;

        LogDomains.getLogger().fine(
            "Removing ASServerManager for server: " + instanceName);

        _serverManagers.remove(instanceName);
    }

    // ---- PRIVATE - VARIABLES -------------------------------
    private static Map _serverManagers = null;
    private static final ASServerManagerRegistry _instance = new ASServerManagerRegistry();
}
