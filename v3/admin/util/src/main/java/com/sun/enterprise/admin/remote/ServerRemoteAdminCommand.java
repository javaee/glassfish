/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 * 
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 * 
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 * 
 * Contributor(s):
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

package com.sun.enterprise.admin.remote;

import com.sun.enterprise.admin.util.HttpConnectorAddress;
import com.sun.enterprise.config.serverbeans.SecureAdmin;
import com.sun.enterprise.security.ssl.SSLUtils;
import java.net.URLConnection;
import java.util.logging.Logger;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.component.Habitat;

/**
 * RemoteAdminCommand which is sent from a server (DAS or instance).
 * <p>
 * This class identifies the origin as a server (as opposed to a true
 * admin client) for server-to-server authentication.
 *
 * @author Tim Quinn
 */
public class ServerRemoteAdminCommand extends RemoteAdminCommand {

    private final static String SSL_SOCKET_PROTOCOL = "TLS";

    private Habitat habitat;

    private SecureAdmin secureAdmin;

    private ServerEnvironment serverEnv;

    private SSLUtils _sslUtils = null;

    public ServerRemoteAdminCommand(Habitat habitat, String name, String host, int port,
            boolean secure, String user, String password, Logger logger)
            throws CommandException {
        super(name, host, port, false, "admin", "", logger);
        completeInit(habitat);
    }

    private synchronized void completeInit(final Habitat habitat) {
        this.habitat = habitat;
        secureAdmin = habitat.getComponent(SecureAdmin.class);
        serverEnv = habitat.getComponent(ServerEnvironment.class);
    }

    @Override
    protected HttpConnectorAddress getHttpConnectorAddress(String host, int port, boolean shouldUseSecure) {
        if (SecureAdmin.Util.isEnabled(secureAdmin)) {
            return new HttpConnectorAddress(host, port,
                sslUtils().getAdminSocketFactory(getCertAlias(), SSL_SOCKET_PROTOCOL));
        } else {
            return super.getHttpConnectorAddress(host, port, shouldUseSecure);
        }
    }

    /**
     * Adds the admin indicator header to the request so, in the unsecured admin
     * use case, admin requests from the DAS to instances will be accepted as
     * legitimate.
     *
     * @param urlConnection
     */
    @Override
    protected void addAdditionalHeaders(final URLConnection urlConnection) {
        /*
         * If secure admin is enabled, we do not need to add the admin indicator header.
         */
        if ( ! SecureAdmin.Util.isEnabled(secureAdmin)) {
            final String indicatorValue = SecureAdmin.Util.configuredAdminIndicator(secureAdmin);
            if (indicatorValue != null) {
                urlConnection.setRequestProperty(
                        SecureAdmin.Util.ADMIN_INDICATOR_HEADER_NAME,
                        indicatorValue);
            }
        }
    }

    private synchronized String getCertAlias() {
        return (serverEnv.isDas() ? SecureAdmin.Util.DASAlias(secureAdmin) :
            SecureAdmin.Util.instanceAlias(secureAdmin));
    }

    private synchronized SSLUtils sslUtils() {
        if (_sslUtils == null) {
            _sslUtils = habitat.getComponent(SSLUtils.class);
        }
        return _sslUtils;
    }
}
