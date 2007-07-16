package org.jvnet.hk2.config.generator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import com.sun.istack.tools.APTTypeVisitor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
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

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * Represents configurable property of the component.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class Property {
    /**
     * Field/method declaration of this property.
     * Used to read annotations on this property.
     */
    abstract MemberDeclaration decl();

    /**
     * Default XML name of this property.
     * Unless overriden, this value is used as XML attribute/element name.
     */
    abstract String name();

    /**
     * The type of the property.
     * If the property is a collection property, this is the individual item type,
     * not the collection type as a whole.
     */
    abstract TypeMirror type();

    /**
     * Generates code to be executed right after the object
     * is created.
     */
    void pre(JCodeModel cm,JBlock block) {}
    abstract void assign(JVar $target, JBlock block, JExpression rhs);
    /**
     * Generates code to be executed when the end element
     * for the object is encountered, which is right before the
     * object is considererd fully parsed and returned.
     */
    void post(JCodeModel cm,JBlock block) {}

    <A extends Annotation> A getAnnotation(Class<A> a) {
        return decl().getAnnotation(a);
    }

    String inferName(String name) {
        if(name.length()==0)
            name = name();
        return name;
    }

    /**
     * Property that consists of a set/add method.
     */
    static final class Method extends Property {
        final MethodDeclaration decl;

        public Method(MethodDeclaration decl) {
            this.decl = decl;
        }

        MemberDeclaration decl() {
            return decl;
        }

        String name() {
            String name = decl.getSimpleName();
            if(name.startsWith("set") || name.startsWith("add")) // cut off the set prefix
                name = Introspector.decapitalize(name.substring(3));
            return name;
        }

        TypeMirror type() {
            return decl.getParameters().iterator().next().getType();
        }

        void assign(JVar $target, JBlock block, JExpression rhs) {
            block.invoke($target,decl.getSimpleName()).arg(rhs);
        }
    }

    static Property createField(AnnotationProcessorEnvironment env, FieldDeclaration decl) {
        TypeMirror t = baseClassFinder.apply(decl.getType(), env.getTypeDeclaration(Collection.class.getName()));
        if(t!=null) {
            DeclaredType d = (DeclaredType)t;
            return new ListField(decl,d.getActualTypeArguments().iterator().next());
        } else {
            return new Field(decl);
        }
    }

    /**
     * Property that consists of a field.
     */
    static final class Field extends Property {
        final FieldDeclaration decl;

        public Field(FieldDeclaration decl) {
            this.decl = decl;
        }

        MemberDeclaration decl() {
            return decl;
        }

        String name() {
            return decl.getSimpleName();
        }

        TypeMirror type() {
            return decl.getType();
        }

        void assign(JVar $target, JBlock block, JExpression rhs) {
            block.assign($target.ref(decl.getSimpleName()),rhs);
        }
    }

    /**
     * Field property of the type {@link List}.
     * This property can receive multiple values.
     */
    static final class ListField extends Property {
        final FieldDeclaration decl;
        final TypeMirror itemType;

        public ListField(FieldDeclaration decl, TypeMirror itemType) {
            this.decl = decl;
            this.itemType = itemType;
        }

        MemberDeclaration decl() {
            return decl;
        }

        String name() {
            return decl.getSimpleName();
        }

        TypeMirror type() {
            return itemType;
        }

        void assign(JVar $target, JBlock block, JExpression rhs) {
            block.invoke($target.ref(decl.getSimpleName()),"add").arg(rhs);
        }
    }

    ///**
    // * Handles array for both field and method.
    // * This class only takes care of the array handling, and the rest is delegated
    // * to {@link Field} or {@link Method}.
    // */
    //static final class Array extends Property {
    //    private final Property core;
    //    private final TypeMirror itemType;
    //
    //    public Array(Property core, TypeMirror itemType) {
    //        this.core = core;
    //        this.itemType = itemType;
    //    }
    //
    //    MemberDeclaration decl() {
    //        return core.decl();
    //    }
    //
    //    String name() {
    //        return core.name();
    //    }
    //
    //    TypeMirror type() {
    //        return itemType;
    //    }
    //
    //    void pre(JCodeModel cm, JBlock block) {
    //        block.decl(cm.ref(List.class), varName(), JExpr._new(cm.ref(ArrayList.class)));
    //    }
    //
    //    private String varName() {
    //        return name()+"Array";
    //    }
    //
    //    void assign(JVar $target, JBlock block, JExpression rhs) {
    //        block.invoke(JExpr.ref(varName()),"add").arg(rhs);
    //    }
    //
    //    void post(JCodeModel cm, JBlock block) {
    //        core.assign();
    //        // TODO
    //        super.post(cm, block);
    //    }
    //}

    private static final APTTypeVisitor<TypeMirror,TypeDeclaration> baseClassFinder = new APTTypeVisitor<TypeMirror,TypeDeclaration>(){
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
    } ;

}
