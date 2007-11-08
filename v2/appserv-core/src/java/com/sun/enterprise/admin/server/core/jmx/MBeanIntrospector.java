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

package com.sun.enterprise.admin.server.core.jmx;

//JDK imports
import java.lang.reflect.*;

//JMX imports
import javax.management.NotCompliantMBeanException;

/**
        Class which has convinience routines to introspect an MBean. The
	parameter passed in constructor should be an MBean, otherwise it
	throws an exception. Provides routines to check whether the MBean
	is JMX compliant.
*/

public class MBeanIntrospector extends Introspector
{
    private boolean mIsStandard;
    private boolean mIsDynamic;
    private Class   mMBeanInterfaceClass;

    /** 
        Creates new MBeanIntrospector 
    */
    public MBeanIntrospector(Class c) throws NotCompliantMBeanException
    {
        super(c);
        checkBasic(c);
        checkMBeanType(c);
        checkCompliance();
    }

    public boolean isStandardMBean()
    {
        return mIsStandard;
    }

    public boolean isDynamicMBean()
    {
        return mIsDynamic;
    }

    public Class getMBeanInterfaceClass()
    {
        return mMBeanInterfaceClass;
    }

    public boolean isSupported(String methodName, Class[] signature)
    {
        boolean isSupported = false;
        try
        {
            Method m = mMBeanInterfaceClass.getMethod(methodName, signature);
            isSupported = (m != null);
        }
        catch (Exception e)
        {
        }
        return isSupported;
    }

    private void checkBasic(Class c) throws NotCompliantMBeanException
    {
        if (!isInstantiableJavaClass(c))
        {
            throw new NotCompliantMBeanException();
        }
    }

    private void checkMBeanType(Class c)
    {
        /*
         * StandardMBean Checks :-
         * 1. It is neither primitive type, nor an interface nor an 
         * abstract class.
         * 2. Implements <className>MBean interface. If not, 
         * 3. A nearest superclass (can omit java.lang.Object) is a 
         * standard mbean.
         *
         * DynamicMBean Checks :-
         * 1. It is neither primitive type, nor an interface nor an 
         * abstract class.
         * 2. It implements DynamicMBean interface. If not,
         * 3. Any of its superclasses implements DynamicMBean interface.
         */

        boolean isStandard  = isStandard(c);
        boolean isDynamic   = isDynamic(c);
        if (!(isStandard || isDynamic)) // (A U B)' = A' ^ B'
        {
            Class superClass = c.getSuperclass();
            //Can omit java.lang.Object.
            if ((superClass != null) && 
                (superClass != java.lang.Object.class))
            {
                checkMBeanType(superClass);
            }
        }
        /**
         * Need to check both conditions even though it is either or.
         * A class that is both standard &  dynamic is not compliant
         * and will be raised in checkCompliance()
         */
        if (isStandard)
        {
            mIsStandard = true;
            setStandardMBeanInterface(c);
        }
        if (isDynamic)
        {
            mIsDynamic = true;
            setDynamicMBeanInterface();
        }
    }

    private void checkCompliance() throws NotCompliantMBeanException
    {
        if (!(mIsStandard || mIsDynamic) || // (A U B)' = A' ^ B'
            (mIsStandard && mIsDynamic))
        {
            throw new NotCompliantMBeanException();
        }
    }

    private void setStandardMBeanInterface(Class c)
    {
        String className = deriveStandardMBeanIntfClassName(c);
        mMBeanInterfaceClass = getImplementedMBeanClass(c, className);
    }

    private void setDynamicMBeanInterface()
    {
        mMBeanInterfaceClass = javax.management.DynamicMBean.class;
    }

    private boolean isStandard(Class c)
    {
        boolean isStandard = false;
        /**
         * Note that mbean impl & mbean interface can be in different
         * packages.
         */
        String  className = deriveStandardMBeanIntfClassName(c);
        if (getImplementedMBeanClass(c, className) != null)
        {
            isStandard = true;
        }
        return isStandard;
    }

    private boolean isDynamic(Class c)
    {
        boolean isDynamic = false;
        Class[] interfaces = c.getInterfaces();
        /* No need to check for null as c.getInterfaces() javadoc claims to
         * return an array of 0 length if the class does not implement any
         * interfaces or if the class is a primitive type.
         */
        int length = interfaces.length;
        for (int i = 0; i < length; i++)
        {
            if (interfaces[i] == javax.management.DynamicMBean.class)
            {
                isDynamic = true;
                break;
            }
        }
        return isDynamic;
    }

    private boolean isInstantiableJavaClass(Class c)
    {
        boolean isInstantiable = false;
        if (!c.isPrimitive() && !c.isArray())
        {
            int modifiers = c.getModifiers();
            boolean isInterface = Modifier.isInterface(modifiers);
            boolean isAbstract  = Modifier.isAbstract(modifiers);
            isInstantiable = !(isInterface || isAbstract);
        }
        return isInstantiable;
    }

    /**
     * If class name is foo.bar, this method returns barMBean
     */
    private String deriveStandardMBeanIntfClassName(Class c)
    {
        String className = truncateClassName(c);
        return (className + "MBean");
    }

    /**
     * If class name is foo.bar, this method returns bar
     */
    private String truncateClassName(Class c)
    {
        String className = c.getName();
        int lastDot = className.lastIndexOf('.');
        className = className.substring(lastDot + 1);
        return className;
    }
    
    private Class getImplementedMBeanClass(Class c, String intfName)
    {
        Class implementedMBean = null;
        Class[] interfaces = c.getInterfaces();
        /* No need to check for null as c.getInterfaces() javadoc claims to
         * return an array of 0 length if the class does not implement any
         * interfaces or if the class is a primitive type.
         */
        for (int i = 0; i < interfaces.length; i++)
        {
            String className = truncateClassName(interfaces[i]);
            if (className.equals(intfName))
            {
                implementedMBean = interfaces[i];
                break;
            }
        }
        return implementedMBean;
    }

    private final String convertToCamelCase(String str)
    {
        String camelCase = "";
        char c = str.charAt(0);
        camelCase += Character.toUpperCase(c);
        if (str.length() > 1)
        {
            camelCase += str.substring(1);
        }
        return camelCase;
    }
}
