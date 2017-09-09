/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.pbuf.internal;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.hk2.pbuf.api.annotations.OneOf;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.MethodWrapper;
import org.glassfish.hk2.utilities.reflection.internal.ClassReflectionHelperImpl;

/**
 * @author jwells
 *
 */
public class PBUtilities {
    private static final String GET = "get";
    private static final String IS = "is";
    private static final String SET = "set";
    
    private static final ClassReflectionHelper reflectionHelper = new ClassReflectionHelperImpl();
    
    public static String getOneOf(Class<?> oInterface, String methodName, Class<?> type) {
        Set<Method> allNamed = getAllMethodsWithName(oInterface, methodName);
        if (allNamed.isEmpty()) {
            throw new AssertionError("Could not find method " + methodName + " on bean " + oInterface.getName());
        }
        
        if (allNamed.size() == 1) {
            // An optimistic optimization
            Method method = allNamed.iterator().next();
            
            OneOf oneOf = method.getAnnotation(OneOf.class);
            if (oneOf == null) {
                return null;
            }
            
            return oneOf.value();
        }
        
        // Need to do this the difficult way
        Method found = null;
        if (isGetter(methodName)) {
            for (Method m : allNamed) {
                Class<?> retType = m.getReturnType();
                
                if (!retType.equals(type)) {
                    continue;
                }
                
                Class<?> allParams[] = m.getParameterTypes();
                if (allParams == null || allParams.length == 0) {
                    found = m;
                    break;
                }
            }
            
        }
        else if (isSetter(methodName)) {
            for (Method m : allNamed) {
                Class<?> retType = m.getReturnType();
                if (!void.class.equals(retType)) {
                    continue;
                }
                
                Class<?> allParams[] = m.getParameterTypes();
                if (allParams == null) {
                    continue;
                }
                if (allParams.length != 1) {
                    continue;
                }
                
                if (allParams[0].equals(type)) {
                    found = m;
                    break;
                }
            }
        }
        else {
            throw new AssertionError("Unable to analyze a method that is neiter a getter or a setter: " + methodName + " on " + oInterface.getName());
        }
        
        if (found == null) {
            throw new AssertionError("Could not find method " + methodName + " on bean " + oInterface.getName() + " with type " + type.getName());
            
        }
        
        OneOf oneOf = found.getAnnotation(OneOf.class);
        if (oneOf == null) {
            return null;
        }
        
        return oneOf.value();
    }
    
    private static boolean isGetter(String methodName) {
        if (methodName.startsWith(GET) && (methodName.length() > 3)) {
            return true;
        }
        if (methodName.startsWith(IS) && (methodName.length() > 2)) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isSetter(String methodName) {
        if (methodName.startsWith(SET) && (methodName.length() > 3)) {
            return true;
        }
        
        return false;
    }
    
    private static Set<Method> getAllMethodsWithName(Class<?> oInterface, String methodName) {
        HashSet<Method> retVal = new HashSet<Method>();
        
        Set<MethodWrapper> allMethods = reflectionHelper.getAllMethods(oInterface);
        
        for (MethodWrapper wrapper : allMethods) {
            if (methodName.equals(wrapper.getMethod().getName())) {
                retVal.add(wrapper.getMethod());
            }
        }
        
        return retVal;
    }
    
    public static String camelCaseToUnderscore(String camelCase) {
        StringBuffer sb = new StringBuffer();
        
        char oneBackCache = 0;
        boolean firstAlreadyWritten = false;
        boolean previousLowerCase = false;
        for (int lcv = 0; lcv < camelCase.length(); lcv++) {
            char charAt = camelCase.charAt(lcv);
            if (Character.isUpperCase(charAt)) {
                charAt = Character.toLowerCase(charAt);
                if (oneBackCache != 0) {
                    if (firstAlreadyWritten && previousLowerCase) {
                        sb.append("_");
                    }
                    
                    sb.append(oneBackCache);
                    firstAlreadyWritten = true;
                    previousLowerCase = false;
                }
                
                oneBackCache = charAt;
            }
            else {
                if (oneBackCache != 0) {
                    if (firstAlreadyWritten) {
                        sb.append("_");
                        firstAlreadyWritten = true;
                    }
                    sb.append(oneBackCache);
                    firstAlreadyWritten = true;
                    
                    oneBackCache = 0;
                }
                
                sb.append(charAt);
                firstAlreadyWritten = true;
                
                previousLowerCase = true;
            }
        }
        
        if (oneBackCache != 0) {
            if (firstAlreadyWritten && previousLowerCase) {
                sb.append("_");
            }
            
            sb.append(oneBackCache);
        }
        
        return sb.toString();
    }

}
