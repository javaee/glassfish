/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.enterprise.tools.apt;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import org.jvnet.hk2.annotations.InhabitantMetadata;
import org.jvnet.hk2.component.MultiMap;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Discoveres all {@link InhabitantMetadata} and puts them into the bag.
 *
 * @author Kohsuke Kawaguchi
 */
public class InhabitantMetadataProcessor extends TypeHierarchyVisitor<MultiMap<String,String>> {

    private final Map<AnnotationType,Model> models = new HashMap<AnnotationType, Model>();

    /**
     * For a particular {@link AnnotationType}, remember what properties are to be added as metadata.
     */
    private static final class Model {
        private final AnnotationType type;
        private final Map<AnnotationTypeElementDeclaration,String> metadataProperties = new HashMap<AnnotationTypeElementDeclaration, String>();

        public Model(AnnotationType type) {
            this.type = type;
            for (AnnotationTypeElementDeclaration e : type.getDeclaration().getMethods()) {
                InhabitantMetadata im = e.getAnnotation(InhabitantMetadata.class);
                if(im==null)    continue;

                String name = im.value();
                if(name.length()==0)    name=type.getDeclaration().getQualifiedName()+'.'+e.getSimpleName();

                metadataProperties.put(e,name);
            }
        }

        /**
         * Based on the model, parse the annotation mirror and updates the metadata bag by adding
         * discovered values.
         */
        public void parse(AnnotationMirror a, MultiMap<String,String> metadataBag) {
            assert a.getAnnotationType().equals(type);

            for (Map.Entry<AnnotationTypeElementDeclaration, String> e : metadataProperties.entrySet()) {
                Map<AnnotationTypeElementDeclaration, AnnotationValue> vals = a.getElementValues();
                AnnotationValue value = vals.get(e.getKey());
                if (value!=null) {
                    metadataBag.add(e.getValue(), toString(value));
                } else {
                    Collection<AnnotationTypeElementDeclaration> methods = 
                      a.getAnnotationType().getDeclaration().getMethods();
                    for (AnnotationTypeElementDeclaration decl : methods) {
                        if (e.getKey().equals(decl)) {
                            value = decl.getDefaultValue();
                            metadataBag.add(e.getValue(), toString(value));
                            break;
                        }
                    }
                }
            }
        }

        private String toString(AnnotationValue value) {
            if (value.getValue() instanceof TypeMirror) {
                TypeMirror tm = (TypeMirror) value.getValue();
                // TODO: needs to be more robust
                if (tm instanceof DeclaredType) {
                    DeclaredType dt = (DeclaredType) tm;
                    return getClassName(dt.getDeclaration());
                }
            }
            return value.toString();
        }

        /**
         * Returns the fully qualified class name.
         * The difference between this and {@link TypeDeclaration#getQualifiedName()}
         * is that this method returns the same format as {@link Class#getName()}.
         *
         * Notably, separator for nested classes is '$', not '.'
         */
        private String getClassName(TypeDeclaration d) {
            if(d.getDeclaringType()!=null)
                return getClassName(d.getDeclaringType())+'$'+d.getSimpleName();
            else
                return d.getQualifiedName();
        }
    }

    public MultiMap<String,String> process(TypeDeclaration d) {
        visited.clear();
        MultiMap<String,String> r = new MultiMap<String, String>();
        check(d,r);
        return r;
    }

    protected void check(TypeDeclaration d, MultiMap<String,String> result) {
        checkAnnotations(d, result);
        super.check(d,result);
    }

    private void checkAnnotations(TypeDeclaration d, MultiMap<String, String> result) {
        for (AnnotationMirror a : d.getAnnotationMirrors()) {
            getModel(a.getAnnotationType()).parse(a,result);
            // check meta-annotations
            for (AnnotationMirror b : a.getAnnotationType().getDeclaration().getAnnotationMirrors()) {
                getModel(b.getAnnotationType()).parse(b,result);
            }
        }
    }

    /**
     * Checks if the given annotation mirror has the given meta-annotation on it.
     */
    private boolean hasMetaAnnotation(AnnotationMirror a, Class<? extends Annotation> type) {
        return a.getAnnotationType().getDeclaration().getAnnotation(type)!=null;
    }

    private Model getModel(AnnotationType type) {
        Model model = models.get(type);
        if(model==null)
            models.put(type,model=new Model(type));
        return model;
    }
}
