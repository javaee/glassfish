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
 * @(#) EjbcContext.java
 *
 */
package com.sun.ejb.codegen;

import java.io.File;
import java.util.*;

import com.sun.enterprise.deployment.backend.DeploymentMode;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.Application;


/**
 * Ejbc runtime environment created by deployment backend. This class 
 * contains all the information needed by ejbc for a particular deployment. 
 *
 * @author Nazrul Islam
 * @since  JDK 1.4
 */
public interface EjbcContext {

    /**
     * Returns the current directory where the archive has been exploded.
     *
     * @return    the directory where the archive has been exploded
     */
    public File getSrcDir();

    /**
     * Returns the (current stubs) directory where all the code is 
     * generated and being compiled for this archive.
     *
     * @return  the current stubs directory for this archive
     */
    public File getStubsDir();

    /**
     * Returns the object representation of the deployment descriptor 
     * for the current archive.
     *
     * @return    the deployment descriptor for the current archive
     */
    public Application getDescriptor();

    /**
     * Returns the class paths need by this archive to compile the 
     * generated src. 
     *
     * @return    the class paths used by javac & rmic for this archive
     */
    public String[] getClasspathUrls();

    /**
     * Returns the ejb class paths of this archive.
     *
     * @return    the ejb class paths for this archive
     */
    public String[] getEjbClasspathUrls();

    /**
     * Returns the RMIC options as defined in the instance's server
     * configuration. 
     *
     * @return   the RMIC options
     */
    public List getRmicOptions();

    /**
     * Returns the JAVAC options as defined in the instance's server
     * configuration. 
     * 
     * @return  the JAVAC options
     */
    public List getJavacOptions();

    /**
     * Returns the timing information for the sub-tasks of ejbc.
     * 
     * @return  timing information for the sub-tasks of ejbc
     */
    public IASEJBCTimes getTiming();

    /**
     * Returns the deployment mode, i.e., archive layout.
     *
     * @return    deployment mode
     */
    public DeploymentMode getDeploymentMode();
    /**
     * Returns the optional arguments - which currently consists of 
	 * CMP-specific deployment arguments
     *
     * @return    optional attributes
     */
    public Properties getOptionalArguments();
    /**
     * Returns the original DeploymentRequest object for the current deployment
     *
     * @return    Deployment Request object
     */
    public DeploymentRequest getDeploymentRequest();
    
}
