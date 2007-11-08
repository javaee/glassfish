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
package com.sun.enterprise.ee.synchronization.impl;

import java.util.List;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.AppclientModule;
import com.sun.enterprise.config.serverbeans.ConnectorModule;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.LifecycleModule;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.admin.server.core.AdminService;

import com.sun.enterprise.server.ApplicationServer;

import com.sun.enterprise.ee.synchronization.JmxGroupRequestMediator;
import com.sun.enterprise.ee.synchronization.http.HttpGroupRequestMediator;
import com.sun.enterprise.ee.synchronization.http.HttpUtils;
import com.sun.enterprise.ee.synchronization.ApplicationSynchRequest;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.ApplicationRequestBuilder;
import com.sun.enterprise.ee.synchronization.AppclientModuleRequestBuilder;
import com.sun.enterprise.ee.synchronization.ConnectorModuleRequestBuilder;
import com.sun.enterprise.ee.synchronization.EjbModuleRequestBuilder;
import com.sun.enterprise.ee.synchronization.LifecycleModuleRequestBuilder;
import com.sun.enterprise.ee.synchronization.WebModuleRequestBuilder;
import com.sun.enterprise.ee.synchronization.tx.Transaction;
import com.sun.enterprise.ee.synchronization.tx.TransactionManager;

import com.sun.enterprise.ee.synchronization.api.ApplicationsMgr;

import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.admin.servermgmt.InstanceConfig;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.ee.synchronization.SynchronizationException;

/**
 * API for Application/Module bits synchronization. The APIs can be called
 * in a server instance to download bits from central repository.
 *
 * @author Satish Viswanatham
 * @since  JDK1.4
 */
public class ApplicationsMgrImpl implements ApplicationsMgr {

    /**
     * Default Constructor. 
     *
     * <p>WARNING: Users of this method must set the config context.
     *
     * @see setConfigContext
     */
    public ApplicationsMgrImpl() {
        if ( AdminService.getAdminService().isDas() ) {
            String msg = _localStrMgr.getString("DasNotSupported");
            throw new RuntimeException(msg);
        }
        _serverName = ApplicationServer.getServerContext().getInstanceName();
        if (_serverName == null ) {
            throw new RuntimeException(_localStrMgr.getString(
                "ServerNameConfigError"));
        }
    }

    /**
     * Sets the config context.
     *
     * @param  ctx  config context of the event
     */
    public void setConfigContext(ConfigContext ctx) {
        _ctx = ctx;
    }

    /**
     * Constructor.
     *
     * @param  ctx  config context of the event
     * @param  sName  server name
     */
    public ApplicationsMgrImpl(ConfigContext ctx) {
        if ( AdminService.getAdminService().isDas() ) {
            String msg = _localStrMgr.getString("DasNotSupported");
            throw new RuntimeException(msg);
        }
        _ctx = ctx;
        _serverName = ApplicationServer.getServerContext().getInstanceName();

        if (_serverName == null ) {
            throw new RuntimeException(_localStrMgr.getString(
                "ServerNameConfigError"));
        }
    }

    // ---- START OF ApplicationsMgr Interface ---------------------------

    public void synchronize(String name) throws SynchronizationException {

        ConfigBean bean = null;
        try {
            bean = ApplicationHelper.findApplication(_ctx, name);
        } catch (ConfigException ce) {
            String msg=_localStrMgr.getString("UnknownApplicationId", name);
            throw new SynchronizationException(msg, ce);
        }

        // delegate to the correct method
        if (bean instanceof J2eeApplication) {
            synchronizeJ2EEApplication(name);
        } else if (bean instanceof AppclientModule) {
            synchronizeAppclientModule(name);
        } else if (bean instanceof EjbModule) {
            synchronizeEJBModule(name);
        } else if (bean instanceof LifecycleModule) {
            synchronizeLifecycleModule(name);
        } else if (bean instanceof WebModule) {
            synchronizeWebModule(name); 
        } else if (bean instanceof ConnectorModule) {
            synchronizeConnectorModule(name); 
        } else {
            // unknown bean type
            String msg=_localStrMgr.getString("UnknownApplicationType", name);
            throw new SynchronizationException(msg);
        }
    }


