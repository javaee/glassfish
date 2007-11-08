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
 *   $Id: BaseConfigMBean.java,v 1.8 2006/03/19 04:09:35 km105526 Exp $
 *   @author: alexkrav
 *
 *   $Log: BaseConfigMBean.java,v $
 *   Revision 1.8  2006/03/19 04:09:35  km105526
 *   Issue number:  BUG 6400594
 *   Obtained from: Anissa
 *   Submitted by:  Kedar/Alex
 *   Reviewed by:   Alex/Kedar
 *
 *   Deployment is broken from GUI. It has to do with the attributes
 *   that were named with characters that can't be java identifier characters.
 *   This was a JMX 1.2 limitation.
 *
 *   PE QL was run successfully.
 *   GUI deployment of apps successful.
 *
 *   Watching Tinderbox ...
 *
 *   Revision 1.7  2006/03/18 00:43:21  kravtch
 *   Issue #394 (Provide an MBean attribute to get JMXServiceURL of the System JMX Connector ...)
 *   admin-core/admin:
 *       repare for getAttributes():
 *       - BaseConfigMBean.getAttributes() now calls BaseConfigMBean.getAttribute() instead of ManagedConfigBean.getAttributes()
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules admin/mbeans
 *   Tests passed: QLT/EE
 *
 *   Revision 1.6  2006/03/12 01:26:56  jluehe
 *   Renamed AS's org.apache.commons.* to com.sun.org.apache.commons.*, to avoid collisions with org.apache.commons.* packages bundled by webapps.
 *
 *   Tests run: QL, Servlet TCK
 *
 *   Revision 1.5  2005/12/25 03:47:29  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.4  2005/11/14 22:53:14  kravtch
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
 *   Revision 1.3  2005/08/10 23:36:26  kravtch
 *   M3: management-rules MBean support.
 *      - new custom ManagementRulesMBean is added
 *   (two methods -createManagementRule() and addActionToManagementRule())
 *      - operation getConfigNameForTarget() is added to ConfigsMBean
 *      - operation getBaseConfigBean is added to BaseConfigMBean
 *      - "management-rules" element changed in admin-mbean-desriptor
 *   Submitted by: kravtch
 *   Reviewed by: Prashanth
 *   Affected modules admin/mbeans; admin-core/admin
 *   Tests passed: QLT/EE + devtests
 *
 *   Revision 1.2  2005/06/27 21:19:40  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.22  2004/11/14 07:04:16  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.21  2004/06/04 05:55:02  kebbs
 *   Sridatta, Alex, & I have decided that some refactoring of the EE mbeans
 *   is necessary to resolve some issues we are having around MBean classes
 *   being created outside the context of the mbean server. Specifically, the
 *   EE mbeans have some non-JMX compliant constructors (which take a
 *   ConfigContext) and are being instantiated via this constructor in other
 *   EE mbeans.
 *
 *   We have decided that the appropriate solution is to convert the problematic
 *   mbean classes into non-mbeans and then delegate to them from the mbeans.
 *   A new package named configbeans will be introduced to hold these mbeans.
 *   These configbeans can be freely new'd.
 *
 *   One benefit of this approach is that the transition to the new ISAPI
 *   mbeans (and removal of the existing MBeans) will go much smoother, since
 *   the business logic in the configbeans (relying only on the schema2beans
 *   classes) is cleanly separated from the MBeans which expose it.
 *
 *   Reviewer: AK, SV
 *
 *   Revision 1.20  2004/04/05 16:44:03  kravtch
 *   admin/meta/AdminConfigEventListener: new configcontext listener's code
 *   This listener is for synchronization of ConfigBeans changes with both MBeans and dotted-name spaces.
 *   admin/meta/MBeanRegistry: added methods (adoptConfigBeanDelete/Add) implementing beans ajustment
 *   admin/config/BaseConfigMBean: calls from MBean's postRegister/unregister methods to dotted-name-manager is commented out.
 *
 *   Reviewer: Sridatta
 *   Tests Passed: QuickLook +  UnitTest
 *
 *   Revision 1.19  2004/03/17 17:45:35  kebbs
 *   1) Re-factored Server, Node Agent, Cluster, APIs for use by GUI.
 *   2) Cleaned up all the various stats interfaces and replaced them with
 *   RuntimeStatus for one consistent view
 *   3) Renamed interfaces from IxxxMBean to xxxMBean to be more compliant
 *   4) Moved node agent proxy and config mbeans into admin-ee/admin so they
 *   could leverage code ther
 *   5) Added inherit flag to properties
 *   6) Object names returned everywhere
 *   7) Can no longer reference DAS config
 *
 *   Revision 1.18  2004/02/20 03:56:07  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *
 *   Revision 1.17  2003/11/18 23:36:05  kebbs
 *   1)Added the mbeanapi package with interfaces for all of our "exposed"
 *   mbeans. This will be a way to view and cleanup our interfaces going
 *   forward as well as providing a way to expose them in the proxy.
 *
 *   2)Added ResourceHelper and ApplicationHelper classes as well as a way for the
 *   ServerTarget and ClusterTarget objects to be able to fetch resource and
 *   application references.
 *
 *   3)Refactored out ApplicationsMBeanHelper and ResourcesMBeanHelper for use
 *   in the PE mbeans. These are also used in the EE implementation of
 *   create/delete/list/resource/application-references.
 *
 *   4)And finally, all this was done so that the resource commands (ResourcesMBean)
 *   now fully support cluster and server targets.
 *
 *   Revision 1.16.4.2  2004/02/02 07:25:13  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.16.4.1  2003/12/23 01:51:43  kravtch
 *   Bug #4959186
 *   Reviewer: Sridatta
 *   Checked in PE8FCS_BRANCH
 *   (1) admin/admin-core/admin-cli/admin-gui/appserv-core/assembly-tool: switch to new domain name "ias:" -> "com.sun.appserv"
 *   (2) admin-core and admin-cli: switch to "dashed" attribute names
 *   (3) admin-core: support both "dashed"/"underscored" names for get/setAttributes
 *   (4) admin-gui: hook for reverse converting attribute names (temporary hack);
 *
 *   Revision 1.16  2003/09/04 05:53:49  kravtch
 *   bugs #4896268 and #4913653
 *   Reviewer: Sridatta
 *      -AuthRealmMbean's getFielRealm is chaged from creating of the new FileRealm object to gettting it from security pool - Realm.getInstance(name) with casting result to FileRealm.
 *   This approach will work only for PE because DAS and instance have the same auth-realms.
 *      -AdminContext expanded by two new methods getAdminMBeanResourcUrl() and getRuntimeMBeanResourcUrl() which used by MBeanRegistryFactory for initialization admin and runtime registries. So, they are become pluggable.
 *      -AdminContext also notifies MBeanRegistryFactory during its construction. So, AdminContext become "visible" to admin-core/admin classes.
 *      -Hardcoded output changed to appropriate logger calls.
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
 *   Revision 1.14  2003/08/18 01:51:15  kravtch
 *   temporary restore deleteConfigElement()
 *
 *   Revision 1.13  2003/08/15 23:09:52  kravtch
 *   calls to notifyRegisterMBean/UnregisterMBean from posrRegister/postDeregister
 *   removeChild support is added;
 *   new test cases for dotted names testing
 *
 *   Revision 1.12  2003/08/14 23:16:16  kravtch
 *   invokeOperation() signature changed;
 *   BaseConfigMBean now uses mcb.invokeOperation();
 *
 *   Revision 1.11  2003/08/08 00:11:58  kravtch
 *   new convenience methods are added to BaseConfigMBean
 *      - getDomainName
 *      - getConfigContext
 *      - getChildObjectName
 *      - getMBeanServer
 *
 *   Revision 1.10  2003/08/07 00:41:04  kravtch
 *   - new DTD related changes;
 *   - properties support added;
 *   - getDefaultAttributeValue() implemented for config MBeans;
 *   - merge Jsr77 and config activity in runtime mbeans;
 *
 *   Revision 1.9  2003/07/18 20:14:43  kravtch
 *   1. ALL config mbeans are now covered by descriptors.xml
 *   2. new infrastructure for runtime mbeans is added
 *   3. generic constructors added to jsr77Mdl beans (String[])
 *   4. new test cases are added to admintest
 *   5. MBeanRegistryFactory has now different methods to obtain admin/runtime registries
 *   6. runtime-descriptors xml-file is added to build
 *
 *   Revision 1.8  2003/06/25 20:03:37  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/

