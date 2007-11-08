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
package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.util.Utility;
import com.sun.logging.*;

import java.io.*;
import java.util.logging.*;

/**
 * The class contains all the information relative to a particular deployment
 * Its lifetime isl imited to the actual deployment process.
 *
 * @author  Jerome Dochez
 * @version 
 */
public class DeploymentContext extends java.lang.Object {

    /**
     * the application descriptor we are deploying
     */
    private Application application;
    
    /**
     * Archive abstraction of the deployable application
     */
    private AbstractArchive archive=null;
    
    /**
     * Temporary directory used to generating the implementation classes
     */
    private File appTmpDir=null;
    

	static Logger _logger = LogDomains.getLogger(LogDomains.DPL_LOGGER);
    
    /** 
     * Creates new DeploymentContext 
     * 
     * @param application descriptor to be deployed
     */
    public DeploymentContext(AbstractArchive archive, Application application) {
        this.application = application;
        this.archive = archive;
    }

    /**
     * @return the Application object we are deploying
     */
    public Application getApplication() {
        return application;
    }
    
    /**
     * @return the archive abstraction used to represent the application
     * bunlde being deployed
     */
    public AbstractArchive getArchive() {
        return archive;
    }
    
    /**
     * @return the tmp output directory used to generate files that will be
     * added later to the cooked archive file
     */
    public File getOutputDirectory() {
        return new File(archive.getArchiveUri());
    }
    
    /**
     * @return the source directory where the content of this deployment has
     * been saved in 
     */ 
    public File getSourceDirectory() {
        return new File(archive.getArchiveUri());
    }    
    
    /**
     * @return the class loader that should be used for loading classes during
     * deployment
     */
    public ClassLoader getClassLoader() {
        return application.getClassLoader();
    }
       
    /**
     * delete all content of a given directory
     */
    public static void deleteDirectory(File dir) {
	File[] files = dir.listFiles();
	if (files != null) {
	    for(int i=0; i < files.length; ++i) {
		File child = files[i];
		if(child.isDirectory()) {
		    deleteDirectory(child);
		} 
		child.delete();
	    }
	}
	dir.delete();
    }
    
}
