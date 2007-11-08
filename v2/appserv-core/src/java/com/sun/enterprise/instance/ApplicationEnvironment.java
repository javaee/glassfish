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

package com.sun.enterprise.instance;

//iAS imports
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.diagnostics.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
/**
	A class that provides the environment for an Application deployed within
	a given Server Instance. Provides convenience methods for the entire application,
	modules within.
	
	@author  Kedar
	@version 1.0
*/

public class ApplicationEnvironment {

	private String					mAppName		= null;
	private String					mAppPath		= null;
	private String					mAppGeneratedXMLPath	= null;
	private String					mAppStubPath	= null;
	private String					mAppBackupPath	= null;
	private String					mAppJSPPath		= null;
        private String                                  mJavaWebStartPath = null;
	private InstanceEnvironment		mInstance		= null;
    private static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
		
	
	/** 
		Creates new ApplicationEnvironment for given InstanceEnvironment
		and given application name.
		@param instance InstanceEnvironment where the application is deployed.
		@param appName String representing the name of the application.
	*/

    public ApplicationEnvironment (InstanceEnvironment instance, String appName) {
		if (instance == null || appName == null) {
			throw new IllegalArgumentException();
		}
		mInstance	= instance;
		mAppName	= appName;
		createAppPath();
		createAppStubPath();
		createAppBackupPath();
		createAppJSPPath();
		createAppGeneratedXMLPath();
                createJavaWebStartPath();
    }
	
	private void createAppPath() {
		String applicationRepositoryDirPath = mInstance.getApplicationRepositoryPath();
		String[] onlyFolderNames = new String[] {
			applicationRepositoryDirPath,
			mAppName
			};
		mAppPath = StringUtils.makeFilePath (onlyFolderNames, false);
	}

	private void createAppBackupPath() {
		// This is used to keep a copy of directory-deployed app files
		String applicationBackupRepositoryDirPath = mInstance.getApplicationBackupRepositoryPath();
		String[] onlyFolderNames = new String[] {
			applicationBackupRepositoryDirPath,
			mAppName
			};
		mAppBackupPath = StringUtils.makeFilePath (onlyFolderNames, false);
	}
	
	/**
		Returns the absolute path for the all the elements within this application.
		Note that it does not create/check file/directory with such a path.
	*/
	
	public String getAppPath() {
		return mAppPath;
	}
	
	/**
		Returns the absolute path for storing all the stubs of this application.
		Note that it does not create/check file/directory with such a path.
	*/
	
	public void createAppStubPath() {
		String[] onlyFolderNames = new String[] {
			mInstance.getApplicationStubPath(),
			mAppName
		};
		
		mAppStubPath = StringUtils.makeFilePath (onlyFolderNames, false);
	}

        /**
                Returns the absolute path for storing all the generated xml of this application.
                Note that it does not create/check file/directory with such a path.
        */

        public void createAppGeneratedXMLPath() {
                String[] onlyFolderNames = new String[] {
                        mInstance.getApplicationGeneratedXMLPath(),
                        mAppName
                };

                mAppGeneratedXMLPath = StringUtils.makeFilePath (onlyFolderNames, false);
        }

	
	/**
		Creates the absolute path String for storing all the generated JSPs of 
		this application.
		Note that it does not create/check file/directory with such a path.
	*/
	
	public void createAppJSPPath() {
		String[] onlyFolderNames = new String[] {
			mInstance.getApplicationCompileJspPath(),
			mAppName
		};
		
		mAppJSPPath = StringUtils.makeFilePath (onlyFolderNames, false);
	}

	/**
		Returns the absolute path for the all the stubs within this application.
		Note that it does not create/check file/directory with such a path.
	*/
	
	public String getAppJSPPath() {
		assert StringUtils.ok(mAppJSPPath);
		return mAppJSPPath;
	}

	/**
		Returns the absolute path for the all the stubs within this application.
		Note that it does not create/check file/directory with such a path.
	*/
	
	public String getAppStubPath() {
		return mAppStubPath;
	}

        /**
                Returns the absolute path for the all the generated xml within this application.
                Note that it does not create/check file/directory with such a path.
        */

        public String getAppGeneratedXMLPath() {
                return mAppGeneratedXMLPath;
        }
        
        /**
                Creates the path to this application's java-web-start directory.
        */
        
        private void createJavaWebStartPath() {
		String[] onlyFolderNames = new String[] {
			mInstance.getJavaWebStartPath(),
			mAppName
		};
		
		mJavaWebStartPath = StringUtils.makeFilePath (onlyFolderNames, false);
        }
        
        /**
                Returns the absolute path to the Java Web Start directory for this application.
                Note that it does not create/check file/directory with such a path.
        */
        
        public String getJavaWebStartPath() {
            return mJavaWebStartPath;
        }
        
	public String toString()
	{
		return ObjectAnalyzer.toString(this);
	}
	
	public static void main(String[] args)
	{
		//System.setProperty("com.sun.aas.instanceRoot", "/usr/appserv/instances");
		InstanceEnvironment env = new InstanceEnvironment("foo");
		ApplicationEnvironment ae = new ApplicationEnvironment(env, "myapp");

		_logger.log(Level.INFO,"core.appenv_dump");
		_logger.log(Level.INFO,ae.toString());
	}
}
