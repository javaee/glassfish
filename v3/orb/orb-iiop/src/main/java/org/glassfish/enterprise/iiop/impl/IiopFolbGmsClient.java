/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.enterprise.iiop.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.enterprise.ee.cms.core.FailureNotificationSignal;
import com.sun.enterprise.ee.cms.core.JoinedAndReadyNotificationSignal;
import com.sun.enterprise.ee.cms.core.PlannedShutdownSignal;
import com.sun.enterprise.ee.cms.core.Signal;
import com.sun.enterprise.ee.cms.core.SignalAcquireException;
import com.sun.enterprise.ee.cms.core.SignalReleaseException;
import com.sun.logging.LogDomains;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.impl.folb.GroupInfoServiceBase;
import com.sun.corba.ee.spi.folb.GroupInfoServiceObserver;
import com.sun.corba.ee.spi.folb.SocketInfo;
import com.sun.corba.ee.spi.orbutil.ORBConstants;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.IiopService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.ee.cms.core.CallBack;
import java.util.ArrayList;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.GlassFishConfigBean;
import org.glassfish.config.support.PropertyResolver;
import org.glassfish.gms.bootstrap.GMSAdapter;
import org.glassfish.gms.bootstrap.GMSAdapterService;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.omg.CORBA.ORBPackage.InvalidName;

// REVISIT impl
//import com.sun.corba.ee.impl.folb.ServerGroupManager;

/**
 * @author Harold Carr
 */
public class IiopFolbGmsClient implements CallBack {
    private static final Logger _logger =
       LogDomains.getLogger(IiopFolbGmsClient.class,
           LogDomains.CORBA_LOGGER);

    @Inject
    private Domain domain ;

    // Get my Server instance so we can find our cluster
    @Inject( name=ServerEnvironment.DEFAULT_INSTANCE_NAME )
    private Server myServer ;

    @Inject
    private Habitat habitat;

    @Inject( optional=true )
    private GMSAdapterService gmsAdapterService ;

    private GMSAdapter gmsAdapter ;

    private Map<String, ClusterInstanceInfo> currentMembers;

    private GroupInfoService gis;

