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
import com.sun.enterprise.util.diagnostics.Reminder;
import com.sun.enterprise.deployment.backend.DeployableObjectType;

/**
	Represents the environment of a standalone module to be deployed to
	given Server Instance.
	@author  Kedar
	@version 1.0
*/

public class ModuleEnvironment {
	//public  static final int		kEJBModule		= 0;
	//public  static final int		kWebModule		= 1;
	private String					mModuleName			= null;
	private String					mModulePath			= null;
	private String					mModuleBackupPath	= null;
	private String					mModuleGeneratedXMLPath		= null;
	private String					mJavaWebStartPath		= null;
	private String					mModuleStubPath		= null;
	private String					mModuleJSPPath		= null;
	private InstanceEnvironment		mInstance			= null;
	private DeployableObjectType	mType				= null;
/*
 *
    public ModuleEnvironment (InstanceEnvironment instance, String moduleName,
		int type) {
		if (instance == null || moduleName == null) {
			throw new IllegalArgumentException();
		}
		if (type!= kEJBModule && type != kWebModule) {
			throw new IllegalArgumentException();
		}
		mInstance	= instance;
		mModuleName	= moduleName;
		createModulePath();
		createModuleStubPath();
		
		if(type == kEJBModule)
			mType = DeployableObjectType.EJB;

		else if(type == kWebModule)
			mType = DeployableObjectType.WEB;
		
		else
			throw new IllegalArgumentException();
		
		Reminder.message("DEPRECTAED API -- use the other constructor for ModuleEnvironment");
    }

 */	
	/** 
		Creates new ModuleEnvironment.
		@param instance InstanceEnvironment representing the Server Instance in which this
		module is to be deployed.
		@param moduleName String representing name of this module.
		@type the type of module - has to be WEB, EJB or RES
		
		@throws IllegalArgumentException if instance or moduleName is null or
		type is not one of the supported ones.
	*/
	
    public ModuleEnvironment (InstanceEnvironment instance, String moduleName,
		DeployableObjectType type) {
		if (instance == null || moduleName == null) {
			throw new IllegalArgumentException();
		}
		
		if(type == null) {	// impossible to have an invalid module type (except null!!)
			throw new IllegalArgumentException(Localizer.getValue(ExceptionType.NULL_MODULE_TYPE));
		}
		
		mType		= type;
		mInstance	= instance;
		mModuleName	= moduleName;
		createModulePath();
		createModuleBackupPath();
		createModuleStubPath();
		createModuleJSPPath();
		createModuleGeneratedXMLPath();
		createJavaWebStartPath();
    }
	
	private void createModulePath() {
		String moduleRepositoryDirPath = mInstance.getModuleRepositoryPath ();
		String[] onlyFolderNames = new String[] {
			moduleRepositoryDirPath,
			mModuleName
			};
		mModulePath = StringUtils.makeFilePath (onlyFolderNames, false);
	}
	
	/**
		Returns the absolute path for the all the elements within this module.
		Note that it does not create/check file/directory with such a path.
	*/
	
	public String getModulePath() {
		return mModulePath;
	}
	
	/**
		Returns the absolute path for storing all the generated xml of this module.
		Note that it does not create/check file/directory with such a path.
	*/
	
	public void createModuleGeneratedXMLPath() {
		String[] onlyFolderNames = new String[] {
			mInstance.getModuleGeneratedXMLPath(),
			mModuleName
		};
		
		mModuleGeneratedXMLPath = StringUtils.makeFilePath (onlyFolderNames, false);
	}

	/**
		Returns the absolute path for the all the generated xml within this module.
		Note that it does not create/check file/directory with such a path.
	*/
	
	public String getModuleGeneratedXMLPath() {
		return mModuleGeneratedXMLPath;
	}


        /**
                Creates the path to this application's java-web-start directory.
        */

        private void createJavaWebStartPath() {
               String[] onlyFolderNames = new String[] {
                       mInstance.getJavaWebStartPath(),
                       mModuleName
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

        /**
                Returns the absolute path for storing all the stubs of this module.
                Note that it does not create/check file/directory with such a path.
        */

        public void createModuleStubPath() {
                String[] onlyFolderNames = new String[] {
                        mInstance.getModuleStubPath(),
                        mModuleName
                };

                mModuleStubPath = StringUtils.makeFilePath (onlyFolderNames, false);
        }
        /**
                Returns the absolute path for the all the stubs within this module.
                Note that it does not create/check file/directory with such a path.
        */

        public String getModuleStubPath() {
                return mModuleStubPath;
        }


	
	/**
		Creates the absolute path for storing the generated JSPs of this module.
		Note that it does not create/check file/directory with such a path.
	*/
	
	public void createModuleJSPPath() {
		String[] onlyFolderNames = new String[] {
			mInstance.getWebModuleCompileJspPath(),
			mModuleName
		};
		
		mModuleJSPPath = StringUtils.makeFilePath (onlyFolderNames, false);
	}
	/**
		Returns the absolute path for the generated JSPs from this module.
	*/
	
	public String getModuleJSPPath() {
		return mModuleJSPPath;
	}

	/**
		Returns the absolute path for the backup path for this directory-deployed module
		Note that it does not create/check file/directory with such a path.
	*/
	
	public String getModuleBackupPath() {
		return mModuleBackupPath;
	}
	/**
		Returns the absolute path for the backup path for this directory-deployed module
		Note that it does not create/check file/directory with such a path.
	*/
	
	public void createModuleBackupPath() {
		String[] onlyFolderNames = new String[] {
			mInstance.getModuleBackupRepositoryPath(),
			mModuleName
		};
		
		mModuleBackupPath = StringUtils.makeFilePath (onlyFolderNames, false);
	}


	/** return a String with the error, in English, if the required 
		directories aren't there or have a problem.
		return null if all is OK
	 */
	public String verify()
	{
			return null;
	}
}
