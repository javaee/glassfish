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

package com.sun.enterprise.admin.server.core.mbean.config;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Hashtable;
import java.lang.reflect.Method;
import java.util.Iterator;      
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

//JMX imports
import javax.management.*;

//Config imports
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.config.ConfigException;
//import com.sun.enterprise.config.ConfigNode;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.serverbeans.ElementProperty;

//Admin imports
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.server.core.mbean.config.naming.ConfigMBeanNamingInfo;

import com.sun.enterprise.admin.server.core.mbean.meta.MBeanEasyConfig;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
//import com.sun.enterprise.admin.server.core.mbean.config.naming.ConfigMBeanNamingInfo;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;


/****************************************************************************************************************
 * This base admin Dynamic MBean class provides access to ConfigNode's elements attributes according to attribute
 * descriptions provided by child class.
 */
public class ConfigMBeanBase extends AdminBase //implements DynamicMBean
{
    public static final Logger sLogger = Logger.getLogger(AdminConstants.kLoggerName);
    private final static String MSG_BASE_GET_ATTRIBUTE      = "mbean.config.base_get_attribute";
    private final static String MSG_BASE_SET_ATTRIBUTE      = "mbean.config.base_set_attribute";
    private final static String MSG_BASE_GOT_ATTRIBUTE      = "mbean.config.base_got_attribute";
    private final static String MSG_BASE_GET_PROPERTY       = "mbean.config.base_get_property";
    private final static String MSG_BASE_SET_PROPERTY       = "mbean.config.base_set_property";
    private final static String MSG_BASE_GET_DEF_ATTR_VALUE = "mbean.config.get_def_attr_value";
    private final static String MSG_GET_CONFBEANBYXPATH     = "mbean.config.get_confbeanbyxpath";
    private final static String MSG_LOG_CONF_CTX            = "mbean.config.log_config_id";
    public static final char ATTRIBUTE_CHAR =  '@'; //attribute name prefix
    public static final char ALLOWS_EMPTY_CHAR =  '@'; //"allows empty value" prefix symbol (can be only after ATTRIBUTE_CHAR)
    public static final String ATTRIBUTE =  ""+ATTRIBUTE_CHAR; //attribute name prefix
    public static final String E_ATTRIBUTE =  "" + ATTRIBUTE_CHAR + ALLOWS_EMPTY_CHAR; //"allows empty value" attribute name prefix
    public static final String PROPERTY =  ServerTags.ELEMENT_PROPERTY+ ServerXPathHelper.XPATH_SEPARATOR+ATTRIBUTE; //property name prefix
    public static final String E_PROPERTY =  ServerTags.ELEMENT_PROPERTY+ ServerXPathHelper.XPATH_SEPARATOR+E_ATTRIBUTE; //property name prefix
    public static final String ELEMENT_CONTENT   =  "";  //element content prefix
    public static final String PSEUDO_ATTR_DESCRIPTION = ServerTags.DESCRIPTION;

    private AdminContext        m_AdminContext;
    private ConfigContext       m_configContext;
    private String              m_BasePath; //target root-node
    private Hashtable           m_Attrs;
    private MBeanInfo           m_MBeanInfo;
    private String              m_ServerInstanceName;
    private ConfigMBeanNamingInfo   m_MBeanNamingInfo;
    
	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( ConfigMBeanBase.class );

  
    /****************************************************************************************************************
     * Binds MBean with Config Bean 
     * @param instanceName instance name.
     *  @throws MbeanConfigException exception if the param parsing is not successful
     */
    public void initialize(ConfigMBeanNamingInfo namingInfo) throws MBeanConfigException
    {
        String instanceName = namingInfo.getServerInstanceName();
        ConfigContext configContext;
        if (m_AdminContext != null) {
            configContext = m_AdminContext.getAdminConfigContext();
        } else {
            try
            {
                InstanceEnvironment instanceEnvironment = new InstanceEnvironment(instanceName);
                //String fileUrl  = instanceEnvironment.getConfigFilePath();
			    /*Everything should be set in the backup file*/
			    String fileUrl  = instanceEnvironment.getConfigFilePath();
                configContext   = ConfigFactory.createConfigContext(fileUrl);
            }
            catch(ConfigException e)
            {
                //e.printStackTrace();
    			String msg = localStrings.getString( "admin.server.core.mbean.config.error_locating_server_node", e.getMessage() );
                throw new MBeanConfigException( msg );
            }
        }
        
        sLogger.log(Level.FINEST, MSG_LOG_CONF_CTX, new Object[] {
                this.getClass().getName(), new Long(configContext.hashCode())});
        m_ServerInstanceName = instanceName;
        m_configContext = configContext;
        m_MBeanNamingInfo = namingInfo;
        m_BasePath = namingInfo.getXPath();
    }
    /*
     Binds MBean with Config Bean 
     **/
    public void initialize(String mbeanType, String[] locations) throws MBeanConfigException
    {
        ConfigMBeanNamingInfo namingInfo;
        try
        {
            namingInfo = new ConfigMBeanNamingInfo(mbeanType, locations);
        }
        catch (MBeanConfigException mce)
        {
            throw mce;
        }
        catch (Exception e)
        {
            throw new MBeanConfigException(e.getMessage());
        }
        
        initialize(namingInfo);
    }

