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
 * StartupReporter.java
 *
 * Created on March 7, 2006, 8:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.cli.commands;

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.util.ArrayList;
import java.util.List;
import com.sun.enterprise.util.i18n.StringManager;
/**
 *
 * @author kedarm
 */
public class DomainReporter {
    
    private final boolean                   detailed;
    private final DomainConfig              dc;
    private static final CLILogger          logger = CLILogger.getInstance();
    private static final StringManager      lsm = StringManager.getManager(DomainReporter.class);
    private final List<String>              records = new ArrayList<String>();
    private ConfigContext                   cc = null; //the config context
    private String                          sn = null; // the server name
    /* package private constructor */
    DomainReporter(final DomainConfig dc, final boolean terse, 
        final ConfigContext cc) {
        if (dc == null || cc == null) {
            throw new IllegalArgumentException ("Null Argument");
        }
        this.dc       = dc;
        this.detailed = !terse;
        this.cc       = cc;
    }
    
    void report() throws ConfigException {
        prepare();
        display();
    }
    
    private void prepare() throws ConfigException {
        final PEFileLayout layout = new PEFileLayout(dc);
        final String xmlPath      = layout.getDomainConfigFile().getAbsolutePath();
        this.sn                   = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;
        
        final String ver = Version.getFullVersion();
        records.add(getDomainBanner(dc.getDomainName(), ver, dc.getDomainRoot()));
        if (detailed) {
            records.add(getAdminConsoleMessage());
            records.add(getHttpUrlsMessage());
            records.add(getWebContextsMessage());
            records.add(getJmxServiceUrlMessage());
            records.add(getOccupiedPorts());
            records.add(getClusterSupport());
            records.add(getDebugMessage());
        }
    }
    
    private String getDomainBanner(final String dn, final String version, final String dr) {
        return ( lsm.getString("domain.report.banner", dn, version, dr));
    }
    
    private String getDebugMessage() throws ConfigException {
        String msg = "";
        boolean debugMode = false;
        try{
            debugMode = (Boolean)dc.get(DomainConfig.K_DEBUG);
        } catch(final Exception e) {
            // squelch, it's OK if we couldn't get the debug flag
            e.hashCode();   // silence FindBugs
        }
        if (debugMode) {
            final JavaConfig jc = ServerHelper.getConfigForServer(cc, sn).getJavaConfig();
            final String dopt = jc.getDebugOptions();
            msg = lsm.getString("start.debug.msg", dopt);
        }
        return ( msg );
    }
    private String getAdminConsoleMessage() throws ConfigException {
        final HttpListener admin = ServerHelper.getHttpListener(cc, sn, ServerHelper.ADMIN_HTTP_LISTNER_ID);
        String url = "", port = null;
        if (admin != null) {
            url = ServerHelper.getUrlString(admin);
            port = admin.getPort();
        }
        return ( lsm.getString("admin.console.msg", url, port) );
    }
    private String getHttpUrlsMessage() throws ConfigException {
        final HttpListener[] lss = ServerHelper.getHttpListeners(cc, sn);
        String urls = "";
        if (lss != null) {
            for (final HttpListener ls : lss) {
                if (ServerHelper.ADMIN_HTTP_LISTNER_ID.equals(ls.getId()))
                    continue;  //skip the admin listener
                if (ls.isEnabled()) {
                    urls += ServerHelper.getUrlString(ls);
                    urls += " ";
                }
            }
        }
        return ( lsm.getString("http.listeners.msg", urls) );
    }
    private String getWebContextsMessage() throws ConfigException {
        final WebModule[] wms = ServerHelper.getAssociatedWebModules(cc, sn);
        String crs = "";
        for (final WebModule wm : wms) {
           crs += wm.getContextRoot();
           crs += " ";
        }
        return ( lsm.getString("web.contexts.msg", crs) );
    }
    
    private String getJmxServiceUrlMessage() throws ConfigException {
        String url = "";
        final JmxConnector sjc = ServerHelper.getServerSystemConnector(cc, sn);
        if (sjc != null) {
            url += ServerHelper.getJmxServiceUrl(sjc);
        }
        return ( lsm.getString("jmx.connector.msg", url) );
    }
    private String getOccupiedPorts() throws ConfigException {
        String ports = "";
        ports = getHttpPorts() + getIiopPorts() + getJmxConnectorPorts();
        return ( lsm.getString("ports.msg", ports) );
    }
    private String getClusterSupport() throws ConfigException {
        final boolean sc = ServerHelper.isClusterAdminSupported(cc);
        if (sc)
            return (lsm.getString("clusters.supported.msg"));
        return (lsm.getString("clusters.not.supported.msg"));
    }
    private String getHttpPorts() throws ConfigException {
        String hp = "";
        final HttpListener[] hss = ServerHelper.getConfigForServer(cc, sn).getHttpService().getHttpListener();
        for (final HttpListener hs : hss) {
            if (hs.isEnabled()) {
                hp += hs.getPort();
                hp += " ";
            }
        }
        return ( hp );
    }
    private String getIiopPorts() throws ConfigException {
        String ip = "";
        final IiopListener[] ils = ServerHelper.getConfigForServer(cc, sn).getIiopService().getIiopListener();
        for (final IiopListener is : ils) {
            if (is.isEnabled()) {
                ip += is.getPort();
                ip += " ";
            }
        }
        return ( ip );
    }
    private String getJmxConnectorPorts() throws ConfigException {
        String jp = "";
        final JmxConnector[] jcs = ServerHelper.getConfigForServer(cc, sn).getAdminService().getJmxConnector();
        for (final JmxConnector jc : jcs) {
            if (jc.isEnabled()) {
                jp += jc.getPort();
                jp += " ";
            }
        }
        return ( jp );
    }
    
    private void display() {
        for (final String record : records) {
            logger.printMessage(record);
        }
    }
}