    public void synchronizeJ2EEApplication(String appName) 
            throws SynchronizationException {

        long b = System.currentTimeMillis();

        _logger.log(Level.FINE, "synchronization.start.download.bits",appName);

        J2eeApplication app = null;
        try {
            app = ((Domain)_ctx.getRootConfigBean()).
                                    getApplications().
                                    getJ2eeApplicationByName(appName); 
        } catch (ConfigException ce) {
            String msg=_localStrMgr.getString("UnknownApplicationId", appName);
            throw new SynchronizationException(msg, ce);
        }

        ApplicationRequestBuilder appReqBuilder = 
             new ApplicationRequestBuilder(_ctx,_serverName);

        ApplicationSynchRequest appSyncReq = appReqBuilder.build(app);
        getBits(appSyncReq);
        
        _logger.log(Level.INFO,_strMgr.getString(
            "synchronization.end.download.bits", appName, 
            new Long(System.currentTimeMillis()-b).toString()));
    }

    public void synchronizeAppclientModule(String appName) 
            throws SynchronizationException {

        long b = System.currentTimeMillis();

        _logger.log(Level.FINE, "synchronization.start.download.bits", appName);

        AppclientModule app = null;
        try {
            app = ((Domain)_ctx.getRootConfigBean()).
                                    getApplications().
                                    getAppclientModuleByName(appName); 
        } catch (ConfigException ce) {
            String msg=_localStrMgr.getString("UnknownApplicationId", appName);
            throw new SynchronizationException(msg, ce);
        }

        AppclientModuleRequestBuilder appReqBuilder = 
             new AppclientModuleRequestBuilder(_ctx,_serverName);

        ApplicationSynchRequest appSyncReq = appReqBuilder.build(app);
        getBits(appSyncReq);

        _logger.log(Level.INFO,_strMgr.getString(
            "synchronization.end.download.bits", appName, 
            new Long(System.currentTimeMillis()-b).toString()));
    }

    public void synchronizeEJBModule(String appName) 
            throws SynchronizationException {

        long b = System.currentTimeMillis();

        _logger.log(Level.FINE,
                    "synchronization.start.download.bits", appName);

        EjbModule app = null;
        try {
            app = ((Domain)_ctx.getRootConfigBean()).
                                    getApplications().
                                    getEjbModuleByName(appName); 
        } catch (ConfigException ce) {
            String msg=_localStrMgr.getString("UnknownApplicationId", appName);
            throw new SynchronizationException(msg, ce);
        }

        EjbModuleRequestBuilder appReqBuilder = 
             new EjbModuleRequestBuilder(_ctx,_serverName);

        ApplicationSynchRequest appSyncReq = appReqBuilder.build(app);
        getBits(appSyncReq);
        
        _logger.log(Level.INFO,_strMgr.getString(
            "synchronization.end.download.bits", appName, 
            new Long(System.currentTimeMillis()-b).toString()));
    }

    public void synchronizeLifecycleModule(String appName) 
            throws SynchronizationException {

        long b = System.currentTimeMillis();

        _logger.log(Level.FINE, "synchronization.start.download.bits", appName);

        LifecycleModule app = null;
        try {
            app = ((Domain)_ctx.getRootConfigBean()).
                                    getApplications().
                                    getLifecycleModuleByName(appName); 
        } catch (ConfigException ce) {
            String msg=_localStrMgr.getString("UnknownApplicationId", appName);
            throw new SynchronizationException(msg, ce);
        }

        LifecycleModuleRequestBuilder appReqBuilder = 
             new LifecycleModuleRequestBuilder(_ctx,_serverName);

        ApplicationSynchRequest appSyncReq = appReqBuilder.build(app);
        getBits(appSyncReq);

        _logger.log(Level.INFO,_strMgr.getString(
            "synchronization.end.download.bits", appName, 
            new Long(System.currentTimeMillis()-b).toString()));
    }