package com.sun.enterprise.admin.config;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.ArrayList;

import javax.management.ObjectName;

//import com.sun.enterprise.admin.meta.MBeanRegistryEntry;

import javax.management.modelmbean.DescriptorSupport;
//import javax.management.modelmbean.InvalidTargetObjectTypeException;
//import javax.management.modelmbean.ModelMBean;
import javax.management.Descriptor;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.*;

import com.sun.org.apache.commons.modeler.BaseModelMBean;
//base admin imports
import com.sun.enterprise.admin.BaseAdminMBean;
import com.sun.enterprise.admin.MBeanHelper;

//Config imports
import com.sun.enterprise.config.ConfigException;
//import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.serverbeans.ElementProperty;

import java.util.logging.Level;

//naming
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.meta.naming.MBeanNamingInfo;
import com.sun.enterprise.admin.meta.MBeanMetaConstants;
import com.sun.enterprise.admin.meta.MBeanMetaHelper;

/**
 * <p>Base class for Config MBeans which implements basic config
 * activity according to ModelMBeanInfo provided by MBeanRegistry
 *
 *
 */

public class BaseConfigMBean extends BaseAdminMBean implements MBeanRegistration
{

    /**
     * FIXME: This is temporary. Ideally when we add a node controller or 
     * flush changes, we would like not to overwrite changes made on disk; 
     * however, all of the other flush() calls in the PE code, etc. implicitly
     * pass overwrite as true. When I try to pass false, I run into the problem
     * when multiple consecutive writes (e.g two calls to addNodeAgent) throw a "file
     * has been modified" exception. So there is a bug somewhare.
     */
    public static final boolean OVERWRITE = true;

