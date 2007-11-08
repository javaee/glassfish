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

public class ASSecondaryTableTest extends EjbTest implements EjbCheck {



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

        String tableName=null;
        ColumnPair[] columnPair=null;
        //FetchedWith fetchedWith=null;


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
                            SecondaryTable[] secTable=null;
                            secTable=entityMapping[rep1].getSecondaryTable();
                            String forEjb=entityMapping[rep1].getEjbName();

                          //since cmrmapping can be (0..n)
                          if(secTable == null || secTable.length==0){
                                result.notApplicable(smh.getLocalString (getClass().getName() + ".notApplicable",
                                "NOT APPLICABLE [AS-CMP-MAPPING] : There is no secondary-table element present for ejb [ {0} ]in the mapping file [ {1}], "+
                                "of the ejb archive [ {2} ]. ",
                                new Object[]{forEjb,forPmConfig,descriptor.getName()}));
                                continue;

                          }



                          for(int rep11=0;rep11<secTable.length;rep11++){

                              tableName=secTable[rep11].getTableName();
                              columnPair=secTable[rep11].getColumnPair();
                              //fetchedWith =cmrFieldMapping[rep11].getFetchedWith();

                              if(validateCmrFieldName(tableName)){

                                  result.passed(smh.getLocalString(getClass().getName()+".passed",
                                  "PASSED [AS-CMP-MAPPING] : secondary-table->table-name [{0} ] is a valid entry, in the mapping file [ {1}], "+
                                  "of the ejb archive [ {2} ]. ",
                                  new Object[]{tableName,forPmConfig,descriptor.getName()}));

                              }else{
                                  oneFailed = true;

                                  result.failed(smh.getLocalString(getClass().getName()+".failed1",
                                  "FAILED [AS-CMP-MAPPING] : secondary-table->table-name [{0} ] is  NOT a valid entry, in the mapping file [ {1}], "+
                                  "of the ejb archive [ {2} ]. "+
                                  "Either  null or empty.",
                                  new Object[]{tableName,forPmConfig,descriptor.getName()}));

                              }

                              if(validateColumnPair(columnPair,result,forPmConfig,descriptor.getName())){

                                  result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                  "PASSED [AS-CMP-MAPPING] : All the secondary-table->column-pair for field-name [{0} ], are valid, in the mapping file [ {1}], "+
                                  "of the ejb archive [ {2} ]. ",
                                  new Object[]{tableName,forPmConfig,descriptor.getName()}));

                              }else{
                                  oneFailed = true;

                                  result.failed(smh.getLocalString(getClass().getName()+".failed2",
                                  "FAILED [AS-CMP-MAPPING] : At least one secondary-table->column-pair, for field-name [{0} ] is  NOT a valid entry, in the mapping file [ {1}], "+
                                  "of the ejb archive [ {2} ]. "+
                                  "Either  null or empty.",
                                  new Object[]{tableName,forPmConfig,descriptor.getName()}));

                              }

                          }//end for(int rep1=0;rep<entityMapping.length;rep1++){

                      }

                }


            //} 4715917 irfan

        }
        else
        {
            notApp = true;
            result.notApplicable(smh.getLocalString
                                 (getClass().getName() + ".notApplicable",
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
              result.failed(smh.getLocalString(getClass().getName()+".failed3",
                  "ERROR [AS-CMP-MAPPING] : The secondary-table->column-pair is EMPTY/NULL, in the mapping file [ {0}], "+
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

                      result.failed(smh.getLocalString(getClass().getName()+".failed4",
                                  "FAILED [AS-CMP-MAPPING] : The secondary-table->column-pair entry [ {0}/{1} ] ,  is  NOT a valid entry, in the mapping file [ {2}], "+
                                  "of the ejb archive [ {3} ]. "+
                                  "Atleast one column of the pair, null or empty.",
                                  new Object[]{col1,col2,forPmConfig,descriptorName}));

                }

            }


        }
        return valid;
    }
}

