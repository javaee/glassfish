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
 *   $Id: ManagedConfigBean.java,v 1.12 2006/05/25 17:33:48 kravtch Exp $
 *   @author: alexkrav
 *
 *   $Log: ManagedConfigBean.java,v $
 *   Revision 1.12  2006/05/25 17:33:48  kravtch
 *   Bug #6426390 (The result of Config MBeans setAttributes() depends of order of attributes in the list)
 *   admin-core:
 *      - ManagedConfigBean.setAttributes reorders attributes, deferring calls to set attributes regected by Validator because of validation attributes dependency (wrong order).
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules: admin-core/admin;
 *   Tests passed: QLT/EE, devtest
 *
 *   Revision 1.11  2006/05/08 17:18:52  kravtch
 *   Bug #6423082 (request for admin infrastructure to support the config changes without DAS running (offline))
 *   Added infrastructure for offline execution under Config Validator for:
 *      - dottednames set/get operation
 *      - Add/remove jvm-options
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules: admin-core/admin; admin/validator;
 *
 *   Revision 1.10  2006/03/18 00:43:22  kravtch
 *   Issue #394 (Provide an MBean attribute to get JMXServiceURL of the System JMX Connector ...)
 *   admin-core/admin:
 *       repare for getAttributes():
 *       - BaseConfigMBean.getAttributes() now calls BaseConfigMBean.getAttribute() instead of ManagedConfigBean.getAttributes()
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules admin/mbeans
 *   Tests passed: QLT/EE
 *
 *   Revision 1.9  2006/03/03 23:33:26  kravtch
 *   Bug #6313936 (additional)
 *   admin-core/admin:
 *     - fix typos in ManagedConfigBean
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules admin-core/admin
 *   Tests passed: QLT/EE
 *
 *   Revision 1.8  2006/03/01 23:50:15  kravtch
 *   Bug #6313936(asadmin set allows setting LazyConnectionAssociation=true and LazyConnectionEnlistment=false)
 *   admin/validator:
 *     - new ReportHelper & PropertyHelper classes created to support properties validation;
 *     - AttrType: remove attr report methods (to move them into ReportHelper)
 *     - DomainMgr: instead of plain AFRuntimeException now throws AdminValidationException;
 *     - GenericValidator: new validatePropertyChanges() method calls during validateAsParent();
 *     - LocalStrings: some messages revised;
 *     - new GenericValidator.getTargetElementPrintName() provides names for validation msgs;
 *     - ValidationContext: unused member ownerBean is removed; getTargetBean() is modified;
 *     - tests.JdbcConnectionPoolTest - checks for LazyConnection props are added;
 *   admin-core/admin:
 *     - new AdminValidationException class is added;
 *     - ManagedConfigBean.wrapAndThrowMBeanException() helper method is added
 *     - AdminValidationException are passed without catch;
 *   Submitted by: kravtch
 *   Reviewed by: Parshanth Abbagani
 *   Affected modules admin/validator; admin-core/admin
 *   Tests passed: QLT/EE
 *
 *   Revision 1.7  2006/02/25 00:05:34  kravtch
 *   Bug #6378808(Unable to set/add a blank password property on JDBC Pool through Web UI)
 *   admin-core/admin/.../config:
 *     - new ConfigMBeanHelper.PROPERTY_SPECIAL_EMPTY_VALUE introduced;
 *     - ManagedConfigBean now checks setting property value; if it equals to SPECIAL_EMPTY_VALUE it will be set to "";
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules admin-core/admin
 *   Tests passed: QLT/EE
 *
 *   Revision 1.6  2005/12/25 03:47:31  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.5  2005/11/14 22:53:15  kravtch
 *   Bug #6338666 (Adding customRealm in adminGUI in the server-config throws BadRealmException in server.log)
 *      - ManagedConfigMBean.createChildByType() now sets Properties as well;
 *      - BaseConfigMBean: convenience methods getManagedConfigBean() & getConfigBeanObjectName() are added;
 *      - ConfigsMBean.createAuthRealm(): instead of call to MBeanServer - use ManagedConfigBean.createChildByType()
 *
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules admin/mbeans; admin-core/util; tools
 *   Tests passed: QLT/EE + devtests
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
 *   Revision 1.3  2005/08/16 22:19:30  kravtch
 *   M3: 1. ConfigMBeans: Support for generic getXXXNamesList() operation (request from management-rules).
 *       2. MBeanRegistry: support for getElementPrintName() to provide readable element's description for validator's messages
 *   Submitted by: kravtch
 *   Reviewed by: Shreedhar
 *   Affected modules admin-core/admin
 *   Tests passed: QLT/EE + devtests
 *
 *   Revision 1.2  2005/06/27 21:19:41  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.29  2005/05/07 04:35:26  ruyakr
 *   Engineer: Rob Ruyak
 *
 *   Fixed warning messages displayed from jdk1.5 because of introduction of varargs in certain apis such as ja
 *   va.lang.Class.
 *
 *
 *   This is my last checkin here at Sun! Thanks for all the good times!
 *
 *   Revision 1.28  2004/11/14 07:04:17  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.27  2004/09/12 18:39:20  ramakant
 *   Bug# 6172060
 *
 *   ASAPI => AMX name changes.
 *
 *   1) QL EE
 *   26 pass, 2 fail, 1 did not run
 *
 *   Failed: ConverterApp. Cause: EJB Timer Service not available.
 *
 *   Didnot run: ejb-cmp-roster
 *
 *   2) Deployment dev tests - DAS, INSTANCE, CLUSTER
 *
 *   All pass except for known failures.
 *
 *   Revision 1.26  2004/05/22 00:35:05  kravtch
 *   "system-properties" backend support is added
 *   Reviewer: Sridatta
 *   Tests passed: QLT/CTS PE
 *
 *   Revision 1.25  2004/04/20 03:10:45  kravtch
 *   Reviewer:Sridatta
 *   cofigbean.setAttributeValue() - cast value to String is replaced by call toString(). Non-string value provided by AMX MBeans - createHTTPListener().
 *   Test passed: QLT, JUnit
 *
 *   Revision 1.24  2004/02/20 03:56:08  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *   Revision 1.22.4.4  2004/02/02 07:25:14  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.22.4.3  2003/12/23 01:51:44  kravtch
 *   Bug #4959186
 *   Reviewer: Sridatta
 *   Checked in PE8FCS_BRANCH
 *   (1) admin/admin-core/admin-cli/admin-gui/appserv-core/assembly-tool: switch to new domain name "ias:" -> "com.sun.appserv"
 *   (2) admin-core and admin-cli: switch to "dashed" attribute names
 *   (3) admin-core: support both "dashed"/"underscored" names for get/setAttributes
 *   (4) admin-gui: hook for reverse converting attribute names (temporary hack);
 *
 *   Revision 1.22.4.2  2003/12/18 20:25:13  ramakant
 *   Bug# 4923404
 *   Reviewer: Alex
 *
 *   Revision 1.22.4.1  2003/12/01 21:52:38  kravtch
 *   Bug #4939964
 *   Reviewer: Sridatta
 *   admin.config.ManagedConfigBean.createChildByType() now analyzes registryEntries.AttributeInfo for each "empty"  valued attribute (similar to setAttribute(), but could not use MBeanAttributeInfo because MBean is not exists yet). If "emptyValueAllowed" field in registrEntry.AttributeInfo is not "true", then "empty" attribute will be ignored.
 *
 *   Revision 1.22  2003/10/11 00:00:04  kravtch
 *   Bug 4933034
 *   Reviewer: Abhijit
 *   New field in attribute descriptor marks attributes which allows empty values.
 *   admin-descrptors file modified for "http-listener" mbean, adding:
 *       <attribute name="server_name" >
 *           <descriptor>
 *              <field name="emptyValueAllowed" value="true" />
 *           </descriptor>
 *       </attribute>
 *   BaseConfigMBean class modified to analyse this flag and properly perform setAttribute().
 *
 *   Revision 1.21  2003/09/26 21:33:03  kravtch
 *   Bug #4926266
 *   Reviewer: Sridatta
 *      - new test cases added to Validator's ThreadPoolTest DELETE - isThreadPoolReferencedFromOrb and isThreadPoolReferencedFromResAdapter to avoid deleteion of the referenced element
 *      - correspondent localStrings are added;
 *      - ManagedConfigBean.deleteSelf() now throws Exception;
 *      - MBeanHelper.invokeOperationInBean() will not suppress runtime exceptions any more;
 *
 *   Revision 1.20  2003/09/16 23:19:11  kravtch
 *   Bug #4923642
 *   reviewer: Abhijit
 *   Special MBeanConfigInstanceNotFoundException will be thrown by getXXX()/getXXXbyYYY(name) operations, returning ObjectName in case if correspondent element in domain.xml does not exist.
 *   This exception will be handled specifically on GUI invoke() operation. So, this exception should not be used for other cases (thrown or extended).
 *
 *   Revision 1.19  2003/09/13 00:40:00  kravtch
 *   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *   Bug #4920070;
 *   Reviewer: Sridatta
 *   More informative Exceptions messeges formed for this and similar cases.
 *
 *   Revision 1.18  2003/09/11 21:45:57  kravtch
 *   Bug #4910961 and few others
 *   Reviewer: Sridatta
 *
 *   BaseConfigMBean now throws Exception if ConfIgBeans opeartions getXXXbyYYY() return "null"
 *   I had to wrap into try/catch  some calls from MBeans/backendDeployment which relied on old behaviour.
 *
 *   Revision 1.17  2003/09/10 22:57:52  kravtch
 *   Bug: #4919955
 *   Reviewer: Sridatta
 *   ManagedConfigBean: during removeChild() operation DottedNames manager was notified only if correspondent MBeans were registered. Notification added.
 *
 *   Revision 1.16  2003/09/10 00:24:22  kravtch
 *   Reviewer: Sridatta
 *   New operation in DomainMBean is returning list of default attribute values according to mbean type. This operation will return DTD defined default values if custom mbean does not implement its own static operation overriding(extending) standard ones.
 *
 *   Revision 1.15  2003/08/29 02:16:41  kravtch
 *   Bug #4910964 (and similar others)
 *   Reviewer: Sridatta
 *
 *   Exception handling and logging enchancements:
 *      - extraction target exception for MBeanException and TargetInvocationException:
 *      - switch to localStrings usage;
 *      - throwing exception for config MBeans if error in creation of ConfigBean;
 *      - exceptions for null-results in configbean operations,like getXXbyYYY() [changes commented because of crashing of quick test]
 *
 *   Revision 1.14  2003/08/20 05:57:24  kravtch
 *   Dotted name registration is added at the end of CreateChildByType():
 *      m_registry.notifyRegisterMBean(objectName, getConfigContext());
 *
 *   Revision 1.13  2003/08/18 21:48:48  kravtch
 *   new static getParentXPath(xpath) returns xpath of parent element correcly bypassing bracketed values with possible escaping inside of quoted values
 *
 *   Revision 1.12  2003/08/18 02:49:08  kravtch
 *   deleteSelf() temporary catches all exceptions
 *   and  prints info to log
 *
 *   Revision 1.11  2003/08/18 01:51:15  kravtch
 *   temporary restore deleteConfigElement()
 *
 *   Revision 1.10  2003/08/15 23:09:52  kravtch
 *   calls to notifyRegisterMBean/UnregisterMBean from posrRegister/postDeregister
 *   removeChild support is added;
 *   new test cases for dotted names testing
 *
 *   Revision 1.9  2003/08/14 23:16:17  kravtch
 *   invokeOperation() signature changed;
 *   BaseConfigMBean now uses mcb.invokeOperation();
 *
 *   Revision 1.8  2003/08/12 21:01:32  kravtch
 *   added support for get/setAttribute for String[] attributes through invoke getValues/setValue in ConfigBean
 *
 *   Revision 1.7  2003/08/07 00:41:04  kravtch
 *   - new DTD related changes;
 *   - properties support added;
 *   - getDefaultAttributeValue() implemented for config MBeans;
 *   - merge Jsr77 and config activity in runtime mbeans;
 *
 *   Revision 1.6  2003/07/18 20:14:43  kravtch
 *   1. ALL config mbeans are now covered by descriptors.xml
 *   2. new infrastructure for runtime mbeans is added
 *   3. generic constructors added to jsr77Mdl beans (String[])
 *   4. new test cases are added to admintest
 *   5. MBeanRegistryFactory has now different methods to obtain admin/runtime registries
 *   6. runtime-descriptors xml-file is added to build
 *
 *   Revision 1.5  2003/06/25 20:03:38  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/