    /*
     Binds MBean with Config Bean 
     **/
    public void initialize(String dottedName) throws MBeanConfigException
    {
        ConfigMBeanNamingInfo namingInfo;
        try
        {
            namingInfo = new ConfigMBeanNamingInfo(dottedName);
        }
        catch (MBeanConfigException mce)
        {
            throw mce;
        }
        catch (Exception e)
        {
            throw new MBeanConfigException(e.getMessage());
        }
        
        initialize(namingInfo);
    }

    /*
     Binds MBean with Config Bean 
     **/
    public void initialize(ObjectName objectName) throws MBeanConfigException
    {
        ConfigMBeanNamingInfo namingInfo;
        try
        {
            namingInfo = new ConfigMBeanNamingInfo(objectName);
        }
        catch (MBeanConfigException mce)
        {
            throw mce;
        }
        catch (Exception e)
        {
            throw new MBeanConfigException(e.getMessage());
        }
        
        initialize(namingInfo);
    }

    
    
    /****************************************************************************************************************
     * Sets Config MBean description data (this method usually is calling from the child class constructor)
     * @param attrsMapList array of attribute mapping descriptions, each subarray represents one attribute by string pairs:
     * <br> <b>{ &ltexternal name>, &ltpath to value> }</b> for each attribute, where
     * <ul> <b>&ltexternal name></b> defines name identifying value in <code>getAttribute()/SetAttribute()</code> methods;
     * <br> <b>&ltpath to value></b> is relative path to value from the base node in XPath notation;
     * </ul> Example (attribute value):
     * <br>   ORBListener port attribute in orblistener node can be described as
     * <ul>     <b>{ "port", "@port" }</b>
     * </ul>
     * If entire description for attribute is missing, then <code>getAttribute()/SetAttribute()</code> treats this situation the same way as
     * it was described as <b>{"@&ltattrName>]","&ltattrName>"}</b>.
     * </ul> Examples (element value):
     * <ul>     <b>{ "about", "description[@type='thisInstance')]" }</b> //this example for value of element with tag 'description' ans attribute 'type' equales to 'myInstance'
     * <br>     <b>{ "about", "description" }</b> //this example for value of <b>unique</b> element with tag 'description'
     * </ul>
     * @param attrDescriptions String array of MBean attribute descriptions in MBeanEasyConfig format.
     * @param operDescriptions String array of MBean operations descriptions in MBeanEasyConfig format.
     *  @throws MBeanConfigException exception if the param parsing is not successful
     */
    public void setDescriptions(String attrsMapList[][], String[] attrDescriptions, String[] operDescriptions) throws MBeanConfigException
    {
        m_Attrs = createAttrsDescriptors(attrsMapList);
        MBeanEasyConfig easyConfig = new MBeanEasyConfig(getClass(), attrDescriptions, operDescriptions, null);
        m_MBeanInfo = easyConfig.getMBeanInfo();
    }
    
