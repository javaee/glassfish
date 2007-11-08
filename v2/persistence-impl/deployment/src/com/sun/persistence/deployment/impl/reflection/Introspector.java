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
 * Introspector.java
 * A lot of code in this class looks like java.beans.Introspector.class.
 * Unfortunately, we can not use that class because Java beans convention is to
 * have accessors methods public where as an entity bean accessors 
 * can be protected as well.
 *
 * Created on February 2, 2005, 7:18 PM
 */


package com.sun.persistence.deployment.impl.reflection;

import com.sun.persistence.api.deployment.AccessType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;

import static java.beans.Introspector.decapitalize;

/**
 * A lot of code in this class looks like java.beans.Introspector.class.
 * Unfortunately, we can not use that class because Java beans convention is to
 * have accessors methods public where as an entity bean accessors can be
 * protected as well.
 *
 * @author Sanjeeb Sahoo
 */
class Introspector {
    private static final String GET_PREFIX = "get"; // NOI18N

    // private static final String SET_PREFIX = "set";
    private static final String IS_PREFIX = "is"; // NOI18N

    private java.lang.Class<?> c;

    public Introspector(java.lang.Class<?> c) {
        this.c = c;
    }

    public FieldOrPropertyImpl[] getProperties(AccessType accessType) {
        if (accessType == AccessType.FIELD) {
            return getFields();
        } else {
            return getGetters();
        }
    }

    public FieldOrPropertyImpl getProperty(
            String propName,
            AccessType accessType) {
        for (FieldOrPropertyImpl p : getProperties(accessType)) {
            if (p.getName().equals(propName)) {
                return p;
            }
        }
        return null;
    }

    private FieldOrPropertyImpl[] getGetters() {
        ArrayList<FieldOrPropertyImpl> result =
                new ArrayList<FieldOrPropertyImpl>();
        Method methodList[] = getDeclaredMethods();
        // Now analyze each method.
        for (int i = 0; i < methodList.length; i++) {
            Method method = methodList[i];
            if (method == null) {
                continue;
            }
            String name = method.getName();
            java.lang.Class argTypes[] = method.getParameterTypes();
            java.lang.Class resultType = method.getReturnType();
            int argCount = argTypes.length;
            FieldOrPropertyImpl pp = null;
            if (argCount == 0) {
                if (name.length() <= 3 && !name.startsWith(IS_PREFIX)) {
                    // Optimization. Don't bother with invalid propertyNames.
                    continue;
                }
                if (name.startsWith(GET_PREFIX)) {
                    // Simple getter
                    pp = new FieldOrPropertyImpl(decapitalize(
                            name.substring(3)),
                            method);
                    result.add(pp);
                } else if (resultType == boolean.class &&
                        name.startsWith(IS_PREFIX)) {
                    // Boolean getter
                    pp = new FieldOrPropertyImpl(decapitalize(
                            name.substring(2)),
                            method);
                    result.add(pp);
                }
            }
        }//for
        return result.toArray(new FieldOrPropertyImpl[0]);
    }

    private FieldOrPropertyImpl[] getFields() {
        ArrayList<FieldOrPropertyImpl> pfs =
                new ArrayList<FieldOrPropertyImpl>();
        Field[] fs =
                AccessController.doPrivileged(new PrivilegedAction<Field[]>() {
                    public Field[] run() {
                        return c.getDeclaredFields();
                    }
                });
        for (Field f : fs) {
            pfs.add(new FieldOrPropertyImpl(f.getName(), f));
        }
        return pfs.toArray(new FieldOrPropertyImpl[0]);
    }

    //returns non-static non-private declared method list.
    private Method[] getDeclaredMethods() {
        // We have to raise privilege for getDeclaredMethods
        Method[] result =
                AccessController.doPrivileged(new PrivilegedAction<Method[]>() {
                    public Method[] run() {
                        return c.getDeclaredMethods();
                    }
                });
        // null out any private or static methods.
        for (int i = 0; i < result.length; i++) {
            Method method = result[i];
            int mods = method.getModifiers();
            if (Modifier.isPrivate(mods) || Modifier.isStatic(mods)) {
                result[i] = null;
            }
        }
        return result;
    }

}