package com.sun.enterprise.admin.config;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Enumeration;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import com.sun.enterprise.util.i18n.StringManager;

//JMX imports
import javax.management.*;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.Descriptor;
import javax.management.modelmbean.ModelMBeanInfo;

//Config imports
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.SystemProperty;

//naming
//import com.sun.enterprise.admin.meta.MBeanDescriptor;
import com.sun.enterprise.admin.meta.naming.MBeanNamingDescriptor;
import com.sun.enterprise.admin.meta.naming.MBeanNamingInfo;
import com.sun.enterprise.admin.meta.MBeanMetaConstants;
import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.meta.MBeanMetaHelper;
import com.sun.enterprise.admin.meta.MBeanRegistryEntry;
import com.sun.enterprise.admin.meta.MBeanMetaException;

import com.sun.enterprise.admin.MBeanHelper;
import com.sun.enterprise.admin.BaseAdminMBean;
import com.sun.enterprise.admin.AdminValidationException;


/****************************************************************************************************************
 */
public class ManagedConfigBean {
    public final String PROPERTY_NAME_PREFIX = "property.";    //FIXME: should be exrternal
    public final String SYSTEM_PROPERTY_NAME_PREFIX = "system-property.";    //FIXME: should be exrternal

    public  static final Logger _sLogger = LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    private final static String MSG_BASE_GET_ATTRIBUTE      = "mbean.config.base_get_attribute";
    private final static String MSG_BASE_SET_ATTRIBUTE      = "mbean.config.base_set_attribute";
    private final static String MSG_BASE_GOT_ATTRIBUTE      = "mbean.config.base_got_attribute";
    private final static String MSG_BASE_GET_PROPERTY       = "mbean.config.base_get_property";
    private final static String MSG_BASE_GET_PROPERTIES     = "mbean.config.base_get_properties";
    private final static String MSG_BASE_SET_PROPERTY       = "mbean.config.base_set_property";
    private final static String MSG_BASE_GET_DEF_ATTR_VALUE = "mbean.config.get_def_attr_value";
    private final static String MSG_GET_CONFBEANBYXPATH     = "mbean.config.get_confbeanbyxpath";
    private final static String MSG_BASE_GET_CUSTOM_PROPERTIESLIST       = "mbean.config.base_get_custom_property";
    
