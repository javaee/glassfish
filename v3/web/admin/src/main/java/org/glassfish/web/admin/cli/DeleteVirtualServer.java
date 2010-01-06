/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.web.admin.cli;

import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigCode;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.TransactionFailure;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Delete virtual server command
 * 
 */
@Service(name="delete-virtual-server")
@Scoped(PerLookup.class)
@I18n("delete.virtual.server")
public class DeleteVirtualServer implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteVirtualServer.class);

    @Param(name="virtual_server_id", primary=true)
    String vsid;

    @Inject
    HttpService httpService;

    @Inject
    NetworkConfig networkConfig;

    @Inject(name=ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Server server;

    //xxx
    
    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
 
        if(!exists()) {
            report.setMessage(localStrings.getLocalString("delete.virtual.server.notexists", "{0} doesn't exist", vsid));
            report.setActionExitCode(ExitCode.FAILURE);
            return;
        }

        // reference check
        String referencedBy = getReferencingListener();
        if(referencedBy != null && referencedBy.length() != 0) {
            report.setMessage(localStrings.getLocalString("delete.virtual.server.referenced", 
                "Virtual Server, {0} can not be deleted because it is referenced from http listener, {1}", vsid, referencedBy));
            report.setActionExitCode(ExitCode.FAILURE);
            return;
        }

        try {

            // we need to determine which deployed applications reference this virtual-server
            List<ApplicationRef> appRefs = new ArrayList<ApplicationRef>();
            for (ApplicationRef appRef : server.getApplicationRef()) {
                if (appRef.getVirtualServers()!=null && appRef.getVirtualServers().contains(vsid)) {
                    appRefs.add(appRef);
                }
            }
            // transfer into the array of arguments
            ConfigBeanProxy[] proxies = new ConfigBeanProxy[appRefs.size()+1];
            proxies[0] = httpService;
            for (int i=0;i<appRefs.size();i++) {
                proxies[i+1] = appRefs.get(i);
            }

            ConfigSupport.apply(new Config(vsid), proxies);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        } catch(TransactionFailure e) {
            report.setMessage(localStrings.getLocalString("delete.virtual.server.fail", "{0} delete failed ", vsid));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
    
    private boolean exists() {
        if(vsid == null)
            return false;
        
        List<VirtualServer> list = httpService.getVirtualServer();
        
        for(VirtualServer vs : list) {
            String currId = vs.getId();
         
            if(currId != null && currId.equals(vsid))
                return true;
        }
        return false;
    }

    private String getReferencingListener() {
        List<NetworkListener> list = networkConfig.getNetworkListeners().getNetworkListener();
        
        for(NetworkListener listener: list) {
            String virtualServer = listener.findHttpProtocol().getHttp().getDefaultVirtualServer();
         
            if(virtualServer != null && virtualServer.equals(vsid)) {
                return listener.getName();
            }
        }
        return null;
    }

    private static class Config implements ConfigCode {
        private Config(String vsid) {
            this.vsid = vsid;
        }
        public Object run(ConfigBeanProxy... proxies) throws PropertyVetoException, TransactionFailure {
            List<VirtualServer> list = ((HttpService) proxies[0]).getVirtualServer();
            for(VirtualServer item : list) {
                String currId = item.getId();
                if (currId != null && currId.equals(vsid)) {
                    list.remove(item);
                    break;
                }
            }
            // we now need to remove the virtual server id from all application-ref passed.
            if (proxies.length>1) {
                // we have some appRefs to clean.
                for (int i=1;i<proxies.length;i++) {
                    ApplicationRef appRef = (ApplicationRef) proxies[i];
                    StringBuilder newList = new StringBuilder();
                    StringTokenizer st = new StringTokenizer(appRef.getVirtualServers(), ",");
                    while (st.hasMoreTokens()) {
                        final String id = st.nextToken();
                        if (!id.equals(vsid)) {
                            if (newList.length()>0) {
                                newList.append(",");
                            }
                            newList.append(id);
                        }
                    }
                    appRef.setVirtualServers(newList.toString());
                }
            }
            return list;
        }
        private String vsid;
    }
}
