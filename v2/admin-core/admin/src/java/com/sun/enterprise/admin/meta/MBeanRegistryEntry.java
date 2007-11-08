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
 *   $Id: MBeanRegistryEntry.java,v 1.9 2007/05/05 05:25:35 tcfujii Exp $
 *   @author: alexkrav
 *
 *   $Log: MBeanRegistryEntry.java,v $
 *   Revision 1.9  2007/05/05 05:25:35  tcfujii
 *   CDDL+GPL header updates.
 *
 *   Revision 1.8  2007/04/03 01:13:40  llc
 *   Issue number:  2752
 *   Obtained from:
 *   Submitted by:  Lloyd Chambers
 *   Reviewed by:   3 day timeout expired
 *
 *   Revision 1.7  2006/05/08 17:18:53  kravtch
 *   Bug #6423082 (request for admin infrastructure to support the config changes without DAS running (offline))
 *   Added infrastructure for offline execution under Config Validator for:
 *      - dottednames set/get operation
 *      - Add/remove jvm-options
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules: admin-core/admin; admin/validator;
 *
 *   Revision 1.6  2006/03/12 01:26:57  jluehe
 *   Renamed AS's org.apache.commons.* to com.sun.org.apache.commons.*, to avoid collisions with org.apache.commons.* packages bundled by webapps.
 *
 *   Tests run: QL, Servlet TCK
 *
 *   Revision 1.5  2005/12/25 03:47:37  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.4  2005/10/06 21:44:07  kravtch
 *   Bug #6331466
 *   create-ssl for iiop-service procedure has been changed.
 *   First, ssl-client-config is created with ssl subelement in it then it added to condig context.
 *   + ManagedConfigBean swithed from enclosing associated MBean to MBeanInfo.
 *   Submitted by: kravtch
 *   Reviewed by: Sreeni
 *   Affected modules admin/mbeans; admin-core/admin
 *   Tests passed: QLT/EE + devtests
 *
 *   Revision 1.3  2005/08/16 22:19:31  kravtch
 *   M3: 1. ConfigMBeans: Support for generic getXXXNamesList() operation (request from management-rules).
 *       2. MBeanRegistry: support for getElementPrintName() to provide readable element's description for validator's messages
 *   Submitted by: kravtch
 *   Reviewed by: Shreedhar
 *   Affected modules admin-core/admin
 *   Tests passed: QLT/EE + devtests
 *
 *   Revision 1.2  2005/06/27 21:19:44  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.21  2005/05/07 04:35:27  ruyakr
 *   Engineer: Rob Ruyak
 *
 *   Fixed warning messages displayed from jdk1.5 because of introduction of varargs in certain apis such as ja
 *   va.lang.Class.
 *
 *
 *   This is my last checkin here at Sun! Thanks for all the good times!
 *
 *   Revision 1.20  2004/11/14 07:04:21  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.19  2004/06/26 02:03:41  sv96363
 *   Part of restart required bug checkin.
 *
 *   Reviewed By: Alex Kr.
 *
 *   Revision 1.18  2004/06/05 02:33:35  kravtch
 *   Reviewer: Nazrul
 *   New operations are added to infrastructure:
 *      isAttributeDynamicallyReconfigurable(attr)
 *      isChangesDynamicallyReconfigurable(changeList)
 *   Test: qlt
 *
 *   Revision 1.17  2004/06/04 19:13:59  kravtch
 *   Reviewer: Nazrul
 *   Support for "dynamicallyReconfigurable" MBean descriptor field is added to infrastructure.
 *   Tests passed: QLT PE/EE
 *
 *   Revision 1.16  2004/03/02 18:26:32  kravtch
 *   MBean's Descriptor field ElementChangeEvent support added (Constant, get method).
 *   MBeanRegistryFactory.setAdminMBeanRegistry() added for tester
 *
 *   Revision 1.14  2003/12/13 16:52:52  qouyang
 *
 *   Revision 1.13.4.1  2003/12/01 21:52:39  kravtch
 *   Bug #4939964
 *   Reviewer: Sridatta
 *   admin.config.ManagedConfigBean.createChildByType() now analyzes registryEntries.AttributeInfo for each "empty"  valued attribute (similar to setAttribute(), but could not use MBeanAttributeInfo because MBean is not exists yet). If "emptyValueAllowed" field in registrEntry.AttributeInfo is not "true", then "empty" attribute will be ignored.
 *
 *   Revision 1.13  2003/09/10 00:24:23  kravtch
 *   Reviewer: Sridatta
 *   New operation in DomainMBean is returning list of default attribute values according to mbean type. This operation will return DTD defined default values if custom mbean does not implement its own static operation overriding(extending) standard ones.
 *
 *   Revision 1.12  2003/08/30 06:25:57  kravtch
 *   Bug #4914348
 *   Reviwer: Sridatta
 *
 *   Commons-modeler managedBean.createMBeanInfo() preserves one copy of ModelMBeanInfo for all mbeans of the same type. S1AS Mbeans instantiation process sets some instance-cpecific field values into    MBeanInfo descriptor. It means that our MBeans can not share MBeanInfo.
 *   MBeanRegistryEntry.createMBeanInfo modified to force managedBean to refresh MBeanInfo.
 *
 *   Revision 1.11  2003/08/29 02:16:41  kravtch
 *   Bug #4910964 (and similar others)
 *   Reviewer: Sridatta
 *
 *   Exception handling and logging enchancements:
 *      - extraction target exception for MBeanException and TargetInvocationException:
 *      - switch to localStrings usage;
 *      - throwing exception for config MBeans if error in creation of ConfigBean;
 *      - exceptions for null-results in configbean operations,like getXXbyYYY() [changes commented because of crashing of quick test]
 *
 *   Revision 1.10  2003/08/21 21:45:26  kravtch
 *   instantiateMBean() is modified for runtime MBeans to provide ConfigBean as one of managed resources along with JSR77ModelBean.
 *
 *   Revision 1.9  2003/08/07 00:41:06  kravtch
 *   - new DTD related changes;
 *   - properties support added;
 *   - getDefaultAttributeValue() implemented for config MBeans;
 *   - merge Jsr77 and config activity in runtime mbeans;
 *
 *   Revision 1.8  2003/07/29 18:59:35  kravtch
 *   MBeanRegistryEntry:
 *   	- support for toFormatString();
 *   	- instantiateMBean() method modified to instantiate runtime MBeans as well;
 *   MBeanRegistryFactory:
 *   	- fixed bug in getRuntimeRegistry();
 *   MBeanNamingInfo:
 *   	- less strict requirements for parm_list_array size in constructor (can be more then needed);
 *   BaseRuntimeMBean:
 *   	- exception ClassCastException("Managed resource is not a Jsr77ModelBean") handling;
 *   ManagedJsr77MdlBean:
 *   	- call managed bean bug fixed ( getDeclaredMethod()->getMethod())
 *   admin/dtds/runtime-mbeans-descriptors.xml - modified to represent new runtime mbeans;
 *
 *   Revision 1.7  2003/07/18 20:14:44  kravtch
 *   1. ALL config mbeans are now covered by descriptors.xml
 *   2. new infrastructure for runtime mbeans is added
 *   3. generic constructors added to jsr77Mdl beans (String[])
 *   4. new test cases are added to admintest
 *   5. MBeanRegistryFactory has now different methods to obtain admin/runtime registries
 *   6. runtime-descriptors xml-file is added to build
 *
 *   Revision 1.6  2003/06/25 20:03:40  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/

