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

package com.sun.enterprise.ee.ejb.iiop;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.enterprise.ee.cms.core.Action;
import com.sun.enterprise.ee.cms.core.FailureNotificationAction;
import com.sun.enterprise.ee.cms.core.FailureNotificationActionFactory;
import com.sun.enterprise.ee.cms.core.FailureNotificationSignal;
import com.sun.enterprise.ee.cms.core.GMSFactory;
import com.sun.enterprise.ee.cms.core.GroupHandle;
import com.sun.enterprise.ee.cms.core.GroupManagementService;
import com.sun.enterprise.ee.cms.core.JoinNotificationAction;
import com.sun.enterprise.ee.cms.core.JoinNotificationActionFactory;
import com.sun.enterprise.ee.cms.core.JoinNotificationSignal;
import com.sun.enterprise.ee.cms.core.PlannedShutdownAction;
import com.sun.enterprise.ee.cms.core.PlannedShutdownActionFactory;
import com.sun.enterprise.ee.cms.core.PlannedShutdownSignal;
import com.sun.enterprise.ee.cms.core.Signal;
import com.sun.enterprise.ee.cms.core.SignalAcquireException;
import com.sun.enterprise.ee.cms.core.SignalReleaseException;
import com.sun.enterprise.ee.cms.ext.IiopInfo;
import com.sun.enterprise.iiop.ASORBUtilities;
import com.sun.logging.LogDomains;

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.spi.folb.GroupInfoServiceBase;
import com.sun.corba.ee.spi.folb.GroupInfoServiceObserver;
import com.sun.corba.ee.spi.folb.SocketInfo;
import com.sun.corba.ee.impl.orbutil.ORBUtility;

// REVISIT impl
//import com.sun.corba.ee.impl.folb.ServerGroupManager;

/**
 * @author Harold Carr
 */
