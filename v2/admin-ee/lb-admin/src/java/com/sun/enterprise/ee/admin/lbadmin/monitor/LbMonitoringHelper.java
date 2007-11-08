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
 * LbConfigPublisher.java
 *
 * Created on July 26, 2005, 2:23 PM
 *
 */

package com.sun.enterprise.ee.admin.lbadmin.monitor;
import com.sun.enterprise.ee.admin.lbadmin.writer.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.appserv.management.config.LoadBalancerConfig;
import com.sun.enterprise.ee.admin.lbadmin.connection.ConnectionManager;
import com.sun.enterprise.ee.admin.lbadmin.beans.Loadbalancer;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.ee.EELogDomains;

import org.apache.coyote.http11.Constants;



/**
 * Class to publish the loadbalancer.xml to the physical loadbalancer.
 * @author hr124446
 */
public class LbMonitoringHelper {
    
    /** Creates a new instance of LbConfigPublisher */
    public LbMonitoringHelper(ConfigContext ctx, String lbConfigName, String lbName){
    
        _name = lbConfigName;
        _lbName = lbName;
        _ctx = ctx;
        com.sun.enterprise.config.serverbeans.LoadBalancer lb = null;
        try{
            lb = ((Domain)_ctx.getRootConfigBean()).
                    getLoadBalancers().getLoadBalancerByName(lbName);
            ElementProperty [] properties = lb.getElementProperty();
            ElementProperty host = null;
            ElementProperty port = null;
            ElementProperty proxyHost = null;
            ElementProperty proxyPort = null;
            ElementProperty secProp = null;
            for(ElementProperty prop : properties){
                if(prop.getName().equals(LoadBalancerConfig.SSL_PROXY_HOST_PROPERTY))
                    proxyHost = prop;
                else if(prop.getName().equals(LoadBalancerConfig.SSL_PROXY_PORT_PROPERTY))
                    proxyPort = prop;
                else if(prop.getName().equals(LoadBalancerConfig.DEVICE_HOST_PROPERTY))
                    host = prop;
                else if(prop.getName().equals(LoadBalancerConfig.DEVICE_ADMIN_PORT_PROPERTY))
                    port = prop;
                else
                if(prop.getName().equals(LoadBalancerConfig.IS_SECURE_PROPERTY)) 
                   secProp = prop; 
            }
            String lbHost = host!=null?host.getValue():null;
            String lbPort = port!=null?port.getValue():null;
            String lbProxyHost = proxyHost!=null?proxyHost.getValue():null;
            String lbProxyPort = proxyPort!=null?proxyPort.getValue():null;
            boolean isSec = secProp!=null? Boolean.getBoolean(secProp.getValue()):true;

            _connectionManager = new
            ConnectionManager(lbHost,lbPort,lbProxyHost,lbProxyPort,lbName,isSec);
        } catch ( Exception e ){
            e.printStackTrace();
        }
    }

    
    /**
     * publishes the loadbalancer.xml to the physical loadbalancer.
     * @throws java.io.IOException 
     * @throws com.sun.enterprise.config.ConfigException 
     * @throws org.netbeans.modules.schema2beans.Schema2BeansException 
     */
    public String getMonitoringXml() throws IOException{

        HttpURLConnection conn = _connectionManager.getConnection(LB_MONITORING_CONTEXT_ROOT);
        InputStream in=null;
        String xml = ""; String s = "";
        try{
            conn.setRequestMethod(Constants.GET);
            conn.connect();
            in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while ((s = reader.readLine()) != null) {
                xml = xml + s + "\n";
            }
        } finally {
            if (in != null && conn!=null) {
                int code = conn.getResponseCode();
                in.close();
                conn.disconnect();
                in = null;
                conn = null;
            }
        }
        return xml;
    }
    

    public boolean reset() throws IOException{
        HttpURLConnection conn = _connectionManager.getConnection(LB_MONITORING_CONTEXT_ROOT+"?reset=true");
        conn.setRequestMethod(Constants.GET);
        conn.connect();
        int code = conn.getResponseCode();
        return true;
    }


    private ConfigContext _ctx = null;
    private String _name = null;
    private String _lbName = null;
    private ConnectionManager _connectionManager = null;
    private static final StringManager _strMgr = 
        StringManager.getManager(LbConfigWriter.class);

    private static final String LB_MONITORING_CONTEXT_ROOT = "/lbgetmonitordata";

    
    private static Logger _logger = Logger.getLogger(
			EELogDomains.EE_ADMIN_LOGGER);
}
