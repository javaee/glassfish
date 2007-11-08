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
 *   $Id: MBeanHelper.java,v 1.6 2007/04/03 01:13:38 llc Exp $
 *   @author: alexkrav
 *
 *   $Log: MBeanHelper.java,v $
 *   Revision 1.6  2007/04/03 01:13:38  llc
 *   Issue number:  2752
 *   Obtained from:
 *   Submitted by:  Lloyd Chambers
 *   Reviewed by:   3 day timeout expired
 *
 *   Revision 1.5  2006/03/08 01:34:34  kravtch
 *   Bug #6239362(many MBean server validation messages are not i18n-ed)
 *   admin-core/admin:
 *     - "Target exception message" string literal put into localstrings
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules admin-core/admin;
 *   Tests passed: QLT/EE
 *
 *   Revision 1.4  2005/12/25 03:47:28  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.3  2005/08/16 22:19:29  kravtch
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
 *   Revision 1.9  2004/11/14 07:04:16  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.8  2004/02/20 03:56:05  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *   Revision 1.7  2003/12/13 16:52:50  qouyang
 *   MERGE: from PE8FCS_BRANCH (archived by TAG S1AS8PE-FCSBRANCH-MERGE-POINT-12_Dec_2003)
 *          to   HEAD (archived by TAG S1AS8EE-HEADBRANCH-PRE-MERGE-12_Dec_2003)
 *
 *   Revision 1.6.4.2  2004/02/02 07:25:12  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.6.4.1  2003/12/01 21:52:37  kravtch
 *   Bug #4939964
 *   Reviewer: Sridatta
 *   admin.config.ManagedConfigBean.createChildByType() now analyzes registryEntries.AttributeInfo for each "empty"  valued attribute (similar to setAttribute(), but could not use MBeanAttributeInfo because MBean is not exists yet). If "emptyValueAllowed" field in registrEntry.AttributeInfo is not "true", then "empty" attribute will be ignored.
 *
 *   Revision 1.6  2003/09/26 21:33:02  kravtch
 *   Bug #4926266
 *   Reviewer: Sridatta
 *      - new test cases added to Validator's ThreadPoolTest DELETE - isThreadPoolReferencedFromOrb and isThreadPoolReferencedFromResAdapter to avoid deleteion of the referenced element
 *      - correspondent localStrings are added;
 *      - ManagedConfigBean.deleteSelf() now throws Exception;
 *      - MBeanHelper.invokeOperationInBean() will not suppress runtime exceptions any more;
 *
 *   Revision 1.5  2003/09/08 00:55:27  se113266
 *   Bugsfixed: 4917554
 *   CheckinComments: isSignatureEqual throws NPE when signature==null and
 *   sign!=null. Added a check
 *   Reviewed by: Sridatta
 *
 *   Revision 1.4  2003/08/29 02:16:40  kravtch
 *   Bug #4910964 (and similar others)
 *   Reviewer: Sridatta
 *
 *   Exception handling and logging enchancements:
 *      - extraction target exception for MBeanException and TargetInvocationException:
 *      - switch to localStrings usage;
 *      - throwing exception for config MBeans if error in creation of ConfigBean;
 *      - exceptions for null-results in configbean operations,like getXXbyYYY() [changes commented because of crashing of quick test]
 *
 *   Revision 1.3  2003/07/18 20:14:42  kravtch
 *   1. ALL config mbeans are now covered by descriptors.xml
 *   2. new infrastructure for runtime mbeans is added
 *   3. generic constructors added to jsr77Mdl beans (String[])
 *   4. new test cases are added to admintest
 *   5. MBeanRegistryFactory has now different methods to obtain admin/runtime registries
 *   6. runtime-descriptors xml-file is added to build
 *
 *   Revision 1.2  2003/06/25 20:03:36  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/

package com.sun.enterprise.admin;

import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import com.sun.enterprise.admin.meta.MBeanMetaConstants;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.meta.MBeanRegistryEntry;

import com.sun.enterprise.admin.meta.naming.MBeanNamingDescriptor;
import com.sun.enterprise.admin.meta.naming.MBeanNamingInfo;


//import com.sun.enterprise.config.ConfigBean;

//JMX imports
import javax.management.MBeanException;
import javax.management.Descriptor;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.ModelMBeanInfo;

// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 *
 */
//*******************************************************************************************
public class MBeanHelper implements MBeanMetaConstants
{
    // Logging
    static protected final Logger _sLogger = LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    static protected final StringManager _localStrings = StringManager.getManager( BaseAdminMBean.class );

    public static final Object INVOKE_ERROR_SIGNAL_OBJECT = new Object();
    
