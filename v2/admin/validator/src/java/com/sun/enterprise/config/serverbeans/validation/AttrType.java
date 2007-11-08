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

package com.sun.enterprise.config.serverbeans.validation;


import java.util.logging.Logger;
import java.util.Hashtable;
import com.sun.logging.LogDomains;
import com.sun.enterprise.config.ConfigBean;

/**
    Class which contains Meta data for all types of attributes which is present in Validation Descriptor
 *  XML File
 *
 *  Sample
 *      <attribute name=<Name> type="address" />
 *      <attribute name=<Name> type="integer" range="low,high" />
 *      <attribute name=<Name> type="string"  max-length="length" />
    
    @author Srinivas Krishnan
    @version 2.0
*/

/* Base Class for all types of attribute */
 
public class AttrType {
    String name;
    String type;
    boolean optional = false; // iff true then attribute can be set to
                              // a null
    Hashtable _specRules;

    final static protected Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    public AttrType(final String name, final String type, final boolean optional) {
        this.name = name;
        this.type = type;
        this.optional = optional;
        _specRules = new Hashtable();
    }
    
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }

    public boolean getOptional(){
        return optional;
    }
    

    void addRuleValue(String ruleName,  Object ruleValue)
    {
        if(ruleValue!=null)
            _specRules.put(ruleName, ruleValue);
            
    }
    
    Object getRuleValue(String ruleName)
    {
        return _specRules.get(ruleName);
    }
    
    public void validate(final Object value, final ValidationContext valCtx) 
    {
        //check for mandatory existance if not optional
        if (!optional && null == value){
            reportAttributeError(valCtx, "nullValueForMandatoryAttribute", 
                  "Attribute ''{0}'' is mandatory. A null value is not allowed",
                  new Object[]{name});
        }
        //key change check
        validateKeyChanges(value, valCtx);

        //checkUniqueness
        validateUniqueness(value, valCtx);
        
        //checkReferences
        validateReferences(value, valCtx);

    }

    private void validateKeyChanges(final Object value, final ValidationContext valCtx) 
    {
        String key = valCtx.getPrimaryKeyName();
        if (key == null || !key.equals(name))
            return;
        
        if(valCtx.isUPDATE())
        {
            if(name.equals(valCtx.name))
            {
                //change primary key is prohibited
                reportAttributeError(valCtx, "primarykeychangeattempt", 
                         "Cannot change a primary key", 
                         new Object[] {valCtx.name});
            }
        }
        else if(valCtx.isSET() || valCtx.isADD() || valCtx.isVALIDATE())
        {
            if ( (valCtx.classObject instanceof ConfigBean) && (valCtx.value instanceof ConfigBean) )
             {

                if(!isAttrValueUniqueAmongSiblings((ConfigBean)valCtx.classObject, (ConfigBean)valCtx.value, key, null))
                {
                    reportAttributeError(valCtx, "keyDuplication", 
                         "Element with the same attribute value ({0} = {1}) already exists.", 
                         new Object[] {key, value});
                }
            }
            
        }
        else if(valCtx.isDELETE())
            return; //FIXME: what to do with DELETE

    }
    
    private void validateUniqueness(final Object value, final ValidationContext valCtx) 
    {
        String[] belongsTo = (String[])getRuleValue("belongs-to");     // unique in name-domains (forms name-domain too)
        if (belongsTo==null || belongsTo.length==0)
            return;
        NameListMgr nameListMgr = valCtx.getNameListMgr();
        if(nameListMgr==null)
            return;
        String xpathForValue = getFutureXPathForValidatingAttribute(valCtx);
        String[] valuesToTest = ((String)value).split(",");
        
        for(int i=0; i<belongsTo.length; i++)
        {
            if(belongsTo[i]!=null)
            {
                for(int j=0; j<valuesToTest.length; j++)
                {
                    if(valCtx.isDELETE())
                    {    
                        if(nameListMgr.isValueInNameDomainReferenced(
                             belongsTo[i], valuesToTest[j], xpathForValue))
                        {
                            String elementPrintName = GenericValidator.
                                 getConfigElementPrintName(valCtx, xpathForValue, true, true);
                            String refXPath= nameListMgr.getDomainValueReferenceeXPath(
                                 belongsTo[i], valuesToTest[j], xpathForValue);
                            String refPrintName = GenericValidator.
                                 getConfigElementPrintName(valCtx, refXPath, true, true);
                            reportAttributeError(valCtx, "isReferenced", 
                               "Element {0} can not be deleted because it is referenced from {1}", 
                               new Object[] {elementPrintName, refPrintName});
                        }
                    }
                    else
                    {
                        if(!nameListMgr.isUniqueValueInNameDomain(belongsTo[i], 
                                                    valuesToTest[j], 
                                                    xpathForValue))
                        {
                            reportAttributeError(valCtx, "notUniqueInList", 
                               "Attribute value ({0} = {1}) is not unique in {2}.", 
                               new Object[] {name, valuesToTest[j], 
                                   nameListMgr.getDescriptionForNameDomain(belongsTo[i])});
                        }
                    }
                }
            }
        }
    }
    
    private void validateReferences(final Object value, final ValidationContext valCtx) 
    {
        String[] referencesTo = (String[])getRuleValue("references-to");  // referencing to name-domains
        if (referencesTo==null || referencesTo.length==0 || value==null)
            return;
        NameListMgr nameListMgr = valCtx.getNameListMgr();
        if(nameListMgr==null)
            return;
        if(valCtx.isDELETE())
            return; //FIXME: what to do with DELETE

        String xpathForValue = getFutureXPathForValidatingAttribute(valCtx);
        String[] valuesToTest = ((String)value).split(",");
        
        for(int i=0; i<referencesTo.length; i++)
        {
            if(referencesTo[i]!=null)
            {
                for(int j=0; j<valuesToTest.length; j++)
                {
//System.out.println("    referencesTo[i]="+referencesTo[i]+ "\n    valuesToTest[j]="+valuesToTest[j] + "\n  xpathForValue="+xpathForValue);
                    if(!nameListMgr.isValueInNameDomain(referencesTo[i], 
                                                valuesToTest[j], 
                                                xpathForValue))
                    {
//System.out.println(">>>value=" + value);
                        reportAttributeError(valCtx, "notFoundInList", 
                             "Attribute value ({0} = {1}) is not found in {2}.", 
                              new Object[] {name, valuesToTest[j], nameListMgr.getDescriptionForNameDomain(referencesTo[i])});
                    }
                }
            }
        }
    }
     /**
      * get attribute values from siblings 
      */
     private boolean isAttrValueUniqueAmongSiblings(ConfigBean parent, ConfigBean cb, String attrName, String newValue) 
     {
         if(parent==null || cb==null)
   		    return true;
         ConfigBean[] cbs = parent.getChildBeansByName(cb.name());
         String value = newValue!=null?newValue:cb.getAttributeValue(attrName);
         if(cbs == null)
             return true;
         for(int i=0; i<cbs.length; i++)
         {
             if( ((Object)cbs[i] != (Object)cb) && 
                 value.equals(cbs[i].getAttributeValue(attrName)))
                return false;
         }
         return true;
     }

     /**
      * get all siblings beans (including this)
      */
     public ConfigBean[] getAllSiblingsForConfigBean(ConfigBean cb) 
     {
         ConfigBean parent = (ConfigBean)cb.parent();
         if(parent==null)
             return new ConfigBean[]{cb};
         else
             return parent.getChildBeansByName(cb.name());
     }

     /**
      * get attribute values from siblings
      */
     private String[] getAttrValuesFromSiblings(ConfigBean cb, String attrName, boolean bIncludingThis) 
     {
         ConfigBean[] cbs = getAllSiblingsForConfigBean(cb);
         if(cbs == null)
             return new String[0];
         int iStrsLen = cbs.length;
         if(!bIncludingThis)
             iStrsLen--;
         if(iStrsLen<=0)
             return new String[0];
         String[] strs = new String[iStrsLen];
         int iStr = 0;
         for(int i=0; i<cbs.length; i++)
         {
             if(bIncludingThis || (Object)cbs[i]!=(Object)cb)
                strs[iStr++] = cbs[i].getAttributeValue(attrName);
         }
         return strs;
     }
     protected String getFutureXPathForValidatingAttribute(ValidationContext valCtx)
     {
        if(valCtx.isSET() || valCtx.isADD() || valCtx.isVALIDATE())
        {
            String[] tokens =  XPathHelper.extractTokens(((ConfigBean)valCtx.value).getAbsoluteXPath(""));
            if(valCtx.classObject==null) //root
            {
                return "/" + tokens[tokens.length-1] + "/@" + name;
            }
            return ((ConfigBean)valCtx.classObject).getXPath() + "/" + 
                            tokens[tokens.length-1] + "/@" + name;
        }
        else if(valCtx.isUPDATE())
        {
            return ((ConfigBean)valCtx.classObject).getXPath() + "/@" + name;
        }
        else if(valCtx.isDELETE())
        {
            if(valCtx.value instanceof ConfigBean)
               return ((ConfigBean)valCtx.value).getXPath() + "/@" + name;
        }
        return null;
     }
    protected String getValueForAttribute(String attrName, ValidationContext valCtx)
    {
        if(attrName==null || valCtx.getTargetBean()==null)
            return null;
        if(attrName.startsWith("@"))
            attrName = attrName.substring(1);
        return valCtx.getTargetBean().getAttributeValue(attrName);
    }

    protected void reportAttributeError(ValidationContext valCtx, 
            String msgNameSuffix, String defaultMsg, Object[] values)
    {
        ReportHelper.reportAttributeError(valCtx, 
                                msgNameSuffix, defaultMsg, values);

    }
}

