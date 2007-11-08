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
 *   $Id: BaseRuntimeMBean.java,v 1.6 2006/12/15 23:35:32 msreddy Exp $
 *   @author: alexkrav
 *
 *   $Log: BaseRuntimeMBean.java,v $
 *   Revision 1.6  2006/12/15 23:35:32  msreddy
 *   Fix for bug# 6504614, fix provided by Lloyd, reviewdd by Sreeni, performed CTS management tests and  quicklook tests
 *
 *   Revision 1.5  2006/03/12 01:26:57  jluehe
 *   Renamed AS's org.apache.commons.* to com.sun.org.apache.commons.*, to avoid collisions with org.apache.commons.* packages bundled by webapps.
 *
 *   Tests run: QL, Servlet TCK
 *
 *   Revision 1.4  2005/12/25 03:47:41  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.3  2005/11/14 22:53:15  kravtch
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
 *   Revision 1.2  2005/06/27 21:19:46  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.8  2004/11/14 07:04:23  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.7  2004/02/20 03:56:18  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *   Revision 1.6.4.2  2004/02/02 07:25:22  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.6.4.1  2003/12/15 18:15:50  kravtch
 *   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *   Bug #4965366
 *   Reviewer: Sreeni
 *   Checked in PE8FCS_BRANCH
 *
 *   BaseRuntimeMBean.getAttributes() now returns empty AttributeList if there is null or empty names array argument.
 *
 *   Revision 1.6  2003/08/29 02:16:42  kravtch
 *   Bug #4910964 (and similar others)
 *   Reviewer: Sridatta
 *
 *   Exception handling and logging enchancements:
 *      - extraction target exception for MBeanException and TargetInvocationException:
 *      - switch to localStrings usage;
 *      - throwing exception for config MBeans if error in creation of ConfigBean;
 *      - exceptions for null-results in configbean operations,like getXXbyYYY() [changes commented because of crashing of quick test]
 *
 *   Revision 1.5  2003/08/24 02:58:50  kravtch
 *   dependancy from J2EEManagedObjectMdl
 *   is removed:  J2EEManagedObjectMdl->Object
 *    CVS: ----------------------------------------------------------------------
 *
 *   Revision 1.4  2003/08/14 23:16:17  kravtch
 *   invokeOperation() signature changed;
 *   BaseConfigMBean now uses mcb.invokeOperation();
 *
 *   Revision 1.3  2003/08/07 00:41:07  kravtch
 *   - new DTD related changes;
 *   - properties support added;
 *   - getDefaultAttributeValue() implemented for config MBeans;
 *   - merge Jsr77 and config activity in runtime mbeans;
 *
 *   Revision 1.2  2003/07/29 18:59:37  kravtch
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
 *   Revision 1.1  2003/07/18 20:14:46  kravtch
 *   1. ALL config mbeans are now covered by descriptors.xml
 *   2. new infrastructure for runtime mbeans is added
 *   3. generic constructors added to jsr77Mdl beans (String[])
 *   4. new test cases are added to admintest
 *   5. MBeanRegistryFactory has now different methods to obtain admin/runtime registries
 *   6. runtime-descriptors xml-file is added to build
 *
 *
 *
*/

package com.sun.enterprise.admin.runtime;

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

import com.sun.enterprise.admin.config.ManagedConfigBean;
import com.sun.enterprise.admin.config.ConfigMBeanHelper;
import com.sun.enterprise.admin.config.MBeanConfigException;

//Config imports
import com.sun.enterprise.config.ConfigException;
//import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.serverbeans.ElementProperty;

//naming
import com.sun.enterprise.admin.meta.naming.MBeanNamingInfo;
import com.sun.enterprise.admin.meta.MBeanMetaConstants;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.meta.MBeanRegistry;

import com.sun.appserv.management.util.jmx.JMXUtil;

//import com.sun.enterprise.management.model.J2EEManagedObjectMdl;

/**
 * <p>Base class for Config MBeans which implements basic config
 * activity according to ModelMBeanInfo provided by MBeanRegistry
 *
 *
 */

public class BaseRuntimeMBean extends BaseAdminMBean implements MBeanRegistration
{

    // ----------------------------------------------------- Instance Variables
    private ManagedConfigBean       mcb = null;
    private ManagedJsr77MdlBean     mrb = null;
    protected MBeanRegistry         m_registry = null;

    ObjectName  mSelfObjectName = null;

    // ----------------------------------------------------------- Constructors
    /**
     * Construct a <code>ModelMBean</code> with default
     * <code>ModelMBeanInfo</code> information.
     *
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception RuntimeOperationsException if an IllegalArgumentException
     *  occurs
     */
    public BaseRuntimeMBean() {

        super();
        m_registry = MBeanRegistryFactory.getRuntimeMBeanRegistry();
    }


