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
package org.glassfish.hk2.utilities.reflection.internal;

import java.lang.reflect.Method;

import org.glassfish.hk2.utilities.reflection.MethodWrapper;
import org.glassfish.hk2.utilities.reflection.Pretty;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

/**
 * Wrapper of methods with an equals and hashCode that
 * makes methods hiding other methods be equal
 * 
 * @author jwells
 *
 */
public class MethodWrapperImpl implements MethodWrapper {
    private final Method method;
    private final int hashCode;
    
    public MethodWrapperImpl(Method method) {
        if (method == null) throw new IllegalArgumentException();
        
        this.method = method;
        
        int hashCode = 0;
        
        hashCode ^= method.getName().hashCode();
        hashCode ^= method.getReturnType().hashCode();
        for (Class<?> param : method.getParameterTypes()) {
            hashCode ^= param.hashCode();
        }
        
        this.hashCode = hashCode;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.reflection.MethodWrapper#getMethod()
     */
    @Override
    public Method getMethod() {
        return method;
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof MethodWrapperImpl)) return false;
        
        MethodWrapperImpl other = (MethodWrapperImpl) o;
        
        if (!method.getName().equals(other.method.getName())) return false;
        if (!method.getReturnType().equals(other.method.getReturnType())) return false;
        
        Class<?> myParams[] = method.getParameterTypes();
        Class<?> otherParams[] = other.method.getParameterTypes();
        
        if (myParams.length != otherParams.length) return false;
        
        if (ReflectionHelper.isPrivate(method) || ReflectionHelper.isPrivate(other.method)) return false;
        
        for (int lcv = 0; lcv < myParams.length; lcv++) {
            if (!myParams[lcv].equals(otherParams[lcv])) return false;
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return "MethodWrapperImpl(" + Pretty.method(method) + "," + System.identityHashCode(this) + ")";
    }

}