    //********************************************************************************************************************
    // get Descriptor's field value by its name 
    public static Object getDescriptorFieldValue(ModelMBeanInfo info, String name) throws MBeanException
    {
        Descriptor descr = info.getMBeanDescriptor();
        return descr.getFieldValue(name);
        
    }

    //********************************************************************************************************************
    // get "Location" field value from MBean's Descriptor
    public static String[] getLocation(ModelMBeanInfo info) throws MBeanException
    {
        return (String[])getDescriptorFieldValue(info, NMLOCATION_FIELD_NAME);
        
    }

    //********************************************************************************************************************
    // get "XPath" field value from MBean's Descriptor (contains XPath pattern from mbean-descriptor-entry)
    public static String getXPathPattern(ModelMBeanInfo info) throws MBeanException
    {
        return (String)getDescriptorFieldValue(info, XPATH_FIELD_NAME);
    }

    
/*    //********************************************************************************************************************
    public static ObjectName[] getChildObjectNames(ModelMBeanInfo parentInfo, ConfigBean[] children) throws Exception
    {
        ObjectName[] objNames = new ObjectName[children.length];
        for(int i=0; i<children.length; i++)
            objNames[i] = getChildObjectName(parentInfo, children[i]);
        return objNames;
    }
    //********************************************************************************************************************
    public static ObjectName getChildObjectName(ModelMBeanInfo parentInfo, ConfigBean childBean) throws Exception
    {
        Descriptor descr = parentInfo.getMBeanDescriptor();
        //String rel = childBean.getRelativeXPath();
        String rel = childBean.getAbsoluteXPath("");
        rel = rel.substring(rel.lastIndexOf('/')+1);
        String xpath = (String)descr.getFieldValue(XPATH_FIELD_NAME);
        String prefix;
        int    pos = rel.indexOf('[');
        if(pos>0)
           prefix = xpath+"/"+rel.substring(0, pos);
        else
           prefix = xpath+"/"+rel;
        MBeanRegistry registry = MBeanRegistryFactory.getMBeanRegistry();
        MBeanRegistryEntry entry = registry.findMBeanRegistryEntryByXPathPattern(prefix);
        MBeanNamingDescriptor namingDescr = entry.getNamingDescriptor();
        
        //
        String[] parentLocation = (String[])descr.getFieldValue(NMLOCATION_FIELD_NAME);
        String[] childLocation = parentLocation;
        childLocation = new String[parentLocation.length+1];
        for(int i=0; i<parentLocation.length; i++)
        {
            childLocation[i] = parentLocation[i];
        }
        if(pos>0)
        {
            int beg = rel.indexOf('=', pos) + 1;
            if(rel.charAt(beg)=='\'')
                beg++;
            int end = rel.indexOf(']', beg) - 1;
            if(rel.charAt(end)=='\'')
                end--;
            childLocation[parentLocation.length]=rel.substring(beg,end+1);
        }
        else
            childLocation[parentLocation.length]=rel;
        MBeanNamingInfo nmi = new MBeanNamingInfo(namingDescr, namingDescr.getType(), childLocation);
        return nmi.getObjectName();
    }
*/
   //********************************************************************************************************************
    private static Class getAttributeClass(String signature) throws Exception
    {
        if (signature.equals(Boolean.TYPE.getName()))
            return Boolean.TYPE;
        else if (signature.equals(Byte.TYPE.getName()))
            return Byte.TYPE;
        else if (signature.equals(Character.TYPE.getName()))
            return Character.TYPE;
        else if (signature.equals(Double.TYPE.getName()))
            return Double.TYPE;
        else if (signature.equals(Float.TYPE.getName()))
            return Float.TYPE;
        else if (signature.equals(Integer.TYPE.getName()))
            return Integer.TYPE;
        else if (signature.equals(Long.TYPE.getName()))
            return Long.TYPE;
        else if (signature.equals(Short.TYPE.getName()))
            return Short.TYPE;
        else {
            try {
                ClassLoader cl=Thread.currentThread().getContextClassLoader();
                if( cl!=null )
                    return cl.loadClass(signature); 
            } catch( ClassNotFoundException e ) {
            }
//            try {
                return Class.forName(signature);
//            } catch (ClassNotFoundException e) {
//                throw new ReflectionException
//                    (e, "Cannot find Class for " + signature);
//            }
        }
    }
   //********************************************************************************************************************
    public static String[] getParamTypesFromOperationInfo(MBeanOperationInfo opInfo)
    {
        MBeanParameterInfo[] params = opInfo.getSignature();
        if(params==null)
            return new String[0];
        ArrayList signature = new ArrayList();
        for(int i=0; i<params.length; i++)
            signature.add(params[i].getType());
        return (String[])signature.toArray(new String[signature.size()]);
    }
    //********************************************************************************************************************
    public static Class[] getSignatureFromOperationInfo(MBeanOperationInfo opInfo) throws Exception
    {
        MBeanParameterInfo[] params = opInfo.getSignature();
        if(params==null)
            return new Class[0];
        ArrayList signature = new ArrayList();
        for(int i=0; i<params.length; i++)
            signature.add(getAttributeClass(params[i].getType()));
        return (Class[])signature.toArray(new Class[signature.size()]);
    }

