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

import java.util.Vector;
import java.net.InetAddress;
import java.net.UnknownHostException;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.validation.tests.StaticTest;
import com.sun.enterprise.config.serverbeans.ElementProperty;

import com.sun.enterprise.admin.meta.MBeanRegistryFactory;

// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.LocalStringManagerImpl;



/** 
 *  Class which validates all attributes in the validation descriptor xml file.
 *  All custom tests use this as the  base class

    @author Srinivas Krishnan
    @version 2.0
*/


public class GenericValidator implements DomainCheck {
    
    // Logging
    final static protected Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    protected LocalStringManagerImpl smh = StringManagerHelper.getLocalStringsManager();
    
    ValidationDescriptor desc;
    
    public GenericValidator(ValidationDescriptor desc) {
        this.desc = desc;
//        try {
            StringManagerHelper.setLocalStringsManager(DomainXmlVerifier.class);
            smh = StringManagerHelper.getLocalStringsManager();
//        }
//        catch (ClassNotFoundException e) {
//           _logger.log(Level.FINE, "domainxmlverifier.class_notfound_exception", e);
//        }
    }
    
    // Initialize all the member variables from the event, invoked by validate. If sub class is not calling 
    // super.validate subclass has to call initialize before starting validation
    public ValidationContext initialize(ConfigContextEvent cce) {
        ValidationContext valCtx = new ValidationContext(new Result(), cce.getObject(), cce.getClassObject(), 
                                        cce.getBeanName(), cce.getConfigContext(), cce.getName(), cce.getChoice(), 
                                        desc.getKey(), smh, desc);
        String key = desc.getKey();
        String element = desc.getElementName();
        if(valCtx.value instanceof ConfigBean) {
            valCtx.result.setAssertion(element);
            try {
                valCtx.result.setTestName(smh.getLocalString(GenericValidator.class.getName() + ".Elementkeyvalue",
                "{0} = {1}", new Object[]{key, ((ConfigBean)valCtx.value).getAttributeValue(key) }));
            } catch(Exception e) {
                _logger.log(Level.FINE, "domainxmlverifier.keynotfound", e);
            }
        }
        valCtx.result.setStatus(0);
        //set operation name
        if(!valCtx.isVALIDATE())
        {
            String elementName = getTargetElementPrintName(valCtx, true, false);
            String strLocalTag;
            String strDefault;
            if(valCtx.isADD() || valCtx.isSET()) {
                strLocalTag = "add_operation_descr";
                strDefault = "Creation config element for {0}";
            } else if(valCtx.isDELETE()) {
                strLocalTag = "delete_operation_descr";
                strDefault = "Deletion of config element for {0}";
            } else {
                strLocalTag = "update_operation_descr";
                strDefault = "Update of config element for {0}";
            }
            valCtx.result.setOperationPrintName(valCtx.smh.getLocalString(
                   strLocalTag, strDefault, new Object[] {elementName}));
        }
        return valCtx;
    }

    private void validateAttribute(AttrType attr, ValidationContext valCtx) 
    {
        if(valCtx.choice == null)
            return;
        String attrName = attr.getName();
        Object value = null;
        ConfigBean ownerBean = null;
        if(valCtx.isADD() || valCtx.isVALIDATE() ||  valCtx.isSET() || valCtx.isDELETE()) 
        {
            ownerBean = (ConfigBean)valCtx.value;
            value = ((ConfigBean)valCtx.value).getAttributeValue(attrName);
            
        }
        else if(valCtx.isUPDATE())
        {
           if(attr.getName().equals(valCtx.name))
           {
                ownerBean = (ConfigBean)valCtx.classObject;
                value = valCtx.value;
//              _logger.log(Level.WARNING, "setting attribute: "+attr.getName()+" to: " +valCtx.value);
           }
        }

       
        if(ownerBean!=null /*&& value!=null*/)
        {
            //-----------set current validation state in valCtx
            valCtx.setAttrName(attrName);
            valCtx.attrValue = value;
            //valCtx.ownerBean = ownerBean;
            // and now - call attributes validation method for element
            validateAttribute(ownerBean, attr, value, valCtx);   
        }
    }
    
    // can be overriden by specific element test class
    public void validateAttribute(ConfigBean ownerBean, AttrType attr, Object value, ValidationContext valCtx) 
    {
        if (/*value!=null &&*/ !StaticTest.valueContainsTokenExpression((String) value))
        {
            //generic validation of attribute
            attr.validate(value, valCtx);
        }
    }

