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
 * ASSunCmpMappingTest.java
 *
 * //test class, to integrate JDOCodeGenerator validation framework
 */


package com.sun.enterprise.tools.verifier.tests.ejb.ias.cmpmapping;


import java.util.*;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.common.dd.ejb.SunEjbJar;
import com.sun.enterprise.tools.common.dd.ejb.Ejb;
import com.sun.enterprise.tools.common.dd.ResourceRef;
import com.sun.enterprise.tools.common.dd.DefaultResourcePrincipal;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.tools.verifier.tests.ejb.*;
import com.sun.enterprise.tools.common.dd.cmpmapping.*;
import com.sun.jdo.spi.persistence.support.ejb.ejbc.*;
import com.sun.enterprise.deployment.DescriptorConstants;
import com.sun.enterprise.deployment.IASEjbBundleDescriptor;
import com.sun.enterprise.deployment.IASEjbCMPEntityDescriptor;
import com.sun.ejb.codegen.GeneratorException;
import com.sun.enterprise.util.JarClassLoader;
import java.io.*;
import java.util.jar.*;
import java.util.zip.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.*;

public class ASSunCmpMappingTest extends EjbTest implements EjbCheck {

    public Result check(EjbDescriptor descriptor)
    {
	Result result = getInitializedResult();
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);
        boolean oneFailed = false;
        boolean notApp = false;
        boolean initFailed=false;
      
        if(descriptor.getEjbBundleDescriptor().getTestsDone().contains(getClass().getName()))
        {   
            result.setStatus(Result.NOT_RUN);
            result.addGoodDetails(smh.getLocalString("iasEjbJar.allReadyRun",
                "NOT RUN [AS-EJB ias-ejb-jar] cmp-mapping is a JAR Level Test. This test has already been run once"));
            return result;
        }
        descriptor.getEjbBundleDescriptor().setTestsDone(getClass().getName());
        
        IASEjbBundleDescriptor desc=(IASEjbBundleDescriptor)descriptor.getEjbBundleDescriptor();
        
        if(!mappingFileExist((EjbDescriptor)descriptor))//4703999
        {
            if(containsCMP(desc)){//4724249 additional check if the archive  contain a cmp
                result.warning(smh.getLocalString(getClass().getName()+".warning",
                "WARNING [JDO-GEN-CMP-Validation] : CMP mapping file [ {0} ] not present, in the ejb archive [ {1} ].",
                new Object[] {EjbBundleXmlReader.IAS_CMP_MAPPING_JAR_ENTRY, descriptor.getName()}));
                return result;
            }else{
                notApp = true;
                result.notApplicable(smh.getLocalString
                                 (getClass().getName() + ".notApplicable",
                                  "NOT APPLICABLE [JDO-GEN-CMP-Validation] : There is no cmp present, within the ejb archive [ {0} ].",
                                  new Object[] {descriptor.getName()}));
                return result;
                
            }    
            
            
        }    
        
        
        ClassLoader loader=getVerifierContext().getClassLoader();
        JDOCodeGenerator jdogen = new JDOCodeGenerator();
        
