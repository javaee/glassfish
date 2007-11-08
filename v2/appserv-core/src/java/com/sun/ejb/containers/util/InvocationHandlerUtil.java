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
package com.sun.ejb.containers.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;

import javax.ejb.EJBException;

public final class InvocationHandlerUtil {

    InvocationHandlerUtil() {}

    public static Object invokeJavaObjectMethod(InvocationHandler handler, 
                                                Method method, Object[] args) 
        throws EJBException {

        Object returnValue = null;

        // Can only be one of : 
        //     boolean java.lang.Object.equals(Object)
        //     int     java.lang.Object.hashCode()
        //     String  java.lang.Object.toString()
        //
        // Optimize by comparing as few characters as possible.

        switch( method.getName().charAt(0) ) {
            case 'e' :
                Object other = Proxy.isProxyClass(args[0].getClass()) ?
                    Proxy.getInvocationHandler(args[0]) : args[0];
                returnValue = new Boolean(handler.equals(other));
                break;
            case 'h' :
                returnValue = new Integer(handler.hashCode());
                break;
            case 't' :
                returnValue = handler.toString();
                break;
            default :
                throw new EJBException(method.getName());
        }

        return returnValue;
    }

    public static Throwable handleInvocationException(Throwable invException) {

        Throwable toThrow = invException;
        
        if (invException instanceof java.lang.RuntimeException) {
            toThrow = invException;
        } else if (invException instanceof Exception) {
            toThrow = invException;
        } else {
            toThrow = new EJBException(invException.getMessage());
        }

        return toThrow;
    }
}