    // can be overriden by specific element test class
    public void validateElement(ValidationContext valCtx) 
    {
     String key = desc.getKey();
     String element = desc.getElementName();
     if(valCtx.isADD() || valCtx.isSET()) 
     {
        ConfigBean thisBean = (ConfigBean)valCtx.value;
        ConfigBean parentBean = (ConfigBean)valCtx.classObject;
        String[] names = desc.requiredChildren;
        for(int i=0; names!=null && i<names.length; i++)
        {
            String childName = names[i];
            if(names[i].endsWith("*"))
                childName = names[i].substring(0, names[i].length()-1);
            childName = XPathHelper.convertName(childName);
            ConfigBean[] beans = thisBean.getChildBeansByName(childName);
            if (beans==null || beans.length==0)
            {
                if(names[i].endsWith("*"))
                {
                    String printParentName = getConfigElementPrintName(
                        getFutureXPath(thisBean, parentBean), false, false);
                    valCtx.result.failed(valCtx.smh.getLocalString(
                        GenericValidator.class.getName() + ".multipleRequiredElemAbsent",
                        "At least one required element {0} should be present in created {1}", 
                        new Object[] {names[i].substring(0, names[i].length()-1),
                                printParentName}));
                }
                else
                {
                    String printParentName = getConfigElementPrintName(
                        getFutureXPath(thisBean, parentBean), false, false);
                    valCtx.result.failed(valCtx.smh.getLocalString(
                        GenericValidator.class.getName() + ".requiredElemAbsent",
                        "Required element {0} should be present in created {1}", 
                        new Object[] {names[i], printParentName}));
                }
            }
        }
        names = desc.exclusiveChildren;
        String alreadyFound = null;
        for(int i=0; names!=null && i<names.length; i++)
        {
            String childName = names[i];
            childName = XPathHelper.convertName(childName);
            ConfigBean[] beans = thisBean.getChildBeansByName(childName);
            if (beans==null || beans.length==0)
                continue;
            if(alreadyFound==null)
            {
                alreadyFound = names[i];
            }
            else
            {
                String printParentName = getConfigElementPrintName(
                    getFutureXPath(thisBean, parentBean), false, false);
                valCtx.result.failed(valCtx.smh.getLocalString(
                        GenericValidator.class.getName() + ".childrenCanExistTogether",
                        "{0} can not contain both sub-elements {1} and {2} in the same time.", 
                        new Object[] {printParentName, alreadyFound, names[i]}));
            }
        }
     }
    }

    // can be overriden by specific element test class
    // this is convenience method for property owner element validator
    // to add reaction on property changes
    // this method is called from <code>validateAsParent</code> method
    // NOTE: ValidationContext is initialized in child's validator
    public void validatePropertyChanges(ValidationContext valCtx) 
    {
       return;        
    }
    
    // can be overriden by specific element test class
    // NOTE: ValidationContext is initialized in child's validator
    public void validateAsParent(ValidationContext valCtx) 
    {
     if(valCtx.isADD() || valCtx.isSET()) 
     {
        ConfigBean newChildBean = (ConfigBean)valCtx.value;
        String newChildBeanName = getBeanElementName(newChildBean);
        ConfigBean parentBean = (ConfigBean)valCtx.classObject;

        String[] names = desc.exclusiveChildren;
        //first, let's be sure that newChildBean in exclusive list
        boolean bNewChildIsInList = false;
        if(names!=null)
        {
            for(int i=0; i<names.length; i++)
            {
               if(newChildBeanName.equals(names[i]))
               {
                   bNewChildIsInList = true;
                   break;
               }
            }
        }
        //now find out if any othjers are there too
        if(bNewChildIsInList)
        {
            for(int i=0; i<names.length; i++)
            {
                String childName = names[i];
                if(childName.equals(newChildBeanName))
                    continue;
                childName = XPathHelper.convertName(childName);
                ConfigBean[] beans = parentBean.getChildBeansByName(childName);
                if (beans!=null && beans.length>0)
                {
                    String printParentName = getConfigElementPrintName(
                        parentBean.getXPath(), false, false);
                    valCtx.result.failed(valCtx.smh.getLocalString(
                            GenericValidator.class.getName() + ".childrenCanExistTogether",
                            "{0} can not contain both sub-elements {1} and {2} in the same time.", 
                            new Object[] {printParentName, 
                                    newChildBeanName, names[i]}));
                }
            }
        }
     }
     else if(valCtx.isDELETE())
     {
        //Check for existence of required sub-elements
        ConfigBean childBean = (ConfigBean)valCtx.value;
        ConfigBean parentBean = (ConfigBean)valCtx.classObject;
        String childBeanName = XPathHelper.convertName(getBeanElementName(childBean));
        ConfigBean[] beans = parentBean.getChildBeansByName(childBeanName);
        if (beans!=null && beans.length==1)
        {  //LAST ELEM DELETION
            ValidationDescriptor parentDescr = desc;
            String[] names = null;
            if(parentDescr!=null)
                names = parentDescr.requiredChildren;
            String compareTo = getBeanElementName(childBean);
            String compareTo2 = compareTo+'*';
            for(int i=0; names!=null && i<names.length; i++)
            {
                if(compareTo.equals(names[i]))
                {
                    String printParentName = getConfigElementPrintName(
                        parentBean.getXPath(), false, false);
                    String printChildName = getConfigElementPrintName(
                        childBean.getXPath(), false, false);
                    valCtx.result.failed(valCtx.smh.getLocalString(
                        GenericValidator.class.getName() + ".requiredElemDelete",
                        "Required element {0} can not be deleted from {1}", 
                        new Object[] {printChildName, printParentName}));
                    break;
                }
                else if(compareTo2.equals(names[i]))
                {
                    String printParentName = getConfigElementPrintName(
                        parentBean.getXPath(), false, false);
                    String printChildName = getConfigElementPrintName(
                        childBean.getXPath(), false, false);
                    valCtx.result.failed(valCtx.smh.getLocalString(
                        GenericValidator.class.getName() + ".lastRequiredElemDelete",
                        "At least one required {0} should be present in {1}."+
                        " Deletion rejected.", 
                        new Object[] {printChildName, printParentName}));
                    break;
                }
            }
        }

     }
     
     if(valCtx.getTargetBean() instanceof ElementProperty)
        validatePropertyChanges(valCtx);
        
    }
    
