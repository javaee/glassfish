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
 * PropertyUtils.java
 *
 * Created on March 18, 2001, 12:30 PM
 */

package com.sun.enterprise.tools.common;

import java.lang.reflect.Method;
import java.beans.PropertyDescriptor;

import com.sun.enterprise.tools.common.util.diagnostics.Reporter;
import com.sun.enterprise.tools.common.util.diagnostics.StackTrace;

/**
 *
 * @author  vkraemer
 * @version 
 */
public class PropertyUtils {

    /** Creates new PropertyUtils */
    private PropertyUtils() {
    }

    public static Method getWriter(Object target, String destFieldName) throws java.beans.IntrospectionException {
        try {
            PropertyDescriptor destPd = new PropertyDescriptor(destFieldName, target.getClass());
            return destPd.getWriteMethod();
        }
        catch (java.beans.IntrospectionException t) {
            Reporter.critical(new StackTrace(t)); //NOI18N
            throw t;
        }
    }
    
        public static Method getReader(Object target, String destFieldName) throws java.beans.IntrospectionException {
        try {
            PropertyDescriptor destPd = new PropertyDescriptor(destFieldName, target.getClass());
            return destPd.getReadMethod();
        }
        catch (java.beans.IntrospectionException t) {
            //Reporter.critical(new StackTrace(t)); //NOI18N
            //throw t;
            return getReader2(target,destFieldName);
        }
    }

        public static Method getReader2(Object target, String destFieldName) throws java.beans.IntrospectionException {
            String getterName = createGetterName(destFieldName);
            Class targetClass = null;
        try {
            targetClass = target.getClass();
            Method reader = targetClass.getMethod(getterName,null);
            return reader;
            //PropertyDescriptor destPd = new PropertyDescriptor(destFieldName, target.getClass());
            //return destPd.getReadMethod();
        }
        catch (Throwable t) {
            Method[] allmethods = targetClass.getMethods();
            for (int i = 0; null != allmethods && i < allmethods.length; i++)
                Reporter.info(allmethods[i].getReturnType() + " " +allmethods[i].getName());//NOI18N
            Reporter.critical(new StackTrace(t)); //NOI18N
            throw new java.beans.IntrospectionException(getterName);
            //return getReader2(target,destFieldName);
        }
        }
        
        static private String createGetterName(String propName) {
            String retVal = "get";//NOI18N
            String capitalizedProp = propName.toUpperCase();
            return retVal + capitalizedProp.substring(0,1) + propName.substring(1);
        }
    }

