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

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.DomainConfig;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 */
public final class DomainConfigTest
        extends AMXTestBase {
    public DomainConfigTest() {
    }

    public void
    testGetDeployedItemProxies() {
        final DomainConfig mgr = getDomainConfig();

        final Set proxies = mgr.getContaineeSet();
        assert (proxies.size() != 0);

        final Iterator iter = proxies.iterator();
        while (iter.hasNext()) {
            final AMX proxy = Util.asAMX(iter.next());
        }
    }

    public void
    testGetDeployedItemProxiesByName() {
        final DomainConfig mgr = getDomainConfig();

        final Map<String, Map<String, AMX>> typeMap = mgr.getMultiContaineeMap(null);

        for (final String j2eeType : typeMap.keySet()) {
            final Map<String, AMX> proxyMap = typeMap.get(j2eeType);
            for (final String name : proxyMap.keySet()) {
                final AMX amx = Util.asAMX(proxyMap.get(name));

                final AMX proxy = mgr.getContainee(j2eeType, name);

                assert (Util.getObjectName(proxy).equals(Util.getObjectName(amx)));
                assert (proxy.getName().equals(name));
            }
        }
    }


    public void
    testGetAttributes() {
        final DomainConfig mgr = getDomainConfig();

        mgr.getApplicationRoot();
        mgr.getLocale();
        mgr.getLogRoot();
    }

    private <T extends AMX> void
    checkMap(final Map<String, T> m) {
        assert (m != null);
        assert (!m.keySet().contains(AMX.NO_NAME));
        assert (!m.keySet().contains(AMX.NULL_NAME));
    }


    public void
    testGetMaps() {
        final DomainConfig m = getDomainConfig();

        checkMap(m.getServerConfigMap());
        checkMap(m.getStandaloneServerConfigMap());
        checkMap(m.getClusteredServerConfigMap());
        checkMap(m.getLBConfigMap());
        checkMap(m.getLoadBalancerConfigMap());
        checkMap(m.getNodeAgentConfigMap());
        checkMap(m.getConfigConfigMap());
        checkMap(m.getClusterConfigMap());

        checkMap(m.getPersistenceManagerFactoryResourceConfigMap());
        checkMap(m.getJDBCResourceConfigMap());
        checkMap(m.getJDBCConnectionPoolConfigMap());
        checkMap(m.getConnectorResourceConfigMap());
        checkMap(m.getConnectorConnectionPoolConfigMap());
        checkMap(m.getAdminObjectResourceConfigMap());
        checkMap(m.getResourceAdapterConfigMap());
        checkMap(m.getMailResourceConfigMap());

        checkMap(m.getJ2EEApplicationConfigMap());
        checkMap(m.getEJBModuleConfigMap());
        checkMap(m.getWebModuleConfigMap());
        checkMap(m.getRARModuleConfigMap());
        checkMap(m.getAppClientModuleConfigMap());
        checkMap(m.getLifecycleModuleConfigMap());
    }

    /*
         KEEP, not quite ready to test this yet.
         public void
     testCreateStandaloneServerConfig()
     {
         final ConfigSetup setup  = new ConfigSetup( getDomainRoot() );

         setup.removeTestServer();

         final StandaloneServerConfig server = setup.createTestServer();
         setup.removeTestServer();
     }
     */


    public void
    testCreateClusterConfig() {
        // to be done
    }
}



























