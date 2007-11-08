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

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.ee.cms.core.GMSConstants;

import java.util.List;
import java.util.Map;

/**
 * Interacts with the Group Management Service (GMS) subsystem for each cluster
 * to be notified of group events for that cluster such as instance started,
 * instance stopped, and instance failed. Provides APIs specific for the
 * Domain Admin Server clients to create a GMS instance for each cluster,
 * get the health of instances of a cluster, and handle notifications for each
 * group event.
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Jul 28, 2005
 * @version $Revision: 1.1.1.1 $
 */
public interface GMSClient {
    /**
     * Provides the current status of the cluster by retrieving the state from
     * a state table maintained by the implementing class that is dynamically
     * updated by GMS notifications for any changes in the cluster state.
     *
     * @param clusterName - String representing the name of the cluster.
     * @return Map&lt;String, List&lt;Long&gt;&gt; - containing a String object
     * for key representing the server instance id, with value being a
     * two-element List of Long objects, the  first of which could have value
     * 0, 1, 2, or 3 where 0 = instanceStarted event, 1 = instanceStopped event,
     * 2 = instanceFailed event, and 3 = instanceNotYetStarted state. The second
     * element in the List is a timestamp of when the event described in the
     * first element occurred. The timestamp will be empty when the state is
     * instanceNotYetStarted.
     */
    Map<String, List<Long>> getClusterHealth(String clusterName);

    /**
     * creates a GroupManagementService object for each cluster in domain config
     * and starts each GroupManagementService instance resulting in joining the
     * GMS group as a SPECTATOR member. Implementation will also register with
     * GMS for all notifications
     */
    void initGMSGroupForAllClusters();

    /**
     * creates a GroupManagementService object for the specified cluster and
     * starts the GroupManagementService instance resulting in joining the
     * GMS group as a SPECTATOR member. Implementation will also register with
     * GMS for all notifications.
     * @param clusterName
     */
    void initGMSGroupForNamedCluster(String clusterName);

    /**
     * Every GMS instance representing each cluster leaves its GMS Group. This
     * is typically called when the Domain Admin Server is being Shutdown with
     * shutdown type specified as instance shutdown
     * @see com.sun.enterprise.ee.cms.core.GMSConstants
     */
    void leaveGMSGroupForAllClusters(GMSConstants.shutdownType shutdownType);

    /**
     * The GMS instance representing the specified cluster, leaves the GMS group.
     * This is typically done when either the domain admin server is being
     * stopped or the specified cluster is being stopped. If the domain admin
     * server is being stopped, the shutdown type is instance shutdown, while
     * the shutdown type for a cluster's shutdown is group shutdown.
     * @see com.sun.enterprise.ee.cms.core.GMSConstants
     * @param clusterName
     */

    void leaveGMSGroupForNamedCluster(String clusterName,
                                      GMSConstants.shutdownType shutdownType);
}
