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
 *   $Id: MBeanMetaHelper.java,v 1.5 2006/03/12 01:26:56 jluehe Exp $
 *   @author: alexkrav
 *
 *   $Log: MBeanMetaHelper.java,v $
 *   Revision 1.5  2006/03/12 01:26:56  jluehe
 *   Renamed AS's org.apache.commons.* to com.sun.org.apache.commons.*, to avoid collisions with org.apache.commons.* packages bundled by webapps.
 *
 *   Tests run: QL, Servlet TCK
 *
 *   Revision 1.4  2005/12/25 03:47:37  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.3  2005/08/16 22:19:31  kravtch
 *   M3: 1. ConfigMBeans: Support for generic getXXXNamesList() operation (request from management-rules).
 *       2. MBeanRegistry: support for getElementPrintName() to provide readable element's description for validator's messages
 *   Submitted by: kravtch
 *   Reviewed by: Shreedhar
 *   Affected modules admin-core/admin
 *   Tests passed: QLT/EE + devtests
 *
 *   Revision 1.2  2005/06/27 21:19:43  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.15  2004/11/14 07:04:21  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.14  2004/05/22 00:35:07  kravtch
 *   "system-properties" backend support is added
 *   Reviewer: Sridatta
 *   Tests passed: QLT/CTS PE
 *
 *   Revision 1.13  2004/02/20 03:56:14  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *   Revision 1.11.4.3  2004/02/02 07:25:19  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.11.4.2  2003/12/23 01:51:45  kravtch
 *   Bug #4959186
 *   Reviewer: Sridatta
 *   Checked in PE8FCS_BRANCH
 *   (1) admin/admin-core/admin-cli/admin-gui/appserv-core/assembly-tool: switch to new domain name "ias:" -> "com.sun.appserv"
 *   (2) admin-core and admin-cli: switch to "dashed" attribute names
 *   (3) admin-core: support both "dashed"/"underscored" names for get/setAttributes
 *   (4) admin-gui: hook for reverse converting attribute names (temporary hack);
 *
 *   Revision 1.11.4.1  2003/12/01 21:52:39  kravtch
 *   Bug #4939964
 *   Reviewer: Sridatta
 *   admin.config.ManagedConfigBean.createChildByType() now analyzes registryEntries.AttributeInfo for each "empty"  valued attribute (similar to setAttribute(), but could not use MBeanAttributeInfo because MBean is not exists yet). If "emptyValueAllowed" field in registrEntry.AttributeInfo is not "true", then "empty" attribute will be ignored.
 *
 *   Revision 1.11  2003/09/25 18:01:58  kravtch
 *   Bug #4927601
 *   Reviewer: Sreeni
 *   MBeanMetaHelper does not rejects custom operations which match to "standard" JMX names, but does it only if signature matches too.
 *
 *   Revision 1.10  2003/09/05 06:06:10  kravtch
 *   Bug #4915176
 *   Reviewer: Sridatta
 *      - new custom mbean - DomainMBean implemented public AttributeList getDefaultCustomProperties(String mbeanTypeName, AttributeList attributeList) to fulfill the reques;
 *      - descriptor file is updated (classname for this mbean added)
 *      - mandatory getDefaultCustomProperties() removed from all MBeanInfos;
 *      - CLI name for http-listener.ssl repared in descriptors file;
 *      - non-neccessary logs suppressed;
 *
 *   Revision 1.9  2003/08/20 15:36:42  kravtch
 *   typo correct getDefaulCustomProperties->getDefaultCustomProperties
 *
 *   Revision 1.8  2003/08/15 23:08:28  kravtch
 *   DottedName Support (generation and call to manager)
 *   notifyRegisterMBean/UnregisterMBean are implemented;
 *   dotted name related opeartions are added to NaminDescriptor and NamingInfo
 *   removeChild support is added;
 *
 *   Revision 1.7  2003/08/14 23:44:44  kravtch
 *   repare for attributes started from "default" they were treated as read-only because of conflicting with getDefaultXXXvalues.
 *   now introspector skips Static methods.
 *
 *   Revision 1.6  2003/08/07 00:41:06  kravtch
 *   - new DTD related changes;
 *   - properties support added;
 *   - getDefaultAttributeValue() implemented for config MBeans;
 *   - merge Jsr77 and config activity in runtime mbeans;
 *
 *   Revision 1.5  2003/07/29 18:59:35  kravtch
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
 *   Revision 1.4  2003/07/18 20:14:44  kravtch
 *   1. ALL config mbeans are now covered by descriptors.xml
 *   2. new infrastructure for runtime mbeans is added
 *   3. generic constructors added to jsr77Mdl beans (String[])
 *   4. new test cases are added to admintest
 *   5. MBeanRegistryFactory has now different methods to obtain admin/runtime registries
 *   6. runtime-descriptors xml-file is added to build
 *
 *   Revision 1.3  2003/06/25 20:03:40  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/

