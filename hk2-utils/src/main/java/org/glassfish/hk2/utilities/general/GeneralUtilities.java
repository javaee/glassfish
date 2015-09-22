/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.utilities.general;

import java.lang.reflect.Array;

import org.glassfish.hk2.utilities.general.internal.WeakHashClockImpl;
import org.glassfish.hk2.utilities.general.internal.WeakHashLRUImpl;

/**
 * This class contains utilities useful for any code
 * 
 * @author jwells
 *
 */
public class GeneralUtilities {
    /**
     * Returns true if a is equals to b, or both
     * and and b are null.  Is safe even if
     * a or b is null.  If a or b is null but
     * the other is not null, this returns false
     * 
     * @param a A possibly null object to compare
     * @param b A possibly null object to compare
     * @return true if equal, false if not
     */
    public static boolean safeEquals(Object a, Object b) {
        if (a == b) return true;
        if (a == null) return false;
        if (b == null) return false;
        
        return a.equals(b);
    }
    
    private static Class<?> loadArrayClass(ClassLoader cl, String aName) {
        Class<?> componentType = null;
        int[] dimensions = null;
        
        int dot = 0;
        while (componentType == null) {
            char dotChar = aName.charAt(dot);
            if (dotChar == '[') {
                dot++;
                continue;
            }
            
            dimensions = new int[dot];
            for (int lcv = 0; lcv < dot; lcv++) {
                dimensions[lcv] = 0;
            }
            
            if (dotChar == 'B') {
                componentType = byte.class;
            }
            else if (dotChar == 'I') {
                componentType = int.class;
            }
            else if (dotChar == 'J') {
                componentType = long.class;
            }
            else if (dotChar == 'Z') {
                componentType = boolean.class;
            }
            else if (dotChar == 'S') {
                componentType = short.class;
            }
            else if (dotChar == 'C') {
                componentType = char.class;
            }
            else if (dotChar == 'D') {
                componentType = double.class;
            }
            else if (dotChar == 'F') {
                componentType = float.class;
            }
            else {
                if (dotChar != 'L') {
                    throw new IllegalArgumentException("Unknown array type " + aName);
                }
                
                if (aName.charAt(aName.length() - 1) != ';') {
                    throw new IllegalArgumentException("Badly formed L array expresion: " + aName);
                }
                
                String cName = aName.substring(dot + 1, aName.length() - 1);
                
                componentType = loadClass(cl, cName);
                if (componentType == null) return null;
            }
        }
        
        Object retArray = Array.newInstance(componentType, dimensions);
        return retArray.getClass();
    }
    
    /**
     * Loads the class from the given classloader or returns null (does not throw).
     * Property handles array classes as well
     * 
     * @param cl The non-null classloader to load the class from
     * @param cName The fully qualified non-null name of the class to load
     * @return The class if it could be loaded from the classloader, or
     * null if it could not be found for any reason
     */
    public static Class<?> loadClass(ClassLoader cl, String cName) {
        if (cName.startsWith("[")) {
            return loadArrayClass(cl, cName);
        }
        
        try {
            return cl.loadClass(cName);
        }
        catch (Throwable th) {
            return null;
        }
    }
    
    /**
     * Creates a weak hash clock
     * @return A weak hash clock implementation
     */
    public static <K,V> WeakHashClock<K,V> getWeakHashClock() {
        return new WeakHashClockImpl<K,V>();
    }
    
    /**
     * Creates a weak hash clock
     * @return A weak hash clock implementation
     */
    public static <K> WeakHashLRU<K> getWeakHashLRU() {
        return new WeakHashLRUImpl<K>();
    }

}
