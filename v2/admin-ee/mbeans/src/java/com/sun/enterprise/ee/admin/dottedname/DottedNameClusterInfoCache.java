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

package com.sun.enterprise.ee.admin.dottedname;

import com.sun.enterprise.admin.dottedname.DottedNameLogger;
import com.sun.enterprise.admin.dottedname.DottedNameServerInfo;
import com.sun.enterprise.admin.util.ArrayConversion;

import java.util.Set;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;

/**
 * Maintains a cache of the current cluster info as an optimization.
 * The user should call refresh() prior to first use, and when wishing to get
 * the current state.
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Jun 2, 2004
 * @version $Revision: 1.1.1.1 $
 */
public class DottedNameClusterInfoCache implements DottedNameClusterInfo{
	final DottedNameClusterInfo	mSrc;

	Set			mConfigNames;
	Set			mClusterNames;
    HashMap     mClusterToServers;
    HashMap		mClusterToConfig;
	HashMap		mConfigToClusters;
    HashMap     mResourceToTargets;
    HashMap     mAppToTargets;

    public DottedNameClusterInfoCache( final DottedNameClusterInfo src ){
		mSrc	= src;
        // underlying source may or may not be ready yet, so it's up
		// to the caller to call refresh() before first use.
		mConfigNames	        = Collections.EMPTY_SET;
		mClusterNames	        = Collections.EMPTY_SET;

		mClusterToConfig   = new HashMap();
        mConfigToClusters  = new HashMap();
        mResourceToTargets = new HashMap();
        mAppToTargets      = new HashMap();
        mClusterToServers  = new HashMap();
	}


    public synchronized Set getConfigNames()
		throws DottedNameServerInfo.UnavailableException {

        return( mConfigNames );
	}

    public synchronized Set	getClusterNames()
            throws DottedNameServerInfo.UnavailableException {

        return( mClusterNames );
	}

    public synchronized String getConfigNameForCluster( final String clusterName )
		throws DottedNameServerInfo.UnavailableException {

        return( (String)mClusterToConfig.get( clusterName ) );
	}

    public synchronized String[] getClusterNamesForConfig( final String configName )
		throws DottedNameServerInfo.UnavailableException	{

        return( (String [])mConfigToClusters.get( configName ) );
	}

    public synchronized String[] getServerNamesForCluster(final String clusterName)
    	throws DottedNameServerInfo.UnavailableException	{

        return (String[]) mClusterToServers.get(clusterName );
    }

    public boolean isClusteredInstance(String serverName) {
        return mSrc.isClusteredInstance(serverName);
    }

    public synchronized Set getResourceNamesForCluster(final String clusterName)
        throws DottedNameServerInfo.UnavailableException	{

        try {
           return (mSrc.getResourceNamesForCluster(clusterName));
        } catch (Exception e) {
            throw new DottedNameServerInfo.UnavailableException(e);
        }
    }

    public synchronized Set getApplicationNamesForCluster(final String clusterName)
        throws DottedNameServerInfo.UnavailableException	{

            try {
                return mSrc.getApplicationNamesForCluster(clusterName);
            } catch (Exception e) {
                throw new DottedNameServerInfo.UnavailableException(e);
            }
        }

    public synchronized String[] getTargetsSharingResource(final String resourceName)
        throws DottedNameServerInfo.UnavailableException {

        try {
            return mSrc.getTargetsSharingResource(resourceName);
        } catch (Exception e) {
            throw new DottedNameServerInfo.UnavailableException(e);
        }
    }

    public String[] getTargetsSharingConfig(final String configName)
            throws DottedNameServerInfo.UnavailableException {

        try {
            return mSrc.getTargetsSharingConfig(configName);
        } catch (Exception e) {
            throw new DottedNameServerInfo.UnavailableException(e);
        }

    }

    public synchronized String[] getTargetsSharingApplication(final String appName)
            throws DottedNameServerInfo.UnavailableException {

        try {
            return mSrc.getTargetsSharingApplication(appName);
        } catch (Exception e) {
            throw new DottedNameServerInfo.UnavailableException(e);
        }
    }

    public synchronized void refresh() {
		try {
			_refresh();
		}
		catch( DottedNameServerInfo.UnavailableException e ){
			DottedNameLogger.logException( e );
		}
	}

    void _refresh()	throws DottedNameServerInfo.UnavailableException {
        try {
            resetCache();
            mConfigNames	= mSrc.getConfigNames();
            mClusterNames	= mSrc.getClusterNames();
            createConfigClusterMappings();
            //createResourcesTargetsMapping();
            //createAppsTargetsMapping();
            createClusterToServersMapping();
        } catch (Exception e) {
            throw new DottedNameServerInfo.UnavailableException(e);
        }
	}

    private void resetCache() {
        	// underlying source may or may not be ready yet, so it's up
		// to the caller to call refresh() before first use.
		mConfigNames	        = Collections.EMPTY_SET;
		mClusterNames	        = Collections.EMPTY_SET;

		mClusterToConfig.clear();
        mConfigToClusters.clear();
        mResourceToTargets.clear();
        mAppToTargets.clear();
        mClusterToServers.clear();
    }

    private void createConfigClusterMappings()  {
        try{
        // create mapping from cluster to config
            Iterator	iter	= mClusterNames.iterator();
            while ( iter.hasNext() ) {
                final String	clusterName	= (String)iter.next();

                final String	configName	= mSrc.getConfigNameForCluster( clusterName );

                if ( configName != null ) {
                    mClusterToConfig.put( clusterName, configName );
                }
            }

            // create mapping from config to clusters
            iter	= mConfigNames.iterator();
            while ( iter.hasNext() ) {
                final String	configName	= (String)iter.next();
                final String [] clusterNames	= mSrc.getClusterNamesForConfig( configName );

                if ( clusterNames.length >0) {
                    mConfigToClusters.put( configName, clusterNames );
                }
            }
        }
        catch(Exception e){
              DottedNameLogger.logException(e);
        }
    }

    private void createResourcesTargetsMapping() {
    // create mapping from resources to all targets
        try{
            final String[] resNames = ((DottedNameClusterInfoImpl)mSrc).getAllResourceNames();
            String[] targets;
            for(int i=0;i < resNames.length;i++){
                targets = mSrc.getTargetsSharingResource(resNames[i]);
                if(targets != null) {
                    mResourceToTargets.put(resNames[i], targets);
                }
            }
        }
        catch(Exception e){
            DottedNameLogger.logException(e);
        }
    }

    private void createAppsTargetsMapping() {
    // create mapping from apps to all targets
        try{
            final String[] appNames = ((DottedNameClusterInfoImpl)mSrc).getAllApplicationNames();
            String[] targets;
            for(int i=0;i<appNames.length;i++){
                targets = mSrc.getTargetsSharingApplication(appNames[i]);
                if(targets != null) {
                    mAppToTargets.put(appNames[i], targets);
                }
            }
        }
        catch(Exception e){
            DottedNameLogger.logException(e);
        }
    }

    private void createClusterToServersMapping() {
        //create mapping for a cluster to its servers
        try{
            final Iterator iter = getClusterNames().iterator();
            String clusterName;
            String[] serverNames;
            while(iter.hasNext()){
                clusterName = (String)iter.next();
                serverNames = mSrc.getServerNamesForCluster(clusterName);
                if(serverNames != null){
                    mClusterToServers.put(clusterName, serverNames);
                }
            }
        }
        catch(Exception e) {
            DottedNameLogger.logException(e);
        }
    }
}
