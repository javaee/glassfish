/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2009 Sun Microsystems, Inc. All rights reserved.
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
