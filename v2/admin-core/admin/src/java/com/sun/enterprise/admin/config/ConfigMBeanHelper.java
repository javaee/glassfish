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
 *   $Id: ConfigMBeanHelper.java,v 1.6 2006/03/12 01:26:56 jluehe Exp $
 *   @author: alexkrav
 *
 *   $Log: ConfigMBeanHelper.java,v $
 *   Revision 1.6  2006/03/12 01:26:56  jluehe
 *   Renamed AS's org.apache.commons.* to com.sun.org.apache.commons.*, to avoid collisions with org.apache.commons.* packages bundled by webapps.
 *
 *   Tests run: QL, Servlet TCK
 *
 *   Revision 1.5  2006/02/25 00:05:34  kravtch
 *   Bug #6378808(Unable to set/add a blank password property on JDBC Pool through Web UI)
 *   admin-core/admin/.../config:
 *     - new ConfigMBeanHelper.PROPERTY_SPECIAL_EMPTY_VALUE introduced;
 *     - ManagedConfigBean now checks setting property value; if it equals to SPECIAL_EMPTY_VALUE it will be set to "";
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules admin-core/admin
 *   Tests passed: QLT/EE
 *
 *   Revision 1.4  2005/12/25 03:47:30  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.3  2005/08/16 22:19:30  kravtch
 *   M3: 1. ConfigMBeans: Support for generic getXXXNamesList() operation (request from management-rules).
 *       2. MBeanRegistry: support for getElementPrintName() to provide readable element's description for validator's messages
 *   Submitted by: kravtch
 *   Reviewed by: Shreedhar
 *   Affected modules admin-core/admin
 *   Tests passed: QLT/EE + devtests
 *
 *   Revision 1.2  2005/06/27 21:19:40  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.9  2004/11/14 07:04:17  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.8  2004/06/04 19:17:03  kravtch
 *   Reviewer: Sridatta
 *   getConfigBeansObjectNames - helper methods are added to support convertion config beans to ObjcectNames
 *   Tests passed: QLT PE/EE
 *
 *   Revision 1.7  2004/02/20 03:56:07  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *   Revision 1.6.4.2  2004/02/02 07:25:14  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.6.4.1  2003/12/23 01:51:43  kravtch
 *   Bug #4959186
 *   Reviewer: Sridatta
 *   Checked in PE8FCS_BRANCH
 *   (1) admin/admin-core/admin-cli/admin-gui/appserv-core/assembly-tool: switch to new domain name "ias:" -> "com.sun.appserv"
 *   (2) admin-core and admin-cli: switch to "dashed" attribute names
 *   (3) admin-core: support both "dashed"/"underscored" names for get/setAttributes
 *   (4) admin-gui: hook for reverse converting attribute names (temporary hack);
 *
 *   Revision 1.6  2003/09/10 00:24:22  kravtch
 *   Reviewer: Sridatta
 *   New operation in DomainMBean is returning list of default attribute values according to mbean type. This operation will return DTD defined default values if custom mbean does not implement its own static operation overriding(extending) standard ones.
 *
 *   Revision 1.5  2003/08/07 00:41:04  kravtch
 *   - new DTD related changes;
 *   - properties support added;
 *   - getDefaultAttributeValue() implemented for config MBeans;
 *   - merge Jsr77 and config activity in runtime mbeans;
 *
 *   Revision 1.4  2003/07/18 20:14:43  kravtch
 *   1. ALL config mbeans are now covered by descriptors.xml
 *   2. new infrastructure for runtime mbeans is added
 *   3. generic constructors added to jsr77Mdl beans (String[])
 *   4. new test cases are added to admintest
 *   5. MBeanRegistryFactory has now different methods to obtain admin/runtime registries
 *   6. runtime-descriptors xml-file is added to build
 *
 *   Revision 1.3  2003/06/25 20:03:37  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/

package com.sun.enterprise.admin.config;

