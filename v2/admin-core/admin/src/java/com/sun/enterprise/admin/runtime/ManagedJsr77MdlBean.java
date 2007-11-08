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
 *   $Id: ManagedJsr77MdlBean.java,v 1.3 2005/12/25 03:47:41 tcfujii Exp $
 *   @author: alexkrav
 *
 *   $Log: ManagedJsr77MdlBean.java,v $
 *   Revision 1.3  2005/12/25 03:47:41  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.2  2005/06/27 21:19:46  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.9  2005/05/07 04:35:28  ruyakr
 *   Engineer: Rob Ruyak
 *
 *   Fixed warning messages displayed from jdk1.5 because of introduction of varargs in certain apis such as ja
 *   va.lang.Class.
 *
 *
 *   This is my last checkin here at Sun! Thanks for all the good times!
 *
 *   Revision 1.8  2004/11/14 07:04:24  tcfujii
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
 *   Revision 1.6.4.2  2004/02/02 07:25:23  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.6.4.1  2004/01/20 20:39:32  ramakant
 *   Bug# 4958195
 *   Reviewers: Abhijit, Alex, Prashanth
 *   Fix:
 *   reintroduced the deprecated command - jms-ping to cli.
 *   added a new operation pingJMS to J2EEServerMdl.
 *   modified exception handling in ManagedJsr77MdlBean.
 *
 *   Revision 1.6  2003/08/24 02:58:50  kravtch
 *   dependancy from J2EEManagedObjectMdl
 *   is removed:  J2EEManagedObjectMdl->Object
 *    CVS: ----------------------------------------------------------------------
 *
 *   Revision 1.5  2003/08/20 15:41:34  kravtch
 *   added support for primitive parameters handling (for JSR77 objects):
 *      getClassForName();
 *
 *   Revision 1.4  2003/08/16 21:26:19  kravtch
 *   repare for setAttribute in runtime mbean:
 *   descriptor.GETTER__FIELD_NAME->SETTER_FIELD_NAME
 *
 *   Revision 1.3  2003/08/14 23:16:17  kravtch
 *   invokeOperation() signature changed;
 *   BaseConfigMBean now uses mcb.invokeOperation();
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
 *   Revision 1.1  2003/07/18 20:14:47  kravtch
 *   1. ALL config mbeans are now covered by descriptors.xml
 *   2. new infrastructure for runtime mbeans is added
 *   3. generic constructors added to jsr77Mdl beans (String[])
 *   4. new test cases are added to admintest
 *   5. MBeanRegistryFactory has now different methods to obtain admin/runtime registries
 *   6. runtime-descriptors xml-file is added to build
 *
*/

package com.sun.enterprise.admin.runtime;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

//JMX imports
import javax.management.*;
import javax.management.Descriptor;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;

//import com.sun.enterprise.management.model.J2EEManagedObjectMdl;

import com.sun.enterprise.admin.MBeanHelper;
import com.sun.enterprise.admin.meta.MBeanMetaConstants;

/****************************************************************************************************************
 */
public class ManagedJsr77MdlBean {
       
    DynamicMBean            m_mbean         = null;
    /*J2EEManagedObjectMdl*/ Object    m_baseJsr77Bean = null;

    public ManagedJsr77MdlBean(DynamicMBean mbean, /*J2EEManagedObjectMdl*/ Object cb)
    {
        m_baseJsr77Bean = cb;
        m_mbean = mbean;
    }
    /****************************************************************************************************************
     * Gets MBean's attribute value.
     * @param externalName the MBean's attribute name.
     * @return The value of the attribute retrieved.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public Object getAttribute(ModelMBeanAttributeInfo attrInfo, String attrName)  throws MBeanException,AttributeNotFoundException {
        Descriptor descr = attrInfo.getDescriptor();
        String getter = (String)descr.getFieldValue(MBeanMetaConstants.GETTER_FIELD_NAME);
        if(getter==null)
            throw new MBeanException(new MBeanRuntimeException("ManagedJsr77MdlBean:getAttribute:No getter found in descriptor"));
        try 
        {
            Method method = m_baseJsr77Bean.getClass().getMethod(getter);
            return method.invoke(m_baseJsr77Bean);
        }
        catch (Exception e)
        {
            //throw new MBeanException(e, "Exception invoking getter method in runtime bean " + getter);
            throw MBeanHelper.extractAndWrapTargetException(e, 
                "Exception invoking getter method in runtime bean " + getter);
        }
    }
    /****************************************************************************************************************
     * Sets MBean's attribute value.
     * @param attr The identification of the attribute to be set and the value it is to be set to.
     *  @throws MBeanException exception
     *  @throws AttributeNotFoundException exception
     */
    public void setAttribute(ModelMBeanAttributeInfo attrInfo, Attribute attr)  throws MBeanException,AttributeNotFoundException {
        Descriptor descr = attrInfo.getDescriptor();
        String setter = (String)descr.getFieldValue(MBeanMetaConstants.SETTER_FIELD_NAME);
        if(setter==null)
            throw new MBeanException(new MBeanRuntimeException("ManagedJsr77MdlBean:getAttribute:No setter found in descriptor"));
        try 
        {
            Method method = m_baseJsr77Bean.getClass().getMethod(setter, new Class[]{getClassForName(attrInfo.getType())});
            method.invoke(m_baseJsr77Bean, new Object[]{attr.getValue()});
        }
        catch (Exception e)
        {
//e.printStackTrace();
            //throw new MBeanException(e, "Exception invoking setter method in runtime bean " + setter);
            throw MBeanHelper.extractAndWrapTargetException(e, 
                "Exception invoking setter method in runtime bean " + setter);
        }
    }

    public Object invokeOperation(ModelMBeanOperationInfo opInfo, Object params[], String signature[]) throws MBeanException, ReflectionException 
    {
       String name = opInfo.getName();
        try 
        {
            Object ret = MBeanHelper.invokeOperationInBean(opInfo, this, params);
            if(ret!=MBeanHelper.INVOKE_ERROR_SIGNAL_OBJECT)
            {
                return ret;
            }
        } 
        catch (Exception e)
        {
            //throw new MBeanException(e, "Exception invoking method in runtime bean" + name);
            throw MBeanHelper.extractAndWrapTargetException(e, 
                "Exception invoking method in runtime bean " + name);
        }

       try 
        {
            return MBeanHelper.invokeOperationInBean(opInfo, m_baseJsr77Bean, params);
            // IT MAYBE:MBeanHelper.INVOKE_ERROR_SIGNAL_OBJECT !!!
        } 
        catch (Exception e)
        {
            //throw new MBeanException(e, "Exception invoking method in runtime bean" + name);
            throw MBeanHelper.extractAndWrapTargetException(e, 
                "Exception invoking method in runtime bean " + name);
        }
    }
private Class getClassForName(String type) throws ClassNotFoundException
    {
        if(type.equals("int"))
           return Integer.TYPE;
        if(type.equals("long"))
           return Long.TYPE;
        if(type.equals("boolean"))
           return Boolean.TYPE;
        return Class.forName(type);
    }
}
 
