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

package com.sun.enterprise.deployment.backend;

import java.util.logging.Level;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.util.zip.ZipFileException;
import com.sun.enterprise.util.zip.ZipItem;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.ModuleEnvironment;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.instance.AppclientModulesManager;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.deployment.archivist.*;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.loader.EJBClassPathUtils;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;

public class AppClientModuleDeployer extends ModuleDeployer
{
    AppClientModuleDeployer(DeploymentRequest r) throws IASDeploymentException
    {
	super(r);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    protected BaseManager createConfigManager(InstanceEnvironment ienv, ModuleEnvironment menv) 
    throws IASDeploymentException, ConfigException
    {
	return  new AppclientModulesManager(ienv);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    protected void preDeploy() throws IASDeploymentException
    {
	assert moduleDir != null;
	assert StringUtils.ok(moduleName);
	
	try
	{
	    if(request.isArchive()) {
		J2EEModuleExploder.explodeJar(request.getFileSource().getFile(), moduleDir);
	    }

	    xmlDir.mkdirs(); 
	    jwsDir.mkdirs(); 
	}
	catch(Exception e)
	{
	    throw new IASDeploymentException(e.toString(), e);
	}
    }
    
    protected boolean needsStubs()
    {
	// override this for any module that needs stubs created
	return true;
    }    
    
    ///////////////////////////////////////////////////////////////////////////
    
    protected void deploy() throws IASDeploymentException, ConfigException
    {
        loadDescriptors();
        runVerifier();
        checkAppclientsMainClasses();
        xmlDir.mkdirs();
        // Set the generated XML directory in application desc
        request.getDescriptor().setGeneratedXMLDirectory(xmlDir.getAbsolutePath());
	ZipItem[] clientStubs = runEJBC();
	createClientJar(clientStubs);
    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    

    
    private static StringManager localStrings =
    StringManager.getManager( AppClientModuleDeployer.class );
}

