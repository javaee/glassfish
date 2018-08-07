/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.jvnet.tiger_types;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.EnumSet;

/**
 * Abstracts away the process of creating a collection (array, {@link List}, etc)
 * of items.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Lister<T> {
    /**
     * Type of the individual item
     */
    public final Class itemType;
    public final Type itemGenericType;

    protected final Collection r;

    protected Lister(Class itemType, Type itemGenericType) {
        this(itemType,itemGenericType,new ArrayList());
    }

    protected Lister(Class itemType, Type itemGenericType, Collection r) {
        this.itemType = itemType;
        this.itemGenericType = itemGenericType;
        this.r = r;
    }

    public void add(Object o) {
        r.add(o);
    }

    public abstract T toCollection();

    /**
     * Creates a {@link Lister} instance that produces the given type.
     */
    public static Lister create(Type t) {
        return create(Types.erasure(t),t);
    }

    /**
     * Creates a {@link Lister} instance that produces the given type.
     *
     * @param c
     *      The erasure version of 't'. This is taken
     *      as a parameter as a performance optimizaiton.
     *
     * @return
     *      null if the given type doesn't look like a collection.
     * @throws IllegalArgumentException
     *      if the given type does look like a collection yet this implementation
     *      is not capable of how to handle it.
     */
    public static <T> Lister<T> create(Class<T> c, Type t) {
        if(c.isArray()) {
            // array
            Class<?> ct = c.getComponentType();
            return new Lister(ct,ct) {
                public Object toCollection() {
                    return r.toArray((Object[])Array.newInstance(itemType,r.size()));
                }
            };
        }
        if(Collection.class.isAssignableFrom(c)) {
            final Type col = Types.getBaseClass(t, Collection.class);

            final Type itemType;
            if (col instanceof ParameterizedType)
                itemType = Types.getTypeArgument(col, 0);
            else
                itemType = Object.class;

            Collection items=null;
            try {
                items = (Collection)c.newInstance();
            } catch (InstantiationException e) {
                // this is not instanciable. Try known instanciable versions.
                for (Class ct : CONCRETE_TYPES) {
                    if(c.isAssignableFrom(ct)) {
                        try {
                            items = (Collection)ct.newInstance();
                            break;
                        } catch (InstantiationException x) {
                            throw toError(x);
                        } catch (IllegalAccessException x) {
                            throw toError(x);
                        }
                    }
                }
                // EnumSet
                if(items==null && c==EnumSet.class) {
                    items = EnumSet.noneOf(Types.erasure(itemType).asSubclass(Enum.class));
                }
                if(items==null)
                    throw new IllegalArgumentException("Don't know how to instanciate "+c);
            } catch (IllegalAccessException e) {
                throw toError(e);
            }

            return new Lister(Types.erasure(itemType),itemType,items) {
                public Object toCollection() {
                    return r;
                }
            };
        }

        return null;
    }

    private static IllegalAccessError toError(IllegalAccessException e) {
        IllegalAccessError x = new IllegalAccessError();
        x.initCause(e);
        return x;
    }

    private static InstantiationError toError(InstantiationException e) {
        InstantiationError x = new InstantiationError();
        x.initCause(e);
        return x;
    }

    private static final Class[] CONCRETE_TYPES = new Class[] {
        ArrayList.class,
        HashSet.class
    };
    public Type getItemGenericType() {
      return this.itemGenericType;
    }

}
