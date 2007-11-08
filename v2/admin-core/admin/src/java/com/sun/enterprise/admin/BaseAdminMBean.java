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
 *   $Id: BaseAdminMBean.java,v 1.4 2007/04/03 01:13:38 llc Exp $
 *   @author: alexkrav
 *
 *   $Log: BaseAdminMBean.java,v $
 *   Revision 1.4  2007/04/03 01:13:38  llc
 *   Issue number:  2752
 *   Obtained from:
 *   Submitted by:  Lloyd Chambers
 *   Reviewed by:   3 day timeout expired
 *
 *   Revision 1.3  2005/12/25 03:47:28  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.2  2005/06/27 21:19:39  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.9  2004/11/14 07:04:15  tcfujii
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
 *   Revision 1.7  2003/11/11 01:33:13  ai109478
 *   Initial notification related changes. It supports the notion of fat pipe model.
 *
 *   Revision 1.6.4.1  2004/02/02 07:25:12  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.6  2003/08/29 02:16:40  kravtch
 *   Bug #4910964 (and similar others)
 *   Reviewer: Sridatta
 *
 *   Exception handling and logging enchancements:
 *      - extraction target exception for MBeanException and TargetInvocationException:
 *      - switch to localStrings usage;
 *      - throwing exception for config MBeans if error in creation of ConfigBean;
 *      - exceptions for null-results in configbean operations,like getXXbyYYY() [changes commented because of crashing of quick test]
 *
 *   Revision 1.5  2003/08/14 20:40:46  kravtch
 *   _sLogger and _LocalStrings are defined and set in the base class for all infra mbeans.
 *
 *   Revision 1.4  2003/06/25 20:03:35  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/

package com.sun.enterprise.admin;

//JMX imports
import javax.management.DynamicMBean;
import javax.management.AttributeList;
import javax.management.MBeanInfo;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.InstanceNotFoundException;
import javax.management.RuntimeOperationsException;
import javax.management.Descriptor;

import javax.management.modelmbean.ModelMBeanInfo;

/* New for 8.0 */
//import com.sun.enterprise.admin.server.core.jmx.Introspector;
import java.lang.reflect.Method;

import com.sun.enterprise.admin.meta.MBeanMetaConstants;

// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import javax.management.NotificationBroadcasterSupport;
import com.sun.enterprise.util.i18n.StringManager;

/**
    The base class for all the MBeans. Note that there will be no MBean that
	is registered in the MBeanServer for this class. It serves as the base class
	of all concrete implementations of MBeans.
 
*/

public class BaseAdminMBean extends NotificationBroadcasterSupport implements DynamicMBean
{
	protected ModelMBeanInfo      info = null;
    protected String              mbeanType = "unknown";

    // Logging
    static final protected Logger _sLogger = LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    protected final StringManager _localStrings;
    public BaseAdminMBean() {
        _localStrings = StringManager.getManager( BaseAdminMBean.class );
    }
    
    public void setModelMBeanInfo(ModelMBeanInfo mbeanInfo) {
        info = mbeanInfo;
        //now we can set the type of bean
        try {
            Descriptor descr = info.getMBeanDescriptor();
            mbeanType = (String)descr.getFieldValue(MBeanMetaConstants.NMTYPE_FIELD_NAME);
        } catch (Exception e)
        {
        }
    }

    public void setManagedResource(Object resource, String type)
                                throws  InstanceNotFoundException, /*InvalidTargetObjectTypeException,*/
                                        MBeanException, RuntimeOperationsException {

        String msg = _localStrings.getString("mbean.baseadmin.setmanagedresource_not_implemented");
        throw new UnsupportedOperationException( msg );
    }

    public Object getAttribute(String attributeName) throws
        AttributeNotFoundException, MBeanException, ReflectionException {
	String msg = _localStrings.getString("mbean.baseadmin.getattribute_not_implemented");
        throw new UnsupportedOperationException( msg );
    }

    public AttributeList getAttributes(String[] attributeNames) {
	String msg = _localStrings.getString("mbean.baseadmin.getattributes_not_implemented");
        throw new UnsupportedOperationException( msg );
    }

    public MBeanInfo getMBeanInfo() {
        return (MBeanInfo)info;
    }

    /** Abstract method that subclasses have to implement. This is the way for
     * invoke method to work, through reflection.
    */
//    protected abstract Class getImplementingClass();

    /** Reflection requires the implementing object. */
//    protected abstract Object getImplementingMBean();   

    /**
     * Every resource MBean should override this method to execute specific
     * operations on the MBean. This method is enhanced in 8.0. It was a no-op
     * in 7.0. In 8.0, it is modified to invoke the actual method through
     * reflection. It relieves all the subclasses to implement the invoke method
     * for various operations. If the subclasses choose to implement it, they may
     * do so.
     * @since 8.0
     * @see javax.management.MBeanServer#invoke
     * @see #getImplementingClass
    */
    public Object invoke(String methodName, Object[] methodParams,
        String[] methodSignature) throws MBeanException, ReflectionException {
            /*
    
        final Class implClass = this.getClass(); //getImplementingClass();
        final Object mbeanReference = this; //getImplementingMBean();
        final Introspector    reflector       = new Introspector(implClass);
        Object value = null;
        try {

            final Method method = reflector.getMethod(methodName, methodSignature);
            value = reflector.invokeMethodOn(method, mbeanReference, methodParams);
            return ( value );
        }
        catch (java.lang.ClassNotFoundException cnfe) {
            throw new javax.management.ReflectionException(cnfe);
        }
        catch (java.lang.NoSuchMethodException nsme) {
            throw new javax.management.ReflectionException(nsme);
        }
        catch (java.lang.SecurityException se) {
            throw new javax.management.ReflectionException(se);
        }
        catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof MBeanException) {
                throw (MBeanException)t;
            }
            else
                if (t instanceof Exception) {
                    throw new MBeanException((Exception) t);
                }
                else { //if an error 
                    String msg = _localStrings.getString("mbean.baseadmin.admin.server.core.jmx.error_from_mbean", t.getMessage() );
                    RuntimeException re = new RuntimeException( msg );
                    throw new MBeanException(re);
                    //Do what?
                }
        }
        catch (java.lang.IllegalAccessException iae) {
            throw new javax.management.ReflectionException(iae);
        }
        catch (Exception e) {
            throw new MBeanException(e);            
        }
    */
            
            throw new java.lang.UnsupportedOperationException("Not Yet Implemented");
    }

    public void setAttribute(Attribute attribute) throws
        AttributeNotFoundException, InvalidAttributeValueException,
        MBeanException, ReflectionException {

        String msg = _localStrings.getString("mbean.baseadmin.setattribute_not_implemented" );
        throw new UnsupportedOperationException( msg );
    }

    public AttributeList setAttributes(AttributeList parm1) {
	String msg = _localStrings.getString("mbean.baseadmin.setattributes_not_implemented" );
        throw new UnsupportedOperationException( msg );
    }
    /** 
     * call app server logging
     */
    protected boolean isDebugEnabled() {
        return true;
    }
    
    protected void debug(String s) {
        //TODO: change this to app server logging
        System.out.println(s);
    }
    protected void info(String s) {
        //TODO: change this to app server logging
        System.out.println(s);
    }
    protected void error(String s) {
        //TODO: change this to app server logging
        System.out.println(s);
    }
    
}