    private void fineLog( String fmt, Object... args ) {
        if(_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, fmt, args ) ;
        }
    }

    public IiopFolbGmsClient() {
	try {
            if (gmsAdapterService != null) {
                gmsAdapter = gmsAdapterService.getGMSAdapter() ;

                fineLog( "IiopFolbGmsClient->: {0}", gmsAdapter );

                gis = new GroupInfoServiceGMSImpl() ;

                fineLog("IiopFolbGmsClient: IIOP GIS created");

                currentMembers = getAllClusterInstanceInfo() ;

                fineLog( "IiopFolbGmsClient: currentMembers = ", currentMembers ) ;

                gmsAdapter.registerFailureNotificationListener(this);
                gmsAdapter.registerJoinedAndReadyNotificationListener(this);
                gmsAdapter.registerPlannedShutdownListener(this);

                fineLog( "IiopFolbGmsClient: GMS action factories added");
            } else {
                gis = new GroupInfoServiceNoGMSImpl() ;
            }

	} catch (Throwable t) {
            _logger.log(Level.SEVERE, t.getLocalizedMessage(), t);
	} finally {
            if(_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, "IiopFolbGmsClient<-: {0}", gmsAdapter );
	    }
	}
    }

    public void setORB( ORB orb ) {
        try {
            orb.register_initial_reference(
                    ORBConstants.FOLB_SERVER_GROUP_INFO_SERVICE,
                    (org.omg.CORBA.Object) gis);
            fineLog( ".initGIS: naming registration complete: {0}", gis);

            if (_logger.isLoggable(Level.FINE)) {
                gis = (GroupInfoService)orb.resolve_initial_references(
                    ORBConstants.FOLB_SERVER_GROUP_INFO_SERVICE);
                List<ClusterInstanceInfo> lcii =
                        gis.getClusterInstanceInfo(null);
                _logger.log(Level.FINE,
                        "Results from getClusterInstanceInfo:");
                if (lcii != null) {
                    for (ClusterInstanceInfo cii : lcii) {
                        _logger.log(Level.INFO, cii.toString() );
                    }
                }
            }
        } catch (InvalidName e) {
            fineLog( ".initGIS: registering GIS failed: {0}", e);
        }
    }

    public GroupInfoService getGroupInfoService() {
        return gis ;
    }

    public boolean isGMSAvailable() {
        return gmsAdapter != null ;
    }

    ////////////////////////////////////////////////////
    //
    // Action
    //

    @Override
    public void processNotification(final Signal signal) {
        try {
            signal.acquire();
            handleSignal(signal);
        } catch (SignalAcquireException e) {
            _logger.log(Level.SEVERE, e.getLocalizedMessage());
	} catch (Throwable t) {
	    _logger.log(Level.SEVERE, t.getLocalizedMessage(), t);
        } finally {
	    try {
		signal.release();
	    } catch (SignalReleaseException e) {
		_logger.log(Level.SEVERE, e.getLocalizedMessage());
	    }
	}
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    private void handleSignal(final Signal signal) 
    {
	if(_logger.isLoggable(Level.FINE)) {
	    _logger.log(Level.FINE, 
                "IiopFolbGmsClient.handleSignal: signal from: {0}",
                signal.getMemberToken());
	    _logger.log(Level.FINE, 
                "IiopFolbGmsClient.handleSignal: map entryset: {0}",
                signal.getMemberDetails().entrySet());
	}

	if (signal instanceof PlannedShutdownSignal ||
	    signal instanceof FailureNotificationSignal) {

	    removeMember(signal);

	} else if (signal instanceof JoinedAndReadyNotificationSignal) {

	    addMember(signal);

	} else {
	    _logger.log(Level.SEVERE, 
                "IiopFolbGmsClient.handleSignal: unknown signal: {0}",
                signal.toString());
	}
    }

    private void removeMember(final Signal signal)
    {
	String instanceName = signal.getMemberToken();
	try {
            fineLog( "IiopFolbGmsClient.removeMember->: {0}",
                instanceName);

	    synchronized (this) {
		if (currentMembers.get(instanceName) != null) {
		    currentMembers.remove(instanceName);

                    fineLog(
                        "IiopFolbGmsClient.removeMember: {0} removed - notifying listeners",
                        instanceName);

		    gis.notifyObservers();

                    fineLog(
                        "IiopFolbGmsClient.removeMember: {0} - notification complete",
                        instanceName);
		} else {
                    fineLog(
                        "IiopFolbGmsClient.removeMember: {0} not present: no action",
                        instanceName);
		}
	    }
	} finally {
            fineLog( "IiopFolbGmsClient.removeMember<-: {0}", instanceName);
	}
    }

    private void addMember(final Signal signal)
    {
	final String instanceName = signal.getMemberToken();
	try {
            fineLog( "IiopFolbGmsClient.addMember->: {0}", instanceName);

	    synchronized (this) {
		if (currentMembers.get(instanceName) != null) {
                    fineLog( "IiopFolbGmsClient.addMember: {0} already present: no action",
                            instanceName);
		} else {
		    ClusterInstanceInfo clusterInstanceInfo = 
                        getClusterInstanceInfo(instanceName) ;

		    currentMembers.put( clusterInstanceInfo.name(),
                        clusterInstanceInfo);

                    fineLog( "IiopFolbGmsClient.addMember: {0} added - notifying listeners",
                        instanceName);

		    gis.notifyObservers();

                    fineLog( "IiopFolbGmsClient.addMember: {0} - notification complete",
                        instanceName);
		}
	    }	
	} finally {
            fineLog( "IiopFolbGmsClient.addMember<-: {0}", instanceName);
	}
    }

    private int resolvePort( Server server, IiopListener listener ) {
        IiopListener ilRaw = GlassFishConfigBean.getRawView( listener ) ;
        PropertyResolver pr = new PropertyResolver( domain, server.getName() ) ;
        String port = pr.getPropertyValue( ilRaw.getPort() ) ;
        return Integer.parseInt(port) ;
    }

    private ClusterInstanceInfo getClusterInstanceInfo( Server server,
        Config config ) {

        String name = server.getName() ;
        int weight = Integer.parseInt( server.getLbWeight() ) ;

        String host = server.getNodeAgentRef() ; // Is this correct?

        IiopService iservice = config.getIiopService() ;
        List<IiopListener> listeners = iservice.getIiopListener() ;

        List<SocketInfo> sinfos = new ArrayList<SocketInfo>() ;
        for (IiopListener il : listeners) {
            SocketInfo sinfo = new SocketInfo( host, il.getId(),
                resolvePort( server, il ) ) ;
            sinfos.add( sinfo ) ;
        }

        ClusterInstanceInfo result = new ClusterInstanceInfo( name, weight,
            sinfos ) ;

        return result ;
    }

    private Config getConfigForServer( Server server ) {
        String configRef = server.getConfigRef() ;
        Configs configs = habitat.getComponent( Configs.class ) ;
        Config config = configs.getConfigByName(configRef) ;
        return config ;
    }

    // For addMember
    private ClusterInstanceInfo getClusterInstanceInfo( String instanceName ) {
        Servers servers = habitat.getComponent( Servers.class ) ;
        Server server = servers.getServer(instanceName) ;

        Config config = getConfigForServer( server ) ;

        return getClusterInstanceInfo( server, config ) ;
    }

    private Map<String,ClusterInstanceInfo> getAllClusterInstanceInfo() {
        Cluster myCluster = myServer.getCluster() ;
        Config myConfig = getConfigForServer( myServer ) ;

        Map<String,ClusterInstanceInfo> result =
            new HashMap<String,ClusterInstanceInfo>() ;

        for (Server server : myCluster.getInstances()) {
            ClusterInstanceInfo cii = getClusterInstanceInfo( server, myConfig ) ;
            result.put( server.getName(), cii ) ;
        }

        return result ;
    }

    class GroupInfoServiceGMSImpl extends GroupInfoServiceBase {
        @Override
        public List<ClusterInstanceInfo> internalClusterInstanceInfo() {
            return new ArrayList<ClusterInstanceInfo>(
                currentMembers.values() ) ;
        }
    }

    class GroupInfoServiceNoGMSImpl extends GroupInfoServiceGMSImpl {
        @Override
        public boolean addObserver(GroupInfoServiceObserver x) {
            throw new RuntimeException("SHOULD NOT BE CALLED");
        }

        @Override
        public void notifyObservers() {
            throw new RuntimeException("SHOULD NOT BE CALLED");
        }

        @Override
        public boolean shouldAddAddressesToNonReferenceFactory(String[] x) {
            throw new RuntimeException("SHOULD NOT BE CALLED");
        }

        @Override
        public boolean shouldAddMembershipLabel(String[] adapterName) {
            throw new RuntimeException("SHOULD NOT BE CALLED");
        }
    }
}

// End of file.
