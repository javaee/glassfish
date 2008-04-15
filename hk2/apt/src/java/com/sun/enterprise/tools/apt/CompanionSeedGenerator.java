/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.hk2.component.CompanionSeed;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import com.sun.tools.xjc.api.util.FilerCodeWriter;
import org.jvnet.hk2.annotations.CompanionOf;

import java.io.IOException;

/**
 * Generates {@link CompanionSeed} classes for each {@link CompanionOf} components.
 * @author Kohsuke Kawaguchi
 */
public class CompanionSeedGenerator extends SimpleDeclarationVisitor implements AnnotationProcessor {
    private final AnnotationProcessorEnvironment env;
    private JCodeModel cm;

    public CompanionSeedGenerator(AnnotationProcessorEnvironment env) {
        this.env = env;
    }

    public void process() {
        cm = new JCodeModel();

        AnnotationTypeDeclaration ann = (AnnotationTypeDeclaration) env.getTypeDeclaration(CompanionOf.class.getName());
        for(Declaration d : env.getDeclarationsAnnotatedWith(ann))
            d.accept(this);

        try {
            cm.build(new FilerCodeWriter(env.getFiler()));
        } catch (IOException e) {
            throw new Error(e);
        }
        cm = null;
    }

    /**
     * For each class annotated with {@link CompanionOf}.
     */
    public void visitClassDeclaration(ClassDeclaration clz) {
        try {
            CompanionOf companionOf = clz.getAnnotation(CompanionOf.class);
            String lead;
            try {
                companionOf.value();
                throw new AssertionError();
            } catch (MirroredTypeException e) {
                lead = e.getTypeMirror().toString();
            }

            JDefinedClass seed = cm._class(clz.getQualifiedName()+"Seed");
            JAnnotationUse a = seed.annotate(CompanionSeed.class);

            a.param("lead",cm.ref(lead));
            a.param("companion",cm.ref(clz.getQualifiedName()));
        } catch (JClassAlreadyExistsException e) {
            env.getMessager().printError(clz.getPosition(),e.toString());
        }
    }
}
