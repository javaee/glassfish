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
package org.glassfish.admin.amx.impl.ext;

import java.util.Map;
import javax.management.ObjectName;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Collection;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Module;

import com.sun.enterprise.security.ssl.SSLUtils;
import javax.management.JMException;
import javax.management.remote.JMXServiceURL;
import org.glassfish.admin.amx.base.Runtime;
import org.glassfish.admin.amx.impl.mbean.AMXImplBase;
import org.glassfish.admin.amx.impl.util.ImplUtil;
import org.glassfish.admin.amx.intf.config.Domain;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkConfig;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkListener;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkListeners;
import org.glassfish.admin.amx.intf.config.grizzly.Protocol;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.api.container.Sniffer;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.jvnet.hk2.component.Habitat;

import org.glassfish.admin.amx.impl.util.InjectedValues;
import org.glassfish.admin.mbeanserver.BooterNewMBean;

/**
AMX RealmsMgr implementation.
Note that realms don't load until {@link #loadRealms} is called.
 */
public final class RuntimeImpl extends AMXImplBase
// implements Runtime
{
    private final ApplicationRegistry appRegistry;

    private final Habitat mHabitat;

    public RuntimeImpl(final ObjectName parent)
    {
        super(parent, Runtime.class);

        mHabitat = InjectedValues.getInstance().getHabitat();

        appRegistry = mHabitat.getComponent(ApplicationRegistry.class);

    }

    /**
     *
     * Returns the deployment configuration(s), if any, for the specified
     * application.
     * <p>
     * For Java EE applications these will typically be the deployment
     * descriptors, with the map key the relative path to the DD and the
     * value that deployment descriptor's contents.
     *
     * @param appName name of the application of interest
     * @return map of app config names to config values
     */
    public Map<String, String> getDeploymentConfigurations(final String appName)
    {
        final ApplicationInfo appInfo = appRegistry.get(appName);
        if (appInfo == null)
        {
            throw new IllegalArgumentException(appName);
        }

        final Map<String, String> result = new HashMap<String, String>();
        try
        {
            for (Sniffer sniffer : appInfo.getSniffers())
            {
                result.putAll(sniffer.getDeploymentConfigurations(appInfo.getSource()));
            }
            return result;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void stopDomain()
    {
        // FIXME:  this might not work with authenticaion; need direct API call
        //executeREST( "stop-domain" );
        ModulesRegistry registry = InjectedValues.getInstance().getModulesRegistry();
        final Collection<Module> modules = registry.getModules("com.sun.enterprise.osgi-adapter");
        if (modules.size() == 1)
        {
            final Module mgmtAgentModule = modules.iterator().next();
            mgmtAgentModule.stop();
        }
        else
        {
            ImplUtil.getLogger().warning("Cannot find primordial com.sun.enterprise.osgi-adapter");
        }

        ImplUtil.getLogger().warning("Stopping server forcibly");
        System.exit(0);
    }

    private NetworkConfig networkConfig()
    {
        return getDomainRootProxy().child(Domain.class).getConfigs().getConfig().get("server-config").getNetworkConfig().as(NetworkConfig.class);
    }

    private static final String ADMIN_LISTENER_NAME = "admin-listener";

    private NetworkListener getAdminListener()
    {
        final NetworkConfig network = networkConfig();

        final NetworkListeners listeners = network.getNetworkListeners();

        final Map<String, NetworkListener> listenersMap = listeners.getNetworkListener();

        final NetworkListener listener = listenersMap.get(ADMIN_LISTENER_NAME);

        return listener;
    }

    private int getRESTPort()
    {
        return (int) (long) getAdminListener().resolveLong("Port");
    }

    private String get_asadmin()
    {
        final Protocol protocol = networkConfig().getProtocols().getProtocol().get(ADMIN_LISTENER_NAME);
        return protocol.getHttp().resolveAttribute("DefaultVirtualServer");
    }

    public String getRESTBaseURL()
    {
        final Protocol protocol = networkConfig().getProtocols().getProtocol().get(ADMIN_LISTENER_NAME);
        final String scheme = protocol.resolveBoolean("SecurityEnabled") ? "https" : "http";
        final String host = "localhost";

        return scheme + "://" + host + ":" + getRESTPort() + "/" + get_asadmin() + "/";
    }

    public String executeREST(final String cmd)
    {
        String result = null;

        HttpURLConnection conn = null;
        try
        {
            final String url = getRESTBaseURL() + cmd;

            final URL invoke = new URL(url);
            //System.out.println( "Opening connection to: " + invoke );
            conn = (HttpURLConnection) invoke.openConnection();

            final InputStream is = conn.getInputStream();
            result = toString(is);
            is.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            result = ExceptionUtil.toString(e);
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }
        return result;
    }

    public String[] getSupportedCipherSuites()
    {
        try
        {
            final SSLUtils sslUtils = mHabitat.getComponent(SSLUtils.class);
            return sslUtils.getSupportedCipherSuites();
        }
        catch (final Exception ex)
        {
            //TODO log exception
            ex.printStackTrace();
            return new String[0];
        }
    }

    public JMXServiceURL[] getJMXServiceURLs()
    {
        try
        {
            return (JMXServiceURL[]) getMBeanServer().getAttribute(BooterNewMBean.OBJECT_NAME, "JMXServiceURLs");
        }
        catch (final JMException e)
        {
            throw new RuntimeException(e);
        }
    }

}