package com.sun.enterprise.admin.meta;

//import javax.management.Descriptor;
import javax.management.DynamicMBean;
import javax.management.modelmbean.ModelMBeanInfo;
import com.sun.org.apache.commons.modeler.ManagedBean;
import com.sun.org.apache.commons.modeler.FeatureInfo;
import com.sun.org.apache.commons.modeler.AttributeInfo;
import com.sun.org.apache.commons.modeler.OperationInfo;
import com.sun.org.apache.commons.modeler.ParameterInfo;
import com.sun.org.apache.commons.modeler.FieldInfo;

import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import java.lang.reflect.Constructor;

import com.sun.enterprise.admin.meta.naming.MBeanNamingDescriptor;
import com.sun.enterprise.admin.meta.naming.MBeansNaming;
import com.sun.enterprise.admin.meta.naming.MBeanNamingInfo;
import com.sun.enterprise.admin.meta.naming.MBeanNamingException;

import com.sun.enterprise.admin.BaseAdminMBean;
import com.sun.enterprise.admin.config.BaseConfigMBean;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBeansFactory;

import javax.management.Descriptor;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.InstanceNotFoundException;
import javax.management.modelmbean.ModelMBeanInfo;


/**
 * 
 */
public class MBeanRegistryEntry implements MBeanMetaConstants
{
    MBeanNamingDescriptor   namingDescriptor = null;   

