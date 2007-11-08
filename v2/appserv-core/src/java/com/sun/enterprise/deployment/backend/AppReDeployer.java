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
 * AppReDeployer.java
 *
 * Created on April 27, 2002, 3:38 PM
 * 
 * @author  bnevins
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/backend/AppReDeployer.java,v $
 *
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim -
 *             :set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *             1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *             2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *             3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 */

package com.sun.enterprise.deployment.backend;

import java.io.*;
import java.util.logging.*;
import com.sun.enterprise.instance.ApplicationEnvironment;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.util.io.FileSource;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.diagnostics.Reminder;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;
import com.sun.enterprise.deployment.Application;

public class AppReDeployer extends AppDeployer
{
	///////////////////////////////////////////////////////////////////////////
	//////////    Constructor                                //////////////////
	///////////////////////////////////////////////////////////////////////////

	AppReDeployer(DeploymentRequest r)  throws IASDeploymentException
	{
		super(r);
	}

	///////////////////////////////////////////////////////////////////////////
	//////////    Protected and Package Visibility Methods   //////////////////
	///////////////////////////////////////////////////////////////////////////

	protected void begin() throws IASDeploymentException
	{
		// this is here because by the time we get to predeploy() -- the app will no
		// longer be registered.  So we need to save the app-dir right NOW!
		
		super.begin();
		
		try
		{
                    // first let's try to get the application from the 
                    // instance manager cache
                    // if it's not there, get it from the request which 
                    // is set through deployment context cache
                    app = getManager().getDescriptor(getAppName()); 
                    if (app == null) {
                        app = request.getDescriptor(); 
                    }

                        originalAppDir = new File(DeploymentServiceUtils.getLocation(getAppName(), request.getType()));
                        getManager().unregisterDescriptor(getAppName());
                        removePolicy();
		}
		catch(Exception e)
		{
			String msg = localStrings.getString(
					"enterprise.deployment.backend.error_getting_app_location",
					getAppName() );
			throw new IASDeploymentException( msg, e);
		}
	}		

	///////////////////////////////////////////////////////////////////////////
	
	protected final void predeploy() throws IASDeploymentException
	{
		appWasUnregistered = true;
		super.predeploy();

                // send PRE_DEPLOY event so the deployment event listener
                // can do the necessary work
                DeploymentEventInfo info = new DeploymentEventInfo(
                    getAppDir(), getStubsDir(), app,
                    getRequest());
                DeploymentEvent ev = new DeploymentEvent(
                    DeploymentEventType.PRE_DEPLOY, info);
                DeploymentEventManager.notifyDeploymentEvent(ev);

		liquidate();

		getAppDir().mkdirs();
	}

	///////////////////////////////////////////////////////////////////////////

	protected final File setAppDir() throws IASDeploymentException
	{
		File newAppDir = null;
		
		if(isArchive())
		{
			newAppDir = setAppDirArchive();
		}
		else if(isDirectory())
		{
			newAppDir = setAppDirDirectory();
		}
		else
		{
			String msg = localStrings.getString(
					"enterprise.deployment.backend.redeployment_not_dir_or_archive" );
			throw new IASDeploymentException( msg );
		}
		
		return newAppDir;
	}

	///////////////////////////////////////////////////////////////////////////

	protected String whatAreYou()
	{
		return "Redeployment";
	}

	///////////////////////////////////////////////////////////////////////////
	
	private final File setAppDirDirectory() throws IASDeploymentException
	{
		FileSource fileSource = request.getFileSource(); 

		if(!fileSource.exists()) 
		{
			String msg = localStrings.getString("enterprise.deployment.backend.file_source_does_not_exist", fileSource );
			throw new IASDeploymentException( msg );
		}

		assert fileSource.isDirectory();
		File appDirectory = fileSource.getFile();

		return appDirectory;
	}

	///////////////////////////////////////////////////////////////////////////

	private final File setAppDirArchive() throws IASDeploymentException
	{
		assert originalAppDir != null;
                return originalAppDir;
        }

	///////////////////////////////////////////////////////////////////////////

	
	private String				failureMessage	= "\n*********************\n****Redeployment Failed -- rolled back redeployment";
	private String				successMessage	= "\n*********************\n****Redeployment Successful for ";
	private	File				originalAppDir					= null;
	private boolean				appWasUnregistered				= false;
        private Application app = null;
	private static StringManager localStrings =
        StringManager.getManager( AppReDeployer.class );
}