import java.util.Enumeration;
import java.util.Hashtable;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

//JMX imports
import javax.management.*;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanInfo;

import com.sun.org.apache.commons.modeler.AttributeInfo;

//admin
import com.sun.enterprise.admin.MBeanHelper;

import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.meta.MBeanRegistryEntry;
//naming
import com.sun.enterprise.admin.meta.naming.MBeanNamingInfo;
import com.sun.enterprise.admin.meta.naming.MBeanNamingDescriptor;
import com.sun.enterprise.admin.meta.MBeanMetaHelper;

//config
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ElementProperty;

/****************************************************************************************************************
 */
public class ConfigMBeanHelper extends MBeanHelper
{
   private static final String XPATH_SEPARATOR = "/";
   public static final String PROPERTY_NAME_PREFIX = "property.";    
   public static final String PROPERTY_SPECIAL_EMPTY_VALUE = "()";
    
    private ConfigBean        m_baseConfigBean;
    private BaseConfigMBean   m_mbean;
    public ConfigMBeanHelper(BaseConfigMBean mbean, ConfigBean cb)
    {
        m_baseConfigBean = cb;
        m_mbean = mbean;
//        if(!descriptor.isConfigBeanRetrospected())
//        {
//        }
    }

/*    public static ConfigBeanHelper getConfigBeanHelper(BaseConfigMBean mbean)
    {
        return new ConfigBeanHelper((MBeanDescriptor)mbean.getMBeanInfo(), mbean.getManagedConfigBean());
    }
*/    
    

        
//********************************************************************************************************************
    public static Object converConfigBeansToObjectNames(MBeanRegistry registry, ModelMBeanInfo parentInfo, Object ret) throws Exception
    {
        if(ret!=null)
        {
            if(ret instanceof ConfigBean)
                return (Object)getChildObjectName(registry, parentInfo, (ConfigBean)ret);
            if(ret instanceof ConfigBean[])
                return (Object)getChildObjectNames(registry, parentInfo, (ConfigBean[])ret);
        }
        return ret;
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

    //********************************************************************************************************************
    public static AttributeList getDefaultAttributeValues(MBeanNamingDescriptor descr, String attrNames[]) throws Exception
    {
        if(attrNames==null || attrNames.length<1)
            return null;
        AttributeList attrs = new AttributeList();
        Class cl = getConfigBeanClass(descr.getXPathPattern());
        if(cl==null)
            return null;
        Method method = cl.getDeclaredMethod("getDefaultAttributeValue", new Class[]{Class.forName("java.lang.String")});
        for(int i=0; i<attrNames.length; i++)
        {
            try {
                Object value = (String)method.invoke(null, new Object[]{MBeanMetaHelper.mapToConfigBeanAttributeName(attrNames[i])});
                if(value!=null)
                    attrs.add(new Attribute(attrNames[i], value));
            } catch(Exception e) {};
        }
        return attrs;
    }
    
    //********************************************************************************************************************
    public static ObjectName getOwnObjectName(MBeanRegistry registry, ModelMBeanInfo parentInfo) throws Exception
    {
        Descriptor descr = parentInfo.getMBeanDescriptor();
        String type = (String)descr.getFieldValue(NMTYPE_FIELD_NAME);
        String[] location = (String[])descr.getFieldValue(NMLOCATION_FIELD_NAME);
        MBeanRegistryEntry entry = registry.findMBeanRegistryEntryByType(type);
        MBeanNamingDescriptor namingDescr = entry.getNamingDescriptor();
        return namingDescr.createObjectName(location);
    }
    //********************************************************************************************************************
    public static ObjectName[] getChildObjectNames(MBeanRegistry registry, ModelMBeanInfo parentInfo, ConfigBean[] children) throws Exception
    {
        Descriptor descr = parentInfo.getMBeanDescriptor();
        return getConfigBeansObjectNames(registry, (String)descr.getFieldValue(DOMAIN_FIELD_NAME), children);
    }
    //********************************************************************************************************************
    public static ObjectName getChildObjectName(MBeanRegistry registry, ModelMBeanInfo parentInfo, ConfigBean childBean) throws Exception
    {
        Descriptor descr = parentInfo.getMBeanDescriptor();
        return getConfigBeanObjectName(registry, (String)descr.getFieldValue(DOMAIN_FIELD_NAME), childBean);
    }
    //********************************************************************************************************************
    public static ObjectName[] getConfigBeansObjectNames(MBeanRegistry registry, String domainName, ConfigBean[] beans) throws Exception
    {
        ObjectName[] objNames = new ObjectName[beans.length];
        for(int i=0; i<beans.length; i++)
            objNames[i] = getConfigBeanObjectName(registry, domainName, beans[i]);
        return objNames;
    }
    //********************************************************************************************************************
    public static ObjectName getConfigBeanObjectName(MBeanRegistry registry, String domainName, ConfigBean childBean) throws Exception
    {
        String xpath = childBean.getAbsoluteXPath("");
        MBeanRegistryEntry entry = registry.findMBeanRegistryEntryByXPath(xpath);
        MBeanNamingDescriptor namingDescr = entry.getNamingDescriptor();
        String[] location = namingDescr.extractParmListFromXPath(xpath);
        location[0] = domainName;
        return namingDescr.createObjectName((Object[])location);
    }
//********************************************************************************************************************
    public static String[] getChildNamesList(ConfigBean[] beans) throws Exception
    {
        String[] names = new String[beans.length];
        for(int i=0; i<beans.length; i++)
        {
            String xpath = beans[i].getAbsoluteXPath("");
            names[i] = MBeanMetaHelper.getMultipleElementKeyValue(xpath);
        }
        return names;
    }
//********************************************************************************************************************
    
    protected static void debug(String s) {
        //TODO: change this to app server logging
        System.out.println(s);
    }
    protected static void info(String s) {
        //TODO: change this to app server logging
        System.out.println(s);
    }
    protected static void error(String s) {
        //TODO: change this to app server logging
        System.out.println(s);
    }

    /****************************************************************************************************************
     * Gets MBean's child element.
     * @param childName the MBean's child element name.
     * @return The value of the property retrieved.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public ConfigBean getChildElementByName(String methodName, String name)  throws MBeanException,AttributeNotFoundException
    {

        Class cl = m_baseConfigBean.getClass();
        ConfigBean bean;
        try
        {
           Method method = cl.getDeclaredMethod(methodName, new Class[]{Class.forName("java.lang.String")});
           return (ConfigBean)method.invoke(m_baseConfigBean, new Object[]{name});
        }
        catch (Exception e)
        {
			String msg = /*localStrings.getString*/( "admin.server.core.mbean.config.getattribute.undefined_childelement_in_base_element"+ name );
            throw new MBeanException(new MBeanConfigException( msg ));
        }
    }

    /****************************************************************************************************************
     * Gets MBean's property value.
     * @param externalName the MBean's property name.
     * @return The value of the property retrieved.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public Object getPropertyElementValue(String propertyName)  throws MBeanException,AttributeNotFoundException
    {

        Class cl = m_baseConfigBean.getClass();
        ElementProperty prop;
        try
        {
           Method method = cl.getDeclaredMethod("getElementPropertyByName", new Class[]{Class.forName("java.lang.String")});
           prop = (ElementProperty)method.invoke(m_baseConfigBean, new Object[]{propertyName});
        }
        catch (Exception e)
        {
			String msg = /*localStrings.getString*/( "admin.server.core.mbean.config.getattribute.undefined_properties_in_base_element"+ propertyName );
            throw new MBeanException(new MBeanConfigException( msg ));
        }
        if(prop==null) {
			String msg = /*localStrings.getString*/( "admin.server.core.mbean.config.getattribute_properties_not_found_in_base_element"+ propertyName );
            throw new MBeanException(new MBeanConfigException( msg ));
		}
        return prop.getValue();
    }
    /****************************************************************************************************************
     * Sets MBean's property value.
     * @param attr The identification of the property to be set and the value it is to be set to.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public void setPropertyElementValue(Attribute attr, boolean bAllowsEmptyValue)  throws MBeanException,AttributeNotFoundException
    {
        String propertyName = attr.getName();
        String value = (String)attr.getValue();
        
        Class cl = m_baseConfigBean.getClass();
        ElementProperty prop;
        try
        {
           Method method = cl.getDeclaredMethod("getElementPropertyByName", new Class[]{Class.forName("java.lang.String")});
           prop = (ElementProperty)method.invoke(m_baseConfigBean, new Object[]{propertyName});
        }
        catch (Exception e)
        {
			String msg = /*localStrings.getString*/( "admin.server.core.mbean.config.setattribute_undefined_properties_in_base_element"+ propertyName );
            throw new MBeanException(new MBeanConfigException( msg ));
        }
        if(prop==null && value!=null && (bAllowsEmptyValue || !value.equals("")))
        {
            prop = new ElementProperty();
            prop.setName(propertyName);
            prop.setValue(value);
            try
            {
                Method method = cl.getDeclaredMethod("addElementProperty", new Class[]{prop.getClass()});
                method.invoke(m_baseConfigBean, new Object[]{prop});
            }
            catch (Exception e)
            {
				String msg = /*localStrings.getString*/( "admin.server.core.mbean.config.setproperty_invoke_error"+propertyName );
                throw new MBeanException(new MBeanConfigException( msg ));
            }
        }
        else
        {
            if(value==null || (!bAllowsEmptyValue && value.equals("")))
            {
                try
                {
                    Method method = cl.getDeclaredMethod("removeElementProperty", new Class[]{prop.getClass()});
                    method.invoke(m_baseConfigBean, new Object[]{prop});
                }
                catch (Exception e)
                {
					String msg = /*localStrings.getString*/( "admin.server.core.mbean.config.setproperty_could_not_remove_propery"+ propertyName );
                    throw new MBeanException(new MBeanConfigException( msg ));
                }
            }
            else
                prop.setValue(value);
        }
        
