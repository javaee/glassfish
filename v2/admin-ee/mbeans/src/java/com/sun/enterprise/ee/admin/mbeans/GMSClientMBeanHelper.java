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

package com.sun.enterprise.ee.admin.mbeans;

import com.sun.enterprise.admin.event.*;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.ee.cms.logging.GMSLogDomain;
import com.sun.enterprise.ee.cms.core.*;
import com.sun.enterprise.ee.cms.core.GroupManagementService;
import com.sun.enterprise.ee.cms.impl.client.*;
import com.sun.enterprise.ee.admin.proxy.InstanceProxy;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigUpdate;
import com.sun.enterprise.config.ConfigChange;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.util.i18n.StringManager;

import javax.management.MBeanException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

/**
 * Helper class for GMS Client MBean that is also listener for cluster creation
 * and deletion events in domain.
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Aug 18, 2005
 * @version $Revision: 1.3 $
 */
public class GMSClientMBeanHelper implements ClusterEventListener, CallBack
 {
    private Logger _logger;
    private int seqNum = 0;
    private static final	StringManager	_strMgr =
                StringManager.getManager(GMSClientMBean.class);
    private static final long JOINWAIT = 3000;
    private final Map < String, Map < String, List <Long> > >healthMap =
                new Hashtable < String, Map < String, List <Long> > >();
    private GMSClientMBean mbean;
    private static final String HEARTBEAT_ENABLED_KEY = "heartbeat-enabled";
    private static final String HEARTBEAT_ADDRESS_KEY = "heartbeat-address";
    private static final String HEARTBEAT_PORT_KEY = "heartbeat-port";

    public GMSClientMBeanHelper(final GMSClientMBean mbean){
        this.mbean = mbean;
    }

    void initGMSGroupForNamedCluster(final String clusterName ){
        try {
            if(GMSFactory.getGMSModule(clusterName) != null){
                return; //we are already a part of this group. No need to initialize
            }
        }
        catch ( GMSException e ) {
            final Cluster cluster;
            try {
                if(AdminService.getAdminService().isDas()){
                    final String dasInstanceName =
                            ApplicationServer.getServerContext().getInstanceName();
                    final ConfigContext configContext = getConfigContext();
                    cluster = getClusterObject( configContext, clusterName );
                    if(cluster != null && cluster.isHeartbeatEnabled()) {
                        getLogger().log(Level.INFO,
                           _strMgr.getString( "gms.initializing", clusterName));
                        GMSFactory.setGMSEnabledState( clusterName, Boolean.TRUE );
                        final Properties props  = getGMSConfigProps(cluster,
                                                              configContext);
                        final GroupManagementService gms =
                                (GroupManagementService) GMSFactory.startGMSModule(
                                    dasInstanceName,
                                    clusterName,
                                    //DAS is always a SPECTATOR member
                                    GroupManagementService.MemberType.SPECTATOR,
                                    props );
                        gms.addActionFactory(
                                new FailureNotificationActionFactoryImpl(this));
                        gms.addActionFactory(
                                new JoinNotificationActionFactoryImpl(this));
                        gms.addActionFactory(
                                new PlannedShutdownActionFactoryImpl(this));
                        gms.addActionFactory(
                                new FailureSuspectedActionFactoryImpl(this));

                        final String threadName ="GroupManagementService_"+
                                                dasInstanceName+'_'+clusterName;
                        gms.join();
                        initHealthMap(
                                    gms.getGroupHandle()
                                        .getCurrentCoreMembersWithStartTimes(),
                                      clusterName, cluster );
                    }
                }
            } catch (ConfigException ex) {
                getLogger().log(Level.WARNING, ex.getLocalizedMessage());
            } catch (GMSException e1) {
                getLogger().log(Level.SEVERE, "Exception occured while initializing Group Management Service."+
                e1);
                //TODO: consider throwing this exception up the stack.
            }
        }
    }

    private Cluster getClusterObject (
            final ConfigContext configContext, final String clusterName ) throws ConfigException {
        Cluster cluster;
        cluster = ClusterHelper
                        .getClusterByName( configContext,
                                            clusterName) ;
        return cluster;
    }

    private ConfigContext getConfigContext () {
        return AdminService.getAdminService()
                            .getAdminContext().getAdminConfigContext();
    }

    private void initHealthMap (
            final List<String> currentCoreMembersWithStartTimes,
            final String groupName,
            final Cluster cluster)
    {
        final Map<String,List<Long>> instanceMap =
                new Hashtable<String, List<Long> >();
        final List<Long> stateAndTime = new ArrayList<Long>();
        //if the instances in cluster are not yet started
        if(currentCoreMembersWithStartTimes.isEmpty()){
            final ServerRef[] servers = cluster.getServerRef();
            if(servers.length >0 ){// in there are instances ref'd in cluster
                for(ServerRef server : servers){
                //state set at 3 initially to indicate "instanceNotYetStarted"
                    stateAndTime.add( 0, 3L);
                    instanceMap.put( server.getRef(), stateAndTime);
                }
            }
        }
        else{
            for(String member : currentCoreMembersWithStartTimes){
                final String[] split = member.split("::");//split the name and timestamp
                stateAndTime.add(0, 0L);//this is the started state
                stateAndTime.add(1, new Long(split[1]));//this is the start timestamp
                instanceMap.put(split[0], stateAndTime );//place a holder for this instance
            }
        }
        synchronized( healthMap ){
            healthMap.put(groupName, instanceMap);
        }
    }

    private void refreshHealthMap ( final String clusterName ) {
        final Map<String, List<Long>> currHltMap  =
                                        new Hashtable<String, List<Long>>();
        synchronized(healthMap){
            currHltMap.putAll( healthMap.get(clusterName));
        }
        if(currHltMap.isEmpty()){
            try {
                initHealthMap( new ArrayList<String>(), clusterName,
                               getClusterObject(getConfigContext(), clusterName ));
            }
            catch ( ConfigException e ) {
                getLogger().log(Level.WARNING, e.getLocalizedMessage());
            }
        }
        else{
            final Set<String> currMembers = currHltMap.keySet();
            try {
                final ServerRef[] servers =
                        getClusterObject( getConfigContext(), clusterName)
                        .getServerRef();
                if(servers.length > currMembers.size()){
                    for(final ServerRef s : servers){
                        if(!currMembers.contains(s.getRef())){
                            updateHealthStatus( 3, clusterName,
                                                s.getRef(), -1 );
                        }
                    }
                }
                else if(servers.length < currMembers.size()) {
                    final List<String> serverNames =
                                                new ArrayList<String>();
                    for( final ServerRef s : servers ){
                        serverNames.add( s.getRef() );
                    }
                    for( final String s : currMembers ){
                        if( !serverNames.contains( s ) ){
                            updateHealthStatus( -1, clusterName, s, -1 );
                        }
                    }
                }
            }
            catch ( ConfigException e ) {
                getLogger().log(Level.WARNING, e.getLocalizedMessage());
            }
        }
    }

    void updateHealthStatus (final long state,
                             final String groupName,
                             final String memberToken,
                             final long timestamp)
    {
        synchronized( healthMap ){
            final Map<String, List<Long>> instanceMap = healthMap.get(groupName);
            if(state < 0 ){
                instanceMap.remove( memberToken );
            }
            else {
                final List<Long> status = new ArrayList<Long>();
                status.add(0, state);
                if(timestamp >= 0){
                    status.add(1, timestamp);
                }
                instanceMap.put(memberToken, status);
            }
            healthMap.put( groupName, instanceMap);
        }
    }


    private Logger getLogger(){
        if(_logger == null)
            _logger = GMSLogDomain.getLogger( GMSLogDomain.GMS_LOGGER);
        return _logger;
    }

    void leaveGMSGroupForNamedCluster( final String clusterName,
                              final GMSConstants. shutdownType shutdownType )
    {
        final GroupManagementService gms;
        try {
            gms = GMSFactory.getGMSModule(clusterName);
            if(gms != null){
                gms.shutdown(shutdownType);
                GMSFactory.setGMSEnabledState( clusterName,  Boolean.FALSE);
                GMSFactory.removeGMSModule( clusterName );
            }
            //gmsInstances.remove( clusterName );
        }
        catch ( GMSException e ) {
            getLogger().log(Level.WARNING, e.getLocalizedMessage());
        }
    }

    private Properties getGMSConfigProps ( final Cluster cluster,
                                           final ConfigContext configContext ) {
        final Properties props = new Properties();
        final Config config;
        try {
            final String configRef = cluster.getConfigRef();
            config = ConfigAPIHelper.getConfigByName( configContext,
                                                     configRef);
            final com.sun.enterprise.config.serverbeans.GroupManagementService
                    gmsConfig = config.getGroupManagementService();
            props.put(ServiceProviderConfigurationKeys.FAILURE_DETECTION_RETRIES.toString(),
                    gmsConfig.getFdProtocolMaxTries());
            props.put(ServiceProviderConfigurationKeys.FAILURE_DETECTION_TIMEOUT.toString(),
                      gmsConfig.getFdProtocolTimeoutInMillis());
            props.put(ServiceProviderConfigurationKeys.DISCOVERY_TIMEOUT.toString(),
                      gmsConfig.getPingProtocolTimeoutInMillis());
            props.put(ServiceProviderConfigurationKeys.FAILURE_VERIFICATION_TIMEOUT.toString(),
                      gmsConfig.getVsProtocolTimeoutInMillis());
            props.put(ServiceProviderConfigurationKeys.MULTICASTADDRESS.toString(),
                      cluster.getHeartbeatAddress());
            props.put(ServiceProviderConfigurationKeys.MULTICASTPORT.toString(),
                      cluster.getHeartbeatPort());
        }
        catch ( ConfigException e ) {
            getLogger().log(Level.WARNING, e.getLocalizedMessage());
        }
        return props;
    }

    Map<String, List<Long>> getClusterHealth ( final String clusterName ) {
        final Map<String, List<Long> > retval;

        if(!healthMap.containsKey( clusterName) ){
            throw new RuntimeException(_strMgr.getString( "gms.noSuchCluster",
                                                          clusterName));
        }
        else{
            refreshHealthMap(clusterName);
            retval =
                new Hashtable<String, List<Long> >(healthMap.get(clusterName));
        }
       return retval;
    }

    public synchronized void processNotification(final Signal signal) {
        seqNum = seqNum++;
        if(signal instanceof JoinNotificationSignal) {
            updateHealthStatus( 0, signal.getGroupName(),
                                signal.getMemberToken(),signal.getStartTime() );
            mbean.sendStartNotification(signal.getMemberToken(),
                                        signal.getStartTime(), seqNum  );
        }
        else if(signal instanceof PlannedShutdownSignal) {
            final long time = System.currentTimeMillis();
            updateHealthStatus( 1, signal.getGroupName(),
                                signal.getMemberToken(), time );
            mbean.sendStoppedNotification(signal.getMemberToken(),
                                          time, seqNum);
        }
        else if(signal instanceof FailureNotificationSignal) {
            final long time  = System.currentTimeMillis();
            updateHealthStatus( 2, signal.getGroupName(),
                                signal.getMemberToken(), time );
            mbean.sendFailureNotification(signal.getMemberToken(),
                                          time, seqNum);
        }
        else if( signal instanceof FailureSuspectedSignal){
            final long time = System.currentTimeMillis();
            updateHealthStatus(4, signal.getGroupName(),
                               signal.getMemberToken(), time);
            //TODO:if needed the mbean can now send notification of suspicion
        }
    }

    public void processEvent(final ElementChangeEvent event){
        getLogger().log(Level.INFO, "*** processEvent "+ event.toString());
    }

    public void handleCreate(final ClusterEvent event){
        final String clusterName = event.getElementId();
        initGMSGroupForNamedCluster( clusterName );
    }

    public void handleDelete(final ClusterEvent event){
        final String clusterName = event.getElementId();
        leaveGMSGroupForNamedCluster( clusterName,
                              GMSConstants.shutdownType.GROUP_SHUTDOWN );
    }

    public void handleUpdate(final ClusterEvent event){
        final String clusterName = event.getElementId();
        final ArrayList<ConfigChange> changeList =event.getConfigChangeList();
        for(final ConfigChange change : changeList){
            if(change instanceof ConfigUpdate){
                final Set<String> attrs = ((ConfigUpdate)change).getAttributeSet();
                for(final String attr : attrs){
                    if(HEARTBEAT_ENABLED_KEY.equals(attr)){
                        if(Boolean.valueOf(((ConfigUpdate)change).getNewValue(attr))){
                            initGMSGroupForNamedCluster( clusterName );
                        }
                        else{
                            leaveGMSGroupForNamedCluster( clusterName,
                                     GMSConstants.shutdownType.INSTANCE_SHUTDOWN);
                        }
                    }
                    else if(HEARTBEAT_ADDRESS_KEY.equals(attr) ||
                            HEARTBEAT_PORT_KEY.equals(attr))
                    {
                        leaveGMSGroupForNamedCluster( clusterName,
                                 GMSConstants.shutdownType.INSTANCE_SHUTDOWN);
                        initGMSGroupForNamedCluster( clusterName );
                    }
                }
                try {
                    final Cluster cluster = ClusterHelper.getClusterByName(
                            getConfigContext(), clusterName);
                    final ServerRef[] refs = cluster.getServerRef();
                    com.sun.enterprise.ee.admin.mbeanapi.ServerRuntimeMBean
                        runtimeMBean;
                    for(final ServerRef ref : refs){
                        runtimeMBean = InstanceProxy.getInstanceProxy(
                                ref.getRef());
                        runtimeMBean.setRestartRequired(true);
                    }
                }
                catch ( ConfigException e ) {
                    getLogger().log(Level.WARNING, e.getLocalizedMessage());
                }
                catch ( InstanceException e ) {
                    getLogger().log(Level.WARNING, e.getLocalizedMessage());
                }
                catch ( MBeanException e ) {
                    getLogger().log(Level.WARNING, e.getLocalizedMessage());
                }
            }
        }
    }
}