    //FIXME: should be removed or protected
    private ManagedBean             managedBean = null;   
    private HashMap                 fields = null;
    private boolean                 _bIntrospectMBeanClass = true;
    

    public MBeanRegistryEntry(ManagedBean mb) throws MBeanNamingException,ClassNotFoundException
    {
        this (mb,true);
    }

    public MBeanRegistryEntry(ManagedBean mb, boolean bMergeWithMbean) throws MBeanNamingException,ClassNotFoundException
    {
        _bIntrospectMBeanClass = bMergeWithMbean;
        managedBean = mb;
        fields = convertListOfFieldsToHashMap(mb.getFields());

        // convert dynamically changeable list to map
        String dynList  = (String)fields.get(DYNAMICALLY_RECONFIGURABLE_LIST_FIELD_NAME);
        if(dynList!=null)
        {
            //replace it by set
            fields.put(DYNAMICALLY_RECONFIGURABLE_LIST_FIELD_NAME, convertDelimListToSet(dynList));
        }

        //modify class info 
        String clazz = managedBean.getClassName();
        if(!_bIntrospectMBeanClass || clazz.startsWith("com.sun.org.apache"))
            clazz = null;
        String group = managedBean.getGroup();
        if("config".equals(group))
        {
            if(clazz==null)
            {
                clazz = "com.sun.enterprise.admin.config.BaseConfigMBean";
                managedBean.setClassName(clazz);
            }
            else
            {
                if(clazz.indexOf('.')<0)
                    clazz = "com.sun.enterprise.admin.config.mbeans."+clazz;
            }
            MBeanMetaHelper.mergeWithConfigBean(managedBean, 
                              MBeanMetaHelper.getConfigBeanClass((String)fields.get(XPATH_FIELD_NAME)), 
                              EXPOSE_ALL);
            MBeanMetaHelper.mergeWithDynamicMBean(managedBean, Class.forName(clazz));
        }
        else if("runtime".equals(group))
        {
            if(clazz==null)
            {
                clazz = "com.sun.enterprise.admin.runtime.BaseRuntimeMBean";
                managedBean.setClassName(clazz);
            }
            else
            {
                if(clazz.indexOf('.')<0)
                    clazz = "com.sun.enterprise.admin.runtime.mbeans"+clazz;
            }
            Class modelBeanClass = MBeanMetaHelper.getRuntimeModelBeanClass(mb.getName());
            if(modelBeanClass!=null)
               MBeanMetaHelper.mergeWithRuntimeModelBean(managedBean, modelBeanClass);
            Class configBeanClass = MBeanMetaHelper.getConfigBeanClass((String)fields.get(XPATH_FIELD_NAME));
            if(configBeanClass!=null)
            {
                int mode;
                if(modelBeanClass!=null)
                    mode = EXPOSE_RUNTIME_WITH_MODEL;
                else
                    mode = EXPOSE_RUNTIME_WITHOUT_MODEL;
                    
                MBeanMetaHelper.mergeWithConfigBean(managedBean, configBeanClass, mode);
            }
            MBeanMetaHelper.mergeWithDynamicMBean(managedBean, Class.forName(clazz));
        }
            
        namingDescriptor = new MBeanNamingDescriptor(  mb.getName(),
                new Integer(MBeansNaming.MODE_CONFIG), 
                (String)fields.get(CLINAME_FIELD_NAME), 
                (String)fields.get(OBJECTNAME_FIELD_NAME), 
                MBeanNamingDescriptor.XPATH_TO_MASK((String)fields.get(XPATH_FIELD_NAME)), 
                (String)clazz); // fields.get("Class")));*/

/*        AttributeInfo[] attrs = managedBean.getAttributes();
        if(attrs!=null && attrs.length>0 && "modelerType".equals(attrs[0].getName()))
        {
            AttributeInfo[] attrsNew = new AttributeInfo[attrs.length-1];
            for(int i=1; i<attrs.lenght; i++)
                attrsNew[i-1] = attrs[i];
            managedBean.setAttributes(attrsNew);
        }
*/
    }

