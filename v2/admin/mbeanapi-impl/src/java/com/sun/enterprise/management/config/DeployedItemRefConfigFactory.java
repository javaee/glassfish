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
package com.sun.enterprise.management.config;

import java.util.Set;
import java.util.Map;
import java.util.Iterator;

import java.io.Serializable;

import javax.management.ObjectName;
import javax.management.AttributeList;
import javax.management.RuntimeOperationsException;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.TypeCast;

import com.sun.enterprise.management.support.oldconfig.OldApplicationsConfigMBean;

import com.sun.appserv.management.deploy.DeploymentStatus;
import com.sun.appserv.management.deploy.DeploymentSupport;

import com.sun.appserv.management.config.DeployedItemRefConfigCR;

/**
	Base impl class for &lt;application-ref&gt;
*/
public class DeployedItemRefConfigFactory  extends ConfigFactory
{
	private final OldApplicationsConfigMBean	mOldApplicationsConfigMBean;
	
		public
	DeployedItemRefConfigFactory(
		final ConfigFactoryCallback callbacks)
	{
		super( callbacks );
		
		mOldApplicationsConfigMBean	= getOldConfigProxies().getOldApplicationsConfigMBean();
	}
	
	private final Set<String>	LEGAL_OPTIONAL_KEYS	= 
		GSetUtil.newUnmodifiableStringSet(
		DeployedItemRefConfigCR.ENABLED_KEY,
		DeployedItemRefConfigCR.VIRTUAL_SERVERS_KEY,
		DeployedItemRefConfigCR.LB_ENABLED_KEY,
		DeployedItemRefConfigCR.DISABLE_TIMEOUT_IN_MINUTES_KEY );
	
	    protected Set<String>
	getLegalOptionalCreateKeys()
	{
		return( LEGAL_OPTIONAL_KEYS );
	}
	
	    
        protected Map<String,String> 
	getParamNameOverrides()
	{
		return( MapUtil.newMap( CONFIG_NAME_KEY, "ref" ) );
	}
                
	public ObjectName create(
		final String referencedApplicationName,
		final Map<String,String> optional )
	{
		trace( "DeployedItemRefConfigFactory.create: creating using: ");

		final Map<String,String> params	= initParams( referencedApplicationName, null, optional );
		
		trace( "params as processed: " + stringify( params ) );

		final ObjectName	amxName	= createNamedChild( referencedApplicationName, params );

		return( amxName );
	}
          
	public ObjectName create(
		final String referencedApplicationName, 
		final boolean enabled,
		final String virtualServers,
		final boolean lbEnabled,
		final int disableTimeoutInMinutes)
	{
		final Map<String,String> optionalParams = new java.util.HashMap<String,String>();
		putNonNull( optionalParams, DeployedItemRefConfigCR.ENABLED_KEY,Boolean.toString(enabled));
		putNonNull( optionalParams, DeployedItemRefConfigCR.VIRTUAL_SERVERS_KEY,virtualServers);
		putNonNull( optionalParams, DeployedItemRefConfigCR.LB_ENABLED_KEY,Boolean.toString(lbEnabled));
		putNonNull( optionalParams, DeployedItemRefConfigCR.DISABLE_TIMEOUT_IN_MINUTES_KEY,Integer.toString(disableTimeoutInMinutes));

		final ObjectName amxName =  create(referencedApplicationName, optionalParams);
		return( amxName );
	}

        
		public ObjectName
	create(final String referencedApplicationName)
	{
		return create(referencedApplicationName, null);
	}

        
		public void
	internalRemove( final ObjectName objectName )
	{
		final String containerName = getFactoryContainer().getName();

		mOldApplicationsConfigMBean.deleteApplicationReferenceAndReturnStatusAsMap( 
				containerName, Util.getName( objectName ), null );
	}

		protected ObjectName
	createOldChildConfig( final AttributeList translatedAttrs )
	{
		trace( "createOldChildConfig: attrs: " + stringify( translatedAttrs ) );

		final String REF_KEY	= "ref";

		final Map<String,String> attributeMap = JMXUtil.attributeListToStringMap( translatedAttrs );

		String appRef	= null;
		try
		{
			appRef = attributeMap.remove( REF_KEY );
		}
		catch ( UnsupportedOperationException uoe )
		{
			assert false;
		}
		assert appRef != null;

		final String target		= getFactoryContainer().getName();
		assert target != null;

		final ObjectName on = createApplicationRef( appRef, target, attributeMap );

		return on;
	}

		private ObjectName 
	createApplicationRef( final String ref, final String target, final Map<String,String> optional )
	{
		Map<String,Serializable> m = TypeCast.checkMap(
		    mOldApplicationsConfigMBean.
		        createApplicationReferenceAndReturnStatusAsMap( target, ref, optional ),
		        String.class,
		        Serializable.class );
		checkDeploymentStatusForExceptions( m );

		final String targetJ2EEType	= getFactoryContainer().getJ2EEType();

		ObjectName on = null;
		if ( XTypes.STANDALONE_SERVER_CONFIG.equals( targetJ2EEType ) )
		{
			on = getOldConfigProxies().getOldServerMBean( target ).
				getApplicationRefByRef( ref );
		}
		else if ( XTypes.CLUSTER_CONFIG.equals( targetJ2EEType ) )
		{
			on = getOldConfigProxies().getOldClusterMBean( target ).
				getApplicationRefByRef( ref );
		}
		else
		{
			throw new RuntimeException(
			"Application refs can be created only on clusters and standalone servers" );
		}

		return on;
	}

		private void 
	checkDeploymentStatusForExceptions( Map<String,Serializable> m )
    {
		DeploymentStatus status = DeploymentSupport.mapToDeploymentStatus( m );
		Throwable t = status.getStageThrowable();
		final Iterator<DeploymentStatus> it = status.getSubStagesList().iterator();
		while ( ( t == null ) && ( it.hasNext() ) )
		{
			status = it.next();
			t = status.getThrowable();
		}
		if ( null != t )
		{
			throw new RuntimeException( status.getStageStatusMessage() );
		}
    }
}















