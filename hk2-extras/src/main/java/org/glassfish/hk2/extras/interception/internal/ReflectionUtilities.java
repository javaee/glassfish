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
package org.glassfish.hk2.extras.interception.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;

import org.glassfish.hk2.extras.interception.InterceptionBinder;

/**
 * @author jwells
 *
 */
public class ReflectionUtilities {
    public static  HashSet<String> getAllBindingsFromMethod(Method m) {
        HashSet<String> retVal = getAllBindingsFromClass(m.getDeclaringClass());
        
        Annotation allMethodAnnotations[] = m.getAnnotations();
        for (Annotation aMethodAnnotation : allMethodAnnotations) {
            if (!isBindingAnnotation(aMethodAnnotation)) continue;
            
            getAllBinderAnnotations(aMethodAnnotation, retVal);
        }
        
        return retVal;
    }
    
    public static  HashSet<String> getAllBindingsFromClass(Class<?> c) {
        HashSet<String> retVal = new HashSet<String>();
        
        Annotation allClassAnnotations[] = c.getAnnotations();
        for (Annotation aClassAnnotation : allClassAnnotations) {
            if (!isBindingAnnotation(aClassAnnotation)) continue;
            
            getAllBinderAnnotations(aClassAnnotation, retVal);
        }
        
        return retVal;
    }
    
    private static boolean isBindingAnnotation(Annotation a) {
        return (a.annotationType().getAnnotation(InterceptionBinder.class)) != null;
    }
    
    private static void getAllBinderAnnotations(Annotation a, HashSet<String> retVal) {
        String aName = a.annotationType().getName();
        if (retVal.contains(aName)) return;
        retVal.add(aName);
        
        Annotation subAnnotations[] = a.annotationType().getAnnotations();
        for (Annotation subAnnotation : subAnnotations) {
            if (!isBindingAnnotation(subAnnotation)) continue;
            
            String subName = subAnnotation.annotationType().getName();
            if (retVal.contains(subName)) continue;
            
            getAllBinderAnnotations(subAnnotation, retVal);
        }
    }

}