    public String toString()
    {
        String str =  "\n--- MBeanRegistryEntry:\n   ";
        if(managedBean!=null)
        {
            str = str + managedBean.toString() + "\n   " +
            MBeanMetaHelper.descriptorToString(managedBean.getFields());
        }
        AttributeInfo[] attrs = managedBean.getAttributes();
        str =  str + "\n\n      --- Attributes:";
        if(attrs!=null)
            for(int i=0; i<attrs.length; i++)
            {
                if("modelerType".equals(attrs[i].getName()))
                    continue;
                str = str + "\n         " + attrs[i].toString();
                if(attrs[i].getFields().size()>0)
                   str = str + "\n            " + MBeanMetaHelper.descriptorToString(attrs[i].getFields());
            }

        OperationInfo[] opers = managedBean.getOperations();
        if(opers!=null && opers.length>0)
        {
            str =  str + "\n\n      --- Operations:";
            for(int i=0; i<opers.length; i++)
            {
                str = str + "\n         " + opers[i].toString();
                if(opers[i].getFields().size()>0)
                   str = str + "\n            " + MBeanMetaHelper.descriptorToString(opers[i].getFields());
                ParameterInfo[] params = opers[i].getSignature();
                if(params!=null && params.length>0)
                {
                    str =  str + "\n            --- Parameters:";
                    for(int j=0; j<params.length; j++)
                    {
                        str = str + "\n            " + params[j].toString();
                        if(params[j].getFields().size()>0)
                           str = str + "\n            " + MBeanMetaHelper.descriptorToString(params[j].getFields());
                    }
                }
            }
        }
        return str;
    }

    final static String filler="                                                                                                             ";
    private String fillStr(String name, int minSize)
    {
        if(name.length()>=minSize)
            return name;
        return name+filler.substring(0, minSize-name.length());
    }
    
    private String formatClassName(String className, int minSize)
    {
        if(className==null || className.length()==0)
            className = "void";
        else
        {
           int iLast = className.lastIndexOf('.');
           boolean bArray = className.startsWith("[");
           if(iLast>=0)
               className = className.substring(iLast+1);
           if(bArray)
               className =  className.substring(0, className.length()-1)+"[]";
        }
        return fillStr(className, minSize);
    }

    public String toFormatString()
    {
        String str =  "\n\n*************** mbean type:"+managedBean.getName()+" ***************";
        String wrk;
        str += "\n--- ObjectName ="+ fields.get(OBJECTNAME_FIELD_NAME);
        str += "\n--- XPath ="+ fields.get(XPATH_FIELD_NAME);
        AttributeInfo[] attrs = managedBean.getAttributes();
        str =  str + "\n--- Attributes:";
        if(attrs!=null)
            for(int i=0; i<attrs.length; i++)
            {
                if("modelerType".equals(attrs[i].getName()))
                    continue;
                str = str + "\n " + formatClassName(attrs[i].getType(), 12)+" "+attrs[i].getName()+",";
                if(attrs[i].getDescription()!=null)
                   str = str + str + " // "+ attrs[i].getDescription();
            }

        OperationInfo[] opers = managedBean.getOperations();
        if(opers!=null && opers.length>0)
        {
            str =  str + "\n--- Operations:";
            for(int i=0; i<opers.length; i++)
            {
                wrk  = formatClassName(opers[i].getReturnType(), 14) + " " +  opers[i].getName() + "(";
                str = str + "\n"+wrk;
                //convert wrk to indent
                wrk = fillStr("\n", wrk.length()+1);
                ParameterInfo[] params = opers[i].getSignature();
                if(params!=null && params.length>0)
                {
                    for(int j=0; j<params.length; j++)
                    {
                        if(j!=0)
                            str += wrk;
                        str = str + formatClassName(params[j].getType(), 0) + " " +  params[j].getName();
                    }
//                    str = str + "[, Target target]";
                }
//                else
//                    str = str + "[Target target]";
                    
                str = str + ");";
                if(opers[i].getDescription()!=null)
                   str = str + str + " // "+ opers[i].getDescription();
            }
        }
        return str;
    }

