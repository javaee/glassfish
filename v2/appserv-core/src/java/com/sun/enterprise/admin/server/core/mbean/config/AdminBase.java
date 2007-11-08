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

//JMX imports
import javax.management.DynamicMBean;
import javax.management.AttributeList;
import javax.management.MBeanInfo;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InvalidAttributeValueException;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;
/* New for 8.0 */
import com.sun.enterprise.admin.server.core.jmx.Introspector;
import java.lang.reflect.Method;
/* New for 8.0 */

/**
    The base class for all the MBeans. Note that there will be no MBean that
	is registered in the MBeanServer for this class. It serves as the base class
	of all concrete implementations of MBeans.
 
*/

public abstract class AdminBase implements DynamicMBean
{
	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( AdminBase.class );

    protected AdminBase() {
    }

    public Object getAttribute(String attributeName) throws
        AttributeNotFoundException, MBeanException, ReflectionException {
	String msg = localStrings.getString( "admin.server.core.mbean.config.getattribute_not_implemented" );
        throw new UnsupportedOperationException( msg );
    }

    public AttributeList getAttributes(String[] attributeNames) {
	String msg = localStrings.getString( "admin.server.core.mbean.config.getattribute_not_implemented" );
        throw new UnsupportedOperationException( msg );
    }

    public MBeanInfo getMBeanInfo() {
        String msg = localStrings.getString( "admin.server.core.mbean.config.getmbeaninfo_not_implemented" );
        throw new UnsupportedOperationException( msg );
    }

    /** Abstract method that subclasses have to implement. This is the way for
     * invoke method to work, through reflection.
    */
    protected abstract Class getImplementingClass();

    /** Reflection requires the implementing object. */
    protected abstract Object getImplementingMBean();   

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
    /* New for 8.0 */
        final Class implClass = getImplementingClass();
        final Object mbeanReference = getImplementingMBean();
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
                    String msg = localStrings.getString( "admin.server.core.jmx.error_from_mbean", t.getMessage() );
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
    /* New for 8.0 */
    }

    public void setAttribute(Attribute attribute) throws
        AttributeNotFoundException, InvalidAttributeValueException,
        MBeanException, ReflectionException {

        String msg = localStrings.getString( "admin.server.core.mbean.config.setattribute_not_implemented" );
        throw new UnsupportedOperationException( msg );
    }

    public AttributeList setAttributes(AttributeList parm1) {
	String msg = localStrings.getString( "admin.server.core.mbean.config.setattributes_not_implemented" );
        throw new UnsupportedOperationException( msg );
    }
}
