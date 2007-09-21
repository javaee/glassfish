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
