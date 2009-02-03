/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.naming.util;

import org.jvnet.hk2.annotations.Service;

import java.io.*;
import java.lang.reflect.Array;

/**
 * This subclass of ObjectInputStream delegates loading of classes to
 * an existing ClassLoader.
 */

@Service
public class ObjectInputStreamWithLoader extends ObjectInputStream {
    protected ClassLoader loader;

    /**
     * Loader must be non-null;
     *
     * @throws IOException              on io error
     * @throws StreamCorruptedException on a corrupted stream
     */

    public ObjectInputStreamWithLoader(InputStream in, ClassLoader loader)
            throws IOException, StreamCorruptedException {

        super(in);
        if (loader == null) {
            throw new IllegalArgumentException("Illegal null argument to ObjectInputStreamWithLoader");
        }
        this.loader = loader;
    }

    /**
     * Make a primitive array class
     */

    private Class primitiveType(char type) {
        switch (type) {
            case 'B':
                return byte.class;
            case 'C':
                return char.class;
            case 'D':
                return double.class;
            case 'F':
                return float.class;
            case 'I':
                return int.class;
            case 'J':
                return long.class;
            case 'S':
                return short.class;
            case 'Z':
                return boolean.class;
            default:
                return null;
        }
    }

    /**
     * Use the given ClassLoader rather than using the system class
     *
     * @throws ClassNotFoundException if class can not be loaded
     */
    protected Class resolveClass(ObjectStreamClass classDesc)
            throws IOException, ClassNotFoundException {

        try {
            String cname = classDesc.getName();
            if (cname.startsWith("[")) {
                // An array
                Class component;        // component class
                int dcount;            // dimension
                for (dcount = 1; cname.charAt(dcount) == '['; dcount++) ;
                if (cname.charAt(dcount) == 'L') {
                    component = loader.loadClass(cname.substring(dcount + 1,
                            cname.length() - 1));
                } else {
                    if (cname.length() != dcount + 1) {
                        throw new ClassNotFoundException(cname);// malformed
                    }
                    component = primitiveType(cname.charAt(dcount));
                }
                int dim[] = new int[dcount];
                for (int i = 0; i < dcount; i++) {
                    dim[i] = 0;
                }
                return Array.newInstance(component, dim).getClass();
            } else {
                return loader.loadClass(cname);
            }
        } catch (ClassNotFoundException e) {
            // Try also the superclass because of primitive types
            return super.resolveClass(classDesc);
        }
    }
}
