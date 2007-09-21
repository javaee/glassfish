package org.jvnet.hk2.config.generator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.type.TypeMirror;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
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
     * Name used as a seed for the XML name of this property.
     * This is the property name / field name.
     */
    abstract String seedName();

    /**
     * The type of the property.
     */
    abstract TypeMirror type();

    abstract void assign(JVar $target, JBlock block, JExpression rhs);

    <A extends Annotation> A getAnnotation(Class<A> a) {
        return decl().getAnnotation(a);
    }

    private Boolean isKey=null;
    
    /**
     * Does this property have {@link Attribute#key()} or {@link Element#key()}.
     */
    final boolean isKey() {
        if(isKey==null)
            isKey = _isKey();
        return isKey;
    }

    private boolean _isKey() {
        Element e = getAnnotation(Element.class);
        if(e!=null && e.key())  return true;

        Attribute a = getAnnotation(Attribute.class);
        if(a!=null && a.key())  return true;

        return false;
    }

    String inferName(String name) {
        if(name.length()==0)
            name = NAME_UTIL.toHyphenated(seedName());
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

        String seedName() {
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

        String seedName() {
            return decl.getSimpleName();
        }

        TypeMirror type() {
            return decl.getType();
        }

        void assign(JVar $target, JBlock block, JExpression rhs) {
            block.assign($target.ref(decl.getSimpleName()),rhs);
        }
    }

    private static final NameUtil NAME_UTIL = new NameUtil();
}
