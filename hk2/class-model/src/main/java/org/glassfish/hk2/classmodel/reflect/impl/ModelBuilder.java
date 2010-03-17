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

/**
 * convenient class to build model implementations
 */
public class ModelBuilder {

    public final String name;
    public final TypeProxy sink;
    public TypeProxy parent;

    public interface ElementType {
        public TypeImpl make(ModelBuilder tb);
    }

    public enum types implements ElementType {
        Class {
            @Override
            public TypeImpl make(ModelBuilder tb) {
                return new ClassModelImpl(tb);
            }},
        Interface {
            @Override
            public TypeImpl make(ModelBuilder tb) {
                return new InterfaceModelImpl(tb);
            }},
        Annotation {
            @Override
            public TypeImpl make(ModelBuilder tb) {
                return new AnnotationModelImpl(tb);
            }}

    }    

    public ModelBuilder(String name, TypeProxy sink) {
        this.name = name;
        this.sink = sink;
    }

    public ModelBuilder setParent(TypeProxy parent) {
        this.parent = parent;
        return this;
    }

    public synchronized TypeImpl build(int access) {
        if ((access & Opcodes.ACC_ANNOTATION)==Opcodes.ACC_ANNOTATION) {
           return build(types.Annotation);
        } else
        if ((access & Opcodes.ACC_INTERFACE)==Opcodes.ACC_INTERFACE) {
            return build(types.Interface);
        } else {
            return build(types.Class);
        }

    }

    public InterfaceModelImpl buildInterface() {
        if (sink.get()==null) {
            return new InterfaceModelImpl(this);
        } else {
            return (InterfaceModelImpl) sink.get();
        }
    }

    public AnnotationModelImpl buildAnnotation() {
        if (sink.get()==null) {
            return new AnnotationModelImpl(this);
        } else {
            return (AnnotationModelImpl) sink.get();
        }
    }

    public ClassModel buildClass() {
        if (sink.get()==null) {
            return new ClassModelImpl(this);
        } else {
            return (ClassModel) sink.get();
        }
    }

    public TypeImpl build(ModelBuilder.ElementType type) {
        return type.make(this);
    }
}
