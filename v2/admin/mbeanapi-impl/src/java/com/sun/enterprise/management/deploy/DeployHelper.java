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
package com.sun.enterprise.management.helper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import javax.management.ObjectName;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationListener;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.Util;


import com.sun.appserv.management.helper.Helper;
import com.sun.appserv.management.helper.DeployNotificationListener;
import com.sun.appserv.management.helper.Misc;

import com.sun.appserv.management.deploy.DeploymentMgr;
import com.sun.appserv.management.deploy.DeploymentProgress;
import com.sun.appserv.management.deploy.DeploymentStatus;
import static com.sun.appserv.management.deploy.DeploymentStatus.*;
import static com.sun.appserv.management.deploy.DeploymentMgr.*;

import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.DeployedItemRefConfigCR;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.DomainConfig;



/**
	Helper class for simplifying deployment.
	@since AppServer 9.0
 */
public final class DeployHelper extends Helper
{
    private DeployNotificationListener  mListener;
    private final DeploymentMgr	        mDeploymentMgr;
    private boolean                     mDidAssociate;
    
		public
	DeployHelper( final DeploymentMgr	deploymentMgr )
	{
		super( deploymentMgr.getDomainRoot() );
		
		mDeploymentMgr   = deploymentMgr;
		mListener        = null;
		mDidAssociate    = false;
	}
	
	/**
	    To monitor progress or check on final status, 
	    use the listener.
	    @see DeployNotificationListener
	 */
	    public DeployNotificationListener
	getDeployNotificationListener()
	{
	    return mListener;
	}
	
		public final static String
	getDefaultAppName( final String archiveName )
	{
		String	result	= archiveName;
		
		final int	idx	= archiveName.lastIndexOf( "." );
		if ( idx > 1 )
		{
			result	= archiveName.substring( 0, idx );
		}
		
		return( result );
	}
	
	/**
	    Deploy the archive.
	    Subsequent progress and status should be obtained
	    from the listener returned from {@link #getDeployNotificationListener}.
	    <p>
	    For most errors, an Exception is not thrown; the returned
	    DeploymentStatus should be checked for errors using
	    {@link DeploymentStatus#getStageStatus}, verifing that 
	    {@link DeploymentStatus#STATUS_CODE_SUCCESS} was received.
        <p>
        If no targets are specified, then the app is deployed, but not
        associated with any server or cluster.
	    
	    @param archive the archive to deploy
	    @param deployOptions optional deployment options as defined by {@link DeploymentMgr}
	    @see DeploymentStatus
	    @see DeploymentProgress
	 */
	    public void
	deploy(
	    final File                archive,
	    final Map<String,String>  deployOptions )
	    throws IOException
	{
	    if ( archive == null )
	    {
	        throw new IllegalArgumentException();
	    }

	    final Object uploadID   = Misc.uploadFile(
	        getDomainRoot().getUploadDownloadMgr(),
	        archive );
	    
		final Object	deployID	= mDeploymentMgr.initDeploy( );
	    mListener   = new DeployNotificationListener( mDeploymentMgr, deployID );
		
		try
		{
			final String	archiveName	= archive.getName();
			
			final Map<String,String>   actualOptions   = new HashMap<String,String>();
			if ( deployOptions != null )
			{
			    actualOptions.putAll( deployOptions );
			}
			if ( ! actualOptions.containsKey( DEPLOY_OPTION_NAME_KEY ) )
			{
			    final String    appName = getDefaultAppName( archiveName );
			    actualOptions.put( DEPLOY_OPTION_NAME_KEY, appName );
			}
			
			mDeploymentMgr.startDeploy( deployID, uploadID, null, actualOptions );
		}
		finally
		{
			// remove right away; not all failures notify the listener
		    mListener.cleanup();
		}
	}
	
	/**
	    Wait for deployment to finish, then return status.
	    @return final status
	 */
	    public DeploymentStatus
	waitTillDone( final long pollMillis )
	{
	    // sanity check
	    if ( pollMillis > 5 * 1000 )
	    {
	        throw new IllegalArgumentException();
	    }
	    
	    // a more sophisticated solution would be to have
	    // the DeploymentNotificationListener wake us up...
	    while ( ! mListener.isCompleted() )
	    {
	        Util.sleep( pollMillis );
	    }
	    
	    final DeploymentStatus  status  = mListener.getDeploymentStatus();
	    
	    return status;
	}
	