package com.sun.enterprise.admin.meta;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.sun.org.apache.commons.modeler.ManagedBean;
import com.sun.org.apache.commons.modeler.FeatureInfo;
import com.sun.org.apache.commons.modeler.FieldInfo;
import com.sun.org.apache.commons.modeler.AttributeInfo;
import com.sun.org.apache.commons.modeler.OperationInfo;
import com.sun.org.apache.commons.modeler.ParameterInfo;

//JMX imports
import javax.management.Descriptor;
import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.NotificationBroadcasterSupport;
import com.sun.enterprise.config.ConfigBeansFactory;

/**
 *
 */
public class MBeanMetaHelper implements MBeanMetaConstants
{
    
    static int ONLY_CHILD=1;
    static int MULTY_CHILDS=2;
    
    static int SETTER_METHODTYPE = 1;
    static int GETTER_METHODTYPE = 2;
    /** Process the methods and extract 'attributes', methods, etc
     *
     */
    //*****************************************************************************
    public static void mergeWithConfigBean(ManagedBean managedBean, Class objectClass, int mode)
    {
        
        if(objectClass==null)
            return;
        Hashtable attrs = new Hashtable();
        Hashtable children = new Hashtable();
        int shift   = 0;
        
        //Introspect and get all the methods
        Method[] methods = objectClass.getMethods();
        for (int j = 0; j < methods.length; ++j)
        {
            String methodName=methods[j].getName();
            
            //----------GETTER----------------
            if( methodName.startsWith("get") || methodName.startsWith("is"))
            {
                shift = methodName.startsWith("is")?2:3;
                if( Modifier.isStatic(methods[j].getModifiers()) || !Modifier.isPublic(methods[j].getModifiers()) )
                {
                    continue;
                }
                Class params[]=methods[j].getParameterTypes();
                if( params.length != 0 )
                {
                    continue;
                }
                if( methods[j].getDeclaringClass() != objectClass )
                    continue;
                
                Class ret=methods[j].getReturnType();
                if( ! supportedType( ret ) )
                {
                    //maybe this is child 
                    String childClassName = ret.getName();
                    if(childClassName.endsWith("."+methodName.substring(shift)) ||
                       childClassName.endsWith("."+methodName.substring(shift)+";"))
                    {
                        children.put(methodName.substring(shift), childClassName); 
                    }
                    continue;
                }
                if((mode&EXPOSE_GETTERS)==0 )
                    continue;
//                if(methodName.startsWith( "getDefault" ))
//                    continue;
                
                AttrIntro ai = (AttrIntro)attrs.get(getAttrNameFromMethodName(methodName, true));
                if(ai==null)
                {
                    ai = new AttrIntro();
                    ai.name = getAttrNameFromMethodName(methodName, true);
                    attrs.put(ai.name, ai);
                }
                if(ai.type!=null)
                {
                    if(!ai.type.equals(ret.getName()))
                        continue;
                }
                else
                {
                    ai.type = ret.getName();
                }
                ai.getName = methodName;
                //----------SETTER----------------
            } else if( methodName.startsWith( "set" ) )
            {
                if((mode&EXPOSE_SETTERS)==0 )
                    continue;

                Class params[]=methods[j].getParameterTypes();
                if( params.length != 1 )
                {
                    continue;
                }
                if( ! supportedType( params[0] ) )
                {
                    continue;
                }
                if( ! Modifier.isPublic( methods[j].getModifiers() ) )
                {
                    continue;
                }
                if( methods[j].getDeclaringClass() != objectClass )
                    continue;

                AttrIntro ai = (AttrIntro)attrs.get(getAttrNameFromMethodName(methodName, true));
                if(ai==null)
                {
                    ai = new AttrIntro();
                    ai.name = getAttrNameFromMethodName(methodName, true);
                    attrs.put(ai.name, ai);
                }
                if(ai.type!=null)
                {
                    if(!ai.type.equals(params[0].getName()))
                        continue;
                }
                else
                {
                    ai.type = params[0].getName();
                }
                ai.setName = methodName;
                if(methodName.startsWith( "setDefault" )) //???
                {
                    ai.setName = "set"+methodName.substring(3);
                }
                
            } else
            {
                continue;
            }
        }
        
        OperationInfo operationInfo;

        //**** A T T R I B U T E S ******
//        attrs.remove("x_path");
//        attrs.remove("attribute_names");
//        attrs.remove("monitoring_enabled");
        
        if(attrs.size()>0)
        {
            AttributeInfo[] infos = managedBean.getAttributes();
            Hashtable infosTable = new Hashtable();
            for(int i=0; i<infos.length; i++)
            {
                infosTable.put(infos[i].getName(), infos[i]);
            }
            
            String key;
            Enumeration keys = attrs.keys();
            while(keys.hasMoreElements())
            {
                key = (String)keys.nextElement();
                AttrIntro ai = (AttrIntro)attrs.get(key);
                AttributeInfo info = (AttributeInfo)infosTable.get(key);
                if(info==null)
                {
                    ai.whereType = LOCATED_IN_CONFIGBEAN; 
                    info = ai.createAttributeInfo();
                    managedBean.addAttribute(info);
                    infosTable.put(key, info);
                }
                else
                {
                    ai.mergeWithAttributeInfo(info);
                }
            }

            //getDefaultAttributeValue
            operationInfo = createOperationInfo("getDefaultAttributeValue", "INFO", 
                   "java.lang.String", 
                   new ParameterInfo("attributeName", "java.lang.String", null),
                   LOCATED_IN_CONFIGBEAN);
            mergeWithOperationInfo(managedBean, operationInfo);
        }
        
        //classnames values for method's types
        String nameClass        = "javax.management.ObjectName";
        String attrClass        = "javax.management.Attribute";
        String namesClass       = (new ObjectName[0]).getClass().getName();
        String attrListClass    = (new AttributeList()).getClass().getName();
        String stringsClass     = (new String[0]).getClass().getName();

        FieldInfo field;
        ParameterInfo param;

        // **** P R O P E R T I E S  ******
        if(children.get("ElementProperty")!=null)
        {
            children.remove("ElementProperty");

            
            //getProperties
            operationInfo = createOperationInfo("getProperties", "INFO", 
                   attrListClass, null, LOCATED_IN_CONFIGBEAN);
            mergeWithOperationInfo(managedBean, operationInfo);
            
            //getDefaulCustomProperties
//            operationInfo = createOperationInfo("getDefaultCustomProperties", "INFO", 
//                   attrListClass, null, null);
//            mergeWithOperationInfo(managedBean, operationInfo);
            
            //getProperty
            operationInfo = createOperationInfo("getPropertyValue", "INFO", 
                   "java.lang.Object", 
                   new ParameterInfo("propertyName", "java.lang.String", null),
                   LOCATED_IN_CONFIGBEAN);
            mergeWithOperationInfo(managedBean, operationInfo);
            
            //setProperty
            operationInfo = createOperationInfo("setProperty", "ACTION", 
                   "void", 
                   new ParameterInfo("nameAndValue", attrClass, null),
                   LOCATED_IN_CONFIGBEAN);
            mergeWithOperationInfo(managedBean, operationInfo);
        }

        // **** S Y S T E M       P R O P E R T I E S  ******
        if(children.get("SystemProperty")!=null)
        {
            children.remove("SystemProperty");
            
            //getSystemProperties
            operationInfo = createOperationInfo("getSystemProperties", "INFO", 
                   attrListClass, null, LOCATED_IN_CONFIGBEAN);
            mergeWithOperationInfo(managedBean, operationInfo);
            
            //getSystemProperty
            operationInfo = createOperationInfo("getSystemPropertyValue", "INFO", 
                   "java.lang.Object", 
                   new ParameterInfo("propertyName", "java.lang.String", null),
                   LOCATED_IN_CONFIGBEAN);
            mergeWithOperationInfo(managedBean, operationInfo);
            
            //setSystemProperty
            operationInfo = createOperationInfo("setSystemProperty", "ACTION", 
                   "void", 
                   new ParameterInfo("nameAndValue", attrClass, null),
                   LOCATED_IN_CONFIGBEAN);
            mergeWithOperationInfo(managedBean, operationInfo);
        }

        //**** C H I L D R E N  ******

        //children.remove("ElementProperty");
        
        if(children.size()>0)
        {
            String key;
            Enumeration keys = children.keys();
            while(keys.hasMoreElements())
            {
                key = (String)keys.nextElement();
                String clazz = (String)children.get(key);
                boolean bMulti = clazz.charAt(0)=='['?true:false;
                String  childName = getAttrNameFromMethodName(key, true, '-');
                //getter
                if((mode&EXPOSE_GETCHILD)!=0)
                {
                    //get child (ObjectName)
                    operationInfo = createOperationInfo("get"+key, "INFO", 
                           bMulti?namesClass:nameClass, 
                           null, LOCATED_IN_CONFIGBEAN);
                    addDataToChildOperInfo(childName, bMulti, operationInfo);
                    mergeWithOperationInfo(managedBean, operationInfo);
                    if(bMulti)
                    {
                        //get child NamesList(ObjectName)
                        operationInfo = createOperationInfo(
                             "get"+key+GET_LISTNAMES_OP_SUFFIX, 
                             "INFO", stringsClass, null, null);
                        addDataToChildOperInfo(childName, bMulti, operationInfo);
                        mergeWithOperationInfo(managedBean, operationInfo);
                    }
                    
                    //getChildByKey
                    if(bMulti)
                    {
                        String prefix = "get"+key+"By";
                        for (int j = 0; j < methods.length; ++j)
                        {
                            String methodName=methods[j].getName();
                            if(methodName.startsWith(prefix))
                            {
                                operationInfo = createOperationInfo(methodName, "INFO", 
                                       nameClass, 
                                       new ParameterInfo("key", "java.lang.String", null),
                                       LOCATED_IN_CONFIGBEAN);
                                addDataToChildOperInfo(childName, bMulti, operationInfo);
                                mergeWithOperationInfo(managedBean, operationInfo);
                                break;
                            }
                        }
                    }
                }
                if((mode&EXPOSE_CREATECHILD)!=0)
                {
                    //CreateChild
                    String prefix = bMulti?"add"+key:"set"+key;
                    for (int j = 0; j < methods.length; ++j)
                    {
                        String methodName=methods[j].getName();
                        if(methodName.startsWith(prefix))
                        {
                            operationInfo = createOperationInfo("create"+key, "ACTION_INFO", 
                                   nameClass, 
                                   new ParameterInfo("attribute_list", attrListClass, null),
                                   LOCATED_IN_CONFIGBEAN);
                            addDataToChildOperInfo(childName, bMulti, operationInfo);
                            mergeWithOperationInfo(managedBean, operationInfo);
                            break;
                        }
                    }
                }
                if((mode&EXPOSE_DESTROYCHILD)!=0)
                {
                    if(!bMulti)
                    {
                        operationInfo = createOperationInfo("remove"+key, 
                               "ACTION", "void", null, LOCATED_IN_CONFIGBEAN);
                        addDataToChildOperInfo(childName, bMulti, operationInfo);
                        mergeWithOperationInfo(managedBean, operationInfo);
                    }
                    else
                    {
                        String prefix = "get"+key+"By";
                        for (int j = 0; j < methods.length; ++j)
                        {
                            String methodName=methods[j].getName();
                            if(methodName.startsWith(prefix))
                            {
                                operationInfo = createOperationInfo("remove"+methodName.substring(3), 
                                       "ACTION", "void", 
                                       new ParameterInfo("key", "java.lang.String", null),
                                       LOCATED_IN_CONFIGBEAN);
                                addDataToChildOperInfo(childName, bMulti, operationInfo);
                                mergeWithOperationInfo(managedBean, operationInfo);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    //*****************************************************************************
    public static AttrIntro findOrCreateAttrInfo(Hashtable attrs, String attrName, String methodName, Class supposedTypeClass, String whereType, int methodType)
    {
        AttrIntro ai = (AttrIntro)attrs.get(attrName);
        if(ai==null)
        {
            ai = new AttrIntro();
            ai.name = attrName;
            ai.type = supposedTypeClass.getName();
            if((methodType&GETTER_METHODTYPE)!=0)
                ai.getName = methodName;
            if((methodType&SETTER_METHODTYPE)!=0)
                ai.setName = methodName;
            ai.getName = methodName;
            ai.whereType = whereType;
            attrs.put(ai.name, ai);
        }
        else
           if(ai.type==null || ai.type.equals(supposedTypeClass.getName()))
           {
                if((methodType&GETTER_METHODTYPE)!=0)
                    ai.getName = methodName;
                if((methodType&SETTER_METHODTYPE)!=0)
                    ai.setName = methodName;
           }
           else
           {
               return null;
           }
        return ai;
    }
    
    //*****************************************************************************
    public static void mergeWithRuntimeModelBean(ManagedBean managedBean, Class objectClass)
    {
        
        if(objectClass==null)
            return;
        Hashtable attrs = new Hashtable();
        
        //Introspect and get all the methods
        Method[] methods = objectClass.getMethods();
        for (int j = 0; j < methods.length; ++j)
        {
            if( Modifier.isStatic(methods[j].getModifiers()))
                continue;
            if( ! Modifier.isPublic( methods[j].getModifiers() ) ) 
                continue;
            if( methods[j].getDeclaringClass() == Object.class )
                continue;
            
            String methodName=methods[j].getName();
            Class  params[]=methods[j].getParameterTypes();
            Class  ret=methods[j].getReturnType();
            
            //----------GETTER----------------
            if( (methodName.startsWith("get") || methodName.startsWith("is")) &&
//                !methodName.startsWith( "getDefault" ) &&
                params.length == 0 && 
                supportedType( ret ) )
            {
                findOrCreateAttrInfo(attrs, getAttrNameFromMethodName(methodName,false), methodName, ret, LOCATED_IN_RUNTIMEBEAN, GETTER_METHODTYPE);
            } 
            //----------SETTER----------------
            else if( methodName.startsWith( "set" ) &&
                     params.length == 1 &&
                     supportedType( params[0] ))
            {
                findOrCreateAttrInfo(attrs, getAttrNameFromMethodName(methodName,false), methodName, params[0], LOCATED_IN_RUNTIMEBEAN, SETTER_METHODTYPE);
                
            } 
            //----------OPERATIONS----------------
            else
            {
                OperationInfo operationInfo = getOperationInfo(methods[j], LOCATED_IN_RUNTIMEBEAN);
                mergeWithOperationInfo(managedBean, operationInfo);
            }
        }
        
        //**** A T T R I B U T E S ******
        if(attrs.size()>0)
        {
            AttributeInfo[] infos = managedBean.getAttributes();
            Hashtable infosTable = new Hashtable();
            for(int i=0; i<infos.length; i++)
            {
                infosTable.put(infos[i].getName(), infos[i]);
            }
            
            String key;
            Enumeration keys = attrs.keys();
            while(keys.hasMoreElements())
            {
                key = (String)keys.nextElement();
                AttrIntro ai = (AttrIntro)attrs.get(key);
                AttributeInfo info = (AttributeInfo)infosTable.get(key);
                if(info==null)
                {
                    ai.whereType = LOCATED_IN_RUNTIMEBEAN; 
                    info = ai.createAttributeInfo();
                    managedBean.addAttribute(info);
                    infosTable.put(key, info);
                }
                else
                {
                    ai.mergeWithAttributeInfo(info);
                }
            }
        }
        
    }

    
    //*****************************************************************************
    private static Class forNameOrNull(String str)
    {
        try {
            return Class.forName(str);
        } catch (Exception e) {
            return null;
        }
    }
    //*****************************************************************************
    private static boolean isMethodMatch(Method m, String name, Class[] clParams)
    {
        if(!name.equals(m.getName()))
           return false;
        Class[] cls = m.getParameterTypes();
        if(clParams==null)
            if(cls==null || cls.length==0)
                return true;
            else
                return false;
        if(cls==null)
            if(clParams.length==0)
                return true;
            else
                return false;
         if(clParams.length!=cls.length)
             return false;
        for(int i=0; i<cls.length; i++)
            if(!clParams[i].equals(cls[i]))
                return false;
        return true;
    }
static Class[] _clsStr          = new Class[]{(new String()).getClass()};
static Class[] _clsStrArr       = new Class[]{(new String[0]).getClass()};
static Class[] _clsAttr         = new Class[]{forNameOrNull("javax.management.Attribute")};
static Class[] _clsAttrList     = new Class[]{forNameOrNull("javax.management.AttributeList")};
static Class[] _clsServAndOname = new Class[]{forNameOrNull("javax.management.MBeanServer"), forNameOrNull("javax.management.ObjectName")};
static Class[] _clsModelMBI     = new Class[]{forNameOrNull("javax.management.modelmbean.ModelMBeanInfo") };
static Class[] _clsBoolean      = new Class[]{(new Boolean(true)).getClass()};
static Class[] _clsObjAndStr    = new Class[]{(new Object()).getClass(), (new String()).getClass()};
static Class[] _clsInvokeParms  = new Class[]{(new String()).getClass(), (new Object[0]).getClass(), (new String[0]).getClass()};
    //*****************************************************************************
    public static void mergeWithDynamicMBean(ManagedBean managedBean, Class objectClass)
    {

        if(objectClass==null)
            return;
        //Introspect and get all the methods
        Method[] methods = objectClass.getMethods();
        for (int j = 0; j < methods.length; ++j)
        {
            if( Modifier.isStatic(methods[j].getModifiers()))
                continue;
            if( ! Modifier.isPublic( methods[j].getModifiers() ) ) 
                continue;

            if( methods[j].getDeclaringClass() == Object.class )
                continue;
            Class declaringClass = methods[j].getDeclaringClass();
//            if( declaringClass == DynamicMBean.class )
//                continue;
            if( NotificationBroadcasterSupport.class.equals(declaringClass) )
                continue;
            String methodName=methods[j].getName();
            if( isMethodMatch(methods[j], "getAttribute", _clsStr)          || 
                isMethodMatch(methods[j], "getAttributes", _clsStrArr)      ||
                isMethodMatch(methods[j], "setAttribute", _clsAttr)         || 
                isMethodMatch(methods[j], "setAttributes", _clsAttrList)    ||
                isMethodMatch(methods[j], "preRegister", _clsServAndOname)  || 
                isMethodMatch(methods[j], "postRegister", _clsBoolean)  ||
                isMethodMatch(methods[j], "preDeregister", null) || 
                isMethodMatch(methods[j], "postDeregister", null)||
                isMethodMatch(methods[j], "setManagedResource", _clsObjAndStr) || 
                isMethodMatch(methods[j], "setModelMBeanInfo", _clsModelMBI)||
                isMethodMatch(methods[j], "getMBeanInfo", null)  || 
                isMethodMatch(methods[j], "invoke", _clsInvokeParms) )
                continue;
            OperationInfo operationInfo = getOperationInfo(methods[j], LOCATED_IN_MBEAN);
            mergeWithOperationInfo(managedBean, operationInfo);
        }
        
    }
    
    //*****************************************************************************
    private static OperationInfo getOperationInfo(Method method, String whereType)
    {
        OperationInfo info = new OperationInfo();
        info.setName(method.getName());
        info.setReturnType(method.getReturnType().getName());
        Class paramsClasses[]=method.getParameterTypes();
        for(int k=0; k<paramsClasses.length; k++)
        {
            info.addParameter(new ParameterInfo("param"+(k+1), paramsClasses[k].getName(), null));
        }
        if (whereType!=null)
            info.addField(newField(WHERE_LOCATED_FIELD_NAME, whereType));
        return info;
    }
    
    //*****************************************************************************
    private static OperationInfo createOperationInfo(String name, String impact, 
          String returnType, ParameterInfo param, 
          String whereType)
    {
        OperationInfo info = new OperationInfo();
        info.setName(name);
        info.setImpact(impact);
        if(returnType!=null)
           info.setReturnType(returnType);
        if(param!=null)
            info.addParameter(param);
        if (whereType!=null)
            info.addField(newField(WHERE_LOCATED_FIELD_NAME, whereType));
        return info;
    }
    //*****************************************************************************
    private static void mergeWithOperationInfo(ManagedBean managedBean, OperationInfo info)
    {
        //OperationInfo[] infos = managedBean.getOperations();
        //FIXME
        managedBean.addOperation(info);
    }
            
    private static String strArray[]=new String[0];
    //*****************************************************************************
    private static boolean supportedType( Class ret )
    {
        return ret == String.class ||
        ret == Integer.class ||
        ret == Integer.TYPE ||
        ret == Long.class ||
        ret == Long.TYPE ||
        ret == java.io.File.class ||
        ret == Boolean.class ||
        ret == Boolean.TYPE ||
        ret == strArray.getClass() || // XXX ???
        ret == ObjectName.class
        ;
    }

    //*****************************************************************************
    private static String getAttrNameFromMethodName(String name, boolean bDecamelaze)
    {
        return getAttrNameFromMethodName(name, bDecamelaze, ATTRIBUTE_NAME_DELIMITER_SYMBOL);
    }
    //*****************************************************************************
    private static String getAttrNameFromMethodName(String name, boolean bDecamelaze, char separatorSymbol)
    {
        if(name.startsWith("set") || name.startsWith("get"))
            name = name.substring(3);
        else
            if(name.startsWith("is"))
                name = name.substring(2);
        if(!bDecamelaze)
            return name;
        if(name.length()==0)
            return name;
        String attrName = name.toLowerCase();
        char[] arr1 =  name.toCharArray();
        char[] arr2 =  name.toLowerCase().toCharArray();
        StringBuffer buf = new StringBuffer(arr1.length*2);
        buf.append(arr2[0]);
        for(int i=1; i<arr1.length; i++)
        {
            if(arr1[i]!=arr2[i])
                buf.append(separatorSymbol);
            buf.append(arr2[i]);
        }
        return buf.toString();
    }
    
    //****************************************************************************************************
    public static Class getConfigBeanClass(String xPath)
    {
        // get ConfigBean classname from XPath
        String beanName = ConfigBeansFactory.getConfigBeanNameByXPath(xPath);
        //getting the class object
        try
        {
            Class cl = Class.forName("com.sun.enterprise.config.serverbeans."+beanName);
            return cl;
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    //****************************************************************************************************
    public static Class getRuntimeModelBeanClass(String j2eeType)
    {
        try
        {
            return Class.forName("com.sun.enterprise.management.model."+j2eeType+"Mdl");
        }
        catch(Exception e)
        {
//            e.printStackTrace();
            return null;
        }
    }
    
    //****************************************************************************************************
    public static String descriptorToString(FeatureInfo descr)
    {
        return descriptorToString(descr.getFields());
    }
    //****************************************************************************************************
    public static String descriptorToString(List fields)
    {
        String str = "Descriptor[";
        for(int i=0; i<fields.size(); i++)
        {
            FieldInfo field = (FieldInfo)fields.get(i);
            str = str + field.getName() + "=" + field.getValue() + " ";
        }
        return  str + "]";
    }

    //*******************************************************************************************
    private static FieldInfo newField(String name, Object value)
    {
        FieldInfo info = new FieldInfo();
        info.setName(name);
        info.setValue(value);
        return info;
    }

    //*******************************************************************************************
    private static class AttrIntro
    {
        public String name    = null;
        public String type    = null;
        public String getName = null;
        public String setName = null;
        public String whereType = null;
        
        public AttributeInfo createAttributeInfo()
        {
            AttributeInfo ati = new AttributeInfo();
            ati.setName(name);
            ati.setType(type);
            if(setName!=null)
            {
                ati.setWriteable(true);
                ati.addField(newField(SETTER_FIELD_NAME, setName));
            }
            else
                ati.setWriteable(false);
            if(getName!=null)
            {
                ati.setReadable(true);
                ati.addField(newField(GETTER_FIELD_NAME, getName));
            }
            else
                ati.setReadable(false);
            
            if (whereType!=null)
                ati.addField(newField(WHERE_LOCATED_FIELD_NAME, whereType));
            return ati;
        }
        
        
        public void mergeWithAttributeInfo(AttributeInfo ati)
        {
            ati.setType(type);
            if(setName==null)
                ati.setWriteable(false);
            if(getName==null)
                ati.setReadable(false);
        }
    }

    //*******************************************************************************************
    private static void addFieldToInfo(String name, String value, FeatureInfo info)
    {
        FieldInfo field = new FieldInfo();
        field.setName(name);
        field.setValue(value);
        info.addField(field);
    }
    //*******************************************************************************************
    private static void addDataToChildOperInfo(String childName, boolean bMulti, FeatureInfo info)
    {
        addFieldToInfo(CHILD_FIELD_NAME, childName, info);
        if(bMulti)
           addFieldToInfo(MULTI_FIELD_NAME, "true", info);
    }

    //*******************************************************************************************
    // converts (if needed) the given name to corresponded MBean's Attribute name 
    public static String mapToMBeanAttributeName(String name)
    {
        if(ATTRIBUTE_NAME_DELIMITER_SYMBOL!='_' && name!=null)
            return name.replace('_', ATTRIBUTE_NAME_DELIMITER_SYMBOL);
        return name;    
    }
    //*******************************************************************************************
    // converts (if needed) the given name to corresponded ConfigBean Attribute name 
    public static String mapToConfigBeanAttributeName(String name)
    {
        if(name!=null)
            return name.replace('_', '-');
        return name;    
    }
    //XPATH helpers
   public static String cutAttributeTokenFromXPath(String xpath)
   {
       if(!xpath.endsWith("]") && !xpath.endsWith("/"))
       {
           int idx = xpath.lastIndexOf('/');
           if(idx>0 && xpath.length()>idx+1 && xpath.charAt(idx+1)=='@')
           {
               return xpath.substring(0, idx);
           }
       }
       return xpath;
   }
   public static String getMultipleElementKeyValue(String xpath)
   {
       if(xpath.endsWith("']"))
       {
           int idx = xpath.lastIndexOf('\'',  xpath.length()-3);
           if(idx>0)
           {
               return xpath.substring(idx+1, xpath.length()-2);
           }
       }
       return null;
   }
   public static String extractLastElemNameFromXPath(String xpath)
   {
       if(xpath.endsWith("]"))
       {
           int idx = xpath.lastIndexOf('[');
           if(idx>0)
              xpath=xpath.substring(0,idx);
       }
       int idx = xpath.lastIndexOf('/');
       if(idx>=0)
           return xpath.substring(idx+1);
       return null;
   }
   public static String cutLastElementFromXPath(String xpath)
   {
       if(xpath.endsWith("]"))
       {
           int idx = xpath.lastIndexOf('[');
           if(idx>0)
              xpath=xpath.substring(0,idx);
       }
       int idx = xpath.lastIndexOf('/');
       if(idx>=0)
           return xpath.substring(0, idx);
       return null;
   }
}

