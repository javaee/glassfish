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
 * AppDeployer.java
 *
 * Created on December 11, 2001, 5:37 PM
 */

package com.sun.enterprise.deployment.backend;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.deploy.shared.FileArchive; 
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.instance.ApplicationEnvironment;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.util.io.FileSource;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.zip.ZipFileException;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.diagnostics.Reminder;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;

/** AppUnDeployer is responsible for Undeploying Applications.
 *
 * @author  bnevins
 * @version
 */

public class AppUnDeployer extends AppDeployerBase
{
	AppUnDeployer(DeploymentRequest r)  throws IASDeploymentException
	{
		super(r);
	}

	///////////////////////////////////////////////////////////////////////////

	public void doRequest() throws IASDeploymentException
	{
		doRequestPrepare();
		doRequestFinish();
	}

	///////////////////////////////////////////////////////////////////////////

	public void doRequestPrepare() throws IASDeploymentException
	{
		try
		{
			begin();
		}
		catch(IASDeploymentException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			String msg = localStrings.getString(
					"enterprise.deployment.backend.dorequest_exception");
			logger.log(Level.WARNING, msg, e);
			throw new IASDeploymentException(msg, e);
		}
	}

	///////////////////////////////////////////////////////////////////////////

	public void doRequestFinish() throws IASDeploymentException
	{
		try
		{
			predeploy();
			localBegin();	// sets cmp drop table variables
			undeploy();
    removePolicy();    
		}
		catch(IASDeploymentException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			String msg = localStrings.getString(
					"enterprise.deployment.backend.dorequest_exception");
			logger.log(Level.WARNING, msg, e);
			throw new IASDeploymentException(msg, e);
		}
		finally
		{
			finish();
		}
	}

	/**
     * Attempt to delete the deployed directory-tree. 
     * <p> This method call is <b>guaranteed</b> to never throw any kind of Exception
     */
    public void cleanup_internal() {
        try {
            if(isMaybeCMPDropTables)
                dropTables();
            
            liquidate();
        }
        catch(Exception e) {
            logger.warning("Caught an Exception in cleanup_internal: " + e);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    protected final File setAppDir() throws IASDeploymentException {
        try {
            return new File(DeploymentServiceUtils.getLocation(getAppName(), 
                request.getType()));
        }
        catch(Exception e) {
            String msg = localStrings.getString(
            "enterprise.deployment.backend.error_getting_app_directory",
            e);
            throw new IASDeploymentException(msg, e);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void undeploy() throws IASDeploymentException {
        try {
            getManager().unregisterDescriptor(getAppName());
            addToSummary(successMessage + getAppName());
        }
        catch(Exception e) {
            if(e instanceof IASDeploymentException)
                throw (IASDeploymentException)e;
            else
                throw new IASDeploymentException(e);
        }
    }
    
    /**
     * localBegin -- if we need to drop cmp tables later, create an Application
     * object NOW before the registration information disappears.  The reason we don't
     * drop the tables now is that it can NOT be rolled back.  So we delay doing it as long
     * as possible -- which is just before the files are deleted.
     */
    private void localBegin() {
        // Why is this stuff complicating localBegin() when it could simply be called
        // from cleanup_internal()?  Because there may be problems with doing the actual
        // drop tables late in the process (namely after unregistering.  If that turns out to be the
        // case then the drop tables call will be moved here.  If not, we can clean this code up by
        // moving it into dropTables() later.
        // bnevins April 2003
        
        try {
            if(getRequest().isMaybeCMPDropTables()) {
                isMaybeCMPDropTables = true;

                //  first let's try to get the application from the
                // instance manager cache
                // if it's not there, get it from the request which
                // is set through deployment context cache
                applicationDD = getManager().getRegisteredDescriptor(getAppName());
                if (applicationDD == null) {
                    applicationDD = request.getDescriptor();
                }
            }
        }
        catch(Exception e) {
            logger.warning("Caught an Exception in localBegin: " + e);
        }
    }
    
    /**
     * Call into CMP to drop tables.  This is called just before files are deleted as part
     * of the cleanup() call.  We do it very late in the process since dropping tables
     * can't be rolled-back.  Thus all of the other steps that can have errors that require a
     * rollback have already been done successfully - or we wouldn't be here.
     * bnevins April 2003
     * <p> This method call is <b>guaranteed</b> to never throw any kind of Exception
     */
    private void dropTables() {
        assert isMaybeCMPDropTables; // programmer error if this is false!
        
        try {
            DeploymentEventInfo info = new DeploymentEventInfo(getAppDir(),
                getStubsDir(), applicationDD, getRequest());
            DeploymentEvent ev = new DeploymentEvent(DeploymentEventType.PRE_UNDEPLOY, info);
            DeploymentEventManager.notifyDeploymentEvent(ev);
        }
        catch(Throwable t) {
            logger.warning("Caught a Throwable in dropTables: " + t);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private String				failureMessage			= "Undeployment Failed -- rolled back undeployment";
    private String				successMessage			= "Undeployment Successful for ";
    private boolean				isMaybeCMPDropTables	= false;
    private Application			applicationDD			= null;
    private static StringManager localStrings			= StringManager.getManager( AppUnDeployer.class );
}