    //******************************************************************************************
    static private HashMap convertListOfFieldsToHashMap(List fields)
    {
        Iterator iter = fields.iterator();
        HashMap fldsMap = new HashMap();
        while(iter.hasNext())
        {
            FieldInfo field = (FieldInfo)iter.next();
            fldsMap.put(field.getName(), field.getValue());
        }
        return fldsMap;
    }
    
    //******************************************************************************************
    static private Set convertDelimListToSet(String list)
    {
        HashSet set = new HashSet();
        StringTokenizer tokenizer = new StringTokenizer(list, ", ");
        while(tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            if(token.length()>0) //?
                set.add(token);
        }
        return set;
    }
    
    //******************************************************************************************
    public String getName()
    {
        if(managedBean!=null)
            return managedBean.getName();
        return null;
    }
    //******************************************************************************************
    public MBeanNamingDescriptor getNamingDescriptor()
    {
        return namingDescriptor;
    }
    //******************************************************************************************
    public boolean isObjectNamePatternMatch(Hashtable ht)
    {
        if(namingDescriptor!=null)
            return namingDescriptor.isObjectNamePatternMatch(ht);
        return false;
    }

    //******************************************************************************************
    public boolean isObjectNamePatternMatch(ObjectName objectName)
    {
        if(namingDescriptor!=null)
            return namingDescriptor.isObjectNamePatternMatch(objectName);
        return false;
    }

    //******************************************************************************************
    public String[] getAttributeNames()
    {
        AttributeInfo[] infos = managedBean.getAttributes();
        if(infos==null)
            return (new String[0]);
        String[] names = new String[infos.length];
        for(int i=0; i<infos.length; i++)
            names[i] = infos[i].getName();
        return names;
    }
    //******************************************************************************************
    public void mergeAttribute( String attrName, String attrType, 
                                String attrGetMethod, String attrSetMethod,
                                boolean attrReadable, boolean attrWriteable)
    {
        AttributeInfo ai = findAttributeInfo(attrName);
        if(ai==null)
        {
            ai = new AttributeInfo();
            ai.setName(attrName);
            ai.setType(attrType);
            managedBean.addAttribute(ai);
        }
        if(ai.isReadable() && !attrReadable)
            ai.setReadable(false);
        if(ai.isWriteable() && !attrWriteable)
            ai.setWriteable(false);
        if(attrGetMethod!=null)
            ai.setGetMethod(attrGetMethod);
        if(attrSetMethod!=null)
            ai.setSetMethod(attrSetMethod);
    }

   
    //******************************************************************************************
    private FeatureInfo findFeatureInfo(String attrName, FeatureInfo[] features)
    {
        if(attrName==null)
            return null;
        for(int i=0; i<features.length; i++)
        {
            if(attrName.equals(features[i].getName()))
                return features[i];
        }
        return null;
    }
    
    //******************************************************************************************
    private AttributeInfo findAttributeInfo(String attrName)
    {
        return (AttributeInfo)findFeatureInfo(attrName, (FeatureInfo[]) managedBean.getAttributes());
    }

