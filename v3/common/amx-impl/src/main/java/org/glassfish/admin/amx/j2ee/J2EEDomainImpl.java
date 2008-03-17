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
package org.glassfish.admin.amx.j2ee;

import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;

import com.sun.appserv.management.j2ee.J2EETypes;
import com.sun.appserv.management.j2ee.J2EEDomain;
import com.sun.appserv.management.j2ee.J2EEServer;
import com.sun.appserv.management.j2ee.J2EECluster;
import com.sun.appserv.management.base.XTypes;


import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.GSetUtil;

import org.glassfish.admin.amx.util.Issues;
import org.glassfish.admin.amx.mbean.Delegate;

/**
 */
public final class J2EEDomainImpl extends J2EEManagedObjectImplBase
{
		public
	J2EEDomainImpl(
        final String fullType,
        final ObjectName parentObjectName,
        final Delegate delegate )
	{
		super( J2EETypes.J2EE_DOMAIN, fullType, parentObjectName, J2EEDomain.class, delegate );
	}

		public boolean
	isConfigProvider()
	{
		return( true );
	}
	
	private static final Set<String> NOT_SUPERFLUOUS =
	    GSetUtil.newUnmodifiableStringSet( "getServerObjectNameMap", "getClusterObjectNameMap" );
	    
	    protected final Set<String>
	getNotSuperfluousMethods()
	{
	    return GSetUtil.newSet( super.getNotSuperfluousMethods(), NOT_SUPERFLUOUS );
	}
	
	    public Map<String,ObjectName>
	getServerObjectNameMap()
	{
	    return Util.toObjectNames( getSelfProxy().getJ2EEServerMap() );
	}
	
	    public Map<String,ObjectName>
	getClusterObjectNameMap()
	{
	    return Util.toObjectNames( getSelfProxy().getJ2EEClusterMap() );
	}

	    private J2EEDomain
	getSelfProxy()
	{
	    return (J2EEDomain)getSelf();
	}
	
	/**
		JSR 77 impl
		@return String representation of the ObjectName
	 */
		public String[]
	getservers()
	{
	    final Map<String,J2EEServer> servers = getSelfProxy().getJ2EEServerMap();
	    final Map<String,ObjectName> objectNames = Util.toObjectNames( servers );
		return( CollectionUtil.toStringArray( objectNames.values() ) );
	}
	
		public ObjectName
	getConfigPeerObjectName()
	{
		return Util.getObjectName( getDomainRoot().getDomainConfig() );
	}

	/**
		JSR 77 impl
		@return String representation of the ObjectName
	 */
		public String[]
	getclusters()
	{
	    final Map<String,J2EECluster> clusters = getSelfProxy().getJ2EEClusterMap();
	    final Map<String,ObjectName> objectNames = Util.toObjectNames( clusters );
		return( CollectionUtil.toStringArray( objectNames.values() ) );
	}
}