    public void setAdminContext(AdminContext adminContext) {
        m_AdminContext = adminContext;
    }

    public AdminContext getAdminContext() {
        return m_AdminContext;
    }

    /****************************************************************************************************************
     */
    public ConfigContext getConfigContext()
    {
        return m_configContext;
    }
    
    /****************************************************************************************************************
     */
    public String getServerInstanceName()
    {
        return m_ServerInstanceName;
    }
    
    /****************************************************************************************************************
     */
    public ConfigMBeanNamingInfo getConfigMBeanNamingInfo()
    {
        return m_MBeanNamingInfo;
    }
    
    /****************************************************************************************************************
   Get path to base node; all attributes pathes are defined relatively this node.
   @return XPath style path to the ConfigNode object of node which is used as base for locationg of the attributes.
     */
    public String getBasePath()
    {
        return m_BasePath;
    }
    
    /****************************************************************************************************************
   Get base node Config Bean;
   @return ConfigBean related to the ConfigNode object of node which is used as base for locationg of the attributes.
     */
    public ConfigBean getBaseConfigBean()
    {
        try
        {
           return getConfigBeanByXPath(m_BasePath);
        }
        catch (Exception e)
        {
           return null;
        }
    }
    
    /****************************************************************************************************************
      Get <code>MBeanInfo</code> MBean description object (required dynamic MBean method).
      @return <code>MBeanInfo</code> description object for configurable MBean;
     */
    public MBeanInfo getMBeanInfo()
    {
        return m_MBeanInfo;
    }
    /****************************************************************************************************************
      Get <code>ConfigBean</code>  object which contains correspondent attribute.
      @param externalName external name of the attribute
      @return <code>ConfigBean</code>  object which contains correspondent attribute
     */
    public ConfigBean getConfigBean(String externalName)
    {
        //this method can be overriten in child class (temporary until Config static method getBeanByXPath will be ready)
        AttrDescriptor descr = getDescriptor(externalName);
        try
        {
            return descr.getConfigBean();
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    /****************************************************************************************************************
      Get <code>ConfigBean</code>  object which binded to ConfigNode with given XPath.
      @param xPath XPath to ConfigNode
      @return <code>ConfigBean</code>  object which linked to ConfigNode with given XPath.
     */
    public ConfigBean getConfigBeanByXPath(String xPath) throws ConfigException
    {
        sLogger.log(Level.FINEST, MSG_GET_CONFBEANBYXPATH, xPath);
        return ConfigBeansFactory.getConfigBeanByXPath(m_configContext, xPath);
    }
    
    /****************************************************************************************************************
     * Gets MBean's attribute value.
     * @param externalName the MBean's attribute name.
     * @return The value of the attribute retrieved.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public Object getAttribute(String externalName)  throws MBeanException,AttributeNotFoundException
    {
        sLogger.log(Level.FINEST, MSG_BASE_GET_ATTRIBUTE, externalName);
        MBeanAttributeInfo ai = getAttrInfo(externalName);
        boolean isProperty = externalName.startsWith(ConfigAttributeName.PROPERTY_NAME_PREFIX);


        if(ai==null && !isProperty)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.attribute_not_defined", externalName );
			throw new AttributeNotFoundException( msg );
        }
        if(ai!=null && !ai.isReadable())
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.attribute_not_readable", externalName );
			throw new AttributeNotFoundException( msg );
        }

        
        
        try
        {
        if(isProperty)
        {
            // get property value
            return getPropertyElementValue(externalName.substring(ConfigAttributeName.PROPERTY_NAME_PREFIX.length()));
        }
        else
        {
            AttrDescriptor descr = getDescriptor(externalName);
            ConfigBean  bean = getConfigBean(externalName);
            String value = null;
            if(descr.isElement())
            {
                //it looks that now we have no Elements with values
                //value = descr.getNode().getContent();
				String msg = localStrings.getString( "admin.server.core.mbean.config.getattribute_not_implemented_for_xml" );
                throw new MBeanException(new MBeanConfigException( msg ));
            }
            else
            {
                value = bean.getRawAttributeValue(descr.getAttributeName());

                //value = descr.getNode().getRawAttributeValue(descr.getAttributeName());
            }
            sLogger.log(Level.FINEST, MSG_BASE_GOT_ATTRIBUTE, new Object[]{externalName,value, MBeanEasyConfig.convertStringValueToProperType(value, ai.getType())});
            return MBeanEasyConfig.convertStringValueToProperType(value, ai.getType());
        }
        }
        catch (MBeanConfigException e)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.getattribute_attribute_exception", externalName, e.getMessage() );
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
        sLogger.log(Level.FINEST, MSG_BASE_GET_PROPERTY, propertyName);

        ConfigBean baseBean = getBaseConfigBean();
        Class cl = baseBean.getClass();
        ElementProperty prop;
        try
        {
           Method method = cl.getDeclaredMethod("getElementPropertyByName", new Class[]{Class.forName("java.lang.String")});
           prop = (ElementProperty)method.invoke(baseBean, new Object[]{propertyName});
        }
        catch (Exception e)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.getattribute.undefined_properties_in_base_element", propertyName );
            throw new MBeanException(new MBeanConfigException( msg ));
        }
        if(prop==null) {
			String msg = localStrings.getString( "admin.server.core.mbean.config.getattribute_properties_not_found_in_base_element", propertyName );
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
        sLogger.log(Level.FINEST, MSG_BASE_SET_PROPERTY, new Object[]{propertyName, value});
        
        ConfigBean baseBean = getBaseConfigBean();

        Class cl = baseBean.getClass();
        ElementProperty prop;
        try
        {
           Method method = cl.getDeclaredMethod("getElementPropertyByName", new Class[]{Class.forName("java.lang.String")});
           prop = (ElementProperty)method.invoke(baseBean, new Object[]{propertyName});
        }
        catch (Exception e)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.setattribute_undefined_properties_in_base_element", propertyName );
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
                method.invoke(baseBean, new Object[]{prop});
            }
            catch (Exception e)
            {
				String msg = localStrings.getString( "admin.server.core.mbean.config.setproperty_invoke_error", propertyName );
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
                    method.invoke(baseBean, new Object[]{prop});
                }
                catch (Exception e)
                {
					String msg = localStrings.getString( "admin.server.core.mbean.config.setproperty_could_not_remove_propery", propertyName );
                    throw new MBeanException(new MBeanConfigException( msg ));
                }
            }
            else
                prop.setValue(value);
        }
        
        try
        {
            m_configContext.flush();
        }
        catch (ConfigException e)
        {
            throw new MBeanException(new MBeanConfigException(e.getMessage()));
        }
    }
    

    /****************************************************************************************************************
     * Gets MBean's attribute default value or null (if def value is not defined).
     * @param externalName the MBean's attribute name.
     * @return The default value of the attribute retrieved null, if def value is not defined.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public Object getDefaultAttributeValue(String externalName)  throws AttributeNotFoundException,MBeanConfigException
    {
        sLogger.log(Level.FINEST, MSG_BASE_GET_DEF_ATTR_VALUE, externalName);
        MBeanAttributeInfo ai = getAttrInfo(externalName);
        if(ai==null) {
			String msg = localStrings.getString( "admin.server.core.mbean.config.getdefaultattribute_undefined_attribute", externalName );
            throw new AttributeNotFoundException( msg );
		}
//        try
        {
            AttrDescriptor descr = getDescriptor(externalName);
            String value = descr.getDefaultValue();
            return MBeanEasyConfig.convertStringValueToProperType(value, ai.getType());
        }
/*        catch (ConfigException e)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.getdefaultattributevalue_exception_for_externalname_basexpath", externalName, m_BasePath, e.getMessage() );
            throw new MBeanException(new ConfigException( msg ));
        } 
*/
    }

    /****************************************************************************************************************
     * Sets MBean's attribute value.
     * @param attr The identification of the attribute to be set and the value it is to be set to.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public void setAttribute(Attribute attr)  throws MBeanException,AttributeNotFoundException
    {
        String externalName = attr.getName();
        Object value = attr.getValue();
        sLogger.log(Level.FINEST, MSG_BASE_SET_ATTRIBUTE, new Object[]{externalName, value});
        MBeanAttributeInfo ai = getAttrInfo(externalName);
        boolean isProperty = externalName.startsWith(ConfigAttributeName.PROPERTY_NAME_PREFIX);
        if(ai==null && !isProperty)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.setattribute_undefined_attribute", externalName );
            throw new AttributeNotFoundException( msg );
        }
        if(ai!=null && !ai.isWritable())
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.setattribute_attribute_not_writable", externalName );
            throw new MBeanException(new MBeanConfigException( msg ));
        }
        
        try
        {
            if(isProperty)
            {
                boolean bAllowsEmptyValue = true;
                if(ai!=null)
                {
                    AttrDescriptor descr = getDescriptor(externalName);
                    if(descr!=null)
                    {
                       bAllowsEmptyValue = descr.isEmptyValueAllowed();
                    }
                }
                // set property value
                setPropertyElementValue(new Attribute(externalName.substring(ConfigAttributeName.PROPERTY_NAME_PREFIX.length()), value), bAllowsEmptyValue);
                return;
            }
            else
            { //normal attribute
                // check type (now only for exception)
// left check for Verifyer now (bug #4725686) 
//                MBeanEasyConfig.convertStringValueToProperType(value.toString(), ai.getType());

                AttrDescriptor descr = getDescriptor(externalName);
                ConfigBean  bean = getConfigBean(externalName);
                if(descr.isElement())
                {
                    //it looks that now we have no Elements with values
                    //bean.set???Value(descr.getAttributeName(), value.toString());
					String msg = localStrings.getString( "admin.server.core.mbean.config.getattribute_not_implemented_for_xml" );
                    throw new MBeanException(new MBeanConfigException( msg ));

    //                descr.getNode().setContent(value.toString());
    //                m_configContext.flush();
                }
                else
                {

                    //descr.getNode().setAttribute(descr.getAttributeName(), value.toString());
                    if(value==null || (value.equals("") && !descr.isEmptyValueAllowed()) )
                       bean.setAttributeValue(descr.getAttributeName(), null);
                    else
                       bean.setAttributeValue(descr.getAttributeName(), value.toString());
                    m_configContext.flush();
                }
            }
            
        }
//        catch (MBeanConfigException mce)
//        {
//			String msg = localStrings.getString( "admin.server.core.mbean.config.setattribute_attribute_exception",  externalName, mce.getMessage() );
//            throw new MBeanException(new MBeanConfigException( msg ));
//        }
        catch (ConfigException e)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.setAttribute_exception_for_externalname_basexpath", externalName, m_BasePath, e.getMessage() );
            throw new MBeanException(new MBeanConfigException( msg ));
        }
    }
    
    /****************************************************************************************************************
     * Gets MBean's attribute values.
     * @param attributeNames A list of the attributes names to be retrieved.
     * @return The list of attributes retrieved.
     */
    public AttributeList getAttributes(String[] attributeNames)
    {
        AttributeList attrs = new AttributeList();
        for(int i=0; i<attributeNames.length; i++)
        {
//            try
            {
                if(attributeNames[i].length()==0)
                {
                    String[] names = getAllAttributeNames();
                    if(names!=null)
                        attrs.addAll(getAttributes(names));
                }
                else
                {
                    if(attributeNames[i].equals(ConfigAttributeName.PROPERTY_NAME_PREFIX))
                    {
                        String[] names = getAllPropertyNames(true);
                        if(names!=null)
                            attrs.addAll(getAttributes(names));
                    }
                    else
                    {
                        try
                        {
                            Object value  = getAttribute(attributeNames[i]);
                            attrs.add(new Attribute(attributeNames[i], value));
                        }
                        catch (MBeanException ce)
                        {
                            attrs.add(new Attribute(attributeNames[i], null));
                        }
                        catch (AttributeNotFoundException ce)
                        {
                            attrs.add(new Attribute(attributeNames[i], null));
                        }
                        catch (NullPointerException npe) //ConfigBean returns this exception by many reasons
                        {
                            attrs.add(new Attribute(attributeNames[i], null));
                        }
                    }
                }
            }
//            catch (Throwable t)
//            {
//            }
        }
        return attrs;
    }
    
    /****************************************************************************************************************
     * Sets the values of several MBean's attributes.
     * @param attrList A list of attributes: The identification of the attributes to be set and the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     */
    public AttributeList setAttributes(AttributeList attrList)
    {
        AttributeList attrs = new AttributeList();
        Iterator it = attrList.iterator();
        while (it.hasNext())
        {
            try
            {
                Attribute attribute = (Attribute) it.next();
                setAttribute(attribute);
                attrs.add(attribute);
            }
            catch (MBeanException mbe)
            {
            }
            catch (AttributeNotFoundException anfe)
            {
            }
            catch (NullPointerException npe)
            {
            }
        }
        return attrs;
    }
    /**
    Every resource MBean should override this method to execute specific
    operations on the MBean.
     */
    public Object invoke(String methodName, Object[] methodParams,
    String[] methodSignature) throws MBeanException, ReflectionException
    {
        //return null;
        /*
         * New for 8.0
        */
        return ( super.invoke(methodName, methodParams, methodSignature) );
    }
    
    //****************************************************************************************************************
    private Hashtable createAttrsDescriptors(String[][] attrs) throws MBeanConfigException
    {
        Hashtable ht = new Hashtable();
        if (attrs != null)
        {
            for(int i=0; i<attrs.length; i++)
            {
                ht.put(attrs[i][0], 
                       new AttrDescriptor(attrs[i][1]));
            }
        }
        return ht;
    }
    
    //****************************************************************************************************************
    private AttrDescriptor getDescriptor(String externalName)
    {
        return (AttrDescriptor)m_Attrs.get(externalName);
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
    public static String convertName(String name)
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
    }
    
    //****************************************************************************************************
    private MBeanAttributeInfo getAttrInfo(String attrName)
    {
        if(attrName==null || m_MBeanInfo==null)
            return null;
        
        MBeanAttributeInfo[] ai = m_MBeanInfo.getAttributes();
        if(ai!=null)
            for(int i=0; i<ai.length; i++)
            {
                String name = ai[i].getName();
                if(attrName.equals(ai[i].getName()))
                    return ai[i];
            }
        return null;
    }
    
    //****************************************************************************************************
    private String[] getAllAttributeNames()
    {
        if(m_Attrs==null )
            return null;
        Enumeration keys = m_Attrs.keys();   
        ArrayList list = new ArrayList();
        while(keys.hasMoreElements())
        {
            list.add(keys.nextElement());
        }
        return (String[])list.toArray(new String[list.size()]);
    }

    //****************************************************************************************************
    private String[] getAllPropertyNames(boolean bAddPropertyPrefix)
    {

        ConfigBean baseBean = getBaseConfigBean();
        Class cl = baseBean.getClass();
        ElementProperty[] props;
        try
        {
           Method method = cl.getDeclaredMethod("getElementProperty", (Class[])null);
           props = (ElementProperty[])method.invoke(baseBean, (Object[])null);
           String[] names = new String[props.length];
           for(int i=0; i<props.length; i++)
           {
               if(bAddPropertyPrefix)
                   names[i] = ConfigAttributeName.PROPERTY_NAME_PREFIX+props[i].getName();
               else
                   names[i] = props[i].getName();
           }
           return names;
        }
        catch (java.lang.NoSuchMethodException nsme)
        {
            return null;
        }
        catch (java.lang.IllegalAccessException iae)
        {
            return null;
        }
        catch (java.lang.reflect.InvocationTargetException ite)
        {
            return null;
        }
    }

    //****************************************************************************************************
    /**
     * This method allows to append another bean attributes (usually child ones) to base attributes;
     * so for MBean user they all will be as base 
     */
    static Object[] MergeAttributesWithAnotherMbean(
             String[][] maplist1, String[] attributes1,
             String[][] maplist2, String[] attributes2,
             String relativeXPath2, String attrNamesPrefix2)
    {
        int size = 0;
        if(maplist1!=null)
            size += maplist1.length;
        if(maplist2!=null)
            size += maplist2.length;
        String[][] new_maplist  = new String[size][];
        String[] new_attributes = new String[size];
        String[] mapelem;
        int i = 0;
        if(maplist1!=null)
            for(i=0; i<maplist1.length; i++)
            {
                new_maplist[i] = (String[])maplist1[i].clone();
                new_attributes[i] = attributes1[i];
            }
        if(maplist2!=null)
            for(int j=0; j<maplist2.length; j++)
            {
                mapelem = (String[])maplist2[j].clone();
                new_attributes[i+j] = attributes2[j];
                if(attrNamesPrefix2!=null)
                {
                   mapelem[0] = attrNamesPrefix2 + mapelem[0];
                   new_attributes[i+j] = attrNamesPrefix2 + attributes2[j].trim();
                }
                mapelem[1] = relativeXPath2 + "/" + mapelem[1];
                new_maplist[i+j] = mapelem;
            }
        return new Object[]{new_maplist, new_attributes};
        
    }

    //****************************************************************************************************
    private static Class getConfigBeanClass(String xPath)
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

    /** Abstract method that subclasses have to implement. This is the way for
     * invoke method to work, through reflection.
     */
    protected Class getImplementingClass() {
        return ( this.getClass() );
    }
    
    /** Reflection requires the implementing object.  */
    protected Object getImplementingMBean() {
        return ( this );
    }    

     //****************************************************************************************************************
    private class AttrDescriptor
    {
        public String m_xPath = "";
        public String m_attributeName;
        public boolean m_bAllowsEmptyValue = false;
        
        //*******************************************************************************************************
        public AttrDescriptor(String description) throws MBeanConfigException
        {
            try
            {
                //first investigate: is it element value or attriibute value
                int lastSlashIdx = description.lastIndexOf(ServerXPathHelper.XPATH_SEPARATOR);
                if(description.charAt(lastSlashIdx+1)==ATTRIBUTE_CHAR) 
                { //element's attribute
                    if(description.charAt(lastSlashIdx+2)==ALLOWS_EMPTY_CHAR)
                    {
                        m_attributeName = description.substring(lastSlashIdx+3);
                        m_bAllowsEmptyValue = true;
                    }
                    else
                       m_attributeName = description.substring(lastSlashIdx+2); 
                    
                    if(lastSlashIdx>0)
                        m_xPath = description.substring(0,lastSlashIdx);
                }
                else
                { //element's content
                    m_attributeName = null; //not attribute
                }
            }
            catch (Throwable e)
            {
				String msg = localStrings.getString( "admin.server.core.mbean.config.attrdescriptor_constructor_exception", description, e.getMessage() );
                throw new MBeanConfigException( msg );
            }
        }
        
        //*******************************************************************************************************
        public String getAttributeName()
        {
            return m_attributeName;
        }
        
        //*******************************************************************************************************
        public String getXPath()
        {
            if(m_BasePath==null || m_BasePath.length()==0 || 
               m_xPath.indexOf(ServerXPathHelper.XPATH_SEPARATOR)==0)
                return m_xPath;
            if(m_xPath.length()==0)
               return m_BasePath;
            return m_BasePath + ServerXPathHelper.XPATH_SEPARATOR + m_xPath;
        }
        
        //*******************************************************************************************************
        public boolean isEmptyValueAllowed()
        {
            return m_bAllowsEmptyValue;
        }
        //*******************************************************************************************************
        public ConfigBean getConfigBean() throws Exception
        {
            return getConfigBeanByXPath(getXPath());
        }
        
        //****************************************************************************************************
        public String getDefaultValue()
        {
            Class cl = getConfigBeanClass(getXPath());
            //4. initiate class and bind with the node
            try
            {
                Method method = cl.getMethod("getDefaultAttributeValue", new Class[] {Class.forName("java.lang.String")} );
                return (String)method.invoke(null, new Object[] {m_attributeName});
            }
            catch(Exception e)
            {
                return null;
            }
        }
        //*******************************************************************************************************
/*        public ConfigNode getNode() throws ConfigException
        {
            return m_configContext.exactLookup(getXPath());
        }
*/
        //*******************************************************************************************************
        public boolean isElement()
        {
            return (m_attributeName==null);
        }
    }
    
}
