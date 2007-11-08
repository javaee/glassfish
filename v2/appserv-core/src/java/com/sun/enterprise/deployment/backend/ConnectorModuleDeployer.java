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
 * ConnectorModuleDeployer.java
 *
 * Created on January 15, 2002, 3:37 PM
 * 
 * @author  bnevins
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/backend/ConnectorModuleDeployer.java,v $
 *
 */

package com.sun.enterprise.deployment.backend;

import java.io.IOException;
import java.io.File;
import java.util.logging.Level;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ConnectorModule;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.instance.ConnectorModulesManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.ModuleEnvironment;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils; 
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.zip.ZipFileException;

public class ConnectorModuleDeployer extends ModuleDeployer
{
	ConnectorModuleDeployer(DeploymentRequest r) throws IASDeploymentException
	{
		super(r);
	}

	///////////////////////////////////////////////////////////////////////////

	protected BaseManager createConfigManager(InstanceEnvironment ienv, ModuleEnvironment menv) 
            throws IASDeploymentException, ConfigException
	{
        connModulesMgr = new ConnectorModulesManager(ienv);
		return connModulesMgr;
	}
	
    	/**
	 * @return the module classpath
	 */
	protected List getModuleClasspath(Archivist archivist,
                AbstractArchive archive) throws IASDeploymentException {
	    
	    return new ArrayList();
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected void preDeploy() throws IASDeploymentException
	{
		assert moduleDir != null;
		assert StringUtils.ok(moduleName);

		try
		{
			if(request.isArchive())
			{
				J2EEModuleExploder.explode(request.getFileSource().getFile(), moduleDir, moduleName);
			}
                        xmlDir.mkdirs();
		}
		catch(Exception e)
		{
			throw new IASDeploymentException(e.toString(), e);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////

	protected void deploy() throws IASDeploymentException, ConfigException
	{
            runVerifier();

            try {
                // copy xml files to generated/xml directory
                // this work around should be removed once connector team
                // provide a unified way to copy necessary files to
                // generated/xml directroy.
      	        String appDir = 
                    request.getDeployedDirectory().getCanonicalPath();
	        String generatedXMLDir = 
                    request.getGeneratedXMLDirectory().getCanonicalPath();

 	        FileArchive srcArchive = new FileArchive();
 	        srcArchive.open(appDir);
 			
     	        FileArchive destArchive = new FileArchive();
 	        destArchive.open(generatedXMLDir); 

                Archivist.copyExtraElements(srcArchive, destArchive);
	    } catch (Exception e) {
                throw new IASDeploymentException(e.getCause());
            }
        }
        // END OF IASRI 4686190

    private			ConnectorModulesManager connModulesMgr	= null;
    private static	StringManager			localStrings	= StringManager.getManager(ConnectorModuleDeployer.class);
}