    public void synchronizeWebModule(String appName) 
            throws SynchronizationException {

        long b = System.currentTimeMillis();

        _logger.log(Level.FINE, "synchronization.start.download.bits", appName);

        WebModule app = null;
        try {
            app = ((Domain)_ctx.getRootConfigBean()).
                                    getApplications().
                                    getWebModuleByName(appName); 
        } catch (ConfigException ce) {
            String msg=_localStrMgr.getString("UnknownApplicationId", appName);
            throw new SynchronizationException(msg, ce);
        }

        WebModuleRequestBuilder appReqBuilder = 
             new WebModuleRequestBuilder(_ctx,_serverName);

        ApplicationSynchRequest appSyncReq = appReqBuilder.build(app);
        getBits(appSyncReq);
        
        _logger.log(Level.INFO,_strMgr.getString(
            "synchronization.end.download.bits", appName, 
            new Long(System.currentTimeMillis()-b).toString()));
    }

    public void synchronizeConnectorModule(String appName) 
            throws SynchronizationException {

        long b = System.currentTimeMillis();

        _logger.log(Level.FINE, "synchronization.start.download.bits", appName);

        ConnectorModule app = null;
        try {
            app = ((Domain)_ctx.getRootConfigBean()).
                                    getApplications().
                                    getConnectorModuleByName(appName); 
        } catch (ConfigException ce) {
            String msg=_localStrMgr.getString("UnknownApplicationId", appName);
            throw new SynchronizationException(msg, ce);
        }

        ConnectorModuleRequestBuilder appReqBuilder = 
             new ConnectorModuleRequestBuilder(_ctx,_serverName);

        ApplicationSynchRequest appSyncReq = appReqBuilder.build(app);
        getBits(appSyncReq);
        
        _logger.log(Level.INFO,_strMgr.getString(
            "synchronization.end.download.bits", appName, 
            new Long(System.currentTimeMillis()-b).toString()));
    }

    // ---- END OF ApplicationsMgr Interface ---------------------------

    // ---- END OF ApplicationsMgr Interface ---------------------------

    private void getBits(ApplicationSynchRequest appSyncReq) 
            throws SynchronizationException {

        DASPropertyReader dpr = new DASPropertyReader(new InstanceConfig());
        List l = appSyncReq.toSynchronizationRequest();
        SynchronizationRequest[] allReqs = new SynchronizationRequest[
                                                l.size()];
        allReqs = (SynchronizationRequest[])l.toArray(allReqs);

        try {
            // reads DAS system JMX connector information
            dpr.read();
        } catch (IOException ioe) {
            String msg=_localStrMgr.getString("DASPropertyReadError");
            throw new SynchronizationException(msg, ioe);
        }

	    TransactionManager txMgr=TransactionManager.getTransactionManager();

        // begin a transaction for the synchronization; it is a 
        // single threaded transaction
        Transaction tx = txMgr.begin(1);

        boolean httpException             = false;
        HttpGroupRequestMediator httpGrm  = null;
        try {
            // execute synchronization using http
            String url = HttpUtils.getSynchronizationURL(_ctx, dpr);
            httpGrm = new HttpGroupRequestMediator(dpr, allReqs, tx, url);
            httpGrm.run();
        } catch (Exception e) {
            httpException = true;
        }

        // if web path based synchronization failed
        if (httpGrm.isException() || httpException) {

            _logger.log(Level.FINE, 
                "Http based synchronization failed, trying jmx based impl..", 
                httpGrm.getException());

            // re-try the synchronization request using jmx
            JmxGroupRequestMediator jmxGrm = 
                    new JmxGroupRequestMediator(dpr, allReqs, tx);
            jmxGrm.run();

            if (jmxGrm.isException()) {
                // synchronization failed
                throw new SynchronizationException(jmxGrm.getException());
            }
        }
    }

    //-------- PRIVATE VARIABLES -----
    private ConfigContext _ctx = null;
    private String   _serverName = null;

    private static Logger _logger =
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);

    private static final StringManagerBase _strMgr =
        StringManagerBase.getStringManager(_logger.getResourceBundleName());

    private static final StringManager _localStrMgr=StringManager.getManager
            (ApplicationsMgrImpl.class);
}
