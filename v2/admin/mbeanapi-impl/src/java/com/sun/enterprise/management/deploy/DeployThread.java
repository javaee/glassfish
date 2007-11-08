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
 
package com.sun.enterprise.management.deploy;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import java.io.Serializable;

import javax.management.Notification;

import com.sun.appserv.management.deploy.DeploymentSource;
import com.sun.appserv.management.deploy.DeploymentStatus;
import com.sun.appserv.management.deploy.DeploymentStatusImpl;
import com.sun.appserv.management.util.misc.TypeCast;

import com.sun.enterprise.deployment.phasing.DeploymentService;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;
import com.sun.enterprise.deployment.backend.DeploymentRequestRegistry;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.deployment.util.DeploymentProperties;


public final class DeployThread extends Thread
{
    private DeployThreadParams mParams;
    private final Object mDeployID;
    private String moduleID;
    private final DeploymentCallback mDeploymentCallback;
	
    private boolean mQuit;
    private boolean mDone;
    private long mDoneMillis;
    private DeploymentStatusImpl mDeploymentStatus;
	
    private final List<Notification> mQueuedNotifications;

	
    public DeployThread(final Object id, 
        final DeploymentCallback deploymentCallback,
        final DeployThreadParams params ) {
        mDeployID = id;
        mDeploymentCallback = deploymentCallback;
        mParams = params;
	
        mDeploymentStatus = null;
	mDone = false;
		
        mQuit = false;
		
        mDoneMillis = 0;
		
        mQueuedNotifications = new ArrayList<Notification>();
		
    }
	
    public void setParams( final DeployThreadParams params ) {
        if ( mParams != null ) {
            throw new IllegalArgumentException();
        }

        mParams = params;
    }


    public void queueNotification( final Notification notif ) {
        if ( isDone() ) {
            throw new IllegalArgumentException(
               "Notification cannot be queued after being done for: " + 
               mDeployID);
        }

        synchronized( mQueuedNotifications ) {
            mQueuedNotifications.add( notif );
        }
    }
	
    public Notification[] takeNotifications(  ) {
        Notification[] notifs = null;
		
        synchronized( mQueuedNotifications ) {
            notifs = new Notification[ mQueuedNotifications.size() ];
            mQueuedNotifications.toArray( notifs );
            mQueuedNotifications.clear();
        }
		
	return( notifs );
    }

    public DeploymentStatus getDeploymentStatus( ) {
        return( mDeploymentStatus );
    }

	
    // set a flag in DeploymentRequest, and the backend will check 
    // if the flag is set to determine whether or not to abort
    public boolean quit() {
        mQuit = true;
        return DeploymentService.getDeploymentService().quit(moduleID);
    }
	
    private void trace( Object o )
    {
    	final String	name	= this.getClass().getName();
        Logger.getLogger( name ).fine( o.toString() );
    }

    private DeploymentStatusImpl deploy (final DeployThreadParams params,
        final DeploymentCallback callback) {
        try { 
            Map options = params.getOptions();
            
            File deployFile = params.getDeployFile();
            File planFile = params.getPlanFile();

            if (deployFile == null) {
                DeploymentSource deploySource = params.getDeploymentSource();
                if (deploySource != null) {
                    deployFile = deploySource.getArchive();
                }
                DeploymentSource deployPlan = params.getDeploymentPlan();
                if (deployPlan != null) {
                    planFile = deployPlan.getArchive();
                }
            }

            DeploymentProperties dProps = new DeploymentProperties(options);
            String archiveName = dProps.getArchiveName();
            if (archiveName == null && deployFile != null) {
                archiveName = deployFile.getAbsolutePath();
                if (deployFile.isDirectory()) {
                    moduleID = dProps.getName(archiveName);
                } else {
                    //In case the archive was passed in as an MemoryArchive, 
                    //ie. no file name available to a default moduleID, ask 
                    //the backend to supply a computed moduleID (using display 
                    //name in dd)
                    String computedID = 
                        DeploymentService.getDeploymentService().getModuleIDFromDD(deployFile);
                    moduleID = dProps.getProperty(
                        DeploymentProperties.NAME, computedID);
                }
            } else {
                moduleID = dProps.getName(archiveName);
            }

            com.sun.enterprise.deployment.backend.DeploymentStatus ds = DeploymentService.getDeploymentService().deploy(deployFile, planFile, archiveName, moduleID, dProps, callback);
            ds.setStageStatus(ds.getStatus()); // This is done for the AMX clients to get complete status
            
            DeploymentRequestRegistry.getRegistry().removeDeploymentRequest(
                moduleID);
            
            final Map<String,Serializable> m    =
                TypeCast.checkMap( ds.asMap(), String.class, Serializable.class );
            
            return new DeploymentStatusImpl( m );
        } catch(Exception e) {
            com.sun.enterprise.deployment.backend.DeploymentStatus ds =
                new com.sun.enterprise.deployment.backend.DeploymentStatus();
            ds.setStageException(e);
            ds.setStageStatus(com.sun.enterprise.deployment.backend.DeploymentStatus.FAILURE);      
            ds.setStageStatusMessage(e.getMessage());
            ds.setStageDescription("Deployment");
            DeploymentRequestRegistry.getRegistry().removeDeploymentRequest(
                moduleID); 
            
            final Map<String,Serializable> m    =
                TypeCast.checkMap( ds.asMap(), String.class, Serializable.class );
            return new DeploymentStatusImpl( m );
        }   
    }
	
    public void run() {
	mDone = false;

        trace( "DeployThread.run: starting: " + getID() );

        if ( mParams == null ) {
            throw new IllegalArgumentException( "no params specified" );
        }
               
	trace( "DeployThread.run: calling deploy() for: " + getID() );
	mDeploymentStatus = deploy( mParams, mDeploymentCallback );
	trace( "DeployThread.run: deploy() successful for: " + getID()  );

	mDeploymentCallback.deploymentDone( mDeploymentStatus );
		
	// success or failure, always kill the file
	if ( mParams.getDeployFile() != null ) {
	    trace( "DeployThread.run: deleting deploy file: " + mParams.getDeployFile() );
	    mParams.getDeployFile().delete();
	}
		
	// success or failure, always kill the file
	if ( mParams.getPlanFile() != null ) {
	    trace( "DeployThread.run: deleting plan file: " + mParams.getPlanFile() );
	    mParams.getPlanFile().delete();
	}
		
	mDoneMillis	= System.currentTimeMillis();
	mDone	= true;
		
        trace( "DeployThread.run: done with: " + getID() );
    }
	
    /**
        @return the number of milliseconds since the deploy finished
    */
    public long getMillisSinceDone() {
        return( isDone() ? (System.currentTimeMillis() - mDoneMillis) : 0  );
    }
	
    /**
        @return the ID of this DeployThread
     */
    public Object getID() {
        return( mDeployID );
    }
	
	
    /**
	@return true if done (success or failure)
    */
    public boolean isDone() {
        return( mDone );
    }
}














