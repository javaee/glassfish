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
 * DeployerFactory.java
 *
 * Created on December 11, 2001, 6:25 PM
 */

package com.sun.enterprise.deployment.backend;

import com.sun.enterprise.util.i18n.StringManager;

/**
 *
 * @author  bnevins
 * @version 
 */
public class DeployerFactory 
{
	private static StringManager localStrings =
		StringManager.getManager( DeployerFactory.class );

	private DeployerFactory() 
	{
	}
	
	///////////////////////////////////////////////////////////////////////////

	public static Deployer getDeployer(DeploymentRequest request) throws IASDeploymentException
	{
		assert request != null;
		request.verify();
		
		if(request.isApplication())
			return getAppDeployer(request);
		else if(request.isEjbModule())
			return new EjbModuleDeployer(request);
		else if(request.isWebModule())
			return new WebModuleDeployer(request);
		else if(request.isConnectorModule())
			return new ConnectorModuleDeployer(request);
		else if (request.isAppClientModule())
		{
			if(request.isDirectory())
			{
				String msg = localStrings.getStringWithDefault
				(
					"enterprise.deployment.backend.DirDeployOfAppClient",
					"App Client Directory-Deployment not supported"
				);
				throw new IASDeploymentException(msg);
			}
			else
				return new AppClientModuleDeployer(request);	
		}
		else {
			String msg = localStrings.getString(
					"enterprise.deployment.backend.deployment_not_supported" );
			throw new IASDeploymentException( msg );
		}
	}
	
	///////////////////////////////////////////////////////////////////////////

	public static Deployer getAppDeployer(DeploymentRequest request) throws IASDeploymentException
	{
		if(request.isDeploy())
		{
			return new AppDeployer(request);
		}
		else if(request.isReDeploy())
		{
			return new AppReDeployer(request);
		}
		else if(request.isUnDeploy())
		{
			return new AppUnDeployer(request);
		}
		else {
			String msg = localStrings.getString(
            "enterprise.deployment.backend.unknown_deployment_request_type" );
			throw new IASDeploymentException( msg );
		}
	}
}