public class IiopFolbGmsClient
    extends org.omg.CORBA.LocalObject
    implements
	// GMS
	Action,
	FailureNotificationAction,
	FailureNotificationActionFactory,
	JoinNotificationAction,
	JoinNotificationActionFactory,
	PlannedShutdownAction,
	PlannedShutdownActionFactory,
	// IIOP
	GroupInfoService
{
    private static Logger _logger = null;
    static {
       _logger = LogDomains.getLogger(LogDomains.CORBA_LOGGER);
    }

    private GroupManagementService gms = null;
    private Map<String, ClusterInstanceInfo> currentMembers;
    private GIS gis;

    /**
     * REVISIT - Document how/when this constructor is called.
     */
    public IiopFolbGmsClient()
    {
	try {
            if(_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, "IiopFolbGmsClient->: " + gms);
	    }
	    gms = GMSFactory.getGMSModule();

	    gms.addActionFactory((FailureNotificationActionFactory) this);
	    gms.addActionFactory((JoinNotificationActionFactory) this);
	    gms.addActionFactory((PlannedShutdownActionFactory) this);

            if(_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE,
			    "IiopFolbGmsClient: GMS action factories added");
	    }

	    gis = new GIS();

            if(_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, "IiopFolbGmsClient: IIOP GIS created");
	    }

	    currentMembers = new HashMap<String, ClusterInstanceInfo>();

	    Set<java.util.Map.Entry<Serializable,Serializable>> entrySet =
		gms.getAllMemberDetails(IiopInfo.IIOP_MEMBER_DETAILS_KEY).entrySet();

            if(_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, 
			    "IiopFolbGmsClient: initializing to: "
			    + entrySet.toString());
	    }

	    for (Map.Entry entry : entrySet) {
		ClusterInstanceInfo cii = 
		    convert((String)entry.getKey(),
			    (List<IiopInfo>)entry.getValue());
		currentMembers.put(cii.name, cii);
	    }
	} catch (Throwable t) {
            _logger.log(Level.SEVERE, t.getLocalizedMessage(), t);
	} finally {
            if(_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, "IiopFolbGmsClient<-: " + gms);
	    }
	}
    }

    ////////////////////////////////////////////////////
    //
    // *Factory
    //

    public Action produceAction()
    {
	return this;
    }

    ////////////////////////////////////////////////////
    //
    // Action
    //

    public void consumeSignal(final Signal signal) {
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
    // GroupInfoService
    //

    private class GIS
	extends GroupInfoServiceBase
    {
	public List<ClusterInstanceInfo> getClusterInstanceInfo(
            String[] adapterName)
	{
	    // REVISIT
	    // Put a check for null in formatStringArray
	    // Workaround here to avoid an ORB integration.
	    if (adapterName == null) {
		adapterName = new String[0];
	    }
	    List result = new LinkedList<ClusterInstanceInfo>();
	    try {
		if(_logger.isLoggable(Level.FINE)) {
		    _logger.log(Level.FINE, 
				"IiopFolbGmsClient.getClusterInstanceInfo->: "
				+ ORBUtility
				    .formatStringArray(adapterName));
		}
		synchronized (currentMembers) {
		    for (ClusterInstanceInfo cii : currentMembers.values()) {
			result.add(cii);
		    }
		    return result;
		}
	    } finally {
		if(_logger.isLoggable(Level.FINE)) {
		    _logger.log(Level.FINE, 
				"IiopFolbGmsClient.getClusterInstanceInfo<-: "
				+ ORBUtility.formatStringArray(adapterName)
				+ ": " + result); // REVISIT - need toString
		}
	    }
	}

	public boolean shouldAddAddressesToNonReferenceFactory(
            String[] adaptername)
	{
	    return false;
	}

	public boolean shouldAddMembershipLabel (String[] adapterName)
	{
	    return true;
	}
    }

    public boolean addObserver(GroupInfoServiceObserver x)
    {
	return gis.addObserver(x);
    }
    public void notifyObservers()
    {
	gis.notifyObservers();
    }
    public List<ClusterInstanceInfo> getClusterInstanceInfo(
        String[] adapterName)
    {
	return gis.getClusterInstanceInfo(adapterName);
    }
    public boolean shouldAddAddressesToNonReferenceFactory(String[] x)
    {
	return gis.shouldAddAddressesToNonReferenceFactory(x);
    }
    public boolean shouldAddMembershipLabel (String[] adapterName)
    {
	return gis.shouldAddMembershipLabel(adapterName);
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    private void handleSignal(final Signal signal) 
    {
	if(_logger.isLoggable(Level.FINE)) {
	    _logger.log(Level.FINE, 
			"IiopFolbGmsClient.handleSignal: signal from: " 
			+ signal.getMemberToken());
	    _logger.log(Level.FINE,
			"IiopFolbGmsClient.handleSignal: map entryset: " 
			+ signal.getMemberDetails().entrySet());
	}

	if (signal instanceof PlannedShutdownSignal ||
	    signal instanceof FailureNotificationSignal) {

	    removeMember(signal);

	} else if (signal instanceof JoinNotificationSignal) {

	    addMember(signal);

	} else {
	    _logger.log(Level.SEVERE,
			"IiopFolbGmsClient.handleSignal: unknown signal: " 
			+ signal.toString());
	}
    }

    private void removeMember(final Signal signal)
    {
	String instanceName = signal.getMemberToken();
	try {
	    if(_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, 
			    "IiopFolbGmsClient.removeMember->: "
			    + instanceName);
	    }

	    synchronized (currentMembers) {
		if (currentMembers.get(instanceName) != null) {
		    currentMembers.remove(instanceName);
		    if(_logger.isLoggable(Level.FINE)) {
			_logger.log(Level.FINE, 
				    "IiopFolbGmsClient.removeMember: "
				    + instanceName
				    + " removed - notifying listeners");
		    }
		    gis.notifyObservers();
		    if(_logger.isLoggable(Level.FINE)) {
			_logger.log(Level.FINE, 
				    "IiopFolbGmsClient.removeMember: "
				    + instanceName
				    + " - notification complete");
		    }
		} else {
		    if(_logger.isLoggable(Level.FINE)) {
			_logger.log(Level.FINE, 
				    "IiopFolbGmsClient.removeMember: " 
				    + instanceName
				    + " not present: no action");
		    }
		}
	    }
	} finally {
            if(_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, 
			    "IiopFolbGmsClient.removeMember<-: " 
			    + instanceName);
	    }
	}
    }

    private void addMember(final Signal signal)
    {
	final String instanceName = signal.getMemberToken();
	try {
            if(_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, 
			    "IiopFolbGmsClient.addMember->: "
			    + instanceName);
	    }

	    synchronized (currentMembers) {
		if (currentMembers.get(instanceName) != null) {
		    if(_logger.isLoggable(Level.FINE)) {
			_logger.log(Level.FINE, 
				    "IiopFolbGmsClient.addMember: " 
				    + instanceName
				    + " already present: no action");
		    }
		} else {
		    ClusterInstanceInfo clusterInstanceInfo = convert(signal);
		    currentMembers.put(clusterInstanceInfo.name,
				       clusterInstanceInfo);
		    if(_logger.isLoggable(Level.FINE)) {
			_logger.log(Level.FINE, 
				    "IiopFolbGmsClient.addMember: "
				    + instanceName
				    + " added - notifying listeners");
		    }
		    gis.notifyObservers();
		    if(_logger.isLoggable(Level.FINE)) {
			_logger.log(Level.FINE, 
				    "IiopFolbGmsClient.addMember: "
				    + instanceName
				    + " - notification complete");
		    }
		}
	    }	
	} finally {
            if(_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, 
			    "IiopFolbGmsClient.addMember<-: "
			    + instanceName);
	    }
	}
    }

    private ClusterInstanceInfo convert(final Signal signal)
    {
	return convert(
	    signal.getMemberToken(),
            (List<IiopInfo>)
	    signal.getMemberDetails().get(IiopInfo.IIOP_MEMBER_DETAILS_KEY));
    }

    private ClusterInstanceInfo convert(final String instanceName,
					final List<IiopInfo> iiopInfoList)
    {
	ClusterInstanceInfo clusterInstanceInfo = null;
	try {
            if(_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, 
			    "IiopFolbGmsClient.convert->: " + instanceName);
	    }

	    List<SocketInfo> listOfSocketInfo = new LinkedList<SocketInfo>();

	    int weight = -1;
	    // bug 6502567
	    // the list should never be null. 
	    //Fix is already in Shoal workspace, but not integrated into 9.1FCS
	    if (iiopInfoList == null) {
	        _logger.fine("GMS did not return the list of members. list is empty.");
		return null;
	    }
	    for (IiopInfo iiopInfo : iiopInfoList) {
		if(_logger.isLoggable(Level.FINE)) {
		    _logger.log(Level.FINE, 
				"IiopFolbGmsClient.convert: " + instanceName
				+ ": " + iiopInfo.toString());
		}
		// REVISIT
		// Make sure all weights equal
		if (weight == -1) {
		    weight = iiopInfo.getWeight();
		    if(_logger.isLoggable(Level.FINE)) {
			_logger.log(Level.FINE,
				    "IiopFolbGmsClient.convert: "
				    + instanceName
				    + ": weight: " + Integer.toString(weight));
		    }
		}
		String type = iiopInfo.getID();
		String host = iiopInfo.getAddress();
		String port = iiopInfo.getPort();
	    
		// REVISIT - need to check all forms of "all interfaces"
		if ("0.0.0.0".equals(host)) {
		    String hostname = iiopInfo.getHostName();
		    if(_logger.isLoggable(Level.FINE)) {
			_logger.log(Level.FINE, 
				    "IiopFolbGmsClient.convert: "
				    + instanceName
				    + ": host is: " + host
				    + "; changing to: " + hostname);
		    }
		    host = hostname;
		}
		if(_logger.isLoggable(Level.FINE)) {
		    _logger.log(Level.FINE, 
				"IiopFolbGmsClient.convert: " + instanceName
				+ ": type/host/port:"
				+ " " + type + " " + host + " " + port);
		}
		listOfSocketInfo.add(
                    new SocketInfo(type, host, Integer.valueOf(port)));
	    }
	    // REVISIT - make orbutil utility
	    SocketInfo[] arrayOfSocketInfo =
		new SocketInfo[listOfSocketInfo.size()];
	    int i = 0;
	    for (SocketInfo si : listOfSocketInfo) {
		arrayOfSocketInfo[i++] = si;
	    }
	    clusterInstanceInfo = 
		new ClusterInstanceInfo(instanceName, weight, 
					arrayOfSocketInfo);

	    return clusterInstanceInfo;	   

	} finally {
            if(_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, 
			    "IiopFolbGmsClient.convert<-: " + instanceName
			    + ": "
			    + ASORBUtilities.toString(clusterInstanceInfo));
	    }
	}
    }
}


// End of file.
