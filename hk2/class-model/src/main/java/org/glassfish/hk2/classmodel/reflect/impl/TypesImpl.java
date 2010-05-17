/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *  Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 *  The contents of this file are subject to the terms of either the GNU
 *  General Public License Version 2 only ("GPL") or the Common Development
 *  and Distribution License("CDDL") (collectively, the "License").  You
 *  may not use this file except in compliance with the License. You can obtain
 *  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 *  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 *  language governing permissions and limitations under the License.
 *
 *  When distributing the software, include this License Header Notice in each
 *  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *  Sun designates this particular file as subject to the "Classpath" exception
 *  as provided by Sun in the GPL Version 2 section of the License file that
 *  accompanied this code.  If applicable, add the following below the License
 *  Header, with the fields enclosed by brackets [] replaced by your own
 *  identifying information: "Portions Copyrighted [year]
 *  [name of copyright owner]"
 *
 *  Contributor(s):
 *
 *  If you wish your version of this file to be governed by only the CDDL or
 *  only the GPL Version 2, indicate your decision by adding "[Contributor]
 *  elects to include this software in this distribution under the [CDDL or GPL
 *  Version 2] license."  If you don't indicate a single choice of license, a
 *  recipient has the option to distribute your version of this file under
 *  either the CDDL, the GPL Version 2 or to extend the choice of license to
 *  its licensees as provided above.  However, if you add GPL Version 2 code
 *  and therefore, elected the GPL Version 2 license, then the option applies
 *  only if the new code is made subject to such option by the copyright
 *  holder.
 */

package org.glassfish.hk2.classmodel.reflect.impl;

import org.glassfish.hk2.classmodel.reflect.*;

import java.util.*;

/**
 * Results of a parsing activity, all java resources are inventoried in three
 * main categories : classes, interfaces and annotations with cross references
 *
 * @author Jerome Dochez
 */
public class TypesImpl implements Types, TypeBuilder {

    public TypesImpl() {
        System.out.println("TypesIMpl creation");
    }

    public Collection<Type> getAllTypes() {
        List<Type> allTypes = new ArrayList<Type>();
        for (TypeProxy typeProxy : map.values()) {
            if (typeProxy.get()!=null) {
                allTypes.add(typeProxy.get());
            }
        }
        return allTypes;
    }

    @Override
    public Type getBy(String name) {
        TypeProxy proxy = map.get(name);
        return (proxy!=null?proxy.get():null);
    }

    @Override
    public <T extends Type> T getBy(Class<T> type, String name) {
        Type t = getBy(name);
        try {
            return type.cast(t);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public ModelBuilder getModelBuilder(String name, String parentName) {
        ModelBuilder mb = new ModelBuilder(name, getHolder(name));
        mb.parent = getHolder(parentName);
        return mb;
    }

    public synchronized TypeImpl getType(int access, String name, TypeProxy parent) {
        TypeProxy typeProxy = getHolder(name);
        if (typeProxy.get()==null) {
            ModelBuilder mb = new ModelBuilder(name, typeProxy);
            mb.parent = parent;
            return mb.build(access);
        }
        return (TypeImpl) typeProxy.get();
    }

    public synchronized ClassModel getClassModel(String name) {
        ModelBuilder mb = new ModelBuilder(name, getHolder(name));
        return mb.buildClass();
    }

    public synchronized InterfaceModelImpl getInterface(String name) {
        ModelBuilder mb = new ModelBuilder(name, getHolder(name));
        return mb.buildInterface();
    }

    public synchronized AnnotationTypeImpl getAnnotation(String name) {
        ModelBuilder mb = new ModelBuilder(name, getHolder(name));
        return mb.buildAnnotation();
    }

    public FieldModelImpl getFieldModel(String name, TypeProxy type) {
        return new FieldModelImpl(new ModelBuilder(name, null), type);
    }

    public synchronized TypeProxy getHolder(String name) {
        TypeProxy typeProxy = map.get(name);
        if (typeProxy ==null) {
            typeProxy = new TypeProxy(null, name);
            map.put(name, typeProxy);
        }
        return typeProxy;
    }

    private final Map<String, TypeProxy> map = new HashMap<String, TypeProxy>();
}