    private ConfigBean          m_baseConfigBean;
    private MBeanInfo           m_mbeanInfo;
    private MBeanRegistry       m_registry;
    private ConfigContext       m_configContext; //we need it for keeping trace to deleted element context
    
    // i18n StringManager
    private static StringManager localStrings =    StringManager.getManager( BaseAdminMBean.class );
    
        
    public ManagedConfigBean(DynamicMBean mbean, ConfigBean cb, MBeanRegistry registry)
    {
        this(mbean.getMBeanInfo(), cb, registry);
    }
    
    public ManagedConfigBean(MBeanInfo mbeanInfo, ConfigBean cb, MBeanRegistry registry)
    {
        m_baseConfigBean = cb;
        m_mbeanInfo = mbeanInfo;
        m_configContext = cb.getConfigContext();
        m_registry = registry;
    }

    public ManagedConfigBean(ConfigBean cb, 
            MBeanRegistry registry, String domainName) throws Exception
    {
        this(cb, null, registry, domainName);
    }
    
    public ManagedConfigBean(ConfigBean cb, String xpath, 
            MBeanRegistry registry, String domainName) throws Exception
    {
        if(xpath==null)
            xpath = cb.getAbsoluteXPath("");
        MBeanRegistryEntry entry  = registry.findMBeanRegistryEntryByXPath(xpath);
        MBeanNamingDescriptor descr = entry.getNamingDescriptor();
        String[] parms = descr.extractParmListFromXPath(xpath);
        MBeanNamingInfo namingInfo = new  MBeanNamingInfo(descr, descr.getType(), parms);
        
        m_mbeanInfo = (MBeanInfo)entry.createMBeanInfo(namingInfo, domainName);
        m_baseConfigBean = cb;
        m_configContext = cb.getConfigContext();
        m_registry = registry;
    }
    /****************************************************************************************************************
    public static ManagedConfigBean getManagedConfigBean(cb
    /****************************************************************************************************************
     */
    public ConfigContext getConfigContext() {
        return m_configContext;
    }
    /****************************************************************************************************************
     */
    public MBeanNamingInfo getConfigMBeanNamingInfo() {
        return null; //FIXME return m_MBeanNamingInfo;
    }
    
    
    /****************************************************************************************************************
     * Get base node Config Bean;
     * @return ConfigBean related to the ConfigNode object of node which is used as base for locationg of the attributes.
     */
    public ConfigBean getBaseConfigBean() {
        return m_baseConfigBean;
    }
    
    
    /****************************************************************************************************************
      Get <code>ConfigBean</code>  object which contains correspondent attribute.
      @param externalName external name of the attribute
      @return <code>ConfigBean</code>  object which contains correspondent attribute
     */
    public ConfigBean getConfigBean(String externalName)
    {
        //in 8.0 we support only base config bean attributes and properties access
        return m_baseConfigBean;
/*7.0        //this method can be overriten in child class (temporary until Config static method getBeanByXPath will be ready)
        AttrDescriptor descr = getDescriptor(externalName);
        try
        {
            return descr.getConfigBean();
        }
        catch (Exception e)
        {
            return null;
        }
*/
    }
    
    
    /****************************************************************************************************************
     * Gets MBean's attribute value.
     * @param externalName the MBean's attribute name.
     * @return The value of the attribute retrieved.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public Object getAttribute(ModelMBeanAttributeInfo attrInfo, String externalName)  throws MBeanException,AttributeNotFoundException {
        _sLogger.log(Level.FINEST, MSG_BASE_GET_ATTRIBUTE, externalName);
        
        MBeanAttributeInfo ai = getAttrInfo(externalName);
        boolean isProperty = externalName.startsWith(PROPERTY_NAME_PREFIX);
        boolean isSystemProperty = externalName.startsWith(SYSTEM_PROPERTY_NAME_PREFIX);
        boolean isArray = false;
        
        if(externalName.equals("modelerType"))
            return null;
        if(ai==null && !isProperty && !isSystemProperty) {
            String msg = localStrings.getString( "admin.server.core.mbean.config.attribute_not_defined", externalName );
            throw new AttributeNotFoundException( msg );
        }
        if(ai!=null && !ai.isReadable()) {
            String msg = localStrings.getString( "admin.server.core.mbean.config.attribute_not_readable", externalName );
            throw new AttributeNotFoundException( msg );
        }
        
        
        
//        try {
        if(isProperty)
        {
            // get property value
            return getPropertyValue(externalName.substring(PROPERTY_NAME_PREFIX.length()));
        }
        else if(isSystemProperty)
        {
            // get system property value
            return getSystemPropertyValue(externalName.substring(SYSTEM_PROPERTY_NAME_PREFIX.length()));
        }
        else
        {
            AttrDescriptor descr = getDescriptor(externalName);
            ConfigBean  bean = getConfigBean(externalName);
            Object value = null;
            if(descr.isElement())
            {
                //it looks that now we have no Elements with values
                //value = descr.getNode().getContent();
                wrapAndThrowMBeanException(null, "getattribute_not_implemented_for_xml", externalName);
            }
            else
            {
                if(ai.getType().startsWith("[")) //array
                {
                    Class cl = bean.getClass();
                    try
                    {
                        Method method = cl.getMethod("getValues", new Class[]{Class.forName("java.lang.String")});
                        value =  method.invoke(bean, new Object[]{ConfigBean.camelize(descr.getAttributeName())});
                    }
                    catch (Exception e)
                    {
                        String msg = localStrings.getString( "admin.server.core.mbean.config.getattribute_invocation_error", externalName );
                        throw MBeanHelper.extractAndWrapTargetException(e, msg);
                    }
                }
                else
                {
                    value = bean.getAttributeValue(descr.getAttributeName());
                }
                
                
                
                //value = descr.getNode().getAttributeValue(descr.getAttributeName());
            }
            _sLogger.log(Level.FINEST, MSG_BASE_GOT_ATTRIBUTE, new Object[]
            {externalName,value});
            return value;
        }
 //       }
 //       catch (MBeanConfigException e) {
 //           String msg = localStrings.getString( "admin.server.core.mbean.config.getattribute_attribute_exception", externalName, e.getMessage() );
 //           throw new MBeanException(new MBeanConfigException( msg ));
 //       }
        
    }
    
    /****************************************************************************************************************
     */
    public MBeanAttributeInfo getAttributeInfo(String attrName) 
    {
        attrName = MBeanMetaHelper.mapToMBeanAttributeName(attrName);
        return (ModelMBeanAttributeInfo)
              MBeanHelper.findMatchingAttributeInfo(m_mbeanInfo, attrName);
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

        ConfigBean baseBean = getBaseConfigBean();
        Class cl = baseBean.getClass();
        ConfigBean bean;
        try
        {
           Method method = cl.getDeclaredMethod(methodName, new Class[]{Class.forName("java.lang.String")});
           return (ConfigBean)method.invoke(baseBean, new Object[]{name});
        }
        catch (Exception e)
        {
            wrapAndThrowMBeanException(e, "getattribute.undefined_childelement_in_base_element", name );
            return null; //just to avoid compiling err
        }
    }

    /****************************************************************************************************************
     */
    public AttributeList getProperties()  throws MBeanException,AttributeNotFoundException {
        _sLogger.log(Level.FINEST, MSG_BASE_GET_PROPERTIES);
        
        ConfigBean baseBean = getBaseConfigBean();
        Class cl = baseBean.getClass();
        ElementProperty[] props = null;
        try {
            Method method = cl.getDeclaredMethod("getElementProperty");
            props = (ElementProperty[])method.invoke(baseBean);
        }
        catch (Exception e) {
            wrapAndThrowMBeanException(e, "getattribute.undefined_properties_in_base_element" );
        }
        if(props==null) {
            return new AttributeList();
        }
        AttributeList list = new AttributeList();
        for(int i=0; i<props.length; i++)
        {
            list.add(new Attribute(props[i].getName(), props[i].getValue()));
        }
        return list;
    }

