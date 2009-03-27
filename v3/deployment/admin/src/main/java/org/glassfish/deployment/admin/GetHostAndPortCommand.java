/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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

package org.glassfish.deployment.admin;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;
import org.jvnet.hk2.annotations.Service;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.HostAndPort;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;
import java.util.List;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Service(name="get-host-and-port")
@Scoped(PerLookup.class)
public class GetHostAndPortCommand implements AdminCommand {

    @Param(optional=true)
    public String target = "server";

    @Param(optional=true)
    public String virtualServer = null;

    @Param(optional=true, defaultValue="false")
    public Boolean securityEnabled = false;

    @Param(optional=true)
    public String moduleId = null;

    @Inject
    HttpService httpService;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(GetHostAndPortCommand.class);    

    public void execute(AdminCommandContext context) {
        
        final ActionReport report = context.getActionReport();

        ActionReport.MessagePart part = report.getTopMessagePart();

        HostAndPort hostAndPort = null;
        try {
            hostAndPort = getHostAndPortForRequest();
        } catch (Exception e) {
            report.setMessage(e.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (hostAndPort != null) {
            part.setMessage(hostAndPort.getHost() + ":" + 
                hostAndPort.getPort());
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private HostAndPort getHostAndPortForRequest() throws Exception {
        if (moduleId == null) {
            if (virtualServer == null) {
                return getHostAndPort(securityEnabled);
            } else {
                VirtualServer vs = httpService.getVirtualServerByName(
                    virtualServer);
                if (vs == null) {
                    throw new Exception("Virtual server: " + 
                        virtualServer + " does not exist!");
                }
                return getHostAndPort(vs, securityEnabled);
            }
        }

        ApplicationRef appRef = ConfigBeansUtilities.getApplicationRefInServer(
            target, moduleId); 

        if (appRef == null) {
            throw new Exception("Application : " + appRef.getRef() + 
                " does not exist on target " + target);
        }
        
        List<String> vsList = StringUtils.parseStringList(
            appRef.getVirtualServers(), " ,");

        if (vsList==null) {
            return getHostAndPort(securityEnabled);
        }

        for (String virtualServer : vsList) {
            HostAndPort hp = getHostAndPort(
                httpService.getVirtualServerByName(virtualServer), 
                securityEnabled);
            if (hp!=null) {
                return hp;
            }
        }
        return null;
    }

    private HostAndPort getHostAndPort(VirtualServer vs, 
        boolean securityEnabled) {
        List<VirtualServer> virtualServerList =
            httpService.getVirtualServer();
        List<HttpListener> httpListenerList =
            httpService.getHttpListener();

        for (VirtualServer virtualServer : virtualServerList) {
            if (!virtualServer.getId().equals(vs.getId())) {
                continue;
            }
            String vsHttpListeners = virtualServer.getHttpListeners();
            List<String> vsHttpListenerList =
                StringUtils.parseStringList(vsHttpListeners, " ,");

            for (String vsHttpListener : vsHttpListenerList) {
                for (HttpListener httpListener : httpListenerList) {
                    if (!httpListener.getId().equals(vsHttpListener)) {
                        continue;
                    }
                    if (!Boolean.valueOf(httpListener.getEnabled())) {
                        continue;
                    }
                    if (Boolean.valueOf(httpListener.getSecurityEnabled())
                        == securityEnabled) {
                        String serverName = httpListener.getServerName();
                        if (serverName == null ||
                            serverName.trim().equals("")) {
                            serverName = getDefaultHostName();
                        }
                        String portStr = httpListener.getPort();
                        String redirPort = httpListener.getRedirectPort();
                        if (redirPort != null &&
                            !redirPort.trim().equals("")) {
                            portStr = redirPort;
                        }
                        int port = Integer.parseInt(portStr);
                        return new HostAndPort(
                            serverName, port, securityEnabled);
                    }
                }
            }
        }
        return null;
    }

    private HostAndPort getHostAndPort(boolean securityEnabled) {
        List<HttpListener> httpListenerList =
            httpService.getHttpListener();

        for (HttpListener httpListener : httpListenerList) {
            if (!Boolean.valueOf(httpListener.getEnabled())) {
                continue;
            }
            if (httpListener.getDefaultVirtualServer().equals("__asadmin")){
                continue;
            }
            if (Boolean.valueOf(httpListener.getSecurityEnabled()) ==
                securityEnabled) {

                String serverName = httpListener.getServerName();
                if (serverName == null ||
                    serverName.trim().equals("")) {
                    serverName = getDefaultHostName();
                }
                String portStr = httpListener.getPort();
                String redirPort = httpListener.getRedirectPort();
                if (redirPort != null &&
                    !redirPort.trim().equals("")) {
                    portStr = redirPort;
                }
                int port = Integer.parseInt(portStr);
                return new HostAndPort(
                    serverName, port, securityEnabled);
            }
        }
        return null;
    }

    private HostAndPort getHostAndPort() {
        return getHostAndPort(false);
    }

    private String getDefaultHostName() {
        String defaultHostName = "localhost";
        try {
            InetAddress host = InetAddress.getLocalHost();
            defaultHostName = host.getCanonicalHostName();
        } catch(UnknownHostException uhe) {

           // ignore
        }
        return defaultHostName;
    }
}