    //******************************************************************************************
    //  returns name for ElementChangeEvent assigned to entry
    //  it analyses the "ElementChangeEvent"-atrribute value for given MBean descriptor
    //  in mbean-descriptor file.
    public String getElementChangeEventName()
    {
        return (String)this.fields.get(ELEMENTCHANGEEVENT_FIELD_NAME);
    }

    //******************************************************************************************
    //  returns set represnting  list of dynamically reconfigurable attributes
    //  it analyses the "dynamicallyReconfigurable"-atrribute value for given MBean descriptor
    //  in mbean-descriptor file (converted to set from comma-separated list).
    public Set getDynamicallyReconfigurableAttributes()
    {
        return (Set)this.fields.get(DYNAMICALLY_RECONFIGURABLE_LIST_FIELD_NAME);
    }

    //******************************************************************************************
    private boolean checkItemsInReconfigurableSet(String name1, String name2)
    {
        Set set = getDynamicallyReconfigurableAttributes();
        if(set==null || set.size()<1)
            return false;
        if(set.contains("**") ||
           set.contains(name1) || 
           (name2!=null && set.contains(name2)))
            return true;
        return false; 
    }
    //******************************************************************************************
    public boolean isAttributeDynamicallyReconfigurable(String attr)
    {
        return checkItemsInReconfigurableSet(attr, "*");
    }
    //******************************************************************************************
    public boolean isPropertyDynamicallyReconfigurable(String attr)
    {
        return checkItemsInReconfigurableSet("property."+attr, "property.*");
    }
    //******************************************************************************************
    public boolean isElementCreationDynamicallyReconfigurable()
    {
        return checkItemsInReconfigurableSet("+", null);
    }
    //******************************************************************************************
    public boolean isElementDeletionDynamicallyReconfigurable()
    {
        return checkItemsInReconfigurableSet("-", null);
    }
    //******************************************************************************************
    public boolean isElementDynamicallyReconfigurable()
    {
        return checkItemsInReconfigurableSet("**", null);
    }

    //******************************************************************************************
    //  returns boolean indicating whether empty value allowed or not for given MBean attribute
    //  it analyzes the "emptyValueAllowed"-atrribute value for given MBean attribute descriptor
    //  in mbean-descriptor file.
    //  returns true, if only it is has "true" or "yes" value 
    public boolean isAttributeEmptyValueAllowed(String attrName) throws MBeanMetaException
    {
        AttributeInfo ai = findAttributeInfo(attrName);
        if(ai==null)
            throw new MBeanMetaException("Attribute info is not founmd for attribute "+attrName);
        //get attribute's descriptor fields
        List fields = ai.getFields();
        if(fields!=null)
            for(int i=0; i<fields.size(); i++) //enum fields
            {
                if(EMPTYVALUEALLOWED_FIELD_NAME.equals(((FieldInfo)fields.get(i)).getName()))
                {
                    //get "emptyValueAllowed" field value and compare it with "true"
                    Object value = ((FieldInfo)fields.get(i)).getValue();
                    if(value!=null && (value instanceof String) &&
                       ("true".equalsIgnoreCase((String)value)|| "yes".equalsIgnoreCase((String)value)))
                        return true; //empty value is allowed
                }
            }
        return false; //empty value is not allowed
    }

    public boolean isAttributeDynamicReconfigNeeded(String attrName) throws MBeanMetaException
    {
        AttributeInfo ai = findAttributeInfo(attrName);
        if(ai==null)
            throw new MBeanMetaException("Attribute info is not founmd for attribute "+attrName);
        //get attribute's descriptor fields
        List fields = ai.getFields();
        if(fields!=null)
            for(int i=0; i<fields.size(); i++) //enum fields
            {
                if("dynamicReconfigNeeded".equals(((FieldInfo)fields.get(i)).getName()))
                {
                    //get "emptyValueAllowed" field value and compare it with "true"
                    Object value = ((FieldInfo)fields.get(i)).getValue();
                    if(value!=null && (value instanceof String) &&
                       ("false".equalsIgnoreCase((String)value)|| "no".equalsIgnoreCase((String)value)))
                        return false;
                }
            }
        return true;
    }