    // ----------------------------------------------------------- Constructors

    private   ManagedConfigBean     mcb = null;
    protected MBeanRegistry         m_registry = null;    
    
    /**
     * Construct a <code>ModelMBean</code> with default
     * <code>ModelMBeanInfo</code> information.
     *
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception RuntimeOperationsException if an IllegalArgumentException
     *  occurs
     */
    public BaseConfigMBean() {
        super();
        m_registry = MBeanRegistryFactory.getAdminMBeanRegistry();
    }      

    // ----------------------------------------------------- Instance Variables



    // --------------------------------------------------- DynamicMBean Methods


    /**
     * Obtain and return the value of a specific attribute of this MBean.
     *
     * @param name Name of the requested attribute
     *
     * @exception AttributeNotFoundException if this attribute is not
     *  supported by this MBean
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception ReflectionException if a Java reflection exception
     *  occurs when invoking the getter
     */
    public Object getAttribute(String name)
        throws AttributeNotFoundException, MBeanException,
            ReflectionException {
                name = MBeanMetaHelper.mapToMBeanAttributeName(name);
                ModelMBeanAttributeInfo attrInfo = (ModelMBeanAttributeInfo)MBeanHelper.findMatchingAttributeInfo((MBeanInfo)info, name);
                if(attrInfo==null)
                    throw new AttributeNotFoundException(name);
                //FIXME add check "is readable"
                Object o = null;
                try {
                    o = super.getAttribute(name);
                } catch (Exception e) {
//                    try {
//                        if(mcb!=null)
                            o=mcb.getAttribute(attrInfo, name);
//                    } catch(Exception e1) {e1.printStackTrace();}
                }
                return o;
    }
    
