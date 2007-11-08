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
 * AutoDirReDeployer.java
 *
 * @author bnevins
 * Simple derived class of AutoDeployer for auto-redeploy via "touch .reload"
 */

package com.sun.enterprise.deployment.autodeploy;

import java.io.File;
import java.util.Properties;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.util.io.FileSource;	
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.util.i18n.StringManager;

public class AutoDirReDeployer extends AutoDeployer
{
	public AutoDirReDeployer(DeploymentRequest req) 
	{
		if(req == null)
			throw new IllegalArgumentException("nullarg");
		
		this.req = req;
	}
	
	public boolean redeploy() throws AutoDeploymentException
	{
                boolean status = false;
                int deployResult = DEPLOY_FAILURE;
		try
		{
			init();
			verify();
			File source	= req.getFileSource().getFile();
			String name	= req.getName();

        		deployResult = deploy(source, null, name);
		}
		catch(AutoDeploymentException ade)
		{
			throw ade;
		}
		catch(Exception e)
		{
			throw new AutoDeploymentException("Error in AutoDirReDeployer.redeploy", e);
		}
                status = (deployResult == DEPLOY_SUCCESS);
                return status;
	}

	/**
	 * I had to override this method because of a bunch of AutoDeployer-specific 
	 * error handling that isn't appropriate here
	 */
	
	boolean invokeDeploymentService(File deployablefile, String action,  Object[] params, String[] signature) 
			throws AutoDeploymentException 
	{
                boolean status = false;
		try 
		{
			Object result = getMBeanServer().invoke(getMBeanName(), action, params, signature);
                        int returnStatus = parseResult(result);
                        if (returnStatus == DeploymentStatus.SUCCESS ||
                            returnStatus == DeploymentStatus.WARNING) {
                            status = true;
                        } else {
                            status = false;
                        }
		} 
		catch(AutoDeploymentException de)
		{
			throw de;
		}
		catch(Exception e) 
		{
			String msg = "Error in AutoReDeployer.invokeDeploymentService";
			throw new AutoDeploymentException(msg, e);
		} 
		return status;
    }

    protected Properties getUndeployActionProperties(String name){
        DeploymentProperties dProps = 
            (DeploymentProperties)super.getUndeployActionProperties(name);
        dProps.setReload(true);
        return (Properties)dProps;
    }

	///////////////////////////////////////////////////////////////////////////
	
	private void verify() throws AutoDeploymentException
	{
		// make sure we have valid info.

		// first -- only ejb modules and J2EE Apps are supported 
		if(!req.isApplication() && !req.isEjbModule() && !req.isWebModule())
			throw new AutoDeploymentException(getString("wrongType"));

		// make sure it is a directory.  Don't worry about NPE here!
		if(!FileUtils.safeIsDirectory(req.getFileSource().getFile()))
			throw new AutoDeploymentException(getString("notDir") + req.getFileSource().getFile().getAbsolutePath());
			
		// check that there is a valid name.
		if(!StringUtils.ok(req.getName()))
			throw new AutoDeploymentException(getString("noName"));
	}
	
	///////////////////////////////////////////////////////////////////////////

	private String getString(String s)
	{
		return localStrings.getString("enterprise.deployment.AutoDirRedeploy." + s);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	
	private			DeploymentRequest	req;
	private static	StringManager		localStrings	= StringManager.getManager(AutoDirReDeployer.class);
}

