/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.classmodel.reflect.impl;

import org.glassfish.hk2.classmodel.reflect.*;
import org.glassfish.hk2.classmodel.reflect.util.ParsingConfig;

import java.util.*;
import java.util.logging.Logger;

/**
 * Implementation of an extensible type (Class or Interface)
 */
public abstract class ExtensibleTypeImpl<T extends ExtensibleType> extends TypeImpl implements ExtensibleType<T> {

    private TypeProxy<?> parent;
    private final List<FieldModel> staticFields = new ArrayList<FieldModel> ();
    private final List<TypeProxy<InterfaceModel>> implementedIntf = new ArrayList<TypeProxy<InterfaceModel>>();
    private final List<ParameterizedInterfaceModel> implementedParameterizedIntf =
            new ArrayList<ParameterizedInterfaceModel>();
    
    public ExtensibleTypeImpl(String name, TypeProxy<Type> sink, TypeProxy parent) {
        super(name, sink);
        this.parent =  parent;
    }

    public T getParent() {
        if (parent!=null) {
            return (T) parent.get();
        } else {
            return null;
        }
    }
    
    public synchronized TypeProxy<?> setParent(final TypeProxy<?> parent) {
        if (null == this.parent) { 
          this.parent = parent;
        }
        return this.parent;
    }

    synchronized void isImplementing(TypeProxy<InterfaceModel> intf) {
        implementedIntf.add(intf);
    }

    synchronized void isImplementing(ParameterizedInterfaceModelImpl pim) {
        implementedIntf.add(pim.rawInterface);
        implementedParameterizedIntf.add(pim);
    }

    @Override
    public Collection<InterfaceModel> getInterfaces() {
        return TypeProxy.adapter(Collections.unmodifiableCollection(implementedIntf));
    }

    @Override
    public Collection<ParameterizedInterfaceModel> getParameterizedInterfaces() {
        return Collections.unmodifiableCollection(implementedParameterizedIntf);
    }

    @Override
    public Collection<T> subTypes() {
        List<T> subTypes = new ArrayList<T>();
        for (Type t : getProxy().getSubTypeRefs()) {
            subTypes.add((T) t);
        }
        return subTypes;
    }

    @Override
    public Collection<T> allSubTypes() {
        Collection<T> allTypes = subTypes();
        for (T child : subTypes()) {
            allTypes.addAll(child.allSubTypes());
        }
        return allTypes;
    }

    synchronized void addStaticField(FieldModel field) {
        staticFields.add(field);
    }

    void addField(FieldModel field) {
        throw new RuntimeException("Cannot add a field to a non classmodel type");
    }

    @Override
    public Collection<FieldModel> getStaticFields() {
        return Collections.unmodifiableCollection(staticFields);
    }

    /**
     * prints a meaningful string
     * @param sb the string buffer to write to.
     */
    @Override
    protected void print(StringBuffer sb) {
        super.print(sb);
        sb.append(", parent=").append(parent==null?"null":parent.getName());
        sb.append(", interfaces=[");
        for (TypeProxy<InterfaceModel> im : implementedIntf) {
            sb.append(" ").append(im.getName());
        }
        sb.append("]");
        
    }
}
