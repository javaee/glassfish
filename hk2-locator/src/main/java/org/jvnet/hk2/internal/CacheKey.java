/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.reflection.Pretty;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

/**
 * This is the cache key, which encapsulates very specific lookup queries.
 * The point of this is to be useable as the key in a hash map, so that
 * equals and hashCode must work properly
 *
 * @author jwells
 *
 */
public class CacheKey {
    private final String removalName;
    private final Type lookupType;
    private final String name;
    private final Annotation qualifiers[];

    /** Pre-calculated in order to improve hashMap lookups */
    private final int hashCode;

    /**
     * Key used for LRU cache
     *
     * @param lookupType The type in the lookup call
     * @param name The name in the lookup call
     * @param qualifiers The set of qualifiers being looked up
     */
    public CacheKey(Type lookupType, String name, Annotation... qualifiers) {
        this.lookupType = lookupType;
        
        Class<?> rawClass = ReflectionHelper.getRawClass(lookupType);
        if (rawClass != null) {
            removalName = rawClass.getName();
        }
        else {
            removalName = null;
        }
        
        this.name = name;
        if (qualifiers.length > 0) {
            this.qualifiers = qualifiers;
        }
        else {
            this.qualifiers = null;
        }

        int retVal = 0;

        if (lookupType != null) {
            retVal ^= lookupType.hashCode();
        }

        if (name != null) {
            retVal ^= name.hashCode();
        }

        for (Annotation anno : qualifiers) {
            retVal ^= anno.hashCode();
        }

        hashCode = retVal;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof CacheKey)) return false;

        final CacheKey other = (CacheKey) o;

        if (hashCode != other.hashCode) return false;
        if (!GeneralUtilities.safeEquals(lookupType, other.lookupType)) return false;
        if (!GeneralUtilities.safeEquals(name, other.name)) return false;

        if (qualifiers != null) {
            if (other.qualifiers == null) return false;

            if (qualifiers.length != other.qualifiers.length) return false;

            boolean isEqual = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

                @Override
                public Boolean run() {
                    for (int lcv = 0; lcv < qualifiers.length; lcv++) {
                        if (!GeneralUtilities.safeEquals(qualifiers[lcv], other.qualifiers[lcv])) return false;
                    }

                    return true;
                }

            });

            if (!isEqual) return false;

        }
        else if (other.qualifiers != null) return false;

        return true;
    }
    
    /**
     * Used when bulk removing a contract that has
     * been removed from the system
     * 
     * @param name The name of the contract that
     * has been removed from the system
     * @return true if this CacheKey is associated
     * with the name contract, and should thus
     * be removed
     */
    public boolean matchesRemovalName(String name) {
        if (removalName == null) return false;
        if (name == null) return false;
        
        return removalName.equals(name);
    }
    
    public String toString() {
        return "CacheKey(" + Pretty.type(lookupType) + "," + name + "," +
            ((qualifiers == null) ? 0 : qualifiers.length) + "," +
                System.identityHashCode(this) + "," + hashCode + ")";
    }

}
