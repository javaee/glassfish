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

import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.ee.admin.lbadmin.transform.Visitor;
import com.sun.enterprise.ee.admin.lbadmin.transform.LoadbalancerVisitor;

import com.sun.enterprise.ee.admin.lbadmin.reader.api.ClusterReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.PropertyReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.LoadbalancerReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.LbReaderException;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.LbConfigs;
import com.sun.enterprise.config.serverbeans.LbConfig;
import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.serverbeans.ServerRef;

import java.util.List;
import java.util.ArrayList;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.util.i18n.StringManager;
        
/**
 * Reader class to get information about load balancer configuration.
 *
 * @author Satish Viswanatham
 */
public class LoadbalancerReaderImpl implements LoadbalancerReader {

    //--- CTORS-------

    public LoadbalancerReaderImpl (ConfigContext ctx, LbConfig lbConfig) {
        if ( (lbConfig == null) || (ctx == null) ){
            String msg = _localStrMgr.getString("ConfigBeanAndNameNull");
            throw new IllegalArgumentException(msg);
        }
        _ctx = ctx;
        _lbConfig = lbConfig;
    }
    
    //--- READER IMPLEMENTATION -----

    /**
     * Returns properties of the load balancer.
     * For example response-timeout-in-seconds, reload-poll-interval-in-seconds
     * and https-routing etc.
     *
     * @return PropertyReader[]     array of properties
     */
    public PropertyReader[] getProperties() throws LbReaderException {

        // all the attributes as properties
        ElementProperty [] properties = _lbConfig.getElementProperty();
        // add default values for the props that are not defined
        PropertyReaderImpl activeHealth = null;
        PropertyReaderImpl numHealth = null;
        PropertyReaderImpl rewriteLoc = null;
        int propSize = properties.length;
        if ( _lbConfig.getElementPropertyByName(
            LoadbalancerReader.ACTIVE_HEALTH_CHECK ) == null) {
            activeHealth = new PropertyReaderImpl(_lbConfig,
            LoadbalancerReader.ACTIVE_HEALTH_CHECK,
            LoadbalancerReader.ACTIVE_HEALTH_CHECK_VALUE);
            propSize++;
        }

        if ( _lbConfig.getElementPropertyByName(
            LoadbalancerReader.NUM_HEALTH_CHECK ) == null) {
            numHealth = new PropertyReaderImpl(_lbConfig,
            LoadbalancerReader.NUM_HEALTH_CHECK,
            LoadbalancerReader.NUM_HEALTH_CHECK_VALUE);
            propSize++;
        }

        if ( _lbConfig.getElementPropertyByName(
            LoadbalancerReader.REWRITE_LOCATION ) == null) {
            rewriteLoc = new PropertyReaderImpl(_lbConfig,
            LoadbalancerReader.REWRITE_LOCATION,
            LoadbalancerReader.REWRITE_LOCATION_VALUE);
            propSize++;
        }

        int i=0;
        PropertyReaderImpl[]  props = new PropertyReaderImpl[propSize+4];


        props[i++] = new PropertyReaderImpl(_lbConfig, 
                        LoadbalancerReader.RESP_TIMEOUT);
        props[i++] = new PropertyReaderImpl(_lbConfig,
                        LoadbalancerReader.RELOAD_INTERVAL);
        props[i++] = new PropertyReaderImpl(_lbConfig,
                        LoadbalancerReader.HTTPS_ROUTING);
        props[i++] = new PropertyReaderImpl(_lbConfig,
                        LoadbalancerReader.REQ_MONITOR_DATA);
//        props[4] = new PropertyReaderImpl(_lbConfig,
//                        LoadbalancerReader.ROUTE_COOKIE);

        // XXX add all the properties
        for(ElementProperty prop : properties) {
            props[i++] = new PropertyReaderImpl(_lbConfig,
                    prop.getName());
        }
        // add default values for the props that are not defined
        if ( activeHealth != null) {
            props[i++] = activeHealth;
        }

        if ( numHealth != null) {
            props[i++] = numHealth;
        }

        if ( rewriteLoc != null) {
            props[i++] = rewriteLoc;
        }

        return props;
    }

    /**
     * Returns the cluster info that are load balanced by this LB.
     *
     * @return ClusterReader        array of cluster readers
     */
    public ClusterReader[] getClusters() throws LbReaderException {

        List list = new ArrayList();

        ClusterRef[] clusters = _lbConfig.getClusterRef();
        ServerRef[] servers = _lbConfig.getServerRef();

        if (((clusters == null) || (clusters.length == 0)) && 
                        ((servers == null) || (servers.length == 0))) {
                return null;
        }
       
        int total = clusters.length + servers.length;
        ClusterReader clImpls = null;

        for (int i=0; i < clusters.length; i++) {
            clImpls = new ClusterReaderImpl(_ctx,_lbConfig.getClusterRef(i));
            list.add(clImpls);
        }
        for (int j= clusters.length; j < total; j++) {
            clImpls = new StandAloneClusterReaderImpl(_ctx,
                    _lbConfig.getServerRef(j-clusters.length));
            list.add(clImpls);
        }
        ClusterReader[] cls = new ClusterReader[list.size()];
        return (ClusterReader[]) list.toArray(cls);
    }

    /**
     * Returns the name of the load balancer
     *
     * @return String               name of the LB
     */
    public String getName() throws LbReaderException{
        return _lbConfig.getName();
    }

    // --- VISITOR IMPLEMENTATION ---

    public void accept(Visitor v) {
    
        LoadbalancerVisitor cv = (LoadbalancerVisitor) v;
        cv.visit(this);
    }

    // --- PRIVATE VARS -----

    LbConfig _lbConfig = null;
    ConfigContext        _ctx = null;

    private static final StringManager _localStrMgr = 
               StringManager.getManager(LoadbalancerReaderImpl.class);

}
