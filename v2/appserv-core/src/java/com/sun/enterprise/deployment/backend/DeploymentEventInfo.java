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
 * DeploymentEventInfo.java
 *
 * Created on April 21, 2003.
 */

package com.sun.enterprise.deployment.backend;

import java.io.File;

import com.sun.enterprise.deployment.Application;

/**
 * A <code>DeploymentEventInfo</code> is used to store all necessary
 * information for the <code>DeploymentEvent</code> event that gets delivered 
 * whenever a <code>DeploymentEventListener</code> registers itself with the 
 * <code>DeploymentEventManager</code>.
 *
 * This class does not have setXXX methods as all the values are set
 * during instance construction.
 * @author Marina Vatkina
 */
public class DeploymentEventInfo
{
    /** The directory where the archive has been exploded */
    private File srcDir;

    /** The directory where all the code is generated and being compiled */
    private File stubsDir;

    /** The Application deployment descriptor object */
    private Application application;

    /** The deployment request object */
    private DeploymentRequest request;

    /** Constructs <code>DeploymentEventInfo</code> from all neccessary
     * elements.
     * @param srcDir the directory where the archive has been exploded.
     * @param stubsDir the directory where all the code is generated and being compiled.
     * @param application the Application deployment descriptor object for the event.
     * @param request the deployment request object.
     */
    public DeploymentEventInfo(File srcDir, File stubsDir, Application application, 
        DeploymentRequest request) {

        this.srcDir = srcDir;
        this.stubsDir = stubsDir;
        this.application = application;
        this.request = request;
       
    }

     /**
      * Constructs DeploymentEventInfo with request object
      * @param DeploymentRequest
      * @return DeploymentEventInfo
      */
     public DeploymentEventInfo(DeploymentRequest request) {
         this.request = request;
     }


    /** Returns the directory where the archive has been exploded.
     * @return directory where the archive has been exploded.
     */
    public File getSrcDir() {
        return srcDir;
    }

    /** Returns the directory where all the code is generated and being compiled.
     * @return directory where all the code is generated and being compiled.
     */
    public File getStubsDir() {
        return stubsDir;
    }

    /** Returns the Application deployment descriptor object.
     * @return application deployment descriptor object.
     */
    public Application getApplicationDescriptor() {
        return application;
    }

    /** Returns the deployment request object.
     * @return request the deployment request object.
     */  
    public DeploymentRequest getDeploymentRequest() {
        return request;
    }

}
