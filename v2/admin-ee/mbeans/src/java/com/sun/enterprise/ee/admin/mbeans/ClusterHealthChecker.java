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
 * ClusterHealthChecker.java
 *
 * Created on August 5, 2005, 12:48 AM
 */

package com.sun.enterprise.ee.admin.mbeans;

import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.util.i18n.StringManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 */
public class ClusterHealthChecker implements ClusterHealthCheckerMBean {

    private final ConfigContext acc;
    private static final StringManager sm = StringManager.getManager(ClusterHealthChecker.class);
    public ClusterHealthChecker(final ConfigContext acc) {
        this.acc = acc;
    }

    /** Returns the health of the cluster as a particular data structure. Here are the details of the data structure:
     * <ul>
     * <li> It is a map with entries keyed on the name of server instance in the cluster.</li>
     * <li> The values in the Map is a list of 2 Long numbers.</li>
     * <li> The first Long number in the list is the actual status of the instance. </li>
     *   <ul>
     *    <li> 0 ==> Running </li>
     *    <li> 1 ==> Stopped </li>
     *    <li> 2 ==> Failed <code> available only when GMS is enabled </code> </li>
     *    <li> 3 ==> Not Yet Started <code> available only when GMS is enabled</li> 
     *   </ul>
     * <li> The second Long number in the list is the time (as a long) for which that instance is in the said state. 
     *      This is available only when GMS is enabled. If GMS is not enabled, then -1 is returned.</li>
     * </ul>
     * @throws InstanceException if there is anything wrong with getting the health data
     * @param targetCluster a String representing the name of the cluster
     */
    public Map<String, List<Long>> getClusterHealth(String targetCluster) throws InstanceException {
        if (targetCluster == null)
            throw new IllegalArgumentException("null_arg");
        try {
            if (!ClusterHelper.isACluster(acc, targetCluster)) {
                throw new InstanceException(sm.getString("noSuchCluster", targetCluster));
            }
        } catch (final Exception e) {
            throw new InstanceException(e);
        }
        Map<String, List<Long>> health = null;
        if (gmsEnabled(targetCluster)) {
            health = getHealthFromGMS(targetCluster);
        }
        else {
            health = getHealthFromJMX(targetCluster);
        }
        return ( health );
    }
    
    private boolean gmsEnabled(final String cn) throws InstanceException {
        try {
            final Cluster cluster = ClusterHelper.getClusterByName(acc, cn);
            final boolean gms = cluster.isHeartbeatEnabled();
            return ( gms );
        } catch(final Exception e) {
            throw new InstanceException(e);
        }
    }
    private Map<String, List<Long>> getHealthFromGMS(final String cluster) throws InstanceException {
        Map<String, List<Long>> health = null;
        try {
            final ObjectName on                 = EEConfigMBeanObjectNames.getGMSClientObjName();
            final MBeanServerConnection mbsc    = MBeanServerFactory.getMBeanServer();
            final String m                      = "getClusterHealth";
            final Object[] p                    = new String[]{cluster};
            final String[] s                    = new String[]{"java.lang.String"};
            health                              = (Map<String, List<Long>>) mbsc.invoke(on, m, p, s);
            return ( health );
        } catch(final Exception e) {
            throw new InstanceException(e);
        }
    }
    private Map<String, List<Long>> getHealthFromJMX(final String cluster) throws InstanceException {
        try {
            final RuntimeStatusList list = invokeClusterMBeanForStatus(cluster);
            final Map<String, List<Long>> map = statusList2Map(list);
            return ( map );
        } catch(final Exception e) {
            throw new InstanceException(e);
        }
    }
    private RuntimeStatusList invokeClusterMBeanForStatus(final String cluster) throws Exception {
        final ObjectName on                     = EEConfigMBeanObjectNames.getClusterConfigMBeanObjectName(cluster);
        final MBeanServerConnection mbsc        = MBeanServerFactory.getMBeanServer();
        final String m                          = "getRuntimeStatus";
        
        final RuntimeStatusList l = (RuntimeStatusList) mbsc.invoke(on, m, null, null);
        return ( l );
    }
    private Map<String, List<Long>> statusList2Map(final RuntimeStatusList list) throws Exception {
        final Map<String, List<Long>> map = new HashMap<String, List<Long>> ();
        int i = 0;
        for (final Object o : list) {
            final RuntimeStatus rs = list.getStatus(i);
            final String name = rs.getName();
            map.put(name, convert(rs));
            i++;
        }
        return ( map );
    }
    
    private List<Long> convert(final RuntimeStatus rs) {
        final List<Long> list       = new ArrayList<Long> ();
        Long s = -1L;
        final Long undefinedTime    = new Long(-1);
        if (rs.isRunning())
            s = 0L;
        if (rs.isStopped())
            s = 1L;
        list.add(s);
        list.add(undefinedTime);
        return ( list );
    }
}