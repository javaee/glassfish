package com.sun.enterprise.tools.verifier.tests.ejb.ias;

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

import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import java.util.*;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;

import com.sun.enterprise.tools.common.dd.ejb.*;
import com.sun.enterprise.tools.common.dd.*;
import java.io.*;
import java.util.jar.*;
import java.util.zip.*;
import com.sun.enterprise.deployment.*;

/** enterprise-beans
 *    pm-descriptors ?
 *        pm-descriptor
 *            pm-identifier [String]
 *            pm-version [String]
 *            pm-config ? [String]
 *            pm-class-generator ? [String]
 *            pm-mapping-factory ? [String]
 *        pm-inuse
 *            pm-identifier [String]
 *            pm-version [String]
 *
 * The pm-descriptors element contains one or more pm-descriptor elements
 * The pm-descriptor describes the properties for the persistence
 * manager associated with the entity bean.
 * The pm-identifier and pm-version fields are required and should not be null.
 * the pm-config should be a valid ias-cmp-mapping descriptor
 *
 * The pm-inuse identifies the persistence manager in use at a particular time.
 * The pm-identifier and pm-version should be from the pm-descriptor
 * element.
 * @author Irfan Ahmed
 */
public class ASEntBeanPmDescriptors extends EjbTest implements EjbCheck { 
    private boolean oneFailed=false;//4698035
    private boolean oneWarning=false;

    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);

        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        String ejbName = null;
        
        
        
        if(descriptor.getEjbBundleDescriptor().getTestsDone().contains(getClass().getName()))
        {
            result.setStatus(Result.NOT_RUN);
            result.addGoodDetails(smh.getLocalString("enterpriseBeans.allReadyRun",
                "NOT RUN [AS-EJB enterprise-beans] pm-descritors test is a JAR Level Test. This test has already been run once"));
            return result;
        }
        descriptor.getEjbBundleDescriptor().setTestsDone(getClass().getName());
        
        if(ejbJar!=null)
        {
            PmDescriptors pmDescs = ejbJar.getEnterpriseBeans().getPmDescriptors();
            if(pmDescs!=null)
            {
                PmDescriptor[] pmDesc = pmDescs.getPmDescriptor();
                Map pmIdVer = new HashMap();
                for(int i=0;i<pmDesc.length;i++)
                {
                    testPmDescriptor(pmDesc[i],result,pmIdVer,descriptor);
                }
                
                PmInuse pmInUse = pmDescs.getPmInuse();
                String pmIdentifier = pmInUse.getPmIdentifier();
                String pmVersion = pmInUse.getPmVersion();
                if(pmIdVer.containsKey(pmIdentifier))
                {
                    result.passed(smh.getLocalString(getClass().getName()+".passed",
                        "PASSED [AS-EJB pm-inuse] : pm-identifier {0} is valid"
                        ,new Object[]{pmIdentifier}));
                    String testVersion = (String)pmIdVer.get(pmIdentifier);
                    if(testVersion.equals(pmVersion))
                    {
                        result.passed(smh.getLocalString(getClass().getName()+".passed1",
                            "PASSED [AS-EJB pm-inuse] : pm-version {0} is valid", 
                            new Object[]{pmVersion}));
                    }
                    else
                    {
                        // <addition> srini@sun.com Bug: 4698038
                        //result.failed(smh.getLocalString(getClass().getName()+".failed",
                        //  "FAILED [AS-EJB pm-inuse] : pm-version {0} for pm-identifier {0} not defined in pm-descriptors"
                        //, new Object[]{pmVersion, pmIdentifier}));
                        result.failed(smh.getLocalString(getClass().getName()+".failed",
                          "FAILED [AS-EJB pm-inuse] : pm-version {0} for pm-identifier {1} not defined in pm-descriptors"
                        , new Object[]{pmVersion, pmIdentifier}));
                        // </addition> Bug: 4698038
                         oneFailed=true;
                    }
                }
                else
                {
                    result.failed(smh.getLocalString(getClass().getName()+".failed1",
                        "FAILED [AS-EJB pm-inuse] : pm-identifier {0} is not defined in pm-descriptors"
                        , new Object[]{pmIdentifier}));
                        oneFailed=true;
                }
                
                if(oneFailed)
                    result.setStatus(Result.FAILED);
                else if(oneWarning)
                    result.setStatus(Result.WARNING);
                
            }
            else
            {
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                    "NOT APPLICABLE [AS-EJB enterprise-beans] : pm-descriptors Element not defined"));
            }
        }
        else
        {
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create an SunEjbJar object"));
        }
        return result;
    }
    
    /**
     * @param desc
     * @param result
     * @param idVerMap  */    
    protected void testPmDescriptor(PmDescriptor desc, Result result,Map idVerMap,EjbDescriptor descriptor)
    {
        //pm-identifier
        String value = desc.getPmIdentifier();
        if(value.length()==0)
        {
            result.failed(smh.getLocalString(getClass().getName()+".failed2",
                "FAILED [AS-EJB pm-descritor] : pm-identifier cannot be an empty string"));
            oneFailed=true;
        }
        else
        {
            result.passed(smh.getLocalString(getClass().getName()+".passed2",
                "PASSED [AS-EJB pm-descriptor] : pm-identifier is {0}",
                new Object[]{value}));
        }
        
        //pm-version
        value = desc.getPmVersion();
        if(value.length()==0)
        {
            result.failed(smh.getLocalString(getClass().getName()+".failed3",
                "FAILED [AS-EJB pm-descritor] : pm-version cannot be an empty string"));
            oneFailed=true;
        }
        else
            result.passed(smh.getLocalString(getClass().getName()+".passed3",
                "PASSED [AS-EJB pm-descriptor] : pm-version is {0}",
                new Object[]{value}));
        idVerMap.put(desc.getPmIdentifier(),desc.getPmVersion());
        //pm-config
        value = desc.getPmConfig();
        if(value==null)
        {
            oneWarning=true;
            result.warning(smh.getLocalString(getClass().getName()+".warning",
                "WARNING [AS-EJB pm-descriptor] : pm-config value is missing"));
        }
        else
        {
            
            /*if(value.length()==0)
            {
                result.failed(smh.getLocalString(getClass().getName()+".failed4",
                    "FAILED [AS-EJB pm-descritor] : pm-config cannot be an empty string"));
            }
            else
                result.passed(smh.getLocalString(getClass().getName()+".passed4",
                    "PASSED [AS-EJB pm-descriptor] : pm-config is {0}",
                    new Object[]{value}));
             */
            
            //////  //4698035
                    if(value.length()==0) 
                    {
                        oneFailed = true;
                        result.failed(smh.getLocalString(getClass().getName()+".failed4",
                                "FAILED [AS-EJB pm-descritor] : pm-config cannot be an empty string"));
                        
                    }
                    else
                    {   File f = Verifier.getJarFile(((EjbBundleArchivist) descriptor.getEjbBundleDescriptor().getArchivist()).getEjbJarFile().getName());
                        JarFile jarFile = null;
                        ZipEntry deploymentEntry=null;
                        try {
                              jarFile = new JarFile(f);
                              if(jarFile!=null)
                              deploymentEntry = jarFile.getEntry(value);
                              

                        }
                        catch(IOException e){}
                        finally{
                           try{  if(jarFile!=null) jarFile.close();} 
                           catch(IOException e){}
                        }

                        if(deploymentEntry !=null){
                            result.passed(smh.getLocalString(getClass().getName()+".passed4",
                            "PASSED [AS-EJB pm-descriptor] : pm-config is {0}",
                            new Object[]{value}));
                        }
                        else{
                            oneWarning=true;
                            result.warning(smh.getLocalString(getClass().getName()+".warning3",
                            "WARNING [AS-EJB pm-descriptor] : config file {0} pointed in pm-config is not present in the jar file",
                            new Object[]{value}));
                        
                        
                        }
                    }
                /////////
                    
        }
                
        //pm-class-generator
        value = desc.getPmClassGenerator();  
        if(value == null)
        {
            oneWarning=true;
            result.warning(smh.getLocalString(getClass().getName()+".warning1",
                "WARNING [AS-EJB pm-descriptor] : pm-class-generaor value is missing"));
        }
        else if(value.trim().indexOf(" ") != -1) // Bug 4698042
        {
            oneFailed=true;
            result.failed(smh.getLocalString(getClass().getName()+".failed7",
                "FAILED [AS-EJB pm-descriptor] : pm-class-generator class name is invalid"));
        }
        else
        {
            if(value.trim().length()==0)
            {
                oneFailed=true;
                result.failed(smh.getLocalString(getClass().getName()+".failed5",
                    "FAILED [AS-EJB pm-descritor] : pm-class-generator cannot be an empty string"));
            }
            else
                result.passed(smh.getLocalString(getClass().getName()+".passed5",
                    "PASSED [AS-EJB pm-descriptor] : pm-class-generator is {0}",
                    new Object[]{value}));
        }

        //pm-mapping-factory
        value = desc.getPmMappingFactory(); 
        if(value == null)
        {
            oneWarning=true;
            result.warning(smh.getLocalString(getClass().getName()+".warning2",
                "WARNING [AS-EJB pm-descriptor] : pm-mapping-factory value is missing"));
        }
        else if(value.trim().indexOf(" ") != -1) // Bug 4698042
        {
            oneFailed=true;
            result.failed(smh.getLocalString(getClass().getName()+".failed8",
                "FAILED [AS-EJB pm-descriptor] : pm-mapping-factory class name is invalid"));
        }
        else
        {
            if(value.trim().length()==0)
            {
                oneFailed=true;
                result.failed(smh.getLocalString(getClass().getName()+".failed6",
                    "FAILED [AS-EJB pm-descritor] : pm-pm-mapping-factory cannot be an empty string"));
            }
            else
                result.passed(smh.getLocalString(getClass().getName()+".passed6",
                    "PASSED [AS-EJB pm-descriptor] : pm-mapping-factory is {0}",
                    new Object[]{value}));
        }
    }
}
