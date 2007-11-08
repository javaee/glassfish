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

package com.sun.enterprise.ee.admin.lbadmin.writer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.LbConfig;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.appserv.management.config.LoadBalancerConfig;
import com.sun.enterprise.ee.admin.lbadmin.connection.ConnectionManager;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.LoadbalancerReader;
import com.sun.enterprise.ee.admin.lbadmin.transform.LoadbalancerVisitor;
import com.sun.enterprise.ee.admin.lbadmin.beans.Loadbalancer;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.security.SSLUtils;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.logging.ee.EELogDomains;

import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.apache.coyote.http11.Constants;



/**
 * Class to publish the loadbalancer.xml to the physical loadbalancer.
 * @author hr124446
 */
public class LbConfigPublisher {
    
    /** Creates a new instance of LbConfigPublisher */
    public LbConfigPublisher(ConfigContext ctx, String lbConfigName, String
    lbName) throws IOException {
    
        _name = lbConfigName;
        _lbName = lbName;
        _ctx = ctx;
        com.sun.enterprise.config.serverbeans.LoadBalancer lb = null;
        try{
            lb = ((Domain)_ctx.getRootConfigBean()).
                    getLoadBalancers().getLoadBalancerByName(lbName);
            ElementProperty host = lb.getElementPropertyByName(LoadBalancerConfig.DEVICE_HOST_PROPERTY);
            ElementProperty port = lb.getElementPropertyByName(LoadBalancerConfig.DEVICE_ADMIN_PORT_PROPERTY);
            ElementProperty proxyHost = lb.getElementPropertyByName(LoadBalancerConfig.SSL_PROXY_HOST_PROPERTY);
            ElementProperty proxyPort = lb.getElementPropertyByName(LoadBalancerConfig.SSL_PROXY_PORT_PROPERTY);
            ElementProperty isSecure = lb.getElementPropertyByName(LoadBalancerConfig.IS_SECURE_PROPERTY);
            String lbHost = host!=null?host.getValue():null;
            String lbPort = port!=null?port.getValue():null;
            String lbProxyHost = proxyHost!=null?proxyHost.getValue():null;
            String lbProxyPort = proxyPort!=null?proxyPort.getValue():null;
            boolean isSec = isSecure!=null? Boolean.getBoolean(isSecure.getValue()):true;
            _connectionManager = new
            ConnectionManager(lbHost,lbPort,lbProxyHost,lbProxyPort,lbName,isSec);
        } catch ( Exception e ){
            throw new IOException(e.getMessage());
        }
    }

    
    /**
     * publishes the loadbalancer.xml to the physical loadbalancer.
     * @throws java.io.IOException 
     * @throws com.sun.enterprise.config.ConfigException 
     * @throws org.netbeans.modules.schema2beans.Schema2BeansException 
     */
    public void publish() throws IOException, ConfigException, 
                Schema2BeansException{


        // check if the lb exists
        LoadbalancerReader lbr = LbConfigExporter.getLbReader(_ctx, _name);

        HttpURLConnection conn =
            _connectionManager.getConnection(LB_UPDATE_CONTEXT_ROOT);
        OutputStream out=null;
        try{
            conn.setDoOutput(true);
            conn.setRequestMethod(Constants.POST);
            conn.connect();
            out = conn.getOutputStream();
            LbConfigExporter.exportXml(lbr, out);
            out.flush();
        } catch (UnknownHostException uhe){
            throw new IOException(_strMgr.getString("CannotConnectToLBHost", uhe.getMessage()));
        }catch(Exception e){
            throw new IOException(e.getMessage());
        }finally {
            if (out != null && conn!=null) {
                int code = conn.getResponseCode();
                String response = conn.getResponseMessage();
                out.close();
                conn.disconnect();
                out = null;
                if(code != HttpURLConnection.HTTP_OK){
                    String url = conn.getURL().toString();
                    conn=null;
                    throw new IOException(_strMgr.getString("HttpError",new Integer(code),response,url));
                }
                conn = null;
            }
        }
    }
    

    public boolean ping() throws IOException{
        HttpURLConnection conn = _connectionManager.getConnection(LB_UPDATE_CONTEXT_ROOT);
        conn.setRequestMethod(Constants.GET);
        conn.connect();
        int code = conn.getResponseCode();
        if(code != HttpURLConnection.HTTP_OK)
            return false;
        return true;
    }


    private Loadbalancer _lb = null;
    private String _name = null;
    private String _lbName = null;
    private ConfigContext _ctx = null;
    private ConnectionManager _connectionManager = null;
    private static final StringManager _strMgr = 
        StringManager.getManager(LbConfigWriter.class);

    private static final String LB_UPDATE_CONTEXT_ROOT = "/lbconfigupdate";

    
    private static Logger _logger = Logger.getLogger(
			EELogDomains.EE_ADMIN_LOGGER);
}
