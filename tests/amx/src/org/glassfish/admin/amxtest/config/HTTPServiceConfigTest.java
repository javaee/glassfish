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
package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.config.AccessLogConfig;
import com.sun.appserv.management.config.ConnectionPoolConfig;
import com.sun.appserv.management.config.HTTPFileCacheConfig;
import com.sun.appserv.management.config.HTTPProtocolConfig;
import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.KeepAliveConfig;
import com.sun.appserv.management.config.RequestProcessingConfig;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 */
public final class HTTPServiceConfigTest
        extends AMXTestBase {
    public HTTPServiceConfigTest() {
    }

    synchronized final HTTPServiceConfig
    proxy()
            throws IOException {
        return getConfigConfig().getHTTPServiceConfig();
    }

    static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

    public void
    testRequestProcessing()
            throws Exception {
        if (!checkNotOffline("testRequestProcessing")) {
            return;
        }

        RequestProcessingConfig on = proxy().getRequestProcessingConfig();
        boolean exists = (on != null);
        if (!exists) {
            on = proxy().createRequestProcessingConfig(EMPTY_MAP);
            assert on == proxy().getRequestProcessingConfig();
        }
        RequestProcessingConfig rp = proxy().getRequestProcessingConfig();
        assert rp != null;
        if (!exists) {
            proxy().removeRequestProcessingConfig();
        }
    }

    public void
    testKeepAlive()
            throws Exception {
        if (!checkNotOffline("testKeepAlive")) {
            return;
        }

        KeepAliveConfig on = proxy().getKeepAliveConfig();
        boolean exists = (on != null);
        if (!exists) {
            on = proxy().createKeepAliveConfig(EMPTY_MAP);
            assert on == proxy().getKeepAliveConfig();
        }
        KeepAliveConfig rp = proxy().getKeepAliveConfig();
        assert rp != null;
        if (!exists) {
            proxy().removeKeepAliveConfig();
        }
    }

    public void
    testAccessLog()
            throws Exception {
        if (!checkNotOffline("testAccessLog")) {
            return;
        }

        AccessLogConfig on = proxy().getAccessLogConfig();
        boolean exists = (on != null);
        if (!exists) {
            on = proxy().createAccessLogConfig(EMPTY_MAP);
            assert on == proxy().getAccessLogConfig();
        }
        AccessLogConfig rp = proxy().getAccessLogConfig();
        assert rp != null;
        if (!exists) {
            proxy().removeAccessLogConfig();
        }
    }

    public void
    testHTTPFileCache()
            throws Exception {
        if (!checkNotOffline("testHTTPFileCache")) {
            return;
        }

        HTTPFileCacheConfig on = proxy().getHTTPFileCacheConfig();
        boolean exists = (on != null);
        if (!exists) {
            on = proxy().createHTTPFileCacheConfig(EMPTY_MAP);
            assert on == proxy().getHTTPFileCacheConfig();
        }
        HTTPFileCacheConfig rp = proxy().getHTTPFileCacheConfig();
        assert rp != null;
        if (!exists) {
            proxy().removeHTTPFileCacheConfig();
        }
    }

    public void
    testConnectionPool()
            throws Exception {
        if (!checkNotOffline("testConnectionPool")) {
            return;
        }

        ConnectionPoolConfig on = proxy().getConnectionPoolConfig();
        boolean exists = (on != null);
        if (!exists) {
            on = proxy().createConnectionPoolConfig(EMPTY_MAP);
            assert on == proxy().getConnectionPoolConfig();
        }
        ConnectionPoolConfig rp = proxy().getConnectionPoolConfig();
        assert rp != null;
        if (!exists) {
            proxy().removeConnectionPoolConfig();
        }
    }

    public void
    testHTTPProtocol()
            throws Exception {
        if (!checkNotOffline("testHTTPProtocol")) {
            return;
        }

        HTTPProtocolConfig on = proxy().getHTTPProtocolConfig();
        boolean exists = (on != null);
        if (!exists) {
            on = proxy().createHTTPProtocolConfig(EMPTY_MAP);
            assert on == proxy().getHTTPProtocolConfig();
        }
        HTTPProtocolConfig rp = proxy().getHTTPProtocolConfig();
        assert rp != null;
        if (!exists) {
            proxy().removeHTTPProtocolConfig();
        }
    }
}