    /****************************************************************************************************************
     */
    public AttributeList getSystemProperties()  throws MBeanException,AttributeNotFoundException {
        _sLogger.log(Level.FINEST, MSG_BASE_GET_PROPERTIES);
        
        ConfigBean baseBean = getBaseConfigBean();
        Class cl = baseBean.getClass();
        SystemProperty[] props = null;
        try {
            Method method = cl.getDeclaredMethod("getSystemProperty");
            props = (SystemProperty[])method.invoke(baseBean);
        }
        catch (Exception e) {
            wrapAndThrowMBeanException(e, "getattribute.undefined_properties_in_base_element" );
        }
        if(props==null) {
            return new AttributeList();
        }
        AttributeList list = new AttributeList();
        for(int i=0; i<props.length; i++)
        {
            list.add(new Attribute(props[i].getName(), props[i].getValue()));
        }
        return list;
    }

    public Object getDefaulCustomProperties(String propertyName)  throws MBeanException,AttributeNotFoundException {
        _sLogger.log(Level.FINEST, MSG_BASE_GET_CUSTOM_PROPERTIESLIST, propertyName);
        return null; //can be overriden
    }

    /****************************************************************************************************************
     * Gets MBean's property value.
     * @param externalName the MBean's property name.
     * @return The value of the property retrieved.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public Object getPropertyValue(String propertyName)  throws MBeanException,AttributeNotFoundException {
        _sLogger.log(Level.FINEST, MSG_BASE_GET_PROPERTY, propertyName);
        
        ConfigBean baseBean = getBaseConfigBean();
        Class cl = baseBean.getClass();
        ElementProperty prop = null;
        try {
            Method method = cl.getDeclaredMethod("getElementPropertyByName", new Class[]{Class.forName("java.lang.String")});
            prop = (ElementProperty)method.invoke(baseBean, new Object[]{propertyName});
        }
        catch (Exception e) {
            wrapAndThrowMBeanException(e, "getattribute.undefined_properties_in_base_element");
        }
        if(prop==null) {
            wrapAndThrowMBeanException(null, "getattribute_properties_not_found_in_base_element", propertyName );
        }
        return prop.getValue();
    }    
    /****************************************************************************************************************
     * Gets MBean's system property value.
     * @param externalName the MBean's system property name.
     * @return The value of the system property retrieved.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public Object getSystemPropertyValue(String propertyName)  throws MBeanException,AttributeNotFoundException {
        _sLogger.log(Level.FINEST, MSG_BASE_GET_PROPERTY, propertyName);
        
        ConfigBean baseBean = getBaseConfigBean();
        Class cl = baseBean.getClass();
        SystemProperty prop = null;
        try {
            Method method = cl.getDeclaredMethod("getSystemPropertyByName", new Class[]{Class.forName("java.lang.String")});
            prop = (SystemProperty)method.invoke(baseBean, new Object[]{propertyName});
        }
        catch (Exception e) {
            wrapAndThrowMBeanException(e, "getattribute.undefined_properties_in_base_element");
        }
        if(prop==null) {
            wrapAndThrowMBeanException(null, "getattribute_properties_not_found_in_base_element", propertyName );
        }
        return prop.getValue();
    }    

    /****************************************************************************************************************
     * Sets MBean's property value.
     * @param attr The identification of the property to be set and the value it is to be set to.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public void setProperty(Attribute attr)  throws MBeanException,AttributeNotFoundException {
        setElementProperty(attr, false);
    }
    /****************************************************************************************************************
     * Sets MBean's system property value.
     * @param attr The identification of the system property to be set and the value it is to be set to.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public void setSystemProperty(Attribute attr)  throws MBeanException,AttributeNotFoundException {
        setSystemProperty(attr, false);
    }

    //****************************************************************************************************************
    private void setElementProperty(Attribute attr, boolean bAllowsEmptyValue)  throws MBeanException,AttributeNotFoundException {
        String propertyName = attr.getName();
        String value = (String)attr.getValue();
        _sLogger.log(Level.FINEST, MSG_BASE_SET_PROPERTY, new Object[]{propertyName, value});
        
        ConfigBean baseBean = getBaseConfigBean();
        
        Class cl = baseBean.getClass();
        ElementProperty prop = null;
        try {
            Method method = cl.getDeclaredMethod("getElementPropertyByName", new Class[]{Class.forName("java.lang.String")});
            prop = (ElementProperty)method.invoke(baseBean, new Object[]{propertyName});
        }
        catch (Exception e) {
            wrapAndThrowMBeanException(e, "setattribute_undefined_properties_in_base_element", propertyName );
        }
        if(prop==null && value!=null && (bAllowsEmptyValue || !value.equals(""))) {
            prop = new ElementProperty();
            prop.setName(propertyName);
            if(ConfigMBeanHelper.PROPERTY_SPECIAL_EMPTY_VALUE.equals(value))
               prop.setValue("");
            else
               prop.setValue(value);
            try {
                Method method = cl.getDeclaredMethod("addElementProperty", new Class[]{prop.getClass()});
                method.invoke(baseBean, new Object[]{prop});
            }
            catch (Exception e) {
                wrapAndThrowMBeanException(e, "setproperty_invoke_error", propertyName );
            }
        }
        else {
            if(value==null || (!bAllowsEmptyValue && value.equals(""))) {
                try {
                    Method method = cl.getDeclaredMethod("removeElementProperty", new Class[]{prop.getClass()});
                    method.invoke(baseBean, new Object[]{prop});
                }
                catch (Exception e) {
                    wrapAndThrowMBeanException(e, "setproperty_could_not_remove_propery", propertyName );
                }
            }
            else
            {
                if(ConfigMBeanHelper.PROPERTY_SPECIAL_EMPTY_VALUE.equals(value))
                   prop.setValue("");
                else
                   prop.setValue(value);
            }
        }
    }
        
    private void setSystemProperty(Attribute attr, boolean bAllowsEmptyValue)  throws MBeanException,AttributeNotFoundException {
        String propertyName = attr.getName();
        String value = (String)attr.getValue();
        _sLogger.log(Level.FINEST, MSG_BASE_SET_PROPERTY, new Object[]{propertyName, value});
        
        ConfigBean baseBean = getBaseConfigBean();
        
        Class cl = baseBean.getClass();
        SystemProperty prop=null;
        try {
            Method method = cl.getDeclaredMethod("getSystemPropertyByName", new Class[]{Class.forName("java.lang.String")});
            prop = (SystemProperty)method.invoke(baseBean, new Object[]{propertyName});
        }
        catch (Exception e) {
            wrapAndThrowMBeanException(e, "setattribute_undefined_properties_in_base_element", propertyName );
        }
        if(prop==null && value!=null && (bAllowsEmptyValue || !value.equals(""))) {
            prop = new SystemProperty();
            prop.setName(propertyName);
            if(ConfigMBeanHelper.PROPERTY_SPECIAL_EMPTY_VALUE.equals(value))
               prop.setValue("");
            else
               prop.setValue(value);
            try {
                Method method = cl.getDeclaredMethod("addSystemProperty", new Class[]{prop.getClass()});
                method.invoke(baseBean, new Object[]{prop});
            }
            catch (Exception e) {
                wrapAndThrowMBeanException(e, "setproperty_invoke_error", propertyName );
            }
        }
        else {
            if(value==null || (!bAllowsEmptyValue && value.equals(""))) {
                try {
                    Method method = cl.getDeclaredMethod("removeSystemProperty", new Class[]{prop.getClass()});
                    method.invoke(baseBean, new Object[]{prop});
                }
                catch (Exception e) {
                    wrapAndThrowMBeanException(e, "setproperty_could_not_remove_propery", propertyName );
                }
            }
            else
            {
                if(ConfigMBeanHelper.PROPERTY_SPECIAL_EMPTY_VALUE.equals(value))
                   prop.setValue("");
                else
                   prop.setValue(value);
            }
        }
/*
        try {
            m_configContext.flush();
        }
        catch (ConfigException e) {
            throw new MBeanException(new MBeanConfigException(e.getMessage()));
        }
*/
 }
    
    
    /****************************************************************************************************************
     * Gets MBean's attribute default value or null (if def value is not defined).
     * @param externalName the MBean's attribute name.
     * @return The default value of the attribute retrieved null, if def value is not defined.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public String getDefaultAttributeValue(String externalName)  throws Exception //AttributeNotFoundException,MBeanConfigException {
    {
        _sLogger.log(Level.FINEST, MSG_BASE_GET_DEF_ATTR_VALUE, externalName);
        MBeanAttributeInfo ai = getAttrInfo(externalName);
        if(ai==null) {
            String msg = localStrings.getString( "admin.server.core.mbean.config.getdefaultattribute_undefined_attribute", externalName );
            throw new AttributeNotFoundException( msg );
        }
        //        try
        {
            AttrDescriptor descr = getDescriptor(externalName);
            String value = descr.getDefaultValue();
            return value;
        }
/*        catch (ConfigException e)
        {
                        String msg = localStrings.getString( "admin.server.core.mbean.config.getdefaultattributevalue_exception_for_externalname_basexpath", externalName, m_BasePath, e.getMessage() );
            throw new MBeanException(new ConfigException( msg ));
        }
 */
    }
    
    //*******************************************************************************************************
    /**
     * @param ai MBeanAttributeInfo 
     * @return true is EmptyValue is allowed for this attribute
     */    
    private static boolean isEmptyValueAllowed(MBeanAttributeInfo ai) {
        if(ai!=null && (ai instanceof ModelMBeanAttributeInfo))
        {
            try {
                Descriptor descr = ((ModelMBeanAttributeInfo)ai).getDescriptor();
                String strAllowed = (String)descr.getFieldValue(MBeanMetaConstants.EMPTYVALUEALLOWED_FIELD_NAME);
                if(Boolean.valueOf(strAllowed).booleanValue())
                    return true;
            } catch(Exception e)
            { 
            }
        }
        return false;
    }
    /****************************************************************************************************************
     * Sets MBean's attribute value.
     * @param attr The identification of the attribute to be set and the value it is to be set to.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public void setAttribute(ModelMBeanAttributeInfo attrInfo, Attribute attr)  throws MBeanException,AttributeNotFoundException {
        String externalName = attr.getName();
        Object value = attr.getValue();
        _sLogger.log(Level.FINEST, MSG_BASE_SET_ATTRIBUTE, new Object[]{externalName, value});
        MBeanAttributeInfo ai = getAttrInfo(externalName);
        boolean isProperty = externalName.startsWith(PROPERTY_NAME_PREFIX);
        boolean isSystemProperty = externalName.startsWith(SYSTEM_PROPERTY_NAME_PREFIX);
        if(ai==null && !isProperty && !isSystemProperty) {
            String msg = localStrings.getString( "admin.server.core.mbean.config.setattribute_undefined_attribute", externalName );
            throw new AttributeNotFoundException( msg );
        }
        if(ai!=null && !ai.isWritable()) {
            wrapAndThrowMBeanException(null, "setattribute_attribute_not_writable", externalName );
        }
        
//        try {
        if(isProperty)
        {
            boolean bAllowsEmptyValue = isEmptyValueAllowed(ai);
            // set property value
            setElementProperty(new Attribute(externalName.substring(PROPERTY_NAME_PREFIX.length()), value), bAllowsEmptyValue);
            return;
        }
        else if(isSystemProperty)
        {
            boolean bAllowsEmptyValue = isEmptyValueAllowed(ai);
            // set system property value
            setSystemProperty(new Attribute(externalName.substring(SYSTEM_PROPERTY_NAME_PREFIX.length()), value), bAllowsEmptyValue);
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
                wrapAndThrowMBeanException(null, "not_supported_attribute_type", externalName );
                
                //                descr.getNode().setContent(value.toString());
                //                m_configContext.flush();
            }
            else
            {
                if(ai.getType().startsWith("[")) //array
                {
                    bean.setValue(ConfigBean.camelize(descr.getAttributeName()), (Object[])value);
/*                    Class cl = bean.getClass();
                    try
                    {
                        Method method = cl.getMethod("setValue", new Class[]{Class.forName("java.lang.String"),(new Object[0]).getClass()});
                        method.invoke(bean, new Object[]{ConfigBean.camelize(descr.getAttributeName()),value});
                    }
                    catch (Exception e)
                    {
                        String msg = localStrings.getString( "admin.server.core.mbean.config.setattribute_invocation_error", externalName );
                        throw MBeanHelper.extractAndWrapTargetException(e, msg);
                    }
 */
                }
                else
                {
                    //descr.getNode().setAttribute(descr.getAttributeName(), value.toString());
                    if(value==null || (value.equals("") && !isEmptyValueAllowed(ai)) )
                        bean.setAttributeValue(descr.getAttributeName(), null);
                    else
                        bean.setAttributeValue(descr.getAttributeName(), value.toString());
                    //                    m_configContext.flush();
                }
            }
        }
        