    //******************************************************************************************
    public ModelMBeanInfo createMBeanInfo(MBeanNamingInfo namingInfo, String domainName) throws Exception
    {
        //force managedBean to reset MBeanInfo
        managedBean.setDescription(managedBean.getDescription());
        
        ModelMBeanInfo info = managedBean.createMBeanInfo();
        Descriptor descr = info.getMBeanDescriptor();
        if(domainName!=null)
            descr.setField(DOMAIN_FIELD_NAME, domainName);
        if(namingInfo!=null)
        {
            descr.setField(NMTYPE_FIELD_NAME, namingInfo.getType());
            descr.setField(NMLOCATION_FIELD_NAME, namingInfo.getLocationParams());
        }
        info.setMBeanDescriptor(descr);
        return info;
    }
    
    //******************************************************************************************
    public BaseAdminMBean instantiateMBean(ObjectName objectName, Object managedResource, ConfigContext ctx) 
                                   throws Exception
    {
        String domainName = objectName.getDomain();
        return instantiateMBean(new MBeanNamingInfo(namingDescriptor, objectName), managedResource, domainName, ctx);
    }
    
    //******************************************************************************************
    public BaseAdminMBean instantiateMBean(String type, String[] location, Object managedResource, String domainName, ConfigContext ctx) 
                                   throws Exception
    {
        return instantiateMBean(new MBeanNamingInfo(namingDescriptor, type, location), managedResource, domainName, ctx);
    }

    //******************************************************************************************
    private BaseAdminMBean instantiateMBean(MBeanNamingInfo namingInfo, Object managedResource, String domainName, ConfigContext ctx) 
                                   throws Exception
    {
        if(managedBean==null)
            return null;
        ModelMBeanInfo mbi = createMBeanInfo(namingInfo, domainName);
        //FIXME set fields xpath&object_name here
        String clazz = managedBean.getClassName();
        String group = managedBean.getGroup();
        
        Constructor constructor  = Class.forName(clazz).getConstructor();
        BaseAdminMBean mbean = (BaseAdminMBean)constructor.newInstance();
        mbean.setModelMBeanInfo(mbi);

        //BaseConfigMBean mbean = new BaseConfigMBean(mbi);
        String xpath = namingInfo.getXPath();
        if("config".equals(group))
        {
            if(xpath!=null && xpath.length()>0)
            {
                Object configBean = (Object)ConfigBeansFactory.getConfigBeanByXPath(ctx, xpath);
                if(configBean!=null)
                    mbean.setManagedResource(configBean, CONFIG_BEAN_REF);
                else
                    throw new MBeanMetaException("instantiateMBean(): config mbean does not exist for xpath - " + xpath );
            }
            return mbean;
        }
        if( "runtime".equals(group))
        {
            //CONFIG BEAN
            if(xpath!=null && xpath.length()>0)
            {
                Object configBean = (Object)ConfigBeansFactory.getConfigBeanByXPath(ctx, xpath);
                if(configBean!=null)
                    mbean.setManagedResource(configBean, CONFIG_BEAN_REF);
            }

 
            //JSR77BEAN
            //construct JSR77Beanname
            String beanClassName = (String)fields.get(JSR77BEAN_FIELD_NAME);
            if(beanClassName==null || beanClassName.length()==0)
                beanClassName = "com.sun.enterprise.management.model."+namingInfo.getType()+"Mdl";
            
            if(managedResource==null)
            {
                Class    cl = Class.forName(beanClassName);
                if(cl!=null)
                {
                    String[] location = namingInfo.getLocationParams();
                    if(location!=null)
                    {
                        Constructor ctr  = cl.getConstructor(new Class[]{location.getClass()});
                        if(ctr!=null)
                           managedResource = ctr.newInstance(new Object[]{location});

                    }
                }
            }
            if(managedResource!=null)
                mbean.setManagedResource(managedResource, JSR77_MODEL_BEAN_REF);
            return mbean;

        }
        return null;
    }
    public String getElementPrintName()
    {
       return (String)fields.get(PRINTNAME_FIELD_NAME);        
    }
}