    public AttributeList getAttributes(String[] attributeNames) {
        ArrayList names = mcb.getSimpleAttributeNames(attributeNames);
        AttributeList attrs = new AttributeList();
        for(int i=0; i<names.size(); i++) {
            try {
                String name = MBeanMetaHelper.mapToMBeanAttributeName((String)names.get(i));
                Object value  = getAttribute(name);
                attrs.add(new Attribute(name, value));
            } catch (Exception e) {
//                 attrs.add(new Attribute((String)names.get(i), null));
            }
        }
        return attrs;
    }

   
 /**
     * Set the instance handle of the object against which we will execute
     * all methods in this ModelMBean management interface.
     *
     * @param resource The resource object to be managed
     * @param type The type of reference for the managed resource
     *  ("ObjectReference", "Handle", "IOR", "EJBHandle", or
     *  "RMIReference" OR "ConfigBeanReference"  or "Jsr77ModelBeanReference")
     *
     * @exception InstanceNotFoundException if the managed resource object
     *  cannot be found
     * @exception InvalidTargetObjectTypeException if this ModelMBean is
     *  asked to handle a reference type it cannot deal with
     * @exception MBeanException if the initializer of the object throws
     *  an exception
     * @exception RuntimeOperationsException if the managed resource or the
     *  resource type is <code>null</code> or invalid
     */
    public void setManagedResource(Object resource, String type)
        throws InstanceNotFoundException, /*InvalidTargetObjectTypeException,*/
        MBeanException, RuntimeOperationsException {

        if (resource == null)
        {
            String msg = _localStrings.getString( "admin.server.core.mbean.config.base.managed_resource_is_null", mbeanType);
            throw new RuntimeOperationsException(new IllegalArgumentException(msg), msg);
        }
        if (MBeanMetaConstants.CONFIG_BEAN_REF.equalsIgnoreCase(type)) {
            if(! (resource instanceof ConfigBean)) 
            {
                String msg = _localStrings.getString( "admin.server.core.mbean.config.base.managed_resource_is_not_configbean", mbeanType);
                throw new RuntimeOperationsException(new ClassCastException(msg), msg);
            }
            this.mcb = new ManagedConfigBean(this, (ConfigBean) resource, m_registry);
            //Also add all attributes (and methods) to model mbean info
//            mcb.addResourceInfo();
            
        } else {
//            super.setManagedResource(resource, type);
        }
    }

    
    /**
     * Invoke a particular method on this MBean, and return any returned
     * value.
     *
     * <p><strong>IMPLEMENTATION NOTE</strong> - This implementation will
     * attempt to invoke this method on the MBean itself, or (if not
     * available) on the managed resource object associated with this
     * MBean.</p>
     *
     * @param name Name of the operation to be invoked
     * @param params Array containing the method parameters of this operation
     * @param signature Array containing the class names representing
     *  the signature of this operation
     *
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception ReflectioNException if a Java reflection exception
     *  occurs when invoking a method
     */
    public Object invoke(String name, Object params[], String signature[])
        throws MBeanException, ReflectionException {
            ModelMBeanOperationInfo opInfo = (ModelMBeanOperationInfo)MBeanHelper.findMatchingOperationInfo((MBeanInfo)info, name, signature);
            if (opInfo == null)
            {
                String msg = _localStrings.getString( "admin.server.core.mbean.config.base.operation_is_not_found", mbeanType, name);
                throw new MBeanException
                    (new ServiceNotFoundException(msg), msg);
            }
            Descriptor descr = opInfo.getDescriptor();
            
            Object ret; 
            //try MBean self
            try 
            {
                ret = MBeanHelper.invokeOperationInBean(opInfo, this, params);
                if(ret!=MBeanHelper.INVOKE_ERROR_SIGNAL_OBJECT)
                {
                    return ret;
                }
                //invoke in config bean
                if(mcb!=null && 
                   (ret=mcb.invokeOperation(opInfo, params, signature))!=MBeanHelper.INVOKE_ERROR_SIGNAL_OBJECT)
                {
                    return ret;
                }
                return super.invoke(name, params, signature);
            } 
            catch (MBeanException mbe)
            {
                _sLogger.log(Level.FINE, "mbean.baseconfig.invoke_exception",mbe);
                throw mbe;
            }
            catch (Exception e)
            {
                _sLogger.log(Level.FINE, "mbean.baseconfig.invoke_exception",e);
                String msg = _localStrings.getString( "admin.server.core.mbean.config.base.invoke_error", mbeanType, name);
                throw MBeanHelper.extractAndWrapTargetException(e, msg);
            }
    }