    // Method for validation (ususally overriden by test classes)
    public Result validate(ConfigContextEvent cce) 
    {
        ValidationContext valCtx = initialize(cce);
        _logger.log(Level.CONFIG, "GenericValidator processing choice: "+valCtx.choice);
        validate(valCtx);
                
        return valCtx.result;
        
    }

    // Method for validation (ususally overriden by test classes)
    public void validate(ValidationContext valCtx) 
    {
        
        //validate element (without attributes) 
        validateElement(valCtx);
        
        //ask parent to confirm change   
        ConfigBean parentBean = valCtx.getParentBean();
        if(parentBean!=null)
        {
           //validate changes by Parent 
           GenericValidator parentValidator = 
                   desc.domainMgr.findConfigBeanValidator((ConfigBean)parentBean);
           if(parentValidator!=null)
           {
               parentValidator.validateAsParent(valCtx);
           }
        }
        
        Vector attrs = desc.getAttributes();
        //Attributes validation    
        for(int i=0; i<attrs.size(); i++) 
        {
            try {
                validateAttribute((AttrType) attrs.get(i), valCtx);
            } catch(IllegalArgumentException e) {
                valCtx.result.failed(e.getMessage());
            } catch(Exception e) {
                _logger.log(Level.WARNING, "domainxmlverifier.errorinvokingmethod", e);
            }
        }
        
    }

   static String getConfigElementPrintName( ValidationContext valCtx, 
            String xpath, boolean bIncludingKey, boolean bReplaceRefByParentElem)
   {
      return valCtx.validationDescriptor.domainMgr.getMBeanRegistry().
               getConfigElementPrintName(xpath, bIncludingKey, bReplaceRefByParentElem);
   }

   String getConfigElementPrintName(
            String xpath, boolean bIncludingKey, boolean bReplaceRefByParentElem)
   {
      return desc.domainMgr.getMBeanRegistry().
               getConfigElementPrintName(xpath, bIncludingKey, bReplaceRefByParentElem);
   }
    static String getTargetElementPrintName( ValidationContext valCtx, 
            boolean bIncludingKey, boolean bReplaceRefByParentElem)
   {
        ConfigBean targetBean = valCtx.getTargetBean();
        if(targetBean==null)
            return null;
        ConfigBean parentBean = valCtx.getParentBean();
        return getConfigElementPrintName( valCtx, 
                getFutureXPath(targetBean, parentBean),
                bIncludingKey, bReplaceRefByParentElem);
   }

    protected static String getFutureXPath(ConfigBean childBean, ConfigBean parentBean) {
        String[] tokens = XPathHelper.extractTokens(childBean.getAbsoluteXPath(""));
        if(parentBean==null) //root
        {
            return "/" + tokens[tokens.length-1];
        }
        return (parentBean.getXPath() + "/" + tokens[tokens.length-1]);
    }
    protected static String getBeanElementName(ConfigBean bean) {
        String[] tokens = XPathHelper.extractTokens(bean.getAbsoluteXPath(""));
        if(tokens.length<1)
            return null;
        String last = tokens[tokens.length-1];
        int idx = last.indexOf('[');
        if(idx>0)
            return last.substring(0, idx);
        else
            return last;
    }    

    /***********************************************************    
     * reports validation error
     ************************************************************/
    static public void reportValidationError(ValidationContext valCtx, 
            String msgNameSuffix, String defaultMsg, Object[] values)
    {
        ReportHelper.reportValidationError(valCtx, 
                        msgNameSuffix, defaultMsg, values);
    }
}
