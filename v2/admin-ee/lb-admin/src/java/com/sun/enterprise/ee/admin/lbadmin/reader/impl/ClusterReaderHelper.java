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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.serverbeans.HealthChecker;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.admin.util.IAdminConstants;

import com.sun.enterprise.ee.admin.lbadmin.reader.api.WebModuleReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.HealthCheckerReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.LocationHelper;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.LbReaderException;

import com.sun.enterprise.tools.common.dd.webapp.SunWebApp;
import com.sun.enterprise.tools.common.dd.ejb.SunEjbJar;
import com.sun.enterprise.tools.common.dd.ejb.Ejb;
import com.sun.enterprise.tools.common.dd.ejb.EnterpriseBeans;
import com.sun.enterprise.tools.common.dd.WebserviceEndpoint;

/**
 * Impl class for ClusterReader. This provides loadbalancer 
 * data for a cluster.
 *
 * @author Satish Viswanatham
 * @since  JDK1.4
 */
public class ClusterReaderHelper {

    /**
     * Returns the web module readers for a set of application refs.
     *
     * @param   _configCtx      Current Config context
     * @param   refs            Application ref(s) from cluster or stand alone
     *                          instance
     * @param   target          Name of the cluster or stand alone instance
     *
     * @return  WebModuleReader[]   Array of the corresponding web module 
     *                              reader(s).
     *
     * @throws  LbReaderException   In case of any error(s).
     */
    public static WebModuleReader[] getWebModules(ConfigContext _configCtx,
            ApplicationRef[] refs, String target) throws LbReaderException {
    
        List list = new ArrayList();
        
        for (int i=0; i<refs.length; i++) {
            String name = refs[i].getRef();
            ConfigBean bean = null;
            try {
                bean = ApplicationHelper.findApplication(_configCtx, name);
            } catch (ConfigException ce) {
                String msg = _strMgr.getString("ErrorFindingApplication", 
                                                name, target);
                throw new LbReaderException(msg, ce);
            }
            LocationHelper lhelper = new LocationHelperImpl(_configCtx);
            if (bean instanceof WebModule) {

                WebModule wMod = (WebModule) bean;
                if ( ( wMod != null) &&
                    ( wMod.getObjectType().equals( IAdminConstants.USER))) {
                    
                    String l =lhelper.getSunWebXmlPathForModule(wMod.getName());
                    WebModuleReader wr = new WebModuleReaderImpl(_configCtx, 
                        refs[i], bean, createSunWebApp(l));
                    list.add(wr);
                }
            } else if (bean instanceof J2eeApplication) {
                J2eeApplication app = (J2eeApplication) bean;
                if ( (app != null) && 
                    (app.getObjectType().equals( IAdminConstants.USER))) {
                    
                    // get all the web modules inside application
                    List l = lhelper.getSunWebXmlPathForApplication(
                                app.getName());
                    int size = l.size();
                    for (int j=0; j<size; j++) {
                        WebModuleReader wr = new WebModuleReaderImpl(_configCtx,
                            refs[i], null, 
                            createSunWebApp((String)l.get(j)));
                        list.add(wr);
                    }
                    List l1 = lhelper.getSunEjbJarXmlPathForApplication(
                                app.getName());
                    int size1 = l1.size();
                    for (int j=0; j<size1; j++) {
                        List<WebserviceEndpoint> wsepList = createWebserviceEndpoint((String)l1.get(j));
                        for(WebserviceEndpoint wsep: wsepList){
                            WebModuleReader wr = new EjbJarModuleReaderImpl(_configCtx,
                                refs[i], wsep );
                            list.add(wr);
                        }
                    }
                }
            }
        }

        // returns the web module reader as array
        WebModuleReader[] webModules = new WebModuleReader[list.size()];
        return (WebModuleReader[]) list.toArray(webModules);
    }


    /**
     * Create SunWebApp bean from the file.
     *
     * @param   String      location of sun-web.xml. (full path, including
     *                      directory and file name).
     *
     * @returns SunWebApp   bean representation.
     *
     * @throws  LbReaderException   In case of any error(s).
     */
    public static SunWebApp createSunWebApp(String l) 
        throws LbReaderException {

        FileInputStream in = null;
        try {
            in = new FileInputStream(new File(l));
        } catch (FileNotFoundException fne) {
            _logger.log(Level.WARNING, 
            _sMgr.getString("http_lb_admin.sunweb.xml.not.found", l));  
        }

        SunWebApp sw = null; 
        try {
            sw = SunWebApp.createGraph(in); 
        } catch( Exception e) {
            _logger.log(Level.WARNING, 
            _sMgr.getString("http_lb_admin.sunweb.bean.create.failed", e));  
        }

        return sw;
    }

    public static List<WebserviceEndpoint> createWebserviceEndpoint(String l) 
        throws LbReaderException {

        FileInputStream in = null;
        try {
            in = new FileInputStream(new File(l));
        } catch (FileNotFoundException fne) {
            _logger.log(Level.WARNING, 
            _sMgr.getString("http_lb_admin.sunejbjar.xml.not.found", l));  
        }

        SunEjbJar se = null;
        List<WebserviceEndpoint> list = new ArrayList<WebserviceEndpoint>();
        try {
            se = SunEjbJar.createGraph(in); 
            EnterpriseBeans beans = se.getEnterpriseBeans();
            Ejb [] ejbs = beans.getEjb();
            for(Ejb ejb : ejbs){
                WebserviceEndpoint [] wseps = ejb.getWebserviceEndpoint();
                list.addAll(Arrays.asList(wseps));
            }
        } catch( Exception e) {
            _logger.log(Level.WARNING, 
            _sMgr.getString("http_lb_admin.ejbjar.bean.create.failed", e));  
        }

        return list;
    }
    
    private static final StringManager _strMgr = 
               StringManager.getManager(ClusterReaderHelper.class);

    private static Logger _logger = Logger.getLogger(
			EELogDomains.EE_ADMIN_LOGGER);

    private static final StringManagerBase _sMgr =
       StringManagerBase.getStringManager(_logger.getResourceBundleName());
  
}
