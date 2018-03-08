/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.DomainRoot;
import java.io.*;
import java.util.*;

import javax.management.Notification;
import javax.management.NotificationListener;

import com.sun.appserv.management.deploy.DeploymentMgr;
import com.sun.appserv.management.deploy.DeploymentProgress;
import com.sun.appserv.management.deploy.DeploymentStatus;
import com.sun.appserv.management.deploy.DeploymentSupport;

import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.DeployedItemRefConfigCR;
import com.sun.appserv.management.deploy.DeploymentSourceImpl;
import com.sun.appserv.management.j2ee.J2EEDomain;
import com.sun.appserv.management.j2ee.J2EELogicalServer;


/**
 */
public class DeployCmd extends BaseCmd
{
    public static final String kArchive     = "archive";
    public static final String kTarget      = "target";
    public static final String kDeployOptions     = "deployOptions";

    public DeployCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        deploy();
        associate();
        startApp();

        return null;
    }

    public Object upload(InputStream is, final long fileSize) throws Exception
    {
        try
        {
		    final DeploymentMgr	mgr	= getDeploymentMgr();
		    assert( mgr != null );
		
            final int	totalSize	= (int)fileSize;
            final int	chunkSize	= 32 * 1024;
		
            final Object	uploadID	= mgr.initiateFileUpload( totalSize );
            int remaining	= totalSize;
            boolean	done	= false;
            while ( remaining != 0 )
            {
                final int	actual	= remaining < chunkSize ? 
                        remaining : chunkSize;
                
                final byte[]	bytes	= new byte[ actual ];
                try
                {
                    is.read(bytes);
                }
                catch (EOFException eofe)
                {
                    done = true;
                    break;
                }
                done	= mgr.uploadBytes( uploadID, bytes );
                remaining	-= actual;
            }
            assert( done );
		
            return( uploadID );
        }
        finally
        {
            //nothing
        }
    }

    protected void deploy() throws Exception
    {
        BufferedInputStream bis = null;
        DeployListener myListener = null;;
        DeploymentMgr mgr = null;
        try
        {
            final File archive = getArchive();
            mgr = getDeploymentMgr();
			
            myListener = new DeployListener();
            mgr.addNotificationListener(myListener, null, null);

            final Object    deployID    = mgr.initDeploy( );
            assert( deployID instanceof String );
			
			if(archive.isDirectory())
			{
				DeploymentSourceImpl dsi = new DeploymentSourceImpl(archive.getAbsolutePath(), true,
					new String[1], new String[1], new String[1], new HashMap());			
	            mgr.startDeploy( deployID, dsi.asMap(), null, getDeploymentOptions());
			}
			else
			{
				bis = new BufferedInputStream(new FileInputStream(archive));
				Object uploadID = upload(bis, archive.length());
	            mgr.startDeploy( deployID, uploadID, null, getDeploymentOptions());
			}

            
            while ( ! myListener.isCompleted() )
            {
                try
                {
                    Thread.sleep( 2000 );
                }
                catch( InterruptedException e )
                {
                }
            }
            checkFailed(checkForException(myListener.getFinalStatus()));
        }
        finally
        {
            if (bis != null) { bis.close(); }
			
			if(myListener != null && mgr != null)
            {
                try
                {
                    mgr.removeNotificationListener(myListener);
                }
                catch(javax.management.ListenerNotFoundException e)
                {
                    System.err.println("Couldn't remove listener!");
                }
            }
        }
    }

    protected void associate() throws Exception
    {
        final String target = getTarget();
        if (!DEFAULT_DEPLOY_TARGET.equals(target)) //not a "domain".
        {
            final DeployedItemRefConfig refConfig = 
                getDeployedItemRefConfigCR(target).createDeployedItemRefConfig(getAppName());
            assert refConfig != null;
        }
    }


    protected void startApp() throws Exception
    {
        final String target = getTarget();
        if (!DEFAULT_DEPLOY_TARGET.equals(target)) //not a "domain".
        {
            J2EELogicalServer	server		= getLogicalServer();
            server.startApp(getAppName(), null);
        }
    }
	
    protected void stopApp() throws Exception
    {
        final String target = getTarget();
        if (!DEFAULT_DEPLOY_TARGET.equals(target)) //not a "domain".
        {
            J2EELogicalServer	server		= getLogicalServer();
            server.stopApp(getAppName(), null);
        }
    }

	 
    protected DeployedItemRefConfigCR getDeployedItemRefConfigCR()
        throws Exception
    {
		return getDeployedItemRefConfigCR(getTarget());
	}
	
    protected DeployedItemRefConfigCR getDeployedItemRefConfigCR(String target)
        throws Exception
    {
		final AMXConfig clusterOrServer = getClusterOrServer(target);
        assert clusterOrServer != null;
        DeployedItemRefConfigCR mgr = null;
        if (clusterOrServer instanceof StandaloneServerConfig)
        {
            mgr = (StandaloneServerConfig)clusterOrServer;
        }
        else if (clusterOrServer instanceof ClusterConfig)
        {
            mgr = (ClusterConfig)clusterOrServer;
        }
        else
        {
            throw new Exception("Invaid deployment target: " + 
                    "Target has to be a cluster or standalone server");
        }
        assert mgr != null;
        return mgr;
    }

    protected AMXConfig getClusterOrServer(String target) throws Exception
    {
        final DomainConfig domainConfig = getDomainConfig();
       	final Map standAloneServers  = domainConfig.getStandaloneServerConfigMap();
       	final Map clusters           = domainConfig.getClusterConfigMap();
       	final Map clusteredServers   = domainConfig.getClusteredServerConfigMap();

       	AMXConfig proxy = null;
       	if (standAloneServers.containsKey(target))
       	{
            proxy = (AMXConfig)standAloneServers.get(target);
       	}
       	else if (clusters.containsKey(target))
       	{
            proxy = (AMXConfig)clusters.get(target);
       	}
       	else
       	{
            //assert !clusteredServers.containsKey(target);
            proxy = (AMXConfig)clusteredServers.get(target);
       	}
       	return proxy;
    }

    protected DeploymentMgr getDeploymentMgr() throws Exception
    {
        return( getDomainRoot().getDeploymentMgr() );
    }

    protected File getArchive()
    {
        final String file = (String)getCmdEnv().get(kArchive);
        assert file != null;
        final File f = new File(file);
        assert (f.exists() && f.canRead());
        return f;
    }

    protected String getAppName()
    {
        String name = null;
        final Map deploymentOptions = getDeploymentOptions();
        if (null != deploymentOptions)
        {
            name = (String)deploymentOptions.get(
                DeploymentMgr.DEPLOY_OPTION_NAME_KEY);
        }
        if (null == name)
        {
            name = getArchive().getName();
            //strip the extension
            name = name.substring(0, name.lastIndexOf('.'));
            System.err.println("App name=" + name);
        }
        return name;
    }

    static final String DEFAULT_DEPLOY_TARGET = "domain";

    protected String getTarget()
    {
        final String target = (String)getCmdEnv().get(kTarget);
        return (null == target) ? DEFAULT_DEPLOY_TARGET : target;
    }

    protected Map getDeploymentOptions()
    {
        return (Map)getCmdEnv().get(kDeployOptions);
    }

	J2EELogicalServer getLogicalServer() throws Exception
    {
		J2EELogicalServer	server		= null;
		J2EEDomain			domain		= getJ2EEDomain();
		Map					serverMap	= domain.getServerMap();
		Map					clusterMap	= domain.getClusterMap();
		String				target		= getTarget();
		
		serverMap.putAll(clusterMap);
		
		if(!serverMap.containsKey(target))
        {
			throw new RuntimeException("Can't find target: " + getTarget());
        }

		return (J2EELogicalServer)serverMap.get(target);
    }

    public static Throwable checkForException(final DeploymentStatus s)
    {
        Throwable ex = s.getThrowable();
        final Iterator it = s.getSubStages();
        while ((ex == null) && (it.hasNext()))
        {
            final DeploymentStatus subStage = 
                DeploymentSupport.mapToDeploymentStatus((Map)it.next());
            ex = subStage.getThrowable();
        }
        return ex;
    }

    public void checkFailed(final Throwable t) throws Exception
    {
        if ( t != null ) 
        {
            if ( t instanceof Exception )
            {
                throw ( Exception )t;
            }
            else
            {
                throw new Exception( t );
            }
        }
    }
}
