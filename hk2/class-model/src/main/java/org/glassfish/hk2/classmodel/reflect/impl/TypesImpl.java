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
import org.objectweb.asm.Opcodes;

import java.net.URI;

/**
 * Results of a parsing activity, all java resources are inventoried in three
 * main categories : classes, interfaces and annotations with cross references
 *
 * @author Jerome Dochez
 */
public class TypesImpl implements TypeBuilder {

    final URI definingURI;
    final TypesCtr types;
    
    public TypesImpl(TypesCtr types, URI definingURI) {
        this.definingURI = definingURI;
        this.types = types;
    }

    public Class<? extends Type> getType(int access) {
        if ((access & Opcodes.ACC_ANNOTATION)==Opcodes.ACC_ANNOTATION) {
           return AnnotationType.class;
        } else
        if ((access & Opcodes.ACC_INTERFACE)==Opcodes.ACC_INTERFACE) {
            return InterfaceModel.class;
        } else {
            return ClassModel.class;
        }

    }

    public TypeImpl getType(int access, String name, TypeProxy parent) {
        Class<? extends Type> requestedType = getType(access);

        TypeProxy<Type> typeProxy = types.getHolder(name, requestedType);
        synchronized(typeProxy) {
            final Type type = typeProxy.get();
            if (null == type) {
                if ((access & Opcodes.ACC_ANNOTATION)==Opcodes.ACC_ANNOTATION) {
                   return new AnnotationTypeImpl(name, typeProxy);
                } else
                if ((access & Opcodes.ACC_INTERFACE)==Opcodes.ACC_INTERFACE) {
                    return new InterfaceModelImpl(name, typeProxy, parent);
                } else {
                    return new ClassModelImpl(name, typeProxy, parent);
                }
            } else {
                TypeImpl impl = (TypeImpl)type;
                if (ExtensibleTypeImpl.class.isInstance(impl)) {
                    // ensure we have the parent right
                    ((ExtensibleTypeImpl<?>)impl).setParent(parent);
                }
                return impl;
            }
        }
    }

    public FieldModelImpl getFieldModel(String name, TypeProxy type, ClassModel declaringType) {
        return new FieldModelImpl(name, type, declaringType);
    }

    @Override
    public TypeProxy getHolder(String name) {
        TypeProxy proxy = getHolder(name, InterfaceModel.class);
        if (proxy!=null) {
            return proxy;
        }
        proxy = getHolder(name, ClassModel.class);
        if (proxy!=null) {
            return proxy;
        }
        return getHolder(name, AnnotationType.class);
    }

    @Override
    public <T extends Type> TypeProxy<T> getHolder(String name, Class<T> type) {
        return (TypeProxy<T>) types.getHolder(name, type);
    }
}
