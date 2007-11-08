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
 * @(#) EjbcContextImpl.java
 *
 */

package com.sun.enterprise.deployment.backend;

import java.io.File;
import java.util.List;
import java.util.Properties;
import com.sun.ejb.codegen.EjbcContext;
import com.sun.ejb.codegen.IASEJBCTimes;
import com.sun.enterprise.loader.EJBClassLoader;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.deployment.Application;


/**
 * Ejbc runtime environment created by deployment backend. This class 
 * contains all the information needed by ejbc for a particular deployment. 
 *
 * @author Nazrul Islam
 * @since  JDK 1.4
 */
public class EjbcContextImpl implements EjbcContext {

	/**
	 * Do-nothing constructor.
	 */
	EjbcContextImpl()
	{
	}

	/**
	 * @author bnevins
	 * I added this constructor to get rid of the long, error-prone sequence of setter commands
	 * sprinkled in the code to create instances for CMP usage.  They all use the
	 * same sequences, so it may as well be a constructor that has some smarts...
	 */
	
	EjbcContextImpl(File srcDir, File stubsDir, Application dd, DeploymentRequest request)
	{
		setSrcDir(srcDir);
		setStubsDir(stubsDir);
		setDeploymentRequest(request);
		setOptionalArguments(request.getOptionalArguments());
		setDescriptor(dd);
	}
	
	
    /**
     * Returns the current directory where the archive has been exploded.
     *
     * @return    the directory where the archive has been exploded
     */
    public File getSrcDir() {
        return this._srcDir;
    }

    /**
     * Sets the directory where the archive has been exploded.
     *
     * @param    srcDir    current directory where the archive has been exploded
     */
    void setSrcDir(File srcDir) {
        this._srcDir = srcDir;
    }

    /**
     * Returns the (current stubs) directory where all the code is 
     * generated and being compiled for this archive.
     *
     * @return  the current stubs directory for this archive
     */
    public File getStubsDir() {
        return this._stubsDir;
    }

    /**
     * Sets the current stubs directory.
     *
     * @param    stubsDir    current stubs directory
     */
    void setStubsDir(File stubsDir) {
        this._stubsDir = stubsDir;
    }

    /**
     * Returns the object representation of the deployment descriptor 
     * for the current archive.
     *
     * @return    the deployment descriptor for the current archive
     */
    public Application getDescriptor() {
        return this._application;
    }

    /**
     * Sets the deployment descriptor object.
     * 
     * @param    application    deployment descriptor object for the
     *                          current archive
     */
    void setDescriptor(Application application) {
        this._application = application;
    }

    /**
     * Returns the class paths need by this archive to compile the 
     * generated src. This includes class-path prefix, class-path 
     * of the target instance, common class loader paths, shared 
     * class loader paths and the ejb class loader paths.
     *
     * @return    the ejb class paths for this archive
     */
    public String[] getClasspathUrls() {
        return this._classpathUrls;
    }

    /**
     * Sets the ejb class paths.
     * 
     * @param    classpathUrls   the ejb class paths for this archive
     */
    void setClasspathUrls(String[] classpathUrls) {
        this._classpathUrls = classpathUrls;
    }

    /**
     * Returns the RMIC options as defined in the instance's server
     * configuration. 
     *
     * The default is: 
     *    "-iiop -poa -alwaysgenerate -keepgenerated -g"
     *
     * @return   the RMIC options
     */
    public List getRmicOptions() {
        return this._rmicOptions;
    }

    /**
     * Sets the RMIC options.
     *
     * @param    rmicOptions    RMIC options
     */
    void setRmicOptions(List rmicOptions) {
        this._rmicOptions = rmicOptions;
    }

    /**
     * Returns the JAVAC options as defined in the instance's server
     * configuration. 
     * 
     * The default is: "-g"
     *
     * @return  the JAVAC options
     */
    public List getJavacOptions() {
        return this._javacOptions;
    }

    /**
     * Sets the JAVAC options.
     *
     * @param    javacOptions   JAVAC options 
     */
    void setJavacOptions(List javacOptions) {
        this._javacOptions = javacOptions;
    }

    /**
     * Returns the timing information for the sub-tasks of ejbc.
     * 
     * @return  timing information for the sub-tasks of ejbc
     */
    public IASEJBCTimes getTiming() {
        return this._timing;
    }

    /**
     * Sets the timing for ejbc.
     *
     * @param    timing    object encapsulating the timing information
     *                     for the sub-tasks of ejbc
     */
    void setTiming(IASEJBCTimes timing) {
        this._timing = timing;
    }

    /**
     * Returns the ejb class paths of this archive.
     *
     * @return    the ejb class paths for this archive
     */
    public String[] getEjbClasspathUrls() {
        return this._ejbClasspathUrls;
    }

    /**
     * Sets the ejb class path for this deployment. 
     * This is the ejb part of the archive and 
     * does not have any other class paths. 
     * 
     * This method is used during re-deployment optimization in ejbc.
     */
    void setEjbClasspathUrls(String[] paths) {
        this._ejbClasspathUrls = paths;
    }

    /**
     * Returns the deployment mode, i.e., archive layout.
     * Default is EXPLODED mode.
     *
     * @return    deployment mode
     */
    public DeploymentMode getDeploymentMode() {
        return this._deploymentMode;
    }

    /**
     * Sets the deployment mode.
     *
     * @param    mode    deployment mode to be used for this deployment
     */
    void setDeploymentMode(DeploymentMode mode) throws IASDeploymentException {
        if (mode == null) {
            String msg = localStrings.getString(
                        "enterprise.deployment.backend.invalid_deployment_mode",
                        "null" );
            throw new IASDeploymentException(msg);
        }
        this._deploymentMode = mode;
    }

    /**
     * Returns the optional arguments - which currently consists of 
	 * CMP-specific deployment arguments
     *
     * @return    optional attributes
     */
	public Properties getOptionalArguments()
	{
		return _optionalArguments;
	}
	
    /**
     * Sets the optional attributes
	 * @see setOptionalAttributes
     * @param  optionalAttributes   the optional attributes as name-value pairs
     */
	void setOptionalArguments(Properties p)
	{
		_optionalArguments = p;
	}

	/** 
	 * Returns the original DeploymentRequest object for the current deployment
	 * @return    Deployment Request object
	 */
	public DeploymentRequest getDeploymentRequest()
	{
		return _request;
	}

	/** 
	 * Sets the original DeploymentRequest object for the current deployment
	 * @param  request  Deployment Request object
	 */
	void setDeploymentRequest(DeploymentRequest request)
	{
		_request = request;
	}
	
    // ---- INSTANCE VARIABLE(S) - PRIVATE ----------------------------------

    /** current directory where the ear file has been exploded */
    private File _srcDir                    = null;

    /** current stubs directory for the archive */
    private File _stubsDir                  = null;

    /** obj representation of deployment descriptor */
    private Application _application        = null;

    /** class paths for this archive */
    private String[] _classpathUrls         = null;

    /** ejb class paths for this archive */
    private String[] _ejbClasspathUrls      = null;

    /** RMIC options */
    private List _rmicOptions               = null;

    /** JAVAC options */
    private List _javacOptions              = null;

    /** timing information for the sub-tasks of ejbc */
    private IASEJBCTimes _timing            = null;

    /** deployment mode */
    private DeploymentMode _deploymentMode  = DeploymentMode.EXPLODED;

    /** optional attributes */
    private Properties		_optionalArguments = null;

    /** Deployment Request object */
    private DeploymentRequest	_request = null;

    /** i18n string manager */
    private static StringManager localStrings =
            StringManager.getManager( EjbcContextImpl.class );
}
