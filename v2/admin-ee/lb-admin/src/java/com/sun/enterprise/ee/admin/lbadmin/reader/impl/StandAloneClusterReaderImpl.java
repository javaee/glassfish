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

import java.util.List;
import java.util.ArrayList;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.config.serverbeans.HealthChecker;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.admin.util.IAdminConstants;

import com.sun.enterprise.ee.admin.lbadmin.transform.Visitor;
import com.sun.enterprise.ee.admin.lbadmin.transform.ClusterVisitor;

import com.sun.enterprise.ee.admin.lbadmin.reader.api.ClusterReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.InstanceReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.WebModuleReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.HealthCheckerReader;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.LbReaderException;

/**
 * Impl class for ClusterReader. This provides loadbalancer 
 * data for a stand alone server (reverse proxy).
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class StandAloneClusterReaderImpl implements ClusterReader {

    public StandAloneClusterReaderImpl(ConfigContext ctx, ServerRef ref) 
            throws LbReaderException {

        if ( (ctx == null) || (ref == null) ) {
            String iMsg = _strMgr.getString("InvalidArgs");
            throw new RuntimeException(iMsg);
        }

        _configCtx   = ctx;
        _serverRef  = ref;

    /*
        try {
            if (!ServerHelper.isServerStandAlone(ctx, ref.getRef())) {
                String msg = _strMgr.getString("ServerNotFound", ref.getRef());
                throw new LbReaderException(msg);
            }
        } catch (ConfigException ce) {
            String msg = _strMgr.getString("ServerNotFound", ref.getRef());
            throw new LbReaderException(msg, ce);
        }
        */
    }

    public String getName() {
        return _serverRef.getRef();
    }

    public InstanceReader[] getInstances() throws LbReaderException {

        InstanceReader[] readers = new InstanceReader[1];

        readers[0] = new InstanceReaderImpl(_configCtx, _serverRef);
        return readers;
    }

    public HealthCheckerReader getHealthChecker() throws LbReaderException {

        HealthChecker bean = _serverRef.getHealthChecker();
        if (bean == null) {
            return null;
        } else {
            HealthCheckerReader reader = new HealthCheckerReaderImpl(bean);
            return reader;
        }
    }

    public WebModuleReader[] getWebModules() throws LbReaderException {


        ApplicationRef[] refs = null;
        try {
            refs = ServerHelper.getApplicationReferences(
                                        _configCtx, _serverRef.getRef()); 
        } catch (ConfigException ce) {
            String msg = _strMgr.getString("ErrorFindingClusteredApplications", 
                                        _serverRef.getRef());
            throw new LbReaderException(msg, ce);
        }

        return ClusterReaderHelper.getWebModules(_configCtx, refs,
                    _serverRef.getRef() );
    }

    public String getLbPolicy() {
        return null;
    }

    public String getLbPolicyModule() {
        return null;
    }

    public void accept(Visitor v) {
        ClusterVisitor cv = (ClusterVisitor)v;
        cv.visit(this);
    }

    // ---- VARIABLE(S) - PRIVATE --------------------------
    private ConfigContext _configCtx  = null;
    private ServerRef _serverRef    = null;
    private static final StringManager _strMgr = 
               StringManager.getManager(StandAloneClusterReaderImpl.class);

}
