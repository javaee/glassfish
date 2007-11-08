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


package com.sun.enterprise.tools.verifier;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;

/**
 * The Verifier Interface for use by the Appserver Deployment Backend
 */
public interface AppVerifyInterface {

   /**
    * Verify an Application Archive (.ear). 
    * @param application, the application descriptor
    * @param fileArchive, the Abstract archive representing the EAR 
    * @param outputDir, the directory to which the verifier output should be written
    *, this argument is optional and the user can choose to pass NULL. 
    * If this argument is NULL, the result would go to the verifier default 
    * output directory "/var/tmp/verifier-results".
    * @param cl, an initialized ClassLoader which verifier can use to load classes 
    * from the exploded Archive.
    * @return the ResultReport, the result
    **/

 public VerifierResults verifyEar(Application application,AbstractArchive fileArchive,
                                     String outputDir, ClassLoader cl);

   /**
    * Verify StandAlone Archive (.war/rar/jar).
    * @param application, the  descriptor
    * @param fileArchive, the Abstract archive representing the EAR 
    * @param outputDir, the directory to which the verifier output should be written
    *, this argument is optional and the user can choose to pass NULL. 
    * If this argument is NULL, the result would go to the verifier default 
    * output directory "/var/tmp/verifier-results".
    * @param cl, an initialized ClassLoader which verifier can use to load classes 
    * from the exploded Archive.
    * @return the ResultReport, the result
    **/
 public VerifierResults verifyStdAloneArchive(RootDeploymentDescriptor desc,
                                              AbstractArchive fileArchive, 
                                              String outputDir, ClassLoader cl);
}
