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
package org.jvnet.hk2.config.generator;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.istack.tools.APTTypeVisitor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.TypeVariable;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.type.WildcardType;

import java.util.Collection;

/**
 * Defines several type arithemetic operations.
 * @author Kohsuke Kawaguchi
 */
class TypeMath {
    protected final AnnotationProcessorEnvironment env;

    public TypeMath(AnnotationProcessorEnvironment env) {
        this.env = env;
    }

    TypeMirror isCollection(TypeMirror t) {
        TypeMirror collectionType = baseClassFinder.apply(t, env.getTypeDeclaration(Collection.class.getName()));
        if(collectionType!=null) {
            DeclaredType d = (DeclaredType)collectionType;
            return d.getActualTypeArguments().iterator().next();
        } else
            return null;
    }

    /**
     * Given a declaration X and mirror Y, finds the parameterization of Z=X&lt;...> such that
     * Y is assignable to Z.
     */
    static final APTTypeVisitor<TypeMirror,TypeDeclaration> baseClassFinder = new APTTypeVisitor<TypeMirror,TypeDeclaration>(){
        public TypeMirror onClassType(ClassType type, TypeDeclaration sup) {
            TypeMirror r = onDeclaredType(type,sup);
            if(r!=null)     return r;

            // otherwise recursively apply super class and base types
            if(type.getSuperclass()!=null) {
                r = onClassType(type.getSuperclass(),sup);
                if(r!=null)     return r;
            }

            return null;
        }

        protected TypeMirror onPrimitiveType(PrimitiveType type, TypeDeclaration param) {
            return null;
        }

        protected TypeMirror onVoidType(VoidType type, TypeDeclaration param) {
            return null;
        }

        public TypeMirror onInterfaceType(InterfaceType type, TypeDeclaration sup) {
            return onDeclaredType(type,sup);
        }

        private TypeMirror onDeclaredType(DeclaredType t, TypeDeclaration sup) {
            // t = sup<...>
            if(t.getDeclaration().equals(sup))
                return t;

            for(InterfaceType i : t.getSuperinterfaces()) {
                TypeMirror r = onInterfaceType(i,sup);
                if(r!=null)     return r;
            }

            return null;
        }

        public TypeMirror onTypeVariable(TypeVariable t, TypeDeclaration sup) {
            // we are checking if T (declared as T extends A&B&C) is assignable to sup.
            // so apply bounds recursively.
            for( ReferenceType r : t.getDeclaration().getBounds() ) {
                TypeMirror m = apply(r,sup);
                if(m!=null)     return m;
            }
            return null;
        }

        public TypeMirror onArrayType(ArrayType type, TypeDeclaration sup) {
            // we are checking if t=T[] is assignable to sup.
            // the only case this is allowed is sup=Object,
            // and Object isn't parameterized.
            return null;
        }

        public TypeMirror onWildcard(WildcardType type, TypeDeclaration sup) {
            // we are checking if T (= ? extends A&B&C) is assignable to sup.
            // so apply bounds recursively.
            for( ReferenceType r : type.getLowerBounds() ) {
                TypeMirror m = apply(r,sup);
                if(m!=null)     return m;
            }
            return null;
        }
    };

    /**
     * Adapts the string expression into the expression of the given type.
     */
    final APTTypeVisitor<JExpression,JExpression> SIMPLE_VALUE_CONVERTER = new APTTypeVisitor<JExpression,JExpression>() {
        protected JExpression onPrimitiveType(PrimitiveType p, JExpression param) {
            String kind = p.getKind().toString();
            return JExpr.invoke("as"+kind.charAt(0)+kind.substring(1).toLowerCase()).arg(param);
        }

        protected JExpression onClassType(ClassType type, JExpression param) {
            String qn = type.getDeclaration().getQualifiedName();
            if(qn.equals("java.lang.String"))
                return param;   // no conversion needed for string
            // return JExpr.invoke("as"+type.getDeclaration().getSimpleName()).arg(param);
            throw new UnsupportedOperationException();
        }

        protected JExpression onArrayType(ArrayType type, JExpression param) {
            throw new UnsupportedOperationException();
        }

        protected JExpression onInterfaceType(InterfaceType type, JExpression param) {
            throw new UnsupportedOperationException();
        }

        protected JExpression onTypeVariable(TypeVariable type, JExpression param) {
            throw new UnsupportedOperationException();
        }

        protected JExpression onVoidType(VoidType type, JExpression param) {
            throw new UnsupportedOperationException();
        }

        protected JExpression onWildcard(WildcardType type, JExpression param) {
            throw new UnsupportedOperationException();
        }
    };
}
