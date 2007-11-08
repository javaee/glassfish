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
package com.sun.enterprise.ee.synchronization.impl;

import java.io.IOException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.admin.server.core.AdminService;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ServerHelper;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.ee.synchronization.api.SecurityServiceMgr;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.ee.synchronization.SynchronizationException;

/**
 * This provides support to synchronize key file from DAS.
 *
 * @author Satish Viswanatham
 * @since  JDK1.4
 */
public class SecurityServiceMgrImpl implements SecurityServiceMgr {

    /**
     * Default Constructor.
     *
     * @param ctx      configContext
     */
    public SecurityServiceMgrImpl(ConfigContext ctx) {
        if ( AdminService.getAdminService().isDas() ) {
            String msg = _localStrMgr.getString("DasNotSupported");
            throw new RuntimeException(msg);
        }
        _ctx = ctx;
    }

    public SecurityServiceMgrImpl() {
        if ( AdminService.getAdminService().isDas() ) {
            String msg = _localStrMgr.getString("DasNotSupported");
            throw new RuntimeException(msg);
        }
    }

    public void setConfigContext(ConfigContext ctx) {
        _ctx = ctx;
    }

    // ---- START OF SecurityServiceMgr Interface -------------------------

    public void synchronizeKeyFile(String realmName) 
            throws SynchronizationException {

        // connects to DAS 
        String dasName = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;
        SynchronizationClientImpl sc = new SynchronizationClientImpl(dasName);

        _logger.log(Level.FINEST,
                    "synchronization.done.keyfile.connect",dasName);
        try {
            sc.connect();
        } catch (IOException ie) {
           String msg = _localStrMgr.getString("ConnectFailedDAS");
           throw new SynchronizationException(msg);
        }

        // current server name
        String serverName=ApplicationServer.getServerContext().
                            getInstanceName();
        if (serverName == null) {
            String msg = _localStrMgr.getString("CurrInstanceError");
           throw new RuntimeException(msg);
        }

        // "${com.sun.aas..instanceRoot}/config/keyfile"
        String keylocation = getLocation(serverName, realmName);  

        _logger.log(Level.FINEST,
                    "synchronization.done.keyfile.getloc",serverName);

        // Properties array-- right now includes instanceRoot
        String []props = new String[1];
        props[0] = SystemPropertyConstants.INSTANCE_ROOT_PROPERTY;

        String src = RelativePathResolver.unresolvePath(keylocation, props);
        if (src == null) {
            String msg=_localStrMgr.getString("UnresolvePathError",keylocation);
            throw new RuntimeException(msg);
        }

        _logger.log(Level.FINEST,_strMgr.getString(
                    "synchronization.done.keyfile.unresolve",keylocation, src));

        sc.get(src, keylocation);
        _logger.log(Level.FINEST,
                    "synchronization.done.keyfile.get",keylocation);
        try {
            sc.disconnect();
        } catch (IOException ie) {
           String msg = _localStrMgr.getString("DisConnectFailedDAS");
           throw new SynchronizationException(msg);
        }
        _logger.log(Level.FINE,
                    "synchronization.done.keyfile.download",keylocation);

    }

    // ---- END OF SecurityServiceMgr Interface -------------------------

    private String getLocation(String serverName, String realmName) 
                throws SynchronizationException {
        try {
            if (_ctx == null ) {
                String msg = _localStrMgr.getString("ContextNotSet");
                throw new RuntimeException(msg);
            }
            Config config = ServerHelper.getConfigForServer(_ctx,serverName);
            if (config == null ) {
                String msg = _localStrMgr.getString("ConfigElementMissing",
                                serverName);
                throw new RuntimeException(msg);
            }
            return  config.getSecurityService().getAuthRealmByName(realmName).
                        getElementPropertyByName("file").getValue();
        } catch (ConfigException ce) {
            String msg = _localStrMgr.getString("GetLocationConfigErr");
            throw new RuntimeException(msg);
        }

    }

    //-------- PRIVATE VARIABLES --------------------------------------
    private ConfigContext _ctx   = null;
    private String   _serverName = null;

    private static Logger _logger =
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);

    private static final StringManagerBase _strMgr =
        StringManagerBase.getStringManager(_logger.getResourceBundleName());

    private static final StringManager _localStrMgr=StringManager.getManager
            (SecurityServiceMgrImpl.class);
}
