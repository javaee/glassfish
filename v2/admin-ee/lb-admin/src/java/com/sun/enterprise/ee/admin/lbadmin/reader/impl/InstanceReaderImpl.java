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
package com.sun.enterprise.ee.admin.lbadmin.reader.impl;

import com.sun.enterprise.ee.admin.lbadmin.transform.Visitor;
import com.sun.enterprise.ee.admin.lbadmin.transform.InstanceVisitor;

import com.sun.enterprise.ee.admin.lbadmin.reader.api.InstanceReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.LbReaderException;
import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.PropertyResolver;

import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.net.InetAddress;
import java.net.UnknownHostException;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.admin.util.JMXConnectorConfig;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Provides instance information relavant to Load balancer tier.
 *
 * @author Satish Viswanatham
 */
public class InstanceReaderImpl implements InstanceReader {

    /**
     * Constructor
     */
    public InstanceReaderImpl(ConfigContext ctx, ClusterRef cRef, Server s) {
        if ( (ctx == null) || (cRef == null) || (s==null)){
            String msg = _localStrMgr.getString("ConfigBeanAndNameNull");
            throw new IllegalArgumentException(msg);
        }
        _ctx = ctx;
        _s = s;
        Cluster c = null;
        try {
           c = ClusterHelper.getClusterByName(_ctx, cRef.getRef());
        } catch(ConfigException ce) {
            String msg = _localStrMgr.getString("InstanceNotFound", s.getName(),
                                        cRef.getRef());
            throw new IllegalArgumentException(msg);
        }
        _sRef = c.getServerRefByRef(s.getName());

        if (_sRef == null) {
            String msg = _localStrMgr.getString("ServerRefNotFound",s.getName(),                             cRef.getRef());
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Constructor
     */
    public InstanceReaderImpl(ConfigContext ctx, ServerRef sRef) {
        if ( (ctx == null) || (sRef == null)){
            String msg = _localStrMgr.getString("ConfigBeanAndNameNull");
            throw new IllegalArgumentException(msg);
        }
        _ctx = ctx;
        _sRef = sRef;
        try {
            _s = ServerHelper.getServerByName(_ctx,sRef.getRef());
        } catch(ConfigException ce) {
            String msg = _localStrMgr.getString("InstanceNotFound", sRef.getRef(),
                                        "");
            throw new IllegalArgumentException(msg);
        }
        if (_s == null) {
            String msg = _localStrMgr.getString("ServerRefNotFound",sRef.getRef(),
                    "");
            throw new IllegalArgumentException(msg);
        }
   }

    /**
     * Return server instance's name.
     *
     * @return String           instance' name
     */
    public String getName() throws LbReaderException {
        return _sRef.getRef();
    }

    /**
     * Returns if the server is enabled in the load balancer or not.
     *
     * @return boolean          true if enabled in LB; false if disabled
     */
    public boolean getLbEnabled() throws LbReaderException {
        return _sRef.isLbEnabled();
    }

    /**
     * This is used in quicescing. Timeouts after this interval and disables the
     * instance in the load balancer. 
     *
     * @return String           Disable time out in minutes
     */
    public String getDisableTimeoutInMinutes() throws LbReaderException {
        return _sRef.getDisableTimeoutInMinutes();
    }

    /**
     * This is used in weighted round robin. returns the weight of the instance
     *
     * @return String           Weight of the instance
     */
    public String getWeight() throws LbReaderException {
        return _s.getLbWeight();
    }

    /**
     * Enlists both http and https listeners of this server instance
     * It will be form "http:<hostname>:<port> https:<hostname>:<port>"
     *
     * @return String   Listener(s) info.
     */
    public String getListeners() throws LbReaderException {
        String listenerStr = "";

        String sName = _sRef.getRef();
        Config c = null;
        try {
             c = ServerHelper.getConfigForServer(_ctx, sName);
        } catch (ConfigException ce) {
            String msg = _localStrMgr.getString("ConfigNotFound", sName);
            throw new LbReaderException(msg,ce);
        }

        HttpService httpSvc = c.getHttpService();
        HttpListener[] lstnrs = httpSvc.getHttpListener();
        for (int i=0; i < lstnrs.length; i++) {
           if (i != 0 ) {
                listenerStr += " "; // space between listener names
           }
           
           if (lstnrs[i].isSecurityEnabled()) {
                listenerStr += HTTPS_PROTO;
           }else {
                listenerStr += HTTP_PROTO; 
           }

          String hostName = getHostNameForServerInstance(_ctx,
                                            sName);
          listenerStr +=  hostName+ ":"; // XXX actual hostname
          // resolve the port name
          String port = lstnrs[i].getPort();

          // If it is system variable, resolve it
          if ( (port != null) && (port.length() > 1) && (port.charAt(0) == '$')
          &&(port.charAt(1) == '{') &&(port.charAt(port.length()-1) == '}') ) {
            
                String portVar = port.substring(2, port.length()-1);
                String sVar = null;
                PropertyResolver propResolver= null;
                try {
                    propResolver = new PropertyResolver(_ctx,sName);
                } catch(ConfigException ce) {
                    // ignore this exception
                }

                if (propResolver != null) {
                    sVar = propResolver.getPropertyValue(portVar);
                }
                if (sVar != null) {
                    listenerStr += sVar;
                } else {
                    listenerStr += port;
                }
          } else {
            listenerStr += port;
          }

        }
        return listenerStr;
    }

    // --- VISITOR IMPLEMENTATION ---

    public void accept(Visitor v) {

        InstanceVisitor pv = (InstanceVisitor) v;
        pv.visit(this);
    }

    private String getHostNameForServerInstance(ConfigContext ctx,
                                String serverName) throws LbReaderException
    {
        try {
            JMXConnectorConfig info =
                ServerHelper.getJMXConnectorInfo(ctx, serverName);
            String host = info.getHost();
            return host;
        } catch (Exception e){
            String msg = _localStrMgr.getString("GetHostNameFailed");
            throw new LbReaderException(msg,e);
        }
    }


    // --- PRIVATE VARS -------
    ConfigContext _ctx = null;
    ServerRef _sRef = null;
    Server _s = null;
    private static final StringManager _localStrMgr = 
               StringManager.getManager(InstanceReaderImpl.class);
    final private String HTTP_PROTO = "http://";
    final private String HTTPS_PROTO = "https://";
}