//        }
        //        catch (MBeanConfigException mce)
        //        {
        //			String msg = localStrings.getString( "admin.server.core.mbean.config.setattribute_attribute_exception",  externalName, mce.getMessage() );
        //            throw new MBeanException(new MBeanConfigException( msg ));
        //        }
 //       catch (ConfigException e) {
 //           String msg = localStrings.getString( "admin.server.core.mbean.config.setAttribute_exception_for_externalname_basexpath", externalName, /*m_BasePath,*/ e.getMessage() );
 //           throw new MBeanException(new MBeanConfigException( msg ));
 //       }
    }
    
    
     //
    private void addArrayToList(ArrayList list, Object[] objects)
    {
        if(objects!=null)
            for(int i=0; i<objects.length; i++) {
                list.add(objects[i]);
            }
    }
    
    /****************************************************************************************************************
     * Gets MBean's attribute values.
     * @param attributeNames A list of the attributes names to be retrieved.
     * @return The list of attributes retrieved.
     */
    protected ArrayList getSimpleAttributeNames(String[] attributeNames) {
        ArrayList simpleNames = new ArrayList();
        for(int i=0; i<attributeNames.length; i++) {
                if(attributeNames[i].length()==0) {
                    addArrayToList(simpleNames, getAllAttributeNames());
                }
                else if(attributeNames[i].equals(PROPERTY_NAME_PREFIX)) {
                    addArrayToList(simpleNames, getAllPropertyNames(true));
                }
                else if(attributeNames[i].equals(SYSTEM_PROPERTY_NAME_PREFIX)) {
                    addArrayToList(simpleNames, getAllSystemPropertyNames(true));
                }
                else {
                    simpleNames.add(attributeNames[i]); 
                }
                
        }
        return simpleNames;
    }

    /****************************************************************************************************************
     * Gets MBean's attribute values.
     * @param attributeNames A list of the attributes names to be retrieved.
     * @return The list of attributes retrieved.
     */
    public AttributeList getAttributes(String[] attributeNames) {
        ArrayList names = getSimpleAttributeNames(attributeNames);
        AttributeList attrs = new AttributeList();
        for(int i=0; i<names.size(); i++) {
            try {
                String name = MBeanMetaHelper.mapToMBeanAttributeName((String)names.get(i));
                Object value  = getAttribute(null, name); //FIXME
                attrs.add(new Attribute(name, value));
            } catch (MBeanException ce) {
//                 attrs.add(new Attribute(attributeNames[i], null));
            } catch (AttributeNotFoundException ce) {
//                 attrs.add(new Attribute(attributeNames[i], null));
            } catch (NullPointerException npe) //ConfigBean returns this exception by many reasons
            {
//                 attrs.add(new Attribute(attributeNames[i], null));
            } catch (IllegalArgumentException iae) //ConfigBean returns this exception by many reasons
            {
//                 attrs.add(new Attribute(attributeNames[i], null));
            }
        }
        return attrs;
    }
    
    /****************************************************************************************************************
     * Sets the values of several MBean's attributes.
     * @param attrList A list of attributes: The identification of the attributes to be set and the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     */
    public AttributeList setAttributes(AttributeList attrList) {
        AttributeList attrs = new AttributeList();
        AttributeList deferredAttrs = new AttributeList();
        Iterator it = attrList.iterator();
        while (it.hasNext()) {
            Attribute attribute = (Attribute) it.next();
            attribute = new Attribute(MBeanMetaHelper.mapToMBeanAttributeName(attribute.getName()), attribute.getValue());
            try {
                setAttribute(null, attribute); //FIXME: attrInfos?
                attrs.add(attribute);
            } catch (MBeanException mbe) {
            } //ignored according to JMX spec
            catch (AttributeNotFoundException anfe) {
            } //ignored according to JMX spec
            catch (NullPointerException npe) {
            } //ignored according to JMX spec (comes from s2b)
            catch (AdminValidationException ave) {
                if(it.hasNext()) //if last - reordering will not help
                {
                    //possibly attributes ordering problem
                    //let's try this attribute later
                    deferredAttrs.add(attribute);
                } else {
                    throw ave;
                }
            }
        }
        
        //now let's try deferred attributes
        while(deferredAttrs.size()>0) {
            AttributeList newDeferredAttrs = new AttributeList();
            it = deferredAttrs.iterator();
            while (it.hasNext()) {
                Attribute attribute = (Attribute) it.next();
                try {
                    setAttribute(null, attribute); //FIXME: attrInfos?
                    attrs.add(attribute);
                } catch (MBeanException mbe) {
                } //ignored according to JMX spec
                catch (AttributeNotFoundException anfe) {
                } //ignored according to JMX spec
                catch (AdminValidationException ave) {
                    if(it.hasNext()) //if last - reordering will not help
                    {
                        //possibly attributes ordering problem
                        //let's try this attribute later
                        newDeferredAttrs.add(attribute);
                    } else {
                        throw ave;
                    }
                }
            }
            deferredAttrs = newDeferredAttrs;
            newDeferredAttrs = null;
        }
        return attrs;
    }
    
    /**
     * Every resource MBean should override this method to execute specific
     * operations on the MBean.
     */
    public Object invoke(String methodName, Object[] methodParams,
    String[] methodSignature) throws MBeanException, ReflectionException {
        return null;
    }
    
    
    //****************************************************************************************************************
    private AttrDescriptor getDescriptor(String externalName) {
        try {
            return new AttrDescriptor(externalName); //FIXME
        } catch (MBeanConfigException mbce)
        {
            return null;
        }
//       return (AttrDescriptor)m_Attrs.get(externalName);
    }
    
    //****************************************************************************************************
    private MBeanAttributeInfo getAttrInfo(String attrName) {
        if(m_mbeanInfo==null )
            return null;
        MBeanAttributeInfo[] ai = m_mbeanInfo.getAttributes();
        if(ai!=null)
            for(int i=0; i<ai.length; i++) {
                String name = ai[i].getName();
                if(attrName.equals(ai[i].getName()))
                    return ai[i];
            }
        return null; //FIXME
    }
    
    //****************************************************************************************************
    private String[] getAllAttributeNames() {
        MBeanInfo mbi = m_mbeanInfo;
        if(mbi==null )
            return null;
        MBeanAttributeInfo[] ai = mbi.getAttributes();
        if(ai==null || ai.length==0)
            return null;
        ArrayList list = new ArrayList();
        if(ai!=null)
            for(int i=0; i<ai.length; i++) 
            {
                String name = ai[i].getName();
                if(!name.equals("modelerType")) //FIXME: temporary
                   list.add(name);
            }
        return (String[])list.toArray(new String[list.size()]);
    }
    
    //****************************************************************************************************
    private String[] getAllPropertyNames(boolean bAddPropertyPrefix) {
        
        ConfigBean baseBean = getBaseConfigBean();
        Class cl = baseBean.getClass();
        ElementProperty[] props;
        try {
            Method method = cl.getDeclaredMethod("getElementProperty");
            props = (ElementProperty[])method.invoke(baseBean);
            String[] names = new String[props.length];
            for(int i=0; i<props.length; i++) {
                if(bAddPropertyPrefix)
                    names[i] = PROPERTY_NAME_PREFIX+props[i].getName();
                else
                    names[i] = props[i].getName();
            }
            return names;
        }
        catch (java.lang.NoSuchMethodException nsme) {
            return null;
        }
        catch (java.lang.IllegalAccessException iae) {
            return null;
        }
        catch (java.lang.reflect.InvocationTargetException ite) {
            return null;
        }
    }
    
    //****************************************************************************************************
    private String[] getAllSystemPropertyNames(boolean bAddPropertyPrefix) {
        
        ConfigBean baseBean = getBaseConfigBean();
        Class cl = baseBean.getClass();
        SystemProperty[] props;
        try {
            Method method = cl.getDeclaredMethod("getSystemProperty");
            props = (SystemProperty[])method.invoke(baseBean);
            String[] names = new String[props.length];
            for(int i=0; i<props.length; i++) {
                if(bAddPropertyPrefix)
                    names[i] = SYSTEM_PROPERTY_NAME_PREFIX+props[i].getName();
                else
                    names[i] = props[i].getName();
            }
            return names;
        }
        catch (java.lang.NoSuchMethodException nsme) {
            return null;
        }
        catch (java.lang.IllegalAccessException iae) {
            return null;
        }
        catch (java.lang.reflect.InvocationTargetException ite) {
            return null;
        }
    }
    