    //convenient method for deploy operations
    protected ObjectName createChildElementByType(String childElementName, Attribute[] attrs) throws Exception
    {
        return createChildElementByType(childElementName, attrs, true, false);
    }
    protected ObjectName createChildElementByType(String childElementName, Attribute[] attrs, boolean bSkipNullValued) throws Exception
    {
        return createChildElementByType(childElementName, attrs, bSkipNullValued, false);
    }
    protected ObjectName createChildElementByType(String childElementName, Attribute[] attrs, boolean bSkipNullValued,  boolean bOnlyOne) throws Exception
    {
        AttributeList list = new AttributeList();
        for(int i=0; i<attrs.length; i++)
        {
            if(!bSkipNullValued || attrs[i].getValue()!=null)
                list.add(attrs[i]);
        }
        ConfigBean bean = mcb.createChildByType(childElementName, list, null, bOnlyOne);
        return ConfigMBeanHelper.getChildObjectName(m_registry, info, bean);
    }

    
     /****************************************************************************************************************
     * Sets the values of several MBean's attributes.
     * @param attrList A list of attributes: The identification of the attributes to be set and the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     */

    public AttributeList setAttributes(AttributeList list) {
            try {
                return super.setAttributes(list);
            } catch (Exception e) {

//                try {
                    //no toString(). remove it. FIXME
                    if(mcb!=null)
                        return mcb.setAttributes(list);
//                } catch(Exception e1) {e1.printStackTrace();}
            }
            return null;
    }

    /**
     * Set the value of a specific attribute of this MBean.
     *
     * @param attribute The identification of the attribute to be set
     *  and the new value
     *
     * @exception AttributeNotFoundException if this attribute is not
     *  supported by this MBean
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception ReflectionException if a Java reflection exception
     *  occurs when invoking the getter
     */
    public void setAttribute(Attribute attribute)
        throws AttributeNotFoundException, MBeanException,
        ReflectionException
    {
                attribute = new Attribute(MBeanMetaHelper.mapToMBeanAttributeName(attribute.getName()), attribute.getValue());
                ModelMBeanAttributeInfo attrInfo = (ModelMBeanAttributeInfo)MBeanHelper.findMatchingAttributeInfo((MBeanInfo)info, attribute.getName());
                if(attrInfo==null)
                    throw new AttributeNotFoundException();
                //FIXME add check "is writable"
                
                try {
                    super.setAttribute(attribute);
                } catch (Exception e) {
                    
//                    try {
                        //no toString(). remove it. FIXME
                        if(mcb!=null)
                            mcb.setAttribute(attrInfo, attribute);
//                    } catch(Exception e1) {e1.printStackTrace();}
                }
        
    }


