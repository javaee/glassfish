/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.admin.adapter;

import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.v3.admin.AdminAdapter;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import org.jvnet.hk2.config.types.Property;
import org.glassfish.server.ServerEnvironmentImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/** Makes various decisions about the admin adapters.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352; (km@dev.java.net)
 * @since GlassFish V3 (March 2008)
 */
public final class AdminEndpointDecider {

    private String asadminContextRoot;
    private String guiContextRoot;
    private List<String> asadminHosts; //list of virtual servers for asadmin
    private List<String> guiHosts;     //list of virtual servers for admin GUI
    
    private int port;  // both asadmin and admin GUI are on same port
    private Config cfg;
    private Logger log;
    
    public static final String ADMIN_LISTENER_ID = "admin-listener";
    public static final int ADMIN_PORT           = 4848;
    
    public AdminEndpointDecider(Config cfg, Logger log) {
        if (cfg == null || log == null)
            throw new IllegalArgumentException("config or logger can't be null");
        this.cfg = cfg;
        this.log = log;
        setValues();
    }
    
    public int getListenPort() {
        return port;
    }
    
    public List<String> getAsadminHosts() {
        return asadminHosts;
    }
    
    public List<String> getGuiHosts() {
        return guiHosts;
    }
    
    public String getAsadminContextRoot() {
        return asadminContextRoot;
    }
    
    public String getGuiContextRoot() {
        return guiContextRoot;
    }
    private void setValues() {
        asadminContextRoot = AdminAdapter.PREFIX_URI;  //can't change
        //asadminHosts       = Collections.emptyList();  //asadmin is handled completely by the adapter, no VS needed
        NetworkConfig config = cfg.getNetworkConfig();
        if (config == null)
            throw new IllegalStateException("Can't operate without <http-service>");
        List<NetworkListener> lss = config.getNetworkListeners().getNetworkListener();
        if (lss == null || lss.isEmpty())
            throw new IllegalStateException("Can't operate without at least one <network-listener>");
        boolean dedicatedAdmin = false;
        for (NetworkListener ls : lss) {
            if(ADMIN_LISTENER_ID.equals(ls.getName())) {
                guiContextRoot = "";  //at the root context for separate admin-listener
                String dvs     = ls.findHttpProtocol().getHttp().getDefaultVirtualServer();
                guiHosts       = Collections.unmodifiableList(Arrays.asList(dvs));
                asadminHosts   = guiHosts;  //same for now
                try {
                    port = Integer.valueOf(ls.getPort());
                } catch(NumberFormatException ne) {
                    port = ADMIN_PORT;
                }
                dedicatedAdmin = true;
                break;
            }
        }
        if (dedicatedAdmin == false) {
            //pick first
            NetworkListener effective = lss.get(0);
            String dvs = effective.findHttpProtocol().getHttp().getDefaultVirtualServer();
            guiHosts = Collections.unmodifiableList(Arrays.asList(dvs));
            asadminHosts = guiHosts;
            try {
                port = Integer.valueOf(effective.getPort());
            } catch(NumberFormatException ne) {
                port = 8080;   // this is the last resort
            }
            //get the context root from admin-service
            AdminService as = cfg.getAdminService();
            if (as == null)
                guiContextRoot = ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT;
            else
                setGuiContextRootFromAdminService(as);
        }
    }
    
    private void setGuiContextRootFromAdminService(AdminService as) {
        for (Property p : as.getProperty()) {
            setGuiContextRoot(p);
        }
    }
    private void setGuiContextRoot(Property prop) {
	if (prop == null) {
	    guiContextRoot = ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT;
	    return;
	}
	if (ServerTags.ADMIN_CONSOLE_CONTEXT_ROOT.equals(prop.getName())) {
	    if (prop.getValue() != null && prop.getValue().startsWith("/")) {
		guiContextRoot = prop.getValue();
		log.info("Admin Console Adapter: context root: " + guiContextRoot);
	    } else {
		log.info("Invalid context root for the admin console application, using default:" + ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT);
		guiContextRoot = ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT;
	    }
	}
    }    
}
