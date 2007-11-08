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

import com.sun.enterprise.admin.event.AdminEventListenerRegistry;
import com.sun.enterprise.admin.event.ClusterEvent;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.ee.cms.core.GMSConstants;
import com.sun.enterprise.ee.cms.core.GroupManagementService;
import com.sun.enterprise.ee.cms.logging.GMSLogDomain;
import com.sun.enterprise.server.ApplicationServer;

import javax.management.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is instantiated into a Standard MBean using the construct:<br>
 * <pre> StandardMBean mBean = new StandardMBean(
 * new GMSClientMBean, GMSClient.class);</pre>
 * in the AdminService class's onReady() method.
 * This is an implementation of GMSClient and extends
 * NotificationBroadcasterSupport
 *  
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Jul 29, 2005
 * @version $Revision: 1.1.1.1 $
 */
public class GMSClientMBean extends NotificationBroadcasterSupport
        implements GMSClient
 {
    private Logger _logger;
    private final Map<String, GroupManagementService> gmsInstances =
                new Hashtable<String, GroupManagementService>();

    private final GMSClientMBeanHelper gmsHelper;

    //default constructor
    public GMSClientMBean(){
        gmsHelper = new GMSClientMBeanHelper(this);
        AdminEventListenerRegistry
                        .addEventListener(ClusterEvent.eventType, gmsHelper);
    }
    private Logger getLogger(){
        if(_logger == null)
            _logger = GMSLogDomain.getLogger( GMSLogDomain.GMS_LOGGER);
        return _logger;
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        return notifsInfo;
    }

    private static final String[] types =  {
        "cluster.health.INSTANCE_STARTED_EVENT",
        "cluster.health.INSTANCE_STOPPED_EVENT",
        "cluster.health.INSTANCE_FAILED_EVENT"
    };

    private static final MBeanNotificationInfo[] notifsInfo = {
        new MBeanNotificationInfo(
                types,
                "com.sun.enterprise.ee.admin.mbeans.GMSNotification",
                "Notifications sent by the GMS Client MBean")
    };

    /**
     * Provides the current status of the cluster by retrieving the state from a
     * state table maintained by the implementing class that is dynamically
     * updated by GMS notifications for any changes in the cluster state.
     *
     * @param clusterName - String representing the name of the cluster.
     *
     * @return Map&lt;String, List&lt;Long&gt;&gt; - containing a String object
     *         for key representing the server instance id, with value being a
     *         two-element List of Long objects, the first of which could have
     *         value 0, 1, or 2, where 0 = instanceStarted event, 1 =
     *         instanceStopped event, and 2 = instanceFailed event. The second
     *         element in the List is a timestamp of when the event described in
     *         the first element occurred.
     */
    public Map<String, List<Long>> getClusterHealth ( final String clusterName )
    {
        return gmsHelper.getClusterHealth( clusterName );
    }

    /**
     * creates a GroupManagementService object for each cluster in domain config
     * and starts each GroupManagementService instance resulting in joining the
     * GMS group as a SPECTATOR member. Implementation will also register with
     * GMS for all notifications
     */
    public void initGMSGroupForAllClusters () {
        final ConfigContext configContext =
                ApplicationServer.getServerContext().getConfigContext();
        final Cluster[] clusters;
        try {
            clusters = ClusterHelper.getClustersInDomain(configContext );
            for(Cluster cluster : clusters){
                initGMSGroupForNamedCluster( cluster.getName());
            }
        }
        catch ( ConfigException e ) {
            getLogger().log(Level.WARNING, e.getLocalizedMessage());
        }
    }

    /**
     * creates a GroupManagementService object for the specified cluster and
     * starts the GroupManagementService instance resulting in joining the GMS
     * group as a SPECTATOR member. Implementation will also register with GMS
     * for all notifications.
     *
     * @param clusterName
     */
    public void initGMSGroupForNamedCluster ( final String clusterName ) {
        gmsHelper.initGMSGroupForNamedCluster( clusterName );
    }


    /**
     * Every GMS instance representing each cluster leaves its GMS Group. This
     * is typically called when the Domain Admin Server is being Shutdown.
     */
    public void leaveGMSGroupForAllClusters (
            final GMSConstants.shutdownType shutdownType) {
        final ConfigContext configContext =
                ApplicationServer.getServerContext().getConfigContext();
        final Cluster[] clusters;
        try {
            clusters = ClusterHelper.getClustersInDomain(configContext );
            for(Cluster cluster : clusters){
                leaveGMSGroupForNamedCluster( cluster.getName(), shutdownType);
            }
        }
        catch ( ConfigException e ) {
            getLogger().log(Level.WARNING, e.getLocalizedMessage());
        }
    }

    /**
     * The GMS instance representing the specified cluster, leaves the GMS
     * group. This is typically done when the specified cluster is being
     * stopped.
     *
     * @param clusterName
     */

    public void leaveGMSGroupForNamedCluster ( final String clusterName, 
                               final GMSConstants.shutdownType shutdownType)
    {
        gmsHelper.leaveGMSGroupForNamedCluster(clusterName, shutdownType);
    }

    void sendFailureNotification( final String memberToken,
                                  final long time, final int seqNum )
    {
        final Notification notif = new Notification( types[2],
                                                     getGMSMBeanObjectName(),
                                                     seqNum, time );

        notif.setUserData( memberToken );
        sendNotification(notif);
    }

    void sendStartNotification(final String memberToken,
                               final long startTime,
                               final int seqNum )
    {
        final Notification notif = new Notification( types[0],
                                                     getGMSMBeanObjectName(),
                                                     seqNum, startTime);
        notif.setUserData( memberToken);
        sendNotification( notif );
    }

    void sendStoppedNotification( final String memberToken,
                                  final long time,
                                  final int seqNum )
    {
        final Notification notif = new Notification(types[1],
                                                    getGMSMBeanObjectName(),
                                                    seqNum, time);
        notif.setUserData( memberToken);
        sendNotification(notif);
    }

    private ObjectName getGMSMBeanObjectName(){
        ObjectName gmsObjName = null;
        try {
            gmsObjName = EEConfigMBeanObjectNames.getGMSClientObjName();
        }
        catch ( MalformedObjectNameException e ) {
            getLogger().log(Level.WARNING, e.getLocalizedMessage());
        }
        return gmsObjName;
    }
}
