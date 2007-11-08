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
 * $Id: HttpListenerVirtualServerAssociationMgr.java,v 1.3 2005/12/25 03:42:22 tcfujii Exp $
 */

package com.sun.enterprise.admin.mbeans;

//jdk imports
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.sun.enterprise.util.i18n.StringManager;

//config imports
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

public class HttpListenerVirtualServerAssociationMgr
{
    /**
     * i18n strings manager object
     */
    private static final StringManager strMgr = 
        StringManager.getManager(HttpListenerVirtualServerAssociationMgr.class);

    private final Config config;

    public HttpListenerVirtualServerAssociationMgr(ConfigContext   cctx,
                                                   String          configRef) 
        throws ConfigException
    {
        checkArg(cctx, strMgr.getString("http_listener_vs_assoc_mgr.null_context"));
        checkArg(configRef, strMgr.getString("http_listener_vs_assoc_mgr.null_config_ref"));
        final Domain domain = (Domain)cctx.getRootConfigBean();
        config = domain.getConfigs().getConfigByName(configRef);
        checkArg(config, strMgr.getString("http_listener_vs_assoc_mgr.no_such_element", configRef));
    }

    public void addHttpListenerRef(String listenerId) throws ConfigException
    {
        final String defaultVS = getHttpListener(listenerId).getDefaultVirtualServer();
        final Set s = getHttpListeners(defaultVS);
        s.add(listenerId);
        setHttpListeners(defaultVS, s);
    }

    public void deleteHttpListenerRef(String listenerId) throws ConfigException
    {
        VirtualServer[] servers = config.getHttpService().getVirtualServer();
        if (servers != null)
        {
            for (int i = 0; i < servers.length; i++)
            {
                Set httpListeners = getHttpListeners(servers[i].getId());
                if (httpListeners.contains(listenerId))
                {
                    httpListeners.remove(listenerId);
                    servers[i].setHttpListeners(setToStr(httpListeners));
                }
            }
        }
    }

    public void changeHttpListenerRef(String    listenerId, 
                                      String    oldVs, 
                                      String    newVs) 
        throws ConfigException
    {
        Set s = getHttpListeners(oldVs);
        s.remove(listenerId);
        setHttpListeners(oldVs, s);

        s = getHttpListeners(newVs);
        s.add(listenerId);
        setHttpListeners(newVs, s);
    }

    Set getHttpListeners(String vsID) throws ConfigException
    {
        return strToSet(getVirtualServer(vsID).getHttpListeners());
    }

    void setHttpListeners(String vsID, Set httpListeners)
        throws ConfigException
    {
        getVirtualServer(vsID).setHttpListeners(setToStr(httpListeners));
    }

    protected HttpListener getHttpListener(String id) throws ConfigException
    {
        HttpListener listener = config.getHttpService().getHttpListenerById(id);
        checkArg(listener, strMgr.getString("http_listener_vs_assoc_mgr.no_such_element", id));
        return listener;
    }

    protected VirtualServer getVirtualServer(String id) throws ConfigException
    {
        VirtualServer vs = config.getHttpService().getVirtualServerById(id);
        checkArg(vs, strMgr.getString("http_listener_vs_assoc_mgr.no_such_element", id));
        return vs;
    }

    private Set strToSet(String httpListeners)
    {
        final Set s = new LinkedHashSet();
        if (null != httpListeners)
        {
            StringTokenizer strTok = new StringTokenizer(httpListeners, ",");
            while (strTok.hasMoreTokens())
            {
                s.add(strTok.nextToken());
            }
        }
        return s;
    }

    private String setToStr(Set httpListeners)
    {
        String s = null;
        final Iterator it = httpListeners.iterator();
        if (it.hasNext())
        {
            StringBuffer sb = new StringBuffer();
            while (it.hasNext())
            {
                sb.append((String)it.next());
                if (it.hasNext()) { sb.append(','); }
            }
            s = sb.toString();
        }
        return s;
    }

    private void checkArg(Object o, Object msg) throws ConfigException
    {
        if (null == o)
        {
            throw new ConfigException(msg.toString());
        }
    }
}