 /**
     * Set the instance handle of the object against which we will execute
     * all methods in this ModelMBean management interface.
     *
     * @param resource The resource object to be managed
     * @param type The type of reference for the managed resource
     *  ("ObjectReference", "Handle", "IOR", "EJBHandle", or
     *  "RMIReference" OR "ConfigBeanReference" or "Jsr77ModelBeanReference" )
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
            throw new RuntimeOperationsException
                (new IllegalArgumentException("Managed resource is null"),
                 "Managed resource is null");
        
        if (MBeanMetaConstants.CONFIG_BEAN_REF.equalsIgnoreCase(type)) {
            if(! (resource instanceof ConfigBean)) {
                throw new RuntimeOperationsException
                 (new ClassCastException("Managed resource is not a ConfigBean"),
                 "Managed resource is not a ConfigBean");
            }
            this.mcb = new ManagedConfigBean(this, (ConfigBean) resource, m_registry);
            
        } else {
            if (MBeanMetaConstants.JSR77_MODEL_BEAN_REF.equalsIgnoreCase(type)) 
            {
/*                if(! (resource instanceof J2EEManagedObjectMdl)) {
                    throw new RuntimeOperationsException
                     (new ClassCastException("Managed resource is not a Jsr77ModelBean :"+resource.getClass().getName()),
                     "Managed resource is not a Jsr77ModelBean: "+resource.getClass().getName());
                }
 */
                this.mrb = new ManagedJsr77MdlBean(this, /*(J2EEManagedObjectMdl)*/ resource);
            } else 
            {
                super.setManagedResource(resource, type);
            }
        }
    }

    



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
    public Object getAttribute(String name) throws AttributeNotFoundException, MBeanException, ReflectionException 
    {
        ModelMBeanAttributeInfo attrInfo = (ModelMBeanAttributeInfo)MBeanHelper.findMatchingAttributeInfo((MBeanInfo)info, name);
        if(attrInfo==null)
            throw new AttributeNotFoundException();
        //FIXME add check "is readable"
        //1. MBean 
        try 
        {
            return super.getAttribute(name);
        } catch (Exception e) {}

        //2. runtime managed bean
        if(mrb!=null)
        {
            // our delegate is not an MBean and won't have its ObjectName
            if ( "objectName".equals( name ) )
            {
                // use of JMXUtil produces a consistently-ordered String for all ObjectNames
                return JMXUtil.toString( mSelfObjectName );
            }
            else
            {
                try
                {
                    return mrb.getAttribute(attrInfo, name);
                }
                catch (Exception e)
                {
                    // yuck, this whole method stinks, but this is the way it was
                }
            }
        }

        //3. config managed bean
        if(mcb!=null)
        {
            try {
                return mcb.getAttribute(attrInfo, name);
            } catch (Exception e) {}
        }
        throw new AttributeNotFoundException(); //?????
    }
    
    public AttributeList getAttributes(String[] attributeNames) 
    {
        AttributeList list = new AttributeList();
        if(attributeNames!=null)
            for(int i=0; i<attributeNames.length; i++)
            {
                try {
                    Object value = getAttribute(attributeNames[i]);
                    list.add(new Attribute(attributeNames[i], value));
                } catch (Exception e) {}
            }
        return list;
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
        ModelMBeanAttributeInfo attrInfo = (ModelMBeanAttributeInfo)MBeanHelper.findMatchingAttributeInfo((MBeanInfo)info, attribute.getName());
        if(attrInfo==null)
            throw new AttributeNotFoundException();
        //FIXME add check "is writable"

        //1. MBean 
        try 
        {
            super.setAttribute(attribute);
            return;
        } catch (Exception e) {}

        //2. runtime managed bean
        if(mrb!=null)
        {
            try {
                mrb.setAttribute(attrInfo, attribute);
                return;
            } catch (Exception e) {}
        }

        //3. config managed bean
        if(mcb!=null)
        {
            try {
                mcb.setAttribute(attrInfo, attribute);
                return;
            } catch (Exception e) {}
        }

    }

    /****************************************************************************************************************
     * Sets the values of several MBean's attributes.
     * @param attrList A list of attributes: The identification of the attributes to be set and the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     */
    public AttributeList setAttributes(AttributeList list) 
    {
        if(list==null || list.size()<=0)
            return null;
        AttributeList listRes = new AttributeList();
        for(int i=0; i<list.size(); i++)
        {
            try {
                Attribute attr = (Attribute)list.get(i);
                setAttribute(attr);
                listRes.add(attr);
            } catch (Exception e) {}
        }
        return listRes;
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

            //Generic Config invoker
            Object ret;
            try 
            {
                ret = MBeanHelper.invokeOperationInBean(opInfo, this, params);
                if(ret!=MBeanHelper.INVOKE_ERROR_SIGNAL_OBJECT)
                    return ret;

                if(mrb!=null)
                {
                    ret = mrb.invokeOperation(opInfo, params, signature);
                    if(ret!=MBeanHelper.INVOKE_ERROR_SIGNAL_OBJECT)
                        return ret;
                }

                if(mcb!=null)
                {
                    ret = mcb.invokeOperation(opInfo, params, signature);
                    if(ret!=MBeanHelper.INVOKE_ERROR_SIGNAL_OBJECT)
                        return ret;
                }

            } 
            catch (MBeanException mbe)
            {
                throw mbe;
            }
            catch (Exception e)
            {
                String msg = _localStrings.getString( "admin.server.core.mbean.runtime.base.invoke_error", mbeanType, name);
                throw MBeanHelper.extractAndWrapTargetException(e, msg);
            }

            //TBD FIXME
            //value = ConfigBeanHandler.invoke(this.cb, name, params, signature);
            return super.invoke(name, params, signature);
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

    
    // -------------------- Registration  --------------------
    // XXX We can add some method patterns here - like setName() and
    // setDomain() for code that doesn't implement the Registration

    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name)
            throws Exception
    {
        mSelfObjectName = name;
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
        mSelfObjectName = null;
    }
    
/*    public boolean destroyConfigElement() throws Exception //FIXME: MBeanException?
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
                    System.out.println("!!!!!!!!!!!!!! Can not unregister MBean: "+objectName);
                }
            return true;
        }

        return false;
        //FIXME handle exceptions
        //return false;
    }
*/
}