	/**
	    Get a Set of all {@link StandaloneServerConfig} and
	    {@link ClusterConfig} corresponding to the target names.
	    @param domainRoot
	    @param names  names (eg getName()) of {@link StandaloneServerConfig} or {@link ClusterConfig}
	 */
	    public static Set<DeployedItemRefConfigCR>
	getTargetProxies(
	    final DomainRoot    domainRoot,
	    final String[]      names )
	{
	    final DomainConfig  domainConfig = domainRoot.getDomainConfig();
	    
	    final Set<DeployedItemRefConfigCR>  result  = new HashSet<DeployedItemRefConfigCR>();
	    
	    final Map<String,StandaloneServerConfig> serverConfigs = 
	        domainConfig.getStandaloneServerConfigMap();
	        
	    final Map<String,ClusterConfig> clusterConfigs =
	        domainConfig.getClusterConfigMap();
	        
	    for( final String name : names )
	    {
	        if ( serverConfigs.containsKey( name ) )
	        {
	            result.add( serverConfigs.get( name ) );
	        }
	        else if ( clusterConfigs.containsKey( name ) )
	        {
	            result.add( clusterConfigs.get( name ) );
	        }
	    }
	    
	    return result;
	}
	
	/**
	    Associate the specified targets with the deployed application by 
	    creating {@link DeployedItemRefConfig} configuration within the targets.
	    Call only after deployment has finished, and call it only once.
	    
	    @param targets  names (eg getName()) of {@link StandaloneServerConfig} or {@link ClusterConfig}
	    @param refOptions  options to be passed when creating
	                    {@link DeployedItemRefConfig} (may be null)
	 */
	    public Set<DeployedItemRefConfig>
	createReferences(
	    final String[]             targets,
	    final Map<String,String>   refOptions )
	{
	    if ( targets == null || targets.length == 0 )
	    {
	        throw new IllegalArgumentException();
	    }

	    if ( mListener == null || ! mListener.isCompleted() )
	    {
	        throw new IllegalStateException();
	    }
	    
	    final DeploymentStatus  status  = mListener.getDeploymentStatus();
	    
	    if ( status == null || status.getStageStatus() == STATUS_CODE_FAILURE )
	    {
	        throw new IllegalStateException();
	    }
	    
	    if ( mDidAssociate )
	    {
	        throw new IllegalStateException();
	    }
	    
	    // OK, go ahead
		final Map<String,Serializable>    additionalStatus = status.getAdditionalStatus();
		final String moduleID	= (String)additionalStatus.get( MODULE_ID_KEY );
	
	    final Set<DeployedItemRefConfig>   refs = new HashSet<DeployedItemRefConfig>();
	    final Set<DeployedItemRefConfigCR>  proxies = getTargetProxies( getDomainRoot(), targets );
	    for( final DeployedItemRefConfigCR cr : proxies )
	    {   
	        final DeployedItemRefConfig ref =
	            cr.createDeployedItemRefConfig( moduleID, refOptions );
	        refs.add( ref );
	    }
	    
	    return refs;
	}
	
	/**
	    Deploy the archive and associate it with all specified targets.
	    Calls {@link #deploy},
	    {@link #waitTillDone}, and 
	    {@link #createReferences}.
	    
	    @param archive the archive to deploy
	    @param deployOptions optional deployment options as defined by {@link DeploymentMgr}
	    @param targets  names (eg getName()) of
	                {@link StandaloneServerConfig} or {@link ClusterConfig}
	    @param refOptions  options for creating references, see {@link DeployedItemRefConfigCR}
	    @return final status
	 */
	    public DeploymentStatus
	deploy(
	    final File                archive,
	    final Map<String,String>  deployOptions,
	    final String[]            targets,
	    final Map<String,String>  refOptions )
	    throws IOException
	{
	    deploy( archive, deployOptions );
	    
	    final DeploymentStatus  status  = waitTillDone( 50 );
	    
	    if ( targets != null && targets.length != 0 )
	    {
	        createReferences( targets, refOptions );
	    }
	    
	    return status;
	}
	
	/**
	    Perform default deployment.  Calls deploy( archive, null, targets, null );
	    @param archive
	    @param targets
	    @return final status
	 */
	    public DeploymentStatus
	deploy(
	    final File                archive,
	    final String[]            targets )
	    throws IOException
	{
	    return deploy( archive, null, targets, null );
	}
}





























