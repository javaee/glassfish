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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.config.ConfigException;

public class MockClusterAndServerRegistry
{
    private static final Map registry               = new HashMap();
    private static final Set clusterServerAssocs    = new HashSet();

    /** Creates a new instance of MockClusterAndInstanceRegistry */
    private MockClusterAndServerRegistry()
    {
    }

    public synchronized static void addCluster(String name)
    {
        registry.put(name, new MockCluster(name));
    }

    public synchronized static void addCluster(MockCluster cluster)
    {
        registry.put(cluster.getName(), cluster);
    }

    public synchronized static void deleteCluster(String name)
    {
        registry.remove(name);
    }

    public synchronized static void addServer(String name)
    {
        registry.put(name, new MockServer(name));
    }

    public synchronized static void addServer(MockServer server)
    {
        registry.put(server.getName(), server);
    }

    public synchronized static void deleteServer(String name)
    {
        registry.remove(name);
    }

    public synchronized static MockCluster getCluster(String clusterName)
    {
        return (MockCluster)registry.get(clusterName);
    }

    public synchronized static MockServer getServer(String serverName)
    {
        return (MockServer)registry.get(serverName);
    }

    public synchronized static List getClusters()
    {
        final ArrayList list = new ArrayList();
        final Iterator it = registry.values().iterator();
        while (it.hasNext())
        {
            Object o = it.next();
            if (o instanceof MockCluster)
            {
                list.add(o);
            }
        }
        return list;
    }

    public synchronized static List getServers()
    {
        final ArrayList list = new ArrayList();
        final Iterator it = registry.values().iterator();
        while (it.hasNext())
        {
            Object o = it.next();
            if (o instanceof MockServer)
            {
                list.add(o);
            }
        }
        return list;
    }

    public synchronized static List getAssociations(String name)
    {
        final Iterator it = clusterServerAssocs.iterator();
        final ArrayList assocs = new ArrayList();
        while (it.hasNext())
        {
            final MockClusterServerAssociation assoc = 
                (MockClusterServerAssociation)it.next();
            if (assoc.getClusterRef().equals(name) || 
                assoc.getServerRef().equals(name))
            {
                assocs.add(assoc);
            }
        }
        return assocs;
    }

    public static void associate(String clusterName, 
                                 String serverName)
    {
        assert !clusterName.equals(serverName);
        final MockCluster   cluster = getCluster(clusterName);
        final MockServer    server  = getServer(serverName);
        assert (cluster != null) && (server != null);
        final MockClusterServerAssociation assoc = 
            new MockClusterServerAssociation(cluster, server);
        synchronized (MockClusterAndServerRegistry.class)
        {
            boolean b = clusterServerAssocs.add(assoc);
        }
    }

    public static void dissociate(String clusterName, 
                                  String serverName)
    {
        assert !clusterName.equals(serverName);
        final MockCluster   cluster = getCluster(clusterName);
        final MockServer    server  = getServer(serverName);
        assert (cluster != null) && (server != null);
        final MockClusterServerAssociation assoc = 
            new MockClusterServerAssociation(cluster, server);
        synchronized (MockClusterAndServerRegistry.class)
        {
            boolean b = clusterServerAssocs.remove(assoc);
        }
    }

    private static String domainName = "mockdomain";
    public static synchronized void setDomain(String domain)
    {
        domainName = domain;
    }

    public static String getDomain()
    {
        return domainName;
    }

    public static Target getTarget(String targetName) throws ConfigException
    {
        if (!domainName.equals(targetName))
        {
            throw new ConfigException("Target not found: " + targetName);
        }
        return new MockTarget(getDomain());
    }
}