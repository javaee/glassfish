/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.examples.caching.aop;

/**
 * This is an immutable object that can be used
 * as a key in our method and constructor caches
 * 
 * @author jwells
 *
 */
public class CacheKey {
    private final String className;
    private final String methodName;
    private final Object input;
    
    /**
     * None of these values can be null
     * 
     * @param className The name of the class
     * @param methodName The name of the method or &lt;init&gt; for a constructor
     * @param input The input parameter of the constructor or method
     */
    public CacheKey(String className, String methodName, Object input) {
        this.className = className;
        this.methodName = methodName;
        this.input = input;
    }
    
    @Override
    public int hashCode() {
        return className.hashCode() ^ methodName.hashCode() ^ input.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof CacheKey)) return false;
        CacheKey other = (CacheKey) o;
        
        if (!other.className.equals(className)) return false;
        if (!other.methodName.equals(methodName)) return false;
        return other.input.equals(input);
    }

}