/*        try
        {
            m_configContext.flush();
        }
        catch (ConfigException e)
        {
            throw new MBeanException(new MBeanConfigException(e.getMessage()));
        }
*/
    }
    // THE FOLLOWING METHOD IS LUDO's CODE
    /**
     * Convert a DTD name into a bean name:
     *
     * Any - or _ character is removed. The letter following - and _
     * is changed to be upper-case.
     * If the user mixes upper-case and lower-case, the case is not
     * changed.
     * If the Word is entirely in upper-case, the word is changed to
     * lower-case (except the characters following - and _)
     * The first letter is always upper-case.
     */
    public static String convertTagName(String name)
    {
        CharacterIterator  ci;
        StringBuffer    n = new StringBuffer();
        boolean    up = true;
        boolean    keepCase = false;
        char    c;
        
        ci = new StringCharacterIterator(name);
        c = ci.first();
        
        // If everything is uppercase, we'll lowercase the name.
        while (c != CharacterIterator.DONE)
        {
            if (Character.isLowerCase(c))
            {
                keepCase = true;
                break;
            }
            c = ci.next();
        }
        
        c = ci.first();
        while (c != CharacterIterator.DONE)
        {
            if (c == '-' || c == '_')
                up = true;
            else
            {
                if (up)
                    c = Character.toUpperCase(c);
                else
                    if (!keepCase)
                        c = Character.toLowerCase(c);
                n.append(c);
                up = false;
            }
            c = ci.next();
        }
        return n.toString();
    } // end of convertTagName()
    
}
