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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/j2ee/J2EEServerImpl.java,v 1.10 2007/03/01 22:24:30 llc Exp $
 * $Revision: 1.10 $
 * $Date: 2007/03/01 22:24:30 $
 */
 
package com.sun.enterprise.management.j2ee;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Collections;
import java.util.Iterator;

import javax.management.ObjectName;

import com.sun.appserv.server.util.Version;

import com.sun.appserv.management.j2ee.J2EEServer;
import com.sun.appserv.management.j2ee.JVM;
import com.sun.appserv.management.j2ee.J2EETypes;

import com.sun.appserv.management.util.misc.GSetUtil;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.AMX;

import com.sun.enterprise.management.support.ObjectNames;
import com.sun.enterprise.management.support.TypeInfos;
import com.sun.enterprise.management.support.TypeInfo;

import com.sun.enterprise.management.support.Delegate;

import com.sun.enterprise.management.support.AMXAttributeNameMapper;

/**
	JSR 77 extension representing an Appserver standalone server (non-clustered).
    
    Note that this class has a subclass:  DASJ2EEServerImpl.
 */
public  class J2EEServerImpl
	extends J2EELogicalServerImplBase
{

	/* The vendor information for this server. */
	private static final String serverVendor = "Sun Microsystems, Inc.";

		public
	J2EEServerImpl( final Delegate delegate )
	{
		super( delegate );
	}

		protected
	J2EEServerImpl( String j2eeType, Delegate delegate )
	{
		super( j2eeType, delegate );
	}


	    private J2EEServer
	getSelfProxy()
	{
	    return (J2EEServer)getSelf();
	}
	
		public String[]
	getjavaVMs()
	{
		final JVM	jvm	= getSelfProxy().getJVM();
		
		String[]	result	= null;
		if ( jvm == null )
		{
			result	= new String[ 0 ];
		}
		else
		{
			result	= new String[]	{ "" + Util.getObjectName( jvm ) };
		}
		
		return result;
	}
	
	private static final Set<String> RESOURCE_TYPES	= 
	GSetUtil.newUnmodifiableStringSet(
		J2EETypes.JDBC_RESOURCE,
		J2EETypes.JAVA_MAIL_RESOURCE,
		J2EETypes.JCA_RESOURCE,
		J2EETypes.JMS_RESOURCE,
		J2EETypes.JNDI_RESOURCE,
		J2EETypes.JTA_RESOURCE,
		J2EETypes.RMI_IIOP_RESOURCE,
		J2EETypes.URL_RESOURCE );
		
	
		public String[]
	getresources()
	{
		return GSetUtil.toStringArray( getResourcesObjectNameSet() );
	}
	
		public Set<ObjectName>
	getResourcesObjectNameSet()
	{
		return	getContaineeObjectNameSet( RESOURCE_TYPES );
	}

		public String
	getserverVersion()
	{
		return ( Version.getVersion() );
	}
	
		public boolean
	isstatisticProvider()
	{
		return( false );
	}
	
		public boolean
	isstatisticsProvider()
	{
		return isstatisticProvider();
	}
	
		public String
	getserverVendor()
	{
		return serverVendor;
	}
		
	private final static Set<String>	DONT_MAP_SET =
	    GSetUtil.newUnmodifiableStringSet( "serverVendor" );
	
		protected Set<String>
	getDontMapAttributeNames()
	{
		return( GSetUtil.newSet( DONT_MAP_SET, super.getDontMapAttributeNames() ) );
	}
	
		protected final Set<String>
	getFauxChildTypes()
	{
		final TypeInfo	j2eeApplicationInfo	=
			TypeInfos.getInstance().getInfo( J2EETypes.J2EE_APPLICATION );
	
		final Set<String>	childJ2EETypes	= j2eeApplicationInfo.getChildJ2EETypes();
		return( childJ2EETypes );
	}
	
	/*
		Override default behavior to find modules that belong to J2EEServer directly
		because J2EEApplication=null.
	 */
	 	public final Set<ObjectName>
	getContaineeObjectNameSet( final String childJ2EEType )
	{
		final Set<ObjectName>	result	= super.getContaineeObjectNameSet( childJ2EEType );
		
		if ( getFauxChildTypes().contains( childJ2EEType ) )
		{
			final String	nullAppProp	= Util.makeProp( J2EETypes.J2EE_APPLICATION, AMX.NULL_NAME );

			final Set<ObjectName>	fauxContainees	=
			    getFauxContaineeObjectNameSet( childJ2EEType, nullAppProp);
			result.addAll( fauxContainees );
		}
		
		return( result );
	}
	
	    public boolean
	getRestartRequired()
	{
        final Object result  = delegateGetAttributeNoThrow( "RestartRequired" );
        return Boolean.valueOf( "" + result );
	}
}





















