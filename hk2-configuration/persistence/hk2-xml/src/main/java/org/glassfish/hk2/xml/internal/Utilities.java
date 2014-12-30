/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.hk2.xml.internal;

import java.beans.Introspector;
import java.lang.reflect.Method;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author jwells
 *
 */
public class Utilities {

    /* package */ static String isGetter(Method method) {
        String name = method.getName();
        
        if (name.startsWith(JAUtilities.GET)) {
            if (name.length() <= JAUtilities.GET.length()) return null;
            if (method.getParameterTypes().length != 0) return null;
            if (void.class.equals(method.getReturnType())) return null;
            
            String variableName = name.substring(JAUtilities.GET.length());
            
            return Introspector.decapitalize(variableName);
        }
        
        if (name.startsWith(JAUtilities.IS)) {
            if (name.length() <= JAUtilities.IS.length()) return null;
            if (method.getParameterTypes().length != 0) return null;
            if (boolean.class.equals(method.getReturnType()) || Boolean.class.equals(method.getReturnType())) {
                String variableName = name.substring(JAUtilities.IS.length());
                
                return Introspector.decapitalize(variableName);
            }
            
            return null;
        }
        
        return null;
    }
    
    /* package */ static String isSetter(Method method) {
        String name = method.getName();
        
        if (name.startsWith(JAUtilities.SET)) {
            if (name.length() <= JAUtilities.SET.length()) return null;
            if (method.getParameterTypes().length != 1) return null;
            if (void.class.equals(method.getReturnType())) {
                String variableName = name.substring(JAUtilities.SET.length());
                
                return Introspector.decapitalize(variableName);
            }
            
            return null;
        }
        
        return null;
    }
    
    /* package */ static String isLookup(Method method) {
        String name = method.getName();
        
        if (!name.startsWith(JAUtilities.LOOKUP)) return null;
        
        if (name.length() <= JAUtilities.LOOKUP.length()) return null;
        Class<?> parameterTypes[] = method.getParameterTypes();
        if (parameterTypes.length != 1) return null;
        if (!String.class.equals(parameterTypes[0])) return null;
            
        if (method.getReturnType() == null || void.class.equals(method.getReturnType())) return null;
            
        String variableName = name.substring(JAUtilities.LOOKUP.length());
                
        return Introspector.decapitalize(variableName);
    }
    
    /* package */ static String getRootElementName(Class<?> clazz) {
        XmlRootElement root = clazz.getAnnotation(XmlRootElement.class);
        if (root == null) throw new AssertionError("XmlRootElement not available on " + clazz.getName());
        
        return convertXmlRootElementName(root, clazz);
    }
    
    /* package */ static String convertXmlRootElementName(XmlRootElement root, Class<?> clazz) {
        if (!"##default".equals(root.name())) return root.name();
        
        String simpleName = clazz.getSimpleName();
        
        char asChars[] = simpleName.toCharArray();
        StringBuffer sb = new StringBuffer();
        
        boolean firstChar = true;
        boolean lastCharWasCapital = false;
        for (char asChar : asChars) {
            if (firstChar) {
                firstChar = false;
                if (Character.isUpperCase(asChar)) {
                    lastCharWasCapital = true;
                    sb.append(Character.toLowerCase(asChar));
                }
                else {
                    lastCharWasCapital = false;
                    sb.append(asChar);
                }
            }
            else {
                if (Character.isUpperCase(asChar)) {
                    if (!lastCharWasCapital) {
                        sb.append('-');
                    }
                    
                    sb.append(Character.toLowerCase(asChar));
                    
                    lastCharWasCapital = true;
                }
                else {
                    sb.append(asChar);
                    
                    lastCharWasCapital = false;
                }
            }
        }
        
        return sb.toString();
    }

}
