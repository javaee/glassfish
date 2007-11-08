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

package com.sun.enterprise.tools.verifier.tests.ejb.ias;

import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import java.io.*;
import java.util.jar.*;
import java.util.zip.*;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import org.xml.sax.*;
import org.w3c.dom.*;

import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;

import com.sun.enterprise.deployment.xml.IASEjbRuntimeDescriptorNode;
/** DOCTYPE
 *
 * The ias ejb deployment descriptor has PUBLIC identifier with a PubidLiteral
 * of an acceptable type
 * @author Irfan Ahmed
 */
public class ASEjbJarPublicID extends EjbTest implements EjbCheck { 

    /** 
     * Ejb PUBLIC identifier test
     * The ejb deployment descriptor has PUBLIC identifier with a PubidLiteral 
     * of "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN" 
     *
     * @param descriptor the Ejb deployment descriptor 
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();

	String acceptablePubidLiterals[] = {
            IASEjbRuntimeDescriptorNode.PUBLIC_DTD_ID};
	String acceptableURLs[] = {IASEjbRuntimeDescriptorNode.SYSTEM_ID};
				               
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);

        // open the jar and read the Ias XML deployment descriptor
        if(!descriptor.getEjbBundleDescriptor().getTestsDone().contains(getClass().getName()))
        {
            descriptor.getEjbBundleDescriptor().setTestsDone(getClass().getName());
            File f = Verifier.getJarFile(((EjbBundleArchivist) descriptor.getEjbBundleDescriptor().getArchivist()).getEjbJarFile().getName());
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(f);
                ZipEntry deploymentEntry = 
                    jarFile.getEntry(EjbBundleArchivist.IAS_DEPLOYMENT_DESCRIPTOR_ENTRY);
                if (deploymentEntry != null) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(jarFile.getInputStream(deploymentEntry)));
                    String s = in.readLine();
                    boolean foundDOCTYPE = false, foundPubid = false, foundURL = false;

                    while(s != null) {
                        if(s.indexOf("DOCTYPE") > -1)
                            foundDOCTYPE = true;
                        if(foundDOCTYPE){
                            for (int i=0;i<acceptablePubidLiterals.length;i++) {
                                if (s.indexOf(acceptablePubidLiterals[i]) > -1) {
                                    foundPubid = true;
                                    result.addGoodDetails(smh.getLocalString
                                                          ("tests.componentNameConstructor",
                                                           "For [ {0} ]",
                                                           new Object[] {compName.toString()}));
                                    result.addGoodDetails
                                        (smh.getLocalString
                                         (getClass().getName() + ".passed1", 
                                          "PASSED [AS-EJB ] : The iAS deployment descriptor has the proper PubidLiteral: {0}", 
                                          new Object[] {acceptablePubidLiterals[i]})); 
                                }
                                //check if the URLs match as well  
                                if (s.indexOf(acceptableURLs[i]) > -1) {
                                    foundURL = true;
                                    result.addGoodDetails(smh.getLocalString
                                                          ("tests.componentNameConstructor",
                                                           "For [ {0} ]",
                                                           new Object[] {compName.toString()}));
                                    result.addGoodDetails
                                        (smh.getLocalString
                                         (getClass().getName() + ".passed2", 
                                          "PASSED [AS-EJB] : The iAS deployment descriptor has the proper URL corresponding the the PubIdLiteral: {0}", 
                                          new Object[] {acceptableURLs[i]})); 
                                }
                            }
                        }
                        if(foundPubid && foundURL) {
                            result.setStatus(Result.PASSED);
                            break;
                        } else if(foundDOCTYPE && s.endsWith(">")) break; // DOCTYPE doesn't have any more lines to check
                        s = in.readLine();
                    }

                    if(!foundDOCTYPE){
                        result.addErrorDetails(smh.getLocalString
                                               ("tests.componentNameConstructor",
                                                "For [ {0} ]",
                                                new Object[] {compName.toString()}));
                        result.failed
                            (smh.getLocalString
                             (getClass().getName() + ".failed1", 
                              "FAILED [AS-EJB] :  No document type declaration found in the deployment descriptor for {0}",
                              new Object[] {descriptor.getName()}));
                    } else if(!foundPubid) {
                        result.addErrorDetails(smh.getLocalString
                                               ("tests.componentNameConstructor",
                                                "For [ {0} ]",
                                                new Object[] {compName.toString()}));
                        result.failed
                            (smh.getLocalString
                             (getClass().getName() + ".failed2", 
                              "FAILED [AS-EJB ejb] : The deployment descriptor for {0} does not have an expected PubidLiteral ",
                              new Object[] {descriptor.getName()}));
                    } else if (!foundURL){
                        result.addErrorDetails(smh.getLocalString
                                               ("tests.componentNameConstructor",
                                                "For [ {0} ]",
                                                new Object[] {compName.toString()}));
                        result.failed(smh.getLocalString
                                      (getClass().getName() + ".failed", 
                                       "The deployment descriptor {0} doesnot have the right URL corresponding to the PubIdLiteral", 
                                       new Object[] {descriptor.getName()})); 
                    }
                }
                jarFile.close();

            } catch (IOException e) {
                result.addErrorDetails(smh.getLocalString
                                       ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                result.failed(smh.getLocalString
                              (getClass().getName() + ".IOException", 
                               "Error: I/O error trying to open {0}", new Object[] {f.getAbsolutePath()}));
            }
        }
        else
        {
            result.setStatus(Result.NOT_RUN);
            result.addGoodDetails(smh.getLocalString(getClass().getName(),
                "[AS-EJB] JAR Level test. Already done"));
        }
	return result;
    }
}

