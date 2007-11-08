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
 *   $Id: ConfigBeanHelper.java,v 1.5 2007/04/03 01:13:39 llc Exp $
 *   @author: alexkrav
 *
 *   $Log: ConfigBeanHelper.java,v $
 *   Revision 1.5  2007/04/03 01:13:39  llc
 *   Issue number:  2752
 *   Obtained from:
 *   Submitted by:  Lloyd Chambers
 *   Reviewed by:   3 day timeout expired
 *
 *   Revision 1.4  2006/03/12 01:26:56  jluehe
 *   Renamed AS's org.apache.commons.* to com.sun.org.apache.commons.*, to avoid collisions with org.apache.commons.* packages bundled by webapps.
 *
 *   Tests run: QL, Servlet TCK
 *
 *   Revision 1.3  2005/12/25 03:47:29  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.2  2005/06/27 21:19:40  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.11  2005/05/07 04:35:26  ruyakr
 *   Engineer: Rob Ruyak
 *
 *   Fixed warning messages displayed from jdk1.5 because of introduction of varargs in certain apis such as ja
 *   va.lang.Class.
 *
 *
 *   This is my last checkin here at Sun! Thanks for all the good times!
 *
 *   Revision 1.10  2004/11/14 07:04:17  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.9  2004/11/10 21:17:01  kmeduri
 *   This is needed for fixing 6177312.
 *   Add the following method
 *   static public boolean checkIfAttributesAndPropertiesAreResolvable(ConfigBean element, String instanceName)
 *   This method tests if all attributes and properties of the tested config element are fully resolvable.
 *
 *   Author: Alexandre Kravtchenko
 *   Reviewer: Sreenivas Munnangi
 *   Tests Ran: PE/EE Quicklook tests
 *   Approved by: Bugswat
 *
 *   Revision 1.8  2004/02/20 03:56:07  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *   Revision 1.7.4.2  2004/02/02 07:25:14  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.7.4.1  2003/12/23 01:51:43  kravtch
 *   Bug #4959186
 *   Reviewer: Sridatta
 *   Checked in PE8FCS_BRANCH
 *   (1) admin/admin-core/admin-cli/admin-gui/appserv-core/assembly-tool: switch to new domain name "ias:" -> "com.sun.appserv"
 *   (2) admin-core and admin-cli: switch to "dashed" attribute names
 *   (3) admin-core: support both "dashed"/"underscored" names for get/setAttributes
 *   (4) admin-gui: hook for reverse converting attribute names (temporary hack);
 *
 *   Revision 1.7  2003/06/25 20:03:37  kravtch
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

//naming
import com.sun.enterprise.admin.meta.naming.MBeanNamingInfo;

//config
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.PropertyResolver;
/****************************************************************************************************************
 */
public class ConfigBeanHelper
{
   private static final String XPATH_SEPARATOR = "/";
   public final String PROPERTY_NAME_PREFIX = "property.";    
    
    private ConfigBean        m_baseConfigBean;
    private BaseConfigMBean   m_mbean;
    public ConfigBeanHelper(BaseConfigMBean mbean, ConfigBean cb)
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
    
    /****************************************************************************************************************/
    public String getAttributeValue(String name) throws MBeanException,AttributeNotFoundException
    {
//FIXME change to CoinfigMBeanBase implementation
        if(m_baseConfigBean==null)
            return null;
        if(name.startsWith(PROPERTY_NAME_PREFIX))
        {
            // get property value
            return (String)getPropertyElementValue(name.substring(PROPERTY_NAME_PREFIX.length()));
        }
        else
            return m_baseConfigBean.getAttributeValue(name);
    }
    public String setAttributeValue(String name, String value)  throws MBeanException,AttributeNotFoundException
    {
//FIXME change to CoinfigMBeanBase implementation
        if(m_baseConfigBean==null)
            return null;
        if(name.startsWith(PROPERTY_NAME_PREFIX))
        {
            // set property value
            setPropertyElementValue(new Attribute(name.substring(PROPERTY_NAME_PREFIX.length()), value), false);
        }
        else
           m_baseConfigBean.setAttributeValue(name, value);
        return value;
    }
    
    
    
    /** 
     * call app server logging
     */
    protected static boolean isDebugEnabled() {
        return true;
    }
    
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

    static private ElementProperty[] getElementProperties(ConfigBean element) 
    {

        Class cl = element.getClass();
        ElementProperty[] props = null;
        try
        {
           Method method = cl.getDeclaredMethod("getElementProperty");
           props = (ElementProperty[])method.invoke(element);
        }
        catch (Exception e)
        {
        }
        return props;
    }

    /**
     * this method tests if all attributes and properties of the tested config element are fully resolvable
     * (not consist of unresolvable ${...} - varibles inside
     * @param element The testing config element bean
     * @param appserver instance name
     * @throws ConfigException
     **/
    static public boolean checkIfAttributesAndPropertiesAreResolvable(ConfigBean element, String instanceName) 
                              throws ConfigException
    {
        final PropertyResolver resolver = new PropertyResolver(element.getConfigContext(), instanceName);
        final String[] attrNames = element.getAttributeNames();
        //first - check attributes;
        if(attrNames!=null)
        {
            for(int i=0; i<attrNames.length; i++)
            {
               String value = element.getAttributeValue(attrNames[i]);
               if(value!=null && !resolver.isResolvable(value, true))
               {
                   return false;
               }
            }
        }
        //then - properties;
        ElementProperty[] props = getElementProperties(element);
        if(props!=null)
        {
            for(int i=0; i<props.length; i++)
            {
               String value = props[i].getValue();
               if(value!=null && !resolver.isResolvable(value, true))
               {
                   return false;
               }
            }
        }
           
        return true;
    }
}