        Iterator ejbs=desc.getEjbs().iterator();
            //For each entity
        if(ejbs.hasNext())
        {
            try{
            jdogen.init(desc, loader);
        
            }catch(Exception re){
                oneFailed = true;
                initFailed=true;

                result.failed(smh.getLocalString(getClass().getName()+".failed2",
                            "FAILED [JDO-GEN-CMP-Validation] : Unexpected exception occured while initializing JDOCodeGenerator, for the mapping file [ {0} ], "+
                            "of the ejb archive [ {1} ] . "+
                            "Check stacktrace for details : \n [ {2} ]" ,
                            new Object[]{EjbBundleXmlReader.IAS_CMP_MAPPING_JAR_ENTRY,descriptor.getName(),re.getMessage()}));
            
            
            }
            
            while ( !initFailed && ejbs.hasNext() ) {
            EjbDescriptor ejbdesc = (EjbDescriptor) ejbs.next();
            
            if ( ejbdesc instanceof IASEjbCMPEntityDescriptor ) {
                  IASEjbCMPEntityDescriptor entd = (IASEjbCMPEntityDescriptor)ejbdesc;
                    try {
                         jdogen.validate(entd);
                         result.passed(smh.getLocalString(getClass().getName()+".passed",
                            "PASSED [JDO-GEN-CMP-Validation] : The descriptor entries, in the mapping file [ {0} ], "+
                            "for CMP [ {1} ]"+
                            "of the ejb archive [ {2} ] are valid. ",
                            new Object[]{EjbBundleXmlReader.IAS_CMP_MAPPING_JAR_ENTRY,ejbdesc.getEjbClassName(),descriptor.getName()}));
                    } catch (GeneratorException e) {
                        oneFailed = true;

                        result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [JDO-GEN-CMP-Validation] : Atleast one entry , in the mapping file [ {0} ], "+
                            "for CMP [ {1} ]"+
                            "of the ejb archive [ {2} ] is not valid. "+
                            "Check stacktrace for details : \n [ {3} ]" ,
                            new Object[]{EjbBundleXmlReader.IAS_CMP_MAPPING_JAR_ENTRY,ejbdesc.getEjbClassName(),descriptor.getName(),e.getMessage()}));
                     
                    }catch (Exception e) {
                        oneFailed = true;

                        result.failed(smh.getLocalString(getClass().getName()+".failed1",
                            "FAILED [JDO-GEN-CMP-Validation] : Unexpected exception occured while validating ,  the mapping file [ {0} ], "+
                            "for CMP [ {1} ]"+
                            "of the ejb archive [ {2} ] . "+
                            "Check stacktrace for details : \n [ {3} ]" ,
                            new Object[]{EjbBundleXmlReader.IAS_CMP_MAPPING_JAR_ENTRY,ejbdesc.getEjbClassName(),descriptor.getName(),e.getMessage()}));
                     
                        
                    }

                }
            }//end while loop
     
        }
        else{
            
          notApp = true;
          result.notApplicable(smh.getLocalString
                                 (getClass().getName() + ".notApplicable",
                                  "NOT APPLICABLE [JDO-GEN-CMP-Validation] : There is no cmp present, within the ejb archive [ {0} ].",
                                  new Object[] {descriptor.getName()}));
        }
        
        
        jdogen.cleanup();
       
        
        if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else if(notApp) {
            result.setStatus(Result.NOT_APPLICABLE);
        }else {
            result.setStatus(Result.PASSED);
        }
        return result;
    }
    
    boolean mappingFileExist(EjbDescriptor descriptor){ //4703999
            try{
                if(descriptor.getEjbBundleDescriptor().getIasCmpMappings() != null)
                    return true;
                else
                    return false;
            }catch(Exception e){
                return false;
            }
	        /*
            File f = Verifier.getJarFile(((EjbBundleArchivist) descriptor.getEjbBundleDescriptor().getArchivist()).getEjbJarFile().getName());
            JarFile jarFile = null;
            ZipEntry deploymentEntry=null;
            try {
                              jarFile = new JarFile(f);
                              if(jarFile!=null)
                              deploymentEntry = jarFile.getEntry(EjbBundleXmlReader.IAS_CMP_MAPPING_JAR_ENTRY);
                              

                  }
            catch(IOException e){}
            finally{
                           try{  if(jarFile!=null) jarFile.close();} 
                           catch(IOException e){}
             }

             if(deploymentEntry!=null)
                     return true;
             else 
                     return false;   
        
		     */
    }
    
    boolean containsCMP(IASEjbBundleDescriptor desc) //4724249
    {
        
        if(desc == null || desc.getEjbs() == null )
            return false;
        Iterator ejbs=desc.getEjbs().iterator();
        if(ejbs.hasNext())
        {
            while ( ejbs.hasNext() ) {
                EjbDescriptor ejbdesc = (EjbDescriptor) ejbs.next();
                //if IASEjbCMPEntityDescriptor then there exist a CMP in this archive
                if ( ejbdesc instanceof IASEjbCMPEntityDescriptor ) 
                    return true;
            }
        } 
        return false;      
        
    }    

    
}


