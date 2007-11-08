/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.enhancer.meta.model;

import java.io.InputStream;

import com.sun.org.apache.jdo.impl.enhancer.util.ResourceLocator;
import com.sun.org.apache.jdo.impl.model.java.reflection.ReflectionJavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOModel;

/**
 * Provides some basic Java type information based on JVM descriptors.
 * 
 * @author Michael Bouschen
 * @author Martin Zaun
 */
public class EnhancerJavaModel
    extends ReflectionJavaModel
{
    /**
     * The "package" jdo file.
     */
    final private ResourceLocator locator;

    /**
     * Creates an instance.
     */
    public EnhancerJavaModel(ClassLoader classLoader,
                             ResourceLocator locator)
    {
        super(classLoader, false, null);
        this.locator = locator;
    }
    
    /**
     * Finds a resource with a given name.  This method returns
     * <code>null</code> if no resource with this name is found.
     * The name of a resource is a "/"-separated path name.
     */
    public InputStream getInputStreamForResource(String resourceName)
    {
        // first try the locator
        InputStream stream = locator.getInputStreamForResource(resourceName);
        if (stream == null)
            // if the locator cannot find the resource try the class loader
            stream = super.getInputStreamForResource(resourceName);
        return stream;
    }

    // ===== Methods not defined in JavaModel =====

    /** 
     * Creates a new JavaType instance for the specified Class object.
     * <p>
     * This implementation returns a EnhancerJavaType instance.
     * @param clazz the Class instance representing the type
     * @return a new JavaType instance
     */
    protected JavaType newJavaTypeInstance(Class clazz)
    {
        return new EnhancerJavaType(clazz, this);
    }

    /** 
     * Returns the fully qualified name of the specified type representation.
     */
    public String getTypeName(String sig)
    {
        // translates a VM type field signature into Java-format signature
        final int n = sig.length();
        affirm(n > 0, "invalid field signature: \"\"");

        // handle arrays
        if (sig.startsWith("["))
            return sig.replace('/','.');

        // parse rest of signature
        final String name;
        final char c = sig.charAt(0);
        switch (c) {
        case 'Z':
            name = "boolean";
            break;
        case 'C':
            name = "char";
            break;
        case 'B':
            name = "byte";
            break;
        case 'S':
            name = "short";
            break;
        case 'I':
            name = "int";
            break;
        case 'F':
            name = "float";
            break;
        case 'J':
            name = "long";
            break;
        case 'D':
            name = "double";
            break;
        case 'L':
            // return reference type with array dimensions
            affirm(sig.indexOf(';') == n - 1,
                   "invalid field signature: " + sig);
            name = sig.substring(1, n - 1);
            affirm(isValidName(name, '/'),
                   "invalid field signature: " + sig);
            return name.replace('/','.');
        default:
            name = "";
            affirm(false, "invalid field signature: " + sig);
        }
        return name;
    }

    static private boolean isValidName(String name, char separator) 
    {
        final int n = name.length();
        if (n == 0) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            return false;
        }
        for (int i = 1; i < n; i++) {
            final char c = name.charAt(i);
            if (!Character.isJavaIdentifierPart(c) && c != separator) {
                return false;
            }
        }
        return true;
    }

    static protected final void affirm(boolean condition, String msg) {
        if (!condition)
            throw new InternalError("assertion failed: " + msg);
    }

}
