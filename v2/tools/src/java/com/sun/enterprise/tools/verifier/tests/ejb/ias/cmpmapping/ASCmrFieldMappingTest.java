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

public class ASCmrFieldMappingTest extends EjbTest implements EjbCheck {



    public Result check(EjbDescriptor descriptor)
    {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);
        boolean oneFailed = false;
        boolean notApp = false;

        //<addition author="irfan@sun.com" [bug/rfe]-id="4715917" >
        /*Set keys = descriptor.getEjbBundleDescriptor().getIasCmpMappingsKeys();

        SunCmpMappings iasCmpMappings=null;
        String forPmConfig=null;*/
        SunCmpMappings iasCmpMappings = descriptor.getEjbBundleDescriptor().getIasCmpMappings();
        String forPmConfig = com.sun.enterprise.deployment.EjbBundleXmlReader.IAS_CMP_MAPPING_JAR_ENTRY;
        //</addition>

        SunCmpMapping[] allIasCmpMapping=null;
        SunCmpMapping iasCmpMapping=null;

        /////test vars
        EntityMapping[] entityMapping=null;

        String cmrfieldName=null;
        ColumnPair[] columnPair=null;
        FetchedWith fetchedWith=null;
        String strlevel="";


        //<addition author="irfan@sun.com" [bug/rfe]-id="4715917" >
        if(iasCmpMappings!=null)
        {
        /*if(keys!=null && keys.size()>0)
        {
            Iterator it = keys.iterator();
            while(it.hasNext()) {

                forPmConfig=(String) it.next();
                iasCmpMappings= descriptor.getEjbBundleDescriptor().getIasCmpMappings(forPmConfig);*/
         //</addition>
                allIasCmpMapping=iasCmpMappings.getSunCmpMapping();

                for(int rep=0;rep<allIasCmpMapping.length;rep++){

                      iasCmpMapping=allIasCmpMapping[rep];

                      //test logic
                      entityMapping=iasCmpMapping.getEntityMapping();

                      if(entityMapping == null || entityMapping.length==0){//this should never happen
                      oneFailed = true;

                      result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-CMP-MAPPING] : entity-mapping is  NOT a valid entry, in the mapping file [ {0}], "+
                            "of the ejb archive [ {1} ]. "+
                            "Either  null or empty.",
                            new Object[]{forPmConfig,descriptor.getName()}));
                      continue;

                      }

                      for(int rep1=0;rep1<entityMapping.length;rep1++){
                            CmrFieldMapping[] cmrFieldMapping=null;
                            cmrFieldMapping=entityMapping[rep1].getCmrFieldMapping();
                            String forEjb=entityMapping[rep1].getEjbName();

                          //since cmrmapping can be (0..n)
                          if(cmrFieldMapping == null || cmrFieldMapping.length==0){
                                result.notApplicable(smh.getLocalString (getClass().getName() + ".notApplicable",
                                "NOT APPLICABLE [AS-CMP-MAPPING] : There is no cmr-field-mappings element present for ejb [ {0} ]in the mapping file [ {1}], "+
                                "of the ejb archive [ {2} ]. ",
                                new Object[]{forEjb,forPmConfig,descriptor.getName()}));
                                continue;

                          }



                          for(int rep11=0;rep11<cmrFieldMapping.length;rep11++){

                              cmrfieldName=cmrFieldMapping[rep11].getCmrFieldName();
                              columnPair=cmrFieldMapping[rep11].getColumnPair();
                              fetchedWith =cmrFieldMapping[rep11].getFetchedWith();

                              if(validateCmrFieldName(cmrfieldName)){

                                  result.passed(smh.getLocalString(getClass().getName()+".passed",
                                  "PASSED [AS-CMP-MAPPING] : cmr-field-mapping->cmr-field-name [{0} ] is a valid entry, in the mapping file [ {1}], "+
                                  "of the ejb archive [ {2} ]. ",
                                  new Object[]{cmrfieldName,forPmConfig,descriptor.getName()}));

                              }else{
                                  oneFailed = true;

                                  result.failed(smh.getLocalString(getClass().getName()+".failed1",
                                  "FAILED [AS-CMP-MAPPING] : cmr-field-mapping->cmr-field-name [{0} ] is  NOT a valid entry, in the mapping file [ {1}], "+
                                  "of the ejb archive [ {2} ]. "+
                                  "Either  null or empty.",
                                  new Object[]{cmrfieldName,forPmConfig,descriptor.getName()}));

                              }

                              if(validateColumnPair(columnPair,result,forPmConfig,descriptor.getName())){

                                  result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                  "PASSED [AS-CMP-MAPPING] : All the cmr-field-mapping->column-pair for field-name [{0} ], are valid, in the mapping file [ {1}], "+
                                  "of the ejb archive [ {2} ]. ",
                                  new Object[]{cmrfieldName,forPmConfig,descriptor.getName()}));

                              }else{
                                  oneFailed = true;

                                  result.failed(smh.getLocalString(getClass().getName()+".failed2",
                                  "FAILED [AS-CMP-MAPPING] : At least one cmr-field-mapping->column-pair, for field-name [{0} ] is  NOT a valid entry, in the mapping file [ {1}], "+
                                  "of the ejb archive [ {2} ]. "+
                                  "Either  null or empty.",
                                  new Object[]{cmrfieldName,forPmConfig,descriptor.getName()}));

                              }

                              ///check  level
                              boolean validLevel=true;
                              int level;

                              if(fetchedWith !=null ){ //since optional field
                                  try{
                                        level=fetchedWith.getLevel();
                                        strlevel=level+strlevel;
                                        if(!(level >0 && level < Integer.MAX_VALUE))
                                        validLevel=false;
                                  } catch(NumberFormatException exception){
                                        validLevel=false;

                                  }catch(RuntimeException re){
                                        //ignore
                                  }
                              }
                              if(!validLevel){
                                  oneFailed = true;

                                  result.failed(smh.getLocalString(getClass().getName()+".failed3",
                                  "FAILED [AS-CMP-MAPPING] : The cmr-field-mapping->fetched-with->level [ {0} ], for field-name [{1} ] is  NOT a valid entry, in the mapping file [ {1}], "+
                                  "of the ejb archive [ {2} ]. "+
                                  "Should be an integer between 1 to Integer.MAX_VALUE.",
                                  new Object[]{strlevel,forPmConfig,descriptor.getName()}));

                              }
                              //end of level chk

                          }//end for(int rep1=0;rep<entityMapping.length;rep1++){

                      }

                }


            //} 4715917 irfan

        }
        else
        {
            notApp = true;
            result.notApplicable(smh.getLocalString
                                 (getClass().getName() + ".notApplicable1",
                                  "NOT APPLICABLE [AS-CMP-MAPPING] : There is no ias-cmp-mappings file present, within the ejb archive [ {0} ].",
                                  new Object[] {descriptor.getName()}));

        }

        if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else if(notApp) {
            result.setStatus(Result.NOT_APPLICABLE);
        }else {
            result.setStatus(Result.PASSED);
        }


        return result;
    }

    boolean validateCmrFieldName(String name){
        boolean valid=false;
        if(name !=null && !name.trim().equals("")){
            valid =true;
        }
        return valid;
    }

    boolean  validateColumnPair(ColumnPair[] columnPair,Result result,String forPmConfig,String descriptorName){
        boolean valid=true;

        String[] colPair=null;
        String col1=null;
        String col2=null;
        if(columnPair ==null && columnPair.length ==0 ){
              result.failed(smh.getLocalString(getClass().getName()+".failed4",
                  "ERROR [AS-CMP-MAPPING] : The cmr-field-mapping->column-pair is EMPTY/NULL, in the mapping file [ {0}], "+
                  "of the ejb archive [ {1} ]. "+
                  "Atleast one column of the pair, null or empty.",
                  new Object[]{forPmConfig,descriptorName}));
            valid =false;
            return valid;
        } else{
            for(int rep=0;rep<columnPair.length;rep++)
            {
                boolean pairValid=false;
                if(columnPair[rep] !=null ){
                    colPair=columnPair[rep].getColumnName();
                    if(colPair !=null && colPair.length==2){
                          col1=colPair[0];
                          col2=colPair[1];

                            if(col1!=null  && !col1.trim().equals("") &&
                                          col2!=null  && !col2.trim().equals("")){
                                pairValid=true;

                            }

                    }
                }

                if(pairValid){

                      result.passed(smh.getLocalString(getClass().getName()+".passed2",
                                  "PASSED [AS-CMP-MAPPING] : The cmr-field-mapping->column-pair entry [ {0}/{1} ]  is valid, in the mapping file [ {2}], "+
                                  "of the ejb archive [ {3} ]. ",
                                  new Object[]{col1,col2,forPmConfig,descriptorName}));

                }else{
                      valid = false;

                      result.failed(smh.getLocalString(getClass().getName()+".failed5",
                                  "FAILED [AS-CMP-MAPPING] : The cmr-field-mapping->column-pair entry [ {0}/{1} ] ,  is  NOT a valid entry, in the mapping file [ {2}], "+
                                  "of the ejb archive [ {3} ]. "+
                                  "Atleast one column of the pair, null or empty.",
                                  new Object[]{col1,col2,forPmConfig,descriptorName}));

                }

            }


        }
        return valid;
    }
}