    //********************************************************************************************************************
    public static  MBeanOperationInfo findMatchingOperationInfo(MBeanInfo mbeanInfo, String name, String signature[])
    {
        MBeanOperationInfo[] opInfos = mbeanInfo.getOperations();
        if(opInfos==null)
            return null;
        boolean bMatch;
        for(int i=0; i<opInfos.length; i++)
        {
            if(name.equals(opInfos[i].getName()))
            {
                String sign[] = getParamTypesFromOperationInfo(opInfos[i]);
                if(isSignaturesEqual(sign, signature))
                    return opInfos[i];
            }
        }
        return null;
    }

    //********************************************************************************************************************
    public static  MBeanAttributeInfo findMatchingAttributeInfo(MBeanInfo mbeanInfo, String name)
    {
        MBeanAttributeInfo[] attrInfos = mbeanInfo.getAttributes();
        if(attrInfos==null)
            return null;
        boolean bMatch;
        for(int i=0; i<attrInfos.length; i++)
        {
            if(name.equals(attrInfos[i].getName()))
            {
                return attrInfos[i];
            }
        }
        return null;
    }

    //********************************************************************************************************************
    public static  Object invokeOperationInBean(MBeanOperationInfo opInfo, Object bean, Object[] params) throws Exception
    {
        return invokeOperationInBean(opInfo.getName(), opInfo, bean, params);
    }
    //********************************************************************************************************************
    public static  Object invokeOperationInBean(String opName, MBeanOperationInfo opInfo, Object bean, Object[] params) throws Exception
    {
        Method method = null;
        try 
        {
            method = findMatchingOperationMethod(opName, opInfo, bean);
        }
        catch (Exception e)
        {
            if(e instanceof SecurityException)
                _sLogger.log(Level.FINEST, "invokeOperationInBean() failed", e);
        }
        if(method==null)
           return INVOKE_ERROR_SIGNAL_OBJECT;
        return method.invoke(bean,  params);
    }
    
    //********************************************************************************************************************
    public static  Method findMatchingOperationMethod(String opName, MBeanOperationInfo opInfo, Object bean) throws Exception
    {
//        try
        {
           Class  signature[] = getSignatureFromOperationInfo(opInfo);
           return bean.getClass().getMethod(opName, signature);
        }
/*        catch (NoSuchMethodException nsme)
        {
        }
        catch (SecurityException nsme)
        {
        }
        return null;
 */
    }
    //********************************************************************************************************************
    private static  boolean isSignaturesEqual(String sign[], String signature[])
    {
        //signature test
        if ((signature==null||signature.length==0) &&
            (sign==null||sign.length==0) )
            return true;
        if((signature==null && sign!=null) ||
           (signature!=null && sign==null) )
           return false;
        if(signature.length != sign.length)
            return false;
        boolean bMatch = true;
        for(int j=0; j<sign.length; j++)
        {
            if(!sign[j].equals(signature[j]))
            {
                bMatch = false;
                break;
            }
        }
        return bMatch;
    }
    //****************************************************************************
    public static  MBeanException extractAndWrapTargetException(Exception e, String wrapMsg)
    {
         while(e instanceof InvocationTargetException ||
               e instanceof MBeanException)
         {
             if(e instanceof InvocationTargetException)
                {
                    Throwable t = ((InvocationTargetException)e).getTargetException();
                    if (t instanceof Exception)
                        e = (Exception)t;
                    else
                        e = new Exception(t.getMessage());
                }
             else
                 if(e instanceof MBeanException)
                    {
                        e = ((MBeanException)e).getTargetException();
                    }
         }
        String msg  = e.getMessage();
        String targetMsgPref = _localStrings.getString( "admin.server.core.mbean.target_exception_prefix");
        if(msg!=null)
            return new MBeanException(e, wrapMsg + "\n"+ targetMsgPref + ": "  + e.getMessage());
        else
            return new MBeanException(e, wrapMsg);
    }
}