    // -------------------- Registration  --------------------
    // XXX We can add some method patterns here - like setName() and
    // setDomain() for code that doesn't implement the Registration

    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name)
            throws Exception
    {
/*        try
        {
            m_registry.notifyRegisterMBean(name, getConfigContext());
        }
        catch(Exception e)
        {
        }
 */
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
/*
        ObjectName oName=null;
        try
        {
            oName = ConfigMBeanHelper.getOwnObjectName(m_registry, info);
            m_registry.notifyUnregisterMBean(oName, getConfigContext());
        }
        catch(Exception e)
        {
        }
*/
    }
    
    public boolean destroyConfigElement() throws Exception //FIXME: MBeanException?
    {
        if(mcb==null) 
        {//FIXME: should be exception if null
            return false;
        }
        mcb.deleteSelf();
        //now unregister MBean
        ObjectName objectName = ConfigMBeanHelper.getOwnObjectName(m_registry, info);
        if(objectName!=null)
        {
            try{
                MBeanServer server = (MBeanServer)(MBeanServerFactory.findMBeanServer(null)).get(0);
                server.unregisterMBean(objectName);
                } 
            catch (Throwable t)
                {
                    _sLogger.fine("!!!!!!!!!!!!!! Can not unregister MBean: "+objectName);
                }
            return true;
    }

        return false;
        //FIXME handle exceptions
        //return false;
    }
 
    // ------------------------------------------------------ Convenient Methods
    //****************************************************************************
    protected ConfigContext getConfigContext()
    {
        if(mcb!=null) {
            return mcb.getConfigContext();        
        }
        return null;
    }
    /****************************************************************************************************************
     * Get base node Config Bean;
     * @return ConfigBean related to the MBean's related ConfigNode .
     */
    public ConfigBean getBaseConfigBean() {
        if(mcb!=null) {
           return mcb.getBaseConfigBean();
        }
        return null;
    }
    //****************************************************************************
    protected  String getDomainName() throws MBeanException
    {
        final ModelMBeanInfo info = (ModelMBeanInfo)getMBeanInfo();
        if (info != null) {
            String[] location =  MBeanHelper.getLocation(info);
            if(location!=null && location.length>0) {
                return location[0];
            }
        }
        return null;
    }
    
    //****************************************************************************
    protected  ManagedConfigBean getManagedConfigBean(ConfigBean cb) throws Exception
    {
        return new ManagedConfigBean(cb, m_registry, getDomainName());
    }
    
    //********************************************************************************************************************
    public ObjectName getConfigBeanObjectName(ConfigBean configBean) throws Exception
    {
        return ConfigMBeanHelper.getConfigBeanObjectName(
                m_registry, getDomainName(), configBean);
    }
    
    //****************************************************************************
    protected  MBeanServer getMBeanServer() throws MBeanException
    {
        return (MBeanServer)MBeanServerFactory.findMBeanServer(null).get(0);
    }
    //****************************************************************************
    protected  ObjectName getChildObjectName(String childMBeanType,  // type in Registry e.g. "jdbc-resource"
                                         String name // e.g. "myJndiName"; can be null for non keyed subelements 
                         ) throws MBeanException
    {
        if(m_registry==null)
            return null;
        String[] parentLocation =  MBeanHelper.getLocation((ModelMBeanInfo)getMBeanInfo());
        if(parentLocation==null || parentLocation.length==0)
            return null;
        if(name==null)
            return m_registry.getMbeanObjectName(childMBeanType, parentLocation);
        String[] childLocation = new String[parentLocation.length+1];
        for(int i=0; i<parentLocation.length; i++)
            childLocation[i] = parentLocation[i];
        childLocation[parentLocation.length] = name;
        return m_registry.getMbeanObjectName(childMBeanType, childLocation);
    }
       
    protected ObjectName getServerObjectName(String server) 
        throws MBeanException
    {
        return m_registry.getMbeanObjectName("server", new String[]{
            getDomainName(), server});
    }
  
    protected ObjectName[] toServerONArray(String[] ca) throws MBeanException
    {
        int num = ca.length;
        final ObjectName[] result = new ObjectName[num];
        
        for (int i = 0; i < num; i++)
        {
            result[i] = getServerObjectName(ca[i]);
        }
        return result;
    }   
              
    protected ObjectName getClusterObjectName(String name)
        throws MBeanException
    {
        return m_registry.getMbeanObjectName("cluster", new String[]{
            getDomainName(), name});
    }
    
    protected ObjectName[] toClusterONArray(String[] ca) throws MBeanException
    {
        int num = ca.length;
        final ObjectName[] result = new ObjectName[num];        
        for (int i = 0; i < num; i++)
        {
            result[i] = getClusterObjectName(ca[i]);
        }
        return result;
    }
    
    protected ObjectName getConfigurationObjectName(String name)
        throws MBeanException
    {       
        return m_registry.getMbeanObjectName("config", new String[]{
            getDomainName(), name});
    }  
    
    protected ObjectName[] toConfigurationONArray(String[] ca) throws MBeanException
    {
        int num = ca.length;
        final ObjectName[] result = new ObjectName[num];
        for (int i = 0; i < num; i++)
        {
            result[i] = getConfigurationObjectName(ca[i]);
        }
        return result;
    }
    
    protected ObjectName getNodeAgentObjectName(String name) 
        throws MBeanException
    {
        return m_registry.getMbeanObjectName("node-agent", new String[]{
            getDomainName(), name});
    }

    protected ObjectName[] toNodeAgentONArray(String[] names) throws MBeanException
    {
        int numNames = names.length;
        final ObjectName[] result = new ObjectName[numNames];        
        for (int i = 0; i < numNames; i++) {
            result[i] = getNodeAgentObjectName(names[i]);
        }
        return result;
    }
}