/*
    //****************************************************************************************************
    public ObjectName[] getChildrenForType(String childName) throws Exception
    {
        if(m_baseConfigBean==null)
            return null;
        String xpath = m_baseConfigBean.getAbsoluteXPath(null) +
                        ServerXPathHelper.XPATH_SEPARATOR + childName;
        Class cl = ConfigMBeanHelper.getConfigBeanClass(xpath);
        ConfigBean bean = (ConfigBean)cl.newInstance();
        if(childValues!=null)
            for(int i=0; i<childValues.size(); i++)
            {
                Attribute attr = (Attribute)childValues.get(i); 
                bean.setAttributeValue(attr.getName(), (String)attr.getValue());
            }
        Class clBase = m_baseConfigBean.getClass();
        Method method = clBase.getDeclaredMethod("get"+convertTagName(childName), 
                                            new Class[]{cl});
        Object res = method.invoke(m_baseConfigBean, new Object[]{bean});
        return bean;
    }
*/    
    //****************************************************************************************************
    public ConfigBean createChildByType(String childName, AttributeList childValues, Properties props, boolean bOnlyOneChildPossible) throws Exception
    {
        //First. Create standalone bean of child type with Attributes and Properties set
        ConfigBean bean = createChildByType(childName, childValues, props);
        if(bean==null)
            return null;
        
        // Now, connect it to papa bean
        addChildBean(childName, bean, bOnlyOneChildPossible);
        return bean;
    }
    
    //****************************************************************************************************
    public ConfigBean createChildByType(String childName, AttributeList childValues, Properties props) throws Exception
    {
        if(m_baseConfigBean==null)
            return null;
        String xpath = MBeanHelper.getXPathPattern( (ModelMBeanInfo)m_mbeanInfo) + 
                           ServerXPathHelper.XPATH_SEPARATOR + childName;
        Class cl = ConfigMBeanHelper.getConfigBeanClass(xpath);
        ConfigBean bean = (ConfigBean)cl.newInstance();
        MBeanRegistryEntry regEntry = m_registry.findMBeanRegistryEntryByXPathPattern(xpath);
        if(childValues!=null)
            for(int i=0; i<childValues.size(); i++)
            {
                
                Attribute attr = (Attribute)childValues.get(i); 
                if( (attr.getValue()==null || attr.getValue().equals("")))
                {  
                    //empty value set bypass if not allowed in the registry
                    boolean bEmptyValueAllowed = false;
                    try {
                       // analyze registry_Entry emptyValueAllowed (if mentioned)
                       bEmptyValueAllowed = regEntry.isAttributeEmptyValueAllowed(attr.getName());
                    } catch (MBeanMetaException mme)
                    {
                        //field not found in registry
                    }
                    if(!bEmptyValueAllowed)
                        continue; //skip set of empty value
                }
                if(attr.getValue()==null)
                    bean.setAttributeValue(MBeanMetaHelper.mapToConfigBeanAttributeName(attr.getName()), (String)attr.getValue());
                else
                    bean.setAttributeValue(MBeanMetaHelper.mapToConfigBeanAttributeName(attr.getName()), ""+attr.getValue());
            }
        if(props!=null && props.size()>0)
        {
            final Enumeration e = props.propertyNames();
            ArrayList propsList = new ArrayList();
            while (e.hasMoreElements())
            {
                ElementProperty ep = new ElementProperty();
                String key = (String)e.nextElement();
                ep.setName(key);
                ep.setValue(props.getProperty(key));
                propsList.add(ep);
            }
            if(propsList.size()>0)
            {
               ElementProperty eps[] = (ElementProperty[])propsList.toArray(
                                 new ElementProperty[propsList.size()]);
               bean.setValue("ElementProperty", eps);
            }
        }
        return bean;
    }
    
    //****************************************************************************************************
    public void addChildBean(String childName, ConfigBean bean, boolean bOnlyOneChildPossible) throws Exception
    {
        Class clBase = m_baseConfigBean.getClass();
        Method method;
        if(bOnlyOneChildPossible)
            method = clBase.getDeclaredMethod("set"+ConfigMBeanHelper.convertTagName(childName), new Class[]{bean.getClass()});
        else
            method = clBase.getDeclaredMethod("add"+ConfigMBeanHelper.convertTagName(childName), new Class[]{bean.getClass()});
        method.invoke(m_baseConfigBean, new Object[]{bean});
    }

    //****************************************************************************************************
    void removeChild(String opName, MBeanOperationInfo opInfo, Object params[], String childTag) throws Exception
    {
        
        MBeanOperationInfo getInfo = new MBeanOperationInfo("get"+opName.substring("remove".length()),
                                                            "",
                                                            opInfo.getSignature(),
                                                            "java.lang.Object",
                                                            MBeanOperationInfo.INFO);
        Object ret = MBeanHelper.invokeOperationInBean(getInfo, this.getBaseConfigBean(), params);
        if(ret==null)
        {
            String parentName = "";
            parentName = this.getBaseConfigBean().getClass().getName();
            int last = parentName.lastIndexOf('.');
            if(last>=0 && last<(parentName.length()-1))
                parentName = parentName.substring(last+1);
            String elemName = "";
            if(params!=null && params.length==1 && params[0] instanceof String)
                elemName = "\""+(String)params[0]+"\"";
            wrapAndThrowMBeanException(null, "element_not_found", new Object[]{parentName, childTag, elemName});
        }
        if(ret==MBeanHelper.INVOKE_ERROR_SIGNAL_OBJECT)
        {
            String elemName = "";
            if(params.length==1 && params[0] instanceof String)
                elemName = (String)params[0];
            wrapAndThrowMBeanException(null, "remove_get_invocation", 
                    new Object[]{opName, elemName} );
        }
        ArrayList arr = new ArrayList();
        collectChildrenObjectNames((ConfigBean)ret, arr);
        //remove bean
        m_baseConfigBean.removeChild((ConfigBean)ret);
        //now is time for unregister
        MBeanServer server = null;
        try {
           server = (MBeanServer)(MBeanServerFactory.findMBeanServer(null)).get(0);
        } catch(Exception e) {}; //try-catch for AdminTest only
        
        for(int i=0; i<arr.size(); i++)
        {
            ObjectName objectName = (ObjectName)arr.get(i);
            try
            {
                if(server.isRegistered(objectName))
                    server.unregisterMBean(objectName);
                else
                    //just to unregister dotted name
                    m_registry.notifyUnregisterMBean(objectName, getConfigContext());
            }
            catch (Throwable t)
            {
                _sLogger.fine("!!!!!!!!!!!!!! Can not unregister MBean: "+objectName);
            }
        }
    }

   //****************************************************************************************************
   void collectChildrenObjectNames(ConfigBean bean, ArrayList arr)
   {
        try 
        {
            ConfigBean[] beans = bean.getAllChildBeans();
            for(int i=0; i<beans.length; i++)
            {
                collectChildrenObjectNames(beans[i], arr);
            }
        }
        catch(Exception e)
        {
        } 
        try 
        {
            ObjectName name = (ObjectName)ConfigMBeanHelper.converConfigBeansToObjectNames(m_registry, (ModelMBeanInfo)m_mbeanInfo, (Object)bean);
            if(name!=null)
               arr.add(name);
        }
        catch(Exception e)
        {
        } 
   }

    //****************************************************************************************************
    public void deleteSelf() throws Exception
    {
        if(m_baseConfigBean==null)
            return;
	    String xpath = null;
//try {
        xpath = m_baseConfigBean.getAbsoluteXPath(null);
        xpath = ServerXPathHelper.getParentXPath(xpath);
        ConfigContext ctx = getConfigContext(); //com.sun.enterprise.admin.config.BaseConfigMBean.getConfigContext(); //FIXME: this method should not be in MBEAN
        ConfigBean parent = ConfigBeansFactory.getConfigBeanByXPath(ctx, xpath);
        parent.removeChild(m_baseConfigBean);
//} catch(Exception e)
//{
   //FIXME:temporary solution
//   System.out.println("Exception during deleteSelf()  xpath="+xpath);
//}
    }
 
    //****************************************************************************************************************
    private class AttrDescriptor {
        public String m_attributeName;
        public boolean m_bAllowsEmptyValue = false;
        
        //*******************************************************************************************************
        public AttrDescriptor(String description) throws MBeanConfigException {
            try {
                m_attributeName = MBeanMetaHelper.mapToConfigBeanAttributeName(description);
                
/*FIXME                //first investigate: is it element value or attriibute value
                int lastSlashIdx = description.lastIndexOf(ServerXPathHelper.XPATH_SEPARATOR);
                if(description.charAt(lastSlashIdx+1)==ATTRIBUTE_CHAR) { //element's attribute
                    if(description.charAt(lastSlashIdx+2)==ALLOWS_EMPTY_CHAR) {
                        m_attributeName = description.substring(lastSlashIdx+3);
                        m_bAllowsEmptyValue = true;
                    }
                    else
                        m_attributeName = description.substring(lastSlashIdx+2);
                    
                    if(lastSlashIdx>0)
                        m_xPath = description.substring(0,lastSlashIdx);
                }
                else { //element's content
                    m_attributeName = null; //not attribute
                }
*/
 }
            catch (Throwable e) {
                String msg = localStrings.getString( "admin.server.core.mbean.config.attrdescriptor_constructor_exception", description, e.getMessage() );
                throw new MBeanConfigException( msg );
            }
        }
        
        //*******************************************************************************************************
        public String getAttributeName() {
            return m_attributeName;
        }
        
        //*******************************************************************************************************
        public boolean isEmptyValueAllowed() {
            return m_bAllowsEmptyValue;
        }

        //****************************************************************************************************
        public String getDefaultValue() throws Exception {
            if(m_baseConfigBean==null || isProperty() || isSystemProperty() || isElement())
                return null;
               //we have to do it through invoke because it's declared as static overriden in extended class
               Class clBase = m_baseConfigBean.getClass();
               Method method = clBase.getDeclaredMethod("getDefaultAttributeValue", new Class[]{Class.forName("java.lang.String")});
               return (String)method.invoke(m_baseConfigBean, new Object[]{m_attributeName});
        }
        //*******************************************************************************************************
        public boolean isProperty() {
            if( m_attributeName!=null)
                return m_attributeName.startsWith(PROPERTY_NAME_PREFIX);
            return false;
        }
        //*******************************************************************************************************
        public boolean isSystemProperty() {
            if( m_attributeName!=null)
                return m_attributeName.startsWith(SYSTEM_PROPERTY_NAME_PREFIX);
            return false;
        }
        //*******************************************************************************************************
        public boolean isElement() {
            return (m_attributeName==null);
        }
    }

    public Object invokeOperation(ModelMBeanOperationInfo opInfo, Object params[], String signature[]) throws Exception 
    {
        Descriptor descr = opInfo.getDescriptor();
        String opName = opInfo.getName();
        String child = (String)descr.getFieldValue(MBeanMetaConstants.CHILD_FIELD_NAME);
        String multi = (String)descr.getFieldValue(MBeanMetaConstants.MULTI_FIELD_NAME);
        boolean bOnlyOne = true;
        if(multi!=null && multi.length()>0 &&
           multi.charAt(0)=='t')
            bOnlyOne = false;
        if(child!=null && opName.startsWith("create"))
        {
           ConfigBean bean = createChildByType(child, (AttributeList)params[0], null, bOnlyOne);
           ObjectName objectName = ConfigMBeanHelper.getChildObjectName(m_registry, (ModelMBeanInfo)m_mbeanInfo, bean);
           m_registry.notifyRegisterMBean(objectName, getConfigContext());
           return objectName;
        }
        
        if(child!=null && opName.startsWith("remove"))
        {
            removeChild(opName, opInfo, params, child);
            return null;
        }


            
        //Generic Config invoker
        Object ret = MBeanHelper.invokeOperationInBean(opInfo, this, params);
        if(ret!=MBeanHelper.INVOKE_ERROR_SIGNAL_OBJECT)
        {
            return ret;
        }
        
        boolean bNamesListOp = false;
        String  modifiedOpName = opName;
        if(!bOnlyOne && child!=null && 
           opName.startsWith("get") &&
           opName.endsWith(MBeanMetaConstants.GET_LISTNAMES_OP_SUFFIX))
        {
            bNamesListOp = true;
            modifiedOpName = opName.substring(0,  opName.length()-
                      MBeanMetaConstants.GET_LISTNAMES_OP_SUFFIX.length());
        }

        ret = MBeanHelper.invokeOperationInBean(modifiedOpName, opInfo, 
                this.getBaseConfigBean(), params);
        
        if(ret!=MBeanHelper.INVOKE_ERROR_SIGNAL_OBJECT)
        {
          if(ret==null && "javax.management.ObjectName".equals(opInfo.getReturnType()))
            {
                String parentName = "";
                parentName = this.getBaseConfigBean().getClass().getName();
                int last = parentName.lastIndexOf('.');
                if(last>=0 && last<(parentName.length()-1))
                    parentName = parentName.substring(last+1);
                String elemName = "";
                if(params!=null && params.length==1 && params[0] instanceof String)
                    elemName = (String)params[0];
                String msg;
                if(child!=null)
                    msg = localStrings.getString( "admin.server.core.mbean.config.element_not_found", new Object[]{parentName, child, elemName});
                else
                    msg = localStrings.getString( "admin.server.core.mbean.config.oper_element_not_found", new Object[]{parentName, modifiedOpName, elemName});
                throw new MBeanConfigInstanceNotFoundException(msg);
            }

          if(ret!=null)
            {
                if(bNamesListOp && (ret instanceof ConfigBean[]))
                {
                    return (Object)ConfigMBeanHelper.getChildNamesList(
                            (ConfigBean[])ret);
                }
                else
                {
                    ret = ConfigMBeanHelper.converConfigBeansToObjectNames(m_registry, 
                            (ModelMBeanInfo)m_mbeanInfo, ret);
                    if(ret instanceof ConfigBean)
                        return (Object)ConfigMBeanHelper.getChildObjectName(m_registry, 
                                (ModelMBeanInfo)m_mbeanInfo, (ConfigBean)ret);
                    if(ret instanceof ConfigBean[])
                        return (Object)ConfigMBeanHelper.getChildObjectNames(m_registry, 
                                (ModelMBeanInfo)m_mbeanInfo, (ConfigBean[])ret);
                }
            }
         return ret;
       }
       return MBeanHelper.INVOKE_ERROR_SIGNAL_OBJECT;
    }

    private void uncatchValidationException(Exception e)
    {
        if(e instanceof AdminValidationException)
                throw (AdminValidationException)e;
        
        if(e instanceof InvocationTargetException)
        {
            Throwable t = ((InvocationTargetException)e).getTargetException();
            if(t instanceof AdminValidationException)
                throw (AdminValidationException)t;
        }
    }
    
    private void wrapAndThrowMBeanException(Exception e,
            String strSuffix) throws MBeanException
    {
        wrapAndThrowMBeanException(e, strSuffix, null);
    }
    private void wrapAndThrowMBeanException(Exception e,
            String strSuffix, Object val) throws MBeanException
    {
        if(e!=null)
            uncatchValidationException(e);
        String msg;
        if(val instanceof Object[])
           msg = localStrings.getString( "admin.server.core.mbean.config."+strSuffix, (Object[])val );
        else
           msg = localStrings.getString( "admin.server.core.mbean.config."+strSuffix, val );
        throw new MBeanException(new MBeanConfigException( msg ));
    }
    
}
 
