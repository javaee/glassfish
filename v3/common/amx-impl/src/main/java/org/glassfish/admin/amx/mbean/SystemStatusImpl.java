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
package org.glassfish.admin.amx.mbean;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.management.base.SystemStatus;
import com.sun.appserv.management.base.UnprocessedConfigChange;
import com.sun.appserv.management.config.JDBCConnectionPoolConfig;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;

import javax.management.ObjectName;
import javax.resource.ResourceException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.beans.PropertyChangeEvent;

import org.glassfish.admin.amx.util.Issues;

/**
    
 */
public final class SystemStatusImpl extends AMXNonConfigImplBase
	implements SystemStatus
{
    public SystemStatusImpl(final ObjectName parentObjectName)
	{
        super( SystemStatus.J2EE_TYPE, SystemStatus.J2EE_TYPE, parentObjectName, SystemStatus.class, null );
	}
    
        public Map<String,Object>
    pingJDBCConnectionPool( final String poolName )
    {
        final Map<String,Object> result = new HashMap<String,Object>();
        boolean pingable = false;
        final Habitat habitat = org.glassfish.internal.api.Globals.getDefaultHabitat();
        ConnectorRuntime connRuntime;

        // check pool name
        final JDBCConnectionPoolConfig  cfg = 
        getDomainRoot().getDomainConfig().getResourcesConfig().getJDBCConnectionPoolConfigMap().get( poolName );
        if (cfg == null) {
            result.put( PING_SUCCEEDED_KEY, pingable);
            result.put( REASON_FAILED_KEY, "The pool name " + poolName + " does not exist");
            return result;
        }

        // habitat
        if (habitat == null) {
            result.put( PING_SUCCEEDED_KEY, pingable);
            result.put( REASON_FAILED_KEY, "Habitat is null");
            return result;
        }

        // get connector runtime
        try {
            connRuntime = habitat.getComponent(ConnectorRuntime.class, null);
        } catch (ComponentException e) {
            result.put( PING_SUCCEEDED_KEY, pingable);
            result.put( REASON_FAILED_KEY, ExceptionUtil.toString(e));
            return result;
        }
        
        // ping
        try {
            pingable = connRuntime.pingConnectionPool(poolName);
        } catch (ResourceException e) {
            result.put( PING_SUCCEEDED_KEY, pingable);
            result.put( REASON_FAILED_KEY, ExceptionUtil.toString(e));
            return result;
        }
        
        // success
        result.put( PING_SUCCEEDED_KEY, pingable);
        return result;
    }
    
        public List<Object[]>
    getUnprocessedConfigChanges(final int howMany) {
        Issues.getAMXIssues().notDone( "SystemStatusImpl.getUnprocessedConfigChanges() needs to get all the config changes and morph them appropriately" );
        
        final List<Object[]> changes = new ArrayList<Object[]>();
        
        final UnprocessedConfigChange test = new UnprocessedConfigChange( "TEST", "old", "new", getObjectName(), "for testing" );
        changes.add(test.toArray());
        
        return changes;
    }
}








