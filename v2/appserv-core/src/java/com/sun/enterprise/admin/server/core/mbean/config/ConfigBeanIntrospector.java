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

//JDK imports
import java.beans.*;
import java.lang.reflect.*;

//Logging imports
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

//Our imports
import com.sun.enterprise.config.*;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.admin.util.ArgChecker;

public final class ConfigBeanIntrospector
{
    private static final Logger sLogger = 
        LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

    public static Object instantiate(Class bean, Object[] params) 
        throws Exception
    {
        ArgChecker.checkValid(bean, "bean");        //NOI18N
        ArgChecker.checkValid(params, "params");    //NOI18N

        Class[] paramTypes = new Class[params.length];
        for (int i = 0; i < params.length; i++)
        {
            paramTypes[i] = params[i].getClass();
        }
        Constructor ctor = bean.getConstructor(paramTypes);
        Object inst = ctor.newInstance(params);
        return inst;
    }

    public static boolean isAttributeSupported(Class    beanClass, 
                                               String   attributeName)
    {
        boolean isSupported = false;
        try
        {
            PropertyDescriptor pd = getPropertyDescriptor(beanClass, 
                                                          attributeName);
            isSupported = (pd != null);
        }
        catch (Exception e)
        {
            sLogger.throwing(ConfigBeanIntrospector.class.getName(), 
                    "isAttributeSupported", e);
            isSupported = false;
        }
        return isSupported;
    }

    public static boolean isAttributeReadable(Class     beanClass,
                                              String    attributeName)
    {
        boolean isReadable = false;
        try
        {
            Method m = getGetter(beanClass, attributeName);
            isReadable = (m != null);
        }
        catch (Exception e)
        {
            sLogger.throwing(ConfigBeanIntrospector.class.getName(), 
                    "isAttributeReadable", e);
            isReadable = false;
        }
        return isReadable;
    }

    public static boolean isAttributeWritable(Class     beanClass,
                                              String    attributeName)
    {
        boolean isWritable = false;
        try
        {
            Method m = getSetter(beanClass, attributeName);
            isWritable = (m != null);
        }
        catch (Exception e)
        {
            sLogger.throwing(ConfigBeanIntrospector.class.getName(), 
                    "isAttributeWritable", e);
            isWritable = false;
        }
        return isWritable;
    }

    public static Object invokeGetter(Object target, String attributeName) 
        throws Exception
    {
        ArgChecker.checkValid(target, "target"); //NOI18N
        ArgChecker.checkValid(attributeName, "attributeName"); //NOI18N

        Object ret = null;
        Class beanClass = target.getClass();
        Method getter = getGetter(beanClass, attributeName);
        if (getter != null)
        {
            ret = getter.invoke(target, (Object[])null);
        }
        return ret;
    }

    public static void invokeSetter(Object  target, 
                                    String  attributeName, 
                                    Object  value) 
        throws Exception
    {
        ArgChecker.checkValid(target, "target"); //NOI18N
        ArgChecker.checkValid(attributeName, "attributeName"); //NOI18N

        Class beanClass = target.getClass();
        Method setter = getSetter(beanClass, attributeName);
        if (setter != null)
        {
            setter.invoke(target, new Object[] {value});
        }
    }

    private static Method getGetter(Class beanClass, String attributeName)
        throws Exception
    {
        Method m = null;
        PropertyDescriptor pd = getPropertyDescriptor(beanClass, attributeName);
        if (pd != null)
        {
            m = pd.getReadMethod();
        }
        return m;
    }

    private static Method getSetter(Class beanClass, String attributeName)
        throws Exception
    {
        Method m = null;
        PropertyDescriptor pd = getPropertyDescriptor(beanClass, attributeName);
        if (pd != null)
        {
            m = pd.getWriteMethod();
        }
        return m;
    }

    private static PropertyDescriptor getPropertyDescriptor(Class beanClass,
                                            String attributeName)
        throws Exception
    {
        PropertyDescriptor descriptor = null;
        BeanInfo javaBeanInfo = Introspector.getBeanInfo(beanClass);
        PropertyDescriptor[] pds = javaBeanInfo.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++)
        {
            PropertyDescriptor pd = pds[i];
            if (pd.getName().equals(attributeName))
            {
                descriptor = pd;
                break;
            }
        }
        return descriptor;
    }

    public static void main(String[] args) throws Exception
    {
        ConfigContext ctx = ConfigFactory.createConfigContext(
                "/u/ramakant/server.xml", true);
        Object obj = null;
//        Server baseBean = (Server) ctx.getRootConfigBean();
        Config          config  = (Config)ConfigBeansFactory.getConfigBeanByXPath(ctx, ServerXPathHelper.XPATH_CONFIG);
            
        IiopService iiopService = config.getIiopService();
        obj = iiopService.getIiopListenerById("orb-listener-1");

        //Getters
        Object value = ConfigBeanIntrospector.invokeGetter(obj, "id");
        sLogger.info(value.toString());
        try
        {
            ConfigBeanIntrospector.invokeGetter(obj, "abcd");
        }
        catch (Exception e)
        {
            sLogger.info("OK " + e.getMessage());
        }
        //Setters
        ConfigBeanIntrospector.invokeSetter(obj, "id", "surya10");
        ConfigBeanIntrospector.invokeSetter(obj, "port", 
                new Integer(8888).toString());

        ctx.flush(true);
    }
}
